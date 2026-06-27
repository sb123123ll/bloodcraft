package com.qiamao.blood.event;

import com.qiamao.blood.entity.EntityCultistPreacher;
import com.qiamao.blood.block.BlockBloodSeekerFlesh;
import com.qiamao.blood.entity.EntityBloodHound;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityWolf && !(event.getEntityLiving() instanceof EntityBloodHound)) {
            EntityWolf wolf = (EntityWolf) event.getEntityLiving();
            if (!wolf.isChild()) {
                // 检查狼脚下的方块
                BlockPos pos = new BlockPos(wolf.posX, wolf.getEntityBoundingBox().minY - 0.1, wolf.posZ);
                IBlockState state = wolf.world.getBlockState(pos);
                if (state.getBlock() instanceof BlockBloodSeekerFlesh) {
                    // 禁锢：重置水平速度
                    wolf.motionX = 0;
                    wolf.motionZ = 0;
                    // 防止狼跳跃逃脱
                    wolf.setJumping(false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        
        if (event.getEntityLiving() instanceof EntityWolf && !(event.getEntityLiving() instanceof EntityBloodHound)) {
            EntityWolf wolf = (EntityWolf) event.getEntityLiving();
            if (!wolf.isChild()) {
                BlockPos pos = new BlockPos(wolf.posX, wolf.getEntityBoundingBox().minY - 0.1, wolf.posZ);
                IBlockState state = wolf.world.getBlockState(pos);
                if (state.getBlock() instanceof BlockBloodSeekerFlesh) {
                    // 取消死亡事件
                    event.setCanceled(true);
                    
                    // 将原版狼设置为已死，防止掉落物品和经验
                    wolf.setDead();

                    // 生成血液猎犬
                    EntityBloodHound hound = new EntityBloodHound(wolf.world);
                    hound.copyLocationAndAnglesFrom(wolf);
                    hound.renderYawOffset = wolf.renderYawOffset;
                    hound.rotationYawHead = wolf.rotationYawHead;

                    // 继承驯服状态和主人
                    if (wolf.isTamed()) {
                        hound.setTamed(true);
                        hound.setOwnerId(wolf.getOwnerId());
                        hound.setSitting(wolf.isSitting());
                    }
                    
                    // 继承血量上限
                    double maxHealth = wolf.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
                    hound.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
                    
                    // 变换后血量回满
                    hound.setHealth(hound.getMaxHealth());
                    
                    // 继承自定义名字
                    if (wolf.hasCustomName()) {
                        hound.setCustomNameTag(wolf.getCustomNameTag());
                        hound.setAlwaysRenderNameTag(wolf.getAlwaysRenderNameTag());
                    }

                    wolf.world.spawnEntity(hound);

                    // 触发粒子效果，通过发包或者设状态
                    wolf.world.setEntityState(hound, (byte) 101); // 自定义状态码 101 代表狼变身
                }
            }
        }
    }
}
