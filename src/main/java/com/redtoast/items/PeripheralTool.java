package com.redtoast.items;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeType;
import com.redtoast.graphics.screens.PeripheralToolScreenHandler;
import com.redtoast.items.generics.DisplayPipes;
import com.redtoast.neet.Networking.PeripheralToolScreenInitPayload;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public class PeripheralTool extends Item implements DisplayPipes {
    public PeripheralTool(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context){
        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof PeripheralProvider peripheralProvider){
            context.getPlayer().openHandledScreen(new ExtendedScreenHandlerFactory<PeripheralToolScreenInitPayload>() {
                @Override
                public PeripheralToolScreenInitPayload getScreenOpeningData(ServerPlayerEntity player) {
                    return PeripheralToolScreenInitPayload.fromPeripheral(peripheralProvider);
                }

                @Override
                public Text getDisplayName() {
                    return Text.of(Language.getInstance().get("item.neetcomputers.peripheral_tool", "Peripheral Config Tool"));
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new PeripheralToolScreenHandler(syncId, peripheralProvider);
                }
            });
        }
        return ActionResult.PASS;
    }

    @Override
    public PipeType getType() {
        return PipeType.PERIPHERAL;
    }
}
