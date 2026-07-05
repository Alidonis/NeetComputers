package com.redtoast.Connections;

import com.redtoast.neet.NeetComputersServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public class CableManager {
    private final Map<String, Map<Long, Short>> data = new Hashtable<>();

    public boolean pipeExists(World world, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(world.getDimensionEntry().getIdAsString())) return false;
        if (!data.get(world.getDimensionEntry().getIdAsString()).containsKey(pos.asLong())) return false;
        short value = data.get(world.getDimensionEntry().getIdAsString()).get(pos.asLong());
        return ((value>>pipeType.ordinal())&1)==1;
    }

    public void createPipe(World world, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(world.getDimensionEntry().getIdAsString())) data.put(world.getDimensionEntry().getIdAsString(), new Hashtable<>());
        if (!data.get(world.getDimensionEntry().getIdAsString()).containsKey(pos.asLong())) data.get(world.getDimensionEntry().getIdAsString()).put(pos.asLong(), (short) 0);
        short value = (short) (data.get(world.getDimensionEntry().getIdAsString()).get(pos.asLong()) | (0b1<<pipeType.ordinal()));
        data.get(world.getDimensionEntry().getIdAsString()).put(pos.asLong(), value);
        markDirty();
    }

    public void removePipe(World world, BlockPos pos, PipeType pipeType){
        if (!data.containsKey(world.getDimensionEntry().getIdAsString())) data.put(world.getDimensionEntry().getIdAsString(), new Hashtable<>());
        if (!data.get(world.getDimensionEntry().getIdAsString()).containsKey(pos.asLong())) data.get(world.getDimensionEntry().getIdAsString()).put(pos.asLong(), (short) 0);
        short value = (short) (data.get(world.getDimensionEntry().getIdAsString()).get(pos.asLong()) & (~(0b1<<pipeType.ordinal())));
        if (value==0b0) {
            data.get(world.getDimensionEntry().getIdAsString()).remove(pos.asLong());
            markDirty();
            return;
        }
        data.get(world.getDimensionEntry().getIdAsString()).put(pos.asLong(), value);
        markDirty();
    }

    public void removeBlockEntry(World world, BlockPos blockPos) {
        if (!data.containsKey(world.getDimensionEntry().getIdAsString())) return;
        Short oldEntry = data.get(world.getDimensionEntry().getIdAsString()).remove(blockPos.asLong());
        if (oldEntry!=null) markDirty();
    }

    public BlockPos[] getPipesForRendering(World world, PipeType pipeType, Function<Long, Boolean> conditional){
        if (!data.containsKey(world.getDimensionEntry().getIdAsString())) data.put(world.getDimensionEntry().getIdAsString(), new Hashtable<>());
        short key = (short) (0b1 << pipeType.ordinal());
        ArrayList<BlockPos> output = new ArrayList<>();
        data.get(world.getDimensionEntry().getIdAsString()).forEach((lPos, keys) -> {
            if (((keys & key) != 0b0) && conditional.apply(lPos)) output.add(BlockPos.fromLong(lPos));
        });
        return output.toArray(new BlockPos[0]);
    }

    public static CableManager getInstance(){
        return NeetComputersServer.cableManager;
    }

    public ByteBuf save() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(data.size());
        data.forEach((key, value) -> {
            StringEncoding.encode(buffer, key, Integer.MAX_VALUE);
            buffer.writeLong(value.size());
            value.forEach((pos, raster) -> {
                buffer.writeLong(pos);
                buffer.writeShort(raster);
            });
        });
        return buffer;
    }

    private void markDirty(){
        NeetComputersServer.updateClientPipes();
    }

    public static CableManager read(ByteBuf buffer) {
        CableManager cableManager = new CableManager();
        int dimensions = buffer.readInt();
        for (int i = 0; i < dimensions; i++) {
            String dimension = StringEncoding.decode(buffer, Integer.MAX_VALUE);
            long cap = buffer.readLong();
            Map<Long, Short> map = new Hashtable<>();
            for (long x = 0; x < cap; x++) {
                map.put(buffer.readLong(), buffer.readShort());
            }
            cableManager.data.put(dimension, map);
        }
        return cableManager;
    }
}
