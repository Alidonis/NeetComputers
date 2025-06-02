package com.redtoast;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.FileHandler;
import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.IDFactory;
import com.redtoast.simulation.peripheral.peripheralAPI;
import com.redtoast.simulation.peripheral.peripheralWrapper;
import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.UUID;

public abstract class Computer {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    private static final Logger errer = LoggerFactory.getLogger("NeetComputers:error");
    private Runtime VM;
    private int pointer = 0;
    private boolean loaded = false;
    private int ROM = -1;
    private boolean isOn = false;
    private UUID uuid = null;
    private FileHandler FS;
    private BinaryGraphicsArray BinGraphics;
    private boolean doesBinaryGraphics = false;
    private short clock = 0;
    private RGBGraphicsArray Graphics;
    private LinkedList<peripheralWrapper> peripherals;
    public Vector2i mousePos;
    private final LinkedList<EventGeneric> eventQue = new LinkedList<>();
    private ComputerSpecs specs;

    public Computer(ComputerSpecs specifications){
        Graphics = new RGBGraphicsArray(specifications.ColorGraphicsSizeX,specifications.ColorGraphicsSizeY);
        specs = specifications;
        doesBinaryGraphics = specifications.doesGraphics;
        BinGraphics = new BinaryGraphicsArray(specifications.GraphicsSizeX, specifications.GraphicsSizeY);
    }

    public boolean IsOn(){return isOn;}
    public int getPointer(String rootName){
        FileHandler.rootDir root = FS.findRoot(rootName);
        if (root != null){
            return root.pointer;
        }
        return 0;
    }

    public abstract void saveNBT();
    public abstract World getWorld();
    public abstract void refreshBinaryGraphics();

    public @Nullable BinaryGraphicsArray getBinaryGraphics() {
        if (!doesBinaryGraphics) return null;
        return BinGraphics;
    }
    public void setBinaryGraphics(BinaryGraphicsArray graphics) {
        if (!doesBinaryGraphics) return;
        BinGraphics = graphics;
    }
    public boolean hasBinaryGraphics() {return doesBinaryGraphics;}

    public ComputerSpecs getSpecifications(){
        return specs;
    }

    public boolean attachPeripheral(peripheralWrapper peripheral){
        for (com.redtoast.simulation.peripheral.peripheralWrapper peripheralWrapper : peripherals) {
            if (peripheralWrapper.uuid.equals(peripheral.uuid)) {
                return false;
            }
        }
        peripherals.add(peripheral);
        return true;
    }
    public boolean attachPeripheral(peripheralAPI peripheral){
        if (peripherals == null){
            peripherals = new LinkedList<>();
        }
        for (com.redtoast.simulation.peripheral.peripheralWrapper peripheralWrapper : peripherals) {
            if (peripheralWrapper.uuid.equals(new peripheralWrapper(peripheral).uuid)) {
                return false;
            }
        }
        peripherals.add(new peripheralWrapper(peripheral));
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

    public void queueEvent(EventGeneric event) {
        if (getWorld().isClient()){
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(uuid);
            event.writeToPacket(buf);
            ClientPlayNetworking.send(NeetComputers.EVENT_PACKET, buf);
        }else{

        }
    }

    public RGBGraphicsArray getGraphics() {
        return Graphics;
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
        NeetComputers.computerMap.put(uuid, this);
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
            VM = new Runtime(this, FS, pointer, ROM) {
                @Override
                public LinkedList<peripheralWrapper> getPeripherals() {
                    return peripherals;
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return eventQue;
                }

                @Override
                public ComputerSpecs getSpecifications() {
                    return specs;
                }
            };
            isOn = true;
            saveNBT();
        }
    }

    private void staticStart(){
        if (isOn && VM==null && loaded && FS!=null){
            VM = new Runtime(this, FS, pointer, ROM) {
                @Override
                public LinkedList<peripheralWrapper> getPeripherals() {
                    return peripherals;
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return eventQue;
                }

                @Override
                public ComputerSpecs getSpecifications() {
                    return specs;
                }
            };
        }
    }

    public void Stop(){
        if (isOn && loaded){
            VM=null;
            isOn=false;
            saveNBT();
        }
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
            staticStart();
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
            if (clock%5==0 && doesBinaryGraphics && loaded) refreshBinaryGraphics();
            clock += 1;
            clock %= 1;
        }
    }
}
