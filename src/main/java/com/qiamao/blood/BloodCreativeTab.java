package com.qiamao.blood;

import com.qiamao.blood.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class BloodCreativeTab extends CreativeTabs {

    // 实例化这个创造模式标签页
    public static final BloodCreativeTab INSTANCE = new BloodCreativeTab();

    public BloodCreativeTab() {
        super(BloodMod.MODID);
        this.setBackgroundImageName("bloodcraft.png");
        this.setNoTitle();
    }

    /**
     * 设置标签页的图标
     */
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack getTabIconItem() {
        // 使用带有顶部眼球纹理的隐藏物品作为标签页的图标
        return new ItemStack(ModItems.TAB_ICON);
    }

    /**
     * 在此方法中将我们模组独有的刷怪蛋添加到本创造模式标签页中
     */
    @Override
    @SuppressWarnings("null")
    public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> p_78018_1_) {
        // 先收集所有的物品
        NonNullList<ItemStack> allItems = NonNullList.create();
        super.displayAllRelevantItems(allItems);

        // 我们自己手动分类
        NonNullList<ItemStack> blocks = NonNullList.create();
        NonNullList<ItemStack> items = NonNullList.create();
        NonNullList<ItemStack> weapons = NonNullList.create();
        NonNullList<ItemStack> materials = NonNullList.create();
        NonNullList<ItemStack> potions = NonNullList.create();
        NonNullList<ItemStack> spawnEggs = NonNullList.create();

        for (ItemStack stack : allItems) {
            net.minecraft.item.Item item = stack.getItem();
            
            // 隐藏图标物品不显示
            if (item == ModItems.TAB_ICON) continue;
            
            if (item == ModItems.BLOOD_BOTTLE || item == ModItems.SPLASH_BLOOD_BOTTLE || item == ModItems.LINGERING_BLOOD_BOTTLE || item == ModItems.BUCKET_BLOOD) {
                potions.add(stack);
            } else if (item == ModItems.VENOMOUS_STINGER_DAGGER || item == ModItems.MONSTER_BONE_AXE || item == ModItems.VENOMOUS_STINGER_STAFF) {
                weapons.add(stack);
            } else if (item instanceof net.minecraft.item.ItemBlock || item == ModItems.BLOOD_DOOR_ITEM) {
                blocks.add(stack);
            } else if (item == ModItems.STING_CORE || item == ModItems.STING_CORE_FRAGMENT || item == ModItems.BLOOD_CORE || item == ModItems.CENTIPEDE_STINGER || item == ModItems.MONSTER_BONE_SKELETON || item == ModItems.STINGER_POWDER) {
                materials.add(stack);
            } else {
                items.add(stack);
            }
        }

        // 1. 多足嗜血虫 刷怪蛋
        ItemStack spawnEggSeeker = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtSeeker = new NBTTagCompound();
        NBTTagCompound entityTagSeeker = new NBTTagCompound();
        entityTagSeeker.setString("id", BloodMod.MODID + ":centipede_blood_seeker");
        nbtSeeker.setTag("EntityTag", entityTagSeeker);
        spawnEggSeeker.setTagCompound(nbtSeeker);
        spawnEggs.add(spawnEggSeeker);

        // 1.5 嗜血多足幼虫 刷怪蛋
        ItemStack spawnEggLarva = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtLarva = new NBTTagCompound();
        NBTTagCompound entityTagLarva = new NBTTagCompound();
        entityTagLarva.setString("id", BloodMod.MODID + ":centipede_blood_seeker_larva");
        nbtLarva.setTag("EntityTag", entityTagLarva);
        spawnEggLarva.setTagCompound(nbtLarva);
        spawnEggs.add(spawnEggLarva);

        // 2. 血螨 刷怪蛋
        ItemStack spawnEggMite = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtMite = new NBTTagCompound();
        NBTTagCompound entityTagMite = new NBTTagCompound();
        entityTagMite.setString("id", BloodMod.MODID + ":blood_mite");
        nbtMite.setTag("EntityTag", entityTagMite);
        spawnEggMite.setTagCompound(nbtMite);
        spawnEggs.add(spawnEggMite);

        // 3. 寄生史蒂夫 刷怪蛋
        ItemStack spawnEggSteve = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtSteve = new NBTTagCompound();
        NBTTagCompound entityTagSteve = new NBTTagCompound();
        entityTagSteve.setString("id", BloodMod.MODID + ":parasitic_steve");
        nbtSteve.setTag("EntityTag", entityTagSteve);
        spawnEggSteve.setTagCompound(nbtSteve);
        spawnEggs.add(spawnEggSteve);

        // 4. 血液母体 刷怪蛋
        ItemStack spawnEggMother = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtMother = new NBTTagCompound();
        NBTTagCompound entityTagMother = new NBTTagCompound();
        entityTagMother.setString("id", BloodMod.MODID + ":blood_mother");
        nbtMother.setTag("EntityTag", entityTagMother);
        spawnEggMother.setTagCompound(nbtMother);
        spawnEggs.add(spawnEggMother);

        // 5. 邪教传教士 刷怪蛋
        ItemStack spawnEggPreacher = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtPreacher = new NBTTagCompound();
        NBTTagCompound entityTagPreacher = new NBTTagCompound();
        entityTagPreacher.setString("id", BloodMod.MODID + ":cultist_preacher");
        nbtPreacher.setTag("EntityTag", entityTagPreacher);
        spawnEggPreacher.setTagCompound(nbtPreacher);
        spawnEggs.add(spawnEggPreacher);

        // 6. 视神经 刷怪蛋
        ItemStack spawnEggOpticNerve = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtOpticNerve = new NBTTagCompound();
        NBTTagCompound entityTagOpticNerve = new NBTTagCompound();
        entityTagOpticNerve.setString("id", BloodMod.MODID + ":optic_nerve");
        nbtOpticNerve.setTag("EntityTag", entityTagOpticNerve);
        spawnEggOpticNerve.setTagCompound(nbtOpticNerve);
        spawnEggs.add(spawnEggOpticNerve);

        // 7. 血液猎犬 刷怪蛋
        ItemStack spawnEggHound = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtHound = new NBTTagCompound();
        NBTTagCompound entityTagHound = new NBTTagCompound();
        entityTagHound.setString("id", BloodMod.MODID + ":blood_hound");
        nbtHound.setTag("EntityTag", entityTagHound);
        spawnEggHound.setTagCompound(nbtHound);
        spawnEggs.add(spawnEggHound);

        // 8. 迫近者 刷怪蛋
        ItemStack spawnEggApproacher = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound nbtApproacher = new NBTTagCompound();
        NBTTagCompound entityTagApproacher = new NBTTagCompound();
        entityTagApproacher.setString("id", BloodMod.MODID + ":approacher");
        nbtApproacher.setTag("EntityTag", entityTagApproacher);
        spawnEggApproacher.setTagCompound(nbtApproacher);
        spawnEggs.add(spawnEggApproacher);

        // 按分类顺序添加到最终列表中
        p_78018_1_.addAll(blocks);
        p_78018_1_.addAll(weapons);
        p_78018_1_.addAll(materials);
        p_78018_1_.addAll(potions);
        p_78018_1_.addAll(items);
        p_78018_1_.addAll(spawnEggs);
    }
}