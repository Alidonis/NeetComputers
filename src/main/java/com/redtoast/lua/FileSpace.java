package com.redtoast.lua;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class FileSpace extends PersistentState {

    public static int PointerIteration = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("PointerIteration",PointerIteration);
        return nbt;
    }
    public static FileSpace createFromNbt(NbtCompound tag) {
        FileSpace state = new FileSpace();
        state.PointerIteration = tag.getInt("PointerIteration");
        return state;
    }

    public static FileSpace createNew() {
        FileSpace state = new FileSpace();
        state.PointerIteration = 0;
        return state;
    }

    public static FileSpace getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        FileSpace state = serverWorld.getPersistentStateManager().getOrCreate(FileSpace::createFromNbt, FileSpace::createNew, "neetcomputers");
        state.markDirty();
        return state;
    }
}