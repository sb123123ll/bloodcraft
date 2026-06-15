package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockBloodPlanks extends Block {

    @SuppressWarnings("null")
    public BlockBloodPlanks() {
        super(Material.WOOD);
        this.setHardness(2.0F); // 和原版木板一样的硬度
        this.setResistance(2.0F);
        this.setSoundType(SoundType.WOOD); // 保持木头的声音特性
        this.setHarvestLevel("axe", 0); // 声明需要斧头挖掘
    }

}