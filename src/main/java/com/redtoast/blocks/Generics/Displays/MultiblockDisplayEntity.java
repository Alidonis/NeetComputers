package com.redtoast.blocks.Generics.Displays;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.Connections.PipeType;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.UUID;

public abstract class MultiblockDisplayEntity extends BlockEntity implements PeripheralProvider, PipeRenderSource, ConnectionMappingAccess {
    public static final IntProperty STATE = IntProperty.of("state",0, 15);
    public static final BooleanProperty LEADER = BooleanProperty.of("leader");
    public static final BooleanProperty GROUP = BooleanProperty.of("group");

    private UUID uuid = null;
    private String tag = null;
    private boolean leader = true;
    private Vector2i size = new Vector2i(1,1);
    private boolean dirtyGraphics = false;
    private BlockPos masterPos = null;
    private boolean loaded = false;
    private BlockEntity masterBlock = null;
    private int clock = 0;

    public MultiblockDisplayEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
            load();
            leader = true;
            loaded = true;
        }
    }

    public abstract void load();
    public abstract void unload();
    public abstract Value<?> callLeader(Runtime runtime, String name, Value<?>... args);
    public abstract void render();

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
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(STATE, model.ordinal()), Block.NOTIFY_ALL);
    }

    public void reconnect(){
        if (world==null) return;
        Direction direction = world.getBlockState(getPos()).get(HorizontalFacingBlock.FACING);
        new ConnectionMapping(world, pos, direction, 10, 8, (pos2) -> (world.getBlockEntity(pos2) != null && world.getBlockEntity(pos2).getClass() == this.getClass() && world.getBlockState(pos2).get(HorizontalFacingBlock.FACING).equals(direction) && world.getBlockState(pos2).get(GROUP).equals(world.getBlockState(getPos()).get(GROUP)))).start();
    }

    @Override
    public void setSlave(BlockPos masterPos){
        leader = false;
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(LEADER, leader), Block.NOTIFY_ALL);
        assert world != null;
        this.masterPos = masterPos;
        unload();
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
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(LEADER, leader), Block.NOTIFY_ALL);
        this.size = size;
        uuid = UUID.randomUUID();
        load();
        loaded = true;
        masterBlock = null;
        tag=null;
        markDirty();
        render();
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState blockState, T t){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof MultiblockDisplayEntity multiblockDisplayEntity){
            if (multiblockDisplayEntity.leader && !multiblockDisplayEntity.loaded) {
                multiblockDisplayEntity.reconnect();
                multiblockDisplayEntity.loaded = true;
            }else if (!multiblockDisplayEntity.leader) {
                multiblockDisplayEntity.masterBlock = world.getBlockEntity(multiblockDisplayEntity.masterPos);
            }else {
                if (multiblockDisplayEntity.clock%2==0 && multiblockDisplayEntity.dirtyGraphics) {
                    multiblockDisplayEntity.dirtyGraphics = false;
                    multiblockDisplayEntity.render();
                }
                multiblockDisplayEntity.clock += 1;
                multiblockDisplayEntity.clock %= 100;
            }
        }
    }

    @Override
    public Value<?> callFunction(Runtime runtime, String name, Value<?>... Args) {
        if (!loaded) return Value.asError("Display not loaded");
        if (leader){
            return callLeader(runtime, name, Args);
        }else{
            if (world!=null && masterBlock instanceof MultiblockDisplayEntity master) return master.callLeader(runtime, name, Args);
            return Value.asError("Display not loaded");
        }
    }

    @Override
    public UUID getUuid() {
        if (!leader && world!=null && world.getBlockEntity(masterPos) instanceof MultiblockDisplayEntity master) return master.getUuid();
        if (uuid==null) uuid = UUID.randomUUID();
        markDirty();
        return uuid;
    }

    @Override
    public String getTag() {
        if (!leader && world!=null && masterBlock instanceof MultiblockDisplayEntity master) return master.getTag();
        return tag;
    }

    @Override
    public void setTag(@NotNull String tag) {
        if (!leader && world!=null && masterBlock instanceof MultiblockDisplayEntity master) {
            master.setTag(tag);
            return;
        }
        this.tag = tag.isBlank() ? null : tag.trim();
    }

    @Override
    public void computerAttached(Computer computer) {

    }

    @Override
    public void computerDetached(Computer computer) {

    }

    @Override
    public boolean shouldRenderPipeType(PipeType type) {
        return type==PipeType.PERIPHERAL;
    }

    public void markDirtyGraphics() {dirtyGraphics = true;}

    public Vector2i getSize() {return size;}
}
