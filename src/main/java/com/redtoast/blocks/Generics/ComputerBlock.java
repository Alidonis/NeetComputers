package com.redtoast.blocks.Generics;

import com.mojang.serialization.MapCodec;
import com.redtoast.Connections.PeripheralBlock;
import com.redtoast.blocks.ComputerDataComponent;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@PeripheralBlock
public abstract class ComputerBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final BooleanProperty ON = BooleanProperty.of("on");
    public static final BooleanProperty CRASHED = BooleanProperty.of("crashed");
    public static final IntProperty STATE = IntProperty.of("state",0,3);

    public ComputerBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ON, false).with(CRASHED, false).with(STATE,0).with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    public abstract BlockEntityType<? extends BlockEntity> getType();

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ON, CRASHED, STATE, Properties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == getType() ? ComputerBlockEntity::tick : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity computer) {
                computer.AssignPointers(world, itemStack);
                if (computer.getComputer().getBuild().entrypoint.isEmpty() || computer.getComputer().getBuild().partitions.isEmpty()){

                }
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ComputerBlockEntity computer) {
                ItemStack item = asItem().getDefaultStack();
                item.setCount(1);
                item.set(ComputerDataComponent.TYPE, computer.getComputer().saveToItem());
                Block.dropStack(world, pos, item);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ActionResult allowed;
        if (!world.isClient){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ComputerBlockEntity computer) {
                allowed = computer.onUse(player, state);
            }else{
                allowed = ActionResult.SUCCESS;
            }
        }else{
            allowed = ActionResult.SUCCESS;
        }

        return allowed;
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
    }
}
