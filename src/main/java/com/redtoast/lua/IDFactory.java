package com.redtoast.lua;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class IDFactory extends PersistentState {

    public static int PointerIteration = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("PointerIteration",PointerIteration);
        return nbt;
    }
    public static IDFactory createFromNbt(NbtCompound tag) {
        IDFactory state = new IDFactory();
        state.PointerIteration = tag.getInt("PointerIteration");
        return state;
    }

    public static IDFactory createNew() {
        IDFactory state = new IDFactory();
        state.PointerIteration = 0;
        return state;
    }

    public static IDFactory getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        IDFactory state = serverWorld.getPersistentStateManager().getOrCreate(IDFactory::createFromNbt, IDFactory::createNew, "neetcomputers");
        state.markDirty();
        return state;
    }
}