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
import net.minecraft.util.math.Vec3d;
import dev.ryanhcode.sable.companion.SableCompanion;

public class AccessPointBlockEntity extends PeripheralBlockEntity {
    protected static Map<DimensionType, BlockPos[]> CURRENT_STACK = new Hashtable<>();
    protected static Map<DimensionType, LinkedList<BlockPos>> REPORTING_STACK = new Hashtable<>();

    private int range = 500;
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
                if (!otherPos.equals(pos) && SableCompanion.INSTANCE.distanceSquaredWithSubLevels(getWorld(), Vec3d.ofCenter(pos), Vec3d.ofCenter(otherPos)) <= range * range) {
                    positions.add(() -> {
                        if (getWorld().getBlockEntity(otherPos) instanceof AccessPointBlockEntity accessPointBlockEntity) accessPointBlockEntity.receive(
                                Math.sqrt(SableCompanion.INSTANCE.distanceSquaredWithSubLevels(getWorld(), Vec3d.ofCenter(pos), Vec3d.ofCenter(otherPos))), args);
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
    public void setRange(@Range(max = 500) int range) {
        this.range = range;
    }

    public void receive(double distance, Tuple args) {
        Object[] argsArray = new Object[args.size() + 1];
        int i = 0;
        argsArray[i] = distance;
        for (Object arg : args) {
            i += 1;
            argsArray[i] = arg;
        }
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
