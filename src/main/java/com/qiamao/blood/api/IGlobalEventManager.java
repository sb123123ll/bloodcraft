package com.qiamao.blood.api;

import net.minecraft.world.World;

/**
 * 全局事件管理器接口
 * 用于协调模组内的各种世界级事件，防止冲突
 */
public interface IGlobalEventManager {
    /**
     * 检查当前是否有任何互斥事件正在运行
     */
    boolean isAnyEventActive(World world);

    /**
     * 获取当前正在运行的事件类型
     */
    String getActiveEventId(World world);

    /**
     * 尝试启动一个事件
     * @return 如果启动成功返回 true，如果已有冲突事件则返回 false
     */
    boolean tryStartEvent(World world, String eventId, int duration);

    /**
     * 强制结束当前事件
     */
    void stopCurrentEvent(World world);

    /**
     * 在每一帧更新事件计时器
     */
    void tick();
}
