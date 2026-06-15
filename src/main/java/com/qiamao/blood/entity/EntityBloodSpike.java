package com.qiamao.blood.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * 血刺 - 多足嗜血虫发射的远程投射物
 */
public class EntityBloodSpike extends EntityThrowable implements IEntityAdditionalSpawnData {

    public EntityBloodSpike(World worldIn) {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
    }

    public EntityBloodSpike(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityBloodSpike(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.motionX = additionalData.readDouble();
        this.motionY = additionalData.readDouble();
        this.motionZ = additionalData.readDouble();
    }

    /**
     * 低重力，轻微抛物线
     */
    @Override
    protected float getGravityVelocity() {
        return 0.02F;
    }

    /**
     * 撞击时的逻辑
     */
    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            if (result.entityHit != null) {
                // 造成 3 点伤害 (1.5颗心)
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 3.0F);
            }
            this.setDead();
        }
    }
}
