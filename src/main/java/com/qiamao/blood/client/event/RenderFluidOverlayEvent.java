package com.qiamao.blood.client.event;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = BloodMod.MODID, value = Side.CLIENT)
public class RenderFluidOverlayEvent {

    private static final ResourceLocation WATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

    @SubscribeEvent
    public static void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player == null || mc.world == null) return;

            // 获取玩家眼睛位置的方块状态
            BlockPos eyePos = new BlockPos(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
            IBlockState state = mc.world.getBlockState(eyePos);

            // 检查玩家眼睛所在方块
            if (state.getBlock() == ModBlocks.BLOCK_BLOOD) {
                // 取消原版的覆盖渲染
                event.setCanceled(true);

                // 计算玩家沉入血液的深度，以此控制纹理的透明度
                double depth = getFluidDepth(mc.world, eyePos, mc.player.posY + mc.player.getEyeHeight());
                
                // 开始我们自定义的血液覆盖渲染
                renderBloodOverlay(mc, depth);
            }
        }
    }

    /**
     * 计算玩家头部在液体中的大致深度
     * 返回值越大说明越深，最大深度可以限制在一个范围内
     */
    private static double getFluidDepth(net.minecraft.world.World world, BlockPos eyePos, double eyeY) {
        int maxSearch = 5; // 往上搜索的最大方块数
        double depth = 0;
        
        for (int i = 0; i < maxSearch; i++) {
            BlockPos checkPos = eyePos.up(i);
            IBlockState state = world.getBlockState(checkPos);
            if (state.getBlock() == ModBlocks.BLOCK_BLOOD) {
                // 每往上一格全是血，深度就增加
                depth += 1.0;
            } else {
                // 找到了液面
                // 加上液面方块内的具体深度（液面高度通常不是满的一格）
                IBlockState surfaceState = world.getBlockState(checkPos.down());
                float fluidHeight = 1.0f;
                if (surfaceState.getBlock() instanceof net.minecraft.block.BlockLiquid) {
                    fluidHeight = net.minecraft.block.BlockLiquid.getLiquidHeightPercent(surfaceState.getBlock().getMetaFromState(surfaceState));
                } else if (surfaceState.getBlock() instanceof net.minecraftforge.fluids.BlockFluidBase) {
                    fluidHeight = ((net.minecraftforge.fluids.BlockFluidBase) surfaceState.getBlock()).getFilledPercentage(world, checkPos.down());
                }
                // 计算眼睛到液面的精确距离
                depth = (checkPos.getY() - eyeY) - (1.0 - fluidHeight);
                break;
            }
        }
        return Math.max(0.0, depth);
    }

    private static void renderBloodOverlay(Minecraft mc, double depth) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        
        // ==========================================
        // 1. 渲染带深度的原版水下纹理（红色染色）
        // ==========================================
        
        // 随着深度增加，纹理的透明度逐渐降低，最深处几乎看不见纹理
        // depth=0 时透明度最高(0.8)，depth=3 时透明度降到极低(0.1)
        float textureAlpha = (float) Math.max(0.1, 0.8 - (depth * 0.25));
        
        mc.getTextureManager().bindTexture(WATER_OVERLAY);
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        // 设置颜色为深红色，并应用深度计算出的透明度
        GlStateManager.color(0.8F, 0.1F, 0.1F, textureAlpha);
        
        float f6 = -mc.player.rotationYaw / 64.0F;
        float f7 = mc.player.rotationPitch / 64.0F;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f6), (double)(4.0F + f7)).endVertex();
        bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f6), (double)(4.0F + f7)).endVertex();
        bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f6), (double)(0.0F + f7)).endVertex();
        bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f6), (double)(0.0F + f7)).endVertex();
        tessellator.draw();

        // ==========================================
        // 2. 渲染全局高透明纯红滤镜（不受深度影响，或随深度加深）
        // ==========================================
        
        // 禁用纹理，直接画纯色矩形
        GlStateManager.disableTexture2D();
        
        // 基础滤镜透明度，越深可以越红
        float filterAlpha = (float) Math.min(0.9, 0.4 + (depth * 0.1));
        GlStateManager.color(0.6F, 0.0F, 0.0F, filterAlpha);
        
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(-1.0D, -1.0D, -0.5D).endVertex();
        bufferbuilder.pos(1.0D, -1.0D, -0.5D).endVertex();
        bufferbuilder.pos(1.0D, 1.0D, -0.5D).endVertex();
        bufferbuilder.pos(-1.0D, 1.0D, -0.5D).endVertex();
        tessellator.draw();
        
        // 恢复状态
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }
}
