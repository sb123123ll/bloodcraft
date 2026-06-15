package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBloodMother extends ModelBase {
	private final ModelRenderer section_2;
	private final ModelRenderer weibadiyijie;
	private final ModelRenderer section_0;
	private final ModelRenderer section_1;
	private final ModelRenderer section_3;

	public ModelBloodMother() {
		this.textureWidth = 64;
		this.textureHeight = 64;

		this.section_2 = new ModelRenderer(this);
		this.section_2.setRotationPoint(0.0F, 24.0F, 2.5F);
		
		this.section_0 = new ModelRenderer(this);
		this.section_0.setRotationPoint(0.0F, 0.0F, -2.5F);
		this.section_2.addChild(this.section_0);
		this.section_0.setTextureOffset(11, 23).addBox(-2.0F, -2.0F, -4.4F, 4, 2, 2, 0.0F);
		this.section_0.setTextureOffset(0, 20).addBox(-3.0F, -2.0F, -6.4F, 1, 1, 3, 0.0F);
		this.section_0.setTextureOffset(0, 24).addBox(2.0F, -2.0F, -6.4F, 1, 1, 3, 0.0F);

		this.section_1 = new ModelRenderer(this);
		this.section_1.setRotationPoint(0.0F, 0.0F, -2.5F);
		this.section_2.addChild(this.section_1);
		this.section_1.setTextureOffset(15, 34).addBox(-2.0F, -3.0F, -2.4F, 4, 3, 9, 0.0F);
		this.section_1.setTextureOffset(34, 15).addBox(-3.0F, -1.0F, -3.0F, 1, 1, 10, 0.0F);
		this.section_1.setTextureOffset(41, 26).addBox(2.0F, -1.0F, -3.0F, 1, 1, 10, 0.0F);
		this.section_1.setTextureOffset(8, 0).addBox(2.0F, -2.0F, -1.0F, 1, 1, 7, 0.0F);
		this.section_1.setTextureOffset(0, 12).addBox(-3.0F, -2.0F, -1.0F, 1, 1, 7, 0.0F);

		this.section_3 = new ModelRenderer(this);
		this.section_3.setRotationPoint(0.0F, 0.0F, -2.5F);
		this.section_2.addChild(this.section_3);
		this.section_3.setTextureOffset(8, 8).addBox(-1.5F, -2.0F, 6.5F, 3, 2, 1, 0.0F);

		this.weibadiyijie = new ModelRenderer(this);
		this.weibadiyijie.setRotationPoint(0.0F, 0.0F, -2.5F);
		this.section_2.addChild(this.weibadiyijie);
		this.weibadiyijie.setTextureOffset(4, 9).addBox(-0.5F, -2.0F, 7.5F, 1, 2, 1, 0.0F);
		this.weibadiyijie.setTextureOffset(24, 7).addBox(-0.5F, -1.0F, 8.0F, 1, 1, 1, 0.0F);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.section_2.render(scale);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		float speedMultiplier = 1.0F;
		if (entityIn instanceof com.qiamao.blood.entity.EntityBloodMother) {
			if (((com.qiamao.blood.entity.EntityBloodMother) entityIn).isCharging()) {
				speedMultiplier = 1.75F;
			}
		}

		// 动画分节摆动，应用速度倍率，减小摆动幅度系数
		float baseSpeed = ageInTicks * 0.4F * speedMultiplier;
		this.section_0.rotateAngleY = MathHelper.sin(baseSpeed + 0.0F) * 0.10F * (float)Math.PI;
		this.section_1.rotateAngleY = MathHelper.sin(baseSpeed + 0.4F) * 0.10F * (float)Math.PI;
		this.section_3.rotateAngleY = MathHelper.sin(baseSpeed + 0.8F) * 0.05F * (float)Math.PI;
		this.weibadiyijie.rotateAngleY = MathHelper.sin(baseSpeed + 1.2F) * 0.08F * (float)Math.PI;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}