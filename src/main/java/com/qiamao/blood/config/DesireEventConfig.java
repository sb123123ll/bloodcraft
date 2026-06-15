package com.qiamao.blood.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.fml.common.Mod;

/**
 * 欲望事件配置文件
 * 用于配置欲望事件的各种参数
 */
@Mod.EventBusSubscriber
@Config(modid = "bloodcraft", name = "bloodcraft", category = "desire_event")
public class DesireEventConfig {

    @Name("Desire Event Spawn Chance")
    @Comment("欲望事件生成频率（百分比）。范围：0.0-100.0，默认：1.5")
    @RangeDouble(min = 0.0, max = 100.0)
    @Config.LangKey("config.blood.desire_event_spawn_chance")
    public static double desireEventSpawnChance = 1.5;

    @Name("Desire Event Spawn Boost")
    @Comment("欲望事件期间目标生物的额外生成概率（百分比）。范围：0.0-100.0，默认：70.0")
    @RangeDouble(min = 0.0, max = 100.0)
    @Config.LangKey("config.blood.desire_event_spawn_boost")
    public static double desireEventSpawnBoost = 70.0;

    /**
     * 获取欲望事件生成概率（0.0-1.0之间的小数）
     */
    public static double getDesireEventProbability() {
        return desireEventSpawnChance / 100.0;
    }

    /**
     * 获取欲望事件期间生物生成增强概率（0.0-1.0之间的小数）
     */
    public static double getSpawnBoostProbability() {
        return desireEventSpawnBoost / 100.0;
    }
}
