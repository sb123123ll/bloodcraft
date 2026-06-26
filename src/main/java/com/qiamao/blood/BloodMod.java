package com.qiamao.blood;

import com.qiamao.blood.init.ModFluids;
import com.qiamao.blood.proxy.CommonProxy;
import com.qiamao.blood.event.DesireEventHandler;
import com.qiamao.blood.event.DesireSpawnManager;
import com.qiamao.blood.event.VanillaMobAITweaks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import com.qiamao.blood.world.ModWorldGeneration;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import com.qiamao.blood.command.CommandBlood;
import com.qiamao.blood.network.BloodNetwork;

@Mod(modid = BloodMod.MODID, name = BloodMod.NAME, version = BloodMod.VERSION)
public class BloodMod {
    public static final String MODID = "blood";
    public static final String NAME = "Bloodcraft";
    public static final String VERSION = "0.0.31a";

    @SidedProxy(clientSide = "com.qiamao.blood.proxy.ClientProxy", serverSide = "com.qiamao.blood.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static BloodMod instance;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        BloodNetwork.init(); // 初始化网络通讯
        ModFluids.registerFluids();
        
        // 通过 Proxy 注册 GUI Handler（防止服务端加载 GuiBloodAltar 等客户端类导致崩溃）
        proxy.registerGuiHandler();

        // 注册世界生成器，权重填 1
        GameRegistry.registerWorldGenerator(new ModWorldGeneration(), 1);

        // 确保 ModBiomes 类被加载（触发静态初始化）
        // @Mod.EventBusSubscriber 会自动处理注册事件
        com.qiamao.blood.init.ModBiomes.class.getName(); // 触发类加载
        
        proxy.preInit(event);
    }

