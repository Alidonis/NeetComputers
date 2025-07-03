package com.redtoast.blocks.generic;

import com.redtoast.Computer;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.computerSpecs;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.ComputerStorage;
import com.redtoast.neet.NeetComputers;
import com.redtoast.peripherals.ProjectorAPI;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
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

import java.util.Objects;

public class ComputerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private Computer computer;
    private boolean collectedComputer = false;
    private RGBGraphicsArray graphics;

    public ComputerBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, computerSpecs specifications) {
        super(type, pos, state);
        ComputerBlockEntity be = this;
        computer = new Computer(specifications) {
            @Override
            public void saveNBT() {
                markDirty();
            }

            @Override
            public boolean isClient() {
                return be.getWorld().isClient();
            }

            @Override
            public void refreshBinaryGraphics() {
                if (specifications.doesBinaryGraphics) renderBinaryGraphics(Objects.requireNonNull(getBinaryGraphics()));
            }

            @Override
            public Object getParentEntity() {
                return be;
            }
        };
        if (specifications.doesBinaryGraphics) computer.attachPeripheral(new ProjectorAPI(computer));
        graphics = computer.getGraphics();
    }

    public void renderBinaryGraphics(BinaryGraphicsArray graphics){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(getPos());
        graphics.writeScreenToPacketBuf(buf);
        assert NeetComputers.BINARY_SCREEN_PACKET != null;

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(NeetComputers.BINARY_SCREEN_PACKET, buf);

        if (getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
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

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt = computer.writeNBT(nbt);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("uuid") && !collectedComputer){
            collectedComputer=true;
            if (ComputerStorage.storage.contains(nbt.getUuid("uuid"))){
                computer = ComputerStorage.storage.get(nbt.getUuid("uuid"));
                graphics = computer.getGraphics();
            }
        }
        computer.load(nbt);
    }

    public ActionResult onUse(PlayerEntity player, BlockState state){
        if (!player.isSneaking() && !player.isUsingItem() && computer.isOn()){
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        if (player.isSneaking()){
            if (computer.isCrashed()){
                computer.reset();
            }else{
                computer.stop();
            }
        }else{
            if (computer.isCrashed()) player.sendMessage(Text.literal(computer.getCrashMessage()));
            computer.start();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        graphics.writeScreenToPacketBuf(buf);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(computer.getSpecifications().MachineName);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!computer.getSpecifications().doesBinaryGraphics) return null;
        return new GraphicsScreenHandler(syncId,graphics,computer);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t) {
        if (!world.isClient()){
            BlockEntity be = world.getBlockEntity(blockPos);
            if (be instanceof ComputerBlockEntity computerBlock) {
                computerBlock.computer.tick(world);
                if (computerBlock.computer.isOn()){
                    if (!ComputerStorage.storage.contains(computerBlock.computer)){
                        ComputerStorage.storage.put(computerBlock.computer.getUuid(), computerBlock.computer);
                    }
                }else{
                    ComputerStorage.storage.remove(computerBlock.computer.getUuid());
                }
                BlockState current = world.getBlockState(blockPos);
                if (current.get(DesktopBlockComputer.ON) != computerBlock.computer.isOn()) {
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.ON, computerBlock.computer.isOn()), Block.NOTIFY_ALL);
                }
                if (current.get(DesktopBlockComputer.CRASHED) != computerBlock.computer.isCrashed()) {
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.CRASHED, computerBlock.computer.isCrashed()), Block.NOTIFY_ALL);
                }
            }
        }
    }

    public void unload(){
        ComputerStorage.storage.remove(computer.getUuid());
    }

    public Computer getComputer() {return computer;}
}
