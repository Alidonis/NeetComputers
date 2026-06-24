package com.redtoast.items;

import com.redtoast.items.generics.DiskItem;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.FS.DiskError;
import com.redtoast.simulation.FS.DiskSystem;
import com.redtoast.simulation.IDFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class Disk extends Item implements DiskItem {
    private final String key;

    public Disk(Settings settings, String key) {
        super(settings);
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public int getAddress(ItemStack stack, World world) {
        if (stack.get(NeetComputersServer.POINTER_COMPONENT)==null || stack.get(NeetComputersServer.POINTER_COMPONENT)==0){
            IDFactory.getServerState(NeetComputersServer.server);
            IDFactory.PointerIteration++;
            stack.set(NeetComputersServer.POINTER_COMPONENT, IDFactory.PointerIteration);
            return IDFactory.PointerIteration;
        }
        return stack.get(NeetComputersServer.POINTER_COMPONENT);
    }

    @Override
    public DiskSystem generateSystem(ItemStack stack, UUID uuid, BlockEntity caller) throws DiskError {
        return new DiskSystem(getAddress(stack, caller.getWorld()), stack.get(NeetComputersServer.TEMPLATE_COMPONENT), uuid);
    }

    @Override
    public void markBootable(ItemStack stack, boolean state, BlockEntity caller) {
        stack.set(NeetComputersServer.BOOTABLE_COMPONENT, state);
    }

    @Override
    public boolean isBootable(ItemStack stack, World world) {
        return Boolean.TRUE.equals(stack.get(NeetComputersServer.BOOTABLE_COMPONENT));
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Boolean.TRUE.equals(stack.get(NeetComputersServer.BOOTABLE_COMPONENT))) tooltip.add(Text.of("Can be crafted into a computer").copy().formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.translatable("itemGroup.neetcomputers.main_item_group").formatted(Formatting.BLUE));
    }
}
