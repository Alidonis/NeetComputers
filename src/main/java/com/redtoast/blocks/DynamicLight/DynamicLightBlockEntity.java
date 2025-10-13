package com.redtoast.blocks.DynamicLight;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public class DynamicLightBlockEntity extends BlockEntity implements PeripheralProvider {
    private static ParameterRules ruleset = new ParameterRules(VarType.INT);
    private UUID uuid = null;
    private Integer lightLevel = null;

    public DynamicLightBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("dynamic_light"), pos, state);
    }

    @Override
    public String[] getFunctionNames() {
        return new String[]{"setLuminance"};
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (uuid==null) uuid = UUID.randomUUID();
        nbt.putUuid("uuid", uuid);
        nbt.putInt("light_level", lightLevel==null ? 0 : lightLevel);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("uuid", NbtElement.INT_ARRAY_TYPE)){
            uuid = nbt.getUuid("uuid");
        }
        if (nbt.contains("light_level", NbtElement.INT_TYPE) && lightLevel==null){
            lightLevel = nbt.getInt("light_level");
        }
    }

    @Override
    public Value<?> callFunction(String name, Value<?>... args) {
        if (!Objects.equals(name, "setLuminance")){
            Value.asError("Cant Find Function '"+name+"'");
        }
        ParameterCheckReturn retur = ParameterRules.checkParameters(args, ruleset, null);
        if (retur.isError()){
            return Value.asError(retur.getMessage());
        }
        lightLevel = retur.getFunctionInput().get(0).toInt();
        return Value.NULL;
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof DynamicLightBlockEntity dynamicLightBlockEntity && dynamicLightBlockEntity.lightLevel!=null){
            world.setBlockState(blockPos, blockState.with(DynamicLightBlock.LUMINANCE, Math.min(Math.max(dynamicLightBlockEntity.lightLevel, 0),15)), Block.NOTIFY_ALL);
        }
    }

    @Override
    public String getTypeName() {
        return "neetcomputers:dynamic_light";
    }

    @Override
    public UUID getUuid() {
        if (uuid==null) uuid = UUID.randomUUID();
        return uuid;
    }
}
