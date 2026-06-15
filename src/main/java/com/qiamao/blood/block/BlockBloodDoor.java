package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Random;

public class BlockBloodDoor extends BlockDoor {
    private Item doorItem;

    public BlockBloodDoor(String name) {
        super(Material.WOOD);
        this.setUnlocalizedName(name);
        this.setRegistryName(BloodMod.MODID, name);
        this.setSoundType(SoundType.WOOD);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.disableStats();
    }

    public void setDoorItem(Item item) {
        this.doorItem = item;
    }

    @Override
    public Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? net.minecraft.init.Items.AIR : this.doorItem;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, net.minecraft.block.state.IBlockState state) {
        return new ItemStack(this.doorItem);
    }
}