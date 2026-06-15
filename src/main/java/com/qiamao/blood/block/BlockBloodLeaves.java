package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockBloodLeaves extends Block implements IShearable {

    @SuppressWarnings("null")
    public BlockBloodLeaves() {
        super(Material.LEAVES);
        this.setHardness(0.2F); // 和原版树叶一样的硬度
        this.setLightOpacity(1); // 允许一部分光线穿透
        this.setSoundType(SoundType.PLANT); // 树叶音效
        this.setTickRandomly(true); // 启用随机更新，用于腐烂机制
        this.setRegistryName(com.qiamao.blood.BloodMod.MODID, "blood_leaves");
        this.setUnlocalizedName("blood_leaves");
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
    }

    /**
     * 允许透明渲染（半透明镂空效果）
     */
    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * 相邻的透明方块面是否需要渲染
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    // --- IShearable 接口实现，使得方块可以用剪刀剪下 ---

    @Override
    public boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
        return true; // 允许用剪刀剪
    }

    @Override
    @Nonnull
    public List<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        // 被剪刀剪掉时，掉落自身（血腥树叶）
        return Collections.singletonList(new ItemStack(this));
    }

    // --- 正常挖掘时的掉落逻辑 ---

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        // 25% 概率掉流血腥树苗
        if (rand.nextFloat() < 0.25F) {
            return Item.getItemFromBlock(com.qiamao.blood.init.ModBlocks.BLOOD_SAPLING);
        }
        return null;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    /**
     * 树叶腐烂机制 - 检查周围是否有血腥原木，没有则逐渐腐烂
     */
    @Override
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        if (!worldIn.isRemote) {
            if (!this.shouldBreakLeaves(worldIn, pos)) {
                // 腐烂前有 25% 概率掉流血腥树苗
                if (rand.nextFloat() < 0.25F) {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }
                worldIn.setBlockToAir(pos);
            }
        }
    }

    /**
     * 检查周围是否有血腥原木或原版原木
     */
    private boolean shouldBreakLeaves(World worldIn, BlockPos pos) {
        for (int i = 1; i <= 6; ++i) {
            BlockPos blockpos = pos.up(i);
            if (worldIn.getBlockState(blockpos).getBlock() == com.qiamao.blood.init.ModBlocks.BLOOD_LOG ||
                worldIn.getBlockState(blockpos).getBlock() == Blocks.LOG ||
                worldIn.getBlockState(blockpos).getBlock() == Blocks.LOG2) {
                return true;
            }
        }

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            for (int i = 1; i <= 4; ++i) {
                BlockPos blockpos1 = pos.offset(enumfacing, i);
                if (worldIn.getBlockState(blockpos1).getBlock() == com.qiamao.blood.init.ModBlocks.BLOOD_LOG ||
                    worldIn.getBlockState(blockpos1).getBlock() == Blocks.LOG ||
                    worldIn.getBlockState(blockpos1).getBlock() == Blocks.LOG2) {
                    return true;
                }
            }
        }

        return false;
    }
}
