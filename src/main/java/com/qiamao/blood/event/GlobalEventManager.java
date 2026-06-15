package com.qiamao.blood.event;

import com.qiamao.blood.api.IGlobalEventManager;
import net.minecraft.world.World;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 全局事件管理器实现
 * 采用单例模式，确保所有功能模块共享同一个状态
 */
public class GlobalEventManager implements IGlobalEventManager {

    public static final GlobalEventManager INSTANCE = new GlobalEventManager();
    
    private final AtomicReference<String> activeEventId = new AtomicReference<>(null);
    private final AtomicInteger remainingTicks = new AtomicInteger(0);

    private GlobalEventManager() {}

    @Override
    public boolean isAnyEventActive(World world) {
        return activeEventId.get() != null;
    }

    @Override
    public String getActiveEventId(World world) {
        return activeEventId.get();
    }

    @Override
    public boolean tryStartEvent(World world, String eventId, int duration) {
        if (activeEventId.compareAndSet(null, eventId)) {
            remainingTicks.set(duration);
            return true;
        }
        return false;
    }

    @Override
    public void stopCurrentEvent(World world) {
        activeEventId.set(null);
        remainingTicks.set(0);
    }

    /**
     * 在每一帧更新事件计时器
     */
    public void tick() {
        if (activeEventId.get() != null) {
            if (remainingTicks.decrementAndGet() <= 0) {
                stopCurrentEvent(null);
            }
        }
    }
}
