package com.redtoast.blocks.Generics;

import com.redtoast.APIS.ProjectorAPI;
import com.redtoast.Compat.GetCC;
import com.redtoast.Computer;
import com.redtoast.Connections.*;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.Generics.Displays.BinaryGraphicsRenderProvider;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.ComputerStorage;
import com.redtoast.neet.Networking.BinaryGraphicsPayload;
import com.redtoast.neet.Networking.ComputerScreenInitPayload;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.config.ComputerConfig;
import com.redtoast.simulation.value.Value;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ComputerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<ComputerScreenInitPayload>, PeripheralReceiver, PipeRenderSource, BinaryGraphicsRenderProvider {
    private Computer computer;
    private boolean collectedComputer = false;
    private RGBGraphicsArray graphics;
    private boolean corrupted = false;
    private List<com.redtoast.Connections.PeripheralProvider> peripheralProviderCache = List.of();

    public ComputerBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, ComputerConfig specifications) {
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
                if (specifications.doesBinaryGraphics()) renderBinaryGraphics(Objects.requireNonNull(getBinaryGraphics()));
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
        return Text.literal(computer.getConfiguration().modelName());
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!computer.getConfiguration().doesColorGraphics()) return null;
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

    @Override
    public List<com.redtoast.Connections.PeripheralProvider> scanForPeripherals(){
        return peripheralProviderCache;
    }

    private boolean isntDuplicate(LinkedList<PeripheralProvider> peripherals, PeripheralProvider duplicate){
        for (PeripheralProvider provider : peripherals) if (provider.getUuid().equals(duplicate.getUuid())) return false;
        return true;
    }

    public List<com.redtoast.Connections.PeripheralProvider> scanForPeripheralsInternal() {
        LinkedList<BlockPos> todoList = new LinkedList<>();
        LinkedList<BlockPos> investigated = new LinkedList<>();
        LinkedList<PeripheralProvider> peripherals = new LinkedList<>();
        boolean ec = (boolean) ConfigLoader.getServerConfig("experimental-compatibility");
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
                if (ec) {
                    PeripheralProvider CCprovider = GetCC.getPeripheral(investigating, world, getComputer());
                    if (CCprovider!=null && isntDuplicate(peripherals, CCprovider)){
                        peripherals.add(CCprovider);
                    }
                }
                BlockState block = world.getBlockState(investigating);
                if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)){
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof PeripheralProvider provider && isntDuplicate(peripherals, provider)){
                        peripherals.add(provider);
                    }
                }
                if (CableManager.getInstance().pipeExists(world.getDimension(), investigating, PipeType.PERIPHERAL)) {
                    todoList.add(investigating);
                }
                else if (block.getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)) {
                    BlockEntity blockEntity = world.getBlockEntity(investigating);
                    if (blockEntity instanceof PeripheralProvider provider && isntDuplicate(peripherals, provider)) {
                        peripherals.add(provider);
                    }
                }else if (ec){
                    PeripheralProvider provider = GetCC.getPeripheral(investigating, world, getComputer());
                    if (provider!=null && isntDuplicate(peripherals, provider)){
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
                return peripheralProvider.callFunction(computer.getRuntime(), functionName, args);
            }
        }

        return Value.asError("Peripheral not found");
    }

    @Override
    public ComputerScreenInitPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new ComputerScreenInitPayload(graphics, computer.getUuid());
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type == PipeType.PERIPHERAL;
    }

    @Override
    public BinaryGraphicsArray getBinaryGraphics() {
        return getComputer().getBinaryGraphics();
    }

    @Override
    public void setBinaryGraphics(BinaryGraphicsArray graphicsArray) {
        computer.setBinaryGraphics(graphicsArray);
    }

    @Override
    public Vec3i getColoration(float clock, int x, int y) {
        int r = 40;
        int g = 226;
        int b = 50;
        if ((x+2)%3==0){
            r++;
            g += 8;
            b += 3;
        }
        if ((y+1)%2==0){
            r++;
            g += 10;
            b += 3;
        }
        float density = 0.4f;
        float offset = (clock + y * density) % 6.2f;
        double weight = 8;
        double effect = Math.sin(offset) * weight;
        r += (int)Math.round(effect);
        g += (int)Math.round(effect);
        b += (int)Math.round(effect);
        return switch (world.getBlockState(getPos()).get(ComputerBlock.STATE)) {
            default -> {
                if ((y%4>1 || x%4>1) && !(y%4>1 && x%4>1)) yield new Vec3i(201, 109, 233);
                yield new Vec3i(0, 0, 0);
            }
            case 1 -> new Vec3i(r,g,b);
            case 2 -> new Vec3i(g, r, b);
            case 3 -> new Vec3i(b, r, g);
        };
    }

    @Override
    public boolean canRender() {
        return world!=null && !world.getBlockState(getPos()).isAir() && world.getBlockState(getPos()).get(ComputerBlock.STATE)!=0;
    }
}
