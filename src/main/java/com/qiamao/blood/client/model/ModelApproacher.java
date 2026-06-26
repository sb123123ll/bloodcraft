package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelApproacher extends ModelBase {
    private final ModelRenderer main;
    private final ModelRenderer fan_base; // 头顶旋转部分
    private final ModelRenderer leg1; // 左前腿
    private final ModelRenderer leg2; // 右前腿
    private final ModelRenderer leg3; // 左后腿
    private final ModelRenderer leg4; // 右后腿

    public ModelApproacher() {
        textureWidth = 64;
        textureHeight = 64;

        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 22.0F, 0.0F);
        
        // 身体和固定部分 (去除了腿和旋转的风扇部分，单独提取)
        // 侧面的装饰、身体主块、眼睛等
        main.cubeList.add(new ModelBox(main, 0, 0, -6.0F, -10.0F, -6.0F, 12, 7, 12, 0.0F, false));
        main.cubeList.add(new ModelBox(main, 16, 33, -1.0F, -12.0F, -1.0F, 2, 2, 2, 0.0F, false)); // 风扇根部（不转）
        main.cubeList.add(new ModelBox(main, 36, 23, -4.0F, -8.0F, -7.0F, 2, 2, 2, 0.0F, false));
        main.cubeList.add(new ModelBox(main, 36, 27, 2.0F, -8.0F, -7.0F, 2, 2, 2, 0.0F, false));
        main.cubeList.add(new ModelBox(main, 36, 31, -4.0F, -9.0F, -7.0F, 1, 1, 1, 0.0F, false));
        main.cubeList.add(new ModelBox(main, 16, 37, 3.0F, -9.0F, -7.0F, 1, 1, 1, 0.0F, false));

        // 提取腿部
        leg1 = new ModelRenderer(this);
        leg1.setRotationPoint(-5.0F, -3.0F, -5.0F); // 设置旋转中心点
        leg1.cubeList.add(new ModelBox(leg1, 28, 19, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F, false));
        main.addChild(leg1);

        leg2 = new ModelRenderer(this);
        leg2.setRotationPoint(5.0F, -3.0F, -5.0F); // 设置旋转中心点
        leg2.cubeList.add(new ModelBox(leg2, 28, 26, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F, false));
        main.addChild(leg2);

        leg3 = new ModelRenderer(this);
        leg3.setRotationPoint(-5.0F, -3.0F, 5.0F); // 设置旋转中心点
        leg3.cubeList.add(new ModelBox(leg3, 0, 33, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F, false));
        main.addChild(leg3);

        leg4 = new ModelRenderer(this);
        leg4.setRotationPoint(5.0F, -3.0F, 5.0F); // 设置旋转中心点
        leg4.cubeList.add(new ModelBox(leg4, 8, 33, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F, false));
        main.addChild(leg4);

        // 提取风扇旋转部分 (原cube_r1 和 最高的三格方块)
        fan_base = new ModelRenderer(this);
        fan_base.setRotationPoint(0.0F, -12.0F, 0.0F);
        
        // 最高的四个 2x2 方块 (原本在 main 里，Y轴坐标是 -16.0F)
        // 转移到 fan_base 后，因为 fan_base 的旋转中心在 -12.0F，所以相对 Y 轴要减去 12.0F => -16.0F - (-12.0F) = -4.0F
        fan_base.cubeList.add(new ModelBox(fan_base, 36, 19, -6.0F, -4.0F, -3.0F, 2, 2, 2, 0.0F, false));
        fan_base.cubeList.add(new ModelBox(fan_base, 32, 33, -6.0F, -4.0F, 1.0F, 2, 2, 2, 0.0F, false));
        fan_base.cubeList.add(new ModelBox(fan_base, 24, 33, 4.0F, -4.0F, 1.0F, 2, 2, 2, 0.0F, false));
        fan_base.cubeList.add(new ModelBox(fan_base, 16, 33, 4.0F, -4.0F, -3.0F, 2, 2, 2, 0.0F, false));

        // 原本的风扇扇叶条状物
        ModelRenderer fan_blades = new ModelRenderer(this);
        fan_blades.setRotationPoint(0.0F, 0.0F, 0.0F);
        setRotationAngle(fan_blades, 0.0F, -1.5708F, 0.0F);
        fan_blades.cubeList.add(new ModelBox(fan_blades, 0, 19, -1.0F, -2.0F, -6.0F, 2, 2, 12, 0.0F, false));
        fan_base.addChild(fan_blades);
        
        main.addChild(fan_base);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        main.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 脑袋固定，不看向玩家 (main 整体不进行额外的 Y/X 轴旋转)
        
        // 风扇不停转动
        float fanSpeed = 0.6F; // 旋转速度 (原本 0.8 的 0.75 倍 = 0.6)
        this.fan_base.rotateAngleY = ageInTicks * fanSpeed;

        // 四条腿行走动画 (对角线迈步，不顺拐，幅度适中)
        // 降低 0.75 倍幅度
        this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.05F * limbSwingAmount;
        this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.05F * limbSwingAmount;
        this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.05F * limbSwingAmount;
        this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.05F * limbSwingAmount;
    }
}
