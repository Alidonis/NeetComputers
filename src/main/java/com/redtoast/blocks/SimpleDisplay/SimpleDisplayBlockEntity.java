package com.redtoast.blocks.SimpleDisplay;

import com.redtoast.APIS.ProjectorAPI;
import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.blocks.ColorDisplay.ColorDisplayBlock;
import com.redtoast.blocks.Generics.Displays.*;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.Networking.BinaryGraphicsPayload;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleDisplayBlockEntity extends MultiblockDisplayEntity implements BinaryGraphicsProvider, BinaryGraphicsRenderProvider {
    private BinaryGraphicsArray graphics = new BinaryGraphicsArray(12, 12);
    private Table api;

    public SimpleDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("simple_display"), pos, state);
        api = APILoader.TableizeAPI(new ProjectorAPI(this), null);
    }

    @Override
    public void load() {
        graphics = new BinaryGraphicsArray(getSize().x*16-4, getSize().y*16-4);
        api = APILoader.TableizeAPI(new ProjectorAPI(this), null);
    }

    @Override
    public void unload() {
        graphics = null;
        api = null;
    }

    @Override
    public Value<?> callLeader(Runtime runtime, String name, Value<?>... args) {
        AtomicReference<Value<?>> dummy = new AtomicReference<>();
        api.foreach((key, value) -> {
            if (key.getValue().equals(name)) {
                dummy.set(value.toFunction().invoke(new FunctionInput(new LinkedList<>(List.of(args)))));
            }
        });
        markDirty();
        return dummy.get()==null ? Value.asError("Cant Find Function '"+name+"'") : dummy.get();
    }

    @Override
    public void render() {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new BinaryGraphicsPayload(getPos(), graphics));

        if (getWorld() instanceof ServerWorld serverWorld) {
            if (serverWorld.getPlayers().isEmpty()) markDirtyGraphics();
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    @Override
    public String[] getFunctionNames() {
        return new String[]{"drawPixel", "getSize", "drawLine", "drawRec", "draw", "clear"};
    }

    @Override
    public String getTypeName() {
        return "neetcomputers:simple_display";
    }

    @Override
    public BinaryGraphicsArray getBinaryGraphics() {
        return graphics;
    }

    @Override
    public Vec3i getColoration(float clock, int x, int y) {
        int r = 40;
        int g = 226;
        int b = 50;
        if ((x)%4==0){
            r++;
            g += 8;
            b += 3;
        }
        if ((y+1)%4==0){
            r++;
            g += 10;
            b += 3;
        }
        float density = 0.4f;
        float offset = (clock + y * density) % 6.2f;
        double weight = 8;
        double effect = Math.sin(offset) * weight;
        r += (int)Math.round(effect);
        g += (int)Math.round(effect);
        b += (int)Math.round(effect);
        return new Vec3i(r, g, b);
    }

    @Override
    public boolean canRender() {
        return getWorld()!=null && !getWorld().getBlockState(getPos()).isAir() && getWorld().getBlockState(getPos()).get(MultiblockDisplayEntity.LEADER);
    }

    @Override
    public void setBinaryGraphics(BinaryGraphicsArray graphicsArray) {
        graphics = graphicsArray;
        markDirtyGraphics();
    }
}
