package com.qiamao.blood.block;

import com.qiamao.blood.BloodCreativeTab;
import com.qiamao.blood.BloodMod;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;

public class BlockBloodFenceGate extends BlockFenceGate {
    public BlockBloodFenceGate(String name) {
        super(BlockPlanks.EnumType.OAK); // EnumType mostly for MapColor, not heavily used
        this.setUnlocalizedName(name);
        this.setRegistryName(BloodMod.MODID, name);
        this.setCreativeTab(BloodCreativeTab.INSTANCE);
        this.setSoundType(SoundType.WOOD);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
    }
}