package com.qiamao.blood.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import com.qiamao.blood.init.ModItems;
import com.qiamao.blood.init.ModSounds;
import net.minecraft.util.math.Vec3d;

public class EntityApproacher extends EntityMob {

    private static final DataParameter<Boolean> IS_FLYING = EntityDataManager.createKey(EntityApproacher.class, DataSerializers.BOOLEAN);
    
    private int strafeTimer = 0;
    private boolean strafeLeft = false;

    private boolean isSpawnedByEgg = false;

    public EntityApproacher(World worldIn) {
        super(worldIn);
        // 原尺寸: 0.8F宽, 1.8F高
        // 增大1.28倍: 宽 = 0.8 * 1.28 = 1.024F, 高 = 1.8 * 1.28 = 2.304F
        this.setSize(1.024F, 2.304F);
        this.experienceValue = 5;
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        // 标记为刷怪蛋或刷怪笼等特殊方式生成
        this.isSpawnedByEgg = true;
        return livingdata;
    }

    @Override
    public boolean getCanSpawnHere() {
        // 如果是刷怪蛋生成，跳过时间、光照等严格限制
        if (this.isSpawnedByEgg || this.ticksExisted < 10) {
            net.minecraft.block.state.IBlockState iblockstate = this.world.getBlockState((new net.minecraft.util.math.BlockPos(this)).down());
            if (!iblockstate.canEntitySpawn(this)) {
                return false;
            }
            return !this.world.containsAnyLiquid(this.getEntityBoundingBox());
        }
        
        // 自然生成走父类逻辑 (MONSTER类型的父类逻辑已经包含了只在黑夜/黑暗处生成的检查)
        return super.getCanSpawnHere();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(IS_FLYING, false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AIApproacherFlee(this, 1.0D));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.1D, false));
        this.tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 7.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityBloodSeeker.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityBloodSeekerLarva.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        // 基础移速降低至 0.75 倍 (0.3D * 0.75 = 0.225D)
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.225D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(7.0D);
    }

    public boolean isFlying() {
        return this.dataManager.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(IS_FLYING, flying);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote) {
            // 7血以下逃跑机制
            if (this.getHealth() <= 7.0F) {
                if (this.getAttackTarget() != null) {
                    this.setAttackTarget(null); // 清除仇恨
                }
            }

            // 侧滑逻辑 (斜着追逐)
            if (this.getAttackTarget() != null && !this.isFlying()) {
                if (this.strafeTimer-- <= 0) {
                    this.strafeTimer = 20 + this.rand.nextInt(30);
                    this.strafeLeft = this.rand.nextBoolean();
                }
                this.moveStrafing = this.strafeLeft ? 0.5F : -0.5F;
            } else {
                this.moveStrafing = 0.0F;
            }
            
            // 飞行状态下禁用重力
            this.setNoGravity(this.isFlying());
        } else {
            // 客户端飞行粒子等可在此添加
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (!this.isFlying()) {
            super.fall(distance, damageMultiplier);
        }
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos) {
        if (!this.isFlying()) {
            super.updateFallState(y, onGroundIn, state, pos);
        }
    }

    @Override
    protected Item getDropItem() {
        return this.isBurning() ? ModItems.COOKED_GORY_FLESH : ModItems.GORY_FLESH; // 掉落模组内的烂肉
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        Item item = this.getDropItem();
        if (item != null) {
            int i = this.rand.nextInt(3);
            if (lootingModifier > 0) {
                i += this.rand.nextInt(lootingModifier + 1);
            }
            for (int j = 0; j < i; ++j) {
                this.dropItem(item, 1);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.APPROACHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    // 自定义逃跑AI
    class AIApproacherFlee extends EntityAIBase {
        private final EntityApproacher entity;
        private final double speed;
        private EntityPlayer closestPlayer;

        public AIApproacherFlee(EntityApproacher entity, double speed) {
            this.entity = entity;
            this.speed = speed;
            this.setMutexBits(3); // 占用移动和看向的通道，确保飞行时不被其他AI干扰
        }

        @Override
        public boolean shouldExecute() {
            // 只有在残血时才触发逃跑
            if (this.entity.getHealth() > 7.0F) {
                return false;
            }
            this.closestPlayer = this.entity.world.getClosestPlayerToEntity(this.entity, 7.0D);
            return this.closestPlayer != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            // 只要还在残血状态，并且玩家还在附近（甚至稍微远一点也继续飞，比如10格），就继续执行
            if (this.entity.getHealth() > 7.0F) {
                return false;
            }
            
            // 如果找不到原来的玩家，重新找一个
            if (this.closestPlayer == null || !this.closestPlayer.isEntityAlive()) {
                this.closestPlayer = this.entity.world.getClosestPlayerToEntity(this.entity, 12.0D); // 扩大搜索范围以保持逃跑
            }
            
            // 如果12格内依然有玩家，继续飞；否则停止飞行落地
            return this.closestPlayer != null && this.entity.getDistance(this.closestPlayer) < 12.0D;
        }

        @Override
        public void startExecuting() {
            this.entity.setFlying(true);
            this.entity.setAttackTarget(null);
            this.entity.setNoGravity(true); // 确保一开始就没有重力
        }

        @Override
        public void updateTask() {
            if (this.closestPlayer != null) {
                Vec3d entityPos = new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
                Vec3d playerPos = new Vec3d(this.closestPlayer.posX, this.closestPlayer.posY, this.closestPlayer.posZ);
                // 计算远离玩家的方向
                Vec3d dir = entityPos.subtract(playerPos).normalize();
                
                // 飞行逻辑：向反方向移动，并保持一定高度
                double targetY = this.entity.posY;
                
                // 探测下方空间，保持离地大约 4-6 格的高度
                if (this.entity.world.isAirBlock(this.entity.getPosition().down(6))) {
                    targetY -= 0.1; // 太高了，缓慢下降
                } else if (!this.entity.world.isAirBlock(this.entity.getPosition().down(4))) {
                    targetY += 0.2; // 太低了，快速爬升
                }
                
                // 添加蝙蝠风格的正弦波动，消除生硬的漂浮感
                double waveY = Math.cos(this.entity.ticksExisted * 0.3) * 0.05;
                double waveX = Math.sin(this.entity.ticksExisted * 0.2) * 0.05;
                double waveZ = Math.cos(this.entity.ticksExisted * 0.25) * 0.05;
                
                // 设置飞行速度
                double speedX = dir.x * 0.35 + waveX;
                double speedZ = dir.z * 0.35 + waveZ;
                double speedY = (targetY - this.entity.posY) * 0.2 + waveY;
                
                this.entity.motionX = speedX;
                this.entity.motionZ = speedZ;
                this.entity.motionY = speedY;
                
                // 强制生物身体转向逃跑方向
                float yaw = (float) (Math.atan2(this.entity.motionZ, this.entity.motionX) * (180D / Math.PI)) - 90.0F;
                this.entity.rotationYaw = yaw;
                this.entity.rotationYawHead = yaw;
                this.entity.renderYawOffset = yaw;
            }
        }

        @Override
        public void resetTask() {
            this.entity.setFlying(false);
            this.entity.setNoGravity(false); // 恢复重力
            this.closestPlayer = null;
        }
    }
}
