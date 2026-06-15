package com.qiamao.blood.network;

import com.qiamao.blood.BloodMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BloodNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BloodMod.MODID);
    private static final Logger LOGGER = LogManager.getLogger(BloodNetwork.class);
    private static int nextId = 0;
    private static boolean initialized = false;

    /**
     * 仅注册一次网络消息，避免重复注册导致崩溃。
     */
    public static void init() {
        if (initialized) {
            return;
        }
        try {
            INSTANCE.registerMessage(PacketSyncDesireEvent.Handler.class, PacketSyncDesireEvent.class, nextId++, Side.CLIENT);
            initialized = true;
        } catch (RuntimeException e) {
            // 防御式处理：记录日志并抛出，让启动阶段错误可见且不被吞掉。
            LOGGER.error("注册 BloodNetwork 消息失败QAQAQAQAQAQAQAQAQAQAQ", e);
            throw e;
        }
    }
}
