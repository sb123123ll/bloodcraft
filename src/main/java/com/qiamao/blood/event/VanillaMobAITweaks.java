package com.qiamao.blood.event;

import com.qiamao.blood.entity.EntityCultistPreacher;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaMobAITweaks {

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        // 为村民添加躲避传教士的 AI
        if (event.getEntity() instanceof EntityVillager) {
            EntityVillager villager = (EntityVillager) event.getEntity();
            // 添加躲避 AI（类似于躲避僵尸）
            // 参数: 目标实体类型, 躲避距离, 远距离躲避速度, 近距离躲避速度
            villager.tasks.addTask(1, new EntityAIAvoidEntity<>(villager, EntityCultistPreacher.class, 8.0F, 0.6D, 0.6D));
        }

        // 为铁傀儡添加攻击传教士的 AI
        if (event.getEntity() instanceof EntityIronGolem) {
            EntityIronGolem golem = (EntityIronGolem) event.getEntity();
            // 参数: 目标实体类型, 是否检查视线, 是否只攻击附近的目标
            golem.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(golem, EntityCultistPreacher.class, false));
        }

        // 为雪傀儡添加攻击传教士的 AI
        if (event.getEntity() instanceof EntitySnowman) {
            EntitySnowman snowman = (EntitySnowman) event.getEntity();
            snowman.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(snowman, EntityCultistPreacher.class, false));
        }
    }
}
