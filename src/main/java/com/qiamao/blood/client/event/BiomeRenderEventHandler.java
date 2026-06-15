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
     * 修改雾颜色 - 统一使用淡红色
     */
    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!isInBloodSurgingLand()) return;

        // 设置雾颜色为淡红色（与天空一致）
        event.setRed(SKY_RED);
        event.setGreen(SKY_GREEN);
        event.setBlue(SKY_BLUE);
    }

    /**
     * 修改雾密度 - 营造血腥氛围的迷雾效果
     */
    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!isInBloodSurgingLand()) return;

        // 增加雾气密度，缩短视线距离营造氛围
        event.setDensity(0.02F);
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
