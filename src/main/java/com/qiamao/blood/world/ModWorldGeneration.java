package com.qiamao.blood.world;

import com.qiamao.blood.init.ModBiomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class ModWorldGeneration implements IWorldGenerator {

    private final WorldGenBloodAltar bloodAltar = new WorldGenBloodAltar();
    private final WorldGenBloodTree bloodTree = new WorldGenBloodTree(false);
    private final WorldGenBloodStructure bloodStructure = new WorldGenBloodStructure("blood_big", "blood_big_2", 32);
    private final WorldGenFleshCluster fleshCluster = new WorldGenFleshCluster();
    private final WorldGenMissionaryHouse missionaryHouse = new WorldGenMissionaryHouse();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // 只在主世界 (维度 0) 生成
        if (world.provider.getDimension() == 0) {
            generateSurface(random, chunkX, chunkZ, world);
        }
    }

    private void generateSurface(Random random, int chunkX, int chunkZ, World world) {
        // 超平坦世界不生成任何结构
        if (world.getWorldType() == net.minecraft.world.WorldType.FLAT) {
            return;
        }

        // 使用区块位置作为种子的基础，确保每个区域的选择是确定性的
        // 这种方法类似于原版村庄/神庙的生成逻辑，通过将坐标划分为更大的网格来避免冲突
        // 网格大小设为 14x14
        int gridX = chunkX / 14; // 每 14x14 个区块为一个大网格单元
        int gridZ = chunkZ / 14;
        Random gridRandom = new Random(world.getSeed() + (long)gridX * 31278612L + (long)gridZ * 43981247L);
        
        // 每个网格单元只选择一个位置尝试生成结构
        // 使用 nextInt(14) 确保整个 14x14 网格的全部位置均可被选中，避免死区
        int selectedChunkX = (gridX * 14) + gridRandom.nextInt(14); 
        int selectedChunkZ = (gridZ * 14) + gridRandom.nextInt(14);

        // 如果当前区块正是被选中的区块，则尝试生成结构
        // 为避免因生物群系/地形检查导致频繁失败，按随机顺序尝试3种结构
        // 只要有一种成功就停止（血巢无群系限制，兜底保证）
        if (chunkX == selectedChunkX && chunkZ == selectedChunkZ) {
            int x = chunkX * 16 + 8;
            int z = chunkZ * 16 + 8;

            // 随机打乱尝试顺序，避免同一类型总是优先
            int[] typeOrder = {0, 1, 2};
            // Fisher-Yates 洗牌
            for (int i = 2; i > 0; i--) {
                int j = gridRandom.nextInt(i + 1);
                int tmp = typeOrder[i];
                typeOrder[i] = typeOrder[j];
                typeOrder[j] = tmp;
            }

            for (int structureType : typeOrder) {
                if (structureType == 0) {
                    // 生成母体血巢（无群系限制，y=30地下，几乎必定成功）
                    BlockPos pos = new BlockPos(x, 30, z);
                    if (bloodStructure.generate(world, gridRandom, pos)) {
                        break;
                    }
                } else if (structureType == 1) {
                    // 生成血祭坛
                    BlockPos pos = world.getHeight(new BlockPos(x, 0, z));
                    Biome biome = world.getBiome(pos);

                    boolean canGenerate = (biome == ModBiomes.BLOOD_SURGING_LAND) ||
                        (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY) &&
                         !BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD));

                    if (canGenerate && bloodAltar.generate(world, gridRandom, pos)) {
                        break;
                    }
                } else if (structureType == 2) {
                    // 生成传教士屋
                    BlockPos pos = world.getHeight(new BlockPos(x, 0, z));
                    Biome biome = world.getBiome(pos);
                    
                    boolean canGenerate = BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS) ||
                                          BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST);
                    
                    if (canGenerate && missionaryHouse.generate(world, gridRandom, pos)) {
                        break;
                    }
                }
            }
        }

        // 生成血腥树
        if (random.nextInt(3) == 0) {
            int treeCount = 1 + random.nextInt(3);
            for (int i = 0; i < treeCount; i++) {
                int x = chunkX * 16 + 8 + random.nextInt(5);
                int z = chunkZ * 16 + 8 + random.nextInt(5);
                BlockPos pos = world.getHeight(new BlockPos(x, 0, z));
                Biome biome = world.getBiome(pos);

                if (biome == ModBiomes.BLOOD_SURGING_LAND) {
                    if (random.nextInt(10) == 0) {
                        new com.qiamao.blood.world.WorldGenBigBloodTree(false).generate(world, random, pos);
                    } else {
                        bloodTree.generate(world, random, pos);
                    }
                }
            }
        }

        // 在地下尝试生成肉块簇，覆盖矿洞、废弃矿井、海底等
        // 为了在曲折的矿洞和水下也能有更多生成，我们提高尝试次数，并扩大高度范围（包含海底）
        if (random.nextInt(100) < 95) { // 95%的区块都会尝试生成
            for (int i = 0; i < 10; i++) { // 增加单区块尝试次数，提高在矿洞中的命中率
                int x = chunkX * 16 + 8 + random.nextInt(5);
                // 海底一般在Y=40~60左右，矿洞可以从Y=5一直到Y=60
                int y = 5 + random.nextInt(55); // 限制在 5 到 60 之间
                int z = chunkZ * 16 + 8 + random.nextInt(5);
                
                fleshCluster.generate(world, random, new BlockPos(x, y, z));
            }
        }
    }
}
