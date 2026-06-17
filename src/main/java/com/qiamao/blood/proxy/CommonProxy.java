package com.qiamao.blood.proxy;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.block.tileentity.TileEntityBloodAltar;
import com.qiamao.blood.client.gui.ContainerBloodAltar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    /**
     * 注册 GUI Handler（服务端安全版本）
     * CommonProxy 仅处理 getServerGuiElement，避免加载客户端 GUI 类（如 GuiBloodAltar）
     */
    public void registerGuiHandler() {
        NetworkRegistry.INSTANCE.registerGuiHandler(BloodMod.instance, new IGuiHandler() {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                // 1 = BLOOD_ALTAR_GUI
                if (ID == 1) {
                    net.minecraft.tileentity.TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                    if (te instanceof TileEntityBloodAltar) {
                        return new ContainerBloodAltar(player.inventory, (TileEntityBloodAltar) te);
                    }
                }
                return null;
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                // 服务端不处理客户端 GUI，返回 null 即可
                return null;
            }
        });
    }

    public void spawnBloodDropParticle(net.minecraft.world.World world, double x, double y, double z, double speedX, double speedY, double speedZ) {
        // 服务端不执行任何操作
    }

    /**
     * 将任务调度到对应端的主线程
     * 服务端在服务端线程执行，客户端在客户端线程执行
     */
    public void addScheduledTask(Runnable runnable) {
        // 服务端默认直接由 FMLCommonHandler 或者不执行处理（通常这类同步包只发给客户端）
        // 如果需要服务端处理网络包的，可在这里调用 FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable)
        // 但此方法主要为客户端网络包修复崩溃使用
    }
}
