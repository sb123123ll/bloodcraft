package com.qiamao.blood.world;

import com.qiamao.blood.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

/**
 * 血腥树生成器
 * 生成由血腥原木和血腥树叶组成的树
 */
public class WorldGenBloodTree extends WorldGenAbstractTree {

    // 树干和树叶的方块状态，原木显式设置为Y轴朝上（直立）
    private static final IBlockState LOG_STATE = ModBlocks.BLOOD_LOG.getDefaultState()
            .withProperty(net.minecraft.block.BlockRotatedPillar.AXIS, EnumFacing.Axis.Y);
    private static final IBlockState LEAVES_STATE = ModBlocks.BLOOD_LEAVES.getDefaultState();

    public WorldGenBloodTree(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        // 随机树高：5 到 8 格
        int height = 5 + rand.nextInt(4);

        // 检查是否可以生成：确保树根和上方有足够空间
        if (!canGenerate(worldIn, position, height)) {
            return false;
        }

        // 生成树干
        generateTrunk(worldIn, position, height);

        // 生成树冠
        generateLeaves(worldIn, position, height, rand);

        return true;
    }

    /**
     * 检查是否可以生成树
     */
    private boolean canGenerate(World worldIn, BlockPos pos, int height) {
        // 检查树根下方是否是固体方块（泥土、草地、石头等）
        BlockPos groundPos = pos.down();
        if (!worldIn.getBlockState(groundPos).isOpaqueCube()) {
            return false;
        }

        // 检查树干位置是否可替换（空气或可替换方块）
        for (int y = 0; y < height; y++) {
            BlockPos trunkPos = pos.up(y);
            if (!canReplace(worldIn.getBlockState(trunkPos).getBlock())) {
                return false;
            }
        }

        // 检查树冠空间（树干上方3-4格）
        int leavesRadius = 2;
        for (int y = height - 3; y <= height + 1; y++) {
            for (int x = -leavesRadius; x <= leavesRadius; x++) {
                for (int z = -leavesRadius; z <= leavesRadius; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    if (!canReplace(worldIn.getBlockState(checkPos).getBlock())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 生成树干
     */
    private void generateTrunk(World worldIn, BlockPos pos, int height) {
        for (int y = 0; y < height; y++) {
            BlockPos trunkPos = pos.up(y);
            this.setBlockAndNotifyAdequately(worldIn, trunkPos, LOG_STATE);
        }
    }

    /**
     * 生成树冠（标准的 Minecraft 橡树树冠形状）
     */
    private void generateLeaves(World worldIn, BlockPos pos, int height, Random rand) {
        // 橡树的树冠通常从树顶往下 3 格开始，一直长到树顶往上 1 格
        // 例如：树高为 5（树干是 0,1,2,3,4），树冠范围是 Y = 2 到 5
        for (int yOffset = height - 3; yOffset <= height; ++yOffset) {
            // yLevel 是相对于树顶的相对高度。
            // 例如 height=5 时，yOffset 为 2(yLevel=3), 3(yLevel=2), 4(yLevel=1), 5(yLevel=0)
            int yLevel = yOffset - (height);
            // 计算当前层的树叶半径
            // 最顶层 (yLevel=0) 和上一层 (yLevel=-1) 半径是 1
            // 下面的层 (yLevel=-2, -3) 半径是 2
            int radius = 1 - yLevel / 2;

            for (int xOffset = pos.getX() - radius; xOffset <= pos.getX() + radius; ++xOffset) {
                int x = xOffset - pos.getX();

                for (int zOffset = pos.getZ() - radius; zOffset <= pos.getZ() + radius; ++zOffset) {
                    int z = zOffset - pos.getZ();

                    // 原版橡树砍角逻辑：
                    // 当处于最外圈（|x| == radius 且 |z| == radius）时：
                    // 1. 如果这是最顶层或次顶层 (radius > 0 且 yLevel == 0，虽然橡树这里 yLevel 通常是 0)
                    // 2. 如果这是一个四角，并且随机数为 0（50%概率砍角），且不是最底层（yLevel != 0）
                    if (Math.abs(x) == radius && Math.abs(z) == radius && (radius > 0 && yLevel == 0 || rand.nextInt(2) == 0)) {
                        continue; // 跳过这个角的树叶，让树冠看起来更圆润
                    }

                    BlockPos leafPos = new BlockPos(xOffset, pos.getY() + yOffset, zOffset);
                    // 确保不要覆盖树干本身
                    if (worldIn.getBlockState(leafPos).getBlock() != ModBlocks.BLOOD_LOG) {
                        placeLeaves(worldIn, leafPos);
                    }
                }
            }
        }
    }

    /**
     * 放置树叶方块
     */
    private void placeLeaves(World worldIn, BlockPos pos) {
        if (canReplace(worldIn.getBlockState(pos).getBlock())) {
            this.setBlockAndNotifyAdequately(worldIn, pos, LEAVES_STATE);
        }
    }

    /**
     * 检查方块是否可替换
     */
    private boolean canReplace(net.minecraft.block.Block block) {
        return block == net.minecraft.init.Blocks.AIR ||
               block == net.minecraft.init.Blocks.TALLGRASS ||
               block == net.minecraft.init.Blocks.DEADBUSH ||
               block == net.minecraft.init.Blocks.SAPLING;
    }
}
