package com.qiamao.blood.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.qiamao.blood.block.tileentity.TileEntityBloodAltar;

public class ModGuiHandler implements IGuiHandler {

    public static final int BLOOD_ALTAR_GUI = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BLOOD_ALTAR_GUI) {
            net.minecraft.tileentity.TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityBloodAltar) {
                return new ContainerBloodAltar(player.inventory, (TileEntityBloodAltar) te);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BLOOD_ALTAR_GUI) {
            net.minecraft.tileentity.TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityBloodAltar) {
                return new GuiBloodAltar(player.inventory, (TileEntityBloodAltar) te);
            }
        }
        return null;
    }
}
