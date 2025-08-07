package com.redtoast.blocks;

import com.redtoast.simulation.connectionManager.ConnectionProvider;
import com.redtoast.simulation.networkInterfaces.NetworkConsumer;
import com.redtoast.simulation.networkInterfaces.NetworkProvider;
import com.redtoast.simulation.peripheralInterfaces.PeripheralConsumer;
import com.redtoast.simulation.peripheralInterfaces.PeripheralProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;

public abstract class GenericConsumerBlock extends BlockEntity {
    public GenericConsumerBlock(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private record providerBundle(BlockPos position, ConnectionProvider provider) {}
    private final LinkedList<providerBundle> providers = new LinkedList<>();
    private boolean loaded = false;

    public final void connectToProvider(BlockEntity block){
        if (block instanceof ConnectionProvider provider){
            if (provider instanceof PeripheralProvider peripheralProvider && this instanceof PeripheralConsumer consumer){
                providers.add(new providerBundle(block.getPos(), peripheralProvider));
                attachPeripheral(peripheralProvider);
            }
            if (provider instanceof NetworkProvider networkProvider && this instanceof NetworkConsumer consumer){
                providers.add(new providerBundle(block.getPos(), networkProvider));
                connectToNetwork(networkProvider);
            }
        }
    }

    public abstract void attachPeripheral(PeripheralProvider api);

    public abstract void connectToNetwork(NetworkProvider networkProvider);

    @Override
    public void readNbt(NbtCompound nbt){
        loaded = true;
        if (nbt.contains("connections")){
            long[] coordinates = nbt.getLongArray("connections");
            for (long data : coordinates){
                boolean type = (data & 0x1) == 1;
                data <<= 1;
                BlockPos position = BlockPos.fromLong(data);
                assert getWorld() != null;
                BlockEntity block = getWorld().getBlockEntity(position);
                if (block instanceof ConnectionProvider provider){
                    if (provider instanceof PeripheralProvider peripheralProvider && !type){
                        providers.add(new providerBundle(block.getPos(), peripheralProvider));
                        attachPeripheral(peripheralProvider);
                    }
                    if (provider instanceof NetworkProvider networkProvider && type){
                        providers.add(new providerBundle(block.getPos(), networkProvider));
                        connectToNetwork(networkProvider);
                    }
                }
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt){
        LinkedList<Long> packedData = new LinkedList<>();
        for (providerBundle data : providers){
            int tick = 0;
            if (data.provider instanceof NetworkProvider){
                tick = 1;
            }
            packedData.add(data.position.asLong() >> 1 | tick);
        }
        nbt.putLongArray("connections", packedData);
    }
}
