package com.redtoast.simulation;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class IDFactory extends PersistentState {

    public static int PointerIteration = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("PointerIteration",PointerIteration);
        return nbt;
    }
    public static IDFactory createFromNbt(NbtCompound tag) {
        IDFactory state = new IDFactory();
        PointerIteration = tag.getInt("PointerIteration");
        return state;
    }

    public static IDFactory createNew() {
        IDFactory state = new IDFactory();
        PointerIteration = 0;
        return state;
    }

    public static IDFactory getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        IDFactory state = serverWorld.getPersistentStateManager().getOrCreate(new Type<>(IDFactory::createNew, (compound, wrapperLookup) -> createFromNbt(compound), DataFixTypes.LEVEL), "neetcomputers");
        state.markDirty();
        return state;
    }
}