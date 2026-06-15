package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelShixueshishenjing extends ModelBase {
    public ModelRenderer zongti;
    public ModelRenderer Waist;
    public ModelRenderer Body;
    public ModelRenderer RightArm;
    public ModelRenderer LeftArm;
    public ModelRenderer RightLeg;
    public ModelRenderer LeftLeg;
    public ModelRenderer neck;
    public ModelRenderer Head;

    public ModelShixueshishenjing() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.zongti = new ModelRenderer(this, 0, 2);
        this.zongti.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.zongti.addBox(2.1F, -49.0F, 2.0F, 8, 1, 1, 0.0F);
        this.zongti.setTextureOffset(0, 0).addBox(-9.9F, -49.0F, 2.0F, 8, 1, 1, 0.0F);

        this.Waist = new ModelRenderer(this, 0, 0); 
        this.Waist.setRotationPoint(0.0F, -12.0F, 0.0F);
        this.zongti.addChild(this.Waist);

        this.Body = new ModelRenderer(this, 54, 0);
        this.Body.setRotationPoint(0.0F, -12.0F, 0.0F);
        this.Body.addBox(-4.0F, -4.0F, -2.0F, 8, 16, 4, 0.0F);
        this.Body.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F);
        this.Waist.addChild(this.Body);

        this.RightArm = new ModelRenderer(this, 40, 32);
        this.RightArm.setRotationPoint(-5.0F, -10.0F, 0.0F);
        this.RightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F);
        this.setRotationAngle(this.RightArm, -0.1745F, 0.0F, 0.0F);
        this.Waist.addChild(this.RightArm);

        this.LeftArm = new ModelRenderer(this, 48, 48);
        this.LeftArm.setRotationPoint(5.0F, -10.0F, 0.0F);
        this.LeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F);
        this.setRotationAngle(this.LeftArm, 0.2094F, 0.0F, 0.0F);
        this.Waist.addChild(this.LeftArm);

        this.RightLeg = new ModelRenderer(this, 23, 0);
        this.RightLeg.setRotationPoint(-1.9F, -12.0F, 0.0F);
        this.RightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.RightLeg.setTextureOffset(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F);
        this.setRotationAngle(this.RightLeg, 0.192F, 0.0F, 0.0349F);
        this.zongti.addChild(this.RightLeg);

        this.LeftLeg = new ModelRenderer(this, 32, 48);
        this.LeftLeg.setRotationPoint(1.9F, -12.0F, 0.0F);
        this.LeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.LeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F);
        this.setRotationAngle(this.LeftLeg, -0.1745F, 0.0F, -0.0349F);
        this.zongti.addChild(this.LeftLeg);

        this.neck = new ModelRenderer(this, 0, 0);
        this.neck.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.zongti.addChild(this.neck);

        this.Head = new ModelRenderer(this, 40, 0);
        this.Head.setRotationPoint(0.0F, -24.0F, 0.0F);
        this.Head.addBox(-2.0F, -32.0F, -2.0F, 4, 28, 3, 0.0F);
        this.setRotationAngle(this.Head, -0.1047F, 0.0873F, 0.0F);
        this.neck.addChild(this.Head);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.zongti.render(scale);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        
        // 头部动画，允许头部随视线旋转
        this.Head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.Head.rotateAngleX = headPitch * 0.017453292F;
    }
}
