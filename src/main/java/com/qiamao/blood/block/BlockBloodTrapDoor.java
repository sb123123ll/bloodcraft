package com.qiamao.blood.block;

import com.qiamao.blood.BloodCreativeTab;
import com.qiamao.blood.BloodMod;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockBloodTrapDoor extends BlockTrapDoor {
    public BlockBloodTrapDoor(String name) {
        super(Material.WOOD);
        this.setUnlocalizedName(name);
        this.setRegistryName(BloodMod.MODID, name);
        this.setCreativeTab(BloodCreativeTab.INSTANCE);
        this.setSoundType(SoundType.WOOD);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
    }
}