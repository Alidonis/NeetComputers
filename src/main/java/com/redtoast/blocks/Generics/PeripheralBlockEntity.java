package com.redtoast.blocks.Generics;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.VarType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PeripheralBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, Exposable {
    private final String typeName;
    private UUID uuid = null;
    private Table callTable = null;
    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String typeName) {
        super(type, pos, state);
        this.typeName = typeName;
        callTable = APILoader.TableizeAPI(this, null);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (uuid==null) uuid = UUID.randomUUID();
        nbt.putUuid("peripheralUUID", uuid);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("peripheralUUID")) uuid = nbt.getUuid("peripheralUUID");
    }

    @Override
    public String[] getFunctionNames() {
        return callTable.getKeysString();
    }

    @Override
    public Value<?> callFunction(String name, Value<?>... Args) {
        AtomicReference<Value<?>> dummy = new AtomicReference<>();
        callTable.foreach((key, value) -> {
            if (key.getValue().equals(name)) {
                if (value.instanceOf(VarType.FUNCTION)) dummy.set(value.toFunction().invoke(new FunctionInput(new LinkedList<>(List.of(Args)))));
            }
        });
        markDirty();
        return dummy.get()==null ? Value.asError("Cant Find Function '"+name+"'") : dummy.get();
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public UUID getUuid() {
        if (uuid==null) {
            uuid = UUID.randomUUID();
            markDirty();
        }
        return uuid;
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }
}
