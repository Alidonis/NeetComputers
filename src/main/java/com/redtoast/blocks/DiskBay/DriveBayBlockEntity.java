package com.redtoast.blocks.DiskBay;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.graphics.screens.DriveBayScreenHandler;
import com.redtoast.items.generics.DiskItem;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.DiskError;
import com.redtoast.simulation.FS.DiskSystem;
import com.redtoast.simulation.FS.FileImplementations.Filepath;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.events.EventLabel;
import com.redtoast.simulation.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DriveBayBlockEntity extends LootableContainerBlockEntity implements PeripheralProvider, PipeRenderSource, Exposable {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private ItemStack lastItem = null;
    private DiskSystem system = null;
    private boolean loaded = false;
    private String itemName = "minecraft:air";
    private int clock = 0;

    private final List<Computer> computers = new LinkedList<>();
    private UUID uuid = null;
    private String tag = null;
    private final String[] functionTable;

    public DriveBayBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("drive_bay"), pos, state);
        functionTable = APILoader.getFunctions(this);
    }

    @Override
    protected Text getContainerName() {
        return Text.of(Language.getInstance().get("block.neetcomputers.drive_bay"));
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registryLookup);
        }
        if (nbt.contains("peripheralUUID")) uuid = nbt.getUuid("peripheralUUID");
        if (nbt.contains("peripheralTag")) tag = nbt.getString("peripheralTag");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registryLookup);
        }
        if (uuid==null) uuid = UUID.randomUUID();
        nbt.putUuid("peripheralUUID", uuid);
        if (tag!=null) nbt.putString("peripheralTag", tag);
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void tick(World world) {
        clock++;
        clock%=10;
        if (!world.isClient() && (lastItem==null || !Objects.equals(lastItem.getTranslationKey(), inventory.getFirst().getTranslationKey()))){
            loaded = false;
            ItemStack item = inventory.getFirst();
            itemName = item.isEmpty() ? "minecraft:air" : item.getRegistryEntry().getIdAsString();
            //close old disk
            if (system!=null) {
                for (Computer computer : computers) {
                    computer.queueEvent(new EventGeneric(getTypeName(), Value.of(getUuid().toString()), Value.of("DiskDetached")), EventLabel.PERIPHERAL);
                    computer.getFileSystem().removeDisk(system);
                }
                system.update();
            }
            system = null;
            //open new disk
            if (item.getItem() instanceof DiskItem diskItem) {
                try{
                    system = diskItem.generateSystem(item, getUuid(), this);
                } catch (DiskError e) {
                    APILoader.errorLogger.warn("Drive bay failed to load drive");
                    APILoader.printJavaError(e);
                    lastItem = inventory.getFirst();
                    Objects.requireNonNull(getWorld()).setBlockState(getPos(), getWorld().getBlockState(getPos()).with(DriveBayBlock.LOADED, !item.isEmpty()), Block.NOTIFY_ALL);
                    return;
                }
                for (Computer computer : computers) {
                    computer.queueEvent(new EventGeneric(getTypeName(), Value.of(getUuid().toString()), Value.of("DiskAttached")), EventLabel.PERIPHERAL);
                    computer.getFileSystem().addDisk(system);
                }
                loaded = true;
            }
            Objects.requireNonNull(getWorld()).setBlockState(getPos(), getWorld().getBlockState(getPos()).with(DriveBayBlock.LOADED, !item.isEmpty()), Block.NOTIFY_ALL);
        }
        if (!world.isClient() && loaded && clock==5) {
            boolean bootable = false;
            if (system.isBootable()){
                try{
                    Filepath bootLocation = system.getEntrypoint();
                    if (bootLocation!=null && bootLocation.canRead()) bootable = true;
                } catch (DiskError ignored) {}
            }
            if (inventory.getFirst().getItem() instanceof DiskItem diskItem) diskItem.markBootable(inventory.getFirst(), bootable, this);
        }
        lastItem = inventory.getFirst();
    }

    @Exposed
    public boolean hasDisk(){
        return loaded;
    }

    @Exposed(nameOverride = "getItem")
    public String getItemId(){
        return itemName.equals("minecraft:air") ? null : itemName;
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
        return "neetcomputers:drive_bay";
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
        if (loaded) {
            computer.queueEvent(new EventGeneric(getTypeName(), Value.of(getUuid().toString()), Value.of("DiskAttached")), EventLabel.PERIPHERAL);
            computer.getFileSystem().addDisk(system);
        }
        computers.add(computer);
    }

    @Override
    public void computerDetached(Computer computer) {
        if (loaded) {
            computer.queueEvent(new EventGeneric(getTypeName(), Value.of(getUuid().toString()), Value.of("DiskDetached")), EventLabel.PERIPHERAL);
            computer.getFileSystem().removeDisk(system);
        }
        computers.remove(computer);
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new DriveBayScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.getItem() instanceof DiskItem;
    }
}
