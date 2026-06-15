package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;

public class BlockBloodSlabHalf extends BlockBloodSlab {
    public BlockBloodSlabHalf() {
        super();
        this.setUnlocalizedName("blood_slab");
        this.setRegistryName(BloodMod.MODID, "blood_slab_half");
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
    }

    @Override
    public boolean isDouble() {
        return false;
    }
}
