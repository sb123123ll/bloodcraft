package com.qiamao.blood.client.render;

import com.qiamao.blood.entity.EntitySplashBlood;
import com.qiamao.blood.init.ModItems;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSplashBlood extends RenderSnowball<EntitySplashBlood> {
    public RenderSplashBlood(RenderManager renderManagerIn, RenderItem itemRendererIn) {
        super(renderManagerIn, ModItems.SPLASH_BLOOD_BOTTLE, itemRendererIn);
    }

    @Override
    public ItemStack getStackToRender(EntitySplashBlood entityIn) {
        return entityIn.getItem();
    }
}
