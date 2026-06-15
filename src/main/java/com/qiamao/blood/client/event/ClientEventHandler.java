package com.qiamao.blood.client.event;

import com.qiamao.blood.entity.EntityBloodMother;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 客户端事件处理器
 * 用于处理客户端特定事件，如绘制自定义boss血条
 */
@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    // 使用原版icons.png纹理
    private static final ResourceLocation BOSS_BAR_TEXTURE = new ResourceLocation("textures/gui/icons.png");

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            renderBloodMotherBossBar(event);
        }
    }

    /**
     * 绘制血液母体的boss血条
     */
    private void renderBloodMotherBossBar(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        
        if (player == null || player.world == null) {
            return;
        }

        // 查找附近的血液母体
        EntityBloodMother bloodMother = null;
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityBloodMother && entity.isEntityAlive()) {
                double distance = player.getDistance(entity);
                if (distance <= 32.0D) { // 索敌范围内
                    bloodMother = (EntityBloodMother) entity;
                    break;
                }
            }
        }

        if (bloodMother == null) {
            return;
        }

        // 绘制boss血条
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        int barWidth = 182;
        int barHeight = 5;
        int x = (width - barWidth) / 2;
        int y = 12; // 屏幕最上面

        // 绑定原版纹理
        mc.getTextureManager().bindTexture(BOSS_BAR_TEXTURE);

        // 绘制血条背景（原版boss血条背景）
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, barWidth, barHeight);

        // 计算血量百分比
        float healthPercent = bloodMother.getHealth() / bloodMother.getMaxHealth();
        int healthWidth = (int)(barWidth * healthPercent);

        // 绘制血条（原版boss血条）
        mc.ingameGUI.drawTexturedModalRect(x, y, 0, 79, healthWidth, barHeight);

        // 绘制boss名称
        String bossName = new TextComponentTranslation("entity.blood.blood_mother.name").getFormattedText();
        mc.fontRenderer.drawString(bossName, (width - mc.fontRenderer.getStringWidth(bossName)) / 2, y - 12, 0xFFFFFF);
    }
}
