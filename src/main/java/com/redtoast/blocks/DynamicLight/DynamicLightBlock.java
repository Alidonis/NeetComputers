package com.redtoast.blocks.DynamicLight;

import com.redtoast.Connections.PeripheralBlock;
import com.redtoast.neet.BulkRegistery;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@PeripheralBlock
public class DynamicLightBlock extends Block implements BlockEntityProvider {
    public static final IntProperty LUMINANCE = IntProperty.of("luminance",0, 15);

    public DynamicLightBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(LUMINANCE, 0));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == BulkRegistery.fetchBlockEntityType("dynamic_light") ? DynamicLightBlockEntity::tick : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DynamicLightBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LUMINANCE);
    }
}
