package com.qiamao.blood.event;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.init.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 处理恐惧效果的状态记录与恢复
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class PotionEventHandler {

    private static final String NBT_KEY_HAS_FEAR = "BloodCraft_HasFear";
    private static final String NBT_KEY_PRE_FEAR_HEALTH = "BloodCraft_PreFearHealth";

    /**
     * 当恐惧效果被添加时记录原始血量
     * 此时属性修改器尚未应用，可以正确记录获得效果前的血量
     */
    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        // 只处理恐惧效果
        if (event.getPotionEffect().getPotion() == ModPotions.FEAR) {
            NBTTagCompound nbt = entity.getEntityData();
            nbt.setBoolean(NBT_KEY_HAS_FEAR, true);
            // 关键：在属性修改器应用前记录原始血量
            nbt.setFloat(NBT_KEY_PRE_FEAR_HEALTH, entity.getHealth());
        }
    }
}
