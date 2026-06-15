package com.qiamao.blood.world.biome;

import com.google.common.collect.Lists;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;

import java.util.List;
import java.util.Random;

/**
 * 自定义BiomeProvider，减小血液翻腾之地群系的生成面积
 * 通过增加额外的Zoom层来减小群系斑块大小
 */
public class BiomeProviderBloodSurgingLand extends BiomeProvider {

    private static final Biome FALLBACK_BIOME = Biomes.PLAINS;

    private GenLayer genBiomes;
    private GenLayer biomeIndexLayer;
    private BiomeCache biomeCache;
    private List<Biome> biomesToSpawn;

    public BiomeProviderBloodSurgingLand(World world) {
        this.biomeCache = new BiomeCache(this);
        this.biomesToSpawn = Lists.newArrayList();

        // 获取原始的GenLayer（传入null作为ChunkGeneratorSettings，使用默认设置）
        GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(world.getSeed(), world.getWorldType(), null);
        
        // 添加额外的Zoom层来减小群系大小
        // 每添加一层Zoom，群系大小约减小一半
        GenLayer zoomLayer = agenlayer[0];
        
        // 额外添加1层Zoom，使群系大小约为原来的一半
        zoomLayer = new GenLayerZoom(1001L, zoomLayer);
        
        this.genBiomes = zoomLayer;
        this.biomeIndexLayer = new GenLayerVoronoiZoom(10L, zoomLayer);
        this.genBiomes.initWorldGenSeed(world.getSeed());
        this.biomeIndexLayer.initWorldGenSeed(world.getSeed());
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.getBiome(pos, FALLBACK_BIOME);
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        Biome fallback = defaultBiome != null ? defaultBiome : FALLBACK_BIOME;
        Biome biome = this.biomeCache.getBiome(pos.getX(), pos.getZ(), fallback);
        return biome != null ? biome : fallback;
    }

    @Override
    public Biome[] getBiomes(Biome[] listToReuse, int x, int z, int width, int depth) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * depth) {
            listToReuse = new Biome[width * depth];
        }

        int[] aint = this.biomeIndexLayer.getInts(x, z, width, depth);

        for (int i = 0; i < width * depth; ++i) {
            listToReuse[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
        }

        return listToReuse;
    }

    @Override
    public Biome[] getBiomes(Biome[] listToReuse, int x, int z, int width, int depth, boolean cacheFlag) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * depth) {
            listToReuse = new Biome[width * depth];
        }

        if (cacheFlag && width == 16 && depth == 16 && (x & 15) == 0 && (z & 15) == 0) {
            Biome[] abiome = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(abiome, 0, listToReuse, 0, width * depth);
            return listToReuse;
        } else {
            int[] aint = this.biomeIndexLayer.getInts(x, z, width, depth);

            for (int i = 0; i < width * depth; ++i) {
                listToReuse[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
            }

            return listToReuse;
        }
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        int[] aint = this.genBiomes.getInts(x, z, width, height);

        for (int i = 0; i < width * height; ++i) {
            biomes[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
        }

        return biomes;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        IntCache.resetIntCache();
        int i = x - radius >> 2;
        int j = z - radius >> 2;
        int k = x + radius >> 2;
        int l = z + radius >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        int[] aint = this.genBiomes.getInts(i, j, i1, j1);

        for (int k1 = 0; k1 < i1 * j1; ++k1) {
            Biome biome = Biome.getBiome(aint[k1]);

            if (!allowed.contains(biome)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        IntCache.resetIntCache();
        int i = x - range >> 2;
        int j = z - range >> 2;
        int k = x + range >> 2;
        int l = z + range >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        int[] aint = this.genBiomes.getInts(i, j, i1, j1);
        BlockPos blockpos = null;
        int k1 = 0;

        for (int l1 = 0; l1 < i1 * j1; ++l1) {
            int i2 = i + l1 % i1 << 2;
            int j2 = j + l1 / i1 << 2;
            Biome biome = Biome.getBiome(aint[l1]);

            if (biomes.contains(biome) && (blockpos == null || random.nextInt(k1 + 1) == 0)) {
                blockpos = new BlockPos(i2, 0, j2);
                ++k1;
            }
        }

        return blockpos;
    }

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }

    @Override
    public boolean isFixedBiome() {
        return false;
    }
}