    @EventHandler
    @SuppressWarnings("null")
    public void init(FMLInitializationEvent event) {
        logger.info("Blood mod initialized.");
        
        proxy.init(event);

        // 注册欲望事件处理器
        MinecraftForge.EVENT_BUS.register(new DesireEventHandler());
        
        // 注册原版生物AI调整（村民躲避传教士等）
        MinecraftForge.EVENT_BUS.register(new VanillaMobAITweaks());
        
        // 注册欲望事件生物生成管理器
        DesireSpawnManager.register();

        // 注册发射器行为
        com.qiamao.blood.init.ModItems.registerDispenserBehaviors();
        
        // 注册血祭坛合成配方
        com.qiamao.blood.init.ModBlocks.initAltarRecipes();

        // 注册原木和木板的 OreDictionary
        // 这样它们就可以像原版木头一样用来合成木棍、工作台、木箱、木剑等任何使用木板的配方
        OreDictionary.registerOre("logWood", com.qiamao.blood.init.ModBlocks.BLOOD_LOG);
        OreDictionary.registerOre("plankWood", com.qiamao.blood.init.ModBlocks.BLOOD_PLANKS);
        OreDictionary.registerOre("slabWood", com.qiamao.blood.init.ModBlocks.BLOOD_SLAB_HALF);
        OreDictionary.registerOre("stairWood", com.qiamao.blood.init.ModBlocks.BLOOD_STAIRS);
        OreDictionary.registerOre("treeLeaves", com.qiamao.blood.init.ModBlocks.BLOOD_LEAVES);
        OreDictionary.registerOre("trapdoorWood", com.qiamao.blood.init.ModBlocks.BLOOD_TRAPDOOR);
        OreDictionary.registerOre("doorWood", com.qiamao.blood.init.ModItems.BLOOD_DOOR_ITEM);
        OreDictionary.registerOre("fenceWood", com.qiamao.blood.init.ModBlocks.BLOOD_FENCE);
        OreDictionary.registerOre("fenceGateWood", com.qiamao.blood.init.ModBlocks.BLOOD_FENCE_GATE);

        // 设置可燃性 (火焰蔓延速度和燃烧速度)
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_LOG, 5, 5);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_PLANKS, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_SLAB_HALF, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_SLAB_DOUBLE, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_STAIRS, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_LEAVES, 30, 60);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_TRAPDOOR, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_DOOR, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_FENCE, 5, 20);
        Blocks.FIRE.setFireInfo(com.qiamao.blood.init.ModBlocks.BLOOD_FENCE_GATE, 5, 20);
        
        // 注册熔炉配方：将烂肉烤熟
        GameRegistry.addSmelting(
            com.qiamao.blood.init.ModItems.GORY_FLESH, 
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.COOKED_GORY_FLESH), 
            0.35F // 经验值，和原版烤肉一样
        );

        // 注册熔炉配方：将心脏煮熟
        GameRegistry.addSmelting(
            com.qiamao.blood.init.ModItems.HUMAN_HEART, 
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART), 
            0.35F // 经验值，和原版烤肉一样
        );

        // 注册工作台合成配方：多足虫的刺 + 木棍 -> 毒刺匕首
        GameRegistry.addShapedRecipe(
            new net.minecraft.util.ResourceLocation(MODID, "venomous_stinger_dagger"),
            new net.minecraft.util.ResourceLocation(MODID, "ingredients"),
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.VENOMOUS_STINGER_DAGGER),
            "S",
            "W",
            'S', net.minecraft.item.crafting.Ingredient.fromItem(com.qiamao.blood.init.ModItems.CENTIPEDE_STINGER),
            'W', net.minecraft.item.crafting.Ingredient.fromItem(net.minecraft.init.Items.STICK)
        );

        // 注册酿造台配方：瓶装血液 + 火药 -> 喷溅型瓶装血液
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.BLOOD_BOTTLE),
            new net.minecraft.item.ItemStack(net.minecraft.init.Items.GUNPOWDER),
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.SPLASH_BLOOD_BOTTLE)
        );

        // 注册酿造台配方：喷溅型瓶装血液 + 龙息 -> 滞留型瓶装血液
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.SPLASH_BLOOD_BOTTLE),
            new net.minecraft.item.ItemStack(net.minecraft.init.Items.DRAGON_BREATH),
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.LINGERING_BLOOD_BOTTLE)
        );

        // 注册工作台合成配方：4个毒刺核心碎片 -> 毒刺核心 (2x2)
        GameRegistry.addShapedRecipe(
            new net.minecraft.util.ResourceLocation(MODID, "sting_core"),
            new net.minecraft.util.ResourceLocation(MODID, "ingredients"),
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.STING_CORE),
            "FF",
            "FF",
            'F', net.minecraft.item.crafting.Ingredient.fromItem(com.qiamao.blood.init.ModItems.STING_CORE_FRAGMENT)
        );

        // 注册工作台合成配方：血液淬炼台
        // 木棍 血液核心 木棍
        // 圆石台阶 受诅咒的肉块 圆石台阶
        GameRegistry.addShapedRecipe(
            new net.minecraft.util.ResourceLocation(MODID, "blood_tempering_altar"),
            new net.minecraft.util.ResourceLocation(MODID, "blocks"),
            new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModBlocks.BLOOD_TEMPERING_ALTAR),
            "SBS",
            "CFC",
            'S', net.minecraft.item.crafting.Ingredient.fromItem(net.minecraft.init.Items.STICK),
            'B', net.minecraft.item.crafting.Ingredient.fromItem(com.qiamao.blood.init.ModItems.BLOOD_CORE),
            'C', net.minecraft.item.crafting.Ingredient.fromStacks(new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.STONE_SLAB, 1, 3)),
            'F', net.minecraft.item.crafting.Ingredient.fromItem(com.qiamao.blood.init.ModItems.CURSED_FLESH_CHUNK_ITEM)
        );

        // --- 注册实体自然生成 ---
        // 获取所有可用的生物群系，并分离出沙漠群系
        java.util.List<net.minecraft.world.biome.Biome> spawnBiomes = new java.util.ArrayList<>();
        java.util.List<net.minecraft.world.biome.Biome> desertBiomes = new java.util.ArrayList<>();
        for (net.minecraft.world.biome.Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.getValuesCollection()) {
            // 只有下雪的群系不生成 (SNOWY / COLD)
            if (!net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.SNOWY) && 
                !net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.COLD)) {
                
                spawnBiomes.add(biome);
                
                // 将沙漠（或干燥炎热）的群系单独挑出来
                if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.SANDY) ||
                    net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.HOT)) {
                    desertBiomes.add(biome);
                }
            }
        }

        // 生成率调整
        // 多足嗜血虫 MONSTER: 7 (降低60%)
        // 多足嗜血虫 CREATURE: 7 (降低60%)
        // 血螨脑控 MONSTER: 7 (降低60%)
        
        // 血螨脑控在沙漠群系额外权重按比例调整
        // 沙漠血螨脑控额外: 1

        // 双管齐下，直接用原版的 MONSTER 和 CREATURE 类型：
        // 1. 注册为 MONSTER：权重 8，主要负责在洞穴中持续不断地刷新
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityBloodSeeker.class, 
                8, 
                3, 
                4, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // 2. 注册为 CREATURE（动物类）：权重 8
        // 关键点：动物类会在【区块第一次生成时】以及【白天光照充足时】强制尝试生成！
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityBloodSeeker.class, 
                8, 
                3, 
                4, 
                net.minecraft.entity.EnumCreatureType.CREATURE, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // --- 血液猎犬生成 (Blood Hound) ---
        // 权重7。每次2-4只。不在干燥和寒冷群系生成。
        java.util.List<net.minecraft.world.biome.Biome> houndBiomes = new java.util.ArrayList<>();
        for (net.minecraft.world.biome.Biome biome : spawnBiomes) {
            if (!net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.SANDY) &&
                !net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.DRY) &&
                !net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.HOT)) {
                houndBiomes.add(biome);
            }
        }
        
        // 注册为 MONSTER (晚上)
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityBloodHound.class, 
                7, 
                2, 
                4, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                houndBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );
        // 注册为 CREATURE (白天)
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityBloodHound.class, 
                7, 
                2, 
                4, 
                net.minecraft.entity.EnumCreatureType.CREATURE, 
                houndBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // --- 视神经生成 (Optic Nerve) ---
        // 主世界晚上: 权重7
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityOpticNerve.class, 
                7, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // 森林群系额外权重: 权重7
        // 找出所有森林群系
        java.util.List<net.minecraft.world.biome.Biome> forestBiomes = new java.util.ArrayList<>();
        for (net.minecraft.world.biome.Biome biome : spawnBiomes) {
            if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.FOREST)) {
                forestBiomes.add(biome);
            }
        }
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityOpticNerve.class, 
                7, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                forestBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // 下界生成
        java.util.List<net.minecraft.world.biome.Biome> netherBiomes = new java.util.ArrayList<>();
        for (net.minecraft.world.biome.Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.getValuesCollection()) {
            if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.NETHER)) {
                netherBiomes.add(biome);
            }
        }
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityOpticNerve.class, 
                7, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                netherBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // --- 寄生史蒂夫生成 (普通群系) ---
        // 单只生成，权重 8
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityParasiticSteve.class, 
                8, 
                1, 
                1, // 单只生成
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // --- 寄生史蒂夫生成 (沙漠额外追加) ---
        // 沙漠群系额外权重按比例调整
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityParasiticSteve.class, 
                2, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                desertBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );

        // --- 血液翻腾之地群系特殊生成（按比例调整） ---
        // 注：血螨不自然生成（由投掷产生），保持原有逻辑
        // 多足嗜血虫 MONSTER: 15
        // 多足嗜血虫 CREATURE: 15
        // 血螨脑控 MONSTER: 15

        net.minecraft.world.biome.Biome bloodSurgingLand = com.qiamao.blood.init.ModBiomes.BLOOD_SURGING_LAND;

        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityParasiticSteve.class,
                15,
                1,
                1,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                bloodSurgingLand
        );

        // 血液翻腾之地额外追加：权重5 (总计12)

        // --- 迫近者生成 (Approacher) ---
        // 主世界全部群系地表生成 (权重7，单只) - 改为 MONSTER 使其只在晚上生成
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityApproacher.class, 
                7, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );
        // 主世界全部群系地下洞穴生成 (权重6，单只) - 使用 MONSTER 在黑暗洞穴生成
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.qiamao.blood.entity.EntityApproacher.class, 
                6, 
                1, 
                1, 
                net.minecraft.entity.EnumCreatureType.MONSTER, 
                spawnBiomes.toArray(new net.minecraft.world.biome.Biome[0])
        );
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // 注册指令
        event.registerServerCommand(new CommandBlood());
    }
}
