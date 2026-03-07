package com.redtoast.blocks.DynamicLight;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public class DynamicLightBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource {
    private static final ParameterRules ruleset = new ParameterRules(VarType.INT);
    private static final ParameterRules ruleset2 = new ParameterRules();
    private UUID uuid = null;
    private Integer lightLevel = null;
    private Integer lightLevelCurrent = 0;

    public DynamicLightBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("dynamic_light"), pos, state);
    }

    @Override
    public String[] getFunctionNames() {
        return new String[]{"setLuminance", "turnOn", "turnOff"};
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (uuid==null) uuid = UUID.randomUUID();
        nbt.putUuid("uuid", uuid);
        nbt.putInt("light_level", lightLevel==null ? 0 : lightLevel);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("uuid", NbtElement.INT_ARRAY_TYPE)){
            uuid = nbt.getUuid("uuid");
        }
        if (nbt.contains("light_level", NbtElement.INT_TYPE) && lightLevel==null){
            lightLevel = nbt.getInt("light_level");
            lightLevelCurrent = nbt.getInt("light_level");
        }

    }

    @Override
    public Value<?> callFunction(String name, Value<?>... args) {
        if (Objects.equals(name, "setLuminance")){
            ParameterCheckReturn retur = ParameterRules.checkParameters(args, ruleset, null);
            if (retur.isError()){
                return Value.asError(retur.getMessage());
            }
            lightLevel = retur.getFunctionInput().get(0).toInt();
            return Value.NULL;
        }
        if (Objects.equals(name, "turnOff")){
            ParameterCheckReturn retur = ParameterRules.checkParameters(args, ruleset2, null);
            if (retur.isError()){
                return Value.asError(retur.getMessage());
            }
            lightLevel = 0;
            return Value.NULL;
        }
        if (Objects.equals(name, "turnOn")){
            ParameterCheckReturn retur = ParameterRules.checkParameters(args, ruleset2, null);
            if (retur.isError()){
                return Value.asError(retur.getMessage());
            }
            lightLevel = 15;
            return Value.NULL;
        }
        return Value.asError("Cant Find Function '"+name+"'");
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof DynamicLightBlockEntity dynamicLightBlockEntity && dynamicLightBlockEntity.lightLevel!=null){
            if (dynamicLightBlockEntity.lightLevel < dynamicLightBlockEntity.lightLevelCurrent && dynamicLightBlockEntity.lightLevelCurrent>0) dynamicLightBlockEntity.lightLevelCurrent--;
            if (dynamicLightBlockEntity.lightLevel > dynamicLightBlockEntity.lightLevelCurrent && dynamicLightBlockEntity.lightLevelCurrent<15) dynamicLightBlockEntity.lightLevelCurrent++;
            world.setBlockState(blockPos, blockState.with(DynamicLightBlock.LUMINANCE, dynamicLightBlockEntity.lightLevelCurrent), Block.NOTIFY_ALL);
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

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return switch (type){
            case PERIPHERAL -> true;
            default -> false;
        };
    }
}
