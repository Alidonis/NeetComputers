package com.redtoast.blocks;

import com.redtoast.Computer;
import com.redtoast.neet.BlockRegistery;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LargeEntityComputer extends BlockEntity implements ClientEntityEvents.Load {
    public Computer computer;

    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(BlockRegistery.fetchBlockEntityType("large_computer"), pos, state);
        computer = new Computer(this);
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
        System.out.println(computer.isLoaded());
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
}