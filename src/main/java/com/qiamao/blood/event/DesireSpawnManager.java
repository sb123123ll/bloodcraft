package com.qiamao.blood.event;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * 欲望事件生物生成管理器
 * 负责在欲望事件期间提升特定生物的生成概率
 */
public class DesireSpawnManager {
    
    private static final Random RANDOM = new Random();
    
    /**
     * 检查生物生成事件，在欲望事件期间提升生成概率
     */
    @SubscribeEvent
    public void onEntitySpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        
        // 只在主世界处理
        if (world.provider.getDimension() != 0) {
            return;
        }
        
        // 检查是否为欲望事件期间
        if (!DesireEventHandler.isDesireEventActive(world)) {
            return;
        }
        
        // 检查是否为指定的生物类型
        if (isTargetEntity(event.getEntity())) {
            // 使用配置文件的概率允许生成（即使原本可能被拒绝）
            if (RANDOM.nextDouble() < com.qiamao.blood.config.DesireEventConfig.getSpawnBoostProbability()) {
                event.setResult(LivingSpawnEvent.Result.ALLOW);
            }
        }
    }
    
    /**
     * 检查是否为目标生物（通过接口或特定类判断）
     */
    private boolean isTargetEntity(Entity entity) {
        return entity instanceof com.qiamao.blood.entity.EntityBloodSeeker || 
               entity instanceof com.qiamao.blood.entity.EntityParasiticSteve;
    }
    
    /**
     * 注册生成管理器
     */
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new DesireSpawnManager());
    }
}
