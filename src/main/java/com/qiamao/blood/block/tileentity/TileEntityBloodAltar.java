package com.qiamao.blood.block.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBloodAltar extends TileEntity implements net.minecraft.inventory.ISidedInventory {

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
            if (facing == EnumFacing.UP) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new SidedInvWrapper(this, EnumFacing.UP));
            } else if (facing == EnumFacing.DOWN) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new SidedInvWrapper(this, EnumFacing.DOWN));
            } else if (facing != null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new SidedInvWrapper(this, facing));
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(capability, facing);
    }

    // ==========================================
    // ISidedInventory 实现 (供原版漏斗和面包装器使用)
    // ==========================================
    private static final int[] SLOTS_TOP = new int[] {0};
    private static final int[] SLOTS_BOTTOM = new int[] {2};
    private static final int[] SLOTS_SIDES = new int[] {1};

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return SLOTS_BOTTOM; // 底部只能访问输出槽
        } else if (side == EnumFacing.UP) {
            return SLOTS_TOP; // 顶部只能访问上方输入槽
        } else {
            return SLOTS_SIDES; // 侧面只能访问下方输入槽
        }
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (direction == EnumFacing.DOWN && index == 2) {
            return true; // 只能从底部抽出输出槽的物品
        }
        return false; // 其他面不允许抽出
    }

    @Override
    public int getSizeInventory() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return itemHandler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = itemHandler.getStackInSlot(index);
        itemHandler.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(net.minecraft.entity.player.EntityPlayer player) {}

    @Override
    public void closeInventory(net.minecraft.entity.player.EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return itemHandler.isItemValid(index, stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public String getName() {
        return "container.blood_altar";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.TextComponentTranslation(this.getName());
    }

    // ==========================================
    // 逻辑部分
    // ==========================================

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
                    java.util.Optional<com.qiamao.blood.api.AltarRecipeRegistry.AltarRecipe> matchedRecipe = com.qiamao.blood.api.AltarRecipeRegistry.getMatchingRecipe(inputA, inputB);
                    if (matchedRecipe.isPresent()) {
                        result = matchedRecipe.get().getOutput();
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
