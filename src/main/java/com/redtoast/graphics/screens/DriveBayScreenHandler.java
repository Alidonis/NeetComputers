package com.redtoast.graphics.screens;

import com.redtoast.neet.NeetComputersServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class DriveBayScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public DriveBayScreenHandler(int id, PlayerInventory player, Inventory inventory) {
        super(NeetComputersServer.DRIVE_BAY_SCREEN_HANDLER, id);
        this.inventory = inventory;
        this.addSlot(new Slot(this.inventory, 0, 80, 35));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(player, x, 8 + x * 18, 142));
        }

    }

    public DriveBayScreenHandler(int id, PlayerInventory player) {
        this(id, player, new SimpleInventory(1));
    }

    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasStack()) {
            ItemStack existing = slot.getStack().copy();
            ItemStack result = existing.copy();
            if (slotIndex == 0) {
                if (!this.insertItem(existing, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(existing, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (existing.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (existing.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            } else {
                slot.onTakeItem(player, existing);
                return result;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }
}
