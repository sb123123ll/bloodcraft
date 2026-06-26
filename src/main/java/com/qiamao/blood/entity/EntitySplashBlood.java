package com.qiamao.blood.entity;

import com.qiamao.blood.init.ModItems;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntitySplashBlood extends EntityThrowable {
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntitySplashBlood.class, DataSerializers.ITEM_STACK);
    private boolean isLingering;

    public EntitySplashBlood(World worldIn) {
        super(worldIn);
    }

    public EntitySplashBlood(World worldIn, EntityLivingBase throwerIn, boolean isLingering) {
        super(worldIn, throwerIn);
        this.isLingering = isLingering;
    }

    public EntitySplashBlood(World worldIn, double x, double y, double z, boolean isLingering) {
        super(worldIn, x, y, z);
        this.isLingering = isLingering;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
    }

    public void setItem(ItemStack stack) {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    public ItemStack getItem() {
        ItemStack itemstack = this.getDataManager().get(ITEM);
        if (itemstack.isEmpty()) {
            return new ItemStack(this.isLingering ? ModItems.LINGERING_BLOOD_BOTTLE : ModItems.SPLASH_BLOOD_BOTTLE);
        }
        return itemstack;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.05F;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            ItemStack itemstack = this.getItem();

            if (this.isLingering) {
                this.makeAreaOfEffectCloud();
            } else {
                this.applySplashEffects(result);
            }

            // 2002 is the potion splash effect, passing the color of the particles
            // We use a custom playEvent call to spawn both red and khaki colored particles
            int redColor = 0x8a0303; // Red color for blood
            int khakiColor = 0xC3B091; // Khaki color
            
            // Randomly choose between red and khaki for the main splash event
            int mainColor = this.world.rand.nextBoolean() ? redColor : khakiColor;
            this.world.playEvent(2002, new BlockPos(this), mainColor);
            
            // And spawn some extra particles of the other color manually
            int otherColor = (mainColor == redColor) ? khakiColor : redColor;
            float f = (float)(otherColor >> 16 & 255) / 255.0F;
            float f1 = (float)(otherColor >> 8 & 255) / 255.0F;
            float f2 = (float)(otherColor >> 0 & 255) / 255.0F;
            
            // Send packet to clients to spawn the other color particles
            // The 2002 event already spawns around 100 particles, we just need to mix it up
            this.world.playEvent(2002, new BlockPos(this), otherColor);
            
            this.setDead();
        }
    }

    private void applySplashEffects(RayTraceResult result) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

        if (!list.isEmpty()) {
            for (EntityLivingBase entitylivingbase : list) {
                if (entitylivingbase.canBeHitWithPotion()) {
                    double d0 = this.getDistanceSq(entitylivingbase);

                    if (d0 < 16.0D) {
                        double multiplier = 1.0D - Math.sqrt(d0) / 4.0D;

                        if (entitylivingbase == result.entityHit) {
                            multiplier = 1.0D;
                        }

                        // Apply base effects
                        entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (int)(5 * 20 * multiplier), 0));
                    }
                }
            }
        }
    }

    private void makeAreaOfEffectCloud() {
        EntityAreaEffectCloud cloud = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
        EntityLivingBase owner = this.getThrower();
        if (owner != null) {
            cloud.setOwner(owner);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        // For AreaEffectCloud, it only supports one color, so we use a color between red and khaki, or alternate
        // But since we want them to coexist, we can spawn TWO clouds, one red and one khaki
        cloud.setColor(0x8a0303); // Red color

        // Add base effects
        cloud.addEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));

        this.world.spawnEntity(cloud);
        
        // Spawn the second cloud for the khaki color
        EntityAreaEffectCloud cloudKhaki = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
        if (owner != null) {
            cloudKhaki.setOwner(owner);
        }
        cloudKhaki.setRadius(3.0F);
        cloudKhaki.setRadiusOnUse(-0.5F);
        cloudKhaki.setWaitTime(10);
        cloudKhaki.setRadiusPerTick(-cloudKhaki.getRadius() / (float)cloudKhaki.getDuration());
        cloudKhaki.setColor(0xC3B091); // Khaki color
        // No need to add effects to the second cloud to prevent double applying, it's just for visual
        this.world.spawnEntity(cloudKhaki);
    }
}
