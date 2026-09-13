package com.redtoast.blocks.AccessPoint;

import com.redtoast.blocks.Generics.PeripheralBlockEntity;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.annotations.Primative;
import com.redtoast.simulation.annotations.Range;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class AccessPointBlockEntity extends PeripheralBlockEntity {
    protected static Map<DimensionType, BlockPos[]> CURRENT_STACK = new Hashtable<>();
    protected static Map<DimensionType, LinkedList<BlockPos>> REPORTING_STACK = new Hashtable<>();

    private int range = 150;
    private final LinkedList<Runnable> positions = new LinkedList<>();

    public AccessPointBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("access_point"), pos, state, "neetcomputers:access_point");
    }

    @Exposed
    public void broadcast(@Primative Tuple args) {
        BlockPos pos = getPos();
        DimensionType dimensionType = getWorld().getDimension();
        if (CURRENT_STACK.containsKey(dimensionType)) {
            Arrays.stream(CURRENT_STACK.get(dimensionType)).forEach((otherPos) -> {
                if (!otherPos.equals(pos) && otherPos.isWithinDistance(pos, range)) {
                    positions.add(() -> {
                        if (getWorld().getBlockEntity(otherPos) instanceof AccessPointBlockEntity accessPointBlockEntity) accessPointBlockEntity.receive(otherPos.getSquaredDistance(pos), args);
                    });
                }
            });
        }
    }

    @Exposed
    public int getRange() {
        return range;
    }

    @Exposed
    public void setRange(@Range(max = 250) int range) {
        this.range = range;
    }

    public void receive(double distance, Tuple args) {
        args.addFirst(Value.of(distance));
        queueEvent("received", (Object[]) args.toArray());
    }

    public void sendall() {
        positions.forEach(Runnable::run);
        positions.clear();
    }

    public static void moveStacks() {
        REPORTING_STACK.forEach((dimensionType, positions) -> {
            CURRENT_STACK.put(dimensionType, positions.toArray(new BlockPos[0]));
            positions.clear();
        });
    }
}
