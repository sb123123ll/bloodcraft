package com.qiamao.blood.client.render;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.item.ItemEntityFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 实体脸部图标物品渲染器
 * 从实体纹理中硬编码截取脸部部分显示
 */
@SideOnly(Side.CLIENT)
public class RenderItemEntityFace {
    
    // 纹理映射：实体名称 -> 纹理路径
    private static final ResourceLocation CULTIST_PREACHER_TEXTURE = 
            new ResourceLocation(BloodMod.MODID, "textures/entity/cultist_preacher.png");
    
    /**
     * 渲染物品
     * @param stack 物品堆
     * @param transformType 变换类型
     */
    public static void render(ItemStack stack, ItemCameraTransforms.TransformType transformType) {
        if (!(stack.getItem() instanceof ItemEntityFace)) {
            return;
        }
        
        ItemEntityFace item = (ItemEntityFace) stack.getItem();
        String entityName = item.getEntityName();
        
        // 绑定对应的实体纹理
        ResourceLocation texture = getTextureForEntity(entityName);
        if (texture == null) {
            // 如果没有找到纹理，使用默认的缺失纹理
            texture = TextureMap.LOCATION_MISSING_TEXTURE;
        }
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        
        GlStateManager.pushMatrix();
        
        // 根据变换类型调整缩放和位置
        float scale = getScaleForTransform(transformType);
        GlStateManager.scale(scale, scale, scale);
        
        // 硬编码渲染脸部区域
        // 村民模型头部在纹理中的大致位置（需要根据实际纹理调整）
        // 通常村民头部占用纹理的左上角区域
        renderFaceFromTexture(transformType, entityName);
        
        GlStateManager.popMatrix();
    }
    
    /**
     * 获取实体对应的纹理
     */
    private static ResourceLocation getTextureForEntity(String entityName) {
        if ("cultist_preacher".equals(entityName)) {
            return CULTIST_PREACHER_TEXTURE;
        }
        return null;
    }
    
    /**
     * 根据变换类型获取缩放比例
     */
    private static float getScaleForTransform(ItemCameraTransforms.TransformType transformType) {
        switch (transformType) {
            case GUI:
                return 0.5F; // GUI 中显示较小
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                return 0.25F;
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                return 0.25F;
            case HEAD:
                return 0.25F;
            case GROUND:
                return 0.25F;
            case FIXED:
                return 0.25F;
            default:
                return 0.5F;
        }
    }
    
    /**
     * 从纹理中渲染脸部
     * 硬编码UV坐标截取脸部区域
     */
    private static void renderFaceFromTexture(ItemCameraTransforms.TransformType transformType, String entityName) {
        // 村民纹理中头部的大致位置（64x64纹理）
        // 头部通常在左上角，大概占据 8x8 或 16x16 的区域
        // 需要根据实际纹理调整这些值
        
        // 传教士使用村民模型，纹理布局如下：
        // 头部正面：通常位于 (8, 8) 开始，8x8 像素
        // 但为了图标显示更大更清晰，我们截取并放大
        
        float minU = 8.0F / 64.0F;   // 头部左侧 U 坐标
        float maxU = 24.0F / 64.0F;  // 头部右侧 U 坐标 (16像素宽)
        float minV = 8.0F / 64.0F;   // 头部顶部 V 坐标
        float maxV = 24.0F / 64.0F;  // 头部底部 V 坐标 (16像素高)
        
        // 根据实体类型调整UV坐标
        if ("cultist_preacher".equals(entityName)) {
            // 传教士头部在纹理中的位置（需要根据实际纹理微调）
            minU = 8.0F / 64.0F;
            maxU = 24.0F / 64.0F;
            minV = 8.0F / 64.0F;
            maxV = 24.0F / 64.0F;
        }
        
        // 渲染一个平面，显示脸部纹理
        drawTexturedRect(minU, minV, maxU, maxV);
    }
    
    /**
     * 绘制带纹理的矩形
     */
    private static void drawTexturedRect(float minU, float minV, float maxU, float maxV) {
        GlStateManager.enableTexture2D();
        
        // 在 GUI 或其他视图中渲染一个 16x16 的正方形
        float x1 = -0.5F;
        float x2 = 0.5F;
        float y1 = -0.5F;
        float y2 = 0.5F;
        
        net.minecraft.client.renderer.Tessellator tessellator = 
                net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        
        // 绘制矩形（两个三角形）
        // 左上
        buffer.pos(x1, y2, 0.0D).tex(minU, maxV).endVertex();
        // 右上
        buffer.pos(x2, y2, 0.0D).tex(maxU, maxV).endVertex();
        // 右下
        buffer.pos(x2, y1, 0.0D).tex(maxU, minV).endVertex();
        // 左下
        buffer.pos(x1, y1, 0.0D).tex(minU, minV).endVertex();
        
        tessellator.draw();
    }
}
