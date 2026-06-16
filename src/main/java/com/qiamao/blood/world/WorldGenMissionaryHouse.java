package com.qiamao.blood.world;

import com.qiamao.blood.entity.EntityCultistPreacher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

import java.util.Map;
import java.util.Random;

public class WorldGenMissionaryHouse extends WorldGenerator {

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        MinecraftServer server = worldIn.getMinecraftServer();
        if (server == null) {
            return false;
        }

        TemplateManager templateManager = server.getWorld(0).getStructureTemplateManager();
        Template template = templateManager.getTemplate(server, new ResourceLocation("blood", "missionary_house"));

        if (template == null) {
            return false;
        }

        // 随机旋转
        Rotation[] rotations = Rotation.values();
        Rotation rotation = rotations[rand.nextInt(rotations.length)];

        PlacementSettings settings = new PlacementSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setReplacedBlock(Blocks.STRUCTURE_VOID)
                .setIgnoreEntities(false);

        // 检查地势是否相对平坦，确保只有地上一层露出
        BlockPos size = template.getSize();
        
        int expectedY = position.getY();
        
        // 采样结构底部区域的地形高度，检查是否平坦
        // 如果有任何一个采样点与中心点的高度差超过2，说明地形崎岖，放弃生成
        int checkStepX = Math.max(1, size.getX() / 3);
        int checkStepZ = Math.max(1, size.getZ() / 3);
        
        for (int x = 0; x < size.getX(); x += checkStepX) {
            for (int z = 0; z < size.getZ(); z += checkStepZ) {
                int height = worldIn.getHeight(position.add(x, 0, z)).getY();
                if (Math.abs(height - expectedY) > 2) {
                    return false;
                }
            }
        }

        // NBT 结构有地下部分，根据您的设定，y第8格算地下室（即地下部分深8格）
        // 所以我们需要将整个结构向下偏移 8 格，确保地下室完美埋入地下
        BlockPos generatePos = position.down(8);

        // 放置结构，忽略模板中的 STRUCTURE_VOID（外围的结构空位不会吞噬自然地形）
        settings.setReplacedBlock(Blocks.STRUCTURE_VOID);
        template.addBlocksToWorld(worldIn, generatePos, settings);
        
        // 专门修复 NBT 中的空气方块：利用室内空气扩散取反法，完美填补坑洞且绝不误填室内
        fillOutsideAirWithDirt(worldIn, generatePos, size, settings);

        // 生成传教士
        spawnCultistPreacher(worldIn, generatePos, rand, settings);

        return true;
    }

    private void spawnCultistPreacher(World world, BlockPos startPos, Random rand, PlacementSettings settings) {
        // 在第一层的床边生成一个传教士。假设床的坐标大概是 (2, 1, 6)，根据旋转进行变换
        BlockPos spawnPos = Template.transformedBlockPos(settings, new BlockPos(2, 1, 6)).add(startPos);
        
        EntityCultistPreacher preacher = new EntityCultistPreacher(world);
        preacher.setLocationAndAngles(
            spawnPos.getX() + 0.5D, 
            spawnPos.getY(), 
            spawnPos.getZ() + 0.5D, 
            rand.nextFloat() * 360.0F, 
            0.0F
        );
        
        world.spawnEntity(preacher);
    }
    
    private void fillOutsideAirWithDirt(World world, BlockPos startPos, BlockPos size, PlacementSettings settings) {
        // 地下室深度为 8 格，所以 Y 轴从 0 到 7 都是地下部分
        int maxY = 7;
        if (maxY >= size.getY()) maxY = size.getY() - 1;

        java.util.Set<BlockPos> indoorAir = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();

        // 1. 寻找确定的“室内空气”起点
        // 我们知道传教士生成在床边，坐标大致是 (2, 1, 6)（这里的 1 是指地上一层，即 Y=8）
        BlockPos indoorSeed = Template.transformedBlockPos(settings, new BlockPos(2, 1, 6)).add(startPos);
        
        // 如果这个点不是空气（比如刚好被方块挡住），我们就往上找一点点
        if (!world.isAirBlock(indoorSeed)) {
            indoorSeed = indoorSeed.up();
        }
        
        if (world.isAirBlock(indoorSeed)) {
            queue.add(indoorSeed);
            indoorAir.add(indoorSeed);
        }

        // 2. 洪水填充，找出所有相连的“室内空气”
        // 范围限制在整个建筑包围盒内（不仅是地下，还包括地上，因为要顺着楼梯扩散下去）
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            // 向六个方向扩散
            for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.values()) {
                BlockPos neighbor = pos.offset(facing);
                
                int nx = neighbor.getX() - startPos.getX();
                int ny = neighbor.getY() - startPos.getY();
                int nz = neighbor.getZ() - startPos.getZ();
                
                // 限制在建筑的整体包围盒范围内扩散（地上+地下）
                if (nx >= 0 && nx < size.getX() && ny >= 0 && ny < size.getY() && nz >= 0 && nz < size.getZ()) {
                    if (!indoorAir.contains(neighbor)) {
                        // 只要是空气（或者可替换方块如草、雪等，或者是NBT里的树叶等不需要的遮挡），就认为是室内空间的一部分
                        // 额外加入判断，如果是原版树木（木头、树叶），这说明室内有树穿模进来了，也要算作室内空间并替换为空气
                        IBlockState neighborState = world.getBlockState(neighbor);
                        net.minecraft.block.Block neighborBlock = neighborState.getBlock();
                        
                        if (world.isAirBlock(neighbor) || 
                            neighborBlock.isReplaceable(world, neighbor) ||
                            neighborBlock instanceof net.minecraft.block.BlockLeaves ||
                            neighborBlock instanceof net.minecraft.block.BlockLog) {
                            
                            // 如果是树叶木头这些乱入的东西，直接先把它变成空气，防止阻断室内判定
                            if (!world.isAirBlock(neighbor)) {
                                world.setBlockToAir(neighbor);
                            }
                            
                            indoorAir.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // 3. 遍历地下室的包围盒 (Y <= maxY)
        // 任何在这个范围内的空气，只要不在 indoorAir 集合中，它就绝对是外部的“大坑”！
        // 我们将其无情替换为泥土，或者原石（如果是紧贴墙壁的底部）
        for (int x = -1; x <= size.getX(); x++) {
            for (int y = -1; y <= maxY; y++) {
                for (int z = -1; z <= size.getZ(); z++) {
                    BlockPos pos = startPos.add(x, y, z);
                    // 注意：只替换空气或者原本被可替换方块占据的地方
                    if (world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                        if (!indoorAir.contains(pos)) {
                            // 为了与地下环境更好融合：
                            // 如果深度比较深（靠近地下室底部），或者四周有石头，填充原石/石头，否则填泥土
                            boolean hasStone = false;
                            for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.values()) {
                                net.minecraft.block.Block b = world.getBlockState(pos.offset(facing)).getBlock();
                                if (b == Blocks.STONE || b == Blocks.COBBLESTONE) {
                                    hasStone = true;
                                    break;
                                }
                            }
                            
                            if (hasStone || pos.getY() < 60) { // Y<60 一般是石头层
                                world.setBlockState(pos, Blocks.STONE.getDefaultState(), 2);
                            } else {
                                world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }
}