package com.qiamao.blood.event;

import com.qiamao.blood.api.IGlobalEventManager;
import com.qiamao.blood.config.DesireEventConfig;
import com.qiamao.blood.entity.EntityBloodSeeker;
import com.qiamao.blood.entity.EntityParasiticSteve;
import com.qiamao.blood.init.ModSounds;
import com.qiamao.blood.network.BloodNetwork;
import com.qiamao.blood.network.PacketSyncDesireEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 欲望事件处理器
 * 负责检测夜晚时间并触发欲望事件
 */
public class DesireEventHandler {
    
    private static final Logger LOGGER = LogManager.getLogger(DesireEventHandler.class);
    private static final Random RANDOM = new Random();
    private static final int EVENT_DURATION_TICKS = 24000; // 事件持续一整天
    private static DesireEvent currentDesireEvent = null;
    private static final AtomicBoolean isEventActive = new AtomicBoolean(false);
    private static final AtomicBoolean isClientEventActive = new AtomicBoolean(false);
    private static boolean forceNextNight = false;
    
    /**
     * 客户端调用：同步事件状态
     */
    public static void setClientEventActive(boolean active) {
        isClientEventActive.set(active);
    }
    
    /**
     * 强制下个夜晚触发事件
     */
    public static void forceNextNightEvent() {
        forceNextNight = true;
    }
    
