package com.qiamao.blood.client.model;

import com.qiamao.blood.entity.EntityBloodHound;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelBloodHound extends ModelWolf {

    public ModelBloodHound() {
        super();
    }

    /**
     * 重写设置旋转角度的方法，修复原版写死的尾巴计算公式导致高血量下尾巴360度旋转的Bug
     */
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 先调用原版模型计算四肢和头部的基础动作
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        // 强行覆盖尾巴的旋转角度
        if (entityIn instanceof EntityBloodHound) {
            EntityBloodHound hound = (EntityBloodHound) entityIn;
            
            net.minecraft.client.model.ModelRenderer tail = getWolfTail();
            if (tail != null) {
                if (hound.isAngry()) {
                    // 激怒状态：尾巴笔直竖起
                    tail.rotateAngleX = 1.5393804F;
                } else {
                    // 非激怒状态：根据当前血量占最大血量的比例计算下垂角度，绝对安全不会溢出
                    float healthPercent = hound.getHealth() / hound.getMaxHealth();
                    // healthPercent 范围 0.0 ~ 1.0
                    // 满血(1.0)时尾巴角度大约是 0.55F
                    // 残血(0.0)时尾巴角度大约是 0.55F - 0.4F = 0.15F (夹着尾巴)
                    tail.rotateAngleX = 0.55F - (1.0F - healthPercent) * 0.4F * (float)Math.PI;
                }
            }
        }
    }

    private net.minecraft.client.model.ModelRenderer cachedWolfTail;

    private net.minecraft.client.model.ModelRenderer getWolfTail() {
        if (cachedWolfTail != null) return cachedWolfTail;
        try {
            int count = 0;
            for (java.lang.reflect.Field f : ModelWolf.class.getDeclaredFields()) {
                if (f.getType() == net.minecraft.client.model.ModelRenderer.class) {
                    if (count == 6) {
                        f.setAccessible(true);
                        cachedWolfTail = (net.minecraft.client.model.ModelRenderer) f.get(this);
                        return cachedWolfTail;
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}