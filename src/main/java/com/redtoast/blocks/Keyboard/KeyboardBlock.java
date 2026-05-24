package com.redtoast.blocks.Keyboard;

import com.mojang.serialization.MapCodec;
import com.redtoast.Connections.PeripheralBlock;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.graphics.screens.KeyboardScreenHandler;
import com.redtoast.neet.BulkRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Language;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Hashtable;

@PeripheralBlock
public class KeyboardBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<KeyboardState> MODEL = EnumProperty.of("model", KeyboardState.class);
    private static final Hashtable<KeyboardState, VoxelShape> SHAPES = new Hashtable<>();
    static {
        final Vector3f minPosNorth = new Vector3f(2, 0, 2);
        final Vector3f maxPosNorth = new Vector3f(14, 1, 9);
        final Vector3f minPosSouth = new Vector3f(2, 0, 7);
        final Vector3f maxPosSouth = new Vector3f(14, 1, 14);
        final Vector3f minPosEast = new Vector3f(2, 0, 2);
        final Vector3f maxPosEast = new Vector3f(9, 1, 14);
        final Vector3f minPosWest = new Vector3f(7, 0, 2);
        final Vector3f maxPosWest = new Vector3f(14, 1, 14);
        final Vector3f minPosNorthWall = new Vector3f(2, 7, 0);
        final Vector3f maxPosNorthWall = new Vector3f(14, 14, 1);
        final Vector3f minPosSouthWall = new Vector3f(2, 7, 15);
        final Vector3f maxPosSouthWall = new Vector3f(14, 14, 16);
        final Vector3f minPosEastWall = new Vector3f(15, 7, 2);
        final Vector3f maxPosEastWall = new Vector3f(16, 14, 14);
        final Vector3f minPosWestWall = new Vector3f(0, 7, 2);
        final Vector3f maxPosWestWall = new Vector3f(1, 14, 14);
        for (KeyboardState state : KeyboardState.values()){
            if (state.isWall()) {
                Vector3f minPos = switch (state.getHorizontalDirection()){
                    case DOWN, NORTH, UP -> minPosNorthWall;
                    case SOUTH -> minPosSouthWall;
                    case WEST -> minPosWestWall;
                    case EAST -> minPosEastWall;
                };
                Vector3f maxPos = switch (state.getHorizontalDirection()){
                    case DOWN, NORTH, UP -> maxPosNorthWall;
                    case SOUTH -> maxPosSouthWall;
                    case WEST -> maxPosWestWall;
                    case EAST -> maxPosEastWall;
                };
                SHAPES.put(state, Block.createCuboidShape(minPos.x, minPos.y - (state.isDown() ? 5 : 0), minPos.z, maxPos.x, maxPos.y - (state.isDown() ? 5 : 0), maxPos.z));
            } else {
                Vector3f minPos = switch (state.getHorizontalDirection()){
                    case DOWN, NORTH, UP -> state.isDown() ? minPosSouth : minPosNorth;
                    case SOUTH -> state.isDown() ? minPosNorth : minPosSouth;
                    case WEST -> state.isDown() ? minPosWest : minPosEast;
                    case EAST -> state.isDown() ? minPosEast : minPosWest;
                };
                Vector3f maxPos = switch (state.getHorizontalDirection()){
                    case DOWN, NORTH, UP -> state.isDown() ? maxPosSouth : maxPosNorth;
                    case SOUTH -> state.isDown() ? maxPosNorth : maxPosSouth;
                    case WEST -> state.isDown() ? maxPosWest : maxPosEast;
                    case EAST -> state.isDown() ? maxPosEast : maxPosWest;
                };
                SHAPES.put(state, Block.createCuboidShape(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z));
            }
        }
    }

    public KeyboardBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(MODEL, KeyboardState.NORTH));
    }

    @Override
    public MapCodec<? extends FacingBlock> getCodec() {
        return null;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.offset(state.get(MODEL).getSupportDirection());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, state.get(MODEL).getSupportDirection().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(MODEL));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
        if (world.getBlockEntity(pos) instanceof KeyboardBlockEntity keyboard){
            player.openHandledScreen(new ExtendedScreenHandlerFactory<KeyboardScreenHandler.Payload>() {
                @Override
                public KeyboardScreenHandler.Payload getScreenOpeningData(ServerPlayerEntity player) {
                    return new KeyboardScreenHandler.Payload();
                }

                @Override
                public Text getDisplayName() {
                    return Text.of(Language.getInstance().get("block.neetcomputers.keyboard"));
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new KeyboardScreenHandler(syncId, keyboard);
                }
            });
        }
        return ActionResult.PASS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        KeyboardState state = KeyboardState.fromDirection(ctx.getPlayer().getHorizontalFacing());
        assert state != null;
        if (ctx.getPlayer().getFacing() != Direction.DOWN || ctx.getWorld().getBlockState(ctx.getBlockPos().down()).isAir()) {
            if (!ctx.getWorld().getBlockState(ctx.getBlockPos().offset(ctx.getPlayer().getHorizontalFacing())).isAir()){
                state = state.getWallVariant();
            }
        }
        if (!ctx.getPlayer().isSneaking()) state = state.getDownVariant();
        return super.getPlacementState(ctx).with(MODEL, state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        return world.getBlockState(pos.offset(state.get(MODEL).getSupportDirection())).isAir() ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MODEL);
    }

    @Nullable
    @Override
    public KeyboardBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new KeyboardBlockEntity(pos, state);
    }
}
