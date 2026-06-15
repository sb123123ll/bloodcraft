package com.qiamao.blood.world;

import com.qiamao.blood.entity.EntityCultistPreacher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class VillageMissionaryHouse extends StructureVillagePieces.Village {
    
    private int averageGroundLvl = -1;

    public VillageMissionaryHouse() {}

    public VillageMissionaryHouse(StructureVillagePieces.Start start, int type, Random rand, StructureBoundingBox boundingBox, EnumFacing facing) {
        super(start, type);
        this.setCoordBaseMode(facing);
        this.boundingBox = boundingBox;
    }

    public static VillageMissionaryHouse createPiece(StructureVillagePieces.Start start, List<StructureComponent> p_175850_1_, Random rand, int p_175850_3_, int p_175850_4_, int p_175850_5_, EnumFacing facing, int p_175850_7_) {
        // NBT is 9x8x9 approx.
        StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p_175850_3_, p_175850_4_, p_175850_5_, 0, 0, 0, 9, 8, 9, facing);
        return StructureComponent.findIntersecting(p_175850_1_, box) != null ? null : new VillageMissionaryHouse(start, p_175850_7_, rand, box, facing);
    }

    @Override
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 8 - 1, 0);
        }

        MinecraftServer server = worldIn.getMinecraftServer();
        if (server != null) {
            TemplateManager templateManager = server.getWorld(0).getStructureTemplateManager();
            Template template = templateManager.getTemplate(server, new ResourceLocation("blood", "missionary_house"));

            if (template != null) {
                PlacementSettings settings = new PlacementSettings()
                        .setReplacedBlock(Blocks.STRUCTURE_VOID)
                        .setBoundingBox(structureBoundingBoxIn)
                        .setIgnoreEntities(false);

                // Setup rotation and mirror based on facing
                if (this.getCoordBaseMode() == EnumFacing.SOUTH) {
                    settings.setRotation(Rotation.CLOCKWISE_180);
                } else if (this.getCoordBaseMode() == EnumFacing.EAST) {
                    settings.setRotation(Rotation.CLOCKWISE_90);
                } else if (this.getCoordBaseMode() == EnumFacing.WEST) {
                    settings.setRotation(Rotation.COUNTERCLOCKWISE_90);
                } else {
                    settings.setRotation(Rotation.NONE);
                }

                BlockPos pos = new BlockPos(this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ);
                template.addBlocksToWorld(worldIn, pos, settings);

                // 铺设草径方块连接原版村庄路径
                // 门在 z=0（北），我们向前铺草径
                for (int i = 0; i < 4; ++i) {
                    for (int j = 3; j <= 5; ++j) {
                        this.clearCurrentPositionBlocksUpwards(worldIn, j, 0, -1 - i, structureBoundingBoxIn);
                        this.replaceAirAndLiquidDownwards(worldIn, Blocks.GRASS_PATH.getDefaultState(), j, -1, -1 - i, structureBoundingBoxIn);
                    }
                }

                // 生成传教士
                spawnCultistPreacher(worldIn, pos, randomIn, settings);
            }
        }

        return true;
    }

    private void spawnCultistPreacher(World world, BlockPos startPos, Random rand, PlacementSettings settings) {
        // 在第一层的床边生成一个传教士。假设床的坐标大概是 (2, 1, 6)，根据旋转进行变换
        BlockPos spawnPos = Template.transformedBlockPos(settings, new BlockPos(2, 1, 6)).add(startPos);
        
        EntityCultistPreacher preacher = new EntityCultistPreacher(world);
        preacher.setLocationAndAngles(
            spawnPos.getX() + 0.5D, 
            spawnPos.getY(), 
            spawnPos.getZ() + 0.5D, 
            rand.nextFloat() * 360.0F, 
            0.0F
        );
        
        // 允许生成，即使是在村庄结构中
        world.spawnEntity(preacher);
    }
}
