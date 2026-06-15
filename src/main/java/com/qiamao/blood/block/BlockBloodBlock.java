package com.qiamao.blood.block;

import com.qiamao.blood.BloodMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 血块方块 - 用于血液翻腾之地群系的地面和地下填充
 * 支持水平四向放置（东南西北）
 */
public class BlockBloodBlock extends Block {

    // 水平四向朝向属性
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBloodBlock(String name) {
        super(Material.GROUND);
        setSoundType(SoundType.SLIME); // 黏糊糊的声音
        setHardness(0.6F);
        setResistance(1.0F);
        setHarvestLevel("shovel", 0);
        setRegistryName(BloodMod.MODID, name);
        setUnlocalizedName(name);
        // 显式不设置创造标签页，或者设置为 null 以从创造模式物品栏中隐藏
        setCreativeTab(null);
        // 血红色调
        setLightOpacity(255);
        // 默认朝向北
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    /**
     * 玩家放置时根据朝向设置
     */
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }
}
