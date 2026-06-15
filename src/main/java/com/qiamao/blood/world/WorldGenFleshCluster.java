package com.qiamao.blood.world;

import com.qiamao.blood.init.ModBlocks;
import com.qiamao.blood.block.BlockFleshChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenFleshCluster extends WorldGenerator {

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        
        int minX = chunkX * 16;
        int maxX = chunkX * 16 + 15;
        int minZ = chunkZ * 16;
        int maxZ = chunkZ * 16 + 15;

        if (worldIn.isAirBlock(position) || worldIn.getBlockState(position).getMaterial() == net.minecraft.block.material.Material.WATER) {
            EnumFacing attachedFace = null;
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos offsetPos = position.offset(facing);
                IBlockState state = worldIn.getBlockState(offsetPos);
                if (state.getBlock() == Blocks.STONE || state.getBlock() == Blocks.DIRT || 
                    state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.SAND || 
                    state.getBlock() == Blocks.COBBLESTONE) {
                    attachedFace = facing;
                    break;
                }
            }

            if (attachedFace != null) {
                // 肉块方向设置（必须是水平的，若是附着在顶/底，则随机水平方向）
                EnumFacing blockFacing = attachedFace.getAxis().isHorizontal() ? attachedFace.getOpposite() : EnumFacing.Plane.HORIZONTAL.random(rand);
                IBlockState fleshState = ModBlocks.FLESH_CHUNK.getDefaultState().withProperty(BlockFleshChunk.FACING, blockFacing);

                // 表面扩散大小：增加以生成更大的面积
                int surfaceSize = 10 + rand.nextInt(20); // 10 到 30 个方块的面积
                // 深入厚度：通常较薄，偶尔会有较厚的部分
                int thickness = 2 + rand.nextInt(2); // 2 到 3 格厚

                generateCluster(worldIn, rand, position.offset(attachedFace), attachedFace, surfaceSize, thickness, fleshState, minX, maxX, minZ, maxZ);
                return true;
            }
        }
        return false;
    }

    private boolean isReplaceable(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.STONE || 
               state.getBlock() == Blocks.DIRT || 
               state.getBlock() == Blocks.GRAVEL ||
               state.getBlock() == Blocks.SAND ||
               state.getBlock() == Blocks.COBBLESTONE ||
               world.isAirBlock(pos) ||
               state.getMaterial() == net.minecraft.block.material.Material.WATER;
    }

    private void generateCluster(World world, Random rand, BlockPos startPos, EnumFacing growDir, int surfaceSize, int maxThickness, IBlockState fleshState, int minX, int maxX, int minZ, int maxZ) {
        int blocksPlaced = 0;
        
        // 我们需要生成一个大面积、贴合地形的肉块。
        // 横向半径极大，深度保持原样（浅）
        double lateralRadius = Math.sqrt(surfaceSize / Math.PI) * (1.8 + rand.nextDouble() * 1.0);
        double depthRadius = maxThickness * (0.8 + rand.nextDouble() * 0.4);
        
        int rLat = (int) Math.ceil(lateralRadius);
        int rDep = (int) Math.ceil(depthRadius);
        
        // 允许向外（空气中）突出一点点，形成贴合感，但绝不鼓起
        int maxOutwardProtrusion = 1; // 最多只往空气里突1格
        
        for (int x = -rLat; x <= rLat; x++) {
            for (int y = -rLat; y <= rLat; y++) {
                for (int z = -rLat; z <= rLat; z++) {
                    BlockPos currentPos = startPos.add(x, y, z);
                    
                    if (currentPos.getX() < minX || currentPos.getX() > maxX || currentPos.getZ() < minZ || currentPos.getZ() > maxZ) {
                        continue;
                    }
                    
                    int dx = currentPos.getX() - startPos.getX();
                    int dy = currentPos.getY() - startPos.getY();
                    int dz = currentPos.getZ() - startPos.getZ();
                    
                    int depth = 0;
                    double lateralDistSq = 0;
                    
                    if (growDir.getAxis() == EnumFacing.Axis.X) {
                        depth = -dx * growDir.getFrontOffsetX();
                        lateralDistSq = dy * dy + dz * dz;
                    } else if (growDir.getAxis() == EnumFacing.Axis.Y) {
                        depth = -dy * growDir.getFrontOffsetY();
                        lateralDistSq = dx * dx + dz * dz;
                    } else if (growDir.getAxis() == EnumFacing.Axis.Z) {
                        depth = -dz * growDir.getFrontOffsetZ();
                        lateralDistSq = dx * dx + dy * dy;
                    }
                    
                    // depth > 0 表示在墙体内
                    // depth < 0 表示在空气/水中（即突出的部分）
                    // 严格限制厚度，不让它鼓起来
                    if (depth >= -maxOutwardProtrusion && depth <= maxThickness) {
                        // 距离中心的距离（标准化到0-1）
                        double distRatio = Math.sqrt(lateralDistSq) / lateralRadius;
                        
                        // 引入随机噪声来制造非常不规则的边缘，而不是规则的圆
                        double noise = (rand.nextDouble() - 0.5) * 1.5; 
                        
                        // 确保它是扁平的：向外的部分迅速衰减，不让它形成球体
                        double allowedDepthInward = maxThickness * (1.0 - distRatio * 0.5); // 内部可以深一点
                        double allowedDepthOutward = -maxOutwardProtrusion * (1.0 - distRatio); // 外部极薄
                        
                        if (depth <= allowedDepthInward + noise && depth >= allowedDepthOutward - noise) {
                            // 另外一个条件：确保连通性，且边缘支离破碎
                            double threshold = 1.0 - distRatio + (rand.nextDouble() * 0.5);
                            if (threshold > 0.4) {
                                if (depth < 0) {
                                    if (world.isAirBlock(currentPos) || world.getBlockState(currentPos).getMaterial() == net.minecraft.block.material.Material.WATER) {
                                        world.setBlockState(currentPos, fleshState, 2);
                                        blocksPlaced++;
                                    }
                                } else {
                                    if (isReplaceable(world, currentPos)) {
                                        world.setBlockState(currentPos, fleshState, 2);
                                        blocksPlaced++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
