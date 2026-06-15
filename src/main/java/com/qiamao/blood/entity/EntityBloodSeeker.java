package com.qiamao.blood.entity;

import com.qiamao.blood.init.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.MathHelper;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBloodSeeker extends EntityMob implements IRangedAttackMob {

    // 用于同步毒泡泡发射动画的 DataParameter
    private static final DataParameter<Boolean> IS_SHOOTING = EntityDataManager.createKey(EntityBloodSeeker.class, DataSerializers.BOOLEAN);
    
    // 用于同步欲望事件状态的 DataParameter
    private static final DataParameter<Boolean> DESIRE_EVENT_ACTIVE = EntityDataManager.createKey(EntityBloodSeeker.class, DataSerializers.BOOLEAN);

    // 服务端发力前摇计时器
    private int shootTicks = 0;

    // 标记是否由刷怪蛋生成
    private boolean isSpawnedByEgg = false;
    private EntityLivingBase delayedTarget = null;
    
    // 客户端动画计时器
    @SideOnly(Side.CLIENT)
    public int clientShootTimer = 0;

    public EntityBloodSeeker(World worldIn) {
        super(worldIn);
        this.setSize(1.325F, 0.925F);
        this.stepHeight = 2.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(IS_SHOOTING, Boolean.valueOf(false));
        this.dataManager.register(DESIRE_EVENT_ACTIVE, Boolean.valueOf(false));
    }

    public void setShooting(boolean shooting) {
        this.dataManager.set(IS_SHOOTING, Boolean.valueOf(shooting));
    }

    public boolean isShooting() {
        return this.dataManager.get(IS_SHOOTING);
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
            double baseSpeed = 0.23D;
            double enhancedSpeed = baseSpeed * 1.25D;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(enhancedSpeed);
        } else {
            // 恢复正常移动速度
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        }
    }

    // --- 自定义 AI：带冷却时间的跳跃攻击（扑击） ---
    static class AILeapAtTargetWithCooldown extends EntityAIBase {
        private final EntityBloodSeeker leaper;
        private EntityLivingBase leapTarget;
        private final float leapMotionY;
        private final int cooldownTicks;
        private int currentCooldown = 0;

        public AILeapAtTargetWithCooldown(EntityBloodSeeker leapingEntity, float leapMotionYIn, int cooldown) {
            this.leaper = leapingEntity;
            this.leapMotionY = leapMotionYIn;
            this.cooldownTicks = cooldown;
            this.setMutexBits(5); // Mutex 5 代表阻止跳跃和移动
        }

        @Override
        public boolean shouldExecute() {
            if (this.currentCooldown > 0) {
                this.currentCooldown--;
                // 即使在冷却中，只要满足条件就返回false，不执行起跳
            }
            
            this.leapTarget = this.leaper.getAttackTarget();
            if (this.leapTarget == null) {
                return false;
            } else {
                double distance = this.leaper.getDistanceSq(this.leapTarget);
                // 距离小于4格（16的平方）且大于1格（避免贴脸乱跳）才扑击
                if (distance >= 1.0D && distance <= 16.0D) {
                    if (!this.leaper.onGround) {
                        return false;
                    } else if (this.currentCooldown <= 0) {
                        return true; // CD好了且在地上，100% 的概率起跳
                    }
                }
                return false;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.leaper.onGround;
        }

        @Override
        public void startExecuting() {
            double dX = this.leapTarget.posX - this.leaper.posX;
            double dZ = this.leapTarget.posZ - this.leaper.posZ;
            float f = MathHelper.sqrt(dX * dX + dZ * dZ);

            if ((double)f >= 1.0E-4D) {
                this.leaper.motionX += dX / (double)f * 0.5D * 0.800000011920929D + this.leaper.motionX * 0.20000000298023224D;
                this.leaper.motionZ += dZ / (double)f * 0.5D * 0.800000011920929D + this.leaper.motionZ * 0.20000000298023224D;
            }
            this.leaper.motionY = (double)this.leapMotionY;
            this.currentCooldown = this.cooldownTicks; // 重置3秒CD
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        
        // 害怕鹦鹉，距离小于5格会奔跑远离
        this.tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityParrot.class, 5.0F, 1.0D, 1.5D));
        
        // 增加晚上寻找顶棚躲避月光的AI，优先级高于攻击
        // 只有当欲望事件未激活时，才执行寻找遮挡的 AI
        this.tasks.addTask(2, new AISeekShadeAtNight(this, 1.5D) {
            @Override
            public boolean shouldExecute() {
                return !EntityBloodSeeker.this.isDesireEventActive() && super.shouldExecute();
            }
        });

        // 核心改动：自定义边走边射击并逼近的 AI
        // 速度 1.25D，射击间隔 40 ticks (2秒)，最大射程 15 格
        this.tasks.addTask(3, new AIAttackRangedAndApproach(this, 1.25D, 40, 15.0F));

        // 扑击AI (距离目标<=4时扑向目标，带3秒CD)
        this.tasks.addTask(4, new AILeapAtTargetWithCooldown(this, 0.6F, 60)); // 0.6F为跳跃力度, 60ticks(3秒)冷却
        
        // 近战攻击，攻击时移速为玩家奔跑速度的 1.75 倍 (基础速度0.23D * 1.75 ≈ 0.40D)
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.75D, false));
        
        // 闲时随机移动
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        
        // 目标 AI：被别的生物攻击会还击
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false) {
            @Override
            public boolean shouldExecute() {
                // 白天正常还击
                if (EntityBloodSeeker.this.world.isDaytime()) {
                    return super.shouldExecute();
                }
                // 晚上只有在欲望事件激活时才还击
                return EntityBloodSeeker.this.isDesireEventActive() && super.shouldExecute();
            }
        });
        // 目标 AI：主动攻击玩家、村民、动物、铁傀儡
        this.targetTasks.addTask(2, new AITargetUnderShade<>(this, EntityPlayer.class));
        this.targetTasks.addTask(3, new AITargetUnderShade<>(this, EntityVillager.class));
        this.targetTasks.addTask(4, new AITargetUnderShade<>(this, EntityAnimal.class));
        this.targetTasks.addTask(5, new AITargetUnderShade<>(this, net.minecraft.entity.monster.EntityIronGolem.class));
    }

    /**
     * 实现 IRangedAttackMob 接口的远程攻击方法
     */
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        // 计算距离判断是否该用远程 (距离大于 4 格，即大于 2 格距离的平方)
        if (this.getDistanceSq(target) > 16.0D) {
            // 触发发射前摇动画，并且记录目标，由 onUpdate 来延迟发射
            this.setShooting(true);
            this.shootTicks = 0;
            this.delayedTarget = target;
        }
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        // IRangedAttackMob 接口要求，这里可以留空
    }

    @Override
    public void onDeath(net.minecraft.util.DamageSource cause) {
        super.onDeath(cause);
        // 增加击杀统计，使玩家击杀多足嗜血虫能获得原版怪物猎人成就
        net.minecraft.entity.Entity entity = cause.getTrueSource();
        if (entity instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) entity;
            player.addStat(net.minecraft.stats.StatList.MOB_KILLS, 1);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            if (this.isShooting()) {
                this.shootTicks++;
                
                // 根据欲望事件状态调整发射时机
                int shootTick = this.isDesireEventActive() ? 6 : 8; // 欲望事件期间提前到第6tick发射
                int endTick = this.isDesireEventActive() ? 12 : 16; // 欲望事件期间第12tick结束
                
                // 发射毒泡泡
                if (this.shootTicks == shootTick && this.delayedTarget != null && !this.delayedTarget.isDead) {
                    EntityPoisonBubble bubble = new EntityPoisonBubble(this.world, this);

                    double d0 = this.delayedTarget.posX - this.posX;
                    double d1 = this.delayedTarget.posY - this.posY;
                    double d2 = this.delayedTarget.posZ - this.posZ;
                    
                    // 速度为 1.5F (满蓄力弓箭是 3.0F，所以这里是它的一半)，不确定度(散射)为 1.0F
                    // 移除了抛物线补偿机制 (d3 * 0.2D)，现在它发射的一瞬间会直直地朝着目标的当前位置飞去
                    bubble.shoot(d0, d1, d2, 1.5F, 1.0F);
                    
                    this.playSound(com.qiamao.blood.init.ModSounds.BLOOD_MITE_STEP, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
                    this.world.spawnEntity(bubble);
                }
                
                // 结束动画
                if (this.shootTicks >= endTick) {
                    this.setShooting(false);
                    this.shootTicks = 0;
                    this.delayedTarget = null;
                }
            }
        } else {
            // 客户端同步动画计时器
            if (this.isShooting()) {
                this.clientShootTimer++;
            } else {
                this.clientShootTimer = 0;
            }
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // 30点血 (15颗心)
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D); 
        // 闲时基础移动速度：0.23D (和原版僵尸一样)
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D); 
        // 攻击力 6 (3颗心)
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D); 
        // 索敌范围：15格
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(15.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        // 欲望事件期间的处理
        if (this.isDesireEventActive()) {
            // 欲望事件期间不自燃
            return;
        }
        
        // 日行怪物，月亮出来会自燃
        // !world.isDaytime() 表示晚上（月亮出来）
        // !world.isRemote 确保在服务端执行
        if (!this.world.isRemote && !this.world.isDaytime()) {
            // 如果不在水里并且没有抗火效果
            if (!this.isInWater() && !this.isImmuneToFire()) {
                // 如果头顶没有方块遮挡（即暴露在天空下），则自燃
                if (this.world.canBlockSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ))) {
                    // 核心修复：不要每 tick 都调用 setFire(8)。
                    // 如果每 tick 都调用，this.fire 会被锁定在 160，导致 (160 % 20 == 0) 永远成立，
                    // 从而无视 1秒/次的伤害间隔，变成只要无敌帧一过就受到伤害（烫死极快）。
                    // 我们只在火快灭的时候续上火，保证正常 1 秒烫 1 次。
                    // 但是 this.fire 是私有字段，不能直接访问。我们可以通过 isBurning() 判断，并且不用每 tick 都去点火
                    // 原版僵尸在阳光下自燃也是每 tick 调用 setFire(8) 的，但原版有处理 this.fire 避免无限受伤。
                    // 刚才烫死快的原因主要是由于无敌帧被清空。现在无敌帧清空逻辑已经修好，我们恢复简单的 setFire。
                    this.setFire(8);
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // 核心修复：为了满足“自燃时不免疫攻击时的击退”需求
        // 之前错误地清除了火焰伤害的无敌时间，导致它每 tick 都会受到火焰伤害，几秒就烫死。
        // 正确做法是：当它受到来自实体（如玩家、铁傀儡）的攻击时，如果它正在自燃，我们强行清除它的无敌时间。
        // 这样火焰依然遵循 1 秒烫 1 次的规则，但玩家的物理攻击和击退能随时穿透火焰的无敌帧。
        if (this.isBurning() && source.getTrueSource() != null) {
            this.hurtResistantTime = 0;
        }
        
        return super.attackEntityFrom(source, amount);
    }

    /**
     * 覆写 setInWeb 方法，并留空。
     * 这将使多足嗜血虫在穿过蜘蛛网时不会被设置减速标记，从而完全无视蜘蛛网的阻挡！
     */
    @Override
    public void setInWeb() {
    }

    // 属于节肢动物 (受节肢杀手附魔克制)
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    // 设置音效
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CENTIPEDE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.CENTIPEDE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CENTIPEDE_DEATH;
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        // 标记为刷怪蛋或刷怪笼等特殊方式生成（这个标记在原逻辑中被用来放宽生成条件）
        this.isSpawnedByEgg = true;

        return livingdata;
    }

    @Override
    public boolean getCanSpawnHere() {
        // 如果是刷怪蛋生成，跳过时间限制
        if (this.isSpawnedByEgg) {
            net.minecraft.block.state.IBlockState iblockstate = this.world.getBlockState((new BlockPos(this)).down());
            if (!iblockstate.canEntitySpawn(this)) {
                return false;
            }
            return !this.world.containsAnyLiquid(this.getEntityBoundingBox());
        }

        // 如果是超平坦世界，则绝对不自然生成
        if (this.world.getWorldType() == net.minecraft.world.WorldType.FLAT) {
            return false;
        }

        // 检查实体头顶是否有方块遮挡（看不见天空就视为洞穴）
        boolean canSeeSky = this.world.canBlockSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ));

        if (canSeeSky) {
            // 地表：只在白天自然生成 (0 到 12000 tick)
            long time = this.world.getWorldTime() % 24000;
            if (time >= 12000) {
                return false;
            }
        }
        // 如果是洞穴 (!canSeeSky)，则保持全天候刷新

        // 检查实体脚下是否是合法的方块，且不淹死在水里
        net.minecraft.block.state.IBlockState iblockstate = this.world.getBlockState((new BlockPos(this)).down());
        if (!iblockstate.canEntitySpawn(this)) {
            return false;
        }
        boolean canSpawn = !this.world.containsAnyLiquid(this.getEntityBoundingBox());
        
        // 如果满足自然生成条件，则有 35% 概率同时生成一只幼虫
        if (canSpawn && !this.world.isRemote && this.rand.nextFloat() < 0.35F) {
            EntityBloodSeekerLarva larva = new EntityBloodSeekerLarva(this.world);
            larva.setLocationAndAngles(this.posX + (this.rand.nextDouble() - 0.5D) * 2.0D, this.posY, this.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D, this.rotationYaw, 0.0F);
            this.world.spawnEntity(larva);
        }
        
        return canSpawn;
    }

    /**
     * 覆盖原版的亮度检查
     */
    @Override
    protected boolean isValidLightLevel() {
        boolean canSeeSky = this.world.canBlockSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ));
        if (!canSeeSky) {
            // 洞穴里：全天候不间断刷新，无视亮度条件（即使插了火把也会刷！）
            return true;
        } else {
            // 地表上：无视亮度（因为我们要白天高亮度生成，而且前面 getCanSpawnHere 已经限制了只在白天）
            return true;
        }
    }

    // 掉落物战利品表 (后续可自定义)
    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // 打死多足嗜血虫，掉落 2 到 4 个烂肉
        int count = 2 + this.rand.nextInt(3); // 2 + [0, 2] = 2 到 4
        // 如果有抢夺附魔，增加掉落量
        if (lootingModifier > 0) {
            count += this.rand.nextInt(lootingModifier + 1);
        }
        
        // 检查实体是否着火
        boolean isBurning = this.isBurning();
        
        for (int i = 0; i < count; ++i) {
            if (isBurning) {
                this.dropItem(com.qiamao.blood.init.ModItems.COOKED_GORY_FLESH, 1);
            } else {
                this.dropItem(com.qiamao.blood.init.ModItems.GORY_FLESH, 1);
            }
        }

        // 25% 的概率掉落多足虫的刺
        // 抢夺附魔可以稍微增加一点掉率
        float dropChance = 0.25F + (lootingModifier * 0.05F);
        if (this.rand.nextFloat() < dropChance) {
            this.dropItem(com.qiamao.blood.init.ModItems.CENTIPEDE_STINGER, 1);
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, net.minecraft.util.EnumHand hand) {
        net.minecraft.item.ItemStack itemstack = player.getHeldItem(hand);

        // 如果玩家拿着刷怪蛋，且正在潜行，右键可以生成一个幼虫
        if (itemstack.getItem() == net.minecraft.init.Items.SPAWN_EGG) {
            if (player.isSneaking()) {
                // 检查刷怪蛋是否是本生物的刷怪蛋
                ResourceLocation id = net.minecraft.item.ItemMonsterPlacer.getNamedIdFrom(itemstack);
                if (id != null && id.equals(new ResourceLocation(com.qiamao.blood.BloodMod.MODID, "centipede_blood_seeker"))) {
                    if (!this.world.isRemote) {
                        EntityBloodSeekerLarva larva = new EntityBloodSeekerLarva(this.world);
                        larva.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                        this.world.spawnEntity(larva);
                        
                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                    }
                    return true;
                }
            }
        }
        
        return super.processInteract(player, hand);
    }

    // --- 自定义 AI：晚上寻找有遮挡的地方 ---
    static class AISeekShadeAtNight extends EntityAIBase {
        private final EntityBloodSeeker seeker;
        private final double movementSpeed;
        private double shelterX;
        private double shelterY;
        private double shelterZ;

        public AISeekShadeAtNight(EntityBloodSeeker seeker, double speed) {
            this.seeker = seeker;
            this.movementSpeed = speed;
            this.setMutexBits(1); // 移动相关的AI
        }

        @Override
        public boolean shouldExecute() {
            if (this.seeker.world.isDaytime() || this.seeker.world.isRemote) {
                return false; // 白天不需要找遮挡
            }
            if (!this.seeker.world.canBlockSeeSky(new BlockPos(this.seeker.posX, this.seeker.posY + (double)this.seeker.getEyeHeight(), this.seeker.posZ))) {
                return false; // 已经在遮挡物下了
            }
            // 优化：每隔5个tick尝试寻找一次，而不是每tick都扫描
            if (this.seeker.ticksExisted % 5 != 0) {
                return false;
            }
            Vec3d vec3d = this.findPossibleShelter();
            if (vec3d == null) {
                return false; // 找不到遮挡物
            } else {
                this.shelterX = vec3d.x;
                this.shelterY = vec3d.y;
                this.shelterZ = vec3d.z;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.seeker.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.seeker.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
        }

        private Vec3d findPossibleShelter() {
            // 优化：尝试寻找附近的一个不在天空下的位置，只尝试5次（原为10次），并且减小搜索范围以提升性能
            for (int i = 0; i < 5; ++i) {
                BlockPos blockpos = new BlockPos(this.seeker.posX + (double)this.seeker.getRNG().nextInt(16) - 8.0D, this.seeker.posY + (double)this.seeker.getRNG().nextInt(4) - 2.0D, this.seeker.posZ + (double)this.seeker.getRNG().nextInt(16) - 8.0D);
                if (!this.seeker.world.canBlockSeeSky(blockpos)) {
                    return new Vec3d((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                }
            }
            return null;
        }
    }

    // --- 自定义 AI：只有当目标到自己这段路径上有遮挡（或自己在遮挡下）才攻击，否则干瞪眼 ---
    static class AITargetUnderShade<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
        private final EntityBloodSeeker seeker;

        public AITargetUnderShade(EntityBloodSeeker creature, Class<T> classTarget) {
            super(creature, classTarget, true);
            this.seeker = creature;
        }

        @Override
        public boolean shouldExecute() {
            if (!super.shouldExecute()) {
                return false;
            }
            // 白天正常工作
            if (this.seeker.world.isDaytime()) {
                return true;
            }
            // 晚上只有在欲望事件激活时才工作
            return this.seeker.isDesireEventActive();
        }
    }

    // --- 自定义 AI：边走边射击并逼近目标 ---
    static class AIAttackRangedAndApproach extends EntityAIBase {
        private final EntityBloodSeeker entityHost;
        private final double entityMoveSpeed;
        private final int attackInterval;
        private final float maxAttackDistanceSq;
        private int rangedAttackTime = -1;

        public AIAttackRangedAndApproach(EntityBloodSeeker attacker, double moveSpeed, int attackInterval, float maxAttackDistance) {
            this.entityHost = attacker;
            this.entityMoveSpeed = moveSpeed;
            this.attackInterval = attackInterval;
            this.maxAttackDistanceSq = maxAttackDistance * maxAttackDistance;
            this.setMutexBits(3); // 占用移动和视角
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.entityHost.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
                return false;
            }
            // 只要距离大于 4 格 (16.0D)，就执行边走边射 (如果在射程外也会去追)
            return this.entityHost.getDistanceSq(target) > 16.0D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = this.entityHost.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
                return false;
            }
            // 只要目标还在 4 格外，就继续执行远程和逼近
            return this.entityHost.getDistanceSq(target) > 16.0D;
        }

        @Override
        public void resetTask() {
            this.rangedAttackTime = -1;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.entityHost.getAttackTarget();
            if (target != null) {
                // 优化：只在一定 tick 间隔下调用寻路
                if (this.entityHost.ticksExisted % 4 == 0) {
                    this.entityHost.getNavigator().tryMoveToEntityLiving(target, this.entityMoveSpeed);
                }
                
                // 优化：降低视线更新频率
                if (this.entityHost.ticksExisted % 2 == 0) {
                    this.entityHost.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                }

                if (--this.rangedAttackTime <= 0) {
                    double distSq = this.entityHost.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
                    boolean canSee = this.entityHost.getEntitySenses().canSee(target);

                    if (canSee && distSq <= this.maxAttackDistanceSq) {
                        // 执行远程攻击
                        float f = MathHelper.sqrt(distSq) / MathHelper.sqrt(this.maxAttackDistanceSq);
                        float distanceFactor = MathHelper.clamp(f, 0.1F, 1.0F);
                        this.entityHost.attackEntityWithRangedAttack(target, distanceFactor);
                        this.rangedAttackTime = this.attackInterval;
                    } else if (this.rangedAttackTime < -this.attackInterval) {
                        // 防止无限积累负数
                        this.rangedAttackTime = this.attackInterval;
                    }
                }
            }
        }
    }
}
