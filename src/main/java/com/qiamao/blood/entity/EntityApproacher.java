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
        this.setSize(0.8F, 1.8F);
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
        return this.isBurning() ? ModItems.COOKED_GORY_FLESH : Items.ROTTEN_FLESH; // 假设使用烂肉，烧死掉落熟烂肉
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
        private Vec3d fleePos;

        public AIApproacherFlee(EntityApproacher entity, double speed) {
            this.entity = entity;
            this.speed = speed;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (this.entity.getHealth() > 7.0F) {
                return false;
            }
            this.closestPlayer = this.entity.world.getClosestPlayerToEntity(this.entity, 7.0D);
            return this.closestPlayer != null;
        }

        @Override
        public void startExecuting() {
            this.entity.setFlying(true);
            this.entity.setAttackTarget(null);
        }

        @Override
        public void updateTask() {
            if (this.closestPlayer != null) {
                Vec3d entityPos = new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
                Vec3d playerPos = new Vec3d(this.closestPlayer.posX, this.closestPlayer.posY, this.closestPlayer.posZ);
                Vec3d dir = entityPos.subtract(playerPos).normalize();
                
                // 飞行逻辑：向反方向移动，飞行高度和移动更自由
                double targetY = this.entity.posY;
                
                // 保持离地大约 3-5 格的高度，不再是贴地飞行
                if (this.entity.world.isAirBlock(this.entity.getPosition().down(4))) {
                    targetY -= 0.15; // 太高了稍微降一点
                } else if (!this.entity.world.isAirBlock(this.entity.getPosition().down(2))) {
                    targetY += 0.25; // 太低了升起
                }
                
                // 添加蝙蝠风格的正弦波动，消除生硬的漂浮感
                double waveY = Math.cos(this.entity.ticksExisted * 0.3) * 0.1;
                double waveX = Math.sin(this.entity.ticksExisted * 0.2) * 0.05;
                double waveZ = Math.cos(this.entity.ticksExisted * 0.25) * 0.05;
                
                // 设置飞行速度 (蝙蝠的大约0.75倍)
                // 增加基础水平速度，同时叠加波动，使飞行轨迹更自由流畅
                double speedX = dir.x * 0.25 + waveX;
                double speedZ = dir.z * 0.25 + waveZ;
                double speedY = (targetY - this.entity.posY) * 0.15 + waveY;
                
                this.entity.motionX = speedX;
                this.entity.motionZ = speedZ;
                this.entity.motionY = speedY;
                
                // 身体转向逃跑方向
                this.entity.rotationYaw = (float) (Math.atan2(this.entity.motionZ, this.entity.motionX) * (180D / Math.PI)) - 90.0F;
                this.entity.renderYawOffset = this.entity.rotationYaw;
            }
        }

        @Override
        public void resetTask() {
            this.entity.setFlying(false);
            this.closestPlayer = null;
        }
    }
}
