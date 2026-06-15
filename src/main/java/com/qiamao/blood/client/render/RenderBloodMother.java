package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.client.model.ModelBloodMother;
import com.qiamao.blood.entity.EntityBloodMother;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBloodMother extends RenderLiving<EntityBloodMother> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/blood_mother.png");

    public RenderBloodMother(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBloodMother(), 2.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodMother entity) {
        return TEXTURE;
    }
    
    @Override
    protected float getDeathMaxRotation(EntityBloodMother entity) {
        return 180.0F;
    }

    @Override
    protected void preRenderCallback(EntityBloodMother entitylivingbaseIn, float partialTickTime) {
        GlStateManager.disableCull(); // 禁用背面剔除，防止缩放后可以看到内部
        GlStateManager.scale(8.0F, 8.0F, 8.0F); // 缩放倍数
        // 调整模型位置，使其中心与碰撞箱中心对齐
        // 经过计算，模型主干和头尾的整体中心相对于旋转点略微偏后
        // 这里的 -0.05F 在 8倍缩放后约为 0.4 个方块单位的后移，能使长条形身体均匀分布在中心
        GlStateManager.translate(0.0F, 0.0F, -0.05F);
    }
}
