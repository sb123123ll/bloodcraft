package com.qiamao.blood.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 视神经 (Optic Nerve) 实体类
 */
public class EntityOpticNerve extends EntityMob {
    private static final DataParameter<Boolean> TARGETING = EntityDataManager.createKey(EntityOpticNerve.class, DataSerializers.BOOLEAN);

    public float headSpinAngle = 0.0F;
    private int hitCount = 0;
    private boolean hasHitPlayer = false;
    
    // 声音计时器
    private int targetSoundTimer = 0;
    private int stepSoundTimer = 0;

    public EntityOpticNerve(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 3.0F); // 视神经较高
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TARGETING, false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        // 基础移速非常快，但平时没有 wander AI，因此只在索敌时移动
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        // 只对玩家有仇恨
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // 血量50颗心 (100点)
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        // 移速：玩家奔跑的2.5倍。玩家基础移速是0.1，奔跑约0.13，这里设为 0.65D 是非常快的速度
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.65D);
        // 攻击伤害
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        // 索敌范围8格
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(8.0D);
        // 防击退能力，设置为1.0D表示免疫击退
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    @Override
    public boolean canBePushed() {
        // 不被其他实体挤压移动
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        // 覆盖此方法使其空实现，避免碰撞移动
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        // 覆盖此方法使其空实现，避免碰撞移动
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        // 强制锁定身体偏转角度，让其静止时完全无法转身
        if (this.getAttackTarget() == null) {
            this.rotationYaw = this.prevRotationYaw;
            this.rotationYawHead = this.prevRotationYawHead;
            this.renderYawOffset = this.prevRenderYawOffset;
        }

        if (this.world.isRemote) {
            boolean isTargeting = this.dataManager.get(TARGETING);
            if (isTargeting) {
                // 半秒一周：10 tick = 360 度
                headSpinAngle += (float) (Math.PI * 2.0 / 10.0);
                if (headSpinAngle > Math.PI * 2) {
                    headSpinAngle -= Math.PI * 2;
                }
            } else {
                // 平滑转回原位
                if (headSpinAngle > 0.01F) {
                    if (headSpinAngle > Math.PI) {
                        headSpinAngle += 0.3F;
                        if (headSpinAngle >= Math.PI * 2) headSpinAngle = 0;
                    } else {
                        headSpinAngle -= 0.3F;
                        if (headSpinAngle <= 0) headSpinAngle = 0;
                    }
                } else {
                    headSpinAngle = 0.0F;
                }
            }
        } else {
            boolean hasTarget = this.getAttackTarget() != null;
            if (this.dataManager.get(TARGETING) != hasTarget) {
                this.dataManager.set(TARGETING, hasTarget);
            }
            
            // --- 声音播放逻辑 (仅在服务端处理以同步给所有客户端) ---
            if (hasTarget) {
                // 1. 索敌声音：每 0.8 秒 (16 ticks) 播放一次
                if (this.targetSoundTimer <= 0) {
                    this.playSound(com.qiamao.blood.init.ModSounds.OPTIC_NERVE_TARGET, this.getSoundVolume(), this.getSoundPitch());
                    this.targetSoundTimer = 16;
                } else {
                    this.targetSoundTimer--;
                }
                
                // 2. 奔跑/行走声音：由于移动速度设为 0.65，基础是每 0.65 秒 (13 ticks) 播放一次
                // 根据实际移动距离来计算是否在移动
                double distanceSq = (this.posX - this.prevPosX) * (this.posX - this.prevPosX) + (this.posZ - this.prevPosZ) * (this.posZ - this.prevPosZ);
                if (distanceSq > 0.001D) { // 确实在移动
                    if (this.stepSoundTimer <= 0) {
                        // 播放脚步声
                        this.playSound(com.qiamao.blood.init.ModSounds.OPTIC_NERVE_STEP, this.getSoundVolume() * 0.8F, this.getSoundPitch());
                        
                        // 根据当前速度动态调整播放间隔，基础速度 0.65 对应 13 ticks
                        // 距离越大，ticks 越小。我们用一个近似公式，或者直接固定 13 tick
                        this.stepSoundTimer = 13;
                    } else {
                        this.stepSoundTimer--;
                    }
                } else {
                    // 没移动时重置脚步计时器
                    this.stepSoundTimer = 0;
                }
            } else {
                // 没有目标时重置计时器
                this.targetSoundTimer = 0;
                this.stepSoundTimer = 0;
            }
        }
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        // 原版系统会自动随机调用此方法播放待机声音
        return com.qiamao.blood.init.ModSounds.OPTIC_NERVE_AMBIENT;
    }

    public float getHeadSpinAngle() {
        return headSpinAngle;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            this.hasHitPlayer = true;
            
            // 造成最高10点伤害
            float damage = 10.0F;
            
            // 如果伤害会致死玩家，强制留一颗心（2点血量）
            if (player.getHealth() - damage <= 0.0F) {
                damage = player.getHealth() - 2.0F;
                // 如果玩家当前血量已经小于等于2，就不造成伤害（或者只造成0点伤害触发击退）
                if (damage <= 0.0F) {
                    damage = 0.0F;
                }
            }
            
            boolean flag = player.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
            if (flag) {
                // 给予 6-10 秒的失明效果
                int duration = (6 + this.rand.nextInt(5)) * 20;
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, duration, 0));
                // 攻击成功后完全消失
                this.setDead();
            }
            return flag;
        }
        return false; // 不反击玩家以外的实体
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.world.isRemote) {
            return super.attackEntityFrom(source, amount);
        }

        // 正常方式不可击杀此生物，将其生命锁定在1点以上
        if (this.getHealth() - amount <= 0.0F) {
            this.setHealth(1.0F);
            amount = 0.0F; // 取消实际伤害计算，避免意外死亡
        }

        // 如果被玩家攻击
        if (source.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getTrueSource();
            
            // 被玩家发现并先攻击
            if (!hasHitPlayer) {
                // 判断是否是近战攻击 (排除弓箭/投掷物等远程弹射物)
                if (source.getImmediateSource() instanceof EntityPlayer) {
                    hitCount++;
                    if (hitCount >= 5) {
                        // 第5次被近战先打到
                        // 生命恢复 II 50秒
                        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 50 * 20, 1));
                        // 生命上限提高 7颗心 (14点)，等级为 3 时增加 16 点
                        player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 50 * 20, 3)); 
                        
                        this.setDead(); // 完全消失
                        return false; // 取消伤害处理，直接消失
                    }
                }
                
                // 无论是近战还是远程，只要没到5次，都会瞬移
                teleportAroundPlayer(player);
            }
        } else if (source.getTrueSource() != null) {
            // 被其他实体攻击：不反击、不消失，只瞬移逃离
            if (this.getAttackTarget() instanceof EntityPlayer) {
                teleportAroundPlayer((EntityPlayer) this.getAttackTarget());
            } else {
                // 随便瞬移一下
                teleportRandomly();
            }
        }
        
        return amount > 0.0F ? super.attackEntityFrom(source, amount) : false;
    }

    private void teleportRandomly() {
        for (int i = 0; i < 16; i++) {
            double targetX = this.posX + (this.rand.nextDouble() - 0.5D) * 16.0D;
            double targetY = this.posY + (this.rand.nextInt(8) - 4);
            double targetZ = this.posZ + (this.rand.nextDouble() - 0.5D) * 16.0D;
            
            BlockPos testPos = new BlockPos(targetX, targetY, targetZ);
            if (world.getBlockState(testPos.down()).isOpaqueCube() && world.isAirBlock(testPos) && world.isAirBlock(testPos.up()) && world.isAirBlock(testPos.up(2))) {
                this.setPositionAndUpdate(targetX, testPos.getY(), targetZ);
                this.getNavigator().clearPath();
                break;
            }
        }
    }

    private void teleportAroundPlayer(EntityPlayer player) {
        for (int i = 0; i < 32; i++) {
            // 距离：3 到 8 格
            double distance = 3.0 + this.rand.nextDouble() * 5.0;
            // 角度：玩家视角的背面半圆，即 90 度到 270 度之间
            float angleOffset = 90.0F + this.rand.nextFloat() * 180.0F;
            float finalAngle = player.rotationYaw + angleOffset;
            
            double rad = Math.toRadians(finalAngle);
            double targetX = player.posX - Math.sin(rad) * distance;
            double targetZ = player.posZ + Math.cos(rad) * distance;
            double targetY = player.posY;
            
            BlockPos pos = new BlockPos(targetX, targetY, targetZ);
            boolean foundSafe = false;
            
            // 在 Y 轴上寻找安全落脚点
            for (int y = -4; y <= 4; y++) {
                BlockPos testPos = pos.up(y);
                if (world.getBlockState(testPos.down()).isOpaqueCube() && world.isAirBlock(testPos) && world.isAirBlock(testPos.up()) && world.isAirBlock(testPos.up(2))) {
                    targetY = testPos.getY();
                    foundSafe = true;
                    break;
                }
            }
            
            if (foundSafe) {
                this.setPositionAndUpdate(targetX, targetY, targetZ);
                this.getNavigator().clearPath(); // 清除旧的寻路
                break;
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!super.getCanSpawnHere()) {
            return false;
        }
        
        // 下界：全天候生成
        if (this.world.provider.getDimension() == -1) {
            return true;
        }
        
        // 主世界：仅在地表晚上生成
        if (this.world.provider.getDimension() == 0) {
            BlockPos pos = new BlockPos(this);
            if (!this.world.canSeeSky(pos)) {
                return false;
            }
            return true;
        }
        
        return true;
    }
}
