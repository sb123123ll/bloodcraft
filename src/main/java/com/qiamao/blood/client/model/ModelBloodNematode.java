package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class ModelBloodNematode extends ModelBase {
	private final ModelRenderer main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer cube_r5;
	private final ModelRenderer cube_r6;
	private final ModelRenderer main4;
	private final ModelRenderer main3;
	private final ModelRenderer main2;
	
	// 新增：独立出来的左右触角节点
	private final ModelRenderer antenna_left;
	private final ModelRenderer antenna_right;

	public ModelBloodNematode() {
		textureWidth = 256;
		textureHeight = 256;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);
		setRotationAngle(main, 0.0F, 3.1416F, 0.0F);
		
		// 身体核心部分
		main.cubeList.add(new ModelBox(main, 0, 0, -1.0F, -2.0F, -8.0F, 2, 2, 22, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 96, 48, 1.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 96, 86, -3.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 192, 0, -7.0F, 0.0F, -2.0F, 2, 0, 10, 0.0F, false));

		// --- 触角重构 ---
		// 左触角 (从 main 中剥离，设置独立的旋转中心点)
		antenna_left = new ModelRenderer(this);
		antenna_left.setRotationPoint(-1.5F, -2.0F, 12.0F);
		main.addChild(antenna_left);
		antenna_left.cubeList.add(new ModelBox(antenna_left, 88, 148, -0.5F, -1.0F, 0.0F, 1, 1, 1, 0.0F, false));
		antenna_left.cubeList.add(new ModelBox(antenna_left, 80, 148, -0.5F, -3.0F, 1.0F, 1, 1, 1, 0.0F, false));
		antenna_left.cubeList.add(new ModelBox(antenna_left, 80, 152, -0.5F, -2.0F, 1.0F, 1, 1, 1, 0.0F, false));
		antenna_left.cubeList.add(new ModelBox(antenna_left, 80, 156, -1.5F, -4.0F, 2.0F, 1, 1, 1, 0.0F, false));

		// 右触角
		antenna_right = new ModelRenderer(this);
		antenna_right.setRotationPoint(1.5F, -2.0F, 12.0F);
		main.addChild(antenna_right);
		antenna_right.cubeList.add(new ModelBox(antenna_right, 80, 144, -0.5F, -1.0F, 0.0F, 1, 1, 1, 0.0F, false));
		antenna_right.cubeList.add(new ModelBox(antenna_right, 88, 144, -0.5F, -2.0F, 1.0F, 1, 1, 1, 0.0F, false));
		antenna_right.cubeList.add(new ModelBox(antenna_right, 88, 152, -0.5F, -3.0F, 1.0F, 1, 1, 1, 0.0F, false));
		antenna_right.cubeList.add(new ModelBox(antenna_right, 88, 156, 0.5F, -4.0F, 2.0F, 1, 1, 1, 0.0F, false));

		// --- 尾巴重构：改为链式父子结构，支持像蝎子一样弯曲 ---
		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(0.0F, -1.0F, -9.0F); // 尾巴根部
		main.addChild(cube_r6);
		cube_r6.cubeList.add(new ModelBox(cube_r6, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(0.0F, 0.0F, -1.75F);
		cube_r6.addChild(cube_r5); // 挂载在上一节
		cube_r5.cubeList.add(new ModelBox(cube_r5, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(0.0F, 0.0F, -1.75F);
		cube_r5.addChild(cube_r4);
		cube_r4.cubeList.add(new ModelBox(cube_r4, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(0.0F, 0.0F, -1.75F);
		cube_r4.addChild(cube_r3);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, 0.0F, -1.75F);
		cube_r3.addChild(cube_r2);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, 0.0F, -1.75F);
		cube_r2.addChild(cube_r1);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F, false));

		// 身体外壳/其余部件
		main4 = new ModelRenderer(this);
		main4.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(main4);
		main4.cubeList.add(new ModelBox(main4, 96, 0, -1.0F, -2.0F, -8.0F, 2, 2, 22, 0.0F, false));
		main4.cubeList.add(new ModelBox(main4, 176, 48, 1.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main4.cubeList.add(new ModelBox(main4, 176, 86, -3.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main4.cubeList.add(new ModelBox(main4, 192, 20, 5.0F, 0.0F, -2.0F, 2, 0, 10, 0.0F, false));

		main3 = new ModelRenderer(this);
		main3.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(main3);
		main3.cubeList.add(new ModelBox(main3, 0, 96, -1.0F, -2.0F, -8.0F, 2, 2, 22, 0.0F, false));
		main3.cubeList.add(new ModelBox(main3, 80, 162, 1.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main3.cubeList.add(new ModelBox(main3, 160, 162, -3.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main3.cubeList.add(new ModelBox(main3, 0, 182, 3.0F, -1.0F, -4.0F, 2, 1, 13, 0.0F, false));

		main2 = new ModelRenderer(this);
		main2.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(main2);
		main2.cubeList.add(new ModelBox(main2, 0, 48, -1.0F, -2.0F, -8.0F, 2, 2, 22, 0.0F, false));
		main2.cubeList.add(new ModelBox(main2, 96, 124, 1.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main2.cubeList.add(new ModelBox(main2, 0, 144, -3.0F, -1.0F, -6.0F, 2, 1, 18, 0.0F, false));
		main2.cubeList.add(new ModelBox(main2, 176, 124, -5.0F, -1.0F, -4.0F, 2, 1, 13, 0.0F, false));
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		// 1. 触角动画：随自然摆动，幅度小而慢 (类似蜗牛)
		float antennaWave = MathHelper.sin(ageInTicks * 0.05F) * 0.15F;
		float antennaWalk = MathHelper.sin(limbSwing * 0.5F) * 0.2F * limbSwingAmount;
		
		this.antenna_left.rotateAngleX = 0.2F + antennaWave + antennaWalk;
		this.antenna_left.rotateAngleZ = -0.1F + (antennaWave * 0.5F);
		
		this.antenna_right.rotateAngleX = 0.2F - antennaWave + antennaWalk;
		this.antenna_right.rotateAngleZ = 0.1F + (antennaWave * 0.5F);
		
		// 2. 尾巴蝎子倒钩动画
		// 基础弯曲角度 -0.4F (向上翘起约23度)，6节加起来约-138度，正好是个高高翘起的蝎子倒钩
		// 加上呼吸和行走的轻微摆动
		float tailBaseCurl = -0.4F;
		float tailWave = MathHelper.sin(ageInTicks * 0.1F) * 0.05F;
		float tailWalk = MathHelper.cos(limbSwing * 0.6F) * 0.1F * limbSwingAmount;
		
		float finalTailRot = tailBaseCurl + tailWave + tailWalk;
		
		// 因为是链式结构，每一节都会继承上一节的旋转，所以只需要给每一节相同的角度即可形成完美的圆弧
		this.cube_r6.rotateAngleX = finalTailRot;
		this.cube_r5.rotateAngleX = finalTailRot;
		this.cube_r4.rotateAngleX = finalTailRot;
		this.cube_r3.rotateAngleX = finalTailRot;
		this.cube_r2.rotateAngleX = finalTailRot;
		this.cube_r1.rotateAngleX = finalTailRot;
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		
		// 3. 全身蠕动动画：像软体动物（蜗牛/蛞蝓）一样的小幅度拉伸和收缩
		GlStateManager.pushMatrix();
		
		// f = limbSwing (距离), f1 = limbSwingAmount (速度)
		// 当实体移动时，身体产生有节奏的拉伸(stretch)和收缩(squish)
		float stretch = 1.0F + MathHelper.sin(f * 0.8F) * 0.15F * f1;
		float squish = 1.0F - MathHelper.sin(f * 0.8F) * 0.1F * f1;
		
		// 稍微往上偏移一点，从底部中心进行缩放，避免陷入地下
		GlStateManager.translate(0.0F, 1.5F, 0.0F);
		GlStateManager.scale(squish, squish, stretch); // 变长时变细，变短时变胖
		GlStateManager.translate(0.0F, -1.5F, 0.0F);
		
		main.render(f5);
		
		GlStateManager.popMatrix();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
