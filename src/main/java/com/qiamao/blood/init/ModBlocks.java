package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.creativetab.CreativeTabs;

import java.util.ArrayList;
import java.util.List;

/**
 * 方块注册类
 * 负责统一管理和注册模组中的所有方块
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModBlocks {

    public static void initAltarRecipes() {
        // 在这里注册所有的自定义配方！
        // 示例：腐肉 (上方/下方) + 骨头 (上方/下方) -> 血腥糜烂的肉
        com.qiamao.blood.api.AltarRecipeRegistry.addRecipe(
            net.minecraft.init.Items.ROTTEN_FLESH, 
            net.minecraft.init.Items.BONE, 
            new net.minecraft.item.ItemStack(ModItems.GORY_FLESH, 1)
        );
    }

    // 实例化流体方块，它继承自 BlockFluidClassic
    public static final Block BLOCK_BLOOD = new BlockFluidClassic(ModFluids.FLUID_BLOOD, Material.WATER) {
        {
            // 开启类似原版水的 2x2 无限水源特性
            this.canCreateSources = true;
        }

        /**
         * 当实体在流体中移动/碰撞时触发
         * 修复向上游速度问题，确保与水一致
         */
        @Override
        public void onEntityCollidedWithBlock(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
            // 先执行原版流体逻辑
            super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);

            if (entityIn instanceof net.minecraft.entity.EntityLivingBase) {
                entityIn.fallDistance = 0.0F;
                
                // 彻底解决向上游慢的问题：
                // 如果实体在流体中，且正在向上尝试移动（按住跳跃键）
                // 1.12.2 原版水上升速度大约是 motionY 达到 0.1 左右，我们直接强制修正
                if (entityIn.isOffsetPositionInLiquid(0.0D, 1.0D, 0.0D)) {
                    if (entityIn.motionY < 0.13D) {
                        // 给予足够的向上冲力，抵消 BlockFluidClassic 的默认阻力
                        entityIn.motionY += 0.05D;
                    }
                    
                    // 限制最大上升速度，防止过快
                    if (entityIn.motionY > 0.15D) {
                        entityIn.motionY = 0.15D;
                    }
                }
            }

            // 仅在服务端执行伤害逻辑，并且仅对活着的基础生物生效，减少开销
            if (!worldIn.isRemote && entityIn instanceof net.minecraft.entity.EntityLivingBase) {
                net.minecraft.entity.EntityLivingBase livingEntity = (net.minecraft.entity.EntityLivingBase) entityIn;

                // 判断是否为亡灵生物
                if (livingEntity.getCreatureAttribute() == net.minecraft.entity.EnumCreatureAttribute.UNDEAD) {
                    
                    // hurtResistantTime 默认为 20 ticks (1秒)。
                    // 只要这个值小于等于 0（或者它没因为该伤害处于无敌期），我们就可以立刻给它造成伤害
                    // 这样刚掉进去时必定受伤，之后每 20 ticks 免疫期结束又会再次受伤。
                    if (livingEntity.hurtResistantTime <= 0) {
                        // 生成 3 到 5 的随机整数伤害：nextInt(3) 会生成 0, 1, 2，加上 3 就是 3, 4, 5
                        float damageAmount = 3.0F + worldIn.rand.nextInt(3);
                        
                        // 使用通用魔法伤害类型（无视护甲，不可格挡）
                        livingEntity.attackEntityFrom(net.minecraft.util.DamageSource.MAGIC, damageAmount);
                    }
                }
            }
        }
    }
            .setRegistryName(BloodMod.MODID, "blood")
            .setUnlocalizedName("blood");

    // 注册受诅咒的肉块 (Cursed Flesh Chunk)
    public static final Block BLOOD_SEEKER_FLESH = new com.qiamao.blood.block.BlockBloodSeekerFlesh("cursed_flesh_chunk")
            .setUnlocalizedName("cursed_flesh_chunk")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥原木方块
    public static final Block BLOOD_LOG = new com.qiamao.blood.block.BlockBloodLog()
            .setUnlocalizedName("blood_log")
            .setRegistryName(BloodMod.MODID, "blood_log")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥木板方块
    public static final Block BLOOD_PLANKS = new com.qiamao.blood.block.BlockBloodPlanks()
            .setUnlocalizedName("blood_planks")
            .setRegistryName(BloodMod.MODID, "blood_planks")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥树叶方块
    public static final Block BLOOD_LEAVES = new com.qiamao.blood.block.BlockBloodLeaves()
            .setUnlocalizedName("blood_leaves")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 注册血腥台阶
    public static final com.qiamao.blood.block.BlockBloodSlab BLOOD_SLAB_HALF = new com.qiamao.blood.block.BlockBloodSlabHalf();
    public static final com.qiamao.blood.block.BlockBloodSlab BLOOD_SLAB_DOUBLE = new com.qiamao.blood.block.BlockBloodSlabDouble();

    // 注册血腥楼梯
    public static final Block BLOOD_STAIRS = new com.qiamao.blood.block.BlockBloodStairs(BLOOD_PLANKS.getDefaultState());

    // 肉块 (Flesh Chunk) - 普通血腥糜烂的肉块
    public static final Block FLESH_CHUNK = new com.qiamao.blood.block.BlockFleshChunk("flesh_chunk")
            .setUnlocalizedName("flesh_chunk")
            .setRegistryName(BloodMod.MODID, "flesh_chunk")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血块 (Blood Block) - 血液翻腾之地群系专用
    // setRegistryName和setUnlocalizedName已在BlockBloodBlock构造函数中设置
    // 不设置创造标签页，使其从创造模式物品栏中隐藏
    public static final Block BLOOD_BLOCK = new com.qiamao.blood.block.BlockBloodBlock("blood_block");

    // 血腥树苗 (Blood Sapling)
    public static final Block BLOOD_SAPLING = new com.qiamao.blood.block.BlockBloodSapling();

    // 血液祭坛 (Blood Altar)
    public static final Block BLOOD_ALTAR = new com.qiamao.blood.block.BlockBloodAltar()
            .setUnlocalizedName("blood_altar")
            .setRegistryName(BloodMod.MODID, "blood_altar");

    // 血液淬炼台
    public static final Block BLOOD_TEMPERING_ALTAR = new com.qiamao.blood.block.BlockBloodTemperingAltar("blood_tempering_altar")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血螨脑控的头颅
    public static final Block PARASITIC_STEVE_HEAD = new com.qiamao.blood.block.BlockModHead("parasitic_steve_head");
    
    // 传教士的头颅
    public static final Block PREACHER_HEAD = new com.qiamao.blood.block.BlockModHead("preacher_head");

    // 血腥活板门
    public static final Block BLOOD_TRAPDOOR = new com.qiamao.blood.block.BlockBloodTrapDoor("blood_trapdoor");

    // 血腥门
    public static final com.qiamao.blood.block.BlockBloodDoor BLOOD_DOOR = new com.qiamao.blood.block.BlockBloodDoor("blood_door");

    // 血腥栅栏
    public static final Block BLOOD_FENCE = new com.qiamao.blood.block.BlockBloodFence("blood_fence");

    // 血腥栅栏门
    public static final Block BLOOD_FENCE_GATE = new com.qiamao.blood.block.BlockBloodFenceGate("blood_fence_gate");

    /**
     * 监听方块注册事件
     * Forge 会在合适的时机自动调用此方法来注册方块
     */
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BLOCK_BLOOD, BLOOD_SEEKER_FLESH, BLOOD_LOG, BLOOD_PLANKS, BLOOD_LEAVES, BLOOD_SLAB_HALF, BLOOD_SLAB_DOUBLE, BLOOD_STAIRS, FLESH_CHUNK, BLOOD_BLOCK, BLOOD_SAPLING, BLOOD_ALTAR, BLOOD_TEMPERING_ALTAR, PARASITIC_STEVE_HEAD, PREACHER_HEAD, BLOOD_TRAPDOOR, BLOOD_DOOR, BLOOD_FENCE, BLOOD_FENCE_GATE);
        
        // 注册 TileEntity
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.qiamao.blood.block.tileentity.TileEntityBloodAltar.class, new net.minecraft.util.ResourceLocation(BloodMod.MODID, "blood_tempering_altar"));
    }
}
