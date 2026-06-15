package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.potion.PotionFear;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 药水效果注册类
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModPotions {

    public static final Potion FEAR = new PotionFear();

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(FEAR);
    }
}