    private static int syncTimer = 0;

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            // 当玩家登录时，同步当前事件状态；发送失败不应影响登录流程。
            try {
                BloodNetwork.INSTANCE.sendTo(new PacketSyncDesireEvent(isEventActive.get()), (EntityPlayerMP) event.player);
            } catch (RuntimeException e) {
                LOGGER.error("玩家登录时同步欲望事件状态失败: {}", event.player.getName(), e);
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.provider.getDimension() != 0) {
            return;
        }
        try {
            World world = event.world;
            IGlobalEventManager manager = GlobalEventManager.INSTANCE;
            
            // 核心保险：定期全量同步状态（每 10 秒一次）
            if (++syncTimer >= 200) {
                syncTimer = 0;
                BloodNetwork.INSTANCE.sendToAll(new PacketSyncDesireEvent(isEventActive.get()));
            }
            
            manager.tick();
            
            String activeEvent = manager.getActiveEventId(world);
            
            // 检查事件是否已经自然结束（由 GlobalEventManager 计时结束）
            if (isEventActive.get() && activeEvent == null) {
                endDesireEvent();
            }
            
            if (activeEvent != null && !"desire_event".equals(activeEvent)) {
                return;
            }
            
            // 1. 如果事件正在运行
            if (isEventActive.get()) {
                // 检查当前是否仍处于夜晚时间
                if (!isNightTime(world)) {
                    // 如果已经不是夜晚（无论是自然流逝还是指令调时），立即强制结束
                    manager.stopCurrentEvent(world);
                    endDesireEvent();
                    return;
                }
                // 事件运行中且是夜晚，直接返回，不进行触发判定
                return;
            }
            
            // 2. 触发逻辑：如果是夜晚，且不在运行中
            if (isNightTime(world)) {
                // 核心修复：确保事件只在刚入夜的一瞬间（13000-13020）尝试触发一次
                long time = world.getWorldTime() % 24000;
                if (time >= 13000 && time <= 13020) {
                    if (forceNextNight || RANDOM.nextDouble() < DesireEventConfig.getDesireEventProbability()) {
                        if (manager.tryStartEvent(world, "desire_event", EVENT_DURATION_TICKS)) {
                            triggerDesireEvent(world);
                            forceNextNight = false;
                        }
                    } else {
                        forceNextNight = false;
                    }
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("处理欲望事件世界 Tick 失败，已跳过本 Tick 防止崩溃", e);
        }
    }
    
    /**
     * 检查是否为夜晚时间
     */
    private boolean isNightTime(World world) {
        long time = world.getWorldTime() % 24000;
        return time >= 13000 && time <= 23000; // 夜晚时间段
    }
    
    /**
     * 触发欲望事件
     */
    private void triggerDesireEvent(World world) {
        // 找到一个在线主世界玩家作为事件触发者
        EntityPlayer player = null;
        if (world.getMinecraftServer() != null) {
            for (EntityPlayerMP p : world.getMinecraftServer().getPlayerList().getPlayers()) {
                if (p.dimension == 0) {
                    player = p;
                    break;
                }
            }
        }
        
        if (player != null) {
            try {
                // 创建并触发事件
                currentDesireEvent = new DesireEvent(world, player);
                isEventActive.set(true);
                
                // 发送聊天消息
                currentDesireEvent.sendDesireMessage();
                
                // 同步到所有客户端
                BloodNetwork.INSTANCE.sendToAll(new PacketSyncDesireEvent(true));
                
                // 播放音效
                world.playSound(null, player.posX, player.posY, player.posZ, 
                              ModSounds.DESIRE_EVENT, 
                              SoundCategory.AMBIENT, 
                              1.0F, 1.0F);
                
                // 应用生物增强效果
                updateEntityEnhancements(world, true);
            } catch (RuntimeException e) {
                LOGGER.error("触发欲望事件失败，准备回滚状态", e);
                isEventActive.set(false);
                currentDesireEvent = null;
                GlobalEventManager.INSTANCE.stopCurrentEvent(world);
            }
        } else {
            // 如果没找到玩家，撤回全局注册
            GlobalEventManager.INSTANCE.stopCurrentEvent(world);
        }
    }
    
    /**
     * 结束欲望事件
     */
    private void endDesireEvent() {
        isEventActive.set(false);
        isClientEventActive.set(false); // 同时也清理本地（如果是单机模式）
        
        if (currentDesireEvent != null) {
            try {
                updateEntityEnhancements(currentDesireEvent.getWorld(), false);
            } catch (RuntimeException e) {
                LOGGER.error("结束欲望事件时清理生物增强失败", e);
            }
            currentDesireEvent = null;
        }
        
        try {
            GlobalEventManager.INSTANCE.stopCurrentEvent(null);
        } catch (RuntimeException e) {
            LOGGER.error("结束欲望事件时停止全局事件失败", e);
        }
        
        // 同步到所有客户端
        try {
            BloodNetwork.INSTANCE.sendToAll(new PacketSyncDesireEvent(false));
        } catch (RuntimeException e) {
            LOGGER.error("结束欲望事件时同步客户端状态失败", e);
        }
    }
    
    /**
     * 统一更新生物增强效果
     */
    private void updateEntityEnhancements(World world, boolean active) {
        if (world == null) {
            return;
        }
        // 优化：只遍历 EntityLivingBase 类型的实体，避免遍历所有非生命实体（如掉落物、投掷物）
        for (Entity entity : world.loadedEntityList) {
            if (entity instanceof EntityBloodSeeker || entity instanceof EntityParasiticSteve) {
                try {
                    applyToEntity((EntityLivingBase) entity, active);
                } catch (RuntimeException e) {
                    LOGGER.warn("更新实体欲望增强状态失败: {}", entity.getClass().getSimpleName(), e);
                }
            }
        }
    }

    private void applyToEntity(EntityLivingBase entity, boolean active) {
        if (entity instanceof EntityBloodSeeker) {
            EntityBloodSeeker bloodSeeker = (EntityBloodSeeker) entity;
            bloodSeeker.setDesireEventActive(active);
            bloodSeeker.updateDesireEventAttributes();
        } else if (entity instanceof EntityParasiticSteve) {
            EntityParasiticSteve parasiticSteve = (EntityParasiticSteve) entity;
            parasiticSteve.setDesireEventActive(active);
            parasiticSteve.updateDesireEventAttributes();
        }
    }
    
    /**
     * 获取当前欲望事件状态
     */
    public static boolean isDesireEventActive(World world) {
        if (world == null || world.provider == null) {
            return false;
        }
        // 主世界判断
        if (world.provider.getDimension() != 0) return false;
        
        // 如果是客户端，使用同步的状态
        if (world.isRemote) {
            return isClientEventActive.get();
        }
        
        // 如果是服务端，使用原始状态
        return isEventActive.get();
    }
}
