package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * 肉块 (Flesh Chunk) - 普通血腥糜烂的肉块方块
 * 与渝血者肉块风格关联但更普通
 * 支持水平四向放置（东南西北）
 */
public class BlockFleshChunk extends Block {

    // 使用 BlockHorizontal.FACING 限制为水平四向（东南西北）
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockFleshChunk(String name) {
        super(Material.CLAY); // 使用CLAY材料，挖掘速度与沙子相同
        // 默认状态设为朝北
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        
        this.setUnlocalizedName(name);
        
        // 挖掘速度与沙子相同 (0.5F)
        this.setHardness(0.5F);
        
        // 爆炸抗性较低
        this.setResistance(1.0F);
        
        // 需要使用镐子类工具挖掘更快
        this.setHarvestLevel("pickaxe", 0); // 木镐即可
        
        // 创建音效类型 - 与渝血者肉块共用踩踏音效
        net.minecraft.block.SoundType customFleshSound = new net.minecraft.block.SoundType(
            1.0F, 1.0F,
            net.minecraft.init.SoundEvents.BLOCK_SLIME_BREAK,      // 破坏音效
            com.qiamao.blood.init.ModSounds.BLOOD_SEEKER_STEP,     // 踩踏音效（与渝血者肉块共用）
            net.minecraft.init.SoundEvents.BLOCK_SLIME_PLACE,      // 放置音效
            net.minecraft.init.SoundEvents.BLOCK_SLIME_HIT,        // 击打音效
            net.minecraft.init.SoundEvents.BLOCK_SLIME_FALL        // 掉落音效
        );
        
        this.setSoundType(customFleshSound);
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
        // 开启随机刻更新，用于检测燃烧状态
        this.setTickRandomly(true);
    }

    /**
     * 随机刻更新逻辑
     * 处理点燃变煤炭和邻近岩浆变煤炭
     */
    @Override
    @SuppressWarnings("null")
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        if (!worldIn.isRemote) {
            // 1. 检查上方是否有点火 (或者直接是火方块)
            if (worldIn.getBlockState(pos.up()).getBlock() == net.minecraft.init.Blocks.FIRE) {
                // 如果上面在烧，有一定概率变成煤炭块 (模拟燃烧一会的过程)
                // 1/3 的概率触发碳化
                if (rand.nextInt(3) == 0) {
                    carbonize(worldIn, pos);
                    return;
                }
            }

            // 2. 检查邻近是否有岩浆
            for (EnumFacing facing : EnumFacing.values()) {
                if (worldIn.getBlockState(pos.offset(facing)).getMaterial() == Material.LAVA) {
                    // 接触岩浆立即（通过随机刻快速反应）变成煤炭块
                    carbonize(worldIn, pos);
                    break;
                }
            }
        }
    }

    /**
     * 当邻近方块改变时触发 (例如岩浆流过来)
     */
    @Override
    @SuppressWarnings("null")
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if (!worldIn.isRemote) {
            // 如果邻近方块变成了岩浆，立即触发检查
            if (worldIn.getBlockState(fromPos).getMaterial() == Material.LAVA) {
                // 岩浆接触是即时的逻辑，但为了平衡性，我们还是稍微检查一下
                carbonize(worldIn, pos);
            }
        }
    }

    /**
     * 执行碳化逻辑：肉块 -> 煤炭块
     */
    @SuppressWarnings("null")
    private void carbonize(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        // 变成煤炭块
        worldIn.setBlockState(pos, net.minecraft.init.Blocks.COAL_BLOCK.getDefaultState());
        // 播放一些烟雾效果和火焰熄灭的声音（表示烧焦了）
        worldIn.playEvent(2001, pos, Block.getStateId(net.minecraft.init.Blocks.COAL_BLOCK.getDefaultState()));
        worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
    }

    /**
     * 实体在方块上行走时触发 - 与渝血者肉块相同的减速效果 (25%减速)
     */
    @Override
    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn) {
        super.onEntityWalk(worldIn, pos, entityIn);
        
        // 物理减速25% - 与渝血者肉块保持一致
        entityIn.motionX *= 0.75D;
        entityIn.motionZ *= 0.75D;
    }

    /**
     * 控制方块破坏时掉落的物品
     */
    @Override
    public net.minecraft.item.Item getItemDropped(@Nonnull IBlockState state, @Nonnull java.util.Random rand, int fortune) {
        // 不掉落自身，可以掉落烂肉
        return com.qiamao.blood.init.ModItems.GORY_FLESH;
    }

    /**
     * 控制掉落物品的数量
     */
    @Override
    public int quantityDropped(@Nonnull java.util.Random random) {
        // 掉落2-4个烂肉
        return 2 + random.nextInt(3);
    }

    /**
     * 控制是否可以被精准采集
     */
    @Override
    protected boolean canSilkHarvest() {
        return false; // 不允许精准采集
    }

    /**
     * 获取方块的粒子效果材质
     * 使用自己的贴图作为粒子效果
     */
    @Override
    @Nonnull
    public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    /**
     * 根据玩家放置方块时的朝向来决定方块的朝向（玩家面对方向的反方向）
     */
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    /**
     * 从元数据中读取状态
     */
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        // 确保只有水平方向，Y轴方向（上下）会被重置为北
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    /**
     * 将状态转换为元数据保存
     */
    @Override
    @SuppressWarnings("null")
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    /**
     * 注册 Block 的属性
     */
    @Override
    @Nonnull
    @SuppressWarnings("null")
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
}
