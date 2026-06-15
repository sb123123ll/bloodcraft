package com.qiamao.blood.item;

import net.minecraft.item.Item;

/**
 * 实体脸部图标物品
 * 用于成就图标，通过渲染器硬编码显示实体纹理的脸部
 */
public class ItemEntityFace extends Item {
    
    private final String entityName;
    
    public ItemEntityFace(String entityName) {
        this.entityName = entityName;
    }
    
    public String getEntityName() {
        return this.entityName;
    }
}
