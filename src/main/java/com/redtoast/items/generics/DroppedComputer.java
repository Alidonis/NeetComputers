package com.redtoast.items.generics;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DroppedComputer {
    private ComputerItem computerItem;
    private ItemEntity itemEntity;

    public DroppedComputer(ComputerItem computerItem, ItemEntity itemEntity) {
        this.computerItem = computerItem;
        this.itemEntity = itemEntity;
    }

    public void tick(World world, ItemStack stack){
        computerItem.inventoryTick(stack, world, itemEntity, -1, false);
    }
}