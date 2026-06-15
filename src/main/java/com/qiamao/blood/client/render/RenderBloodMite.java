package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityBloodEndermite;
import net.minecraft.client.model.ModelEnderMite;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBloodMite extends RenderLiving<EntityBloodEndermite> {

    // 绑定我们刚才生成的血腥末影螨贴图
    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/blood_endermite.png");

    public RenderBloodMite(RenderManager renderManagerIn) {
        // 直接使用原版的末影螨模型 (ModelEnderMite)
        super(renderManagerIn, new ModelEnderMite(), 0.3F); // 0.3F 是底部阴影的大小
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodEndermite entity) {
        return TEXTURE;
    }
    
    @Override
    protected float getDeathMaxRotation(EntityBloodEndermite entityBug) {
        return 180.0F; // 死亡时翻肚皮的角度
    }
}