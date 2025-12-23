package com.redtoast.mixin;

import com.redtoast.items.generics.ConnectorItem;
import com.redtoast.neet.NeetComputersServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public class PlayerInventory {
    @Shadow @Final public PlayerEntity player;
    @Shadow public int selectedSlot;
    @Unique int selection2 = -1;

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void updateItems(CallbackInfo ci) {
        if (!player.getWorld().isClient()){
            if (selection2 != selectedSlot){
                selection2 = selectedSlot;
                if (player.getMainHandStack().getItem() instanceof ConnectorItem connectorItem){
                    NeetComputersServer.sendPipeBufferToPlayer((ServerPlayerEntity) player, connectorItem.getType());
                }
            }
        }
    }
}
