package com.qiamao.blood.world;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 母体血巢战利品管理器
 * 负责管理结构内箱子的战利品分配
 * 设计为可扩展，方便其他模组添加自定义战利品
 */
public class BloodStructureLootManager {

    // 战利品提供者接口，允许其他模组添加自定义战利品
    public interface LootProvider {
        /**
         * 提供自定义战利品
         * @param world 世界
         * @param random 随机数生成器
         * @param chestIndex 箱子索引（用于区分不同的箱子）
         * @param totalChests 总箱子数
         * @return 战利品物品列表，返回null表示不添加战利品
         */
        List<ItemStack> provideLoot(World world, Random random, int chestIndex, int totalChests);
    }

    // 战利品提供者注册表
    private static final Map<ResourceLocation, LootProvider> lootProviders = new HashMap<>();

    /**
     * 注册战利品提供者
     * @param modId 模组ID
     * @param name 提供者名称
     * @param provider 战利品提供者
     */
    public static void registerLootProvider(String modId, String name, LootProvider provider) {
        lootProviders.put(new ResourceLocation(modId, name), provider);
    }

    /**
     * 移除战利品提供者
     * @param modId 模组ID
     * @param name 提供者名称
     */
    public static void unregisterLootProvider(String modId, String name) {
        lootProviders.remove(new ResourceLocation(modId, name));
    }

    /**
     * 为结构中的箱子分配战利品
     * @param world 世界
     * @param startPos 结构起始位置
     * @param widthX 结构X轴宽度
     * @param widthZ 结构Z轴宽度
     * @param height 结构高度
     * @param random 随机数生成器
     */
    public static void distributeLoot(World world, BlockPos startPos, int widthX, int widthZ, int height, Random random) {
        // 查找结构中的所有箱子
        List<TileEntityChest> chests = findChestsInStructure(world, startPos, widthX, widthZ, height);

        if (chests.isEmpty()) {
            return;
        }

        int totalChests = chests.size();

        // 为每个箱子分配战利品
        for (int i = 0; i < totalChests; i++) {
            TileEntityChest chest = chests.get(i);
            distributeLootToChest(world, chest, i, totalChests, random);
        }
    }

    /**
     * 查找结构中的所有箱子
     */
    private static List<TileEntityChest> findChestsInStructure(World world, BlockPos startPos, int widthX, int widthZ, int height) {
        List<TileEntityChest> chests = new ArrayList<>();

        for (int x = 0; x < widthX; x++) {
            for (int z = 0; z < widthZ; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = startPos.add(x, y, z);
                    if (world.getTileEntity(pos) instanceof TileEntityChest) {
                        chests.add((TileEntityChest) world.getTileEntity(pos));
                    }
                }
            }
        }

