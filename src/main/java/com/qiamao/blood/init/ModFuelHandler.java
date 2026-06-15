package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 燃料事件处理器
 * 注册模组物品的燃烧时间
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModFuelHandler {

    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();

        // 血腥原木：300 ticks (与原版原木一致)
        if (item == Item.getItemFromBlock(ModBlocks.BLOOD_LOG)) {
            event.setBurnTime(300);
        }
        // 血腥木板：150 ticks (与原版木板一致)
        else if (item == Item.getItemFromBlock(ModBlocks.BLOOD_PLANKS)) {
            event.setBurnTime(150);
        }
        // 血腥树苗：100 ticks (与原版树苗一致)
        else if (item == Item.getItemFromBlock(ModBlocks.BLOOD_SAPLING)) {
            event.setBurnTime(100);
        }
    }
}
