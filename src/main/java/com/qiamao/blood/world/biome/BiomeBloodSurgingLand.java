package com.qiamao.blood.world.biome;

import com.qiamao.blood.block.BlockFleshChunk;
import com.qiamao.blood.init.ModBlocks;
import com.qiamao.blood.world.WorldGenBloodTree;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * 血液翻腾之地生物群系
 * 地面由血块组成，充满血液的河流和湖泊
 * 地形生成时肉块覆盖深度加倍，确保斜坡侧面不裸露石头
 */
public class BiomeBloodSurgingLand extends Biome {

    // 血腥树木生成器
    private static final WorldGenAbstractTree BLOOD_TREE = new WorldGenBloodTree(false);
    private static final WorldGenAbstractTree BIG_BLOOD_TREE = new com.qiamao.blood.world.WorldGenBigBloodTree(false);
    
    // 底部基岩和深层的原版石头
    private static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();

    public BiomeBloodSurgingLand() {
        super(new BiomeProperties("blood_surging_land")
            .setTemperature(0.85F)
            .setRainfall(0.85F)
            .setBaseHeight(0.255F)
            .setHeightVariation(0.25F)
            .setWaterColor(0x8B0000));  // 深红色，与血液一致

        // 设置顶部和填充方块为肉块
        this.topBlock = ModBlocks.FLESH_CHUNK.getDefaultState();
        this.fillerBlock = ModBlocks.FLESH_CHUNK.getDefaultState();

        // 设置树木密集度与橡树群系相同（原版 Forest 是 10）
        this.decorator.treesPerChunk = 10;
        this.decorator.grassPerChunk = 0;
        this.decorator.flowersPerChunk = 0;
        this.decorator.deadBushPerChunk = 0;

        // 矿洞照常生成（不修改 cavesPerChunk 和 ravinesPerChunk）

        // 清除原版生物生成
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();

        // 添加模组生物到spawnableMonsterList，使刷怪蛋能够正常使用
        // 注意：自然生成仍由主类 BloodMod 控制，这里只允许刷怪蛋放置
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(
            com.qiamao.blood.entity.EntityBloodSeeker.class, 1, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(
            com.qiamao.blood.entity.EntityBloodEndermite.class, 1, 1, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(
            com.qiamao.blood.entity.EntityParasiticSteve.class, 1, 1, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(
            com.qiamao.blood.entity.EntityBloodMother.class, 1, 1, 1));
    }

    /**
     * 重写地形生成逻辑，大幅增加表层肉块覆盖深度
     * 原版使用 noiseVal/3+3 作为表层深度，斜坡侧面容易裸露石头
     * 这里使用 noiseVal/3+8+rand*3，表层肉块深度约为原来的2-3倍
     * 同时保留水平随机朝向逻辑
     */
    @Override
    @SuppressWarnings("null")
    public void genTerrainBlocks(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int seaLevel = worldIn.getSeaLevel();
        int chunkX = x & 15;
        int chunkZ = z & 15;

        // 随机朝向的肉块状态
        EnumFacing topFacing = EnumFacing.Plane.HORIZONTAL.random(rand);
        EnumFacing fillerFacing = EnumFacing.Plane.HORIZONTAL.random(rand);
        IBlockState topBlockState = ModBlocks.FLESH_CHUNK.getDefaultState().withProperty(BlockFleshChunk.FACING, topFacing);
        IBlockState fillerBlockState = ModBlocks.FLESH_CHUNK.getDefaultState().withProperty(BlockFleshChunk.FACING, fillerFacing);

        // 表层深度：4-7格深（包含随机变化），覆盖斜坡侧面裸露石头的效果适度
        int maxSurfaceDepth = 4 + rand.nextInt(4);

        int surfaceDepth = -1;

        for (int y = 255; y >= 0; --y) {
            if (y <= rand.nextInt(5)) {
                chunkPrimerIn.setBlockState(chunkX, y, chunkZ, BEDROCK);
            } else {
                IBlockState state = chunkPrimerIn.getBlockState(chunkX, y, chunkZ);
                if (state.getBlock() != null && state.getMaterial() != Material.AIR) {
                    if (state.getBlock() == Blocks.STONE) {
                        if (surfaceDepth == -1) {
                            // 表层起始
                            surfaceDepth = maxSurfaceDepth;
                            if (y >= seaLevel - 1) {
                                chunkPrimerIn.setBlockState(chunkX, y, chunkZ, topBlockState);
                            } else if (y < seaLevel - 1 - maxSurfaceDepth) {
                                // 超过表层深度的下层，恢复为石头
                                chunkPrimerIn.setBlockState(chunkX, y, chunkZ, STONE);
                            } else {
                                chunkPrimerIn.setBlockState(chunkX, y, chunkZ, fillerBlockState);
                            }
                        } else if (surfaceDepth > 0) {
                            --surfaceDepth;
                            chunkPrimerIn.setBlockState(chunkX, y, chunkZ, fillerBlockState);
                        }
                    }
                } else {
                    // 遇到空气（洞穴），重置表层深度计数器
                    surfaceDepth = -1;
                }
            }
        }
    }

    /**
     * 获取天空颜色 - 淡红色
     */
    @SideOnly(Side.CLIENT)
    @Override
    public int getSkyColorByTemp(float currentTemperature) {
        return 0xFF6666; // 淡红色天空
    }

    /**
     * 获取草的颜色（虽然此群系不生成草）
     */
    @SideOnly(Side.CLIENT)
    @Override
    public int getGrassColorAtPos(@Nonnull BlockPos pos) {
        return 0x990000;
    }

    /**
     * 获取树叶颜色
     */
    @SideOnly(Side.CLIENT)
    @Override
    public int getFoliageColorAtPos(@Nonnull BlockPos pos) {
        return 0x880000;
    }

    /**
     * 获取随机树木生成器
     */
    @Override
    public WorldGenAbstractTree getRandomTreeFeature(@Nonnull Random rand) {
        // 自然生成大变种概率和原版橡树大变种一致（原版通常是 10% 概率生成大型橡树）
        return (rand.nextInt(10) == 0) ? BIG_BLOOD_TREE : BLOOD_TREE;
    }

    /**
     * 是否可以下雨
     */
    @Override
    public boolean canRain() {
        return true; // 允许下雨，但客户端会渲染成淡红色
    }

    /**
     * 是否是高湿度群系
     */
    @Override
    public boolean isHighHumidity() {
        return true; // 高湿度，配合0.85降雨量
    }

    /**
     * 获取水颜色
     */
    @Override
    public int getWaterColorMultiplier() {
        return 0x8B0000; // 深红色
    }

    // 使用原版地形生成，topBlock 和 fillerBlock 已在构造函数中设置为血块
    // 不再强制替换深层地下方块
}