        return chests;
    }

    /**
     * 为单个箱子分配战利品
     */
    private static void distributeLootToChest(World world, TileEntityChest chest, int chestIndex, int totalChests, Random random) {
        // 清空箱子原有内容
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            chest.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        // 规则1：所有箱子都会刷新5到10个熟肉
        addCookedMeat(chest, random);

        // 规则2：绝对会在一个箱子里生成一个附魔金苹果
        // 规则3：最多有两个箱子会出现，第二个箱子也出现附魔金苹果的概率是10%
        addEnchantedGoldenApple(chest, chestIndex, totalChests, random);

        // 规则4：有且只有一个箱子会刷新模组物品（煮熟的烂肉）10-20个
        addModItem(chest, chestIndex, totalChests, random);

        // 规则5：每个遗迹有15%概率刷新钻石套任意一个部位任意两个有效附魔
        // 这个规则只在第一个箱子应用
        if (chestIndex == 0) {
            addDiamondArmor(chest, random);
        }

        // 调用其他模组的战利品提供者
        for (LootProvider provider : lootProviders.values()) {
            List<ItemStack> customLoot = provider.provideLoot(world, random, chestIndex, totalChests);
            if (customLoot != null && !customLoot.isEmpty()) {
                for (ItemStack stack : customLoot) {
                    addItemToChest(chest, stack);
                }
            }
        }
    }

    /**
     * 添加熟肉（5-10个）
     * 一个箱子所有肉加起来5到10个，不是一种5到10个
     * 每个槽位最多3个，随机分散放置
     */
    private static void addCookedMeat(TileEntityChest chest, Random random) {
        int totalMeat = 5 + random.nextInt(6); // 5-10个

        ItemStack[] cookedMeats = {
            new ItemStack(Items.COOKED_CHICKEN),
            new ItemStack(Items.COOKED_PORKCHOP),
            new ItemStack(Items.COOKED_BEEF),
            new ItemStack(Items.COOKED_MUTTON)
        };

        for (int i = 0; i < totalMeat; i++) {
            ItemStack meat = cookedMeats[random.nextInt(cookedMeats.length)].copy();
            meat.setCount(1);
            addItemToChestScattered(chest, meat, random);
        }
    }

    /**
     * 添加附魔金苹果
     * 绝对会在一个箱子里生成一个
     * 最多有两个箱子会出现，第二个箱子也出现的概率是10%
     */
    private static void addEnchantedGoldenApple(TileEntityChest chest, int chestIndex, int totalChests, Random random) {
        if (chestIndex == 0) {
            // 第一个箱子必定有
            ItemStack apple = new ItemStack(Items.GOLDEN_APPLE);
            EnchantmentHelper.addRandomEnchantment(random, apple, 30, true);
            addItemToChestScattered(chest, apple, random);
        } else if (chestIndex == 1 && random.nextFloat() < 0.1f) {
            // 第二个箱子10%概率有
            ItemStack apple = new ItemStack(Items.GOLDEN_APPLE);
            EnchantmentHelper.addRandomEnchantment(random, apple, 30, true);
            addItemToChestScattered(chest, apple, random);
        }
    }

    /**
     * 添加模组物品（煮熟的烂肉）
     * 有且只有一个箱子会刷新，10-20个
     */
    private static void addModItem(TileEntityChest chest, int chestIndex, int totalChests, Random random) {
        // 只在随机选择的一个箱子中添加
        int specialChestIndex = random.nextInt(totalChests);
        if (chestIndex == specialChestIndex) {
            // 尝试获取模组的煮熟的烂肉
            ItemStack modItem = getCookedRottenFlesh();
            if (modItem != null) {
                int count = 10 + random.nextInt(11); // 10-20个
                modItem.setCount(count);
                // 分散放置，每个槽位最多3个
                addItemToChestScattered(chest, modItem, random);
            }
        }
    }

    /**
     * 获取煮熟的烂肉（模组物品）
     * 如果模组不存在，返回null
     * 其他模组可以通过注册LootProvider来自定义此物品
     */
    private static ItemStack getCookedRottenFlesh() {
        // 暂时返回null，待模组物品确定后实现
        // 其他模组可以通过registerLootProvider添加自定义物品
        return null;
    }

    /**
     * 添加钻石套
     * 每个遗迹有15%概率刷新钻石套任意一个部位任意两个有效附魔
     */
    private static void addDiamondArmor(TileEntityChest chest, Random random) {
        if (random.nextFloat() < 0.15f) {
            // 15%概率
            ItemStack[] diamondArmor = {
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS)
            };

            ItemStack armorPiece = diamondArmor[random.nextInt(diamondArmor.length)].copy();

            // 添加两个有效附魔
            addRandomValidEnchantment(armorPiece, random);
            addRandomValidEnchantment(armorPiece, random);

            addItemToChestScattered(chest, armorPiece, random);
        }
    }

    /**
     * 添加随机有效附魔
     */
    private static void addRandomValidEnchantment(ItemStack stack, Random random) {
        List<Enchantment> validEnchantments = new ArrayList<>();

        // 获取物品支持的所有附魔
        for (Enchantment enchantment : Enchantment.REGISTRY) {
            if (enchantment != null && enchantment.canApply(stack)) {
                validEnchantments.add(enchantment);
            }
        }

        if (!validEnchantments.isEmpty()) {
            Enchantment enchantment = validEnchantments.get(random.nextInt(validEnchantments.size()));
            int level = enchantment.getMinLevel() + random.nextInt(enchantment.getMaxLevel() - enchantment.getMinLevel() + 1);
            stack.addEnchantment(enchantment, level);
        }
    }

    /**
     * 添加物品到箱子（随机分散放置，每个槽位最多3个）
     */
    private static void addItemToChestScattered(TileEntityChest chest, ItemStack stack, Random random) {
        int remainingCount = stack.getCount();
        int maxPerSlot = 3; // 每个槽位最多3个

        while (remainingCount > 0) {
            // 随机选择一个槽位
            int slot = random.nextInt(chest.getSizeInventory());
            ItemStack slotStack = chest.getStackInSlot(slot);

            if (slotStack.isEmpty()) {
                // 空槽位，直接放置
                int count = Math.min(remainingCount, maxPerSlot);
                ItemStack newStack = stack.copy();
                newStack.setCount(count);
                chest.setInventorySlotContents(slot, newStack);
                remainingCount -= count;
            } else if (slotStack.getItem() == stack.getItem() &&
                       slotStack.getItemDamage() == stack.getItemDamage() &&
                       ItemStack.areItemStackTagsEqual(slotStack, stack)) {
                // 相同物品，尝试堆叠
                int currentCount = slotStack.getCount();
                if (currentCount < maxPerSlot) {
                    int space = maxPerSlot - currentCount;
                    int add = Math.min(remainingCount, space);
                    slotStack.grow(add);
                    remainingCount -= add;
                }
            }

            // 如果无法放置，尝试下一个槽位
            // 防止无限循环，最多尝试箱子大小的次数
            int attempts = 0;
            while (remainingCount > 0 && attempts < chest.getSizeInventory()) {
                slot = random.nextInt(chest.getSizeInventory());
                slotStack = chest.getStackInSlot(slot);

                if (slotStack.isEmpty()) {
                    int count = Math.min(remainingCount, maxPerSlot);
                    ItemStack newStack = stack.copy();
                    newStack.setCount(count);
                    chest.setInventorySlotContents(slot, newStack);
                    remainingCount -= count;
                    break;
                } else if (slotStack.getItem() == stack.getItem() &&
                           slotStack.getItemDamage() == stack.getItemDamage() &&
                           ItemStack.areItemStackTagsEqual(slotStack, stack)) {
                    int currentCount = slotStack.getCount();
                    if (currentCount < maxPerSlot) {
                        int space = maxPerSlot - currentCount;
                        int add = Math.min(remainingCount, space);
                        slotStack.grow(add);
                        remainingCount -= add;
                        break;
                    }
                }
                attempts++;
            }

            // 如果还是无法放置，丢弃剩余物品
            if (remainingCount > 0 && attempts >= chest.getSizeInventory()) {
                break;
            }
        }
    }

    /**
     * 添加物品到箱子（旧方法，保留用于兼容性）
     */
    private static void addItemToChest(TileEntityChest chest, ItemStack stack) {
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack slotStack = chest.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                chest.setInventorySlotContents(i, stack);
                return;
            } else if (slotStack.getItem() == stack.getItem() &&
                       slotStack.getItemDamage() == stack.getItemDamage() &&
                       ItemStack.areItemStackTagsEqual(slotStack, stack)) {
                // 可以合并
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space >= stack.getCount()) {
                    slotStack.grow(stack.getCount());
                    return;
                } else if (space > 0) {
                    slotStack.grow(space);
                    stack.shrink(space);
                    // 继续尝试放入剩余部分
                }
            }
        }
        // 如果箱子满了，丢弃多余物品
    }
}
