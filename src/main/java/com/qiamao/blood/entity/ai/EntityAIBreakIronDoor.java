package com.qiamao.blood.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumDifficulty;

public class EntityAIBreakIronDoor extends EntityAIBreakDoor {
    private int breakingTime;
    private int previousBreakProgress = -1;
    // 5 to 7 hits. (Zombie is usually 240 ticks)
    // Here we'll use 100 to 140 ticks (5 to 7 seconds roughly 5-7 hits if hitting 1 per sec)
    private final int breakThreshold;

    public EntityAIBreakIronDoor(EntityLiving entityIn) {
        super(entityIn);
        this.breakThreshold = 100 + entityIn.getRNG().nextInt(41);
    }

    @Override
    public boolean shouldExecute() {
        if (!super.shouldExecute()) {
            return false;
        }
        
        Block block = this.entity.world.getBlockState(this.doorPosition).getBlock();
        // 任何难度都能触发，不仅限于困难，且只对铁门生效
        return block instanceof BlockDoor && block.getDefaultState().getMaterial() == Material.IRON;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.breakingTime = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        double d0 = this.entity.getDistanceSq(this.doorPosition);
        boolean flag = this.breakingTime <= this.breakThreshold;
        Block block = this.entity.world.getBlockState(this.doorPosition).getBlock();
        return d0 < 4.0D && flag && block instanceof BlockDoor && block.getDefaultState().getMaterial() == Material.IRON;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, -1);
    }

    @Override
    public void updateTask() {
        super.updateTask();

        // 播放僵尸砸门声音
        if (this.entity.getRNG().nextInt(20) == 0) {
            this.entity.world.playEvent(1019, this.doorPosition, 0);
        }

        ++this.breakingTime;
        int i = (int)((float)this.breakingTime / (float)this.breakThreshold * 10.0F);

        if (i != this.previousBreakProgress) {
            this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, i);
            this.previousBreakProgress = i;
        }

        if (this.breakingTime == this.breakThreshold && this.entity.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            this.entity.world.setBlockToAir(this.doorPosition);
            this.entity.world.playEvent(1021, this.doorPosition, 0); // 破门音效
            this.entity.world.playEvent(2001, this.doorPosition, Block.getIdFromBlock(Blocks.IRON_DOOR)); // 铁门碎片粒子效果
        }
    }
}
