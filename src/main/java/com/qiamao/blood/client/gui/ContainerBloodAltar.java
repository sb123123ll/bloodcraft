package com.qiamao.blood.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import com.qiamao.blood.block.tileentity.TileEntityBloodAltar;

public class ContainerBloodAltar extends Container {

    private final TileEntityBloodAltar tileEntity;

    public ContainerBloodAltar(InventoryPlayer playerInventory, TileEntityBloodAltar tileEntity) {
        this.tileEntity = tileEntity;
        IItemHandler inventory = tileEntity.getInventory();

        // 绑定机器自身的槽位 (对应我们在贴图上画的位置)
        // 0: 输入槽
        this.addSlotToContainer(new SlotItemHandler(inventory, 0, 56, 17));
        // 1: 辅助/燃料槽
        this.addSlotToContainer(new SlotItemHandler(inventory, 1, 56, 53));
        // 2: 输出槽 (自定义一个 SlotItemHandler 禁止放入)
        this.addSlotToContainer(new SlotItemHandler(inventory, 2, 116, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false; // 玩家不能往输出槽放东西
            }
        });

        // 绑定玩家背包 (1-27)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 绑定玩家快捷栏 (0-8)
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(tileEntity.getPos().add(0.5, 0.5, 0.5)) <= 64.0D;
    }

    // Shift + 点击物品时的转移逻辑
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            // 如果点击的是机器里的槽位 (0, 1, 2)
            if (index < 3) {
                // 尝试转移到玩家背包 (3-38)
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } 
            // 如果点击的是玩家背包里的物品，尝试转移到输入槽 (0)
            else if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}
