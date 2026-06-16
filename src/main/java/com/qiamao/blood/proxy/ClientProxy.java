package com.qiamao.blood.proxy;

import com.qiamao.blood.client.ModelRegistryHandler;
import com.qiamao.blood.client.event.ClientEventHandler;
import com.qiamao.blood.client.event.DesireSkyRenderHandler;
import com.qiamao.blood.client.particle.ParticleBloodDrop;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new DesireSkyRenderHandler());
    }

    @Override
    public void spawnBloodDropParticle(World world, double x, double y, double z, double speedX, double speedY, double speedZ) {
        if (world.isRemote) {
            ParticleBloodDrop particle = new ParticleBloodDrop(
                world, 
                x, y, z, 
                speedX, speedY, speedZ, 
                ModelRegistryHandler.BLOOD_DROP_PARTICLE_SPRITE
            );
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }

    @Override
    public void addScheduledTask(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }
}
