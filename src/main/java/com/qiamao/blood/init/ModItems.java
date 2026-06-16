package com.qiamao.blood.init;

import com.qiamao.blood.BloodMod;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;

import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

/**
 * 物品注册类
 * 负责统一管理和注册模组中的所有物品
 */
@Mod.EventBusSubscriber(modid = BloodMod.MODID)
public class ModItems {

    // 实例化瓶装血液，继承 ItemFood 来实现类似食物/饮料的食用逻辑
    // 0 是恢复的饥饿值，0.0F 是恢复的饱和度，false 表示不是狼肉
    public static final Item BLOOD_BOTTLE = new ItemFood(0, 0.0F, false) {

        // 重写 onItemRightClick 方法，让其在任何模式下（包括创造模式和满饱食度）都可以触发饮用动作
        @Override
        public net.minecraft.util.ActionResult<net.minecraft.item.ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, net.minecraft.util.EnumHand handIn) {
            playerIn.setActiveHand(handIn);
            return new net.minecraft.util.ActionResult<net.minecraft.item.ItemStack>(net.minecraft.util.EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }

        // 设置动作类型为饮用 (DRINK) 而不是默认的吃 (EAT)
        @Override
        public EnumAction getItemUseAction(net.minecraft.item.ItemStack stack) {
            return EnumAction.DRINK;
        }

        // 删除 onUsingTick 的重写，恢复原版饮用音效逻辑，因为重写导致了性能问题

        // 饮用完毕后触发的音效
        @Override
        public net.minecraft.item.ItemStack onItemUseFinish(net.minecraft.item.ItemStack stack, World worldIn, net.minecraft.entity.EntityLivingBase entityLiving) {
            // 在 super 之前调用，确保我们的音效在原版之前被触发
            if (entityLiving instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entityLiving;
                if (!worldIn.isRemote) {
                    // 服务端处理数据逻辑和物品扣除，不播放声音
                } else {
                    // 客户端直接播放自定义声音
                    // 在 1.12.2 中，如果 world.isRemote 为 true，则 entityLiving 就是客户端实体
                    player.playSound(ModSounds.DRINK_BLOOD, 1.0F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
                }
            }

            // 调用 super 执行吃完食物的基本逻辑（原版 ItemFood 默认会消耗 1 个物品）
            net.minecraft.item.ItemStack result = super.onItemUseFinish(stack, worldIn, entityLiving);

            // 饮用后的物品处理
            if (entityLiving instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entityLiving;
                if (player.capabilities.isCreativeMode) {
                    // 如果是创造模式，我们强制把消耗掉的数量加回来，以保持不消耗
                    // 并且不要给空瓶子
                    stack.grow(1);
                    return stack;
                } else {
                    // 如果不是创造模式，则返回一个空玻璃瓶
                    if (result.isEmpty()) {
                        return new net.minecraft.item.ItemStack(Items.GLASS_BOTTLE);
                    } else {
                        player.inventory.addItemStackToInventory(new net.minecraft.item.ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            }
            
            return result;
        }

        // 食用完毕后触发的逻辑
        @Override
        protected void onFoodEaten(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer player) {
            if (!worldIn.isRemote) {
                // 基础惩罚效果：每次必得
                // 1. 失明5秒
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 5 * 20, 0));
                
                // 2. 反胃8~15秒
                int nauseaDuration = (8 + worldIn.rand.nextInt(8)) * 20; // 8 到 15 (8 + [0, 7])
                player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, nauseaDuration, 0));

                // 50%的几率触发强力厄运惩罚，且不获得增益
                if (worldIn.rand.nextFloat() < 0.50F) {
                    // 1. 凋零5级（对应等级4） 8分钟
                    player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 8 * 60 * 20, 4));
                    
                    // 2. 虚弱2级（对应等级1） 2分钟
                    player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2 * 60 * 20, 1));
                    
