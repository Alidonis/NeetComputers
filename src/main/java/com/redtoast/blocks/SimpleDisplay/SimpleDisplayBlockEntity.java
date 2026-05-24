package com.redtoast.blocks.SimpleDisplay;

import com.redtoast.APIS.ProjectorAPI;
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

public class SimpleDisplayBlockEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, ConnectionMappingAccess, BinaryGraphicsProvider, BinaryGraphicsRenderProvider {
    private UUID uuid = null;
    private String tag = null;
    private boolean leader = true;
    private Vector2i size = new Vector2i(1,1);
    private BinaryGraphicsArray graphics = new BinaryGraphicsArray(12, 12);
    private boolean dirtyGraphics = false;
    private Table api;
    private BlockPos masterPos = null;
    private boolean loaded = false;
    private BlockEntity masterBlock = null;
    private int clock = 0;

    public SimpleDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("simple_display"), pos, state);
        api = APILoader.TableizeAPI(new ProjectorAPI(this), null);
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
            graphics = new BinaryGraphicsArray(size.x*16-4, size.y*16-4);
            api = APILoader.TableizeAPI(new ProjectorAPI(this), null);
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

    public void setModel(DisplayModelStates model){
        assert getWorld()!=null;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(SimpleDisplayBlock.STATE, model.ordinal()), Block.NOTIFY_ALL);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof SimpleDisplayBlockEntity simpleDisplayBlockEntity){
            if (simpleDisplayBlockEntity.leader && !simpleDisplayBlockEntity.loaded) {
                simpleDisplayBlockEntity.loadSubordinates();
                simpleDisplayBlockEntity.loaded = true;
            }else if (!simpleDisplayBlockEntity.leader) {
                simpleDisplayBlockEntity.masterBlock = world.getBlockEntity(simpleDisplayBlockEntity.masterPos);
            }else {
                if (simpleDisplayBlockEntity.clock%2==0 && simpleDisplayBlockEntity.dirtyGraphics) {
                    simpleDisplayBlockEntity.dirtyGraphics = false;
                    simpleDisplayBlockEntity.renderBinaryGraphics(simpleDisplayBlockEntity.graphics);
                }
                simpleDisplayBlockEntity.clock += 1;
                simpleDisplayBlockEntity.clock %= 100;
            }
        }
    }

    private void loadSubordinates(){
        if (world==null) return;
        Direction direction = world.getBlockState(getPos()).get(SimpleDisplayBlock.FACING);
        new ConnectionMapping(world, pos, direction, 10, 8, (pos2) -> (world.getBlockEntity(pos2) instanceof SimpleDisplayBlockEntity && world.getBlockState(pos2).get(SimpleDisplayBlock.FACING).equals(direction) && world.getBlockState(pos2).get(SimpleDisplayBlock.GROUP).equals(world.getBlockState(getPos()).get(SimpleDisplayBlock.GROUP)))).start();
    }

    @Override
    public void setSlave(BlockPos masterPos){
        leader = false;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(SimpleDisplayBlock.LEADER, leader), Block.NOTIFY_ALL);
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
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(SimpleDisplayBlock.LEADER, leader), Block.NOTIFY_ALL);
        this.size = size;
        uuid = UUID.randomUUID();
        graphics = new BinaryGraphicsArray(size.x*16-4, size.y*16-4);
        api = APILoader.TableizeAPI(new ProjectorAPI(this), null);
        loaded = true;
        masterBlock = null;
        tag=null;
        markDirty();
        renderBinaryGraphics(graphics);
    }

    @Override
    public String[] getFunctionNames() {
        return new String[]{"drawPixel", "getSize", "drawLine", "drawRec", "draw", "clear"};
    }

    @Override
    public Value<?> callFunction(Runtime runtime, String name, Value<?>... Args) {
        if (!loaded) return Value.asError("Display not loaded");
        if (leader){
            AtomicReference<Value<?>> dummy = new AtomicReference<>();
            api.foreach((key, value) -> {
                if (key.getValue().equals(name)) {
                    dummy.set(value.toFunction().invoke(new FunctionInput(new LinkedList<>(List.of(Args)))));
                }
            });
            markDirty();
            return dummy.get()==null ? Value.asError("Cant Find Function '"+name+"'") : dummy.get();
        }else{
            if (world!=null && masterBlock instanceof SimpleDisplayBlockEntity master) return master.callFunction(runtime, name, Args);
            return Value.asError("Display not loaded");
        }
    }

    @Override
    public String getTypeName() {
        return "neetcomputers:simple_display";
    }

    @Override
    public UUID getUuid() {
        if (!leader && world!=null && world.getBlockEntity(masterPos) instanceof SimpleDisplayBlockEntity master) return master.getUuid();
        if (uuid==null) uuid = UUID.randomUUID();
        markDirty();
        return uuid;
    }

    @Override
    public String getTag() {
        if (!leader && world!=null && masterBlock instanceof SimpleDisplayBlockEntity master) return master.getTag();
        return tag;
    }

    @Override
    public void setTag(@NotNull String tag) {
        if (!leader && world!=null && masterBlock instanceof SimpleDisplayBlockEntity master) {
            master.setTag(tag);
            return;
        }
        this.tag = tag.isBlank() ? null : tag.trim();
    }

    public void renderBinaryGraphics(BinaryGraphicsArray graphics){
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new BinaryGraphicsPayload(getPos(), graphics));

        if (getWorld() instanceof ServerWorld serverWorld) {
            if (serverWorld.getPlayers().isEmpty()) dirtyGraphics = true;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public Vector2i getSize(){return leader ? size : null;}

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
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
        return getWorld()!=null && !getWorld().getBlockState(getPos()).isAir() && getWorld().getBlockState(getPos()).get(SimpleDisplayBlock.LEADER);
    }

    @Override
    public void setBinaryGraphics(BinaryGraphicsArray graphicsArray) {
        graphics = graphicsArray;
        dirtyGraphics = true;
    }
}
