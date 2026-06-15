package com.qiamao.blood.entity;

import com.qiamao.blood.init.ModItems;
import com.qiamao.blood.init.ModSounds;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityBloodSeekerLarva extends EntityCreature {

    private boolean isSpawnedByEgg = false;

    public EntityBloodSeekerLarva(World worldIn) {
        super(worldIn);
        // 成虫是 1.325F x 0.925F，幼虫是它的三分之一
        this.setSize(1.325F / 3.0F, 0.925F / 3.0F);
        this.stepHeight = 1.0F; // 稍微降低一点跳跃高度
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        
        // 躲避月光
        this.tasks.addTask(1, new AISeekShadeAtNightLarva(this, 1.5D));
        
        // 跟随成虫
        this.tasks.addTask(2, new AIFollowAdult(this, 1.25D));

        // 闲时随机移动
        this.tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // 15点血 (7.5颗心)，是成虫的一半
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D); 
        // 闲时基础移动速度：0.23D (和成虫一样，否则跟不上)
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D); 
        // 无攻击力，所以不需要设置攻击力
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        // 类似成虫的自燃逻辑
        if (!this.world.isRemote && !this.world.isDaytime()) {
            if (!this.isInWater() && !this.isImmuneToFire()) {
                if (this.world.canBlockSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ))) {
                    this.setFire(8);
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isBurning() && source.getTrueSource() != null) {
            this.hurtResistantTime = 0;
        }
        
        boolean flag = super.attackEntityFrom(source, amount);
        
        // 当幼虫被攻击时，呼叫附近的成虫保护
        if (flag && !this.world.isRemote) {
            net.minecraft.entity.Entity attacker = source.getTrueSource();
            if (attacker instanceof EntityLivingBase) {
                List<EntityBloodSeeker> adults = this.world.getEntitiesWithinAABB(EntityBloodSeeker.class, this.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
                for (EntityBloodSeeker adult : adults) {
                    // 设置复仇目标和攻击目标，确保成虫立刻响应
                    adult.setRevengeTarget((EntityLivingBase) attacker);
                    adult.setAttackTarget((EntityLivingBase) attacker);
                }
            }
        }
        
        return flag;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CENTIPEDE_LARVA_AMBIENT; 
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.CENTIPEDE_LARVA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CENTIPEDE_LARVA_DEATH;
    }
    
    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        // 幼虫声音调高一点，显得更小
        super.playSound(soundIn, volume, pitch + 0.5F);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // 掉落概率和掉落数量减半
        int count = 1 + this.rand.nextInt(2); // 1 到 2 (成虫是2到4)
        if (lootingModifier > 0) {
            count += this.rand.nextInt(lootingModifier + 1) / 2;
        }
        
        boolean isBurning = this.isBurning();
        
        for (int i = 0; i < count; ++i) {
            if (isBurning) {
                this.dropItem(ModItems.COOKED_GORY_FLESH, 1);
            } else {
                this.dropItem(ModItems.GORY_FLESH, 1);
            }
        }

        // 12.5% 的概率掉落多足虫的刺 (成虫是25%)
        float dropChance = 0.125F + (lootingModifier * 0.025F);
        if (this.rand.nextFloat() < dropChance) {
            this.dropItem(ModItems.CENTIPEDE_STINGER, 1);
        }
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 2; // 经验减半
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.isSpawnedByEgg = true;
        return livingdata;
    }

    // --- 自定义 AI：晚上寻找有遮挡的地方 ---
    static class AISeekShadeAtNightLarva extends EntityAIBase {
        private final EntityBloodSeekerLarva larva;
        private final double movementSpeed;
        private double shelterX;
        private double shelterY;
        private double shelterZ;

        public AISeekShadeAtNightLarva(EntityBloodSeekerLarva larva, double speed) {
            this.larva = larva;
            this.movementSpeed = speed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.larva.world.isDaytime() || this.larva.world.isRemote) {
                return false;
            }
            if (!this.larva.world.canBlockSeeSky(new BlockPos(this.larva.posX, this.larva.posY + (double)this.larva.getEyeHeight(), this.larva.posZ))) {
                return false;
            }
            Vec3d vec3d = this.findPossibleShelter();
            if (vec3d == null) {
                return false;
            } else {
                this.shelterX = vec3d.x;
                this.shelterY = vec3d.y;
                this.shelterZ = vec3d.z;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.larva.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.larva.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
        }

        private Vec3d findPossibleShelter() {
            for (int i = 0; i < 10; ++i) {
                BlockPos blockpos = new BlockPos(this.larva.posX + (double)this.larva.getRNG().nextInt(20) - 10.0D, this.larva.posY + (double)this.larva.getRNG().nextInt(6) - 3.0D, this.larva.posZ + (double)this.larva.getRNG().nextInt(20) - 10.0D);
                if (!this.larva.world.canBlockSeeSky(blockpos)) {
                    return new Vec3d((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                }
            }
            return null;
        }
    }

    // --- 自定义 AI：跟随成虫 ---
    static class AIFollowAdult extends EntityAIBase {
        private final EntityBloodSeekerLarva larva;
        private EntityBloodSeeker adult;
        private final double moveSpeed;
        private int delayCounter;

        public AIFollowAdult(EntityBloodSeekerLarva larva, double speed) {
            this.larva = larva;
            this.moveSpeed = speed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            List<EntityBloodSeeker> list = this.larva.world.getEntitiesWithinAABB(EntityBloodSeeker.class, this.larva.getEntityBoundingBox().grow(10.0D, 4.0D, 10.0D));
            
            EntityBloodSeeker closestAdult = null;
            double minDistance = Double.MAX_VALUE;

            for (EntityBloodSeeker adultInList : list) {
                double dist = this.larva.getDistanceSq(adultInList);
                if (dist <= minDistance) {
                    minDistance = dist;
                    closestAdult = adultInList;
                }
            }

            if (closestAdult == null) {
                return false;
            } else if (minDistance < 9.0D) {
                return false; // 如果距离小于 3 格，就不再靠近
            } else {
                this.adult = closestAdult;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (this.adult.isDead) {
                return false;
            } else {
                double dist = this.larva.getDistanceSq(this.adult);
                return dist >= 9.0D && dist <= 256.0D;
            }
        }

        @Override
        public void startExecuting() {
            this.delayCounter = 0;
        }

        @Override
        public void resetTask() {
            this.adult = null;
        }

        @Override
        public void updateTask() {
            if (--this.delayCounter <= 0) {
                this.delayCounter = 10;
                this.larva.getNavigator().tryMoveToEntityLiving(this.adult, this.moveSpeed);
            }
        }
    }
}