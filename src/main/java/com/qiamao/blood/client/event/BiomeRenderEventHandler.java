package com.qiamao.blood.client.event;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.init.ModBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 血液翻腾之地客户端渲染事件处理器
 * 处理天空颜色、雾效、雨效果等视觉表现
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class BiomeRenderEventHandler {

    private static final Minecraft MC = Minecraft.getMinecraft();

    // 淡红色天空/雾 - RGB(255, 150, 150) 淡红色
    private static final float SKY_RED = 1.0F;
    private static final float SKY_GREEN = 0.588F; // 150/255
    private static final float SKY_BLUE = 0.588F;

    // 血红色雾气 - RGB(139, 0, 0) 深血红色
    private static final float FOG_RED = 0.545F;
    private static final float FOG_GREEN = 0.0F;
    private static final float FOG_BLUE = 0.0F;

    // 雨的淡红色 - RGB(200, 80, 80)
    private static final float RAIN_RED = 0.78F;
    private static final float RAIN_GREEN = 0.31F;
    private static final float RAIN_BLUE = 0.31F;

    /**
     * 修改雾颜色 - 根据时间平滑过渡
     * 白天保持淡红色，夜晚随太阳亮度自然变暗，避免夜视效果
     */
    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!isInBloodSurgingLand()) return;

        // 获取当前太阳亮度：0.0=午夜, 1.0=正午，用于平滑过渡
        float sunBrightness = MC.world.getSunBrightness(1.0F);
        // 最低保留15%亮度，夜晚仍有微弱红光，不至于完全漆黑
        float brightness = Math.max(0.15F, sunBrightness);

        // 白天保持原有淡红色，夜晚随亮度下降自然变暗
        event.setRed(SKY_RED * brightness);
        event.setGreen(SKY_GREEN * brightness);
        event.setBlue(SKY_BLUE * brightness);
    }

    /**
     * 修改雾密度 - 根据时间变化营造不同氛围
     * 白天薄雾，夜晚浓雾更显阴森
     */
    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!isInBloodSurgingLand()) return;

        float sunBrightness = MC.world.getSunBrightness(1.0F);
        // 夜晚雾更浓（0.02→0.08），平滑过渡
        float nightFactor = 1.0F - sunBrightness;
        float density = 0.02F + nightFactor * 0.06F;

        event.setDensity(density);
        event.setCanceled(true);
    }

    /**
     * 客户端tick事件 - 处理雨粒子颜色修改
     * 通过修改OpenGL颜色状态实现雨的颜色变化
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (MC.world == null || MC.player == null) return;

        // 检查是否在血液翻腾之地且正在下雨
        if (!isInBloodSurgingLand()) return;

        // 获取当前生物群系的降雨强度
        float rainStrength = MC.world.getRainStrength(1.0F);
        if (rainStrength <= 0.0F) return;

        // 修改当前渲染颜色为淡红色
        // 这会影响雨和雪的渲染颜色
        GlStateManager.color(RAIN_RED, RAIN_GREEN, RAIN_BLUE, 1.0F);
    }

    /**
     * 检查玩家是否在血液翻腾之地群系
     */
    private static boolean isInBloodSurgingLand() {
        if (MC.world == null || MC.player == null) return false;

        BlockPos playerPos = MC.player.getPosition();
        Biome biome = MC.world.getBiome(playerPos);

        return biome == ModBiomes.BLOOD_SURGING_LAND;
    }

    /**
     * 获取是否是血液翻腾之地群系（供其他渲染类使用）
     */
    public static boolean isInBloodSurgingLandBiome() {
        return isInBloodSurgingLand();
    }

    /**
     * 获取天空颜色（用于天空渲染器）
     */
    public static float[] getSkyColor() {
        return new float[]{SKY_RED, SKY_GREEN, SKY_BLUE};
    }

    /**
     * 获取雾颜色
     */
    public static float[] getFogColor() {
        return new float[]{FOG_RED, FOG_GREEN, FOG_BLUE};
    }

    /**
     * 获取雨颜色
     */
    public static float[] getRainColor() {
        return new float[]{RAIN_RED, RAIN_GREEN, RAIN_BLUE};
    }
}
