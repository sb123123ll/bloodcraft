package com.qiamao.blood.world;

import javax.annotation.Nonnull;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import com.qiamao.blood.init.ModBlocks;

import java.util.Random;

/**
 * 母体血巢生成器
 * 加载 NBT 结构文件并拼接生成
 * 在地下Y=30高度生成，清理空间并留出入口
 */
public class WorldGenBloodStructure extends WorldGenerator {

    private final String structureName1;
    private final String structureName2;
    private final int offsetX;
    private static final int GENERATION_HEIGHT = 30; // 固定生成高度

    /**
     * @param structureName1 第一个结构名称（不含.nbt扩展名）
     * @param structureName2 第二个结构名称（不含.nbt扩展名）
     * @param offsetX 第二个结构相对于第一个的X轴偏移量
     */
    public WorldGenBloodStructure(String structureName1, String structureName2, int offsetX) {
        this.structureName1 = structureName1;
        this.structureName2 = structureName2;
        this.offsetX = offsetX;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        MinecraftServer server = worldIn.getMinecraftServer();
        if (server == null) {
            return false;
        }

        // 固定在Y=30生成
        BlockPos generatePos = new BlockPos(position.getX(), GENERATION_HEIGHT, position.getZ());

        // 1.12.2 中从服务器获取 TemplateManager
        TemplateManager templateManager = server.getWorld(0).getStructureTemplateManager();

        // 加载第一个结构
        Template template1 = templateManager.getTemplate(server, new ResourceLocation("blood", structureName1));
        if (template1 == null) {
            System.err.println("无法加载结构: " + structureName1);
            return false;
        }

        // 加载第二个结构
        Template template2 = templateManager.getTemplate(server, new ResourceLocation("blood", structureName2));
        if (template2 == null) {
            System.err.println("无法加载结构: " + structureName2);
            return false;
        }

        // 获取结构尺寸
        BlockPos size1 = template1.getSize();
        BlockPos size2 = template2.getSize();

        // 计算总结构尺寸（考虑偏移）
        int totalWidthX = Math.max(size1.getX(), offsetX + size2.getX());
        int totalWidthZ = Math.max(size1.getZ(), size2.getZ());
        int maxHeight = Math.max(size1.getY(), size2.getY());

        // 不清理结构内部空间，让结构直接融入自然洞穴
        // 只清理出入口

        // 设置放置参数
        PlacementSettings settings = new PlacementSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        // 放置第一个结构
        template1.addBlocksToWorld(worldIn, generatePos, settings);

        // 放置第二个结构（偏移 offsetX 格）
        BlockPos pos2 = generatePos.add(offsetX, 0, 0);
        template2.addBlocksToWorld(worldIn, pos2, settings);

        // 将结构内的地狱岩替换为肉块
        replaceBlocks(worldIn, generatePos, totalWidthX, totalWidthZ, maxHeight);

        // 清理X轴前后两端作为出入口
        clearEntrances(worldIn, generatePos, totalWidthX, totalWidthZ, maxHeight);

        // 分配战利品到箱子
        BloodStructureLootManager.distributeLoot(worldIn, generatePos, totalWidthX, totalWidthZ, maxHeight, rand);

        // 在结构中心生成血液母体（地狱石上）
        spawnBloodMother(worldIn, generatePos, totalWidthX, totalWidthZ, maxHeight, rand);

        return true;
    }

    /**
     * 生成X轴前后两端的隧道连接到洞穴
     * 不清理长门区域，只保留自然隧道连接
     */
    private void clearEntrances(World world, BlockPos startPos, int widthX, int widthZ, int height) {
        int entranceZ = widthZ / 2; // 在Z轴中间位置

        // 生成前端隧道（X负方向），从结构内部3格处开始
        generateTunnelToCave(world, startPos.add(3, 0, entranceZ), -1, 0, height);

        // 生成后端隧道（X正方向），从结构内部3格处开始
        int maxX = Math.max(widthX, offsetX + 32);
        generateTunnelToCave(world, startPos.add(maxX - 3, 0, entranceZ), 1, 0, height);
    }

