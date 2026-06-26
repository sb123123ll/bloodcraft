package com.qiamao.blood.client.render;

import com.qiamao.blood.entity.EntityBloodHound;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerBloodHoundCollar implements LayerRenderer<EntityBloodHound> {
    private static final ResourceLocation WOLF_COLLAR = new ResourceLocation("textures/entity/wolf/wolf_collar.png");
    private final RenderBloodHound renderer;

    public LayerBloodHoundCollar(RenderBloodHound rendererIn) {
        this.renderer = rendererIn;
    }

    @Override
    public void doRenderLayer(EntityBloodHound entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isTamed() && !entitylivingbaseIn.isInvisible()) {
            this.renderer.bindTexture(WOLF_COLLAR);
            float[] afloat = entitylivingbaseIn.getCollarColor().getColorComponentValues();
            GlStateManager.color(afloat[0], afloat[1], afloat[2]);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
