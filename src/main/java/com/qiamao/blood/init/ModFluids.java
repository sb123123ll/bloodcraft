package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ModFluids {

    public static final Fluid FLUID_BLOOD = new Fluid(
                "blood",
                new ResourceLocation(BloodMod.MODID, "blocks/blood_still"),
                new ResourceLocation(BloodMod.MODID, "blocks/blood_flow")
        ).setLuminosity(0)
         .setDensity(1000)  // 与水相同密度，确保浮力和游泳速度一致
         .setViscosity(1000) // 与水相同粘度，确保流动速度一致
         .setTemperature(300)
         .setFillSound(ModSounds.BUCKET_FILL_BLOOD)
         .setEmptySound(ModSounds.BUCKET_EMPTY_BLOOD);
         // 移除了 setColor，让其完全不透明，并保持贴图原本的颜色

    /**
     * 注册流体
     * 注意：流体注册必须在方块和物品注册之前（通常在 preInit 中）
     */
    public static void registerFluids() {
        FluidRegistry.registerFluid(FLUID_BLOOD);
        // 移除 FluidRegistry.addBucketForFluid，以防它自动注册通用桶，我们用自定义桶
    }
}
