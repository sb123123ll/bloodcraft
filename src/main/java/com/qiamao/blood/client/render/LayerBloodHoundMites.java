package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityBloodHound;
import com.qiamao.blood.client.model.ModelBloodHound;
import com.qiamao.blood.client.model.ModelAnimatedMite;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import java.lang.reflect.Field;

public class LayerBloodHoundMites implements LayerRenderer<EntityBloodHound> {
    private final RenderBloodHound renderer;
    private final ModelAnimatedMite miteModel = new ModelAnimatedMite();
    
    private static final ResourceLocation MITE_TEX = new ResourceLocation(BloodMod.MODID, "textures/entity/blood_endermite.png");

    private ModelRenderer cachedWolfBody;

    public LayerBloodHoundMites(RenderBloodHound rendererIn) {
        this.renderer = rendererIn;
    }

    private ModelRenderer getWolfBody(ModelWolf model) {
        if (cachedWolfBody != null) return cachedWolfBody;
        try {
            int count = 0;
            for (Field f : ModelWolf.class.getDeclaredFields()) {
                if (f.getType() == ModelRenderer.class) {
                    if (count == 1) { // 1 是 wolfBody
                        f.setAccessible(true);
                        cachedWolfBody = (ModelRenderer) f.get(model);
                        return cachedWolfBody;
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void doRenderLayer(EntityBloodHound entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(MITE_TEX);

        ModelBloodHound houndModel = (ModelBloodHound) this.renderer.getMainModel();
        ModelRenderer body = getWolfBody(houndModel);

        if (body == null) return;

        GlStateManager.pushMatrix();
        // 绑定到狼身体的坐标系
        body.postRender(0.0625F);

        // 统一缩放 0.2F (相当于血螨脑控 0.5F 的 0.4 倍)
        float miteScale = 0.2F;

        // ========== 1. 背部 (朝天) ==========
        // 身体盒子 z: -3 到 3。局部 -Z 是背部。
        float[][] backPos = {
            { 0.0F,  0.0F, -0.1875F },
            { 0.1F,  0.2F, -0.1875F },
            {-0.1F,  0.3F, -0.1875F }
        };
        for (int i = 0; i < backPos.length; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(backPos[i][0], backPos[i][1], backPos[i][2]);
            // 原始模型指向 +Z，所以转 180 度指向 -Z
            GlStateManager.rotate(180F, 1F, 0F, 0F);
            GlStateManager.scale(miteScale, miteScale, miteScale);
            this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * (0.9F + i * 0.1F) + i * 45.0F, netHeadYaw, headPitch, scale, entity);
            this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }

        // ========== 2. 屁股 (正后方) ==========
        // 身体盒子 y: -2 到 7。局部 +Y 是屁股。
        float[][] buttPos = {
            { 0.1F,  0.4375F, -0.05F },
            {-0.1F,  0.4375F,  0.05F }
        };
        for (int i = 0; i < buttPos.length; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(buttPos[i][0], buttPos[i][1], buttPos[i][2]);
            // 原始模型指向 +Z，转 -90 度绕 X 轴，使其指向 +Y
            GlStateManager.rotate(-90F, 1F, 0F, 0F);
            GlStateManager.scale(miteScale, miteScale, miteScale);
            this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * (1.1F + i * 0.1F) + i * 90.0F, netHeadYaw, headPitch, scale, entity);
            this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }

        // ========== 3. 腹部 (背部的反面) ==========
        // 身体盒子 z: -3 到 3。局部 +Z 是腹部。
        float[][] bellyPos = {
            { 0.0F,  0.1F, 0.1875F },
            {-0.1F,  0.3F, 0.1875F },
            { 0.1F,  0.0F, 0.1875F }
        };
        for (int i = 0; i < bellyPos.length; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(bellyPos[i][0], bellyPos[i][1], bellyPos[i][2]);
            // 原始模型指向 +Z，不需要旋转
            GlStateManager.scale(miteScale, miteScale, miteScale);
            this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * (0.85F + i * 0.15F) + i * 135.0F, netHeadYaw, headPitch, scale, entity);
            this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}