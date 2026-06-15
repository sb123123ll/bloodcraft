package com.qiamao.blood.client.event;

import com.qiamao.blood.event.DesireEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.World;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 欲望事件天空渲染处理器
 * 在欲望事件期间让夜晚天空变成纯黑色（没有星星，只有月亮）
 */
@SideOnly(Side.CLIENT)
public class DesireSkyRenderHandler {

    private static final Minecraft MC = Minecraft.getMinecraft();
    
    // 亮度过渡相关变量
    private static float brightnessFactor = 1.0F;
    private static final float TRANSITION_SPEED = 0.5F / 40.0F; // 2秒完成 (40 ticks)，每次变动 0.5/40
    private static final IRenderHandler MOON_HIDER = new MoonHider();
    private IRenderHandler originalSkyRenderer = null;

    /**
     * 欲望事件期间将雾气设为深红色，随亮度因子平滑过渡
     */
    @SubscribeEvent
    public void onFogColors(FogColors event) {
        if (MC.world != null) {
            float currentFactor = getBrightnessFactor();
            // 如果亮度不是 1.0，说明正在事件中或正在过渡
            if (currentFactor < 1.0F) {
                // 计算红色比例：亮度 1.0 -> 红色 0%, 亮度 0.4 -> 红色 100%
                float transitionProgress = (1.0F - currentFactor) / (1.0F - 0.4F);
                
                // 线性插值计算雾气颜色
                // 假设夜晚原版雾气接近黑色 (0.0)，目标是深红 (0.4)
                float r = 0.4F * transitionProgress;
                
                event.setRed(r);
                event.setGreen(0.0F);
                event.setBlue(0.0F);
            }
        }
    }

    /**
     * 每一帧更新亮度因子和天空渲染器
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || MC.world == null) return;

        World world = MC.world;
        boolean targetActive = isDesireActive(world);
        float targetFactor = targetActive ? 0.4F : 1.0F; // 降低到40%亮度

        if (brightnessFactor != targetFactor) {
            if (brightnessFactor > targetFactor) {
                brightnessFactor = Math.max(targetFactor, brightnessFactor - TRANSITION_SPEED);
            } else {
                brightnessFactor = Math.min(targetFactor, brightnessFactor + TRANSITION_SPEED);
            }
        }

        // 隐藏月亮的逻辑：在事件期间接管天空渲染
        if (world.provider.getDimension() == 0) {
            IRenderHandler currentRenderer = world.provider.getSkyRenderer();
            if (targetActive) {
                if (currentRenderer != MOON_HIDER) {
                    originalSkyRenderer = currentRenderer;
                    world.provider.setSkyRenderer(MOON_HIDER);
                }
            } else if (currentRenderer == MOON_HIDER) {
                world.provider.setSkyRenderer(originalSkyRenderer);
            }
        }
    }

    /**
     * 自定义天空渲染器，用于隐藏月亮
     * 1.12.2 中如果不画任何东西，默认会保留星星（由渲染管线处理），但不会画太阳/月亮
     */
    private static class MoonHider extends IRenderHandler {
        @Override
        public void render(float partialTicks, WorldClient world, Minecraft mc) {
            // 这里留空或只处理必要的 GL 状态，不绘制月亮贴图
            // 默认的天空背景和星星是在渲染世界之前由 RenderGlobal 处理的
            // 当自定义 SkyRenderer 存在且不执行任何绘制时，通常会跳过太阳/月亮的绘制
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        World world = MC.world;
        float currentFactor = getBrightnessFactor();
        
        if (world != null && (isDesireActive(world) || currentFactor < 1.0F)) {
            int width = event.getResolution().getScaledWidth();
            int height = event.getResolution().getScaledHeight();
            
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableAlpha();
            
            float alphaFactor = (1.0F - currentFactor) * 1.25F;
            
            // 绘制血红色滤镜
            int redAlpha = (int)(alphaFactor * 100);
            net.minecraft.client.gui.Gui.drawRect(0, 0, width, height, (redAlpha << 24) | 0x440000);
            
            // 绘制黑色遮罩增强黑暗感
            int blackAlpha = (int)(alphaFactor * 187);
            net.minecraft.client.gui.Gui.drawRect(0, 0, width, height, (blackAlpha << 24) | 0x000000);

            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private float getBrightnessFactor() {
        return brightnessFactor;
    }

    private boolean isDesireActive(World world) {
        // 第一层保险：维度检查
        if (world.provider.getDimension() != 0) return false;
        
        // 第二层保险：客户端常识判断。如果天亮了，无论同步状态如何，都强制关闭渲染
        long time = world.getWorldTime() % 24000;
        boolean isNight = time >= 13000 && time <= 23500; // 稍微放宽一点点到日出前
        if (!isNight) return false;
        
        // 第三层保险：完全信任同步过来的事件状态
        return DesireEventHandler.isDesireEventActive(world);
    }
}
