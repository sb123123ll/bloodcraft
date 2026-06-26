package com.qiamao.blood.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * 血液母体 - 放大15倍的血螨
 * Boss生物，拥有自定义血条
 */
public class EntityBloodMother extends EntityMob {

    // 冲撞相关状态
    private int chargeCooldown = 0; // 冲撞冷却时间（ticks）
    private int chargePrepareTime = 0; // 冲撞准备时间（ticks）
    private int miteSprayTimer = 0; // 血螨喷射计时器
    private int miteSprayDirection = 0; // 当前喷射方向（0-3）
    private int miteSprayCount = 0; // 当前方向已喷射次数

    private static final net.minecraft.network.datasync.DataParameter<Boolean> CHARGING = net.minecraft.network.datasync.EntityDataManager.<Boolean>createKey(EntityBloodMother.class, net.minecraft.network.datasync.DataSerializers.BOOLEAN);

    public EntityBloodMother(World worldIn) {
        super(worldIn);
        this.setSize(3.25F, 2.25F);
        this.experienceValue = 70;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHARGING, Boolean.valueOf(false));
    }

    @Override
    protected void initEntityAI() {
        // 游荡AI - 空闲时慢速移动（和玩家行走速度一样，0.1）
        this.tasks.addTask(5, new EntityAIWander(this, 0.1D));
        // 看向附近的玩家
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        // 发呆
        this.tasks.addTask(9, new EntityAILookIdle(this));

        // 冲撞AI - 优先级最高
        this.tasks.addTask(1, new com.qiamao.blood.entity.ai.EntityAIBloodMotherCharge(this));
        
        // 近战攻击AI - 使用自定义AI增加攻击距离
        this.tasks.addTask(2, new com.qiamao.blood.entity.ai.EntityAIBloodMotherAttack(this, 0.135D, false));

        // 目标AI - 只攻击玩家
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, false, false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(240.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.125D); // 空闲时慢速移动，提高25%
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0F); // 攻击伤害10滴血（5颗心）
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(25.0D);
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);

        if (flag && entityIn instanceof EntityLivingBase) {
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 1));
        }

        return flag;
    }

    @Override
    public float getEyeHeight() {
        return 2.0F;
    }

    @Override
    public double getYOffset() {
        return 0.0D;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    // 使用血液母体的声音
    @Override
    protected SoundEvent getAmbientSound() {
        return com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_DEATH;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.Block blockIn) {
        this.playSound(com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_STEP, 0.5F, 1.0F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        // 减慢身体动画速度为原来的一半
        this.limbSwingAmount *= 0.5F;
        
        // 处理冲撞冷却（只在冷却中时才检查）
        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
        }
        
        // 处理血螨喷射（只在计时器大于0时才处理）
        if (this.miteSprayTimer > 0) {
            this.miteSprayTimer--;
            if (this.miteSprayTimer <= 0) {
                sprayBloodMites();
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote && this.isEntityAlive() && this.handleWaterMovement()) {
            // 效仿末影人的遇水扣血：每秒造成1点伤害（每20ticks扣一次血）
            // 注意：handleWaterMovement() 会自动处理在水中的判定
            this.attackEntityFrom(DamageSource.DROWN, 1.0F);
        }
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean getCanSpawnHere() {
        // 只要不是和平模式，就允许生成。
        // Boss 生物的具体生成时机由结构生成器或玩家行为控制。
        return this.world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL && super.getCanSpawnHere();
    }

    @Override
    public void onDeath(net.minecraft.util.DamageSource cause) {
        super.onDeath(cause);
        
        net.minecraft.entity.Entity entity = cause.getTrueSource();
        if (entity instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) entity;
            player.addStat(net.minecraft.stats.StatList.MOB_KILLS, 1);
            
            // 玩家击杀血液母体，触发肉块地形生成
            if (!this.world.isRemote) {
                generateFleshTerrain(this.world, new net.minecraft.util.math.BlockPos(this));
            }
        }

        if (!this.world.isRemote) {
            this.dropItem(com.qiamao.blood.init.ModItems.BLOOD_MITE_MEAT, 5 + this.rand.nextInt(5));
            
            // 8% 概率掉落血液核心
            if (this.rand.nextFloat() < 0.08F) {
                this.dropItem(com.qiamao.blood.init.ModItems.BLOOD_CORE, 1);
            }
        }
    }

    /**
     * 在母体死亡位置生成散射状肉块地形
     */
    private void generateFleshTerrain(World world, net.minecraft.util.math.BlockPos center) {
        int radius = 3; // 6x6范围大约就是半径3
        java.util.Random random = this.rand;
        
        net.minecraft.block.state.IBlockState fleshState = com.qiamao.blood.init.ModBlocks.FLESH_CHUNK.getDefaultState();
        net.minecraft.block.state.IBlockState cursedFleshState = com.qiamao.blood.init.ModBlocks.BLOOD_SEEKER_FLESH.getDefaultState();
        net.minecraft.util.EnumFacing[] horizontals = net.minecraft.util.EnumFacing.Plane.HORIZONTAL.facings();
        
        // 遍历 6x6x6 立方体区域（允许一定高度差的地面）
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // 不规则散射：越靠近边缘概率越低
                double distSq = x * x + z * z;
                if (distSq > radius * radius) continue; // 裁掉四个角，形成近似圆形的散射
                
                // 距离中心越远，生成肉块的概率越低
                double spawnChance = 1.0 - (Math.sqrt(distSq) / (radius + 1.0));
                // 给概率加上一些随机抖动，使其看起来更破碎、散射
                spawnChance += (random.nextDouble() - 0.5) * 0.4;
                
                if (spawnChance > 0.2) {
                    // 寻找该 (x, z) 坐标最高处的固体方块（用于贴地生成）
                    net.minecraft.util.math.BlockPos targetCol = center.add(x, 0, z);
                    net.minecraft.util.math.BlockPos topBlock = world.getTopSolidOrLiquidBlock(targetCol).down(); // getTopSolidOrLiquidBlock 获取的是顶层方块的上方空气格
                    
                    // 确保不会离中心过远的高度差
                    if (Math.abs(topBlock.getY() - center.getY()) < 5) {
                        net.minecraft.util.EnumFacing randomFacing = horizontals[random.nextInt(horizontals.length)];
                        
                        // 中心区域（距离平方<=2）生成山峰形突起，高1-2格
                        if (distSq <= 2) {
                            int height = 1 + random.nextInt(2); // 1 或 2
                            // 先铺设底部普通肉块
                            for (int h = 0; h < height; h++) {
                                net.minecraft.util.math.BlockPos buildPos = topBlock.up(h + 1);
                                world.setBlockState(buildPos, fleshState.withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, horizontals[random.nextInt(horizontals.length)]), 3);
                            }
                            // 顶部固定为受诅咒的肉块
                            net.minecraft.util.math.BlockPos cursedPos = topBlock.up(height + 1);
                            world.setBlockState(cursedPos, cursedFleshState.withProperty(com.qiamao.blood.block.BlockBloodSeekerFlesh.FACING, randomFacing), 3);
                        } else {
                            // 非中心区域，只替换表层方块为普通肉块（或者直接放置在表面）
                            // 优先替换软地表，如果是硬方块则考虑在其上铺设
                            net.minecraft.block.state.IBlockState targetState = world.getBlockState(topBlock);
                            if (targetState.getMaterial().isReplaceable() || targetState.getBlock().isLeaves(targetState, world, topBlock)) {
                                world.setBlockState(topBlock, fleshState.withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, randomFacing), 3);
                            } else {
                                // 替换地表层
                                world.setBlockState(topBlock, fleshState.withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, randomFacing), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    // Getter和Setter方法
    public int getChargeCooldown() {
        return this.chargeCooldown;
    }

    public void setChargeCooldown(int cooldown) {
        this.chargeCooldown = cooldown;
    }

    public int getChargePrepareTime() {
        return this.chargePrepareTime;
    }

    public void setChargePrepareTime(int prepareTime) {
        this.chargePrepareTime = prepareTime;
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING).booleanValue();
    }

    public void setCharging(boolean charging) {
        this.dataManager.set(CHARGING, Boolean.valueOf(charging));
    }

    /**
     * 启动血螨喷射
     * @param delay 延迟时间（ticks）
     */
    public void startMiteSpray(int delay) {
        this.miteSprayTimer = delay;
        this.miteSprayDirection = 0;
        this.miteSprayCount = 0;
    }


    /**
     * 喷射血螨
     * 每半秒同时朝4个方向喷射（喷泉效果），共4次（总共16个）
     */
    private void sprayBloodMites() {
        if (this.world.isRemote) {
            return; // 只在服务端执行
        }

        // 计算喷射方向（基于血液母体的朝向）
        // 4个方向：左前、右前、左后、右后，都向上倾斜（喷泉效果）
        float baseYaw = this.rotationYaw;
        float[] yawOffsets = {-45, 45, -135, 135}; // 4个方向的偏移角度
        float pitch = -70; // 向上70度（Minecraft中pitch负值向上）
        
        // 每次同时生成4个血螨（4个方向各1个）
        for (int i = 0; i < 4; i++) {
            float yaw = baseYaw + yawOffsets[i];
            
            // 生成血螨投掷物
            com.qiamao.blood.entity.EntityThrownBloodMite mite = new com.qiamao.blood.entity.EntityThrownBloodMite(this.world, this);
            
            // 设置喷射方向和力度（喷泉力度）
            float velocity = 1.2F; // 增加力度，向上飞更远
            
            // 将球坐标转换为笛卡尔坐标
            double vx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * velocity;
            double vy = -Math.sin(Math.toRadians(pitch)) * velocity; // 取反，向上喷射
            double vz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * velocity;
            
            mite.shoot(vx, vy, vz, 0.5F, 0.1F);
            
            // 从背部位置生成（在实体后方），提高发射点高度和距离
            double spawnX = this.posX - Math.sin(Math.toRadians(baseYaw)) * 3.5D;
            double spawnY = this.posY + this.height * 1.3; // 提高到身体上方
            double spawnZ = this.posZ + Math.cos(Math.toRadians(baseYaw)) * 3.5D;
            
            mite.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, pitch);
            this.world.spawnEntity(mite);
        }
        
        // 播放喷射音效
        this.playSound(com.qiamao.blood.init.ModSounds.BLOOD_MOTHER_HURT, 0.8F, 1.2F);
        
        // 更新计数
        this.miteSprayCount++;
        
        // 检查是否完成所有喷射（共5次）
        if (this.miteSprayCount >= 5) {
            this.miteSprayCount = 0;
            this.miteSprayTimer = 0; // 确保计时器清零，防止无限循环
            return; // 喷射完成
        }
        
        // 设置下一次喷射的延迟（半秒 = 10 ticks）
        this.miteSprayTimer = 10;
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("ChargeCooldown", this.chargeCooldown);
        compound.setInteger("ChargePrepareTime", this.chargePrepareTime);
        compound.setBoolean("IsCharging", this.isCharging());
        compound.setInteger("MiteSprayTimer", this.miteSprayTimer);
        compound.setInteger("MiteSprayDirection", this.miteSprayDirection);
        compound.setInteger("MiteSprayCount", this.miteSprayCount);
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.chargeCooldown = compound.getInteger("ChargeCooldown");
        this.chargePrepareTime = compound.getInteger("ChargePrepareTime");
        this.setCharging(compound.getBoolean("IsCharging"));
        this.miteSprayTimer = compound.getInteger("MiteSprayTimer");
        this.miteSprayDirection = compound.getInteger("MiteSprayDirection");
        this.miteSprayCount = compound.getInteger("MiteSprayCount");
    }
}
