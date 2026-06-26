package com.qiamao.blood.client.particle;

import net.minecraft.client.particle.ParticleExplosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticlePoisonExplosion extends ParticleExplosion {

    public ParticlePoisonExplosion(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        
        // 更改为淡绿色色调 (R: 0.4, G: 0.9, B: 0.4)
        // 深浅色对应：结合原版爆炸粒子自带的随机深浅机制（原版构造函数里有 this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.3F + 0.7F;）
        // 我们在它的基础上叠加我们的绿色调
        float shade = this.rand.nextFloat() * 0.3F + 0.7F;
        this.particleRed = shade * 0.4F;
        this.particleGreen = shade * 0.9F;
        this.particleBlue = shade * 0.4F;

        // 设置透明度为 75% (0.75F)，因为要求透明度拉高25% (原版是不透明1.0F，拉高25%透明度即变为0.75F)
        this.setAlphaF(0.75F);
    }
}