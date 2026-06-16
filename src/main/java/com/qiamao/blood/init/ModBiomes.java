package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.world.biome.BiomeBloodSurgingLand;
import com.qiamao.blood.world.biome.BiomeProviderBloodSurgingLand;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

/**
 * 生物群系注册类
 * 负责注册血液翻腾之地群系
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModBiomes {

    // 血液翻腾之地群系实例
    public static final Biome BLOOD_SURGING_LAND = new BiomeBloodSurgingLand();

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        // 注册群系
        event.getRegistry().register(BLOOD_SURGING_LAND
            .setRegistryName(BloodMod.MODID, "blood_surging_land"));

        // 添加到生物群系字典，标记为炎热、潮湿、死寂、特殊类型
        BiomeDictionary.addTypes(BLOOD_SURGING_LAND,
            BiomeDictionary.Type.HOT,
            BiomeDictionary.Type.WET,
            BiomeDictionary.Type.SPOOKY,
            BiomeDictionary.Type.DEAD,
            BiomeDictionary.Type.RARE
        );

        // 添加到世界生成，生成在温暖气候带，权重为11
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM,
            new BiomeManager.BiomeEntry(BLOOD_SURGING_LAND, 11));
        
        // 生成在凉爽气候带，权重为9
        BiomeManager.addBiome(BiomeManager.BiomeType.COOL,
            new BiomeManager.BiomeEntry(BLOOD_SURGING_LAND, 9));

        // 禁用原版村庄生成（特殊环境不适合）
        BiomeManager.removeVillageBiome(BLOOD_SURGING_LAND);

        // 添加为可生成生物群系
        BiomeManager.addSpawnBiome(BLOOD_SURGING_LAND);
    }
}
