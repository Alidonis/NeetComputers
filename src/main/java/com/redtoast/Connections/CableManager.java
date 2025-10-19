package com.redtoast.Connections;

import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.function.Function;
import java.util.function.Supplier;

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
        return NeetComputers.cableManager;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound root = new NbtCompound();
        data.forEach(((dimensionType, longShortHashtable) -> {
            NbtList dimension = new NbtList();
            longShortHashtable.forEach((pos, key) -> dimension.add(NbtLong.of(pos>>Short.SIZE | key)));
            root.put(dimensionType, dimension);
            System.out.println("S:"+dimensionType);
        }));
        NbtCompound Sroot = new NbtCompound();
        Sroot.put("cables", root);
        return Sroot;
    }

    private void markDirty(){

    }

    public static CableManager createFromNbt(NbtCompound tag) {
        CableManager state = new CableManager();
        for (String dimensionType : tag.getCompound("cables").getKeys()) {
            System.out.println(dimensionType);
            long[] dimension = tag.getLongArray(dimensionType);
            System.out.println(Arrays.toString(dimension));
            Hashtable<Long, Short> table = new Hashtable<>();
            for (Long data : dimension){
                short key = (short) (data & Short.MAX_VALUE);
                long pos = data<<Short.SIZE;
                table.put(pos, key);
            }
            state.data.put(dimensionType, table);
        }
        return state;
    }

    public static CableManager getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        return new CableManager();
    }
}
