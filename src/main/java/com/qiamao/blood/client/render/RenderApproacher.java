package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.client.model.ModelApproacher;
import com.qiamao.blood.entity.EntityApproacher;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderApproacher extends RenderLiving<EntityApproacher> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/approacher.png");

    public RenderApproacher(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelApproacher(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityApproacher entity) {
        return TEXTURE;
    }
}
