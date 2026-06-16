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

    /**
     * 将任务调度到对应端的主线程
     * 服务端在服务端线程执行，客户端在客户端线程执行
     */
    public void addScheduledTask(Runnable runnable) {
        // 服务端默认直接由 FMLCommonHandler 或者不执行处理（通常这类同步包只发给客户端）
        // 如果需要服务端处理网络包的，可在这里调用 FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable)
        // 但此方法主要为客户端网络包修复崩溃使用
    }
}
