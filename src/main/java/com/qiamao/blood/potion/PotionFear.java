package com.qiamao.blood.potion;

import com.qiamao.blood.BloodMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * 恐惧效果 - 削减50%最大生命值
 */
public class PotionFear extends Potion {
    // 固定的UUID用于属性修改器，确保可以被正确移除
    private static final UUID FEAR_MODIFIER_UUID = UUID.fromString("732d8471-12c4-4d8e-a9b0-3367d3e0987c");
    private static final ResourceLocation ICON = new ResourceLocation(BloodMod.MODID, "textures/gui/potion_icons.png");

    public PotionFear() {
        super(true, 0x4A0404); // 是负面效果，颜色为深血红
        this.setPotionName("effect.fear");
        this.setRegistryName(new ResourceLocation(BloodMod.MODID, "fear"));
        // 1.12.2 中通过属性修改器来实现生命上限削减
        this.registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH, FEAR_MODIFIER_UUID.toString(), -0.5D, 2); // 2 表示百分比削减 (multiply total)
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        // 在应用修改器前记录当前血量和上限，用于恢复
        // 注意：由于修改器是直接作用于属性的，我们需要通过 NBT 或自定义 Capability 记录状态
        super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        
        // 调整当前血量，防止血量溢出（虽然上限减半了，但当前血量也应该按比例减少）
        if (entityLivingBaseIn.getHealth() > entityLivingBaseIn.getMaxHealth()) {
            entityLivingBaseIn.setHealth(entityLivingBaseIn.getMaxHealth());
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        // 1. 获取移除修改器前的生命值百分比
        float maxHealthBefore = entityLivingBaseIn.getMaxHealth();
        float healthBefore = entityLivingBaseIn.getHealth();
        
        // 2. 移除修改器（这会恢复最大生命值属性）
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        
        // 3. 从 NBT 中读取获得效果前的原始血量
        if (!entityLivingBaseIn.world.isRemote) {
            if (entityLivingBaseIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
                // 强制发送属性同步包，确保客户端先更新最大生命值，避免因生命上限未更新导致血量被客户端截断
                net.minecraft.entity.ai.attributes.AbstractAttributeMap map = entityLivingBaseIn.getAttributeMap();
                if (map instanceof net.minecraft.entity.ai.attributes.AttributeMap) {
                    java.util.Collection<net.minecraft.entity.ai.attributes.IAttributeInstance> dirty = ((net.minecraft.entity.ai.attributes.AttributeMap) map).getDirtyInstances();
                    if (!dirty.isEmpty()) {
                        ((net.minecraft.entity.player.EntityPlayerMP) entityLivingBaseIn).connection.sendPacket(new net.minecraft.network.play.server.SPacketEntityProperties(entityLivingBaseIn.getEntityId(), dirty));
                    }
                }
            }

            net.minecraft.nbt.NBTTagCompound nbt = entityLivingBaseIn.getEntityData();
            if (nbt.hasKey("BloodCraft_PreFearHealth")) {
                float preFearHealth = nbt.getFloat("BloodCraft_PreFearHealth");
                // 恢复到原始血量，但不超过当前（已恢复的）最大生命值
                entityLivingBaseIn.setHealth(Math.min(preFearHealth, entityLivingBaseIn.getMaxHealth()));
                // 清理 NBT 标记
                nbt.removeTag("BloodCraft_HasFear");
                nbt.removeTag("BloodCraft_PreFearHealth");
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, net.minecraft.potion.PotionEffect effect, net.minecraft.client.Minecraft mc) {
        renderIcon(x + 6, y + 7, mc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, net.minecraft.potion.PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
        renderIcon(x + 3, y + 3, mc);
    }

    @SideOnly(Side.CLIENT)
    private void renderIcon(int x, int y, net.minecraft.client.Minecraft mc) {
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(ICON);
        // 这里假设有一张包含图标的图，恐惧图标在 0,0 位置
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 256, 256);
        // 渲染完图标后，必须将纹理重新绑定回原版药水界面的纹理，否则会导致其他药水或UI渲染混乱（比如产生红屏或贴图错乱）
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
    }
}
