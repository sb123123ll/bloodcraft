package com.qiamao.blood.client.render;

import com.qiamao.blood.client.model.ModelBloodSeeker;
import com.qiamao.blood.entity.EntityBloodSeekerLarva;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBloodSeekerLarva extends RenderLiving<EntityBloodSeekerLarva> {

    // 复用成虫的贴图资源路径
    private static final ResourceLocation TEXTURE = new ResourceLocation("blood", "textures/entity/centipede_blood_seeker.png");

    public RenderBloodSeekerLarva(RenderManager renderManagerIn) {
        // 使用成虫的模型，阴影大小也缩小为成虫的1/3 (0.5F / 3 ≈ 0.15F)
        super(renderManagerIn, new ModelBloodSeeker(), 0.15F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodSeekerLarva entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityBloodSeekerLarva entitylivingbaseIn, float partialTickTime) {
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
        // 将模型缩小为原来的三分之一
        GlStateManager.scale(0.3333F, 0.3333F, 0.3333F);
    }
}