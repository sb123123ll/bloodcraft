package com.qiamao.blood.block;

import com.qiamao.blood.BloodCreativeTab;
import com.qiamao.blood.BloodMod;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockBloodFence extends BlockFence {
    public BlockBloodFence(String name) {
        super(Material.WOOD, MapColor.NETHERRACK);
        this.setUnlocalizedName(name);
        this.setRegistryName(BloodMod.MODID, name);
        this.setCreativeTab(BloodCreativeTab.INSTANCE);
        this.setSoundType(SoundType.WOOD);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
    }
}