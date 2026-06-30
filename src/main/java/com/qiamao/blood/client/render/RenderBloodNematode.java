package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.client.model.ModelBloodNematode;
import com.qiamao.blood.entity.EntityBloodNematode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBloodNematode extends RenderLiving<EntityBloodNematode> {

    // 假设贴图文件名为 blood_nematode.png
    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/blood_nematode.png");

    public RenderBloodNematode(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBloodNematode(), 0.18F); // 阴影大小也按比例缩小 (0.3 * 0.6 = 0.18)
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodNematode entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityBloodNematode entity, float partialTickTime) {
        // 模型整体缩放至原来的 68%
        GlStateManager.scale(0.68F, 0.68F, 0.68F);

        // 如果正在吸血，我们需要将模型翻转（脚朝天）并贴在屏幕上方
        if (entity.isSuckingBlood()) {
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F); // 上下翻转
            GlStateManager.translate(0.0F, -0.6F, 0.0F); // 调整位置贴近脸部
        }
    }
}
