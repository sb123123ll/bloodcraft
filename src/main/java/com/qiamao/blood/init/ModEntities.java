package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityBloodEndermite;
import com.qiamao.blood.entity.EntityBloodSeeker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModEntities {

    private static int entityId = 0;

    @SubscribeEvent
    public static void onLivingSetAttackTarget(net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent event) {
        // 铁傀儡的特殊仇恨逻辑
        if (event.getEntityLiving() instanceof net.minecraft.entity.monster.EntityIronGolem) {
            // 铁傀儡绝对不要主动攻击血螨
            if (event.getTarget() instanceof EntityBloodEndermite) {
                ((net.minecraft.entity.monster.EntityIronGolem) event.getEntityLiving()).setAttackTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
        // 当铁傀儡加入世界时，给它添加主动攻击血螨脑控和多足虫的 AI
        if (event.getEntity() instanceof net.minecraft.entity.monster.EntityIronGolem) {
            net.minecraft.entity.monster.EntityIronGolem golem = (net.minecraft.entity.monster.EntityIronGolem) event.getEntity();
            
            // 铁傀儡主动攻击血螨脑控
            golem.targetTasks.addTask(2, new net.minecraft.entity.ai.EntityAINearestAttackableTarget<>(golem, com.qiamao.blood.entity.EntityParasiticSteve.class, false));
            // 铁傀儡主动攻击多足嗜血虫
            golem.targetTasks.addTask(2, new net.minecraft.entity.ai.EntityAINearestAttackableTarget<>(golem, com.qiamao.blood.entity.EntityBloodSeeker.class, false));
        }
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        // 注册 多足嗜血虫 实体 (Centipede Blood Seeker / 多足嗜血虫)
        EntityEntry bloodSeeker = EntityEntryBuilder.create()
                .entity(EntityBloodSeeker.class)
                .id(new ResourceLocation(BloodMod.MODID, "centipede_blood_seeker"), entityId++)
                .name(BloodMod.MODID + ".centipede_blood_seeker")
                .tracker(80, 3, true)
                .egg(0x8B0000, 0xC3B091)
                .build();

        event.getRegistry().register(bloodSeeker);

        // 注册 嗜血多足幼虫 (Centipede Blood Seeker Larva)
        EntityEntry bloodSeekerLarva = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityBloodSeekerLarva.class)
                .id(new ResourceLocation(BloodMod.MODID, "centipede_blood_seeker_larva"), entityId++)
                .name(BloodMod.MODID + ".centipede_blood_seeker_larva")
                .tracker(80, 3, true)
                .egg(0x8B0000, 0xE5D6C1) // 蛋颜色稍微浅一点点，以区分成虫
                .build();

        event.getRegistry().register(bloodSeekerLarva);

        // 注册 血螨 (Blood Mite)
        EntityEntry bloodMite = EntityEntryBuilder.create()
                .entity(EntityBloodEndermite.class)
                .id(new ResourceLocation(BloodMod.MODID, "blood_mite"), entityId++)
                .name(BloodMod.MODID + ".blood_mite")
                .tracker(64, 3, true)
                .egg(0x1a0505, 0xffffff) // 极暗的血黑色底色，纯白色斑点
                .build();

        event.getRegistry().register(bloodMite);

        // 注册 寄生史蒂夫 (Parasitic Steve) - 刷怪蛋颜色改为血肉主题：底色深红 (0x550000)，斑点惨白骨色 (0xD0C8B4)
        EntityEntry parasiticSteve = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityParasiticSteve.class)
                .id(new ResourceLocation(BloodMod.MODID, "parasitic_steve"), entityId++)
                .name(BloodMod.MODID + ".parasitic_steve")
                .tracker(80, 3, true)
                .egg(0x550000, 0xD0C8B4) 
                .build();

        event.getRegistry().register(parasiticSteve);

        // 注册 投掷血螨 (Thrown Blood Mite)
        EntityEntry thrownBloodMite = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityThrownBloodMite.class)
                .id(new ResourceLocation(BloodMod.MODID, "thrown_blood_mite"), entityId++)
                .name(BloodMod.MODID + ".thrown_blood_mite")
                .tracker(64, 10, true) // 投掷物更新频率需要高一点(10)
                // 必须为抛射物提供一个虚假的刷怪蛋或者明确告诉Forge这是一个实体
                // 如果不指定，有时候会在客户端引发渲染同步问题
                .build();

        event.getRegistry().register(thrownBloodMite);

        // 注册 喷溅/滞留瓶装血液实体
        EntityEntry splashBlood = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntitySplashBlood.class)
                .id(new ResourceLocation(BloodMod.MODID, "splash_blood"), entityId++)
                .name(BloodMod.MODID + ".splash_blood")
                .tracker(64, 10, true)
                .build();
        event.getRegistry().register(splashBlood);

        // 注册 毒泡泡 (Poison Bubble)
        EntityEntry poisonBubble = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityPoisonBubble.class)
                .id(new ResourceLocation(BloodMod.MODID, "poison_bubble"), entityId++)
                .name(BloodMod.MODID + ".poison_bubble")
                .tracker(64, 10, true)
                .build();

        event.getRegistry().register(poisonBubble);

        // 注册 血刺 (Blood Spike) - 多足嗜血虫的远程投射物
        EntityEntry bloodSpike = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityBloodSpike.class)
                .id(new ResourceLocation(BloodMod.MODID, "blood_spike"), entityId++)
                .name(BloodMod.MODID + ".blood_spike")
                .tracker(64, 10, true)
                .build();

        event.getRegistry().register(bloodSpike);

        // 注册 血液母体 (Blood Mother) - 放大15倍的血螨
        EntityEntry bloodMother = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityBloodMother.class)
                .id(new ResourceLocation(BloodMod.MODID, "blood_mother"), entityId++)
                .name(BloodMod.MODID + ".blood_mother")
                .tracker(80, 3, true)
                .egg(0x8B0000, 0xC3B091)
                .build();

        event.getRegistry().register(bloodMother);

        // 注册 邪教传教士 (Cultist Preacher) - 使用原版村民模型
        EntityEntry cultistPreacher = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityCultistPreacher.class)
                .id(new ResourceLocation(BloodMod.MODID, "cultist_preacher"), entityId++)
                .name(BloodMod.MODID + ".cultist_preacher")
                .tracker(80, 3, true)
                .egg(0x4A0404, 0xD4AF37)
                .build();

        event.getRegistry().register(cultistPreacher);

        // 注册 血液猎犬 (Blood Hound)
        EntityEntry bloodHound = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityBloodHound.class)
                .id(new ResourceLocation(BloodMod.MODID, "blood_hound"), entityId++)
                .name(BloodMod.MODID + ".blood_hound")
                .tracker(80, 3, true)
                .egg(0x8B0000, 0x555555) // 深红色和深灰色
                .build();

        event.getRegistry().register(bloodHound);

        // 注册 视神经 (Optic Nerve)
        EntityEntry opticNerve = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityOpticNerve.class)
                .id(new ResourceLocation(BloodMod.MODID, "optic_nerve"), entityId++)
                .name(BloodMod.MODID + ".optic_nerve")
                .tracker(80, 3, true)
                .egg(0x7c0000, 0xffcccc) // 暗红底色，浅粉斑点
                .build();

        event.getRegistry().register(opticNerve);

        // 注册 迫近者 (Approacher)
        EntityEntry approacher = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityApproacher.class)
                .id(new ResourceLocation(BloodMod.MODID, "approacher"), entityId++)
                .name(BloodMod.MODID + ".approacher")
                .tracker(80, 3, true)
                .egg(0x2a3b2a, 0x5a7a5a) // 暗绿色底色，浅暗绿斑点
                .build();

        event.getRegistry().register(approacher);

        // 注册 吸血线虫 (Blood Nematode)
        EntityEntry bloodNematode = EntityEntryBuilder.create()
                .entity(com.qiamao.blood.entity.EntityBloodNematode.class)
                .id(new ResourceLocation(BloodMod.MODID, "blood_nematode"), entityId++)
                .name(BloodMod.MODID + ".blood_nematode")
                .tracker(80, 3, true)
                .egg(0x8a1a1a, 0x3d0000) // 血液颜色和深红色
                .build();

        event.getRegistry().register(bloodNematode);
    }
}
