package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelParasiticSteve extends ModelPlayer {
    
    public ModelParasiticSteve(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 先调用原版的 setRotationAngles 进行基础运算
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        
        // 核心修改：在慢速移动时，由于 limbSwingAmount (速度因子) 很小，原版计算出的腿和手摆动幅度会很小（像平移）。
        // 我们强行放大这个幅度。
        
        // 我们将原版基于 MathHelper.cos(limbSwing) 计算出来的基础角度，乘以一个放大系数
        // 注意：原版代码中：
        // rightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        // 我们可以直接覆写它们。

        // 我们取一个固定的摆动幅度，无论走得快还是慢，手脚前后摆动的最大角度都是固定的。
        // 原版中 limbSwingAmount 随速度从 0 变化到 1。
        // 为了实现“慢走时幅度减半（相对之前0.8F来说），快跑时保持大幅度”
        // 我们根据当前的速度因子来设置上限。
        float constantSwingAmount = 0.0F;
        if (limbSwingAmount > 0.01F) {
            if (limbSwingAmount < 0.6F) {
                // 慢走状态：幅度改为之前(0.8F)的一半，即0.4F
                constantSwingAmount = 0.4F;
            } else {
                // 快跑状态：保持大幅度
                constantSwingAmount = 0.8F;
            }
        }

        // 重新计算手脚的摆动 (使用固定的幅度 constantSwingAmount)
        this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * constantSwingAmount * 0.5F;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * constantSwingAmount * 0.5F;
        
        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * constantSwingAmount;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * constantSwingAmount;

        // 消除手臂原版可能附带的微小Z轴旋转，让摆动更纯粹有力
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        
        // 额外的僵尸式“抬手”效果 (如果你觉得需要的话，这里保持手臂自然下垂即可，只靠前后摆动)
    }
}
