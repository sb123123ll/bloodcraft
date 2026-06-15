package com.qiamao.blood.client;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.init.ModBlocks;
import com.qiamao.blood.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.qiamao.blood.entity.EntityBloodSeeker;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * 模型注册处理类
 * 仅在客户端加载，负责将物品/方块与对应的模型和贴图绑定
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = BloodMod.MODID)
public class ModelRegistryHandler {

    public static TextureAtlasSprite BLOOD_DROP_PARTICLE_SPRITE;

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        // 在游戏拼接方块图集 (blocks) 时，把我们自定义的粒子贴图强行加进去
        // 这样 Particle 系统就可以通过 TextureAtlasSprite 来渲染它了
        BLOOD_DROP_PARTICLE_SPRITE = event.getMap().registerSprite(new ResourceLocation(BloodMod.MODID, "particle/blood_drop"));
    }

    /**
     * 监听模型注册事件
     * 在物品注册完成后调用，为物品绑定渲染模型
     */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // 绑定实体与渲染器
        RenderingRegistry.registerEntityRenderingHandler(EntityBloodSeeker.class, com.qiamao.blood.client.render.RenderBloodSeeker::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityBloodSeekerLarva.class, com.qiamao.blood.client.render.RenderBloodSeekerLarva::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityBloodEndermite.class, com.qiamao.blood.client.render.RenderBloodMite::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityParasiticSteve.class, com.qiamao.blood.client.render.RenderParasiticSteve::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityThrownBloodMite.class, com.qiamao.blood.client.render.RenderThrownBloodMite::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityPoisonBubble.class, com.qiamao.blood.client.render.RenderPoisonBubble::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityBloodMother.class, com.qiamao.blood.client.render.RenderBloodMother::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityCultistPreacher.class, com.qiamao.blood.client.render.RenderCultistPreacher::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntityOpticNerve.class, com.qiamao.blood.client.render.RenderOpticNerve::new);
        RenderingRegistry.registerEntityRenderingHandler(com.qiamao.blood.entity.EntitySplashBlood.class, manager -> new com.qiamao.blood.client.render.RenderSplashBlood(manager, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));

        // 为 bucket_blood 注册模型资源位置，"inventory" 代表物品栏中的状态
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BUCKET_BLOOD, 
                0,
                new ModelResourceLocation(ModItems.BUCKET_BLOOD.getRegistryName(), "inventory")
        );

        // 为 blood_bottle 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_BOTTLE,
                0,
                new ModelResourceLocation(ModItems.BLOOD_BOTTLE.getRegistryName(), "inventory")
        );

        // 为 splash_blood_bottle 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.SPLASH_BLOOD_BOTTLE,
                0,
                new ModelResourceLocation(ModItems.SPLASH_BLOOD_BOTTLE.getRegistryName(), "inventory")
        );

        // 为 lingering_blood_bottle 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.LINGERING_BLOOD_BOTTLE,
                0,
                new ModelResourceLocation(ModItems.LINGERING_BLOOD_BOTTLE.getRegistryName(), "inventory")
        );

        // 为 cursed_flesh_chunk 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.CURSED_FLESH_CHUNK_ITEM,
                0,
                new ModelResourceLocation(ModItems.CURSED_FLESH_CHUNK_ITEM.getRegistryName(), "inventory")
        );

        // 为 blood_log_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_LOG_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_LOG_ITEM.getRegistryName(), "inventory")
        );

        // 为 blood_planks_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_PLANKS_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_PLANKS_ITEM.getRegistryName(), "inventory")
        );

        // 为 blood_slab_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_SLAB_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_SLAB_ITEM.getRegistryName(), "inventory")
        );

        // 为 blood_stairs_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_STAIRS_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_STAIRS_ITEM.getRegistryName(), "inventory")
        );

        // 为 blood_leaves_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_LEAVES_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_LEAVES_ITEM.getRegistryName(), "inventory")
        );

        // 为 flesh_chunk_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.FLESH_CHUNK_ITEM,
                0,
                new ModelResourceLocation(ModItems.FLESH_CHUNK_ITEM.getRegistryName(), "inventory")
        );

        // 为 centipede_stinger 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.CENTIPEDE_STINGER,
                0,
                new ModelResourceLocation(ModItems.CENTIPEDE_STINGER.getRegistryName(), "inventory")
        );

        // 为 stinger_powder 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.STINGER_POWDER,
                0,
                new ModelResourceLocation(ModItems.STINGER_POWDER.getRegistryName(), "inventory")
        );

        // 为隐藏图标物品注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.TAB_ICON,
                0,
                new ModelResourceLocation(ModItems.TAB_ICON.getRegistryName(), "inventory")
        );

        // 为 plucked_eye 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.PLUCKED_EYE,
                0,
                new ModelResourceLocation(ModItems.PLUCKED_EYE.getRegistryName(), "inventory")
        );

        // 为 gory_flesh 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.GORY_FLESH,
                0,
                new ModelResourceLocation(ModItems.GORY_FLESH.getRegistryName(), "inventory")
        );

        // 为 cooked_gory_flesh 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.COOKED_GORY_FLESH,
                0,
                new ModelResourceLocation(ModItems.COOKED_GORY_FLESH.getRegistryName(), "inventory")
        );

        // 为 thrown_blood_mite_item 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.THROWN_BLOOD_MITE_ITEM,
                0,
                new ModelResourceLocation(ModItems.THROWN_BLOOD_MITE_ITEM.getRegistryName(), "inventory")
        );

        // 为 parasitic_steve_face 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.PARASITIC_STEVE_FACE,
                0,
                new ModelResourceLocation(ModItems.PARASITIC_STEVE_FACE.getRegistryName(), "inventory")
        );

        // 为 venomous_stinger_dagger 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.VENOMOUS_STINGER_DAGGER,
                0,
                new ModelResourceLocation(ModItems.VENOMOUS_STINGER_DAGGER.getRegistryName(), "inventory")
        );

        // 为 blood_mite_meat 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_MITE_MEAT,
                0,
                new ModelResourceLocation(ModItems.BLOOD_MITE_MEAT.getRegistryName(), "inventory")
        );

        // 为 monster_bone_axe 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.MONSTER_BONE_AXE,
                0,
                new ModelResourceLocation(ModItems.MONSTER_BONE_AXE.getRegistryName(), "inventory")
        );

        // 为 blood_sapling_item 注册模型
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_SAPLING_ITEM,
                0,
                new ModelResourceLocation(ModItems.BLOOD_SAPLING_ITEM.getRegistryName(), "inventory")
        );

        // 为 monster_bone_skeleton 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.MONSTER_BONE_SKELETON,
                0,
                new ModelResourceLocation(ModItems.MONSTER_BONE_SKELETON.getRegistryName(), "inventory")
        );

        // 为 cultist_preacher_face 注册模型资源位置（使用自定义渲染）
        ModelLoader.setCustomModelResourceLocation(
                ModItems.CULTIST_PREACHER_FACE,
                0,
                new ModelResourceLocation(ModItems.CULTIST_PREACHER_FACE.getRegistryName(), "inventory")
        );

        // 为 sting_core 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.STING_CORE,
                0,
                new ModelResourceLocation(ModItems.STING_CORE.getRegistryName(), "inventory")
        );

        // 为 sting_core_fragment 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.STING_CORE_FRAGMENT,
                0,
                new ModelResourceLocation(ModItems.STING_CORE_FRAGMENT.getRegistryName(), "inventory")
        );

        // 为 blood_core 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BLOOD_CORE,
                0,
                new ModelResourceLocation(ModItems.BLOOD_CORE.getRegistryName(), "inventory")
        );

        // 为 blood_altar 注册模型资源位置
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.BLOOD_ALTAR),
                0,
                new ModelResourceLocation(ModBlocks.BLOOD_ALTAR.getRegistryName(), "inventory")
        );

        // 注册血液淬炼台的模型
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.BLOOD_TEMPERING_ALTAR),
                0,
                new ModelResourceLocation(ModBlocks.BLOOD_TEMPERING_ALTAR.getRegistryName(), "inventory")
        );

        ModelLoader.setCustomModelResourceLocation(ModItems.PARASITIC_STEVE_HEAD_ITEM, 0, new ModelResourceLocation(ModBlocks.PARASITIC_STEVE_HEAD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.PREACHER_HEAD_ITEM, 0, new ModelResourceLocation(ModBlocks.PREACHER_HEAD.getRegistryName(), "inventory"));

        ModelLoader.setCustomModelResourceLocation(ModItems.BLOOD_TRAPDOOR_ITEM, 0, new ModelResourceLocation(ModBlocks.BLOOD_TRAPDOOR.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.BLOOD_DOOR_ITEM, 0, new ModelResourceLocation(ModItems.BLOOD_DOOR_ITEM.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.BLOOD_FENCE_ITEM, 0, new ModelResourceLocation(ModBlocks.BLOOD_FENCE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.BLOOD_FENCE_GATE_ITEM, 0, new ModelResourceLocation(ModBlocks.BLOOD_FENCE_GATE.getRegistryName(), "inventory"));

        // 忽略门和栅栏门状态中的 powered 属性，防止由于缺少该属性变种导致模型加载失败（即出现紫黑块）
        ModelLoader.setCustomStateMapper(ModBlocks.BLOOD_DOOR, (new net.minecraft.client.renderer.block.statemap.StateMap.Builder()).ignore(net.minecraft.block.BlockDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.BLOOD_FENCE_GATE, (new net.minecraft.client.renderer.block.statemap.StateMap.Builder()).ignore(net.minecraft.block.BlockFenceGate.POWERED).build());

        // 注册流体方块的渲染状态映射
        registerFluidModel(ModBlocks.BLOCK_BLOOD, "blood");
    }

    private static void registerFluidModel(Block block, String name) {
        Item item = Item.getItemFromBlock(block);
        ModelBakery.registerItemVariants(item);

        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(BloodMod.MODID + ":" + name, "fluid");

        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return modelResourceLocation;
            }
        });

        ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(net.minecraft.block.state.IBlockState state) {
                return modelResourceLocation;
            }
        });
    }
}
