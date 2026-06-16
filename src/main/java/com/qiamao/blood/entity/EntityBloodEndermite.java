package com.qiamao.blood.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import com.qiamao.blood.init.ModSounds;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityBloodEndermite extends EntityMob implements IRangedAttackMob {

    // 标记是否由刷怪蛋生成
    private boolean isSpawnedByEgg = false;

    public EntityBloodEndermite(World worldIn) {
        super(worldIn);
        // 和原版末影螨一样的碰撞箱大小
        this.setSize(0.4F, 0.3F);
        // 原版末影螨有很高的经验值
        this.experienceValue = 3;
    }

    @Override
    protected void initEntityAI() {
        // 游泳 (最高优先级)
        this.tasks.addTask(1, new EntityAISwimming(this));
        // 自定义远程攻击AI：边靠近边射击，3格外射击，3格内切换近战
        // 射速减少15%：原25ticks → 29ticks (25 * 1.15 = 28.75)
        this.tasks.addTask(2, new AIRangedAttackMoveCloser(this, 1.2D, 29, 10.0F));
        // 近战攻击 (速度倍率 1.2，进入3格内触发)
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, false));
        // 随机走动 (速度倍率 1.0)
        this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
        // 看向附近的玩家
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        // 发呆
        this.tasks.addTask(8, new EntityAILookIdle(this));

        // 目标AI：主动寻找玩家攻击
        this.targetTasks.addTask(1, new net.minecraft.entity.ai.EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        // 主动攻击血液猎犬
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityBloodHound.class, true));
    }

    /**
     * 自定义远程攻击AI：边移动边射击，持续靠近目标直到进入近战范围
     */
    static class AIRangedAttackMoveCloser extends net.minecraft.entity.ai.EntityAIBase {
        private final EntityBloodEndermite entity;
        private final double moveSpeed;
        private final int attackInterval;
        private final float maxAttackDistance;
        private int attackTime = 0;
        private int seeTime = 0;

        public AIRangedAttackMoveCloser(EntityBloodEndermite entity, double moveSpeed, int attackInterval, float maxAttackDistance) {
            this.entity = entity;
            this.moveSpeed = moveSpeed;
            this.attackInterval = attackInterval;
            this.maxAttackDistance = maxAttackDistance * maxAttackDistance; // 转为平方
            this.setMutexBits(3); // 阻止移动和其他攻击
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.entity.getAttackTarget();
            if (target == null) return false;
            // 只在3格外且10格内执行远程
            double distSq = this.entity.getDistanceSq(target);
            if (distSq <= 9.0D || distSq > this.maxAttackDistance) return false;
            // 如果目标低于多足虫超过1.5格，可能射地，改为近战追击
            double heightDiff = target.posY - this.entity.posY;
            return heightDiff >= -1.5D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = this.entity.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            double distSq = this.entity.getDistanceSq(target);
            // 保持执行直到进入3格内、超出射程、或目标太低可能射地
            if (distSq <= 9.0D || distSq > this.maxAttackDistance) return false;
            double heightDiff = target.posY - this.entity.posY;
            return heightDiff >= -1.5D;
        }

        @Override
        public void startExecuting() {
            this.attackTime = 10; // 初次启动延迟
            this.seeTime = 0;
        }

        @Override
        public void resetTask() {
            this.entity.getNavigator().clearPath();
            this.seeTime = 0;
            this.attackTime = 0;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.entity.getAttackTarget();
            if (target == null) return;

            double distSq = this.entity.getDistanceSq(target);
            // 如果目标太低，放弃远程直接跑过去近战（不射击只移动）
            double heightDiff = target.posY - this.entity.posY;
            if (heightDiff < -1.5D) {
                this.entity.getNavigator().tryMoveToEntityLiving(target, this.moveSpeed);
                this.entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                return;
            }

            boolean canSee = this.entity.canEntityBeSeen(target);

            if (canSee) {
                this.seeTime++;
            } else {
                this.seeTime = Math.max(0, this.seeTime - 1);
            }

            // 持续向目标移动靠近（不同于原版保持距离）
            this.entity.getNavigator().tryMoveToEntityLiving(target, this.moveSpeed);
            this.entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

            // 处理射击
            if (--this.attackTime <= 0) {
                this.attackTime = this.attackInterval;
                if (canSee && distSq <= this.maxAttackDistance) {
                    this.entity.attackEntityWithRangedAttack(target, 0);
                }
            }
        }
    }

    // 实现 IRangedAttackMob 接口：远程攻击方法
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityBloodSpike spike = new EntityBloodSpike(this.world, this);
        
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
        double d2 = target.posZ - this.posZ;
        
        // 速度 1.2F，散射 1.7F（提高15%精准度，原2.0F * 0.85 = 1.7F）
        spike.shoot(d0, d1, d2, 1.2F, 1.7F);
        
        this.playSound(ModSounds.BLOOD_MITE_AMBIENT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(spike);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        // IRangedAttackMob 接口要求，这里可以留空
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // 血螨如果受到其他实体的攻击（比如骷髅小白射箭误伤），不要改变它的目标，它的目标永远只有玩家
        Entity attacker = source.getTrueSource();
        if (attacker != null && !(attacker instanceof EntityPlayer)) {
            // 如果不是玩家攻击的，我们就不让它还手（原版 EntityMob 挨打后会自动将攻击者设为目标）
            // 我们通过重写并过滤掉非玩家的攻击者的仇恨更新来保证它死死咬住玩家
            boolean flag = super.attackEntityFrom(source, amount);
            if (this.getAttackTarget() != null && !(this.getAttackTarget() instanceof EntityPlayer)) {
                this.setAttackTarget(null);
            }
            return flag;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // 修改后属性：7点血，移速减少 11% (从之前提高后的 0.2875D 变成 0.2875 * 0.89 ≈ 0.2558D)
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(7.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2558D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        // 将血螨的索敌范围改成8格
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(8.0D);
    }

    // 攻击玩家时的特效（40% 概率赋予 20秒 饥饿 I，78% 概率赋予 0.4秒 失明）
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);

        return flag;
    }

    // --- 以下全是末影螨原版的杂项特性 ---

    // 模型会用到偏航角度（让它爬行时身体扭动）
    @Override
    public float getEyeHeight() {
        return 0.1F;
    }

    @Override
    public double getYOffset() {
        return 0.1D;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    // 音效使用我们新注册的血螨声音（它们在json中映射到了原版末影螨音频）
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BLOOD_MITE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.BLOOD_MITE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BLOOD_MITE_DEATH;
    }

    // 属于节肢动物（原版末影螨就是节肢动物）
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.Block blockIn) {
        this.playSound(ModSounds.BLOOD_MITE_STEP, 0.15F, 1.0F);
    }

    // 原版末影螨的独特逻辑：在灵魂沙上走得慢（如果需要的话，或者可以省略）
    @Override
    public void onUpdate() {
        // 原版末影螨有粒子效果，为了显得更"血腥"，我们也可以加点原版的粒子，或者去掉。
        // 这里为了纯粹的血肉怪物，暂时不加末影粒子。如果需要可以随时加。
        super.onUpdate();
        
        // 遇水秒死：只检测原版水方块（WATER/FLOWING_WATER），而非所有WATER材质的流体
        // 这样模组自己的血液流体就不会秒杀血螨，而岩浆等其他流体会保持原版特性（慢慢烫死）
        net.minecraft.block.state.IBlockState state = this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.posX, this.posY + this.getEyeHeight(), this.posZ));
        net.minecraft.block.Block block = state.getBlock();
        if (block == net.minecraft.init.Blocks.WATER || block == net.minecraft.init.Blocks.FLOWING_WATER) {
            this.attackEntityFrom(DamageSource.DROWN, 100.0F); // 造成巨大伤害直接死亡
        }
    }

    // 禁用自然生成，只能通过刷怪蛋、指令或代码生成
    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        // 标记为刷怪蛋生成
        this.isSpawnedByEgg = true;
        return livingdata;
    }

    @Override
    public boolean getCanSpawnHere() {
        // 如果是刷怪蛋生成，允许生成
        if (this.isSpawnedByEgg) {
            return super.getCanSpawnHere();
        }
        // 否则不允许自然生成（血螨通过投掷物生成）
        return false;
    }

    @Override
    public void onDeath(net.minecraft.util.DamageSource cause) {
        super.onDeath(cause);
        // 增加击杀统计，使玩家击杀血螨能获得原版怪物猎人成就
        net.minecraft.entity.Entity entity = cause.getTrueSource();
        if (entity instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) entity;
            // MOB_KILLS 是怪物击杀总统计，触发怪物猎人成就
            player.addStat(net.minecraft.stats.StatList.MOB_KILLS, 1);
        }

        // 50%概率掉落1个血螨肉
        if (!this.world.isRemote && this.rand.nextFloat() < 0.5F) {
            this.dropItem(com.qiamao.blood.init.ModItems.BLOOD_MITE_MEAT, 1);
        }
    }
}
