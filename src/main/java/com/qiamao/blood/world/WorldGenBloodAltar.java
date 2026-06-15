package com.qiamao.blood.world;

import com.qiamao.blood.entity.EntityBloodSeeker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WorldGenBloodAltar extends WorldGenerator {

    public static float CHEST_SPAWN_CHANCE = 1.0f; // 100% 刷新箱子
    public static float BREAD_SPAWN_CHANCE = 0.80f;
    public static float ARMOR_SPAWN_CHANCE = 0.05f;
    public static float COAL_SPAWN_CHANCE = 0.50f;

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        // 1. 验证地基：确保中心点在固体地面上
        Block blockBelow = worldIn.getBlockState(position.down()).getBlock();
        if (blockBelow != Blocks.GRASS && blockBelow != Blocks.DIRT && blockBelow != Blocks.STONE) {
            return false;
        }

        // ==============================
        // 建造 13x13 破败遗迹 (带微调)
        // ==============================
        // 大小 13 左右，微调范围 [-1, +1]，即 12 到 14
        int width = 12 + rand.nextInt(3);
        int depth = 12 + rand.nextInt(3);
        int roomHeight = 7 + rand.nextInt(3); // 内部空间高度显著增加 (7到9格高)

        int startX = position.getX() - width / 2;
        // 降低地基：将建筑整体下沉 1 格，这样 1 楼的地板刚好与地面平齐或稍微埋入一点
        // 保证门的底部方块可以直接走到平地上
        int startY = position.getY() - 1;
        int startZ = position.getZ() - depth / 2;

        // 门的位置 (选择在前墙正中间)
        int doorX = width / 2;
        int doorZ = depth - 1;

        // 1.5 生成底部的放射状肉块地表
        // 放射长度（半径）比结构的一半长6格，即向外延伸6格
        // 为了防止祭坛生成时其肉块地表跨越区块导致死循环，把最大半径限制在 6 以内
        int radius = Math.min(Math.max(width, depth) / 2 + 6, 6);
        int centerX = position.getX();
        int centerZ = position.getZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // 计算当前坐标到中心的距离
                double distance = Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ));
                if (distance <= radius) {
                    // 获取当前 (x, z) 的最高地表方块
                    BlockPos topPos = worldIn.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
                    
                    // 向下寻找真正的地表实体方块（跳过树叶、草、原木等）
                    while (topPos.getY() > 0 && 
                           (worldIn.isAirBlock(topPos) || 
                            worldIn.getBlockState(topPos).getBlock().isReplaceable(worldIn, topPos) ||
                            worldIn.getBlockState(topPos).getBlock().isLeaves(worldIn.getBlockState(topPos), worldIn, topPos) ||
                            worldIn.getBlockState(topPos).getBlock() == Blocks.LOG ||
                            worldIn.getBlockState(topPos).getBlock() == Blocks.LOG2)) {
                        topPos = topPos.down();
                    }
                    
                    // 替换深度 1-2 格
                    int replaceDepth = 1 + rand.nextInt(2);
                    for (int d = 0; d < replaceDepth; d++) {
                        BlockPos replacePos = topPos.down(d);
                        Block block = worldIn.getBlockState(replacePos).getBlock();
                        
                        // 替换常见的地表方块为肉块
                        if (block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.STONE || 
                            block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.SANDSTONE) {
                            this.setBlockAndNotifyAdequately(worldIn, replacePos, getRandomFleshState(rand));
                        }
                    }
                }
            }
        }

        // 2. 建造地基和地板 (适应地形，防止建筑变形)
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos currentPos = new BlockPos(startX + x, startY, startZ + z);
                // 地板主要用平滑石头
                this.setBlockAndNotifyAdequately(worldIn, currentPos, getFloorStoneState(rand));
                
                // 向下延伸地基，防止悬空
                BlockPos supportPos = currentPos.down();
                int depthCount = 0;
                while ((worldIn.isAirBlock(supportPos) || worldIn.getBlockState(supportPos).getBlock().isReplaceable(worldIn, supportPos)) && depthCount < 15) {
                    this.setBlockAndNotifyAdequately(worldIn, supportPos, getWallStoneState(rand));
                    supportPos = supportPos.down();
                    depthCount++;
                }
            }
        }

        // 3. 建造墙壁和清空内部
        for (int y = 1; y <= roomHeight; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos wallPos = new BlockPos(startX + x, startY + y, startZ + z);
                    
                    // 如果是四周边缘，就是墙壁
                    if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                        // 为门留出空位 (高度 1 和 2)
                        if (x == doorX && z == doorZ && (y == 1 || y == 2)) {
                            this.setBlockAndNotifyAdequately(worldIn, wallPos, Blocks.AIR.getDefaultState());
                            continue;
                        }
                        
                        // 破败感：有 15% 的几率墙壁会有缺口（门附近除外，保证门框完整）
                        if (Math.abs(x - doorX) > 1 || z != doorZ) {
                            if (rand.nextFloat() > 0.15f) {
                                this.setBlockAndNotifyAdequately(worldIn, wallPos, getWallStoneState(rand));
                            }
                        } else {
                            this.setBlockAndNotifyAdequately(worldIn, wallPos, getWallStoneState(rand));
                        }
                    } else {
                        // 内部空间必须清空 (替换掉可能存在的树干/树叶/草)
                        this.setBlockAndNotifyAdequately(worldIn, wallPos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // 4. 封顶 (屋顶)
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos roofPos = new BlockPos(startX + x, startY + roomHeight + 1, startZ + z);
                // 破败感：屋顶有 20% 的几率塌陷漏光
                if (rand.nextFloat() > 0.20f) {
                    this.setBlockAndNotifyAdequately(worldIn, roofPos, getRoofStoneState(rand));
                }
            }
        }

        // 5. 放置门 (根据群系决定材质)
        BlockDoor doorBlock = getBiomeDoor(worldIn.getBiome(position));
        // 我们之前把门放在了 startZ + depth - 1 的位置 (即朝南/Z轴正方向的那面墙)
        BlockPos doorBottom = new BlockPos(startX + doorX, startY + 1, startZ + doorZ);
        
        // 门的朝向，这里我们让门放在南面的墙上 (Z最大值)，所以门外应该是 SOUTH
        BlockPos outsideDoor = doorBottom.south();
        // 清理门外的方块，防止因为建筑下沉导致出门撞墙
        if (!worldIn.isAirBlock(outsideDoor)) {
            this.setBlockAndNotifyAdequately(worldIn, outsideDoor, Blocks.AIR.getDefaultState());
            this.setBlockAndNotifyAdequately(worldIn, outsideDoor.up(), Blocks.AIR.getDefaultState());
        }
        
        // 门外的地基如果悬空（出门跳河），我们需要帮玩家填平一格或者搭个阶梯
        BlockPos outsideFloor = outsideDoor.down();
        if (worldIn.isAirBlock(outsideFloor) || worldIn.getBlockState(outsideFloor).getBlock().isReplaceable(worldIn, outsideFloor) || worldIn.getBlockState(outsideFloor).getMaterial().isLiquid()) {
            // 如果门外脚下是空气、可替换物或者水，我们铺一块原石作为台阶
            this.setBlockAndNotifyAdequately(worldIn, outsideFloor, Blocks.COBBLESTONE.getDefaultState());
        }

        // 确保门底下是实体方块
        if (worldIn.getBlockState(doorBottom.down()).isOpaqueCube()) {
            // 门放在南墙，朝外(向南)
            IBlockState lowerDoor = doorBlock.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(BlockDoor.FACING, EnumFacing.SOUTH);
            IBlockState upperDoor = doorBlock.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(BlockDoor.FACING, EnumFacing.SOUTH);
            this.setBlockAndNotifyAdequately(worldIn, doorBottom, lowerDoor);
            this.setBlockAndNotifyAdequately(worldIn, doorBottom.up(), upperDoor);
        }

        // 6. 屋内四周插上火把 (高度在 y=2，吸附在墙上)
        placeWallTorch(worldIn, new BlockPos(startX + 1, startY + 2, startZ + depth / 2), EnumFacing.EAST); // 左墙
        placeWallTorch(worldIn, new BlockPos(startX + width - 2, startY + 2, startZ + depth / 2), EnumFacing.WEST); // 右墙
        placeWallTorch(worldIn, new BlockPos(startX + width / 2, startY + 2, startZ + 1), EnumFacing.SOUTH); // 后墙
        // 前墙 (门的两边插火把)
        placeWallTorch(worldIn, new BlockPos(startX + doorX - 1, startY + 2, startZ + depth - 2), EnumFacing.NORTH);
        placeWallTorch(worldIn, new BlockPos(startX + doorX + 1, startY + 2, startZ + depth - 2), EnumFacing.NORTH);

        // 7. 生成准确的 3 处蜘蛛网 (高度偏上)
        for (int i = 0; i < 3; i++) {
            // x 和 z 在内部随机 (避开紧贴墙的位置)
            int webX = startX + 2 + rand.nextInt(width - 4);
            // 高度在天花板下面 1-2 格
            int webY = startY + roomHeight - rand.nextInt(2); 
            int webZ = startZ + 2 + rand.nextInt(depth - 4);
            BlockPos webPos = new BlockPos(webX, webY, webZ);
            if (worldIn.isAirBlock(webPos)) {
                this.setBlockAndNotifyAdequately(worldIn, webPos, Blocks.WEB.getDefaultState());
            }
        }

        // 8. 建造一楼中等大小祭坛 (中心 3x3)
        BlockPos altarCenter = new BlockPos(startX + width / 2, startY + 1, startZ + depth / 2);
        for (int ax = -1; ax <= 1; ax++) {
            for (int az = -1; az <= 1; az++) {
                BlockPos aPos = altarCenter.add(ax, 0, az);
                this.setBlockAndNotifyAdequately(worldIn, aPos, Blocks.STONE_SLAB.getDefaultState());
            }
        }

        // 9. 生成祭坛宝箱 (67%概率)
        if (rand.nextFloat() < CHEST_SPAWN_CHANCE) {
            BlockPos chestPos = altarCenter.up();
            // 修正箱子朝向：门在 SOUTH 墙上朝外，意味着进门后玩家面向 NORTH。
            // 为了让玩家进门后能正对箱子正面，箱子应该面向 SOUTH。
            this.setBlockAndNotifyAdequately(worldIn, chestPos, Blocks.CHEST.getDefaultState().withProperty(net.minecraft.block.BlockChest.FACING, EnumFacing.SOUTH));
            TileEntity tileEntity = worldIn.getTileEntity(chestPos);
            
            if (tileEntity instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) tileEntity;
                java.util.List<Integer> usedSlots = new java.util.ArrayList<>();
                
                // 辅助方法：获取一个随机的空槽位 (0 到 26)
                java.util.function.Supplier<Integer> getEmptySlot = () -> {
                    if (usedSlots.size() >= 27) return -1;
                    int s;
                    do {
                        s = rand.nextInt(27);
                    } while (usedSlots.contains(s));
                    usedSlots.add(s);
                    return s;
                };

                // 金苹果 1 个
                int slot = getEmptySlot.get();
                if (slot != -1) chest.setInventorySlotContents(slot, new ItemStack(Items.GOLDEN_APPLE, 1, 1));

                // 面包 100% 刷新 (5 到 10 个)，分散成 1~3 个一堆，随机放
                int breadCount = 5 + rand.nextInt(6);
                while (breadCount > 0) {
                    int count = 1 + rand.nextInt(3); // 每次 1 到 3 个
                    if (count > breadCount) count = breadCount;
                    slot = getEmptySlot.get();
                    if (slot == -1) break;
                    chest.setInventorySlotContents(slot, new ItemStack(Items.BREAD, count));
                    breadCount -= count;
                }

                // 煤炭 100% 刷新 (10 到 20 个)，分散成 1~3 个一堆，随机放
                int coalCount = 10 + rand.nextInt(11);
                while (coalCount > 0) {
                    int count = 1 + rand.nextInt(3); // 每次 1 到 3 个
                    if (count > coalCount) count = coalCount;
                    slot = getEmptySlot.get();
                    if (slot == -1) break;
                    chest.setInventorySlotContents(slot, new ItemStack(Items.COAL, count));
                    coalCount -= count;
                }

                // 钻石防具 5% 概率
                if (rand.nextFloat() < ARMOR_SPAWN_CHANCE) {
                    ItemArmor[] diamondArmors = {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
                    int idx1 = rand.nextInt(4);
                    int idx2 = (idx1 + 1 + rand.nextInt(3)) % 4;
                    
                    slot = getEmptySlot.get();
                    if (slot != -1) chest.setInventorySlotContents(slot, createEnchantedArmor(diamondArmors[idx1], rand));
                    
                    slot = getEmptySlot.get();
                    if (slot != -1) chest.setInventorySlotContents(slot, createEnchantedArmor(diamondArmors[idx2], rand));
                }
            }
        }

        // 10. 刷新 5 到 6 只多足嗜血虫
        int seekerCount = 5 + rand.nextInt(2);
        for (int i = 0; i < seekerCount; i++) {
            EntityBloodSeeker seeker = new EntityBloodSeeker(worldIn);
            // 防卡墙：严格限制在内部空间 (离墙至少 2 格距离)，并且高度固定在一楼地板上
            double sx = startX + 2.5D + rand.nextDouble() * (width - 5);
            // 将生成高度提升一格，原先是 startY + 1.0D，由于建筑整体下沉了，所以可能卡在地板方块里或者被某种原因阻止生成。
            // 为了安全起见，我们在 startY + 1.5D 甚至 +2 的位置生成，让它自然掉落在地板上。
            double sy = startY + 1.5D;
            double sz = startZ + 2.5D + rand.nextDouble() * (depth - 5);

            seeker.setLocationAndAngles(sx, sy, sz, rand.nextFloat() * 360.0F, 0.0F);

            // 关键：不要调用 onInitialSpawn，因为 EntityBloodSeeker 重写了 getCanSpawnHere，
            // 里面有严格的自然生成限制（只在早上、不能在某些群系等）。
            // 结构生成是强制刷怪，所以我们直接强制 setPosition 并 spawnEntity，绕过它的自然生成条件检查。

            // 如果实体的碰撞箱和周围有冲突可能会导致它被立刻挤死或无法生成，我们忽略碰撞强制生成
            seeker.setPositionAndUpdate(sx, sy, sz);
            worldIn.spawnEntity(seeker);
        }

        return true;
    }

    /**
     * 在墙上放置火把
     */
    private void placeWallTorch(World worldIn, BlockPos pos, EnumFacing facing) {
        if (worldIn.isAirBlock(pos)) {
            // 注意：BlockTorch 的 FACING 是指它附着在哪一面上
            this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, facing));
        }
    }

    /**
     * 根据生物群系获取对应的木门
     */
    private BlockDoor getBiomeDoor(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) return Blocks.JUNGLE_DOOR;
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) return Blocks.SPRUCE_DOOR; // 针叶林
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) return Blocks.ACACIA_DOOR; // 萨凡纳
        if (biome.getRegistryName() != null) {
            String name = biome.getRegistryName().getResourcePath();
            if (name.contains("dark_forest") || name.contains("roofed_forest")) return Blocks.DARK_OAK_DOOR;
            if (name.contains("birch")) return Blocks.BIRCH_DOOR;
        }
        // 默认橡木门
        return Blocks.OAK_DOOR;
    }

    private IBlockState getFloorStoneState(Random rand) {
        // 地板：70% 平滑石头，15% 原石，5% 苔石，10% 模组肉块
        float r = rand.nextFloat();
        if (r < 0.70f) return Blocks.STONE.getDefaultState();
        if (r < 0.85f) return Blocks.COBBLESTONE.getDefaultState();
        if (r < 0.90f) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
        return getRandomFleshState(rand);
    }

    private IBlockState getWallStoneState(Random rand) {
        // 墙壁：50% 平滑石头，25% 原石，15% 苔石，10% 模组肉块
        float r = rand.nextFloat();
        if (r < 0.50f) return Blocks.STONE.getDefaultState();
        if (r < 0.75f) return Blocks.COBBLESTONE.getDefaultState();
        if (r < 0.90f) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
        return getRandomFleshState(rand);
    }

    private IBlockState getRoofStoneState(Random rand) {
        // 屋顶：65% 平滑石头，20% 原石，10% 苔石，5% 模组肉块
        float r = rand.nextFloat();
        if (r < 0.65f) return Blocks.STONE.getDefaultState();
        if (r < 0.85f) return Blocks.COBBLESTONE.getDefaultState();
        if (r < 0.95f) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
        return getRandomFleshState(rand);
    }

    /**
     * 统一血腥祭坛中肉块的四向随机朝向，避免地表和结构本体朝向过于一致。
     */
    private IBlockState getRandomFleshState(Random rand) {
        EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(rand);
        return com.qiamao.blood.init.ModBlocks.FLESH_CHUNK.getDefaultState()
            .withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, facing);
    }

    private ItemStack createEnchantedArmor(ItemArmor armorItem, Random rand) {
        ItemStack armor = new ItemStack(armorItem);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        Enchantment protection = Enchantment.getEnchantmentByID(0); 
        if (protection != null) {
            int level = 1 + rand.nextInt(3); 
            enchantments.put(protection, level);
            EnchantmentHelper.setEnchantments(enchantments, armor);
        }
        return armor;
    }
}
