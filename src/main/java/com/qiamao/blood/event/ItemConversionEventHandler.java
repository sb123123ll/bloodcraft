package com.qiamao.blood.event;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

/**
 * 物品转化事件处理器
 * 负责处理物品在特定环境下的转化逻辑
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ItemConversionEventHandler {

    /**
     * 世界Tick事件 - 处理树苗在血液中的转化
     * 为减少性能消耗，每10 tick（0.5秒）执行一次扫描
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }

        World world = event.world;
        
        // 每 10 tick 检查一次，优化性能
        if (world.getTotalWorldTime() % 10 != 0) {
            return;
        }

        List<Entity> loadedEntityList = world.loadedEntityList;
        Item bloodSaplingItem = Item.getItemFromBlock(ModBlocks.BLOOD_SAPLING);
        
        for (int i = 0; i < loadedEntityList.size(); i++) {
            Entity entity = loadedEntityList.get(i);
            
            if (entity instanceof EntityItem && entity.isEntityAlive()) {
                EntityItem entityItem = (EntityItem) entity;
                ItemStack stack = entityItem.getItem();
                
                if (stack.isEmpty()) {
                    continue;
                }

                // 获取实体所在位置的方块状态
                BlockPos pos = new BlockPos(entityItem);
                IBlockState state = world.getBlockState(pos);
                
                // 检查是否在血液方块中
                if (state.getBlock() == ModBlocks.BLOCK_BLOOD) {
                    
                    // 如果已经是血腥树苗，则跳过
                    if (stack.getItem() == bloodSaplingItem) {
                        continue;
                    }
                    
                    // 检查是否是树苗 (通过矿物词典 treeSapling)
                    boolean isSapling = false;
                    int[] oreIDs = OreDictionary.getOreIDs(stack);
                    for (int id : oreIDs) {
                        if ("treeSapling".equals(OreDictionary.getOreName(id))) {
                            isSapling = true;
                            break;
                        }
                    }
                    
                    // 如果是树苗，执行转化
                    if (isSapling) {
                        int count = stack.getCount();
                        ItemStack newStack = new ItemStack(bloodSaplingItem, count);
                        
                        // 替换掉落物的内容
                        entityItem.setItem(newStack);
                        
                        // 播放粒子效果
                        if (world instanceof WorldServer) {
                            ((WorldServer) world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, 
                                entityItem.posX, entityItem.posY + 0.5D, entityItem.posZ, 
                                10, 0.2D, 0.2D, 0.2D, 0.05D);
                        }
                    }
                }
            }
        }
    }
}
