package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockBloodSeekerFlesh extends Block {

    // 使用 BlockHorizontal.FACING 提供水平方向的四个朝向（东南西北）
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBloodSeekerFlesh(String name) {
        super(Material.SPONGE);
        // 默认状态设为朝北
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setRegistryName(com.qiamao.blood.BloodMod.MODID, name);
        this.setUnlocalizedName(name);
        
        // 原版黑曜石硬度为 50.0F，75% 就是 37.5F
        // 这将使方块极难挖掘（普通稿子几乎挖不动，需要高等级稿子和很长时间）
        this.setHardness(37.5F);
        // 同时提高爆炸抗性（黑曜石的抗性是 2000.0F，我们给它一个比较高的抗性防止被苦力怕轻易炸毁）
        this.setResistance(1500.0F);
        
        // 设置只能用镐子挖掘，并且挖掘等级为 3 (钻石镐级别，和黑曜石一样)
        this.setHarvestLevel("pickaxe", 3);
        
        // 创建一个自定义的 SoundType，使用专属的踩踏音效
        net.minecraft.block.SoundType customFleshSound = new net.minecraft.block.SoundType(
            1.0F, 1.0F, 
            net.minecraft.init.SoundEvents.BLOCK_SLIME_BREAK, 
            com.qiamao.blood.init.ModSounds.BLOOD_SEEKER_STEP, // 专属的踩踏声
            net.minecraft.init.SoundEvents.BLOCK_SLIME_PLACE, 
            net.minecraft.init.SoundEvents.BLOCK_SLIME_HIT, 
            net.minecraft.init.SoundEvents.BLOCK_SLIME_FALL
        );
        
        this.setSoundType(customFleshSound);
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
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
     * Forge 提供的钩子：完全接管实体在这个方块上的移动声
     * 返回 true 意味着 "我已经处理了声音，原版你别再放了"
     */
    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull net.minecraft.world.WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int numberOfParticles) {
        // 返回 false 让原版处理掉落粒子，我们只关心脚步声，所以不用管这里
        return false;
    }

    /**
     * 实体在方块上行走时触发
     */
    @Override
    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull net.minecraft.entity.Entity entityIn) {
        super.onEntityWalk(worldIn, pos, entityIn);
        
        // 物理减速机制：当实体在肉块上行走时，将其速度降低 25% (即乘以 0.75)
        // 这种减速是直接修改实体的物理运动向量，不会给玩家添加任何药水效果图标
        // 原版灵魂沙和黏液块也是通过这种方式或者 slipperiness 来实现的
        entityIn.motionX *= 0.75D;
        entityIn.motionZ *= 0.75D;
    }

    /**
     * 控制正常破坏方块时掉落的物品
     * 返回 null 或 空物品代表不掉落
     */
    @Override
    public net.minecraft.item.Item getItemDropped(@Nonnull IBlockState state, @Nonnull java.util.Random rand, int fortune) {
        return null; // 默认不掉落任何东西
    }

    /**
     * 控制掉落物品的数量
     */
    @Override
    public int quantityDropped(@Nonnull java.util.Random random) {
        return 0; // 默认掉落数量为 0
    }

    /**
     * 控制是否可以被精准采集（Silk Touch）
     */
    @Override
    protected boolean canSilkHarvest() {
        return true; // 允许精准采集
    }

    /**
     * 玩家右键点击方块时触发
     */
    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull net.minecraft.entity.player.EntityPlayer playerIn, @Nonnull net.minecraft.util.EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        net.minecraft.item.ItemStack heldItem = playerIn.getHeldItem(hand);

        // 防御性检查：如果手持物品为空，不处理
        if (heldItem.isEmpty()) {
            return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }

        // 如果玩家手里拿着原版的空玻璃瓶
        if (heldItem.getItem() == net.minecraft.init.Items.GLASS_BOTTLE) {
            if (!worldIn.isRemote) {
                // 消耗一个玻璃瓶
                if (!playerIn.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                }

                // 给玩家一瓶血液
                net.minecraft.item.ItemStack bloodBottle = new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.BLOOD_BOTTLE);
                if (heldItem.isEmpty()) {
                    playerIn.setHeldItem(hand, bloodBottle);
                } else if (!playerIn.inventory.addItemStackToInventory(bloodBottle)) {
                    playerIn.dropItem(bloodBottle, false);
                }

                // 播放方块碎裂粒子效果
                // 2001 是原版破坏方块产生粒子的事件 ID，Block.getStateId() 用来指定使用哪种方块的粒子
                worldIn.playEvent(2001, pos, Block.getStateId(state));

                // 获取当前渝血者肉块的朝向
                EnumFacing fleshFacing = state.getValue(FACING);

                // 将当前方块替换为原版的骷髅头方块 (Skeleton Skull)
                // 骷髅头的 FACING 属性：UP 表示放在地上（会有额外的旋转元数据），水平方向表示贴在墙上。
                // 渝血者肉块的 FACING 也是水平方向（东南西北）。
                // 为了让地上的骷髅头也能朝向正确的方向，我们需要在设置 BlockState 之后，修改其 TileEntity 的旋转角度。
                worldIn.setBlockState(pos, net.minecraft.init.Blocks.SKULL.getDefaultState().withProperty(net.minecraft.block.BlockSkull.FACING, net.minecraft.util.EnumFacing.UP), 3);
                
                // 设置 TileEntity 为普通骷髅头并同步旋转角度
                net.minecraft.tileentity.TileEntity tileentity = worldIn.getTileEntity(pos);
                if (tileentity instanceof net.minecraft.tileentity.TileEntitySkull) {
                    net.minecraft.tileentity.TileEntitySkull skullTe = (net.minecraft.tileentity.TileEntitySkull) tileentity;
                    skullTe.setType(0); // 0 代表普通的骨头骷髅
                    
                    // 将 EnumFacing (东南西北) 转换为骷髅头的旋转值 (0-15)
                    // 修正朝向：刚才的方向是反的，骷髅头 0 对应南（面朝北），8 对应北（面朝南）。
                    // 我们将旋转值翻转 180 度（+8 或 -8）来让它正面朝向我们原本预期的方向。
                    int rotation = 0;
                    switch (fleshFacing) {
                        case NORTH: rotation = 0; break; // 修正后：面朝北
                        case SOUTH: rotation = 8; break; // 修正后：面朝南
                        case WEST:  rotation = 12; break; // 修正后：面朝西
                        case EAST:  rotation = 4; break; // 修正后：面朝东
                        default: rotation = 0; break;
                    }
                    skullTe.setSkullRotation(rotation);
                }

                // 播放专属的采血音效（血肉被挤压、抽取的恐怖声）
                worldIn.playSound(null, pos, com.qiamao.blood.init.ModSounds.BLOOD_SEEKER_EXTRACT, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 0.9F + worldIn.rand.nextFloat() * 0.2F);

                // 100% 的概率生成 3-4 只血螨
                int miteCount = 3 + worldIn.rand.nextInt(2); // 生成 3 或 4 只 (3 + [0,1])
                for (int i = 0; i < miteCount; i++) {
                    com.qiamao.blood.entity.EntityBloodEndermite mite = new com.qiamao.blood.entity.EntityBloodEndermite(worldIn);
                    // 在方块中心稍微偏上的位置生成，增加随机偏移避免重叠
                    double spawnX = pos.getX() + 0.5D + (worldIn.rand.nextDouble() - 0.5D) * 0.5D;
                    double spawnY = pos.getY() + 0.5D;
                    double spawnZ = pos.getZ() + 0.5D + (worldIn.rand.nextDouble() - 0.5D) * 0.5D;
                    mite.setLocationAndAngles(spawnX, spawnY, spawnZ, worldIn.rand.nextFloat() * 360.0F, 0.0F);
                    worldIn.spawnEntity(mite);
                }
            }
            return true; // 返回 true 表示处理了右键事件，不再继续传递
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
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