    /**
     * 生成弯曲通道连接到洞穴
     * @param startPos 起始位置
     * @param dirX X轴方向（-1或1）
     * @param dirZ Z轴方向（-1、0或1）
     * @param height 通道高度
     */
    private void generateTunnelToCave(World world, BlockPos startPos, int dirX, int dirZ, int height) {
        Random random = new Random();
        int maxLength = 30; // 增加最大通道长度到30格
        int currentX = 0;
        int currentZ = 0;
        int currentDirX = dirX;
        int currentDirZ = dirZ;

        for (int i = 0; i < maxLength; i++) {
            // 计算当前通道位置
            BlockPos tunnelPos = startPos.add(currentX, 0, currentZ);

            // 检查前方是否是洞穴（空气）
            BlockPos frontPos = startPos.add(currentX + currentDirX, 0, currentZ + currentDirZ);
            if (isAir(world, frontPos)) {
                // 找到洞穴，停止生成
                break;
            }

            // 清理通道（3格宽，高度为结构高度）
            for (int dx = 0; dx < 3; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int y = 0; y < height; y++) {
                        BlockPos pos = tunnelPos.add(dx * currentDirX, y, dz);
                        world.setBlockToAir(pos);
                    }
                }
            }

            // 随机改变方向，让通道弯曲
            if (random.nextFloat() < 0.3) {
                // 30%概率改变Z轴方向
                if (random.nextBoolean()) {
                    currentDirZ = random.nextBoolean() ? 1 : -1;
                } else {
                    currentDirZ = 0;
                }
            }

            // 前进
            currentX += currentDirX;
            currentZ += currentDirZ;
        }
    }

    /**
     * 将结构范围内的地狱岩替换为模组自定义的肉块
     */
    private void replaceBlocks(World world, BlockPos startPos, int widthX, int widthZ, int height) {
        int scanWidthX = Math.max(widthX, offsetX + 32);
        for (int x = 0; x < scanWidthX; x++) {
            for (int z = 0; z < widthZ; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = startPos.add(x, y, z);
                    if (world.getBlockState(pos).getBlock() == net.minecraft.init.Blocks.NETHERRACK) {
                        world.setBlockState(pos, ModBlocks.FLESH_CHUNK.getDefaultState(), 2);
                    }
                }
            }
        }
    }

    /**
     * 检查方块是否是空气
     */
    private boolean isAir(World world, BlockPos pos) {
        return world.isAirBlock(pos);
    }

    /**
     * 在结构内生成血液母体
     * 全局扫描结构覆盖区域寻找肉块，不再依赖不稳定的中心点计算
     */
    private void spawnBloodMother(World world, BlockPos startPos, int widthX, int widthZ, int height, Random rand) {
        boolean spawned = false;
        
        // 扩展扫描范围，确保包含偏移后的第二个结构 (offsetX + 32 是为了覆盖完整宽度)
        int scanWidthX = Math.max(widthX, offsetX + 32);
        
        // 我们在结构覆盖的整个地平面范围内进行随机采样和搜索
        // 尝试 50 次随机位置采样，这样比死板的中心点更灵活
        for (int i = 0; i < 50 && !spawned; i++) {
            int rx = rand.nextInt(scanWidthX);
            int rz = rand.nextInt(widthZ);
            BlockPos checkBasePos = startPos.add(rx, 0, rz);

            // 在该垂直线上寻找肉块
            for (int y = 0; y < height; y++) {
                BlockPos checkPos = checkBasePos.up(y);
                net.minecraft.block.Block block = world.getBlockState(checkPos).getBlock();

                if (block == ModBlocks.FLESH_CHUNK) {
                    BlockPos spawnPos = checkPos.up();
                    // 检查上方是否有足够空间容纳母体 (5x4x5的体积)
                    if (world.isAirBlock(spawnPos) || world.getBlockState(spawnPos).getMaterial().isReplaceable()) {
                        com.qiamao.blood.entity.EntityBloodMother mother = new com.qiamao.blood.entity.EntityBloodMother(world);
                        mother.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
                        
                        // 关键：在 1.12.2 中，必须手动调用 onInitialSpawn 来初始化实体数据
                        mother.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
                        
                        world.spawnEntity(mother);
                        spawned = true;
                        break;
                    }
                }
            }
        }

        // 最终兜底方案：如果随机采样失败，直接在结构入口或中心强制生成
        if (!spawned) {
            // 强制偏移到 Y=32 (因为 GENERATION_HEIGHT 是 30，肉块通常在 31 左右)
            BlockPos fallbackPos = startPos.add(offsetX / 2, 2, widthZ / 2);
            com.qiamao.blood.entity.EntityBloodMother mother = new com.qiamao.blood.entity.EntityBloodMother(world);
            mother.setLocationAndAngles(fallbackPos.getX() + 0.5D, fallbackPos.getY(), fallbackPos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
            
            // 同样需要初始化
            mother.onInitialSpawn(world.getDifficultyForLocation(fallbackPos), null);
            
            world.spawnEntity(mother);
        }
    }
}
