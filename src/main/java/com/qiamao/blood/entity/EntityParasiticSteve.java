package com.qiamao.blood.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.DamageSource;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import com.qiamao.blood.init.ModSounds;

public class EntityParasiticSteve extends EntityMob {

    // 用于同步欲望事件状态的 DataParameter
    private static final DataParameter<Boolean> DESIRE_EVENT_ACTIVE = EntityDataManager.createKey(EntityParasiticSteve.class, DataSerializers.BOOLEAN);

    public EntityParasiticSteve(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F); // 标准史蒂夫大小
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DESIRE_EVENT_ACTIVE, Boolean.valueOf(false));
    }

    public void setDesireEventActive(boolean active) {
        this.dataManager.set(DESIRE_EVENT_ACTIVE, Boolean.valueOf(active));
    }

    public boolean isDesireEventActive() {
        return this.dataManager.get(DESIRE_EVENT_ACTIVE);
    }

    /**
     * 更新欲望事件期间的属性
     */
    public void updateDesireEventAttributes() {
        if (this.isDesireEventActive()) {
            // 欲望事件期间移动速度提升25%
            double baseSpeed = 0.25D;
            double enhancedSpeed = baseSpeed * 1.25D;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(enhancedSpeed);
        } else {
            // 恢复正常移动速度
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        // 害怕鹦鹉，快速逃离（近处1.5倍速度，稍远处1.2倍速度）
        this.tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityParrot.class, 8.0F, 1.2D, 1.5D));
        // 攻击目标时加速奔跑冲向目标 (1.5倍速度)
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.5D, false));
        // 闲时慢走 (速度增加一半，从0.4D变成0.6D)
        this.tasks.addTask(3, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));

        // 攻击玩家和村民，索敌范围在属性中设置为12格
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, net.minecraft.entity.passive.EntityVillager.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D); // 血量: 15
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D); // 攻击：3颗心 (6滴血)
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0D); // 索敌范围12格
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        // 被攻击到会有12%的概率获得3秒的反胃
        if (flag && entityIn instanceof EntityLivingBase) {
            if (this.rand.nextInt(100) < 12) {
                ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0)); // 3秒 = 60 ticks
            }
        }
        return flag;
    }

    @Override
    public void onDeath(net.minecraft.util.DamageSource cause) {
        super.onDeath(cause);

        // 增加击杀统计，使玩家击杀寄生史蒂夫能获得原版怪物猎人成就
        net.minecraft.entity.Entity entity = cause.getTrueSource();
        if (entity instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) entity;
            player.addStat(net.minecraft.stats.StatList.MOB_KILLS, 1);
        }

        // 增加击退逻辑的范围限制和调用优化
        if (!this.world.isRemote) {
            // 被打死的一瞬间会自爆，无伤害，播放TNT粒子
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);

            // 击退玩家几格，优化为仅获取4格内玩家
            for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(4.0D))) {
                double d0 = player.posX - this.posX;
                double d1 = player.posZ - this.posZ;
                player.knockBack(this, 1.5F, -d0, -d1);
            }

            // 自爆之后向四周像洒水车一样抛出4到6个血螨投掷物
            int miteCount = 4 + this.rand.nextInt(3); // 4, 5, 6
            for (int i = 0; i < miteCount; i++) {
                EntityThrownBloodMite thrownMite = new EntityThrownBloodMite(this.world, this);
                // 设置起始位置在胸口附近
                thrownMite.setPosition(this.posX, this.posY + this.getEyeHeight() - 0.5D, this.posZ);

                // 随机水平角度 0~360
                float randomYaw = this.rand.nextFloat() * 360.0F;

                // 仰角稍微压低一点 (-40 到 -70度)，不让它飞得太直上直下
                float randomPitch = -40.0F - this.rand.nextFloat() * 30.0F;

                // 因为重力加大了 (0.035)，要控制它落在 4 格以内
                // 初速度不需要太大，0.5 ~ 0.7 足够它跳出个抛物线然后迅速砸在地上
                float velocity = 0.5F + this.rand.nextFloat() * 0.2F;

                thrownMite.shoot(this, randomPitch, randomYaw, 0.0F, velocity, 5.0F);
                this.world.spawnEntity(thrownMite);
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        // 刷怪蛋生成的实体存活时间很短（< 10 ticks），跳过时间/光照限制
        if (this.ticksExisted < 10) {
            // 只做基本的安全检查：脚下有实体方块，且不淹死在水里
            net.minecraft.block.state.IBlockState iblockstate = this.world.getBlockState((new net.minecraft.util.math.BlockPos(this)).down());
            if (!iblockstate.canEntitySpawn(this)) {
                return false;
            }
            return !this.world.containsAnyLiquid(this.getEntityBoundingBox());
        }

        // 自然生成：只在晚上自然生成 (12000 到 24000 tick)
        long time = this.world.getWorldTime() % 24000;
        if (time < 12000) {
            return false;
        }

        // 自然生成：模仿僵尸的光照检查，只在黑暗处生成
        if (!this.isValidLightLevel()) {
            return false;
        }

        // 只做基本的安全检查：脚下有实体方块，且不淹死在水里
        net.minecraft.block.state.IBlockState iblockstate = this.world.getBlockState((new net.minecraft.util.math.BlockPos(this)).down());
        if (!iblockstate.canEntitySpawn(this)) {
            return false;
        }
        return !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    /**
     * 模仿僵尸的光照检查逻辑
     * 只在光照等级 <= 7 的地方生成（黑暗处）
     */
    protected boolean isValidLightLevel() {
        net.minecraft.util.math.BlockPos blockpos = new net.minecraft.util.math.BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

        if (this.world.getLightFor(net.minecraft.world.EnumSkyBlock.SKY, blockpos) > this.rand.nextInt(32)) {
            return false;
        } else {
            int i = this.world.getLightFromNeighbors(blockpos);
            if (this.world.isThundering()) {
                int j = this.world.getSkylightSubtracted();
                this.world.setSkylightSubtracted(10);
                i = this.world.getLightFromNeighbors(blockpos);
                this.world.setSkylightSubtracted(j);
            }

            return i <= this.rand.nextInt(8);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PARASITIC_STEVE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.PARASITIC_STEVE_HURT;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // 掉落 1 到 2 个怪物躯骨
        int boneCount = 1 + this.rand.nextInt(2); // 1 + [0, 1] = 1 到 2
        // 抢夺附魔增加掉落
        if (lootingModifier > 0) {
            boneCount += this.rand.nextInt(lootingModifier + 1);
        }
        
        for (int i = 0; i < boneCount; ++i) {
            this.dropItem(com.qiamao.blood.init.ModItems.MONSTER_BONE_SKELETON, 1);
        }

        // 寄生史蒂夫掉落 1 到 3 个烂肉
        int fleshCount = 1 + this.rand.nextInt(3); // 1 + [0, 2] = 1 到 3
        // 抢夺附魔增加掉落
        if (lootingModifier > 0) {
            fleshCount += this.rand.nextInt(lootingModifier + 1);
        }

        // 检查是否着火：着火掉熟烂肉，否则掉生的
        boolean isBurning = this.isBurning();

        if (fleshCount > 0) {
            if (this.rand.nextFloat() < 0.3F) {
                if (isBurning) {
                    this.dropItem(com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART, 1);
                } else {
                    this.dropItem(com.qiamao.blood.init.ModItems.HUMAN_HEART, 1);
                }
            } else {
                for (int i = 0; i < fleshCount; ++i) {
                    if (isBurning) {
                        this.dropItem(com.qiamao.blood.init.ModItems.COOKED_GORY_FLESH, 1);
                    } else {
                        this.dropItem(com.qiamao.blood.init.ModItems.GORY_FLESH, 1);
                    }
                }
            }
        }
    }

}