package com.qiamao.blood.entity;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.World;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 血液猎犬
 * 暂时使用原版狼的模型和纹理
 */
public class EntityBloodHound extends EntityWolf {

    public EntityBloodHound(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.85F);
        // 默认处于激怒状态
        this.setAngry(true);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new net.minecraft.entity.ai.EntityAISwimming(this));
        this.tasks.addTask(4, new net.minecraft.entity.ai.EntityAIAttackMelee(this, 1.35D, true)); // 奔跑速度是原版的1.35倍
        this.tasks.addTask(5, new net.minecraft.entity.ai.EntityAIWanderAvoidWater(this, 1.0D)); // 闲时移动，速度和原版狼一样
        this.tasks.addTask(6, new net.minecraft.entity.ai.EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new net.minecraft.entity.ai.EntityAILookIdle(this));

        this.targetTasks.addTask(1, new net.minecraft.entity.ai.EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, com.qiamao.blood.entity.EntityBloodEndermite.class, true));
    }

    @Override
    public boolean processInteract(EntityPlayer player, net.minecraft.util.EnumHand hand) {
        // 不可驯服
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(37.0D); // 血量37
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D); // 原版狼的基础速度
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 4 + this.rand.nextInt(4); // 经验掉落 4-7
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int count = this.rand.nextInt(3); // 0-2个烂肉
        for (int i = 0; i < count; ++i) {
            if (this.isBurning()) {
                this.dropItem(com.qiamao.blood.init.ModItems.COOKED_GORY_FLESH, 1);
            } else {
                this.dropItem(com.qiamao.blood.init.ModItems.GORY_FLESH, 1);
            }
        }
    }

    @Override
    protected float getSoundPitch() {
        // Minecraft引擎中，降低音调(Pitch)会原生等比例降低倍速(Speed)。
        // 设置为 0.575F (0.5音调与0.65倍速的平均折中值) 可以完美实现那种沉闷、缓慢的血肉怪物音效
        return 0.575F;
    }

    @Override
    public boolean getCanSpawnHere() {
        // 限制只在主世界地表生成
        if (this.world.provider.getDimension() != 0) {
            return false;
        }
        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if (!this.world.canSeeSky(pos)) {
            return false;
        }
        return super.getCanSpawnHere();
    }

    /**
     * 重写尾巴的旋转角度，防止因为最大生命值改变导致的原版计算错误（尾巴360度旋转）
     */
    @Override
    public float getTailRotation() {
        if (this.isAngry()) {
            return 1.5393804F; // 激怒状态下尾巴竖起（原版的固定角度）
        } else {
            // 根据生命值比例计算，防止溢出
            return (0.55F - (float)(this.getMaxHealth() - this.getHealth()) * 0.02F) * (float)Math.PI;
        }
    }
}
