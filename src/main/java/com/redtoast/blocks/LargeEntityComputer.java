package com.redtoast.blocks;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.neet.NeetComputers;
import com.redtoast.peripherals.ProjectorAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
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

public class LargeEntityComputer extends BlockEntity implements ExtendedScreenHandlerFactory, ClientEntityEvents.Load {
    public Computer computer;
    public RGBGraphicsArray graphics;

    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("large_computer"), pos, state);
        BlockEntity be = this;
        computer = new Computer(new ComputerSpecs()
                .setGraphics(12, 11)
                .setColorGraphics(192,108)
                .setIPS(580000, 500)
                .setMaxCores(6)
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
        };
        computer.attachPeripheral(new ProjectorAPI(this));
        graphics = computer.getGraphics();
    }

    public void AssignPointers(World world, ItemStack itemStack){
        if (itemStack.hasNbt()) {
            NbtCompound nbt = itemStack.getNbt();
            computer.Load(nbt);
        }else{
            MinecraftServer server = world.getServer();
            computer.Load(server);
        }
    }

    public ActionResult onUse(PlayerEntity player, BlockState state){
        if (!player.isSneaking() && !player.isUsingItem() && computer.IsOn()){
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        if (player.isSneaking()){
            computer.Stop();
        }else{
            computer.Start();
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
        computer.Load(nbt);
    }

    @Override
    public void onLoad(Entity entity, ClientWorld world) {
        if (this.world != null && !this.world.isClient) {
            computer.staticStart();
        }
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t) {
        if (!world.isClient()){
            BlockEntity be = world.getBlockEntity(blockPos);
            if (be instanceof LargeEntityComputer computerBlock) {
                computerBlock.computer.Tick(world);
                BlockState current = world.getBlockState(blockPos);
                if (current.get(LargeBlockComputer.ON) != computerBlock.computer.IsOn()) {
                    world.setBlockState(blockPos, current.with(LargeBlockComputer.ON, computerBlock.computer.IsOn()), Block.NOTIFY_ALL);
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
        return Text.literal("Large Computer");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GraphicsScreenHandler(syncId,graphics,this);
    }
}