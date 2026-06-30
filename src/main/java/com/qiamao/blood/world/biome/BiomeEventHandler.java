package com.qiamao.blood.world.biome;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityBloodEndermite;
import com.qiamao.blood.entity.EntityBloodSeeker;
import com.qiamao.blood.entity.EntityParasiticSteve;
import com.qiamao.blood.init.ModBiomes;
import com.qiamao.blood.init.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 血液翻腾之地服务端事件处理器
 * 处理mood值增加、河流湖泊血液替换
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class BiomeEventHandler {

    // 存储玩家上次增加mood的时间（tick）
    private static final Map<UUID, Long> lastMoodIncreaseTick = new HashMap<>();

    // 0.5秒 = 10 ticks (20 ticks = 1秒)
    private static final int MOOD_INCREASE_INTERVAL = 10;

    /**
     * 玩家tick事件 - 增加mood值
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.world.isRemote) return; // 只在服务端处理
        if (!(event.player instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;
        BlockPos playerPos = player.getPosition();

        // 检查玩家是否在血液翻腾之地群系
        Biome biome = world.getBiome(playerPos);
        if (biome != ModBiomes.BLOOD_SURGING_LAND) return;

        UUID playerId = player.getUniqueID();
        long currentTick = world.getTotalWorldTime();
        long lastTick = lastMoodIncreaseTick.getOrDefault(playerId, 0L);

        // 每0.5秒（10 ticks）增加1% mood值
        if (currentTick - lastTick >= MOOD_INCREASE_INTERVAL) {
            // 获取当前mood值并增加1%
            // Minecraft使用一个复杂的计算方式，这里我们增加soundLevel
            // 实际上MC的mood是随机的，这里我们增加环境音效触发概率

            // 通过增加玩家的统计时间来间接影响mood
            // 或者我们可以通过网络包直接修改客户端mood值

            lastMoodIncreaseTick.put(playerId, currentTick);

            // 发送网络包到客户端增加mood值
            // 由于mood值是客户端计算的，我们需要使用其他方式
            // 这里我们可以触发特定的环境音效来模拟氛围

            // 每10秒（200 ticks）播放一次环境音效
            if (currentTick % 200 == 0) {
                // 可以在这里触发自定义氛围效果
            }
        }
    }

    /**
     * 区块生成事件 - 替换河流和湖泊中的水为血液
     * 在湖泊/河流生成后进行处理
     */
    @SubscribeEvent
    public static void onPopulateChunk(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        Random rand = event.getRand();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();

        // 检查区块是否包含血液翻腾之地群系
        boolean hasBloodBiome = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = new BlockPos(chunkX * 16 + x, 64, chunkZ * 16 + z);
                if (world.getBiome(pos) == ModBiomes.BLOOD_SURGING_LAND) {
                    hasBloodBiome = true;
                    break;
                }
            }
            if (hasBloodBiome) break;
        }

        if (!hasBloodBiome) return;

        // 替换该区块中的水为血液
        replaceWaterWithBlood(world, chunkX, chunkZ);

        // 为血块设置随机朝向
        randomizeBloodBlockFacing(world, chunkX, chunkZ);

        // 覆盖斜坡侧面的裸露石头/泥土，使其与肉块地表融为一体
        capExposedSurfaces(world, chunkX, chunkZ);
    }

    /**
     * 为血液群系中的血块设置随机4面朝向
     */
    private static void randomizeBloodBlockFacing(World world, int chunkX, int chunkZ) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        Random rand = new Random(world.getSeed() + chunkX * 341873128712L + chunkZ * 132897987541L);

        // 4个水平方向
        EnumFacing[] facings = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};

        // 检查整个区块中的血块
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos columnPos = new BlockPos(startX + x, 64, startZ + z);
                if (world.getBiome(columnPos) != ModBiomes.BLOOD_SURGING_LAND) {
                    continue;
                }

                // 从地表向下检查5-6格
                for (int y = 55; y < 75; y++) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    if (world.getBiome(pos) != ModBiomes.BLOOD_SURGING_LAND) continue;

                    IBlockState state = world.getBlockState(pos);
                    // 检查是否是血块
                    if (state.getBlock() == ModBlocks.BLOOD_BLOCK) {
                        // 随机选择一个朝向
                        EnumFacing randomFacing = facings[rand.nextInt(facings.length)];
                        // 设置新的朝向状态
                        IBlockState newState = state.withProperty(com.qiamao.blood.block.BlockBloodBlock.FACING, randomFacing);
                        world.setBlockState(pos, newState, 2);
                    }
                }
            }
        }
    }

    /**
     * 覆盖斜坡侧面的裸露方块
     * 
     * 原理：对于血液翻腾之地群系中的每一列方块，检查其4个水平相邻列。
     * 如果邻居列的地表比当前列高，则邻居列侧面对应于两列高度差之间的区域会暴露在外。
     * 将这些暴露的石头/泥土/草方块替换为肉块，实现类似原版草方块覆盖斜坡的效果。
     */
    private static void capExposedSurfaces(World world, int chunkX, int chunkZ) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        Random rand = new Random(world.getSeed() + chunkX * 654187L + chunkZ * 943211L);

        // 4个水平方向
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos columnPos = new BlockPos(startX + x, 64, startZ + z);
                if (world.getBiome(columnPos) != ModBiomes.BLOOD_SURGING_LAND) {
                    continue;
                }

                // 当前列的地表高度（最上方非空气方块的Y坐标）
                int currentSurface = world.getHeight(startX + x, startZ + z) - 1;
                if (currentSurface < 0) continue;

                for (int[] dir : dirs) {
                    int nx = startX + x + dir[0];
                    int nz = startZ + z + dir[1];
                    
                    // 边界保护
                    if (nx < 0 || nz < 0) continue;

                    // 邻居列的地表高度
                    int neighborSurface = world.getHeight(nx, nz) - 1;
                    if (neighborSurface < 0) continue;

                    // 只有邻居比当前列高时才需要覆盖（邻居列的侧面暴露）
                    if (neighborSurface > currentSurface) {
                        // 两个自然方块替换列表
                        for (int y = currentSurface + 1; y <= neighborSurface; y++) {
                            BlockPos sidePos = new BlockPos(nx, y, nz);
                            IBlockState sideState = world.getBlockState(sidePos);
                            net.minecraft.block.Block sideBlock = sideState.getBlock();

                            // 替换所有原版自然方块
                            if (sideBlock == Blocks.STONE ||
                                sideBlock == Blocks.DIRT ||
                                sideBlock == Blocks.GRASS ||
                                sideBlock == Blocks.GRAVEL ||
                                sideBlock == Blocks.SAND ||
                                sideBlock == Blocks.SANDSTONE ||
                                sideBlock == Blocks.COBBLESTONE ||
                                sideBlock == Blocks.MOSSY_COBBLESTONE) {
                                
                                // 随机朝向，使坡面更自然
                                EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(rand);
                                world.setBlockState(sidePos, ModBlocks.FLESH_CHUNK.getDefaultState()
                                    .withProperty(com.qiamao.blood.block.BlockFleshChunk.FACING, facing), 2);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换指定区块中的静止水和流动水为血液
     * 严格只替换血液翻腾之地群系内部的水，群系外的水保持不变
     */
    private static void replaceWaterWithBlood(World world, int chunkX, int chunkZ) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        // 首先检查区块是否包含任何血液群系区域
        boolean hasBloodBiomeArea = false;
        for (int x = 0; x < 16; x += 4) { // 采样检查，每4格检查一次
            for (int z = 0; z < 16; z += 4) {
                BlockPos checkPos = new BlockPos(startX + x, 64, startZ + z);
                if (world.getBiome(checkPos) == ModBiomes.BLOOD_SURGING_LAND) {
                    hasBloodBiomeArea = true;
                    break;
                }
            }
            if (hasBloodBiomeArea) break;
        }

        if (!hasBloodBiomeArea) return; // 区块完全不包含血液群系，跳过

        // 逐点处理，严格检查每个位置是否在血液群系内
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 先检查这一列是否在血液群系内
                BlockPos columnPos = new BlockPos(startX + x, 64, startZ + z);
                if (world.getBiome(columnPos) != ModBiomes.BLOOD_SURGING_LAND) {
                    continue; // 这一列不在血液群系内，跳过
                }

                for (int y = 0; y < 256; y++) { // 检查整个高度范围
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);

                    // 再次确认该位置在血液群系内（防止群系边界渐变）
                    if (world.getBiome(pos) != ModBiomes.BLOOD_SURGING_LAND) {
                        continue;
                    }

                    IBlockState state = world.getBlockState(pos);

                    // 检查是否是水（包括静止水和流动水）
                    if (state.getMaterial() == Material.WATER) {
                        // 替换为血液流体方块
                        world.setBlockState(pos, ModBlocks.BLOCK_BLOOD.getDefaultState(), 2);
                    }
                }
            }
        }
    }

    /**
     * 区块加载事件 - 确保已生成区块的水被替换
     * 严格只处理血液翻腾之地群系内部的水
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote) return;

        World world = event.getWorld();
        int chunkX = event.getChunk().x;
        int chunkZ = event.getChunk().z;
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        // 采样检查区块是否包含任何血液群系区域
        boolean hasBloodBiomeArea = false;
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                BlockPos checkPos = new BlockPos(startX + x, 64, startZ + z);
                if (world.getBiome(checkPos) == ModBiomes.BLOOD_SURGING_LAND) {
                    hasBloodBiomeArea = true;
                    break;
                }
            }
            if (hasBloodBiomeArea) break;
        }

        if (!hasBloodBiomeArea) return; // 区块不包含血液群系，跳过

        // 执行水替换（内部会逐点检查群系）
        replaceWaterWithBlood(world, chunkX, chunkZ);
    }

    /**
     * 方块放置事件 - 阻止水在血液群系中放置，改为血液
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        if (world.isRemote) return;

        BlockPos pos = event.getPos();
        IBlockState state = event.getPlacedBlock();

        // 检查是否在血液群系
        if (world.getBiome(pos) != ModBiomes.BLOOD_SURGING_LAND) {
            return;
        }

        // 如果放置的是水，替换为血液
        if (state.getMaterial() == Material.WATER) {
            event.setCanceled(true);
            world.setBlockState(pos, ModBlocks.BLOCK_BLOOD.getDefaultState(), 3);
        }
    }

    /**
     * 检查生物是否可以生成 - 只允许本模组生物在血液群系生成
     * 寄生史蒂夫全局只在晚上生成（13000-23000 ticks），不限于血液群系
     */
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        Entity entity = event.getEntity();

        boolean isModEntity = entity.getClass().getName().startsWith("com.qiamao.blood.entity");

        // --- 全局：阻止本模组生物在超平坦自然生成 ---
        if (isModEntity && world.getWorldType() == net.minecraft.world.WorldType.FLAT) {
            // CheckSpawn 事件只在【自然生成】和【刷怪笼生成】时触发！
            // 玩家使用【刷怪蛋】或【/summon 指令】时不会触发此事件。
            // 因此在这里直接 DENY 就可以完美阻止自然生成，同时保留刷怪蛋功能。
            event.setResult(Event.Result.DENY);
            return;
        }

        // 检查是否在血液翻腾之地群系
        if (world.getBiome(pos) != ModBiomes.BLOOD_SURGING_LAND) {
            return; // 不在血液群系，不干预
        }

        if (!isModEntity) {
            // 阻止非本模组生物生成
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * 特殊生成事件检查 - 防止结构生成等其他方式产生生物
     * 寄生史蒂夫全局只在晚上生成（13000-23000 ticks），不限于血液群系
     */
    @SubscribeEvent
    public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        World world = event.getWorld();
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        Entity entity = event.getEntity();

        // 检查是否在血液翻腾之地群系
        if (world.getBiome(pos) != ModBiomes.BLOOD_SURGING_LAND) {
            return;
        }

        // 只允许本模组生物
        boolean isModEntity = entity.getClass().getName().startsWith("com.qiamao.blood.entity");

        if (!isModEntity) {
            event.setCanceled(true);
        }
    }

    /**
     * 实体加入世界事件 - 作为生物生成的最后一道防线
     * 寄生史蒂夫全局只在晚上生成（13000-23000 ticks），不限于血液群系
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (world.isRemote) return; // 只在服务端处理

        Entity entity = event.getEntity();
        if (entity == null) return; // 防御性检查：实体为空则直接返回

        // 跳过玩家和已死亡的实体
        if (entity instanceof net.minecraft.entity.player.EntityPlayer) return;
        if (!entity.isEntityAlive()) return;
        
        // 检查是否是本模组生物
        boolean isModEntity = entity.getClass().getName().startsWith("com.qiamao.blood.entity");

        BlockPos pos = entity.getPosition();
        if (pos == null) return; // 防御性检查：位置为空则直接返回

        // 检查是否在血液翻腾之地群系
        Biome biome = world.getBiome(pos);
        if (biome == null) return; // 防御性检查：群系为空则直接返回
        if (biome != ModBiomes.BLOOD_SURGING_LAND) {
            return; // 不在血液群系，不干预
        }

        if (!isModEntity) {
            // 阻止非本模组生物加入世界
            event.setCanceled(true);
        }
    }

    /**
     * 实体更新事件 - 处理受诅咒的肉块的负面效果
     * 踩在上方持续获得凋零1和反胃，离开马上消失，1格高低差不断续
     */
    @SubscribeEvent
    public static void onLivingUpdate(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
        net.minecraft.entity.EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (world.isRemote) return;

        // 如果是本模组的生物，免疫这个肉块的负面效果
        if (entity.getClass().getName().startsWith("com.qiamao.blood.entity")) {
            return;
        }

        boolean onFlesh = false;
        
        // 检查脚下的方块（取碰撞箱底部向下 0.1 格的位置）
        BlockPos posUnder = new BlockPos(
                net.minecraft.util.math.MathHelper.floor(entity.posX),
                net.minecraft.util.math.MathHelper.floor(entity.getEntityBoundingBox().minY - 0.1D),
                net.minecraft.util.math.MathHelper.floor(entity.posZ)
        );
        
        if (world.getBlockState(posUnder).getBlock() == ModBlocks.BLOOD_SEEKER_FLESH) {
            onFlesh = true;
        }

        if (onFlesh) {
            // 给予凋零 1 (amplifier 0) 和 反胃，持续 30 ticks (1.5秒)
            // 这样玩家跳跃到高 1 格的肉块时，在空中的 10-15 tick 期间，效果不会断掉
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.WITHER, 30, 0, false, false));
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 30, 0, false, false));
        } else if (entity.onGround) {
            // 如果玩家落地，但脚下不是受诅咒的肉块，立即清除这俩效果
            // （前提是效果剩余时间极短，说明是我们刚刚赋予的缓冲，而不是玩家喝药得来的长时间效果）
            net.minecraft.potion.PotionEffect wither = entity.getActivePotionEffect(net.minecraft.init.MobEffects.WITHER);
            if (wither != null && wither.getDuration() <= 30) {
                entity.removePotionEffect(net.minecraft.init.MobEffects.WITHER);
            }
            
            net.minecraft.potion.PotionEffect nausea = entity.getActivePotionEffect(net.minecraft.init.MobEffects.NAUSEA);
            if (nausea != null && nausea.getDuration() <= 30) {
                entity.removePotionEffect(net.minecraft.init.MobEffects.NAUSEA);
            }
        }
    }
}
