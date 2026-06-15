package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;

public class BlockBloodSlabDouble extends BlockBloodSlab {
    public BlockBloodSlabDouble() {
        super();
        this.setUnlocalizedName("blood_slab");
        this.setRegistryName(BloodMod.MODID, "blood_slab_double");
    }

    @Override
    public boolean isDouble() {
        return true;
    }
}
