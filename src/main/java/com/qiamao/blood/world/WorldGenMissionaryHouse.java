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

        // NBT 结构有地下部分，根据您的设定，第6格及以下算地下室（即地下部分深6格）
        // 所以我们需要将整个结构向下偏移 6 格，确保地下室完美埋入地下，而一楼正好在地面
        BlockPos generatePos = position.down(6);

        // 在生成结构前，保存包围盒内所有方块的原始状态
        Map<BlockPos, IBlockState> originalStates = new java.util.HashMap<>();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockPos worldPos = Template.transformedBlockPos(settings, localPos).add(generatePos);
                    originalStates.put(worldPos, worldIn.getBlockState(worldPos));
                }
            }
        }

        // 放置结构，忽略模板中的 STRUCTURE_VOID（外围的结构空位不会吞噬自然地形）
        settings.setReplacedBlock(Blocks.STRUCTURE_VOID);
        template.addBlocksToWorld(worldIn, generatePos, settings);
        
        // 智能修复：利用室内空气扩散，将外围的空气方块恢复为原始地形，完美防止结构吞噬周围环境
        restoreOutsideTerrain(worldIn, generatePos, size, settings, originalStates);

        // 智能替换木材：将结构中的木材智能替换为该群系本地的木材种类，同时保持原有方块状态
        replaceWoodWithBiomeType(worldIn, originalStates.keySet(), worldIn.getBiome(position));

        // 生成传教士
        spawnCultistPreacher(worldIn, generatePos, rand, settings);

        return true;
    }

    private void spawnCultistPreacher(World world, BlockPos startPos, Random rand, PlacementSettings settings) {
        // 在地下室生成一个传教士。地下室地面的 Y 坐标在结构中大约是 1
        BlockPos spawnPos = Template.transformedBlockPos(settings, new BlockPos(2, 1, 6)).add(startPos);
        
        // 确保生成在方块上方
        while (world.isAirBlock(spawnPos.down()) && spawnPos.getY() > startPos.getY()) {
            spawnPos = spawnPos.down();
        }
        while (!world.isAirBlock(spawnPos) && spawnPos.getY() < startPos.getY() + 15) {
            spawnPos = spawnPos.up();
        }

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
    
    private void restoreOutsideTerrain(World world, BlockPos startPos, BlockPos size, PlacementSettings settings, Map<BlockPos, IBlockState> originalStates) {
        java.util.Set<BlockPos> structurePositions = originalStates.keySet();
        Map<BlockPos, IBlockState> toRestore = new java.util.HashMap<>();

        // NBT结构往下移动了6格，所以 startPos.getY() + 6 正好是地表高度
        int groundY = startPos.getY() + 6;

        for (BlockPos pos : structurePositions) {
            IBlockState currentState = world.getBlockState(pos);
            
            // 如果放置后该位置是空气，或者是可替换方块（比如草丛）
            if (world.isAirBlock(pos) || currentState.getBlock().isReplaceable(world, pos)) {
                boolean isOutside = false;

                // 将四面墙探测法向上延伸2格（涵盖到 groundY + 2 以下）
                if (pos.getY() < groundY + 2) {
                    // 【地下及地表部分】：使用“四面墙探测法”
                    // 室内空气必然被墙壁包裹。如果向外延伸直到结构边界都没有碰到实体方块，说明这是墙外的空气
                    boolean hasWallEast = false;
                    boolean hasWallWest = false;
                    boolean hasWallSouth = false;
                    boolean hasWallNorth = false;

                    // 检查 +X 方向
                    for (int x = pos.getX() + 1; ; x++) {
                        BlockPos checkPos = new BlockPos(x, pos.getY(), pos.getZ());
                        if (!structurePositions.contains(checkPos)) break; // 到达结构边界
                        if (!world.isAirBlock(checkPos) && !world.getBlockState(checkPos).getBlock().isReplaceable(world, checkPos)) {
                            hasWallEast = true; break;
                        }
                    }
                    // 检查 -X 方向
                    for (int x = pos.getX() - 1; ; x--) {
                        BlockPos checkPos = new BlockPos(x, pos.getY(), pos.getZ());
                        if (!structurePositions.contains(checkPos)) break; // 到达结构边界
                        if (!world.isAirBlock(checkPos) && !world.getBlockState(checkPos).getBlock().isReplaceable(world, checkPos)) {
                            hasWallWest = true; break;
                        }
                    }
                    // 检查 +Z 方向
                    for (int z = pos.getZ() + 1; ; z++) {
                        BlockPos checkPos = new BlockPos(pos.getX(), pos.getY(), z);
                        if (!structurePositions.contains(checkPos)) break; // 到达结构边界
                        if (!world.isAirBlock(checkPos) && !world.getBlockState(checkPos).getBlock().isReplaceable(world, checkPos)) {
                            hasWallSouth = true; break;
                        }
                    }
                    // 检查 -Z 方向
                    for (int z = pos.getZ() - 1; ; z--) {
                        BlockPos checkPos = new BlockPos(pos.getX(), pos.getY(), z);
                        if (!structurePositions.contains(checkPos)) break; // 到达结构边界
                        if (!world.isAirBlock(checkPos) && !world.getBlockState(checkPos).getBlock().isReplaceable(world, checkPos)) {
                            hasWallNorth = true; break;
                        }
                    }

                    // 只要有任何一个方向没有墙壁，它就是室外空气
                    if (!hasWallNorth || !hasWallSouth || !hasWallEast || !hasWallWest) {
                        isOutside = true;
                    }
                } else {
                    // 【地上部分】：使用“屋顶探测法”
                    boolean hasRoof = false;
                    for (int y = pos.getY() + 1; ; y++) {
                        BlockPos upPos = new BlockPos(pos.getX(), y, pos.getZ());
                        if (!structurePositions.contains(upPos)) break; // 到达结构顶部边界
                        IBlockState upState = world.getBlockState(upPos);
                        if (!world.isAirBlock(upPos) && !upState.getBlock().isReplaceable(world, upPos)) {
                            hasRoof = true;
                            break;
                        }
                    }
                    if (!hasRoof) {
                        isOutside = true;
                    }
                }

                if (isOutside) {
                    IBlockState originalState = originalStates.get(pos);
                    // 只有当这个地方原本是实体地形（如泥土、石头等），我们才将其恢复
                    if (originalState != null && originalState.getBlock() != Blocks.AIR) {
                        toRestore.put(pos, originalState);
                    }
                }
            }
        }

        // 统一恢复地形
        for (Map.Entry<BlockPos, IBlockState> entry : toRestore.entrySet()) {
            world.setBlockState(entry.getKey(), entry.getValue(), 2);
        }
    }

    private void replaceWoodWithBiomeType(World world, java.util.Set<BlockPos> structurePositions, net.minecraft.world.biome.Biome biome) {
        net.minecraft.block.BlockPlanks.EnumType targetWood = getBiomeWoodType(biome);
        if (targetWood == net.minecraft.block.BlockPlanks.EnumType.OAK) {
            return; // 默认是橡木，如果是橡木则不需要替换，节省性能
        }

        for (BlockPos pos : structurePositions) {
            IBlockState state = world.getBlockState(pos);
            IBlockState newState = convertWoodState(state, targetWood);
            if (state != newState) {
                // 使用 2 标志（仅同步客户端，不触发方块更新），防止替换门、楼梯等多方块结构时因为邻方块更新而断裂掉落
                world.setBlockState(pos, newState, 2);
            }
        }
    }

    private net.minecraft.block.BlockPlanks.EnumType getBiomeWoodType(net.minecraft.world.biome.Biome biome) {
        if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS)) return net.minecraft.block.BlockPlanks.EnumType.SPRUCE;
        if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.JUNGLE)) return net.minecraft.block.BlockPlanks.EnumType.JUNGLE;
        if (net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.SAVANNA)) return net.minecraft.block.BlockPlanks.EnumType.ACACIA;
        
        if (biome.getRegistryName() != null) {
            String name = biome.getRegistryName().getResourcePath().toLowerCase();
            if (name.contains("birch")) return net.minecraft.block.BlockPlanks.EnumType.BIRCH;
            if (name.contains("roofed") || name.contains("dark_oak")) return net.minecraft.block.BlockPlanks.EnumType.DARK_OAK;
        }
        return net.minecraft.block.BlockPlanks.EnumType.OAK;
    }

    private IBlockState convertWoodState(IBlockState state, net.minecraft.block.BlockPlanks.EnumType targetWood) {
        net.minecraft.block.Block block = state.getBlock();
        
        // 1. 木板
        if (block == Blocks.PLANKS) {
            return Blocks.PLANKS.getDefaultState().withProperty(net.minecraft.block.BlockPlanks.VARIANT, targetWood);
        }
        
        // 2. 原木
        if (block == Blocks.LOG || block == Blocks.LOG2) {
            net.minecraft.block.BlockLog.EnumAxis axis = state.getValue(net.minecraft.block.BlockLog.LOG_AXIS);
            if (targetWood == net.minecraft.block.BlockPlanks.EnumType.ACACIA || targetWood == net.minecraft.block.BlockPlanks.EnumType.DARK_OAK) {
                return Blocks.LOG2.getDefaultState()
                    .withProperty(net.minecraft.block.BlockNewLog.VARIANT, targetWood == net.minecraft.block.BlockPlanks.EnumType.ACACIA ? net.minecraft.block.BlockPlanks.EnumType.ACACIA : net.minecraft.block.BlockPlanks.EnumType.DARK_OAK)
                    .withProperty(net.minecraft.block.BlockLog.LOG_AXIS, axis);
            } else {
                return Blocks.LOG.getDefaultState()
                    .withProperty(net.minecraft.block.BlockOldLog.VARIANT, targetWood)
                    .withProperty(net.minecraft.block.BlockLog.LOG_AXIS, axis);
            }
        }
        
        // 3. 楼梯
        if (block instanceof net.minecraft.block.BlockStairs) {
            boolean isWoodStair = block == Blocks.OAK_STAIRS || block == Blocks.SPRUCE_STAIRS || 
                                  block == Blocks.BIRCH_STAIRS || block == Blocks.JUNGLE_STAIRS || 
                                  block == Blocks.ACACIA_STAIRS || block == Blocks.DARK_OAK_STAIRS;
            if (isWoodStair) {
                net.minecraft.block.Block newStair = getStairBlock(targetWood);
                return newStair.getDefaultState()
                    .withProperty(net.minecraft.block.BlockStairs.FACING, state.getValue(net.minecraft.block.BlockStairs.FACING))
                    .withProperty(net.minecraft.block.BlockStairs.HALF, state.getValue(net.minecraft.block.BlockStairs.HALF));
            }
        }
        
        // 4. 门
        if (block instanceof net.minecraft.block.BlockDoor) {
            boolean isWoodDoor = block == Blocks.OAK_DOOR || block == Blocks.SPRUCE_DOOR || 
                                 block == Blocks.BIRCH_DOOR || block == Blocks.JUNGLE_DOOR || 
                                 block == Blocks.ACACIA_DOOR || block == Blocks.DARK_OAK_DOOR;
            if (isWoodDoor) {
                net.minecraft.block.Block newDoor = getDoorBlock(targetWood);
                return newDoor.getDefaultState()
                    .withProperty(net.minecraft.block.BlockDoor.FACING, state.getValue(net.minecraft.block.BlockDoor.FACING))
                    .withProperty(net.minecraft.block.BlockDoor.OPEN, state.getValue(net.minecraft.block.BlockDoor.OPEN))
                    .withProperty(net.minecraft.block.BlockDoor.HINGE, state.getValue(net.minecraft.block.BlockDoor.HINGE))
                    .withProperty(net.minecraft.block.BlockDoor.HALF, state.getValue(net.minecraft.block.BlockDoor.HALF))
                    .withProperty(net.minecraft.block.BlockDoor.POWERED, state.getValue(net.minecraft.block.BlockDoor.POWERED));
            }
        }
        
        // 5. 半砖
        if (block == Blocks.WOODEN_SLAB) {
            return Blocks.WOODEN_SLAB.getDefaultState()
                .withProperty(net.minecraft.block.BlockWoodSlab.VARIANT, targetWood)
                .withProperty(net.minecraft.block.BlockSlab.HALF, state.getValue(net.minecraft.block.BlockSlab.HALF));
        } else if (block == Blocks.DOUBLE_WOODEN_SLAB) {
            return Blocks.DOUBLE_WOODEN_SLAB.getDefaultState()
                .withProperty(net.minecraft.block.BlockWoodSlab.VARIANT, targetWood);
        }
        
        // 6. 栅栏
        if (block instanceof net.minecraft.block.BlockFence) {
            boolean isWoodFence = block == Blocks.OAK_FENCE || block == Blocks.SPRUCE_FENCE || 
                                  block == Blocks.BIRCH_FENCE || block == Blocks.JUNGLE_FENCE || 
                                  block == Blocks.ACACIA_FENCE || block == Blocks.DARK_OAK_FENCE;
            if (isWoodFence) {
                return getFenceBlock(targetWood).getDefaultState(); // 连接状态会在渲染时动态计算
            }
        }
        
        // 7. 栅栏门
        if (block instanceof net.minecraft.block.BlockFenceGate) {
            boolean isWoodGate = block == Blocks.OAK_FENCE_GATE || block == Blocks.SPRUCE_FENCE_GATE || 
                                 block == Blocks.BIRCH_FENCE_GATE || block == Blocks.JUNGLE_FENCE_GATE || 
                                 block == Blocks.ACACIA_FENCE_GATE || block == Blocks.DARK_OAK_FENCE_GATE;
            if (isWoodGate) {
                return getFenceGateBlock(targetWood).getDefaultState()
                    .withProperty(net.minecraft.block.BlockFenceGate.FACING, state.getValue(net.minecraft.block.BlockFenceGate.FACING))
                    .withProperty(net.minecraft.block.BlockFenceGate.OPEN, state.getValue(net.minecraft.block.BlockFenceGate.OPEN))
                    .withProperty(net.minecraft.block.BlockFenceGate.IN_WALL, state.getValue(net.minecraft.block.BlockFenceGate.IN_WALL))
                    .withProperty(net.minecraft.block.BlockFenceGate.POWERED, state.getValue(net.minecraft.block.BlockFenceGate.POWERED));
            }
        }

        return state;
    }

    private net.minecraft.block.Block getStairBlock(net.minecraft.block.BlockPlanks.EnumType type) {
        switch (type) {
            case SPRUCE: return Blocks.SPRUCE_STAIRS;
            case BIRCH: return Blocks.BIRCH_STAIRS;
            case JUNGLE: return Blocks.JUNGLE_STAIRS;
            case ACACIA: return Blocks.ACACIA_STAIRS;
            case DARK_OAK: return Blocks.DARK_OAK_STAIRS;
            default: return Blocks.OAK_STAIRS;
        }
    }
    
    private net.minecraft.block.Block getDoorBlock(net.minecraft.block.BlockPlanks.EnumType type) {
        switch (type) {
            case SPRUCE: return Blocks.SPRUCE_DOOR;
            case BIRCH: return Blocks.BIRCH_DOOR;
            case JUNGLE: return Blocks.JUNGLE_DOOR;
            case ACACIA: return Blocks.ACACIA_DOOR;
            case DARK_OAK: return Blocks.DARK_OAK_DOOR;
            default: return Blocks.OAK_DOOR;
        }
    }
    
    private net.minecraft.block.Block getFenceBlock(net.minecraft.block.BlockPlanks.EnumType type) {
        switch (type) {
            case SPRUCE: return Blocks.SPRUCE_FENCE;
            case BIRCH: return Blocks.BIRCH_FENCE;
            case JUNGLE: return Blocks.JUNGLE_FENCE;
            case ACACIA: return Blocks.ACACIA_FENCE;
            case DARK_OAK: return Blocks.DARK_OAK_FENCE;
            default: return Blocks.OAK_FENCE;
        }
    }

    private net.minecraft.block.Block getFenceGateBlock(net.minecraft.block.BlockPlanks.EnumType type) {
        switch (type) {
            case SPRUCE: return Blocks.SPRUCE_FENCE_GATE;
            case BIRCH: return Blocks.BIRCH_FENCE_GATE;
            case JUNGLE: return Blocks.JUNGLE_FENCE_GATE;
            case ACACIA: return Blocks.ACACIA_FENCE_GATE;
            case DARK_OAK: return Blocks.DARK_OAK_FENCE_GATE;
            default: return Blocks.OAK_FENCE_GATE;
        }
    }
}