package com.qiamao.blood.block;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockBloodLog extends BlockRotatedPillar {

    @SuppressWarnings("null")
    public BlockBloodLog() {
        super(Material.WOOD);
        this.setHardness(2.0F); // 和原木一样的硬度
        this.setResistance(2.0F);
        this.setSoundType(SoundType.WOOD);
        this.setHarvestLevel("axe", 0); // 声明需要斧头挖掘
    }

}