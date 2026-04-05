package com.redtoast.blocks.Generics;

import com.redtoast.Compat.GetCC;
import com.redtoast.Connections.*;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.config.ConfigLoader;
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
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeripheralBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, Exposable {
    private final String typeName;
    private UUID uuid = null;
    private String tag = null;
    private final String[] functionTable;
    private final ConcurrentLinkedQueue<EventPackage> sendQueue = new ConcurrentLinkedQueue<>();

    private record EventPackage(EventGeneric event, UUID address){}

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
            sendQueue.add(new EventPackage(new EventGeneric(typeName, buffer), recipient));
            if (!NeetComputersServer.peripheralUpdateQueue.contains(this)) NeetComputersServer.peripheralUpdateQueue.add(this);
        }
    }

    public final void queueEvent(String eventName, Object... args){
        queueEvent(eventName, null, args);
    }

    @Deprecated
    public final void processEventQueue(){
        java.util.List<ComputerBlockEntity> computers = scanForComputersInternal();
        while (!sendQueue.isEmpty()){
            EventPackage eventPackage = sendQueue.poll();
            for (ComputerBlockEntity computer : computers){
                if (eventPackage.address==null || eventPackage.address==computer.getUuid()){
                    computer.getComputer().queueEvent(eventPackage.event, EventLabel.PERIPHERAL);
                }
            }
        }
    }

    private java.util.List<ComputerBlockEntity> scanForComputersInternal() {
        LinkedList<BlockPos> todoList = new LinkedList<>();
        LinkedList<BlockPos> investigated = new LinkedList<>();
        LinkedList<ComputerBlockEntity> computers = new LinkedList<>();
        todoList.add(getPos());

        World world = getWorld();

        while (!todoList.isEmpty()){
            BlockPos current = todoList.getFirst();
            todoList.remove();
            for (Direction direction : Direction.values()){
                BlockPos investigating = current.offset(direction);
                if (investigated.contains(investigating)) {
                    continue;
                }
                BlockState block = world.getBlockState(investigating);
                if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)){
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof ComputerBlockEntity computer && isntDuplicate(computers, computer)){
                        computers.add(computer);
                    }
                }
                if (CableManager.getInstance().pipeExists(world.getDimension(), investigating, PipeType.PERIPHERAL)) {
                    todoList.add(investigating);
                }
                else if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)) {
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof ComputerBlockEntity computer && isntDuplicate(computers, computer)){
                        computers.add(computer);
                    }
                }
                investigated.add(investigating);
            }
        }

        return computers;
    }

    private boolean isntDuplicate(LinkedList<ComputerBlockEntity> computers, ComputerBlockEntity duplicate){
        for (ComputerBlockEntity computer : computers) if (computer.getUuid().equals(duplicate.getUuid())) return false;
        return true;
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

    @Override
    public boolean equals(Object object){
        return object instanceof PeripheralBlockEntity be && be.getUuid().equals(getUuid());
    }
}
