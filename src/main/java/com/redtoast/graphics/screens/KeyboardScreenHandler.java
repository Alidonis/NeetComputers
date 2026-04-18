package com.redtoast.graphics.screens;

import com.redtoast.blocks.Keyboard.KeyboardBlockEntity;
import com.redtoast.neet.NeetComputersServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class KeyboardScreenHandler extends ScreenHandler {
    public final KeyboardBlockEntity keyboard;
    public record Payload() implements CustomPayload{
        public static final PacketCodec<RegistryByteBuf, Payload> CODEC = PacketCodec.of((value, buf) -> {}, new PacketCodec<>() {
            @Override
            public Payload decode(RegistryByteBuf buf) {
                return new Payload();
            }

            @Override
            public void encode(RegistryByteBuf buf, Payload value) {

            }
        });

        @Override
        public Id<? extends CustomPayload> getId() {
            return new Id<>(Identifier.of("keyboard_screen"));
        }
    }

    public KeyboardScreenHandler(int syncId, PlayerInventory playerInventory, Payload payload) {
        super(NeetComputersServer.KEYBOARD_SCREEN_HANDLER, syncId);
        keyboard = null;
    }

    public KeyboardScreenHandler(int syncId, KeyboardBlockEntity keyboard) {
        super(NeetComputersServer.KEYBOARD_SCREEN_HANDLER, syncId);
        this.keyboard = keyboard;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
