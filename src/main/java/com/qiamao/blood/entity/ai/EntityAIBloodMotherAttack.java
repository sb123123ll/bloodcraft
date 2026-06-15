package com.qiamao.blood.entity.ai;

import com.qiamao.blood.entity.EntityBloodMother;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;

/**
 * 血液母体近战攻击AI
 * 攻击距离减少3格（血液母体体积大，基础距离远）
 */
public class EntityAIBloodMotherAttack extends EntityAIAttackMelee {
    
    private final EntityBloodMother mother;
    private final double attackReachPenalty = -3.0D; // 攻击距离减少3格（血液母体体积大，基础距离远）
    
    public EntityAIBloodMotherAttack(EntityBloodMother mother, double speedIn, boolean longMemoryIn) {
        super(mother, speedIn, longMemoryIn);
        this.mother = mother;
    }
    
    @Override
    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        // 原版攻击距离平方 - 减少距离
        double baseReach = super.getAttackReachSqr(attackTarget);
        // 计算实际攻击距离（原版基础上减少0.5格）
        double actualReach = Math.max(0, Math.sqrt(baseReach) + attackReachPenalty);
        return actualReach * actualReach;
    }
    
    @Override
    public boolean shouldExecute() {
        // 只在准备冲撞时不执行普通近战
        // 冲撞中也可以近战（冷却期间可以用近战）
        if (mother.getChargePrepareTime() > 0) {
            return false;
        }
        return super.shouldExecute();
    }
}
