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

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote && this.ticksExisted > 160) {
            this.setDead();
        }
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

            // 如果碰到血腥木系列方块，触发小型爆炸
            if (isBloodWoodBlock(block)) {
                triggerSmallExplosion();
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
     * 触发小型爆炸：只有粒子和1-3点范围伤害，不破坏方块
     */
    private void triggerSmallExplosion() {
        // 在服务端不能直接生成自定义粒子，因为 Particle 类是 @SideOnly(Side.CLIENT) 的
        // 因此我们发送一个自定义的粒子生成网络包，或者通过设置实体状态在 handleStatusUpdate 中生成
        this.world.setEntityState(this, (byte) 100); // 使用自定义状态码 100 触发爆炸粒子
        
        // 播放爆炸音效
        this.world.playSound(null, this.posX, this.posY, this.posZ, 
            net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE, 
            net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.5F + this.rand.nextFloat() * 0.5F);

        // 对周围 2.5 格内的实体造成 1-3 点伤害
        java.util.List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5D));
        for (EntityLivingBase entity : entities) {
            // 注意：entity 和 this 都是不同类型，应该判断它们不是同一个对象，另外还要排除 thrower
            if (entity != this.getThrower()) {
                float damage = 1.0F + this.rand.nextFloat() * 2.0F; // 1.0 到 3.0 的伤害
                entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()).setExplosion(), damage);
            }
        }
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 100) {
            // 在客户端生成我们自定义的淡绿色高透明度爆炸粒子
            for (int i = 0; i < 10; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                double px = this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width;
                double py = this.posY + (double)(this.rand.nextFloat() * this.height);
                double pz = this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width;
                
                net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(
                    new com.qiamao.blood.client.particle.ParticlePoisonExplosion(this.world, px, py, pz, d0, d1, d2)
                );
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * 检查是否是血腥木系列方块
     */
    private boolean isBloodWoodBlock(net.minecraft.block.Block block) {
        return block == com.qiamao.blood.init.ModBlocks.BLOOD_LOG ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_PLANKS ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_DOOR ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_TRAPDOOR ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_SLAB_HALF ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_SLAB_DOUBLE ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_STAIRS ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_FENCE ||
               block == com.qiamao.blood.init.ModBlocks.BLOOD_FENCE_GATE;
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
