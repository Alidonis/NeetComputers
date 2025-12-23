package com.redtoast.Connections;

import com.redtoast.neet.NeetComputersServer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.function.Function;

public class CableManager {
    private final Hashtable<String, Hashtable<Long, Short>> data = new Hashtable<>();

    public boolean pipeExists(DimensionType dimensionType, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(dimensionType.toString())) return false;
        if (!data.get(dimensionType.toString()).containsKey(pos.asLong())) return false;
        short value = data.get(dimensionType.toString()).get(pos.asLong());
        return (value<<pipeType.ordinal()&1)==1;
    }

    public void createPipe(DimensionType dimensionType, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(dimensionType.toString())) data.put(dimensionType.toString(), new Hashtable<>());
        if (!data.get(dimensionType.toString()).containsKey(pos.asLong())) data.get(dimensionType.toString()).put(pos.asLong(), (short) 0);
        short value = (short) (data.get(dimensionType.toString()).get(pos.asLong()) | (1>>pipeType.ordinal()));
        data.get(dimensionType.toString()).put(pos.asLong(), value);
        markDirty();
    }

    public void removePipe(DimensionType dimensionType, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(dimensionType.toString())) data.put(dimensionType.toString(), new Hashtable<>());
        if (!data.get(dimensionType.toString()).containsKey(pos.asLong())) data.get(dimensionType.toString()).put(pos.asLong(), (short) 0);
        short value = (short) (data.get(dimensionType.toString()).get(pos.asLong()) & (~(1>>pipeType.ordinal())));
        if (value==0) {
            data.get(dimensionType.toString()).remove(pos.asLong());
            markDirty();
            return;
        }
        data.get(dimensionType.toString()).put(pos.asLong(), value);
        markDirty();
    }

    public void removeBlockEntry(DimensionType dimensionType, BlockPos blockPos) {
        if (!data.containsKey(dimensionType.toString())) return;
        Short oldEntry = data.get(dimensionType.toString()).remove(blockPos.asLong());
        if (oldEntry!=null) markDirty();
    }

    public BlockPos[] getPipesForRendering(DimensionType dimensionType, PipeType pipeType, Function<Long, Boolean> conditional){
        if (!data.containsKey(dimensionType.toString())) data.put(dimensionType.toString(), new Hashtable<>());
        short key = (short) (1 >> pipeType.ordinal());
        ArrayList<BlockPos> output = new ArrayList<>();
        data.get(dimensionType.toString()).forEach((lPos, keys) -> {
            if (((keys & key) != 0) && conditional.apply(lPos)) output.add(BlockPos.fromLong(lPos));
        });
        return output.toArray(new BlockPos[0]);
    }

    public static CableManager getInstance(){
        return NeetComputersServer.cableManager;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        data.forEach(((dimensionType, longShortHashtable) -> {
            NbtCompound subCom = new NbtCompound();
            longShortHashtable.forEach((pos, key) -> {
                subCom.put(pos.toString(), NbtShort.of(key));
            });
            nbt.put(dimensionType, subCom);
        }));
        return nbt;
    }

    private void markDirty(){
        NeetComputersServer.updateClientPipes();
    }

    public static CableManager createFromNbt(NbtCompound tag) {
        CableManager cableManager = new CableManager();
        tag.getKeys().forEach((key) -> {
            NbtCompound nbt = (NbtCompound) tag.get(key);
            Hashtable<Long, Short> table = new Hashtable<>();
            nbt.getKeys().forEach((posS) -> table.put(Long.valueOf(posS), ((NbtShort) nbt.get(posS)).shortValue()));
            cableManager.data.put(key, table);
        });
        return cableManager;
    }
}
