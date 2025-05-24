package com.redtoast;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.lua.FileHandler;
import com.redtoast.lua.LuaVM;
import com.redtoast.lua.IDFactory;
import com.redtoast.lua.events.LuaEvent;
import com.redtoast.lua.peripheral.peripheralWrapper;
import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector2i;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.UUID;

public class Computer {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    private LuaVM VM;
    private int pointer = 0;
    private boolean loaded = false;
    private int ROM = -1;
    private boolean isOn = false;
    private final AnyEntity parent;
    private UUID uuid = null;
    private FileHandler FS;
    private BinaryGraphicsArray BinGraphics;
    private boolean doesBinaryGraphics = false;
    private short clock = 0;
    private RGBGraphicsArray Graphics;
    private final LinkedList<peripheralWrapper> peripherals = new LinkedList<>();
    public Vector2i mousePos;

    public boolean IsOn(){return isOn;}
    public int getPointer(String rootName){
        FileHandler.rootDir root = FS.findRoot(rootName);
        if (root != null){
            return root.pointer;
        }
        return 0;
    }

    public BinaryGraphicsArray getBinaryGraphics() {
        return BinGraphics;
    }
    public void setBinaryGraphics(BinaryGraphicsArray graphics) {
        BinGraphics = graphics;
        doesBinaryGraphics = true;
    }
    public void broadcastGraphics(){
        if (!doesBinaryGraphics) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(parent.getLocation());
        getBinaryGraphics().writeScreenToPacketBuf(buf);
        assert NeetComputers.BINARY_SCREEN_PACKET != null;

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(NeetComputers.BINARY_SCREEN_PACKET, buf);

        if (parent.getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public boolean attachPeripheral(peripheralWrapper peripheral){
        for (com.redtoast.lua.peripheral.peripheralWrapper peripheralWrapper : peripherals) {
            if (peripheralWrapper.uuid.equals(peripheral.uuid)) {
                return false;
            }
        }
        peripherals.add(peripheral);
        return true;
    }

    public boolean detachPeripheral(UUID uuid){
        for (int i = 0; i < peripherals.size(); i++){
            if (peripherals.get(i).uuid.equals(uuid)){
                peripherals.remove(i);
                return true;
            }
        }
        return false;
    }

    public void queueEvent(LuaEvent event) {
        System.out.println("Queued event! "+event.getName()+" "+event.getValue());
    }

    public RGBGraphicsArray getGraphics() {
        return Graphics;
    }

    private static class AnyEntity{
        public int type;
        private BlockEntity blockEntity;
        public AnyEntity(Entity parentEntity){
            type = 0;
        }
        public AnyEntity(BlockEntity parentEntity){
            type = 1;
            blockEntity = parentEntity;
        }
        public void markDirty(){
            switch (type){
                case 0:
                    return;
                case 1:
                    blockEntity.markDirty();
                    if (blockEntity.getWorld() != null && !blockEntity.getWorld().isClient) {
                        blockEntity.getWorld().updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
                    }
            }
        }
        public BlockPos getLocation(){
            switch (type){
                case 0:
                    return null;
                case 1:
                    return blockEntity.getPos();
            }
            return null;
        }
        public World getWorld(){
            switch (type){
                case 0:
                    return null;
                case 1:
                    return blockEntity.getWorld();
            }
            return null;
        }
    }

    public Computer(BlockEntity Parent){
        parent = new AnyEntity(Parent);
        uuid = UUID.randomUUID();
        Graphics = new RGBGraphicsArray(128,96);
    }

    public UUID getUuid() {return uuid;}

    public boolean isLoaded(){return loaded;}
    private void Load(){
        loaded = true;
        if (NeetComputers.worldPath!=null){
            FS = new FileHandler(pointer,ROM,"null");
        }
        if (isOn){
            Start();
        }
        if (uuid==null) uuid = UUID.randomUUID();
    }
    public void Load(NbtCompound nbt){
        if (!loaded){
            pointer = nbt.getInt("UserPointer");
            ROM = nbt.getInt("ROMPointer");
            if (nbt.contains("isOn")){
                isOn = nbt.getBoolean("isOn");
                if (isOn && doesBinaryGraphics && nbt.contains("screen")){
                    BinGraphics = BinaryGraphicsArray.fromNbt(nbt.getCompound("screen"));
                }
                if (nbt.contains("ComputerID")){
                    uuid = nbt.getUuid("ComputerID");
                }
            }else{
                isOn = false;
            }
            Load();
        }else{
            if (nbt.contains("isOn")){
                if (nbt.contains("isOn")!=isOn){
                    if (nbt.getBoolean("isOn")){
                        Start();
                    }else{
                        Stop();
                    }
                }
            }
        }
    }
    public void Load(int UserPointer, int ROMPointer){
        if (!loaded){
            pointer = UserPointer;
            ROM = ROMPointer;
            isOn = false;
            Load();
        }
    }

    public void Load(MinecraftServer GameServer){
        if (!loaded){
            assert GameServer != null;
            IDFactory.getServerState(GameServer);
            IDFactory.PointerIteration++;
            pointer = IDFactory.PointerIteration;
            ROM = -1;
            isOn = false;
            Load();
        }
    }

    public NbtCompound writeNBT(NbtCompound nbt){
        nbt.putInt("UserPointer", pointer);
        nbt.putInt("ROMPointer",ROM);
        nbt.putBoolean("isOn",isOn);
        if (isOn && doesBinaryGraphics){
            nbt.put("screen", BinGraphics.writeScreenToNBT());
        }
        if (uuid!=null) nbt.putUuid("ComputerID",uuid);
        return nbt;
    }

    public void Start(){
        if (!isOn && loaded){
            for (int x = 0; x < BinGraphics.getSize().x; x++){
                for (int y = 0; y < BinGraphics.getSize().y; y++){
                    BinGraphics.set(x,y,false);
                }
            }
            VM = new LuaVM(this, FS, pointer, ROM) {
                @Override
                public LinkedList<peripheralWrapper> getPeripherals() {
                    return peripherals;
                }
            };
            isOn = true;
            markDirty();
        }
    }

    public void staticStart(){
        if (isOn && VM==null){
            VM = new LuaVM(this, FS, pointer, ROM) {
                @Override
                public LinkedList<peripheralWrapper> getPeripherals() {
                    return peripherals;
                }
            };
        }
    }

    public void Stop(){
        if (isOn && loaded){
            VM=null;
            isOn=false;
            markDirty();
        }
    }

    private void markDirty(){
        parent.markDirty();
    }

    private void step(){
        if (loaded){
            if (isOn && VM!=null){
                if (VM.isDead()){
                    Stop();
                }else{
                    if (isOn) {
                        VM.tick();
                    }
                }
            }
        }
    }

    public void Tick(World world){
        if (loaded){
            if (NeetComputers.worldPath!=null && FS==null){
                FS = new FileHandler(pointer,ROM,"null");
            }
            if (NeetComputers.worldPath!=null){
                if (isOn && VM==null) {
                    Start();
                }
                step();
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.currentScreenHandler instanceof GraphicsScreenHandler g && g.comp.computer == this) {
                        PacketByteBuf temp = PacketByteBufs.create();
                        Graphics.writeScreenToPacketBuf(temp);
                        ServerPlayNetworking.send((ServerPlayerEntity) p, NeetComputers.SCREEN_PACKET_ID, temp);
                    }
                }
            }
            if (clock%5==0 && doesBinaryGraphics) broadcastGraphics();
            clock += 1;
            clock %= 1;
        }
    }
}
