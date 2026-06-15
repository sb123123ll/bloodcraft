package com.qiamao.blood.world.biome;

import com.qiamao.blood.init.ModBlocks;
import com.qiamao.blood.world.WorldGenBloodTree;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * 血液翻腾之地生物群系
 * 地面由血块组成，充满血液的河流和湖泊
 */
public class BiomeBloodSurgingLand extends Biome {

    // 血腥树木生成器
    private static final WorldGenAbstractTree BLOOD_TREE = new WorldGenBloodTree(false);

    public BiomeBloodSurgingLand() {
        super(new BiomeProperties("blood_surging_land")
            .setTemperature(0.85F)
            .setRainfall(0.85F)
            .setBaseHeight(0.255F)
            .setHeightVariation(0.13F)
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
     * 重写地形生成逻辑，实现地表方块随机朝向
     */
    @Override
    @SuppressWarnings("null")
    public void genTerrainBlocks(@Nonnull net.minecraft.world.World worldIn, @Nonnull Random rand, @Nonnull net.minecraft.world.chunk.ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        // 在生成每一列方块（x, z 坐标）之前，随机设置 topBlock 和 fillerBlock 的水平朝向
        // 这样可以确保地表的肉块（Flesh Chunk）在水平方向上随机旋转，看起来更自然
        net.minecraft.util.EnumFacing topFacing = net.minecraft.util.EnumFacing.Plane.HORIZONTAL.random(rand);
        net.minecraft.util.EnumFacing fillerFacing = net.minecraft.util.EnumFacing.Plane.HORIZONTAL.random(rand);

        this.topBlock = ModBlocks.FLESH_CHUNK.getDefaultState().withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, topFacing);
        this.fillerBlock = ModBlocks.FLESH_CHUNK.getDefaultState().withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, fillerFacing);

        // 调用父类方法执行实际的方块填充逻辑
        super.genTerrainBlocks(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
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
        return BLOOD_TREE;
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
