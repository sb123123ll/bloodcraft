package com.qiamao.blood.block.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBloodAltar extends TileEntity {

    // ==========================================
    // 配方系统注册
    // ==========================================
    public static class AltarRecipe {
        public final net.minecraft.item.Item inputA;
        public final net.minecraft.item.Item inputB;
        public final ItemStack output;

        public AltarRecipe(net.minecraft.item.Item inputA, net.minecraft.item.Item inputB, ItemStack output) {
            this.inputA = inputA;
            this.inputB = inputB;
            this.output = output;
        }
    }

    public static final java.util.List<AltarRecipe> RECIPES = new java.util.ArrayList<>();

    static {
        // 在这里添加所有的自定义配方！
        // 示例：腐肉 (上方) + 骨头 (下方) -> 血腥糜烂的肉
        RECIPES.add(new AltarRecipe(
            net.minecraft.init.Items.ROTTEN_FLESH, 
            net.minecraft.init.Items.BONE, 
            new ItemStack(com.qiamao.blood.init.ModItems.GORY_FLESH, 1)
        ));
        
        // 如果有新的配方，只需要在这里继续 RECIPES.add(...) 即可！
    }
    // ==========================================

    private boolean isProcessing = false;

    // 0 = 输入, 1 = 燃料/辅助, 2 = 输出
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            // 当物品发生变化时，标记方块需要保存，并且尝试进行合成或修复
            markDirty();
            if (slot == 0 || slot == 1) {
                processCraftingOrRepair();
            }
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // 输出槽位不允许玩家手动放东西进去
            if (slot == 2) {
                return false;
            }
            // 恢复为什么都能放（只要不是输出槽）
            return super.isItemValid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            // 限制上面 (0) 和 下面 (1) 槽位最多只能放 1 个物品
            if (slot == 0 || slot == 1) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }
    };

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("items")) {
            itemHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("items", itemHandler.serializeNBT());
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(capability, facing);
    }

    // 获取背包以便在 Container 中绑定
    public ItemStackHandler getInventory() {
        return itemHandler;
    }

    // 瞬间合成或修复逻辑
    private void processCraftingOrRepair() {
        // 防止由于我们自己 setStackInSlot 导致的无限递归调用
        if (isProcessing || (world != null && world.isRemote)) {
            return;
        }

        // 优化：只有当输入槽位有变化且不为空时才尝试合成/修复
        ItemStack currentA = itemHandler.getStackInSlot(0);
        ItemStack currentB = itemHandler.getStackInSlot(1);
        if (currentA.isEmpty() || currentB.isEmpty()) {
            return;
        }

        isProcessing = true;
        try {
            boolean craftedAny = false;

            // 首先检查是否是修复毒刺之杖的特殊配方
            if (currentA.getItem() == com.qiamao.blood.init.ModItems.VENOMOUS_STINGER_STAFF && 
                currentB.getItem() == com.qiamao.blood.init.ModItems.STING_CORE_FRAGMENT) {
                
                ItemStack outputC = itemHandler.getStackInSlot(2);
                
                // 只有输出槽为空时才能进行修复
                if (outputC.isEmpty()) {
                    // 检查法杖是否有损耗
                    if (currentA.isItemDamaged()) {
                        // 消耗下面槽位的一个毒刺核心碎片
                        itemHandler.extractItem(1, 1, false);
                        
                        // 复制上面槽位的法杖
                        ItemStack repairedStaff = currentA.copy();
                        
                        // 计算修复量（总耐久的四分之一）
                        int maxDamage = repairedStaff.getMaxDamage();
                        int repairAmount = maxDamage / 4;
                        
                        // 设置新的损伤值，但不低于0
                        int newDamage = Math.max(0, repairedStaff.getItemDamage() - repairAmount);
                        repairedStaff.setItemDamage(newDamage);
                        
                        // 消耗上面槽位的法杖
                        itemHandler.extractItem(0, 1, false);
                        
                        // 将修复后的法杖放入输出槽
                        itemHandler.setStackInSlot(2, repairedStaff);
                        craftedAny = true;
                    }
                }
            } else {
                // 常规合成配方逻辑
                // 使用 while 循环：只要材料足够，瞬间把整叠物品全部转化完
                while (true) {
                    ItemStack inputA = itemHandler.getStackInSlot(0); // 上面槽位 (A)
                    ItemStack inputB = itemHandler.getStackInSlot(1); // 下面槽位 (B)
                    ItemStack outputC = itemHandler.getStackInSlot(2); // 输出槽位 (C)

                    // 只要有一个材料槽为空，就不可能合成，退出循环
                    if (inputA.isEmpty() || inputB.isEmpty()) {
                        break;
                    }

                    ItemStack result = ItemStack.EMPTY;

                    // 遍历配方列表，寻找匹配的合成
                    for (AltarRecipe recipe : RECIPES) {
                        if (inputA.getItem() == recipe.inputA && inputB.getItem() == recipe.inputB) {
                            result = recipe.output.copy();
                            break;
                        }
                    }

                    // 如果没有匹配到任何配方，退出循环
                    if (result.isEmpty()) {
                        break;
                    }

                    // 检查输出槽是否能放得下生成的物品（输出槽为空，或者物品种类相同且没达到最大堆叠数）
                    if (outputC.isEmpty() || (outputC.getItem() == result.getItem() && outputC.getCount() + result.getCount() <= outputC.getMaxStackSize())) {
                        
                        // 消耗材料 A 和 B (各消耗 1 个)
                        itemHandler.extractItem(0, 1, false);
                        itemHandler.extractItem(1, 1, false);

                        // 产出物品 C
                        if (outputC.isEmpty()) {
                            itemHandler.setStackInSlot(2, result);
                        } else {
                            outputC.grow(result.getCount());
                            itemHandler.setStackInSlot(2, outputC);
                        }
                        craftedAny = true;
                    } else {
                        // 输出槽满了，停止合成
                        break;
                    }
                }
            }

            if (craftedAny) {
                markDirty();
            }
        } finally {
            isProcessing = false;
        }
    }
}
