package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityParasiticSteve;
import com.qiamao.blood.client.model.ModelAnimatedMite;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerBloodMites implements LayerRenderer<EntityParasiticSteve> {
    private final RenderParasiticSteve renderer;
    private final ModelAnimatedMite miteModel = new ModelAnimatedMite();
    
    // 血螨的贴图
    private static final ResourceLocation MITE_TEX = new ResourceLocation(BloodMod.MODID, "textures/entity/blood_endermite.png");

    public LayerBloodMites(RenderParasiticSteve rendererIn) {
        this.renderer = rendererIn;
    }

    @Override
    public void doRenderLayer(EntityParasiticSteve entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(MITE_TEX);

        ModelPlayer steveModel = (ModelPlayer) this.renderer.getMainModel();

        // ========== 1. 头部的血螨 (眼睛2只，耳朵2只) ==========
        GlStateManager.pushMatrix();
        steveModel.bipedHead.postRender(0.0625F);
        
        // 1.1 左眼血管 (整体向下偏移一格 -> Y -0.28F 变成 -0.22F 左右，一格是 1/16 = 0.0625F，所以 -0.28 + 0.0625 = -0.2175F)
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.125F, -0.2175F, -0.2F); // 起点设在眼眶稍微偏里的位置
        GlStateManager.rotate(180F, 0F, 1F, 0F); // 转 180 度，向前方(-Z)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        // 每次渲染前给一个不同的相位偏移（利用固定的魔数）和稍微不同的速度
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 0.9F + 120.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();

        // 1.2 右眼血管
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.125F, -0.2175F, -0.2F);
        GlStateManager.rotate(180F, 0F, 1F, 0F); // 向前方(-Z)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 1.1F + 45.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();

        // 1.3 左耳血管
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.28F, -0.2175F, 0.0F); // 起点在左耳表面
        GlStateManager.rotate(90F, 0F, 1F, 0F); // 向左侧(+X)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 0.85F + 200.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();

        // 1.4 右耳血管
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.28F, -0.2175F, 0.0F); // 起点在右耳表面
        GlStateManager.rotate(-90F, 0F, 1F, 0F); // 向右侧(-X)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 1.05F + 330.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
        
        GlStateManager.popMatrix();

        // ========== 1.5 背部增加两条血管 ==========
        GlStateManager.pushMatrix();
        steveModel.bipedBody.postRender(0.0625F);
        
        // 背部左侧血管
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.15F, 0.3F, 0.15F); // 背面 (+Z)
        GlStateManager.rotate(0F, 0F, 1F, 0F); // 向后(+Z)钻出 (原模型就是向+Z长的，所以不需要转180度)
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 0.95F + 80.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();

        // 背部右侧血管
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.15F, 0.6F, 0.15F); // 背面 (+Z)，稍微高一点错落开
        GlStateManager.rotate(0F, 0F, 1F, 0F); // 向后(+Z)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 1.15F + 270.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
        
        GlStateManager.popMatrix();

        // ========== 2. 左手的血管 ==========
        GlStateManager.pushMatrix();
        steveModel.bipedLeftArm.postRender(0.0625F);
        // 手掌向前：调整 Z 轴坐标到手臂前方，并绕 Y 轴旋转 180 度指向 -Z（前方）
        GlStateManager.translate(0.05F, 0.65F, -0.15F);
        GlStateManager.rotate(180F, 0F, 1F, 0F); // 向前(-Z)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 0.9F + 15.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();

        // ========== 3. 右手的血管 ==========
        GlStateManager.pushMatrix();
        steveModel.bipedRightArm.postRender(0.0625F);
        GlStateManager.translate(-0.05F, 0.65F, -0.15F);
        GlStateManager.rotate(180F, 0F, 1F, 0F); // 向前(-Z)钻出
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.miteModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks * 1.1F + 190.0F, netHeadYaw, headPitch, scale, entity);
        this.miteModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}