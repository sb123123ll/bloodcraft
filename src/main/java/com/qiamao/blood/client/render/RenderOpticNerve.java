package com.qiamao.blood.client.render;

import com.qiamao.blood.client.model.ModelOpticNerve;
import com.qiamao.blood.entity.EntityOpticNerve;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * 视神经 (Optic Nerve) 渲染器
 */
public class RenderOpticNerve extends RenderLiving<EntityOpticNerve> {

    // 贴图路径：assets/blood/textures/entity/optic_nerve.png
    private static final ResourceLocation TEXTURE = new ResourceLocation("blood", "textures/entity/optic_nerve.png");

    public RenderOpticNerve(RenderManager renderManagerIn) {
        // 使用刚刚创建的模型，设置阴影大小为 0.5F
        super(renderManagerIn, new ModelOpticNerve(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityOpticNerve entity) {
        return TEXTURE;
    }
}