                    // 3. 缓慢1级（对应等级0） 10~20秒
                    int slownessDuration = (10 + worldIn.rand.nextInt(11)) * 20; // 10 到 20 (10 + [0, 10])
                    player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, slownessDuration, 0));
                } else {
                    // 没有触发厄运时，获得以下增益效果
                    // 1. 生命恢复1级（对应等级0） 25秒
                    player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 25 * 20, 0));
                    
                    // 2. 抗性提升2级（对应等级1） 25秒
                    player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 25 * 20, 1));
                    
                    // 3. 急迫1级（对应等级0） 2分钟
                    player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 2 * 60 * 20, 0));
                    
                    // 4. 疾跑1级（对应等级0） 50秒 (原版速度效果)
                    player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 50 * 20, 0));
                    
                    // 5. 跳跃提升1级（对应等级0） 20秒
                    player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20 * 20, 0));
                    
                    // 6. 血量上限2级（对应等级1） 1分钟
                    player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 60 * 20, 1));
                    
                    // 7. 力量1级（对应等级0） 1分钟
                    player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 0));
                    
                    // 8. 瞬间治疗1级（对应等级0） 瞬间
                    player.addPotionEffect(new PotionEffect(MobEffects.INSTANT_HEALTH, 1, 0));
                }
            }
        }
    }
            .setAlwaysEdible() // 允许玩家在满饥饿值的情况下也能饮用
            .setMaxStackSize(1) // 堆叠上限改为 1，与原版药水一致
            .setUnlocalizedName("blood_bottle")
            .setRegistryName(BloodMod.MODID, "blood_bottle")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 实例化物品，使用 ItemBucket 继承类来实现桶的装载和放置逻辑
    public static final Item BUCKET_BLOOD = new ItemBucket(ModBlocks.BLOCK_BLOOD) {
        
        // 彻底接管方块放置逻辑，覆盖原版水声
        @Override
        public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
            net.minecraft.block.state.IBlockState iblockstate = worldIn.getBlockState(posIn);
            Material material = iblockstate.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

            if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
                return false;
            } else {
                if (worldIn.provider.doesWaterVaporize()) {
                    // 在下界等会蒸发的地方，产生烟雾和嘶嘶声
                    int i = posIn.getX();
                    int j = posIn.getY();
                    int k = posIn.getZ();
                    worldIn.playSound(player, posIn, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                    for (int l = 0; l < 8; ++l) {
                        worldIn.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_LARGE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                } else {
                    if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid()) {
                        worldIn.destroyBlock(posIn, true);
                    }

                    // 播放自定义音效
                    worldIn.playSound(player, posIn, ModSounds.BUCKET_EMPTY_BLOOD, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    // 放置血液方块
                    worldIn.setBlockState(posIn, ModBlocks.BLOCK_BLOOD.getDefaultState(), 11);
                }

                return true;
            }
        }
    }
            .setUnlocalizedName("bucket_blood")
            .setRegistryName(BloodMod.MODID, "bucket_blood")
            .setContainerItem(Items.BUCKET) // 交互后变成空桶
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 声明 ItemBlock
    public static final Item CURSED_FLESH_CHUNK_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_SEEKER_FLESH)
            .setRegistryName(ModBlocks.BLOOD_SEEKER_FLESH.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_LOG_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_LOG)
            .setRegistryName(ModBlocks.BLOOD_LOG.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_PLANKS_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_PLANKS)
            .setRegistryName(ModBlocks.BLOOD_PLANKS.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 仅用于创造模式标签页图标的隐藏物品（不加入任何标签页，不在游戏中正常获取）
    public static final Item TAB_ICON = new Item()
            .setUnlocalizedName("tab_icon")
            .setRegistryName(BloodMod.MODID, "tab_icon");

    // 怨瞳 (Plucked Eye / Resentful Eye)
    // 2 是恢复的饥饿值 (1个鸡腿)，0.2F 是饱和度 (相对较低，代表不是主食)，false 表示不能用来喂狼
    public static final Item PLUCKED_EYE = new ItemFood(2, 0.2F, false) {

        @Override
        protected void onFoodEaten(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer player) {
            if (!worldIn.isRemote) {
                // 15分钟的夜视效果 (15 * 60秒 * 20 ticks = 18000 ticks)
                // 等级 0 代表夜视 I 级
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 60 * 20, 0));
            }
            super.onFoodEaten(stack, worldIn, player);
        }
    }
            .setAlwaysEdible() // 饱食度满的时候也可食用
            .setUnlocalizedName("plucked_eye")
            .setRegistryName(BloodMod.MODID, "plucked_eye")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥糜烂的肉 (Gory Flesh)
    // 3 是恢复的饥饿值 (1.5个鸡腿图标)，0.3F 是饱和度，true 表示是类似腐肉/狼肉，允许玩家在满饱食度时食用
    public static final Item GORY_FLESH = new ItemFood(3, 0.3F, true) {

        @Override
        protected void onFoodEaten(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer player) {
            if (!worldIn.isRemote) {
                // 75% 概率获得 12秒的反胃和 2秒失明
                if (worldIn.rand.nextFloat() < 0.75F) {
                    player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 12 * 20, 0));
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 2 * 20, 0));
                }
            }
            super.onFoodEaten(stack, worldIn, player);
        }
    }
            // 默认饱食度满了不能吃，所以不加 .setAlwaysEdible()
            .setUnlocalizedName("gory_flesh")
            .setRegistryName(BloodMod.MODID, "gory_flesh")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 烤熟的烂肉 (Cooked Gory Flesh)
    // 10 是恢复的饥饿值 (5个鸡腿图标)，0.6F 是饱和度，没有负面效果
    public static final Item COOKED_GORY_FLESH = new ItemFood(10, 0.6F, false) {
        @Override
        public int getMaxItemUseDuration(net.minecraft.item.ItemStack stack) {
            // 原版吃东西默认是 32 ticks。这里重写并返回和原版一致的 32 
            // 烤鸡肉(Cooked Chicken)的食用时间也是 32 ticks。
            return 32;
        }
    }
            .setUnlocalizedName("cooked_gory_flesh")
            .setRegistryName(BloodMod.MODID, "cooked_gory_flesh")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 心脏 (Human Heart)
    // 饱食度是 16 (8个鸡腿图标)，饱和度 0.9F，允许满饱食度食用
    public static final Item HUMAN_HEART = new ItemFood(16, 0.9F, true) {
        @Override
        public int getMaxItemUseDuration(net.minecraft.item.ItemStack stack) {
            return 32;
        }
    }
            .setAlwaysEdible()
            .setUnlocalizedName("human_heart")
            .setRegistryName(BloodMod.MODID, "human_heart")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 熟心脏 (Cooked Human Heart)
    // 饱食度是 16 (8个鸡腿图标)，饱和度 0.9F
    public static final Item COOKED_HUMAN_HEART = new ItemFood(16, 0.9F, true) {
        @Override
        public int getMaxItemUseDuration(net.minecraft.item.ItemStack stack) {
            return 32;
        }

        @Override
        protected void onFoodEaten(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer player) {
            if (!worldIn.isRemote) {
                // 生命恢复 II (等级为 1 代表 II)，持续 5-7 秒 (100 - 140 ticks)
                int regenDuration = 100 + worldIn.rand.nextInt(41);
                player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, regenDuration, 1));
                
                // 力量 I (等级为 0 代表 I)，持续 1 分钟 (1200 ticks)
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 0));
            }
            super.onFoodEaten(stack, worldIn, player);
        }
    }
            .setAlwaysEdible()
            .setUnlocalizedName("cooked_human_heart")
            .setRegistryName(BloodMod.MODID, "cooked_human_heart")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 投掷血螨 (Thrown Blood Mite Item)
    public static final Item THROWN_BLOOD_MITE_ITEM = new Item() {
        @Override
        public ActionResult<net.minecraft.item.ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            net.minecraft.item.ItemStack itemstack = playerIn.getHeldItem(handIn);

            if (!playerIn.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!worldIn.isRemote) {
                com.qiamao.blood.entity.EntityThrownBloodMite entity = new com.qiamao.blood.entity.EntityThrownBloodMite(worldIn, playerIn);
                // 使用和丢鸡蛋一样的抛出参数
                entity.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
                worldIn.spawnEntity(entity);
            }

            playerIn.addStat(StatList.getObjectUseStats(this));
            return new ActionResult<net.minecraft.item.ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
    }
            .setMaxStackSize(16) // 最多堆叠 16 个，和雪球/鸡蛋一样
            .setUnlocalizedName("thrown_blood_mite_item")
            .setRegistryName(BloodMod.MODID, "thrown_blood_mite_item")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 仅用于成就图标的隐藏物品：血螨脑控的脸
    public static final Item PARASITIC_STEVE_FACE = new Item()
            .setUnlocalizedName("parasitic_steve_face")
            .setRegistryName(BloodMod.MODID, "parasitic_steve_face");

    // 仅用于成就图标的隐藏物品：邪教传教士的脸（代码硬编码渲染）
    public static final Item CULTIST_PREACHER_FACE = new com.qiamao.blood.item.ItemEntityFace("cultist_preacher")
            .setUnlocalizedName("cultist_preacher_face")
            .setRegistryName(BloodMod.MODID, "cultist_preacher_face");

    // 毒刺粉 (Stinger Powder)
    public static final Item STINGER_POWDER = new Item()
            .setUnlocalizedName("stinger_powder")
            .setRegistryName(BloodMod.MODID, "stinger_powder")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 多足虫的刺 (Centipede Stinger)
    // 普通物品，可通过合成分解成毒刺粉
    public static final Item CENTIPEDE_STINGER = new Item()
            .setUnlocalizedName("centipede_stinger")
            .setRegistryName(BloodMod.MODID, "centipede_stinger")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 毒刺核心 (Sting Core)
    public static final Item STING_CORE = new Item()
            .setUnlocalizedName("sting_core")
            .setRegistryName(BloodMod.MODID, "sting_core")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 毒刺核心碎片 (Sting Core Fragment)
    public static final Item STING_CORE_FRAGMENT = new Item()
            .setUnlocalizedName("sting_core_fragment")
            .setRegistryName(BloodMod.MODID, "sting_core_fragment")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血液核心 (Blood Core)
    public static final Item BLOOD_CORE = new Item()
            .setUnlocalizedName("blood_core")
            .setRegistryName(BloodMod.MODID, "blood_core")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血螨肉 (Blood Mite Meat) - 击杀血螨掉落，快速食用，不回饱食度但回血
    public static final Item BLOOD_MITE_MEAT = new ItemFood(0, 0.0F, false) {
        @Override
        public int getMaxItemUseDuration(net.minecraft.item.ItemStack stack) {
            // 1秒 = 20 ticks
            return 20;
        }

        @Override
        protected void onFoodEaten(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer player) {
            if (!worldIn.isRemote) {
                // 恢复1点血（半颗心）
                player.heal(1.0F);
                
                // 20%概率获得5秒反胃效果（100 ticks）
                if (worldIn.rand.nextFloat() < 0.2F) {
                    player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
                }
            }
            super.onFoodEaten(stack, worldIn, player);
        }

        @Override
        @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
        public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
            // 添加简介提示
            tooltip.add("这真的能吃吗？");
        }
    }
            .setAlwaysEdible() // 允许玩家在满饥饿值的情况下也能食用
            .setUnlocalizedName("blood_mite_meat")
            .setRegistryName(BloodMod.MODID, "blood_mite_meat")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 毒刺匕首 (Venomous Stinger Dagger)
    public static final Item VENOMOUS_STINGER_DAGGER = new net.minecraft.item.ItemSword(net.minecraft.item.Item.ToolMaterial.IRON) {
        @Override
        public int getMaxDamage() {
            return 400;
        }

        @Override
        public com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot equipmentSlot) {
            com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

            if (equipmentSlot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND) {
                // 攻击伤害 4.5 点 (玩家自带 1 点基础伤害，所以这里加 3.5 即可达到 4.5 点总伤害)
                multimap.put(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new net.minecraft.entity.ai.attributes.AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3.5D, 0));
                // 攻击速度 3.3 (原版基础攻击速度是 4.0，所以 4.0 - 0.7 = 3.3)
                multimap.put(net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED.getName(), new net.minecraft.entity.ai.attributes.AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -0.7D, 0));
            }

            return multimap;
        }

        @Override
        public boolean hitEntity(net.minecraft.item.ItemStack stack, net.minecraft.entity.EntityLivingBase target, net.minecraft.entity.EntityLivingBase attacker) {
            // 100% 概率中毒 1 级 3 秒 (60 ticks)
            if (!target.world.isRemote) {
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0));

                // 35% 概率反胃 4 秒 (80 ticks)
                if (target.world.rand.nextFloat() < 0.35F) {
                    target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
                }
            }
            // 武器耐久度扣除
            stack.damageItem(1, attacker);
            return true;
        }

        @Override
        public float getDestroySpeed(net.minecraft.item.ItemStack stack, net.minecraft.block.state.IBlockState state) {
            // 继承原版剑的挖掘特性：蜘蛛网、树叶、竹子等可以快速破坏
            if (state.getBlock() == net.minecraft.init.Blocks.WEB) {
                return 15.0F;
            }
            return super.getDestroySpeed(stack, state);
        }

        @Override
        public boolean canHarvestBlock(net.minecraft.block.state.IBlockState state) {
            // 继承原版剑的挖掘限制：不能挖掘大多数方块
            return super.canHarvestBlock(state);
        }
    }
            .setUnlocalizedName("venomous_stinger_dagger")
            .setRegistryName(BloodMod.MODID, "venomous_stinger_dagger")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // {凡}妖骸骨斧 (Monster Bone Axe)
    public static final Item MONSTER_BONE_AXE = new net.minecraft.item.ItemAxe(net.minecraft.item.Item.ToolMaterial.DIAMOND) {
        {
            // 设置耐久为 800
            this.setMaxDamage(800);
            // 设置挖掘速度为钻石斧的 1.55 倍 (钻石斧默认 8.0)
            this.efficiency = 8.0F * 1.55F;
            // 设置攻击伤害为 11
            // 注意：ItemAxe 默认会给攻击力加成，但为了精确控制，我们通过 AttributeModifier 覆盖
            this.attackDamage = 11.0F - 1.0F; // 减去玩家基础伤害 1
        }

        @Override
        public com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot equipmentSlot) {
            com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
            if (equipmentSlot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND) {
                // 清除原有的攻击力和速度加成，重新设定
                multimap.removeAll(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName());
                multimap.removeAll(net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED.getName());
                // 伤害 11 点 (基础 1.0 + 10.0)
                multimap.put(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new net.minecraft.entity.ai.attributes.AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 10.0D, 0));
                // 速度和原版斧头一致 (默认是 -3.0 到 -3.2，钻石斧是 -3.0，即 1.0 攻击速度)
                multimap.put(net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED.getName(), new net.minecraft.entity.ai.attributes.AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.0D, 0));
            }
            return multimap;
        }
    }
            .setUnlocalizedName("monster_bone_axe")
            .setRegistryName(BloodMod.MODID, "monster_bone_axe")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 怪物躯骨 (Monster Bone Skeleton) - 暂无用途的材料物品
    public static final Item MONSTER_BONE_SKELETON = new Item()
            .setUnlocalizedName("monster_bone_skeleton")
            .setRegistryName(BloodMod.MODID, "monster_bone_skeleton")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥树叶物品
    public static final Item BLOOD_LEAVES_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_LEAVES)
            .setRegistryName(ModBlocks.BLOOD_LEAVES.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血腥台阶物品
    public static final Item BLOOD_SLAB_ITEM = new net.minecraft.item.ItemSlab(ModBlocks.BLOOD_SLAB_HALF, (com.qiamao.blood.block.BlockBloodSlabHalf) ModBlocks.BLOOD_SLAB_HALF, (com.qiamao.blood.block.BlockBloodSlabDouble) ModBlocks.BLOOD_SLAB_DOUBLE)
            .setRegistryName(ModBlocks.BLOOD_SLAB_HALF.getRegistryName());

    // 血腥楼梯物品
    public static final Item BLOOD_STAIRS_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_STAIRS)
            .setRegistryName(ModBlocks.BLOOD_STAIRS.getRegistryName());

    // 肉块物品
    public static final Item FLESH_CHUNK_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.FLESH_CHUNK)
            .setRegistryName(ModBlocks.FLESH_CHUNK.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血块物品 - 已隐藏，不注册到创造标签页
    public static final Item BLOOD_BLOCK_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_BLOCK)
            .setRegistryName(ModBlocks.BLOOD_BLOCK.getRegistryName());

    // 血腥树苗物品
    public static final Item BLOOD_SAPLING_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_SAPLING)
            .setRegistryName(ModBlocks.BLOOD_SAPLING.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 血液祭坛物品
    public static final Item BLOOD_ALTAR_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_ALTAR)
            .setRegistryName(ModBlocks.BLOOD_ALTAR.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    // 头颅物品
    public static final Item PARASITIC_STEVE_HEAD_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.PARASITIC_STEVE_HEAD)
            .setRegistryName(ModBlocks.PARASITIC_STEVE_HEAD.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);
    public static final Item PREACHER_HEAD_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.PREACHER_HEAD)
            .setRegistryName(ModBlocks.PREACHER_HEAD.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_TRAPDOOR_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_TRAPDOOR)
            .setRegistryName(ModBlocks.BLOOD_TRAPDOOR.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_DOOR_ITEM = new net.minecraft.item.ItemDoor(ModBlocks.BLOOD_DOOR)
            .setUnlocalizedName("blood_door")
            .setRegistryName(BloodMod.MODID, "blood_door")
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_FENCE_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_FENCE)
            .setRegistryName(ModBlocks.BLOOD_FENCE.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item BLOOD_FENCE_GATE_ITEM = new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_FENCE_GATE)
            .setRegistryName(ModBlocks.BLOOD_FENCE_GATE.getRegistryName())
            .setCreativeTab(com.qiamao.blood.BloodCreativeTab.INSTANCE);

    public static final Item SPLASH_BLOOD_BOTTLE = new com.qiamao.blood.item.ItemSplashBloodBottle();
    public static final Item LINGERING_BLOOD_BOTTLE = new com.qiamao.blood.item.ItemLingeringBloodBottle();
    public static final Item VENOMOUS_STINGER_STAFF = new com.qiamao.blood.item.ItemVenomousStingerStaff();

    /**
     * 注册发射器行为
     */
    public static void registerDispenserBehaviors() {
        net.minecraft.block.BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(THROWN_BLOOD_MITE_ITEM, new net.minecraft.dispenser.BehaviorProjectileDispense() {
            @Override
            protected net.minecraft.entity.IProjectile getProjectileEntity(World worldIn, net.minecraft.dispenser.IPosition position, net.minecraft.item.ItemStack stackIn) {
                com.qiamao.blood.entity.EntityThrownBloodMite mite = new com.qiamao.blood.entity.EntityThrownBloodMite(worldIn, position.getX(), position.getY(), position.getZ());
                return mite;
            }
        });
    }

    /**
     * 监听物品注册事件
     * Forge 会在合适的时机自动调用此方法来注册物品
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                BUCKET_BLOOD,
                BLOOD_BOTTLE,
                SPLASH_BLOOD_BOTTLE,
                LINGERING_BLOOD_BOTTLE,
                CURSED_FLESH_CHUNK_ITEM,
                BLOOD_LOG_ITEM,
                BLOOD_PLANKS_ITEM,
                BLOOD_LEAVES_ITEM,
                BLOOD_SLAB_ITEM,
                BLOOD_STAIRS_ITEM,
                FLESH_CHUNK_ITEM,
                BLOOD_BLOCK_ITEM,
                BLOOD_SAPLING_ITEM,
                BLOOD_ALTAR_ITEM,
                TAB_ICON,
                PLUCKED_EYE,
                GORY_FLESH,
                COOKED_GORY_FLESH,
                HUMAN_HEART,
                COOKED_HUMAN_HEART,
                THROWN_BLOOD_MITE_ITEM,
                PARASITIC_STEVE_FACE,
                CENTIPEDE_STINGER,
                STINGER_POWDER,
                VENOMOUS_STINGER_DAGGER,
                BLOOD_MITE_MEAT,
                MONSTER_BONE_AXE,
                MONSTER_BONE_SKELETON,
                CULTIST_PREACHER_FACE,
                STING_CORE,
                STING_CORE_FRAGMENT,
                BLOOD_CORE,
                PARASITIC_STEVE_HEAD_ITEM,
                PREACHER_HEAD_ITEM,
                BLOOD_TRAPDOOR_ITEM,
                BLOOD_DOOR_ITEM,
                BLOOD_FENCE_ITEM,
                BLOOD_FENCE_GATE_ITEM,
                VENOMOUS_STINGER_STAFF
        );

        // 绑定门物品到方块
        ModBlocks.BLOOD_DOOR.setDoorItem(BLOOD_DOOR_ITEM);

        // 注册血液淬炼台对应的物品
        event.getRegistry().register(new net.minecraft.item.ItemBlock(ModBlocks.BLOOD_TEMPERING_ALTAR).setRegistryName(ModBlocks.BLOOD_TEMPERING_ALTAR.getRegistryName()));
    }

    /**
     * 监听空桶装水事件，实现自定义桶的装载逻辑
     */
    @SubscribeEvent
    public static void onBucketFill(FillBucketEvent event) {
        // 如果玩家手里拿的是空桶
        if (event.getEmptyBucket().getItem() == Items.BUCKET) {
            net.minecraft.util.math.RayTraceResult target = event.getTarget();
            if (target != null && target.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
                net.minecraft.world.World world = event.getWorld();
                net.minecraft.util.math.BlockPos pos = target.getBlockPos();
                net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
                
                // 检查被点击的方块是否是血液，并且是源方块（LEVEL == 0 表示完整的源方块）
                if (state.getBlock() == ModBlocks.BLOCK_BLOOD && state.getValue(BlockFluidClassic.LEVEL) == 0) {
                    world.setBlockToAir(pos); // 移除世界中的血液方块
                    world.playSound(event.getEntityPlayer(), pos, ModSounds.BUCKET_FILL_BLOOD, SoundCategory.BLOCKS, 1.0F, 1.0F); // 播放装水音效
                    event.setFilledBucket(new net.minecraft.item.ItemStack(ModItems.BUCKET_BLOOD)); // 给玩家我们自定义的血液桶
                    event.setResult(Event.Result.ALLOW); // 允许事件，覆盖原版的默认逻辑
                }
            }
        }
    }
}
