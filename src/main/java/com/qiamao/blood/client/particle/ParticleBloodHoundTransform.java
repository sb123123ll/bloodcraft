package com.qiamao.blood.client.particle;

import net.minecraft.client.particle.ParticleSpit;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBloodHoundTransform extends ParticleSpit {

    public ParticleBloodHoundTransform(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        
        // 存在时间 5-7 秒 (100 - 140 ticks)
        this.particleMaxAge = 100 + this.rand.nextInt(41);
        
        // 设置颜色为深红色调
        float redBase = 0.7F + this.rand.nextFloat() * 0.3F; // 0.7 - 1.0
        float greenBase = this.rand.nextFloat() * 0.1F; // 0.0 - 0.1
        float blueBase = this.rand.nextFloat() * 0.1F; // 0.0 - 0.1
        this.setRBGColorF(redBase, greenBase, blueBase);
        
        // 调整初始速度，使喷出直径大约为 2 格。由于每 tick 速度都会衰减，需要一个适中的初速度
        this.motionX = xSpeedIn * 0.5D;
        this.motionY = ySpeedIn * 0.5D + 0.1D; // 给一个向上的初速度，让它呈抛物线
        this.motionZ = zSpeedIn * 0.5D;
        
        // 粒子大小可以稍微大一点点
        this.particleScale *= 1.5F;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        // 原版羊驼口水动画有几帧，我们减缓它的帧播放速度，因为存在时间变长了
        int frameCount = 4;
        int currentFrame = (this.particleAge * frameCount) / this.particleMaxAge;
        if (currentFrame > 3) currentFrame = 3;
        this.setParticleTextureIndex(32 + currentFrame);

        // 物理下落 (重力)
        this.motionY -= 0.008D; // 原版是 0.004D，我们适当增加重力或者保持
        
        this.move(this.motionX, this.motionY, this.motionZ);

        // 摩擦力 / 速度衰减
        this.motionX *= 0.95D; // 原版是 0.9D，我们让它飘得远一点点再停
        this.motionY *= 0.95D;
        this.motionZ *= 0.95D;

        if (this.onGround) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;
        }
    }
}
