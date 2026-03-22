package com.redtoast.blocks.Generics;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PeripheralBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, Exposable {
    private final String typeName;
    private UUID uuid = null;
    private String tag = null;
    private final String[] functionTable;
    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String typeName) {
        super(type, pos, state);
        this.typeName = typeName;
        functionTable = APILoader.getFunctions(this);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (uuid==null) uuid = UUID.randomUUID();
        nbt.putUuid("peripheralUUID", uuid);
        if (tag!=null) nbt.putString("peripheralTag", tag);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("peripheralUUID")) uuid = nbt.getUuid("peripheralUUID");
        if (nbt.contains("peripheralTag")) tag = nbt.getString("peripheralTag");
    }

    @Override
    public String[] getFunctionNames() {
        return functionTable;
    }

    @Override
    public Value<?> callFunction(Runtime runtime, String name, Value<?>... Args) {
        try{
            return APILoader.searchAndCall(this, runtime, name, Args);
        } catch (APILoader.LoaderError e) {
            APILoader.printJavaError(e);
            return Value.asError("API loading error");
        }
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
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(@NotNull String tag) {
        this.tag = tag.isBlank() ? null : tag.trim();
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }
}
