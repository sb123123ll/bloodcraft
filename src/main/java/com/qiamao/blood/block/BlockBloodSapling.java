package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.world.WorldGenBloodTree;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.init.Blocks;

/**
 * 血腥树树苗
 * 可用骨粉催熟，可作燃料
 */
public class BlockBloodSapling extends BlockBush implements IGrowable {

    // 生长阶段属性：0 为初始，1 为准备生长
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);

    public BlockBloodSapling() {
        super(Material.PLANTS);
        this.setRegistryName(BloodMod.MODID, "blood_sapling");
        this.setUnlocalizedName("blood_sapling");
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState().withProperty(STAGE, 0));
    }

    /**
     * 检测下方方块是否支持种植
     * 除了原版的草地/泥土，额外支持肉块
     */
    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == com.qiamao.blood.init.ModBlocks.FLESH_CHUNK || 
               state.getBlock() == Blocks.GRASS || 
               state.getBlock() == Blocks.DIRT || 
               state.getBlock() == Blocks.FARMLAND;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);

            // 随机刻自动生长逻辑（光照足够时）
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(worldIn, rand, pos, state);
            }
        }
    }

    @Override
    public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return (double)worldIn.rand.nextFloat() < 0.45D;
    }

    @Override
    public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (state.getValue(STAGE) == 0) {
            // 从阶段 0 升级到阶段 1
            worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
        } else {
            // 已经是阶段 1，尝试生成树木
            generateTree(worldIn, rand, pos, state);
        }
    }

    private void generateTree(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        // 先移除树苗
        worldIn.setBlockToAir(pos);
        
        WorldGenBloodTree treeGen = new WorldGenBloodTree(true);
        
        if (!treeGen.generate(worldIn, rand, pos)) {
            // 生成失败，放回阶段 1 的树苗
            worldIn.setBlockState(pos, state, 4);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(STAGE, meta & 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }
}
