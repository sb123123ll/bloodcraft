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
    }

    private static final List<AltarRecipe> RECIPES = new ArrayList<>();

    /**
     * 注册一个新的祭坛配方
     */
    public static void addRecipe(Item inputA, Item inputB, ItemStack output) {
        RECIPES.add(new AltarRecipe(inputA, inputB, output));
    }

    /**
     * 获取所有已注册的配方
     */
    public static List<AltarRecipe> getRecipes() {
        return new ArrayList<>(RECIPES);
    }

    /**
     * 根据当前槽位物品查找匹配的配方
     */
    public static Optional<AltarRecipe> getMatchingRecipe(ItemStack slotA, ItemStack slotB) {
        return RECIPES.stream().filter(recipe -> recipe.matches(slotA, slotB)).findFirst();
    }
}
