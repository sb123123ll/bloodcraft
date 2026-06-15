package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityParasiticSteve;
import com.qiamao.blood.client.model.ModelParasiticSteve;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
// import com.qiamao.blood.client.render.LayerBloodMites;

public class RenderParasiticSteve extends RenderLiving<EntityParasiticSteve> {

    // 使用刚才画好的带有破损衣服、黑眼血泪、血肉模糊双腿的贴图
    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/entity/steve_gore.png");

    public RenderParasiticSteve(RenderManager renderManagerIn) {
        // 使用我们重写了动作幅度的新模型
        super(renderManagerIn, new ModelParasiticSteve(0.0F, false), 0.5F);
        
        // 关键：我们添加一个额外的“图层(Layer)”，在这个图层上渲染 6 只不断蠕动的血螨！
        // this.addLayer(new LayerBloodMites(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityParasiticSteve entity) {
        return TEXTURE;
    }
}