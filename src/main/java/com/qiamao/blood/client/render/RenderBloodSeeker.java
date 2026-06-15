package com.qiamao.blood.client.render;

import com.qiamao.blood.client.model.ModelBloodSeeker;
import com.qiamao.blood.entity.EntityBloodSeeker;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBloodSeeker extends RenderLiving<EntityBloodSeeker> {

    // 绑定贴图资源路径
    private static final ResourceLocation TEXTURE = new ResourceLocation("blood", "textures/entity/centipede_blood_seeker.png");

    public RenderBloodSeeker(RenderManager renderManagerIn) {
        // 传入渲染管理器，实例化刚刚写的模型类，并设置阴影大小 (0.5F)
        super(renderManagerIn, new ModelBloodSeeker(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodSeeker entity) {
        return TEXTURE;
    }
}
