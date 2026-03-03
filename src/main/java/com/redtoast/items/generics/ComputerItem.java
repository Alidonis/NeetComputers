package com.redtoast.items.generics;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.neet.ComputerStorage;
import com.redtoast.simulation.value.Value;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ComputerItem{} /**extends CustomRenderItem {
    private Hashtable<UUID, computerStack> memory = new Hashtable<>();
    public computerSpecs specifications;

    public record computerStack(AtomicReference<Computer> computer, AtomicBoolean isClient, AtomicBoolean collected, NbtCompound compound){}

    public ComputerItem(Settings settings, computerSpecs specifications) {
        super(settings);
        this.specifications = specifications;
    }

    public computerStack generateComputerStack(ItemStack stack){
        NbtCompound saveCompound = new NbtCompound();
        AtomicBoolean isClient = new AtomicBoolean(false);
        computerStack Cstack = new computerStack(
                new AtomicReference<>(new Computer(specifications) {
                    @Override
                    public List<PeripheralProvider> scanForPeripherals() {
                        return List.of();
                    }

                    @Override
                    public Value<?> sendFunctionCall(UUID uuid, String functionName, Value<?>... args) {
                        return Value.asError("Peripheral not found");
                    }

                    @Override
                    public void saveNBT() {
                        for (String key : saveCompound.getKeys()){
                            saveCompound.remove(key);
                        }
                        writeNBT(saveCompound);
                    }

                    @Override
                    public boolean isClient() {
                        return isClient.get();
                    }

                    @Override
                    public void refreshBinaryGraphics() {

                    }

                    @Override
                    public @Nullable Object getParentEntity() {
                        return stack;
                    }
                }),
                isClient,
                new AtomicBoolean(false),
                saveCompound
        );
        return Cstack;
    }

    public computerStack getComputerStack(ItemStack stack){
        if (stack.getNbt()!=null && !stack.getNbt().isEmpty() && stack.getSubNbt("data") != null){
            computerStack Cstack = generateComputerStack(stack);
            if (!Cstack.computer().get().isLoaded()) Cstack.computer().get().load(stack.getSubNbt("data"));
            return Cstack;
        }else{
            NbtCompound nbt = stack.getOrCreateSubNbt("data");
            if (nbt.contains("uuid") && memory.contains(nbt.getUuid("uuid"))){
                computerStack Cstack = memory.get(nbt.getUuid("uuid"));
                if (!Cstack.collected().get()){
                    Cstack.collected().set(true);
                    if (ComputerStorage.storage.contains(nbt.getUuid("uuid"))){
                        Cstack.computer.set(ComputerStorage.storage.get(nbt.getUuid("uuid")));
                    }
                }
                if (!Cstack.computer().get().isLoaded()) Cstack.computer().get().load(nbt);
                return Cstack;
            }else{
                return generateComputerStack(stack);
            }
        }
    }

    public void tickStack(ItemStack stack, World world){
        computerStack Cstack = getComputerStack(stack);
        if (!Cstack.computer().get().isLoaded()){
            Cstack.computer().get().load(world.getServer());
            stack.getOrCreateNbt().put("data", Cstack.compound());
        }
        Cstack.isClient().set(world.isClient());
        NbtCompound nbt = stack.getOrCreateSubNbt("data");

        assert nbt != null;

        Cstack.computer().get().tick(world);
        if (Cstack.computer().get().isOn()){
            if (!ComputerStorage.storage.contains(Cstack.computer().get())){
                ComputerStorage.storage.put(Cstack.computer().get().getUuid(), Cstack.computer().get());
            }
        }else{
            ComputerStorage.storage.remove(Cstack.computer().get().getUuid());
        }

        NbtCompound stackNbt = stack.getOrCreateNbt();
        stackNbt.put("data", Cstack.compound());
    }

    public Computer getComputer(ItemStack stack){
        return getComputerStack(stack).computer().get();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        System.out.println(world.isClient() ? "CLIENT" : "SERVER");
        if (world.isClient()){

        }else{
            System.out.println(1);
            if (!player.isSneaking() && getComputer(stack).isOn()){
                System.out.println(2);
                player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                        getComputer(stack).getGraphics().writeScreenToPacketBuf(buf);
                    }

                    @Override
                    public Text getDisplayName() {
                        return Text.literal(specifications.MachineName);
                    }

                    @Nullable
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        if (!specifications.doesRBGGraphics) return null;
                        Computer computer = getComputer(stack);
                        return new RGBScreenHandler(syncId, computer.getGraphics(), computer);
                    }
                });
//                PacketByteBuf temp = PacketByteBufs.create();
//                assert NeetComputers.SCREEN_INIT_PACKET != null;
//                ClientPlayNetworking.send(NeetComputers.SCREEN_INIT_PACKET, temp);
            }
            System.out.println(3);
            if (player.isSneaking()){
                if (getComputer(stack).isCrashed()){
                    System.out.println(4);
                    getComputer(stack).reset();
                }else{
                    System.out.println(5);
                    getComputer(stack).stop();
                }
            }else{
                System.out.println(6);
                if (getComputer(stack).isCrashed()) player.sendMessage(Text.literal(getComputer(stack).getCrashMessage()));
                getComputer(stack).start();
            }
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()){

        }else{
            tickStack(stack, world);
        }
    }

    public void render(ItemStack stack,
                       ModelTransformationMode renderMode,
                       double deltaTime,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       int overlay,
                       BakedModel model,
                       Transformation transformation){}

}
**/