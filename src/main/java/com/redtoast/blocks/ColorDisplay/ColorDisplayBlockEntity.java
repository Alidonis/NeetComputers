package com.redtoast.blocks.ColorDisplay;

import com.redtoast.APIS.DrawableGraphicalAPI;
import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.blocks.Generics.Displays.ConnectionMapping;
import com.redtoast.blocks.Generics.Displays.ConnectionMappingAccess;
import com.redtoast.blocks.Generics.Displays.DisplayModelStates;
import com.redtoast.blocks.Generics.Displays.MultiblockDisplayEntity;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.Networking.ColorDisplayGraphicsPayload;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.value.Value;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.UUID;

public class ColorDisplayBlockEntity extends MultiblockDisplayEntity {
    public static final int pixelDensity = 2;

    private RGBGraphicsArray graphics = new RGBGraphicsArray(12 * pixelDensity, 12 * pixelDensity);
    private DrawableGraphicalAPI api = new DrawableGraphicalAPI(graphics, null);
    private SectoredGraphics renderGraphics = null;
    private String[] methods = APILoader.getFunctions(api);

    public ColorDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("color_display"), pos, state);
    }

    @Override
    public void load() {
        graphics = new RGBGraphicsArray((getSize().x*16-4) * pixelDensity, (getSize().y*16-4) * pixelDensity);
        api = new DrawableGraphicalAPI(graphics, null);
    }

    @Override
    public void unload() {
        graphics = null;
        api = null;
    }

    @Override
    public Value<?> callLeader(Runtime runtime, String name, Value<?>... args) {
        try{
            Value<?> temp = APILoader.searchAndCall(api, runtime, name, args);
            if (name.equals("draw") && args.length==0) {
                markDirtyGraphics();
            }
            return temp;
        } catch (APILoader.LoaderError e) {
            APILoader.printJavaError(e);
            return Value.asError("API loading error");
        }
    }

    @Override
    public void render() {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new ColorDisplayGraphicsPayload(getPos(), graphics));

        if (getWorld() instanceof ServerWorld serverWorld) {
            if (serverWorld.getPlayers().isEmpty()) markDirtyGraphics();
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    @Override
    public String[] getFunctionNames() {
        return methods;
    }

    @Override
    public String getTypeName() {
        return "neetcomputers:color_display";
    }

    public void setGraphics(SectoredGraphics sectoredGraphics) {
        renderGraphics = sectoredGraphics;
    }

    public boolean isLeader() {
        return getWorld()!=null && !getWorld().getBlockState(getPos()).isAir() && getWorld().getBlockState(getPos()).get(MultiblockDisplayEntity.LEADER);
    }

    public SectoredGraphics getRenderGraphics() {
        return renderGraphics;
    }
}
