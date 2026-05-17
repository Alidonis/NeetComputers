package com.redtoast.blocks.ColorDisplay;

import com.redtoast.APIS.DrawableGraphicalAPI;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.blocks.Generics.Displays.ConnectionMapping;
import com.redtoast.blocks.Generics.Displays.ConnectionMappingAccess;
import com.redtoast.blocks.Generics.Displays.DisplayModelStates;
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

public class ColorDisplayBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, ConnectionMappingAccess {
    public static final int pixelDensity = 2;
    private UUID uuid = null;
    private String tag = null;
    private boolean leader = true;
    private Vector2i size = new Vector2i(1,1);
    private RGBGraphicsArray graphics = new RGBGraphicsArray(12 * pixelDensity, 12 * pixelDensity);
    private DrawableGraphicalAPI api = new DrawableGraphicalAPI(graphics, null);
    private SectoredGraphics renderGraphics = null;
    private boolean dirtyGraphics = false;
    private String[] methods = APILoader.getFunctions(api);
    private BlockPos masterPos = null;
    private boolean loaded = false;
    private BlockEntity masterBlock = null;
    private int clock = 0;

    public ColorDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("color_display"), pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!loaded) return;
        nbt.putBoolean("leader", leader);
        if (leader) {
            if (uuid==null) uuid = UUID.randomUUID();
            nbt.putUuid("uuid", uuid);
            nbt.putInt("sizex", size.x);
            nbt.putInt("sizey", size.y);
            if (tag!=null) nbt.putString("tag", tag);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (!nbt.contains("leader")){
            loaded = true;
            return;
        }
        if (nbt.getBoolean("leader")){
            uuid = nbt.getUuid("uuid");
            size = new Vector2i(nbt.getInt("sizex"), nbt.getInt("sizey"));
            tag = nbt.contains("tag") ? nbt.getString("tag") : null;
            graphics = new RGBGraphicsArray((size.x*16-4) * pixelDensity, (size.y*16-4) * pixelDensity);
            api = new DrawableGraphicalAPI(graphics, null);
            leader = true;
            loaded = true;
        }
    }

    @Override
    public void calculateModel(Vector2i size, Vector2i relativePosition) {
        boolean left = relativePosition.x == 0;
        boolean top = relativePosition.y == 0;
        boolean right = relativePosition.x == size.x-1;
        boolean bottom = relativePosition.y == size.y-1;
        //bitmap configuration
        int code = left ? 0b0001 : 0; //left
        code |= top ?     0b0010 : 0; //top
        code |= right ?   0b0100 : 0; //right
        code |= bottom ?  0b1000 : 0; //bottom
        setModel(switch (code) {
            case 0b1011 -> DisplayModelStates.HLEFTTHIN;
            case 0b1010 -> DisplayModelStates.HMIDTHIN;
            case 0b1110 -> DisplayModelStates.HRIGHTTHIN;
            case 0b1101 -> DisplayModelStates.VBOTTOMTHIN;
            case 0b0101 -> DisplayModelStates.VMIDTHIN;
            case 0b0111 -> DisplayModelStates.VTOPTHIN;
            case 0b1001 -> DisplayModelStates.BOTTOMLEFT;
            case 0b1000 -> DisplayModelStates.BOTTOMMID;
            case 0b1100 -> DisplayModelStates.BOTTOMRIGHT;
            case 0b0100 -> DisplayModelStates.MIDRIGHT;
            case 0b0110 -> DisplayModelStates.TOPRIGHT;
            case 0b0010 -> DisplayModelStates.TOPMID;
            case 0b0011 -> DisplayModelStates.TOPLEFT;
            case 0b0001 -> DisplayModelStates.MIDLEFT;
            case 0b0000 -> DisplayModelStates.CENTER;
            default -> DisplayModelStates.BASE;
        });
    }

    @Override
    public void setSlave(BlockPos masterPos){
        leader = false;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(ColorDisplayBlock.LEADER, leader), Block.NOTIFY_ALL);
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(ColorDisplayBlock.SCALE, 1), Block.NOTIFY_ALL);
        assert world != null;
        this.masterPos = masterPos;
        graphics = null;
        api = null;
        uuid = null;
        loaded = true;
        masterBlock = null;
        tag=null;
        markDirty();
    }

    @Override
    public void setMaster(Vector2i size){
        if (leader && size.equals(this.size)) return;
        leader = true;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(ColorDisplayBlock.LEADER, leader), Block.NOTIFY_ALL);
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(ColorDisplayBlock.SCALE, size.y()), Block.NOTIFY_ALL);
        this.size = size;
        uuid = UUID.randomUUID();
        graphics = new RGBGraphicsArray((size.x * 16 - 4) * pixelDensity, (size.y * 16 - 4) * pixelDensity);
        api = new DrawableGraphicalAPI(graphics, null);
        loaded = true;
        masterBlock = null;
        tag=null;
        markDirty();
        render(graphics);
    }

    private void render(RGBGraphicsArray graphics) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new ColorDisplayGraphicsPayload(getPos(), graphics));

        if (getWorld() instanceof ServerWorld serverWorld) {
            if (serverWorld.getPlayers().isEmpty()) dirtyGraphics = true;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public void setModel(DisplayModelStates model){
        assert getWorld()!=null;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(ColorDisplayBlock.STATE, model.ordinal()), Block.NOTIFY_ALL);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof ColorDisplayBlockEntity colorDisplayBlockEntity){
            if (colorDisplayBlockEntity.leader && !colorDisplayBlockEntity.loaded) {
                colorDisplayBlockEntity.loadSubordinates();
                colorDisplayBlockEntity.loaded = true;
            }else if (!colorDisplayBlockEntity.leader) {
                colorDisplayBlockEntity.masterBlock = world.getBlockEntity(colorDisplayBlockEntity.masterPos);
            }else {
                if (colorDisplayBlockEntity.clock%2==0 && colorDisplayBlockEntity.dirtyGraphics) {
                    colorDisplayBlockEntity.dirtyGraphics = false;
                    colorDisplayBlockEntity.render(colorDisplayBlockEntity.graphics);
                }
                colorDisplayBlockEntity.clock += 1;
                colorDisplayBlockEntity.clock %= 20;
            }
        }
    }

    private void loadSubordinates(){
        if (world==null) return;
        Direction direction = world.getBlockState(getPos()).get(ColorDisplayBlock.FACING);
        new ConnectionMapping(world, pos, direction, 10, 8, (pos2) -> (world.getBlockEntity(pos2) instanceof ColorDisplayBlockEntity && world.getBlockState(pos2).get(ColorDisplayBlock.FACING).equals(direction) && world.getBlockState(pos2).get(ColorDisplayBlock.GROUP).equals(world.getBlockState(getPos()).get(ColorDisplayBlock.GROUP)))).start();
    }

    @Override
    public String[] getFunctionNames() {
        return methods;
    }

    @Override
    public Value<?> callFunction(Runtime runtime, String name, Value<?>... args) {
        if (!loaded) return Value.asError("Display not loaded");
        if (leader){
            try{
                Value<?> temp = APILoader.searchAndCall(api, runtime, name, args);
                if (name.equals("draw") && args.length==0) {
                    dirtyGraphics = true;
                }
                return temp;
            } catch (APILoader.LoaderError e) {
                APILoader.printJavaError(e);
                return Value.asError("API loading error");
            }
        }else{
            if (world!=null && masterBlock instanceof ColorDisplayBlockEntity master) return master.callFunction(runtime, name, args);
            return Value.asError("Display not loaded");
        }
    }

    @Override
    public String getTypeName() {
        return "neetcomputers:color_display";
    }

    @Override
    public UUID getUuid() {
        if (!leader && world!=null && world.getBlockEntity(masterPos) instanceof ColorDisplayBlockEntity master) return master.getUuid();
        if (uuid==null) uuid = UUID.randomUUID();
        markDirty();
        return uuid;
    }

    @Override
    public String getTag() {
        if (!leader && world!=null && masterBlock instanceof ColorDisplayBlockEntity master) return master.getTag();
        return tag;
    }

    @Override
    public void setTag(@NotNull String tag) {
        if (!leader && world!=null && masterBlock instanceof ColorDisplayBlockEntity master) {
            master.setTag(tag);
            return;
        }
        this.tag = tag.isBlank() ? null : tag.trim();
    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }

    public void setGraphics(SectoredGraphics sectoredGraphics) {
        renderGraphics = sectoredGraphics;
    }

    public boolean isLeader() {
        return getWorld()!=null && !getWorld().getBlockState(getPos()).isAir() && getWorld().getBlockState(getPos()).get(ColorDisplayBlock.LEADER);
    }

    public SectoredGraphics getRenderGraphics() {
        return renderGraphics;
    }
}
