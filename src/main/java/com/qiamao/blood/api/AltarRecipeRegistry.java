package com.qiamao.blood.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 血液祭坛的合成配方注册表（软编码）
 * 允许其他模组或脚本通过此 API 添加、移除或查询配方
 */
public class AltarRecipeRegistry {

    public static class AltarRecipe {
        private final Item inputA;
        private final Item inputB;
        private final ItemStack output;

        public AltarRecipe(Item inputA, Item inputB, ItemStack output) {
            this.inputA = inputA;
            this.inputB = inputB;
            this.output = output.copy();
        }

        public Item getInputA() { return inputA; }
        public Item getInputB() { return inputB; }
        public ItemStack getOutput() { return output.copy(); }

        public boolean matches(ItemStack slotA, ItemStack slotB) {
            if (slotA.isEmpty() || slotB.isEmpty()) return false;
            // 允许 A 和 B 位置互换
            return (slotA.getItem() == inputA && slotB.getItem() == inputB) ||
                   (slotA.getItem() == inputB && slotB.getItem() == inputA);
        }
        
        /**
         * 允许配方自定义如何处理输出结果，例如用于修复装备。
         * 默认返回预设的 output 副本。
         */
        public ItemStack getCraftingResult(ItemStack slotA, ItemStack slotB) {
            return getOutput();
        }
    }

    /**
     * 支持自定义处理逻辑的配方接口
     */
    public interface IAltarRecipeHandler {
        boolean matches(ItemStack slotA, ItemStack slotB);
        ItemStack getCraftingResult(ItemStack slotA, ItemStack slotB);
    }

    /**
     * 包装传统配方以支持新接口
     */
    public static class AltarRecipeWrapper implements IAltarRecipeHandler {
        private final AltarRecipe recipe;
        public AltarRecipeWrapper(AltarRecipe recipe) { this.recipe = recipe; }
        @Override public boolean matches(ItemStack slotA, ItemStack slotB) { return recipe.matches(slotA, slotB); }
        @Override public ItemStack getCraftingResult(ItemStack slotA, ItemStack slotB) { return recipe.getCraftingResult(slotA, slotB); }
    }

    private static final List<IAltarRecipeHandler> RECIPE_HANDLERS = new ArrayList<>();

    /**
     * 注册一个新的祭坛配方
     */
    public static void addRecipe(Item inputA, Item inputB, ItemStack output) {
        RECIPE_HANDLERS.add(new AltarRecipeWrapper(new AltarRecipe(inputA, inputB, output)));
    }

    /**
     * 注册一个自定义逻辑的配方
     */
    public static void addCustomRecipe(IAltarRecipeHandler handler) {
        RECIPE_HANDLERS.add(handler);
    }

    /**
     * 获取所有已注册的普通配方（向下兼容，不含自定义配方）
     */
    public static List<AltarRecipe> getRecipes() {
        List<AltarRecipe> list = new ArrayList<>();
        for (IAltarRecipeHandler handler : RECIPE_HANDLERS) {
            if (handler instanceof AltarRecipeWrapper) {
                list.add(((AltarRecipeWrapper) handler).recipe);
            }
        }
        return list;
    }

    /**
     * 根据当前槽位物品查找匹配的配方
     */
    public static Optional<IAltarRecipeHandler> getMatchingRecipe(ItemStack slotA, ItemStack slotB) {
        return RECIPE_HANDLERS.stream().filter(handler -> handler.matches(slotA, slotB)).findFirst();
    }
}
