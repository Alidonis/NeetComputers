package com.redtoast.items.generics;

import com.redtoast.Computer;
import com.redtoast.computerSpecs;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.ComputerStorage;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ComputerItem extends Item {
    private Computer computer;
    private boolean collectedComputer = false;
    private RGBGraphicsArray graphics;
    private boolean isClient = false;
    private NbtCompound nbt = null;
    private long timeframe = 0;

    public ComputerItem(Settings settings, computerSpecs specifications) {
        super(settings);
        System.out.println(-1);
        timeframe = System.currentTimeMillis();
        ComputerItem be = this;
        computer = new Computer(specifications) {
            @Override
            public void saveNBT() {
                be.saveNBT();
            }

            @Override
            public boolean isClient() {
                return be.isClient;
            }

            @Override
            public void refreshBinaryGraphics() {
                //write me
            }

            @Override
            public @Nullable Object getParentEntity() {
                return be;
            }
        };
        graphics = computer.getGraphics();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (System.currentTimeMillis() - 100 < timeframe) return TypedActionResult.fail(stack);
        timeframe = System.currentTimeMillis();
        if (!player.isSneaking() && computer.isOn()){
            player.openHandledScreen(getScreen());
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
        return TypedActionResult.success(stack);
    }

    private void saveNBT(){
        if (nbt==null) return;
        computer.writeNBT(nbt);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        isClient = world.isClient;
        if (isClient) return;

        nbt = stack.getOrCreateNbt();
        if (nbt.contains("IsOn")){
            if (nbt.contains("uuid") && !collectedComputer){
                collectedComputer=true;
                if (ComputerStorage.storage.contains(nbt.getUuid("uuid"))){
                    System.out.println(-999);
                    computer = ComputerStorage.storage.get(nbt.getUuid("uuid"));
                    graphics = computer.getGraphics();
                }
            }
            if (!computer.isLoaded()) computer.load(nbt);
        }else{
            computer.load(world.getServer());
        }

        computer.tick(world);
        if (computer.isOn()){
            if (!ComputerStorage.storage.contains(computer)){
                ComputerStorage.storage.put(computer.getUuid(), computer);
            }
        }else{
            ComputerStorage.storage.remove(computer.getUuid());
        }
    }

    public SimpleNamedScreenHandlerFactory getScreen(){
        return null;
    }

    public void unload(){
        ComputerStorage.storage.remove(computer.getUuid());
    }

    public Computer getComputer() {return computer;}
}
