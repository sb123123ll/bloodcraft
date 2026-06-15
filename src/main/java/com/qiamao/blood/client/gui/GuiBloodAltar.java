package com.qiamao.blood.client.gui;

import com.qiamao.blood.BloodMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import com.qiamao.blood.block.tileentity.TileEntityBloodAltar;

public class GuiBloodAltar extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMod.MODID, "textures/gui/container/blood_tempering_altar.png");
    private final InventoryPlayer playerInventory;
    private final TileEntityBloodAltar tileEntity;

    public GuiBloodAltar(InventoryPlayer playerInv, TileEntityBloodAltar te) {
        super(new ContainerBloodAltar(playerInv, te));
        this.playerInventory = playerInv;
        this.tileEntity = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // 绘制标题文字，颜色使用白色，并且我们这里使用原版支持的 TextFormatting.BOLD 来加粗
        String title = net.minecraft.util.text.TextFormatting.BOLD + net.minecraft.client.resources.I18n.format("tile.blood_tempering_altar.name");
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0xFFFFFF);
        this.fontRenderer.drawString(net.minecraft.util.text.TextFormatting.BOLD + this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
