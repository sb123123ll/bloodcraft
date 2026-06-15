package com.qiamao.blood.entity.ai;

import com.qiamao.blood.entity.EntityBloodMother;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * 血液母体冲撞AI
 * 当目标较远时，静止1.5秒后以10倍速度冲撞
 */
public class EntityAIBloodMotherCharge extends EntityAIBase {
    
    private final EntityBloodMother mother;
    private final double chargeSpeedMultiplier = 13.0D; // 冲撞速度倍数（提高30%，原本是10.0D）
    private final int prepareTime = 30; // 准备时间1.5秒（30 ticks）
    private final int cooldownTime = 300; // 冷却时间15秒（300 ticks）
    private final double chargeDamage = 10.0D; // 冲撞伤害
    private int chargeDuration = 0; // 冲撞持续时间计数器
    private boolean hasHitTarget = false; // 是否已经冲撞到目标
    private int postHitDuration = 0; // 冲撞到目标后的滑行时间
    
    public EntityAIBloodMotherCharge(EntityBloodMother mother) {
        this.mother = mother;
        this.setMutexBits(3); // 与移动和攻击AI互斥
    }

    @Override
    public boolean shouldExecute() {
        // 没有目标不执行
        EntityLivingBase target = mother.getAttackTarget();
        if (target == null || !target.isEntityAlive()) {
            return false;
        }
        
        // 已经在冲撞中不执行
        if (mother.isCharging()) {
            return false;
        }
        
        // 已经在准备中不执行
        if (mother.getChargePrepareTime() > 0) {
            return false;
        }
        
        // 计算距离
        double distance = mother.getDistanceSq(target);
        
        // 冷却结束后，只要在索敌范围内就自动准备冲撞
        // 距离太近时使用普通近战攻击
        return distance > 16.0D && distance < 625.0D; // 25^2 = 625
    }

    @Override
    public void startExecuting() {
        mother.setChargePrepareTime(prepareTime);
        mother.getNavigator().clearPath(); // 停止移动，准备冲撞
    }

    @Override
    public boolean shouldContinueExecuting() {
        // 如果在准备阶段，检查目标是否还在范围内
        if (mother.getChargePrepareTime() > 0) {
            EntityLivingBase target = mother.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
                return false;
            }
            double distance = mother.getDistanceSq(target);
            return distance < 625.0D; // 目标仍在25格范围内
        }
        
        // 冲撞阶段继续执行
        return mother.isCharging();
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = mother.getAttackTarget();
        if (target == null) {
            resetTask();
            return;
        }
        
        // 准备阶段：实时对准玩家位置
        if (mother.getChargePrepareTime() > 0) {
            // 面向目标
            mother.getLookHelper().setLookPosition(target.posX, target.posY + target.getEyeHeight(), target.posZ, 30.0F, 30.0F);
            
            // 减少准备时间
            mother.setChargePrepareTime(mother.getChargePrepareTime() - 1);
            
            // 准备完成，开始冲撞
            if (mother.getChargePrepareTime() <= 0) {
                startCharge(target);
            }
        }
        
        // 冲撞阶段
        if (mother.isCharging()) {
            // 持续朝目标方向移动
            double dx = target.posX - mother.posX;
            double dz = target.posZ - mother.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > 0.1D) {
                // 归一化方向并应用冲撞速度
                double speed = mother.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * chargeSpeedMultiplier;
                mother.motionX = (dx / distance) * speed;
                mother.motionZ = (dz / distance) * speed;
                
                // 设置实体朝向为移动方向，避免屁股往前
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                mother.rotationYaw = yaw;
                mother.renderYawOffset = yaw;
            }
            
            // 优化：每隔2个tick检测一次范围内的生物碰撞，或者降低扩大的AABB范围
            // 缩小AABB拓展范围至1.5格（原来是3.0D），以减少扫描数量
            if (mother.ticksExisted % 2 == 0) {
                java.util.List<net.minecraft.entity.Entity> entities = mother.world.getEntitiesWithinAABBExcludingEntity(mother, mother.getEntityBoundingBox().grow(1.5D, 1.0D, 1.5D));
                for (net.minecraft.entity.Entity entity : entities) {
                    if (entity instanceof EntityLivingBase && entity != mother) {
                        EntityLivingBase living = (EntityLivingBase) entity;
                        if (living.hurtResistantTime <= 0) {
                            living.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(mother), (float)chargeDamage);
                            hasHitTarget = true;
                        }
                    }
                }
            }
            
            // 冲撞逻辑
            if (hasHitTarget) {
                // 已经冲撞到目标，继续滑行15 ticks（约0.75秒）
                postHitDuration++;
                if (postHitDuration >= 15) {
                    endCharge();
                    // 设置冷却
                    mother.setChargeCooldown(cooldownTime);
                    // 启动血螨喷射（2秒后开始）
                    mother.startMiteSpray(40); // 40 ticks = 2秒
                }
            } else {
                // 还没冲撞到目标，按原定时间结束
                chargeDuration--;
                if (chargeDuration <= 0) {
                    endCharge();
                    // 设置冷却
                    mother.setChargeCooldown(cooldownTime);
                    // 启动血螨喷射（2秒后开始）
                    mother.startMiteSpray(40); // 40 ticks = 2秒
                }
            }
        }
    }
    
    private void startCharge(EntityLivingBase target) {
        mother.setCharging(true);
        chargeDuration = 60; // 冲撞持续3秒（60 ticks），增加冲撞距离
        hasHitTarget = false;
        postHitDuration = 0;
        // 设置stepHeight为2.0，使其能够越过栅栏等1.5格高度的障碍
        mother.stepHeight = 2.0F;
        // 播放冲撞开始音效（可以复用受伤音效或添加新音效）
        mother.playSound(com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_HURT, 1.0F, 0.8F);
    }
    
    private void endCharge() {
        mother.setCharging(false);
        // 清零速度，避免抖动
        mother.motionX = 0;
        mother.motionZ = 0;
        // 恢复stepHeight为默认值
        mother.stepHeight = 0.6F;
    }

    @Override
    public void resetTask() {
        if (mother.isCharging()) {
            endCharge();
        }
        mother.setChargePrepareTime(0);
    }
}
