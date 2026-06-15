package com.qiamao.blood.client.render;

import com.qiamao.blood.entity.EntityThrownBloodMite;
import com.qiamao.blood.init.ModItems;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public class RenderThrownBloodMite extends RenderSnowball<EntityThrownBloodMite> {

    public RenderThrownBloodMite(RenderManager renderManagerIn) {
        // 使用原版的 RenderSnowball，并传入我们刚刚注册好的 THROWN_BLOOD_MITE_ITEM
        // 这将保证它在空中像雪球或烈焰弹一样，以2D物品的形式面向玩家渲染
        super(renderManagerIn, ModItems.THROWN_BLOOD_MITE_ITEM, Minecraft.getMinecraft().getRenderItem());
    }
}