package com.qiamao.blood.world;

import com.qiamao.blood.entity.EntityCultistPreacher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Map;
import java.util.Random;

public class WorldGenMissionaryHouse extends WorldGenerator {

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        MinecraftServer server = worldIn.getMinecraftServer();
        if (server == null) {
            return false;
        }

        TemplateManager templateManager = server.getWorld(0).getStructureTemplateManager();
        Template template = templateManager.getTemplate(server, new ResourceLocation("blood", "missionary_house"));

        if (template == null) {
            return false;
        }

        // 随机旋转
        Rotation[] rotations = Rotation.values();
        Rotation rotation = rotations[rand.nextInt(rotations.length)];

        PlacementSettings settings = new PlacementSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setReplacedBlock(Blocks.STRUCTURE_VOID)
                .setIgnoreEntities(false);

        // NBT 结构有地下部分，根据您的设定，y第8格算地下室（即地下部分深8格）
        // 所以我们需要将整个结构向下偏移 8 格，确保地下室完美埋入地下
        BlockPos generatePos = position.down(8);
        
        BlockPos size = template.getSize();

        // 放置结构，忽略模板中的 STRUCTURE_VOID（外围的结构空位不会吞噬自然地形）
        settings.setReplacedBlock(Blocks.STRUCTURE_VOID);
        template.addBlocksToWorld(worldIn, generatePos, settings);

        // 生成传教士
        spawnCultistPreacher(worldIn, generatePos, rand, settings);

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
        
        world.spawnEntity(preacher);
    }
}