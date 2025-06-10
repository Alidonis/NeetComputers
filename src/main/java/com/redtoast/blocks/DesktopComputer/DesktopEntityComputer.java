package com.redtoast.blocks.DesktopComputer;

import com.redtoast.Computer;
import com.redtoast.computerSpecs;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.neet.NeetComputers;
import com.redtoast.peripherals.ProjectorAPI;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DesktopEntityComputer extends BlockEntity implements ExtendedScreenHandlerFactory {
    public Computer computer;
    public RGBGraphicsArray graphics;

    public DesktopEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("desktop_computer"), pos, state);
        BlockEntity be = this;
        computer = new Computer(new computerSpecs()
                .setBinaryGraphicsSize(11, 6)
                .setColorGraphicsSize(192,108)
                .setIPS(194000)
                .setMaxCores(3)
                .setCoreUtilizationBonus(5)
                .setMachineName("Desktop Computer")
        ) {
            @Override
            public void saveNBT() {
                markDirty();
            }

            @Override
            public World getWorld() {
                return be.getWorld();
            }

            @Override
            public void refreshBinaryGraphics() {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(be.getPos());
                getBinaryGraphics().writeScreenToPacketBuf(buf);
                assert NeetComputers.BINARY_SCREEN_PACKET != null;

                CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(NeetComputers.BINARY_SCREEN_PACKET, buf);

                if (getWorld() instanceof ServerWorld serverWorld) {
                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                        player.networkHandler.sendPacket(packet);
                    }
                }
            }

            @Override
            public Object getParentEntity() {
                return be;
            }
        };
        computer.attachPeripheral(new ProjectorAPI(computer));
        graphics = computer.getGraphics();
    }

    public void AssignPointers(World world, ItemStack itemStack){
        if (itemStack.hasNbt()) {
            NbtCompound nbt = itemStack.getNbt();
            computer.load(nbt);
        }else{
            MinecraftServer server = world.getServer();
            computer.load(server);
        }
    }

    public ActionResult onUse(PlayerEntity player, BlockState state){
        if (!player.isSneaking() && !player.isUsingItem() && computer.isOn()){
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        if (player.isSneaking()){
            computer.stop();
        }else{
            computer.start();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt = computer.writeNBT(nbt);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        computer.load(nbt);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t) {
        if (!world.isClient()){
            BlockEntity be = world.getBlockEntity(blockPos);
            if (be instanceof DesktopEntityComputer computerBlock) {
                computerBlock.computer.tick(world);
                BlockState current = world.getBlockState(blockPos);
                if (current.get(DesktopBlockComputer.ON) != computerBlock.computer.isOn()) {
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.ON, computerBlock.computer.isOn()), Block.NOTIFY_ALL);
                }
            }
        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        graphics.writeScreenToPacketBuf(buf);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Desktop Computer");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GraphicsScreenHandler(syncId,graphics,computer);
    }
}