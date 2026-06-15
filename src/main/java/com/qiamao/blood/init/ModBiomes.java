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

        // 添加到世界生成，权重为8，生成在温暖气候带
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM,
            new BiomeManager.BiomeEntry(BLOOD_SURGING_LAND, 8));

        // 禁用原版村庄生成（特殊环境不适合）
        BiomeManager.removeVillageBiome(BLOOD_SURGING_LAND);

        // 添加为可生成生物群系
        BiomeManager.addSpawnBiome(BLOOD_SURGING_LAND);
    }

    /**
     * 世界加载事件 - 使用反射替换BiomeProvider以减小群系面积大小
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        // 只在主世界和默认世界类型替换BiomeProvider
        if (world.provider.getDimension() == 0 && world.getWorldType() == WorldType.DEFAULT) {
            try {
                if (world.provider.getBiomeProvider() instanceof BiomeProviderBloodSurgingLand) {
                    return;
                }

                // Forge 环境下的 ReflectionHelper 有时由于底层类加载器差异可能仍抛出异常
                // 我们继续采用双重捕获，但将抛错全部吃掉以防止崩溃游戏，因为这不是致命功能
                Field biomeProviderField = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(
                        net.minecraft.world.WorldProvider.class, 
                        "biomeProvider", "field_76578_c"
                );
                
                if (biomeProviderField != null) {
                    // 替换为自定义BiomeProvider，通过额外Zoom层使群系面积减半
                    BiomeProviderBloodSurgingLand newProvider = new BiomeProviderBloodSurgingLand(world);
                    biomeProviderField.set(world.provider, newProvider);
                }
            } catch (Exception e) {
                // 捕获所有异常（包括 UnableToFindFieldException, RuntimeException, SecurityException等）
                // 仅打印堆栈信息，不要让异常往上抛出导致客户端崩溃
                System.err.println("[Bloodcraft] Failed to replace BiomeProvider for custom biome size. The world will generate with default sizes.");
                e.printStackTrace();
            }
        }
    }
}
