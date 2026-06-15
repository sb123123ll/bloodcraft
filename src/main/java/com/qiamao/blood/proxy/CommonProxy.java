package com.qiamao.blood.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void spawnBloodDropParticle(net.minecraft.world.World world, double x, double y, double z, double speedX, double speedY, double speedZ) {
        // 服务端不执行任何操作
    }
}
