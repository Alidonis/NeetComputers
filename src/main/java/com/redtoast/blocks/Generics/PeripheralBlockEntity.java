package com.redtoast.blocks.Generics;

import com.redtoast.Computer;
import com.redtoast.Connections.*;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.events.EventLabel;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

public class PeripheralBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, Exposable {
    private final String typeName;
    private final java.util.List<Computer> computers = new LinkedList<>();
    private UUID uuid = null;
    private String tag = null;
    private final String[] functionTable;

    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String typeName) {
        super(type, pos, state);
        this.typeName = typeName;
        functionTable = APILoader.getFunctions(this);
        if (doAutoCache() && functionTable.length>0) APILoader.preemptiveCache(this.getClass());
    }

    public boolean doAutoCache(){
        return true;
    }

    public final void queueEvent(String eventName, UUID recipient, Object... args){
        if (getWorld()!=null && !getWorld().isClient){
            List buffer = Value.of(args).getValue();
            buffer.addFirst(Value.of(eventName));
            buffer.addFirst(Value.of(getUuid().toString()));
            EventGeneric event = new EventGeneric(typeName, buffer);
            for (Computer computer : computers){
                if (recipient==null || recipient==computer.getUuid()){
                    computer.queueEvent(event, EventLabel.PERIPHERAL);
                }
            }
        }
    }

    public final void queueEvent(String eventName, Object... args){
        queueEvent(eventName, null, args);
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
    public void computerAttached(Computer computer) {
        computers.add(computer);
    }

    @Override
    public void computerDetached(Computer computer) {
        computers.remove(computer);
    }

    public java.util.List<Computer> getAttachedComputers() {
        return Collections.unmodifiableList(computers);
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }

    @Override
    public boolean equals(Object object){
        return object instanceof PeripheralBlockEntity be && be.getUuid().equals(getUuid());
    }
}
