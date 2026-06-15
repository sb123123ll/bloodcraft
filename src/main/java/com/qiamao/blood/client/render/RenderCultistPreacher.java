package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityCultistPreacher;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * 邪教传教士渲染器
 * 使用原版村民模型
 */
public class RenderCultistPreacher extends RenderLiving<EntityCultistPreacher> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/cultist_preacher.png");

    public RenderCultistPreacher(RenderManager renderManagerIn) {
        // 使用原版村民模型，类型0表示普通村民
        super(renderManagerIn, new ModelVillager(0.0F), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCultistPreacher entity) {
        return TEXTURE;
    }
}
