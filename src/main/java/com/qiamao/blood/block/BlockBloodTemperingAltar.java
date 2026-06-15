package com.qiamao.blood.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import com.qiamao.blood.BloodMod;
import com.qiamao.blood.block.tileentity.TileEntityBloodAltar;
import com.qiamao.blood.client.gui.ModGuiHandler;

public class BlockBloodTemperingAltar extends Block {

    public BlockBloodTemperingAltar(String name) {
        super(Material.ROCK); // 石头材质
        this.setUnlocalizedName(name);
        this.setRegistryName(BloodMod.MODID, name);
        
        // 黑曜石硬度是 50.0F，0.75倍就是 37.5F
        this.setHardness(37.5F); 
        this.setResistance(2000.0F); // 高爆炸抗性，类似黑曜石
        
        // 需要铁镐 (等级2) 及以上才能挖掘掉落
        this.setHarvestLevel("pickaxe", 2); 
        
        // 由于方块不是完整的 1x1x1 立方体，允许光线穿过，避免渲染阴影错误
        this.setLightOpacity(0);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityBloodAltar();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityBloodAltar) {
                playerIn.openGui(BloodMod.instance, ModGuiHandler.BLOOD_ALTAR_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityBloodAltar) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); ++i) {
                    if (!handler.getStackInSlot(i).isEmpty()) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                    }
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    // 告诉渲染引擎这不是一个普通的立方体
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    // 模型有空隙，避免相邻方块的面被剔除
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}