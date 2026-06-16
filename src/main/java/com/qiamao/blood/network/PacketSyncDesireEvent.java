package com.qiamao.blood.network;

import com.qiamao.blood.event.DesireEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketSyncDesireEvent implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(PacketSyncDesireEvent.class);
    private boolean active;

    public PacketSyncDesireEvent() {}

    public PacketSyncDesireEvent(boolean active) {
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.active = buf.readBoolean();
        } catch (IndexOutOfBoundsException e) {
            // 防御式降级：异常包体按 false 处理，避免客户端因为坏包崩溃。
            LOGGER.warn("读取欲望事件同步包失败，使用默认值 false", e);
            this.active = false;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.active);
    }

    public static class Handler implements IMessageHandler<PacketSyncDesireEvent, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncDesireEvent message, MessageContext ctx) {
            if (message == null) {
                LOGGER.warn("收到空的欲望事件同步包，已忽略");
                return null;
            }
            // 使用 Proxy 来安全地调度客户端任务，防止服务端加载类时因为找不到 Minecraft 类而崩溃
            com.qiamao.blood.BloodMod.proxy.addScheduledTask(() -> {
                try {
                    com.qiamao.blood.event.DesireEventHandler.setClientEventActive(message.active);
                } catch (RuntimeException e) {
                    LOGGER.error("处理欲望事件客户端同步失败", e);
                }
            });
            return null;
        }
    }
}
