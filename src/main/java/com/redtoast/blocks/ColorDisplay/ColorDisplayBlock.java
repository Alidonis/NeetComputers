package com.redtoast.blocks.ColorDisplay;

import com.mojang.serialization.MapCodec;
import com.redtoast.Connections.PeripheralBlock;
import com.redtoast.blocks.Generics.Displays.MultiblockDisplayEntity;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.value.Value;
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
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@PeripheralBlock
public class ColorDisplayBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public ColorDisplayBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(MultiblockDisplayEntity.STATE, 0).with(FACING, Direction.NORTH).with(MultiblockDisplayEntity.LEADER, true).with(MultiblockDisplayEntity.GROUP, false));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.getBlockEntity(pos) instanceof MultiblockDisplayEntity multiblockDisplayEntity) multiblockDisplayEntity.reconnect();
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        //holy run-on sentence
        if (world!=null && !world.isClient && (boolean) ConfigLoader.getServerConfig("shift-click-to-clear-displays") && player.getActiveItem() == ItemStack.EMPTY && player.isSneaking() && world.getBlockEntity(pos) instanceof ColorDisplayBlockEntity simpleDisplayBlockEntity && !simpleDisplayBlockEntity.callFunction(null, "fill", Value.of(0), Value.of(0), Value.of(0)).instanceOf(VarType.EXCEPTION) && !simpleDisplayBlockEntity.callFunction(null, "draw").instanceOf(VarType.EXCEPTION)) player.sendMessage(Text.of("Screen Cleared!"));
        return ActionResult.PASS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(MultiblockDisplayEntity.GROUP, ctx.getPlayer()!=null & ctx.getPlayer().isSneaking());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == BulkRegistry.fetchBlockEntityType("color_display") ? MultiblockDisplayEntity::tick : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ColorDisplayBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MultiblockDisplayEntity.STATE, Properties.HORIZONTAL_FACING, MultiblockDisplayEntity.LEADER, MultiblockDisplayEntity.GROUP);
    }
}
