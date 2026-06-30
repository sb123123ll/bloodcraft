package com.qiamao.blood.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class EntityBloodNematode extends EntityMob {

    private static final DataParameter<Boolean> IS_SUCKING_BLOOD = EntityDataManager.createKey(EntityBloodNematode.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SUCK_TARGET_ID = EntityDataManager.createKey(EntityBloodNematode.class, DataSerializers.VARINT);

    private int suckTimer = 0;
    private int suckCooldown = 0;

    public EntityBloodNematode(World worldIn) {
        super(worldIn);
        // 原尺寸为 0.6F, 0.3F。现在改为原来的 60%
        // 0.6 * 0.6 = 0.36F, 0.3 * 0.6 = 0.18F
        this.setSize(0.36F, 0.18F);
        this.experienceValue = 2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(IS_SUCKING_BLOOD, false);
        this.dataManager.register(SUCK_TARGET_ID, -1);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D); // 5血量
        // 降低30%移速：原先是0.1D，现在改为0.07D
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.07D); 
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D); // 基础伤害，主要靠吸血
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(4.0D); // 索敌4格
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        // 自定义瞎子AI，只听声音或被踩
        this.tasks.addTask(1, new AIBloodNematodeAttack(this));
        
        // 闲时AI：改为持续移动，不走走停停。将 executionChance 设为 1，代表只要不忙就会一直走
        EntityAIWanderAvoidWater wanderAI = new EntityAIWanderAvoidWater(this, 1.0D);
        wanderAI.setExecutionChance(1);
        this.tasks.addTask(2, wanderAI);
        
        this.tasks.addTask(3, new EntityAILookIdle(this));
        
        // 我们不使用传统的 NearestAttackableTarget，因为它是瞎子
    }

    public boolean isSuckingBlood() {
        return this.dataManager.get(IS_SUCKING_BLOOD);
    }

    public void setSuckingBlood(boolean sucking) {
        this.dataManager.set(IS_SUCKING_BLOOD, sucking);
    }

    public EntityPlayer getSuckTarget() {
        int id = this.dataManager.get(SUCK_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.world.getEntityByID(id);
        return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
    }

    public void setSuckTarget(EntityPlayer player) {
        if (player == null) {
            this.dataManager.set(SUCK_TARGET_ID, -1);
            this.setSuckingBlood(false);
            this.suckCooldown = 2000; // 100秒冷却 = 2000 tick
            this.suckTimer = 0;
            // 掉落下来
            this.dismountRidingEntity();
        } else {
            // 如果玩家已经死亡，则不吸附
            if (!player.isEntityAlive()) {
                return;
            }
            // 如果已经在吸血了，不要重复调用骑乘，防止死循环
            if (this.isSuckingBlood() && this.getRidingEntity() == player) {
                return;
            }
            this.dataManager.set(SUCK_TARGET_ID, player.getEntityId());
            this.setSuckingBlood(true);
            // 骑在玩家身上
            this.startRiding(player, true);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.suckCooldown > 0) {
            this.suckCooldown--;
        }

        if (this.isSuckingBlood()) {
            EntityPlayer target = getSuckTarget();
            if (target != null && target.isEntityAlive()) {
                // 如果玩家进入火中或岩浆中，立刻下来
                if (target.isBurning() || target.isInLava()) {
                    if (!this.world.isRemote) {
                        setSuckTarget(null);
                    }
                    return;
                }

                // 强制同步位置和朝向到玩家头部
                this.setPositionAndRotation(target.posX, target.posY + target.getEyeHeight(), target.posZ, target.rotationYaw, target.rotationPitch);
                this.rotationYawHead = target.rotationYawHead;
                this.renderYawOffset = target.renderYawOffset;
                
                // 确保我们一直是骑乘状态，如果没有，说明被外力打断了，重新骑上去或者掉下来
                if (this.getRidingEntity() != target && !this.world.isRemote) {
                     setSuckTarget(null);
                     return;
                }

                if (!this.world.isRemote) {
                    this.suckTimer++;
                    // 每秒(20 ticks)吸一滴血
                    if (this.suckTimer % 20 == 0) {
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F); // 吸血1滴
                        this.heal(1.0F); // 自身回血1滴
                    }

                    // 7-10秒后自行下来 (140 - 200 ticks)
                    if (this.suckTimer > 140 + this.rand.nextInt(60)) {
                        setSuckTarget(null);
                    }
                }
            } else {
                if (!this.world.isRemote) {
                    setSuckTarget(null);
                }
            }
        } else {
            // 踩踏触发机制：如果冷却好了，并且有玩家主动踩到它身上，直接开始吸血
            if (this.suckCooldown <= 0 && !this.world.isRemote) {
                // 将向上膨胀的判定范围改小，只有当玩家的脚确切踩在它头顶或与它发生实质性碰撞时才触发
                for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(0.1D, 0.2D, 0.1D))) {
                    if (player != null && !player.isCreative() && !player.isSpectator()) {
                        setSuckTarget(player);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public double getYOffset() {
        // 如果在吸血，由于我们是骑在玩家身上的，我们需要调整 Y 偏移，让它盖在脸上
        if (this.isSuckingBlood()) {
            return -0.2D; // 根据实际情况微调
        }
        return super.getYOffset();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("SuckCooldown", this.suckCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.suckCooldown = compound.getInteger("SuckCooldown");
    }

    // --------------------------------------------------------
    // 自定义 AI：听觉索敌与攻击
    // --------------------------------------------------------
    class AIBloodNematodeAttack extends EntityAIBase {
        EntityBloodNematode nematode;
        EntityPlayer target;

        public AIBloodNematodeAttack(EntityBloodNematode entity) {
            this.nematode = entity;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (this.nematode.suckCooldown > 0 || this.nematode.isSuckingBlood()) {
                return false;
            }
            
            // 听觉索敌：寻找4格内的玩家
            EntityPlayer closestPlayer = this.nematode.world.getClosestPlayerToEntity(this.nematode, 4.0D);
            if (closestPlayer != null && !closestPlayer.isCreative() && !closestPlayer.isSpectator()) {
                // 判断是否发出声音：没有潜行 且 发生了移动或动作
                // 由于无法直接完美检测"所有声音"，我们近似认为：只要玩家不在潜行且发生移动(距离上一tick坐标有变化)，或者正在挥动手臂(挖掘/放置)，就算作发出声音
                boolean isMoving = closestPlayer.distanceWalkedModified > closestPlayer.prevDistanceWalkedModified;
                boolean isSwinging = closestPlayer.isSwingInProgress;
                boolean isHurt = closestPlayer.hurtTime > 0;
                
                if (!closestPlayer.isSneaking() && (isMoving || isSwinging || isHurt)) {
                    this.target = closestPlayer;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void startExecuting() {
            this.nematode.setAttackTarget(this.target);
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (this.target == null || !this.target.isEntityAlive() || this.target.isCreative() || this.target.isSpectator()) return false;
            if (this.nematode.suckCooldown > 0 || this.nematode.isSuckingBlood()) return false;
            if (this.nematode.getDistanceSq(this.target) > 16.0D) return false; // 超过4格失去目标
            
            // 持续追踪时，如果玩家突然蹲下不动了，线虫会因为眼瞎失去目标
            boolean isMoving = this.target.distanceWalkedModified > this.target.prevDistanceWalkedModified;
            if (this.target.isSneaking() && !isMoving) {
                return false;
            }
            return true;
        }

        @Override
        public void updateTask() {
            if (this.target == null || !this.target.isEntityAlive()) {
                return;
            }
            
            this.nematode.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            this.nematode.getNavigator().tryMoveToEntityLiving(this.target, 1.0D); // 使用基础速度靠近

            // 主动爬到玩家脚边触发：通过碰撞箱相交来判定是否真正触碰到了玩家
            if (this.nematode.getEntityBoundingBox().grow(0.1D, 0.1D, 0.1D).intersects(this.target.getEntityBoundingBox())) {
                this.nematode.setSuckTarget(this.target);
            }
        }
        
        @Override
        public void resetTask() {
            this.target = null;
            this.nematode.setAttackTarget(null);
            this.nematode.getNavigator().clearPath();
        }
    }
}