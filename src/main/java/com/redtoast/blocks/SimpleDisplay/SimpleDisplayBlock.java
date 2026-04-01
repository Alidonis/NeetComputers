package com.redtoast.blocks.SimpleDisplay;

import com.mojang.serialization.MapCodec;
import com.redtoast.Connections.PeripheralBlock;
import com.redtoast.blocks.Generics.Displays.ConnectionMapping;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.value.VarType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@PeripheralBlock
public class SimpleDisplayBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final IntProperty STATE = IntProperty.of("state",0, 15);

    public SimpleDisplayBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(STATE, 0).with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        Direction direction = state.get(FACING);
        new ConnectionMapping(world, pos, direction, (pos2) -> (world.getBlockEntity(pos2) instanceof SimpleDisplayBlockEntity && world.getBlockState(pos2).get(SimpleDisplayBlock.FACING).equals(direction))).start();
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        //holy run-on sentence
        if (world!=null && !world.isClient && (boolean) ConfigLoader.getServerConfig("shift-click-to-clear-displays") && player.getActiveItem() == ItemStack.EMPTY && player.isSneaking() && world.getBlockEntity(pos) instanceof SimpleDisplayBlockEntity simpleDisplayBlockEntity && !simpleDisplayBlockEntity.callFunction(null, "clear").instanceOf(VarType.EXCEPTION) && !simpleDisplayBlockEntity.callFunction(null, "draw").instanceOf(VarType.EXCEPTION)) player.sendMessage(Text.of("Screen Cleared!"));
        return ActionResult.PASS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == BulkRegistry.fetchBlockEntityType("simple_display") ? SimpleDisplayBlockEntity::tick : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SimpleDisplayBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE, Properties.HORIZONTAL_FACING);
    }
}
