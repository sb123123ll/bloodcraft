package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelAnimatedMite extends ModelBase {
    public ModelRenderer[] segments = new ModelRenderer[8];
    private final int SEGMENT_COUNT = 8;
    private final float SEGMENT_LENGTH = 3.0F; // 每节稍微短一点，保证整体长度适中，且关节更多更柔软

    public ModelAnimatedMite() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            this.segments[i] = new ModelRenderer(this, 0, 0);
            // 长宽高：2x2x3
            this.segments[i].addBox(-1.0F, -1.0F, 0.0F, 2, 2, (int)SEGMENT_LENGTH);
            
            if (i == 0) {
                // 第一节是根部
                this.segments[i].setRotationPoint(0.0F, 0.0F, 0.0F);
            } else {
                // 后续的节都连接在上一节的末端
                this.segments[i].setRotationPoint(0.0F, 0.0F, SEGMENT_LENGTH);
                this.segments[i - 1].addChild(this.segments[i]);
            }
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        // 去除这里的 setRotationAngles，交由外部控制以支持不同实例渲染不同的动画参数
        // this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        
        // 只渲染根节点，所有的子节点会跟随渲染
        this.segments[0].render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float speed = 0.4F; // 稍微放慢一点，显得更自然
        float degree = 0.15F; // 幅度稍微减小，避免折角过大，整体更柔顺
        
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            // 相位差减小，让波浪更长更平滑（更柔顺）
            float phaseOffset = i * 0.5F; 
            
            // Y轴左右摇摆
            this.segments[i].rotateAngleY = MathHelper.sin(ageInTicks * speed - phaseOffset) * degree;
            
            // X轴上下摇摆 + 重力下垂
            // 使用更平滑的下垂曲线
            float gravityDrop = -0.1F - (i * 0.04F);
            this.segments[i].rotateAngleX = gravityDrop + MathHelper.cos(ageInTicks * speed - phaseOffset) * (degree * 0.8F);
            
            // Z轴微小的自转抽搐
            this.segments[i].rotateAngleZ = MathHelper.sin(ageInTicks * speed * 1.5F - phaseOffset) * (degree * 0.3F);
        }
    }
}