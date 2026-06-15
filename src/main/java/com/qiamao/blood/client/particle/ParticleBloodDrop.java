package com.qiamao.blood.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

public class ParticleBloodDrop extends Particle {

    public ParticleBloodDrop(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite sprite) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        
        // 设置初始速度 (带有随机飞溅)
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        
        // 随机调整粒子大小，使其不规则 (大小从 0.5 到 1.5 倍基础大小)
        this.particleScale = this.rand.nextFloat() * 1.0F + 0.5F;
        
        // 随机颜色微调，让它有的是暗红、有的是鲜红，增加层次感
        float colorVariant = 0.6F + this.rand.nextFloat() * 0.4F; // 0.6 到 1.0
        this.particleRed = colorVariant; 
        this.particleGreen = 0.0F; // 纯红，或者稍微加一点点黑
        this.particleBlue = 0.0F;
        
        // 粒子寿命：20到40 tick (1~2秒)
        this.particleMaxAge = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
        
        // 开启物理碰撞，让血滴能落在地上
        this.canCollide = true;
        
        // 设置重力，血滴比较重
        this.particleGravity = 1.0F;
        
        // 设置绑定的自定义贴图
        this.setParticleTexture(sprite);
    }
    
    @Override
    public int getFXLayer() {
        // 返回 1 表示使用自定义的 TextureAtlasSprite (通过 TextureMap.LOCATION_BLOCKS_TEXTURE 绑定的图集)
        return 1;
    }
    
    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.motionY -= 0.04D * (double)this.particleGravity; // 模拟重力下落
        this.move(this.motionX, this.motionY, this.motionZ);
        
        // 摩擦力和空气阻力
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround) {
            // 落地后速度迅速减小，像黏在地上
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }
}
