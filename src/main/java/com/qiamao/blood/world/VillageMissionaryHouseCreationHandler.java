package com.qiamao.blood.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;

import java.util.List;
import java.util.Random;

public class VillageMissionaryHouseCreationHandler implements IVillageCreationHandler {

    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i) {
        // 每个村庄有28%概率生成一个传教士屋，且最多生成1个
        // i 是当前生成的组件数量或者说是生成参数，原版用它调整生成。
        // 第一个参数是权重(weight)，第二个参数是限制数量(limit)
        return new StructureVillagePieces.PieceWeight(VillageMissionaryHouse.class, 28, 1);
    }

    @Override
    public Class<?> getComponentClass() {
        return VillageMissionaryHouse.class;
    }

    @Override
    public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5) {
        // 使用村庄中心坐标作为种子，确保每个村庄有固定 28% 的概率允许生成
        long seed = (long)startPiece.getBoundingBox().minX * 341873128712L + (long)startPiece.getBoundingBox().minZ * 132897987541L;
        Random villageRandom = new Random(seed);
        if (villageRandom.nextInt(100) < 28) {
            return VillageMissionaryHouse.createPiece(startPiece, pieces, random, p1, p2, p3, facing, p5);
        }
        return null;
    }
}
