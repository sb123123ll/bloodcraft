package com.qiamao.blood.client.render;

import com.qiamao.blood.client.model.ModelBloodHound;
import com.qiamao.blood.entity.EntityBloodHound;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBloodHound extends RenderLiving<EntityBloodHound> {
    
    // 使用自定义的血液猎犬纹理
    private static final ResourceLocation BLOOD_HOUND_TEXTURES = new ResourceLocation("blood", "textures/entity/blood_hound.png");

    public RenderBloodHound(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBloodHound(), 0.5F);
        this.addLayer(new LayerBloodHoundCollar(this));
        this.addLayer(new LayerBloodHoundMites(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBloodHound entity) {
        return BLOOD_HOUND_TEXTURES;
    }
}
