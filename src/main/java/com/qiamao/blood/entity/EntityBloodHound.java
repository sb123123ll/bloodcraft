package com.qiamao.blood.entity;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.World;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 血液猎犬
 * 暂时使用原版狼的模型和纹理
 */
public class EntityBloodHound extends EntityWolf {

    public EntityBloodHound(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.85F);
        // 默认处于激怒状态
        this.setAngry(true);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new net.minecraft.entity.ai.EntityAISit(this);
        this.tasks.addTask(1, new net.minecraft.entity.ai.EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new net.minecraft.entity.ai.EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(4, new net.minecraft.entity.ai.EntityAIAttackMelee(this, 1.35D, true)); // 奔跑速度是原版的1.35倍
        this.tasks.addTask(5, new net.minecraft.entity.ai.EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(6, new net.minecraft.entity.ai.EntityAIWanderAvoidWater(this, 1.0D)); // 闲时移动，速度和原版狼一样
        this.tasks.addTask(7, new net.minecraft.entity.ai.EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new net.minecraft.entity.ai.EntityAILookIdle(this));

        this.targetTasks.addTask(1, new net.minecraft.entity.ai.EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new net.minecraft.entity.ai.EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new net.minecraft.entity.ai.EntityAIHurtByTarget(this, true, new Class[0]));
        
        // 只有未被驯服时才主动攻击玩家
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityBloodHound.this.isTamed() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(5, new EntityAINearestAttackableTarget<>(this, com.qiamao.blood.entity.EntityBloodEndermite.class, true));
    }

    // 判断是否为所有肉类（包括原版狼喜欢的肉和模组添加的肉）
    private boolean isAnyMeat(net.minecraft.item.ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof net.minecraft.item.ItemFood)) {
            return false;
        }
        net.minecraft.item.ItemFood itemfood = (net.minecraft.item.ItemFood)stack.getItem();
        
        // 原版狼喜欢的肉 (isWolfsFavoriteMeat) 包括了所有原版的肉类
        if (itemfood.isWolfsFavoriteMeat()) {
            return true;
        }
        
        // 模组自定义的肉类
        return itemfood == com.qiamao.blood.init.ModItems.GORY_FLESH || 
               itemfood == com.qiamao.blood.init.ModItems.COOKED_GORY_FLESH || 
               itemfood == com.qiamao.blood.init.ModItems.HUMAN_HEART || 
               itemfood == com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART;
    }

    @Override
    public boolean processInteract(EntityPlayer player, net.minecraft.util.EnumHand hand) {
        net.minecraft.item.ItemStack itemstack = player.getHeldItem(hand);

        if (this.isTamed()) {
            if (!itemstack.isEmpty()) {
                if (this.isAnyMeat(itemstack)) {
                    net.minecraft.item.ItemFood itemfood = (net.minecraft.item.ItemFood)itemstack.getItem();
                    
                    if (this.getHealth() < this.getMaxHealth()) {
                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                        this.heal((float)itemfood.getHealAmount(itemstack));
                        
                        // 熟心脏buff
                        if (itemfood == com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART) {
                            this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 200, 1)); // 生命恢复 II, 10秒
                            this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 400, 0)); // 力量 I, 20秒
                            this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 400, 0)); // 抗性提升 I, 20秒
                        }
                        return true;
                    } else if (itemfood == com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART) {
                        // 即使满血也可以喂食熟心脏来获取Buff
                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 200, 1));
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 400, 0));
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 400, 0));
                        return true;
                    }
                }
                else if (itemstack.getItem() == net.minecraft.init.Items.DYE) {
                    // 染料给项圈染色
                    net.minecraft.item.EnumDyeColor enumdyecolor = net.minecraft.item.EnumDyeColor.byDyeDamage(itemstack.getMetadata());
                    if (enumdyecolor != this.getCollarColor()) {
                        this.setCollarColor(enumdyecolor);
                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                        return true;
                    }
                }
            }

            if (this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
                this.aiSit.setSitting(!this.isSitting());
                this.isJumping = false;
                this.navigator.clearPath();
                this.setAttackTarget((net.minecraft.entity.EntityLivingBase)null);
            }
        }
        else if (this.isAnyMeat(itemstack)) {
            // 用任何肉驯服，无论是否处于激怒状态都可以驯服
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            if (!this.world.isRemote) {
                // 概率和原版用骨头驯服狼一样 (1/3)
                if (this.rand.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.setTamedBy(player);
                    this.navigator.clearPath();
                    this.setAttackTarget((net.minecraft.entity.EntityLivingBase)null);
                    this.setAngry(false);
                    this.aiSit.setSitting(true);
                    this.setHealth(this.getMaxHealth()); // 驯服后回满血
                    this.playTameEffect(true);
                    this.world.setEntityState(this, (byte)7);
                    
                    // 如果是用熟心脏驯服的，驯服时也给Buff
                    if (itemstack.getItem() == com.qiamao.blood.init.ModItems.COOKED_HUMAN_HEART) {
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 200, 1));
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 400, 0));
                        this.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 400, 0));
                    }
                } else {
                    this.playTameEffect(false);
                    this.world.setEntityState(this, (byte)6);
                }
            }
            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(37.0D); // 血量37
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D); // 原版狼的基础速度
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 4 + this.rand.nextInt(4); // 经验掉落 4-7
    }

    @Override
    @javax.annotation.Nullable
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return new net.minecraft.util.ResourceLocation("blood", "entities/blood_hound");
    }

    @Override
    protected float getSoundPitch() {
        // Minecraft引擎中，降低音调(Pitch)会原生等比例降低倍速(Speed)。
        // 设置为 0.575F (0.5音调与0.65倍速的平均折中值) 可以完美实现那种沉闷、缓慢的血肉怪物音效
        return 0.575F;
    }

    @Override
    public boolean isBreedingItem(net.minecraft.item.ItemStack stack) {
        // 覆盖原版狼的判定，血液猎犬无法通过喂食进入发情状态进行繁殖
        return false;
    }

    @Override
    public EntityWolf createChild(net.minecraft.entity.EntityAgeable ageable) {
        // 由于禁止了繁殖，此方法理论上不会被调用，但返回 null 更安全
        return null;
    }

    @Override
    public boolean getCanSpawnHere() {
        // 限制只在主世界地表生成
        if (this.world.provider.getDimension() != 0) {
            return false;
        }
        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if (!this.world.canSeeSky(pos)) {
            return false;
        }
        return super.getCanSpawnHere();
    }

    /**
     * 重写尾巴的旋转角度，防止因为最大生命值改变导致的原版计算错误（计算错误会导致尾巴360度旋转）
     */
    @Override
    public float getTailRotation() {
        if (this.isAngry()) {
            return 1.5393804F; // 激怒状态下尾巴竖起（原版的固定角度）
        } else {
            // 根据生命值比例计算，防止溢出
            return (0.55F - (float)(this.getMaxHealth() - this.getHealth()) * 0.02F) * (float)Math.PI;
        }
    }
}
