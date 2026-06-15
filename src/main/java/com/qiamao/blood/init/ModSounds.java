package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 模组音效注册类
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModSounds {

    // 倒出血液（清空桶）的音效
    public static final SoundEvent BUCKET_EMPTY_BLOOD = new SoundEvent(new ResourceLocation(BloodMod.MODID, "bucket_empty_blood"))
            .setRegistryName(BloodMod.MODID, "bucket_empty_blood");

    // 装起血液（填满桶）的音效
    public static final SoundEvent BUCKET_FILL_BLOOD = new SoundEvent(new ResourceLocation(BloodMod.MODID, "bucket_fill_blood"))
            .setRegistryName(BloodMod.MODID, "bucket_fill_blood");

    // 饮用血液瓶时的音效
    public static final SoundEvent DRINK_BLOOD = new SoundEvent(new ResourceLocation(BloodMod.MODID, "drink_blood"))
            .setRegistryName(BloodMod.MODID, "drink_blood");

    // 行走在渝血者肉块上的音效
    public static final SoundEvent BLOOD_SEEKER_STEP = new SoundEvent(new ResourceLocation(BloodMod.MODID, "blood_seeker_step"))
            .setRegistryName(BloodMod.MODID, "blood_seeker_step");

    // 玻璃瓶右键榨取渝血者肉块的音效
    public static final SoundEvent BLOOD_SEEKER_EXTRACT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "blood_seeker_extract"))
            .setRegistryName(BloodMod.MODID, "blood_seeker_extract");

    // 多足嗜血虫的音效
    public static final SoundEvent CENTIPEDE_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.centipede.ambient");
            
    public static final SoundEvent CENTIPEDE_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.centipede.hurt");
            
    public static final SoundEvent CENTIPEDE_DEATH = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede.death"))
            .setRegistryName(BloodMod.MODID, "entity.centipede.death");

    // 多足嗜血幼虫的音效
    public static final SoundEvent CENTIPEDE_LARVA_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede_larva.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.centipede_larva.ambient");
            
    public static final SoundEvent CENTIPEDE_LARVA_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede_larva.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.centipede_larva.hurt");
            
    public static final SoundEvent CENTIPEDE_LARVA_DEATH = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.centipede_larva.death"))
            .setRegistryName(BloodMod.MODID, "entity.centipede_larva.death");

    // 血螨的音效
    public static final SoundEvent BLOOD_MITE_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mite.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mite.ambient");
            
    public static final SoundEvent BLOOD_MITE_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mite.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mite.hurt");
            
    public static final SoundEvent BLOOD_MITE_DEATH = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mite.death"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mite.death");
            
    public static final SoundEvent BLOOD_MITE_STEP = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mite.step"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mite.step");

    // 螨虫脑控 (Parasitic Steve) 的音效
    public static final SoundEvent PARASITIC_STEVE_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.parasitic_steve.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.parasitic_steve.ambient");
            
    public static final SoundEvent PARASITIC_STEVE_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.parasitic_steve.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.parasitic_steve.hurt");

    // 血液母体 (Blood Mother) 的音效
    public static final SoundEvent BLOOD_MOTHER_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mother.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mother.ambient");
            
    public static final SoundEvent BLOOD_MOTHER_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mother.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mother.hurt");
            
    public static final SoundEvent BLOOD_MOTHER_DEATH = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mother.death"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mother.death");
            
    public static final SoundEvent BLOOD_MOTHER_STEP = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.blood_mother.step"))
            .setRegistryName(BloodMod.MODID, "entity.blood_mother.step");

    // 邪教传教士 (Cultist Preacher) 的音效
    public static final SoundEvent CULTIST_PREACHER_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.cultist_preacher.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.cultist_preacher.ambient");
            
    public static final SoundEvent CULTIST_PREACHER_HURT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.cultist_preacher.hurt"))
            .setRegistryName(BloodMod.MODID, "entity.cultist_preacher.hurt");
            
    public static final SoundEvent CULTIST_PREACHER_DEATH = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.cultist_preacher.death"))
            .setRegistryName(BloodMod.MODID, "entity.cultist_preacher.death");

    // 欲望事件音效
    public static final SoundEvent DESIRE_EVENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "event.desire"))
            .setRegistryName(BloodMod.MODID, "event.desire");

    // 视神经 (Optic Nerve) 的音效
    public static final SoundEvent OPTIC_NERVE_AMBIENT = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.optic_nerve.ambient"))
            .setRegistryName(BloodMod.MODID, "entity.optic_nerve.ambient");
            
    public static final SoundEvent OPTIC_NERVE_TARGET = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.optic_nerve.target"))
            .setRegistryName(BloodMod.MODID, "entity.optic_nerve.target");
            
    public static final SoundEvent OPTIC_NERVE_STEP = new SoundEvent(new ResourceLocation(BloodMod.MODID, "entity.optic_nerve.step"))
            .setRegistryName(BloodMod.MODID, "entity.optic_nerve.step");

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
            BUCKET_EMPTY_BLOOD, 
            BUCKET_FILL_BLOOD, 
            DRINK_BLOOD, 
            BLOOD_SEEKER_STEP, 
            BLOOD_SEEKER_EXTRACT,
            CENTIPEDE_AMBIENT,
            CENTIPEDE_HURT,
            CENTIPEDE_DEATH,
            CENTIPEDE_LARVA_AMBIENT,
            CENTIPEDE_LARVA_HURT,
            CENTIPEDE_LARVA_DEATH,
            BLOOD_MITE_AMBIENT,
            BLOOD_MITE_HURT,
            BLOOD_MITE_DEATH,
            BLOOD_MITE_STEP,
            PARASITIC_STEVE_AMBIENT,
            PARASITIC_STEVE_HURT,
            BLOOD_MOTHER_AMBIENT,
            BLOOD_MOTHER_HURT,
            BLOOD_MOTHER_DEATH,
            BLOOD_MOTHER_STEP,
            CULTIST_PREACHER_AMBIENT,
            CULTIST_PREACHER_HURT,
            CULTIST_PREACHER_DEATH,
            DESIRE_EVENT,
            OPTIC_NERVE_AMBIENT,
            OPTIC_NERVE_TARGET,
            OPTIC_NERVE_STEP
        );
    }
}
