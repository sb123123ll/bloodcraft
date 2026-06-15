package com.qiamao.blood.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class EntityThrownBloodMite extends EntityThrowable implements IEntityAdditionalSpawnData {

    // 追踪机制变量
    private boolean isFromMother = false;
    private int trackTimer = 30; // 追踪1.5秒 (30 ticks)
    private EntityPlayer targetPlayer = null;

    public EntityThrownBloodMite(World worldIn) {
        super(worldIn);
    }

    public EntityThrownBloodMite(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
        if (throwerIn instanceof EntityBloodMother) {
            this.isFromMother = true;
            // 喷出瞬间识别最近玩家
            this.targetPlayer = this.world.getNearestAttackablePlayer(throwerIn.posX, throwerIn.posY, throwerIn.posZ, 64.0D, 64.0D, null, null);
        }
    }

    public EntityThrownBloodMite(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }
    
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
        buffer.writeBoolean(this.isFromMother);
        buffer.writeInt(this.targetPlayer != null ? this.targetPlayer.getEntityId() : -1);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.motionX = additionalData.readDouble();
        this.motionY = additionalData.readDouble();
        this.motionZ = additionalData.readDouble();
        this.isFromMother = additionalData.readBoolean();
        int targetId = additionalData.readInt();
        if (targetId != -1) {
            net.minecraft.entity.Entity entity = this.world.getEntityByID(targetId);
            if (entity instanceof EntityPlayer) {
                this.targetPlayer = (EntityPlayer) entity;
            }
        }
    }

    @Override
    public void onUpdate() {
        // 自定义运动逻辑：忽略草丛、花等可替换植物的碰撞
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onEntityUpdate();

        // 如果已经死亡或处于地面上，不执行飞行逻辑
        if (this.isDead) {
            return;
        }

        // 计算下一帧的位置
        Vec3d currentPos = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        // 执行射线检测，查看飞行路径上有无碰撞
        RayTraceResult rayResult = this.world.rayTraceBlocks(currentPos, nextPos, false, true, false);

        // 优化：只有当目标有一定速度（且距离较近）时，才检测实体碰撞，避免过远的 AABB 扫描
        double travelDistance = currentPos.distanceTo(nextPos);
        EntityLivingBase thrower = this.getThrower();
        RayTraceResult entityRay = null;
        
        if (thrower != null && travelDistance > 0.01D) {
            entityRay = this.findEntityOnPath(currentPos, nextPos, thrower);
        }

        // 优先处理实体碰撞
        if (entityRay != null) {
            // 被母体喷出的投掷物不会被母体自己的碰撞箱触发
            if (this.isFromMother && entityRay.entityHit instanceof EntityBloodMother) {
                // Ignore collision with any Blood Mother
            } else {
                rayResult = entityRay;
            }
        }

        // 处理碰撞结果
        if (rayResult != null && rayResult.typeOfHit != RayTraceResult.Type.MISS) {
            // 如果是方块碰撞，检查是否可以穿过
            if (rayResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos hitPos = rayResult.getBlockPos();
                if (this.canPassThroughBlock(hitPos)) {
                    // 可以穿过的方块，忽略碰撞继续飞行
                    rayResult = null;
                }
            }
        }

        // 处理追踪逻辑 (仅由母体发出且有追踪目标)
        if (this.isFromMother && this.targetPlayer != null && this.targetPlayer.isEntityAlive() && this.trackTimer > 0) {
            this.trackTimer--;
            
            // 计算朝向玩家中心的方向
            double dx = this.targetPlayer.posX - this.posX;
            double dy = (this.targetPlayer.posY + (double)this.targetPlayer.getEyeHeight() / 2.0D) - this.posY;
            double dz = this.targetPlayer.posZ - this.posZ;
            
            // 向量归一化
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.01D) {
                // 当前速度大小
                double currentSpeed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                if (currentSpeed < 0.1D) currentSpeed = 0.5D; // 保证有一定速度
                
                // 缓慢转向目标 (插值)
                double turnSpeed = 0.15D; // 转向灵敏度
                this.motionX += (dx / dist * currentSpeed - this.motionX) * turnSpeed;
                this.motionY += (dy / dist * currentSpeed - this.motionY) * turnSpeed;
                this.motionZ += (dz / dist * currentSpeed - this.motionZ) * turnSpeed;
            }
        }

        // 应用碰撞结果
        if (rayResult != null && rayResult.typeOfHit != RayTraceResult.Type.MISS) {
            this.onImpact(rayResult);
            this.isAirBorne = true;
        } else {
            // 正常更新位置
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            // 应用空气阻力
            float f = 0.98F;
            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
                }
                f = 0.8F;
            }

            this.motionX *= f;
            this.motionY *= f;
            this.motionZ *= f;

            // 应用重力
            this.motionY -= this.getGravityVelocity();

            this.setPosition(this.posX, this.posY, this.posZ);
            this.isAirBorne = true;
        }

        // 飞行时生成密集的血肉粒子（仅客户端）
        if (this.world.isRemote) {
            spawnTrailParticles();
        }
    }

    /**
     * 检查方块是否可以穿过（草丛、花等植物）
     */
    private boolean canPassThroughBlock(BlockPos pos) {
        if (pos == null) return false;
        Material material = this.world.getBlockState(pos).getMaterial();
        // 可穿过的材料：植物、藤蔓、空气、水等
        return material == Material.PLANTS ||
               material == Material.VINE ||
               material == Material.AIR ||
               material == Material.WATER ||
               material == Material.LAVA ||
               material == Material.FIRE ||
               material == Material.WEB ||
               material == Material.SNOW ||
               material == Material.CIRCUITS || // 红石线等
               material == Material.PORTAL;
    }

    /**
     * 查找路径上的实体
     */
    private RayTraceResult findEntityOnPath(Vec3d start, Vec3d end, EntityLivingBase thrower) {
        EntityLivingBase target = null;
        RayTraceResult result = null;
        double minDist = 0.0D;

        // 获取飞行路径上的所有实体
        for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D))) {
            if (entity != thrower && entity.canBeCollidedWith()) {
                AxisAlignedBB bb = entity.getEntityBoundingBox().grow(0.3D);
                RayTraceResult trace = bb.calculateIntercept(start, end);
                if (trace != null) {
                    double dist = start.squareDistanceTo(trace.hitVec);
                    if (dist < minDist || minDist == 0.0D) {
                        target = entity;
                        result = trace;
                        minDist = dist;
                    }
                }
            }
        }

        if (target != null) {
            result = new RayTraceResult(target);
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    private void spawnTrailParticles() {
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.rand.nextDouble() - 0.5D) * 0.3D;
            double offsetY = (this.rand.nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (this.rand.nextDouble() - 0.5D) * 0.3D;
            
            double speedX = (this.rand.nextDouble() - 0.5D) * 0.05D;
            double speedY = (this.rand.nextDouble() - 0.5D) * 0.05D;
            double speedZ = (this.rand.nextDouble() - 0.5D) * 0.05D;
            
            // 通过代理安全地生成粒子
            com.qiamao.blood.BloodMod.proxy.spawnBloodDropParticle(
                this.world, 
                this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ, 
                speedX, speedY, speedZ
            );
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            // 落地之后会变成实体血螨攻击玩家
            EntityBloodEndermite mite = new EntityBloodEndermite(this.world);
            
            // 防止生成的血螨卡在墙里：如果撞击到方块，将生成位置沿着撞击面的法线向外偏移一小段距离
            double spawnX = this.posX;
            double spawnY = this.posY;
            double spawnZ = this.posZ;
            
            if (result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit != null) {
                // 偏移从0.5减小到0.15，生成位置更贴近方块表面，同时避免卡墙
                spawnX += result.sideHit.getFrontOffsetX() * 0.15D;
                spawnY += result.sideHit.getFrontOffsetY() * 0.15D;
                spawnZ += result.sideHit.getFrontOffsetZ() * 0.15D;
            }
            
            mite.setLocationAndAngles(spawnX, spawnY, spawnZ, this.rotationYaw, 0.0F);
            this.world.spawnEntity(mite);
            
            this.setDead();
        }
    }
    
    @Override
    protected float getGravityVelocity() {
        // 加快落地时间：之前是 0.015F，飘在空中将近 3 秒。
        // 现在提升到 0.035F，大概会在 1~1.5 秒内落地，减少滞空感。
        return 0.035F;
    }
}
