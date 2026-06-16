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
     * 检查周围是否有血腥原木或原版原木 (使用连通性寻路探测)
     */
    private boolean shouldBreakLeaves(World worldIn, BlockPos pos) {
        // 按照需求：向上8格，向下2格，四面8格
        int rangeX = 8;
        int rangeYUp = 8;
        int rangeYDown = 2;
        int rangeZ = 8;

        int sizeX = rangeX * 2 + 1;
        int sizeY = rangeYUp + rangeYDown + 1;
        int sizeZ = rangeZ * 2 + 1;

        // 3D 数组记录是否访问过，避免死循环和重复计算
        boolean[][][] visited = new boolean[sizeX][sizeY][sizeZ];
        
        // 简易队列用于广度优先搜索 (BFS)
        BlockPos[] queue = new BlockPos[sizeX * sizeY * sizeZ];
        int head = 0;
        int tail = 0;

        queue[tail++] = pos;
        visited[rangeX][rangeYDown][rangeZ] = true;

        while (head < tail) {
            BlockPos current = queue[head++];
            net.minecraft.block.Block block = worldIn.getBlockState(current).getBlock();

            // 1. 只要顺着树叶蔓延找到了原木，说明它仍与某棵树"物理相连"，不应腐烂
            if (block == com.qiamao.blood.init.ModBlocks.BLOOD_LOG ||
                block == net.minecraft.init.Blocks.LOG ||
                block == net.minecraft.init.Blocks.LOG2) {
                return true;
            }

            // 2. 如果当前方块是起始树叶或相连的树叶，则继续向它周围 6 个方向扩散寻找
            if (current.equals(pos) || block == this) {
                for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.values()) {
                    int nx = current.getX() + facing.getFrontOffsetX();
                    int ny = current.getY() + facing.getFrontOffsetY();
                    int nz = current.getZ() + facing.getFrontOffsetZ();

                    int dx = nx - pos.getX();
                    int dy = ny - pos.getY();
                    int dz = nz - pos.getZ();

                    // 确保扩散没有超出我们设定的边界 (上8, 下2, 四面8)
                    if (Math.abs(dx) <= rangeX && Math.abs(dz) <= rangeZ && dy <= rangeYUp && dy >= -rangeYDown) {
                        int arrX = dx + rangeX;
                        int arrY = dy + rangeYDown;
                        int arrZ = dz + rangeZ;

                        if (!visited[arrX][arrY][arrZ]) {
                            visited[arrX][arrY][arrZ] = true;
                            queue[tail++] = new BlockPos(nx, ny, nz);
                        }
                    }
                }
            }
        }
        
        // 遍历了范围内所有能连通的树叶，都没找到原木，说明被彻底砍断了
        return false;
    }
}
