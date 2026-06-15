package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 血液祭坛方块
 * 具有自定义非完整立方体模型，并跟随玩家放置方向旋转
 */
public class BlockBloodAltar extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockBloodAltar() {
        super(Material.ROCK);
        this.setHardness(3.5F);
        this.setResistance(10.0F);
        this.setHarvestLevel("pickaxe", 1);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float y, float hitZ, int meta, EntityLivingBase placer) {
        // 设置放置时的朝向为玩家的反方向（即正面朝向玩家）
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        // 告诉游戏这不是一个完整的不透明立方体，允许光照穿透凹痕
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        // 告诉游戏这不是一个占满 1x1 空间的方块，防止相邻方块面被错误剔除
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        // 使用 CUTOUT 层以确保凹痕边缘清晰
        return BlockRenderLayer.CUTOUT;
    }
}
