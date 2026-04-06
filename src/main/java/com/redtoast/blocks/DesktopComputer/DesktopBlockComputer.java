package com.redtoast.blocks.DesktopComputer;

import com.redtoast.blocks.Generics.ComputerBlock;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.NeetComputersServer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DesktopBlockComputer extends ComputerBlock {
    public DesktopBlockComputer(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getType() {
        return NeetComputersServer.desktopComputerType;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return VoxelShapes.cuboid(1f / 16f, 0f, 2f / 16f, 15f / 16f, 13f / 16f, 15f / 16f);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DesktopEntityComputer(pos, state);
    }
}