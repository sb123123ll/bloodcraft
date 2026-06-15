package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockBloodStairs extends BlockStairs {
    public BlockBloodStairs(IBlockState modelState) {
        super(modelState);
        this.setUnlocalizedName("blood_stairs");
        this.setRegistryName(BloodMod.MODID, "blood_stairs");
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
        this.useNeighborBrightness = true;
    }
}
