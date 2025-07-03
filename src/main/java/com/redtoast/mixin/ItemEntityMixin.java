package com.redtoast.mixin;

import com.redtoast.items.generics.ComputerItem;
import com.redtoast.items.generics.DroppedComputer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    DroppedComputer droppedComputer = null;

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void onCreate(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof ComputerItem computerItem) {
            ItemEntity self = (ItemEntity)(Object)this;
            self.setNeverDespawn();
            droppedComputer = new DroppedComputer(computerItem, self);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (droppedComputer!=null){
            ItemEntity self = (ItemEntity)(Object)this;
            droppedComputer.tick(self.getWorld(), self.getStack());
        }
    }
}