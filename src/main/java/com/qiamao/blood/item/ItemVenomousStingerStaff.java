package com.qiamao.blood.item;

import com.google.common.collect.Multimap;
import com.qiamao.blood.BloodMod;
import com.qiamao.blood.entity.EntityPoisonBubble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

public class ItemVenomousStingerStaff extends ItemSword {

    // 创建工具材料，耐久度为钻石(1561)的1.25倍 = 1951
    // 剑的默认伤害 = 3.0F + material.getAttackDamage() + 1(基础)
    // 我们想要总伤害为 5点，所以 material.getAttackDamage() = 1.0F
    public static final Item.ToolMaterial VENOMOUS_STINGER = EnumHelper.addToolMaterial("VENOMOUS_STINGER", 0, 1951, 2.0F, 1.0F, 15);

    public ItemVenomousStingerStaff() {
        super(VENOMOUS_STINGER);
        this.setRegistryName(BloodMod.MODID, "venomous_stinger_staff");
        this.setUnlocalizedName("venomous_stinger_staff");
        this.setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
    }

    /**
     * 右键发射毒液投掷物
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            EntityPoisonBubble bubble = new EntityPoisonBubble(worldIn, playerIn);
            // 增加一点发射速度，使其符合法杖的特点
            bubble.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(bubble);
            
            // 每次发射消耗 2 点耐久
            itemstack.damageItem(2, playerIn);
        }

        // 播放投掷音效
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, com.qiamao.blood.init.ModSounds.VENOMOUS_STINGER_STAFF_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        // 给玩家增加冷却时间，防止连发过快（例如 10 ticks = 0.5秒）
        playerIn.getCooldownTracker().setCooldown(this, 10);

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    /**
     * 近战攻击击中实体时
     * 25% 概率施加虚弱效果（同毒液投掷物）
     */
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (!attacker.world.isRemote && attacker.world.rand.nextFloat() < 0.25F) {
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
        }
        return super.hitEntity(stack, target, attacker);
    }
}