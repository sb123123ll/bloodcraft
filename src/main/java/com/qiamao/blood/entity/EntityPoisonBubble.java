package com.qiamao.blood.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPoisonBubble extends EntityThrowable implements IEntityAdditionalSpawnData {

    public EntityPoisonBubble(World worldIn) {
        super(worldIn);
    }

    public EntityPoisonBubble(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityPoisonBubble(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    /**
     * Forge 客户端数据同步：写入初始速度，防止客户端抛射物掉线/不移动/隐形
     */
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
    }

    /**
     * Forge 客户端数据同步：读取初始速度
     */
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.motionX = additionalData.readDouble();
        this.motionY = additionalData.readDouble();
        this.motionZ = additionalData.readDouble();
    }

    /**
     * 重力设置为 0，让泡泡发射后呈绝对直线飞行，没有抛物线下坠
     */
    @Override
    protected float getGravityVelocity() {
        return 0.0F; // 0 重力，直线飞行
    }

    /**
     * 撞击时的逻辑
     */
    @Override
    protected void onImpact(RayTraceResult result) {
        if (this.world.isRemote) return;

        // 如果撞击到方块，检查是否是可穿过的植物/装饰性方块
        if (result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos() != null) {
            net.minecraft.block.Block block = this.world.getBlockState(result.getBlockPos()).getBlock();

            // 可穿过的方块：草、枯灌木、蜘蛛网、藤蔓、花等植物
            if (isPassableBlock(block)) {
                // 直接穿过，不执行任何操作，继续飞行
                return;
            }
        }

        // 如果击中实体，造成伤害和效果
        if (result.entityHit != null) {
            // 造成 4 点伤害 (2颗心)
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 4.0F);

            // 如果是活体生物，施加虚弱 I (0级) 3秒 (60 ticks)
            if (result.entityHit instanceof EntityLivingBase) {
                ((EntityLivingBase) result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
            }
        }

        // 撞击后生成一些粘液粒子 (原版史莱姆粒子)
        this.world.setEntityState(this, (byte) 3);

        // 实体消失
        this.setDead();
    }

    /**
     * 检查方块是否可穿过（投射物可以飞过）
     */
    private boolean isPassableBlock(net.minecraft.block.Block block) {
        return block == net.minecraft.init.Blocks.TALLGRASS ||
               block == net.minecraft.init.Blocks.DEADBUSH ||
               block == net.minecraft.init.Blocks.WEB ||
               block == net.minecraft.init.Blocks.VINE ||
               block == net.minecraft.init.Blocks.WATERLILY ||
               block == net.minecraft.init.Blocks.RED_FLOWER ||
               block == net.minecraft.init.Blocks.YELLOW_FLOWER ||
               block == net.minecraft.init.Blocks.BROWN_MUSHROOM ||
               block == net.minecraft.init.Blocks.RED_MUSHROOM ||
               block == net.minecraft.init.Blocks.SAPLING ||
               block == net.minecraft.init.Blocks.REEDS ||
               block == net.minecraft.init.Blocks.CARPET ||
               block == net.minecraft.init.Blocks.SNOW_LAYER ||
               block instanceof net.minecraft.block.BlockBush ||
               block instanceof net.minecraft.block.BlockVine;
    }
}
