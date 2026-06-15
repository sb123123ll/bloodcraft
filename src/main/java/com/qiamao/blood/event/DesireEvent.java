package com.qiamao.blood.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * 欲望事件 - 夜晚触发的特殊事件
 * 当事件触发时，增强嗜血多足虫和血螨脑控的能力
 */
@Cancelable
public class DesireEvent extends Event {
    private final World world;
    private final EntityPlayer player;
    private boolean isActive;
    
    public DesireEvent(World world, EntityPlayer player) {
        this.world = world;
        this.player = player;
        this.isActive = true;
    }
    
    public World getWorld() {
        return world;
    }
    
    public EntityPlayer getPlayer() {
        return player;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * 发送聊天消息 - 鲜红色加粗字体
     */
    public void sendDesireMessage() {
        if (player != null) {
            Style style = new Style().setColor(TextFormatting.RED).setBold(true);
            ITextComponent message = new TextComponentTranslation("message.blood.desire_event_start").setStyle(style);
            
            player.sendMessage(message);
        }
    }
}
