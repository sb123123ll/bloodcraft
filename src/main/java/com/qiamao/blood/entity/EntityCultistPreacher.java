package com.qiamao.blood.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import com.qiamao.blood.init.ModSounds;
import com.qiamao.blood.entity.ai.EntityAIBreakIronDoor;

/**
 * 邪教传教士
 * 使用原版村民模型
 */
public class EntityCultistPreacher extends EntityMob {

    public EntityCultistPreacher(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F); // 标准史蒂夫大小
        if (this.getNavigator() instanceof PathNavigateGround) {
            ((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
            ((PathNavigateGround)this.getNavigator()).setEnterDoors(true);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        
        // 强拆铁门AI
        this.tasks.addTask(1, new EntityAIBreakIronDoor(this));
        // 开关木门AI
        this.tasks.addTask(2, new EntityAIOpenDoor(this, true));
        
        // 近战攻击: 速度倍率设为 1.0D，配合 0.23D 的基础速度，使其索敌和追击速度与原版僵尸一模一样
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, false));
        
        // 闲时移动: 速度倍率设为 1.0D
        this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));

        // 仇恨目标：玩家、村民、铁傀儡、雪傀儡
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntitySnowman.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // 17.5颗心（35血）
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(35.0D);
        // 闲时移动速度与僵尸一致 (原版僵尸的基础速度是0.23D)
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        // 攻击力：3颗心（6点伤害）
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        // 索敌范围：16格
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        // 移除了动态修改 MOVEMENT_SPEED 的逻辑。
        // 现在直接依赖 AI 系统的倍率：闲逛 AI (EntityAIWander) 倍率为 1.0D，
        // 近战攻击 AI (EntityAIAttackMelee) 原本是 1.3D，我现在已将其下调，使其实际追击速度与原版僵尸追击完全一致。
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CULTIST_PREACHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.CULTIST_PREACHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CULTIST_PREACHER_DEATH;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (!this.world.isRemote) {
            // 检查是否被怪物骨斧击杀以掉落头颅
            if (cause.getTrueSource() instanceof net.minecraft.entity.player.EntityPlayer) {
                net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) cause.getTrueSource();
                net.minecraft.item.ItemStack heldItem = player.getHeldItemMainhand();
                
                if (!heldItem.isEmpty() && heldItem.getItem() == com.qiamao.blood.init.ModItems.MONSTER_BONE_AXE) {
                    // 30%概率掉落头颅
                    if (this.rand.nextFloat() < 0.3F) {
                        this.entityDropItem(new net.minecraft.item.ItemStack(com.qiamao.blood.init.ModItems.PREACHER_HEAD_ITEM), 0.0F);
                    }
                }
            }

            // 44% 概率触发恐惧效果
            if (this.rand.nextFloat() < 0.44F) {
                net.minecraft.entity.Entity killer = cause.getTrueSource();
                if (killer instanceof EntityLivingBase) {
                    // 38 到 45 秒随机时间 (38 + [0, 7])
                    int durationSeconds = 38 + this.rand.nextInt(8);
                    int durationTicks = durationSeconds * 20;
                    
                    ((EntityLivingBase) killer).addPotionEffect(new net.minecraft.potion.PotionEffect(com.qiamao.blood.init.ModPotions.FEAR, durationTicks));

                    // 如果击杀者是玩家，解锁成就
                    if (killer instanceof net.minecraft.entity.player.EntityPlayerMP) {
                        net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) killer;
                        net.minecraft.advancements.Advancement advancement = player.getServerWorld().getAdvancementManager().getAdvancement(new net.minecraft.util.ResourceLocation(com.qiamao.blood.BloodMod.MODID, "fear_preacher"));
                        if (advancement != null) {
                            net.minecraft.advancements.AdvancementProgress progress = player.getAdvancements().getProgress(advancement);
                            if (!progress.isDone()) {
                                for (String criterion : progress.getRemaningCriteria()) {
                                    player.getAdvancements().grantCriterion(advancement, criterion);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        // 击杀掉落4点经验
        return 4;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        // 绝对会掉落2到3个毒刺粉
        int powderCount = 2 + this.rand.nextInt(2); // 2 + [0, 1] = 2 到 3
        this.dropItem(com.qiamao.blood.init.ModItems.STINGER_POWDER, powderCount);

        // 绝对会掉落血螨投掷物
        this.dropItem(com.qiamao.blood.init.ModItems.THROWN_BLOOD_MITE_ITEM, 1);

        // 4%概率掉落怨瞳
        float dropChance = 0.04F + (lootingModifier * 0.01F);
        if (this.rand.nextFloat() < dropChance) {
            this.dropItem(com.qiamao.blood.init.ModItems.PLUCKED_EYE, 1);
        }

        // 4.5% 概率掉落毒刺核心碎片
        float fragmentDropChance = 0.045F + (lootingModifier * 0.01F);
        if (this.rand.nextFloat() < fragmentDropChance) {
            this.dropItem(com.qiamao.blood.init.ModItems.STING_CORE_FRAGMENT, 1);
        }
    }

    @Override
    public boolean canDespawn() {
        // 作为结构的守卫，不自动消失
        return false;
    }
}
