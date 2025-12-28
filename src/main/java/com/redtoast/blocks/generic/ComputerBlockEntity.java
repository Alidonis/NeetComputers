package com.redtoast.blocks.generic;

import com.redtoast.APIS.ProjectorAPI;
import com.redtoast.Compat.GetCC;
import com.redtoast.Computer;
import com.redtoast.Connections.*;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.GenericConsumerBlock;
import com.redtoast.computerSpecs;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.ComputerStorage;
import com.redtoast.neet.Networking.BinaryGraphicsPayload;
import com.redtoast.neet.Networking.ComputerScreenInitPayload;
import com.redtoast.simulation.networkInterfaces.NetworkProvider;
import com.redtoast.simulation.value.Value;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ComputerBlockEntity extends GenericConsumerBlock implements ExtendedScreenHandlerFactory<ComputerScreenInitPayload>, PeripheralReceiver {
    private Computer computer;
    private boolean collectedComputer = false;
    private RGBGraphicsArray graphics;
    private boolean corrupted = false;
    private List<com.redtoast.Connections.PeripheralProvider> peripheralProviderCache = List.of();

    public ComputerBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, computerSpecs specifications) {
        super(type, pos, state);
        ComputerBlockEntity be = this;
        computer = new Computer(specifications) {
            @Override
            public List<PeripheralProvider> scanForPeripherals() {
                return be.scanForPeripherals();
            }

            @Override
            public Value<?> sendFunctionCall(UUID uuid, String functionName, Value<?>... args) {
                return be.sendFunctionCall(uuid, functionName, args);
            }

            @Override
            public void saveNBT() {
                markDirty();
            }

            @Override
            public boolean isClient() {
                return be.getWorld().isClient();
            }

            @Override
            public void refreshBinaryGraphics() {
                if (specifications.doesBinaryGraphics) renderBinaryGraphics(Objects.requireNonNull(getBinaryGraphics()));
            }

            @Override
            public Object getParentEntity() {
                return be;
            }
        };
        computer.setLibrary("projector", new ProjectorAPI(computer));
        computer.createLibraryAlias("display", "projector");
        graphics = computer.getGraphics();
    }

    public void renderBinaryGraphics(BinaryGraphicsArray graphics){
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new BinaryGraphicsPayload(getPos(), graphics));

        if (getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public void AssignPointers(World world, ItemStack itemStack){
        if (itemStack.contains(ComputerDataComponent.TYPE)) {
            ComputerDataComponent data = itemStack.get(ComputerDataComponent.TYPE);
            computer.load(data);
        }else{
            MinecraftServer server = world.getServer();
            computer.load(server);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!corrupted) nbt = computer.saveNBT(nbt);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void attachPeripheral(com.redtoast.simulation.peripheralInterfaces.PeripheralProvider api) {

    }

    @Override
    public void connectToNetwork(NetworkProvider networkProvider) {

    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        corrupted = false;//evalCorruption(nbt);
        if (!corrupted){
            if (nbt.contains("uuid") && !collectedComputer){
                collectedComputer=true;
                if (ComputerStorage.storage.contains(nbt.getUuid("uuid"))){
                    computer = ComputerStorage.storage.get(nbt.getUuid("uuid"));
                    graphics = computer.getGraphics();
                }
            }
            computer.load(nbt);
        }
    }

    public ActionResult onUse(PlayerEntity player, BlockState state){
        if (corrupted) {
            player.sendMessage(Text.literal("NBT DATA CORRUPTED, the files are still being stored server-side"));
            return ActionResult.SUCCESS;
        }
        if (!player.isSneaking() && !player.isUsingItem() && computer.isOn()){
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        if (player.isSneaking()){
            if (computer.isCrashed()){
                computer.reset();
            }else{
                computer.stop();
            }
        }else{
            if (computer.isCrashed()) player.sendMessage(Text.literal(computer.getCrashMessage()));
            computer.start();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(computer.getSpecifications().MachineName);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!computer.getSpecifications().doesBinaryGraphics) return null;
        return new RGBScreenHandler(syncId,graphics,computer);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t) {
        if (!world.isClient()){
            BlockEntity be = world.getBlockEntity(blockPos);
            if (be instanceof ComputerBlockEntity computerBlock) {
                computerBlock.peripheralProviderCache = computerBlock.scanForPeripheralsInternal();
                if (computerBlock.corrupted){
                    BlockState current = world.getBlockState(blockPos);
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.CRASHED, computerBlock.computer.isCrashed()), Block.NOTIFY_ALL);
                    return;
                }
                if (!computerBlock.computer.isLoaded()) return;
                computerBlock.computer.tick(world);
                if (computerBlock.computer.isOn()){
                    if (!ComputerStorage.storage.contains(computerBlock.computer)){
                        ComputerStorage.storage.put(computerBlock.computer.getUuid(), computerBlock.computer);
                    }
                }else{
                    ComputerStorage.storage.remove(computerBlock.computer.getUuid());
                }
                BlockState current = world.getBlockState(blockPos);
                if (current.get(DesktopBlockComputer.ON) != computerBlock.computer.isOn()) {
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.ON, computerBlock.computer.isOn()), Block.NOTIFY_ALL);
                }
                if (current.get(DesktopBlockComputer.CRASHED) != computerBlock.computer.isCrashed()) {
                    world.setBlockState(blockPos, current.with(DesktopBlockComputer.CRASHED, computerBlock.computer.isCrashed()), Block.NOTIFY_ALL);
                }
                world.setBlockState(blockPos, current.with(DesktopBlockComputer.STATE, computerBlock.computer.getStatus().ordinal()), Block.NOTIFY_ALL);
            }
        }
    }

    public void unload(){
        ComputerStorage.storage.remove(computer.getUuid());
    }

    public Computer getComputer() {return computer;}

    private boolean evalCorruption(NbtCompound nbt){
        if (
                (nbt.contains("Address") && (nbt.get("Address") instanceof NbtInt && nbt.getInt("Address") != 0)) &&
                (nbt.contains("IsOn") && nbt.get("IsOn") instanceof NbtByte)
        ) return false;
        return true;
    }

    @Override
    public List<com.redtoast.Connections.PeripheralProvider> scanForPeripherals(){
        return peripheralProviderCache;
    }

    public List<com.redtoast.Connections.PeripheralProvider> scanForPeripheralsInternal() {
        if (!CableManager.getInstance().pipeExists(getWorld().getDimension(), getPos(), PipeType.PERIPHERAL)) return List.of();
        LinkedList<BlockPos> todoList = new LinkedList<>();
        LinkedList<BlockPos> investigated = new LinkedList<>();
        LinkedList<PeripheralProvider> peripherals = new LinkedList<>();
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
                PeripheralProvider CCprovider = GetCC.getPeripheral(investigating, world, getComputer());
                if (CCprovider!=null){
                    peripherals.add(CCprovider);
                }
                BlockState block = world.getBlockState(investigating);
                if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)){
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof PeripheralProvider provider){
                        peripherals.add(provider);
                    }
                }
                if (CableManager.getInstance().pipeExists(world.getDimension(), investigating, PipeType.PERIPHERAL)) {
                    todoList.add(investigating);
                }
                else if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)) {
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof PeripheralProvider provider) {
                        peripherals.add(provider);
                    }
                }
                    else{
                        PeripheralProvider provider = GetCC.getPeripheral(investigating, world, getComputer());
                        if (provider!=null){
                            peripherals.add(provider);
                        }
                    }
                investigated.add(investigating);
            }
        }

        return peripherals;
    }

    @Override
    public Value<?> sendFunctionCall(UUID uuid, String functionName, Value<?>... args) {
        for (PeripheralProvider peripheralProvider : peripheralProviderCache) {
            if (peripheralProvider.getUuid() == uuid) {
                return peripheralProvider.callFunction(functionName, args);
            }
        }

        return Value.asError("Peripheral not found");
    }

    @Override
    public ComputerScreenInitPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new ComputerScreenInitPayload(graphics, computer.getUuid());
    }
}
