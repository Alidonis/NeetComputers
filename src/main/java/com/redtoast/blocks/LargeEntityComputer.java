package com.redtoast.blocks;

import com.redtoast.Computer;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.BlockRegistery;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class LargeEntityComputer extends BlockEntity implements ExtendedScreenHandlerFactory, ClientEntityEvents.Load {
    public Computer computer;
    public RGBGraphicsArray graphics;

    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(BlockRegistery.fetchBlockEntityType("large_computer"), pos, state);
        computer = new Computer(this);
        computer.
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

    public ActionResult onUse(PlayerEntity player){
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
                computerBlock.computer.Tick();
            }
        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        Vector2i size = graphics.getSize();
        int y = size.y();
        buf.writeInt(y);
        buf.writeInt(size.x());
        for (int i=0; i < y; i++) {
            buf.writeIntArray(graphics.pixels[y]);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("computer menu?!?!?!?!?!?!?!");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GraphicsScreenHandler(syncId,graphics);
    }
}