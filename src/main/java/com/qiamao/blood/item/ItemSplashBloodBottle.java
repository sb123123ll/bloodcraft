package com.qiamao.blood.item;

import com.qiamao.blood.BloodMod;
import com.qiamao.blood.BloodCreativeTab;
import com.qiamao.blood.entity.EntitySplashBlood;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemSplashBloodBottle extends Item {

    public ItemSplashBloodBottle() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName("splash_blood_bottle");
        this.setRegistryName(BloodMod.MODID, "splash_blood_bottle");
        this.setCreativeTab(BloodCreativeTab.INSTANCE);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        ItemStack throwStack = itemstack.copy();
        
        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            EntitySplashBlood entitysplash = new EntitySplashBlood(worldIn, playerIn, false);
            entitysplash.setItem(throwStack);
            entitysplash.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, -20.0F, 0.5F, 1.0F);
            worldIn.spawnEntity(entitysplash);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }
}
