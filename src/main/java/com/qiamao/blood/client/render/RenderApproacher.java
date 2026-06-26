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
        // 阴影大小也按比例放大 (0.5F * 1.28 = 0.64F)
        super(renderManagerIn, new ModelApproacher(), 0.64F);
    }

    @Override
    protected void preRenderCallback(EntityApproacher entitylivingbaseIn, float partialTickTime) {
        // 将模型渲染大小缩放1.28倍
        net.minecraft.client.renderer.GlStateManager.scale(1.28F, 1.28F, 1.28F);
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityApproacher entity) {
        return TEXTURE;
    }
}
