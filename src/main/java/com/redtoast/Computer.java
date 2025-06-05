package com.redtoast;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.FileHandler;
import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.IDFactory;
import com.redtoast.simulation.Peripheral;
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
    //logger used for debugging
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    //the instance representing a computers runtime, cycles with computer restarts
    private Runtime runtime;
    //resource pointers
    private int pointer = 0;
    private int ROM = -1;
    //determines if a 'load' function has been called, providing important information to computer, most methods won't run if this is false
    private boolean loaded = false;
    //determines if the computer is on
    private boolean isOn = false;
    //uuid representing the computer, acquired by chip.getUUID() in runtime. generated during loading
    private UUID uuid = null;
    //object representing the computers file interpreter
    private FileHandler FS;
    //object representing the graphics render seen on some computer blocks/entity's
    private BinaryGraphicsArray BinGraphics;
    private boolean doesBinaryGraphics = false;
    //increments every tick, loops back at 100
    private short clock = 0;
    //object representing colored graphics (gui)
    private RGBGraphicsArray Graphics;
    //stores pre-wrapped peripherals
    private LinkedList<Peripheral> peripheralWrappers;
    //stores un-wrapped peripherals to be wrapped with runtime context
    private final LinkedList<API> unwrappedPeripherals = new LinkedList<>();
    //stores the peripherals has access to during runtime
    private LinkedList<Peripheral> peripheralBuffer = new LinkedList<>();
    //vector determining mouse pos
    public Vector2i mousePos;
    //represents que for events
    private @Deprecated final LinkedList<EventGeneric> eventQue = new LinkedList<>();
    //specify computer specifications
    private ComputerSpecs specs;

    //abstract methods
    public abstract void saveNBT(); //commands parent object to register a saved nbt (mostly useful in blocks) (markDirty)
    public abstract World getWorld();// gets a World object from parent
    public abstract void refreshBinaryGraphics();//tells the parent object to load new binary graphics
    public abstract Object getParentEntity();//gets parent entity, not used internally

    //constructor
    public Computer(ComputerSpecs specifications){
        Graphics = new RGBGraphicsArray(specifications.ColorGraphicsSizeX,specifications.ColorGraphicsSizeY);
        specs = specifications;
        doesBinaryGraphics = specifications.doesGraphics;
        BinGraphics = new BinaryGraphicsArray(specifications.GraphicsSizeX, specifications.GraphicsSizeY);
    }

    //generic load function all other load functions call after implementing data
    private void Load(){
        loaded = true;
        if (NeetComputers.worldPath!=null){
            FS = new FileHandler(pointer,ROM,"null");
        }
        if (uuid==null) uuid = UUID.randomUUID();
        NeetComputers.computerMap.put(uuid, this);
    }
    //loads computer from NBT data
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
    //generates a new computer from scratch
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

    /*'starts' the computer if its off, does nothing if its on
     * starts referring to building a new Runtime instance and marking its state as on
     */
    public void Start(){
        if (!isOn && loaded){
            //wipes binary graphics
            for (int x = 0; x < BinGraphics.getSize().x; x++){
                for (int y = 0; y < BinGraphics.getSize().y; y++){
                    BinGraphics.set(x,y,false);
                }
            }
            //starts assembling peripherals
            peripheralBuffer = new LinkedList<>();
            peripheralBuffer.addAll(peripheralWrappers);
            //overwrites runtime with a new instance
            Computer com = this;
            runtime = new Runtime(this, FS, pointer, ROM) {
                @Override
                public LinkedList<Peripheral> getPeripherals() {
                    return com.getPeripherals();
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return com.eventQue;
                }

                @Override
                public ComputerSpecs getSpecifications() {
                    return specs;
                }
            };
            //adds context-sensitive peripherals
            for (API api : unwrappedPeripherals){
                peripheralBuffer.add(APILoader.WrapAPI(api, runtime));
            }
            //marks state as on
            isOn = true;
            saveNBT();
        }
    }

    //marks computer as off and overrides the runtime with null
    public void Stop(){
        if (isOn){
            runtime=null;
            isOn=false;
            saveNBT();
        }
    }

    //one line fetch methods
    public boolean IsOn(){return isOn;}
    public boolean isLoaded(){return loaded;}
    public boolean hasBinaryGraphics() {return doesBinaryGraphics;}
    public RGBGraphicsArray getGraphics() {
        return Graphics;
    }
    private LinkedList<Peripheral> getPeripherals() {return peripheralBuffer;}
    public UUID getUuid() {return uuid;}

    //muli-line fetch methods
    public int getPointer(String rootName){
        FileHandler.rootDir root = FS.findRoot(rootName);
        if (root != null){
            return root.pointer;
        }
        return 0;
    }
    public @Nullable BinaryGraphicsArray getBinaryGraphics() {
        if (!doesBinaryGraphics) return null;
        return BinGraphics;
    }
    public ComputerSpecs getSpecifications(){
        return specs;
    }

    //set methods
    public void setBinaryGraphics(BinaryGraphicsArray graphics) {
        if (!doesBinaryGraphics) return;
        BinGraphics = graphics;
        refreshBinaryGraphics();
    }

    //ticks the computer
    public void Tick(World world){
        if (loaded){
            maintainState();
            if (NeetComputers.worldPath!=null && FS==null){
                FS = new FileHandler(pointer,ROM,"null");
            }
            if (NeetComputers.worldPath!=null){
                if (isOn && runtime ==null) {
                    Start();
                }
                step();
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.currentScreenHandler instanceof GraphicsScreenHandler g && g.comp == this) {
                        PacketByteBuf temp = PacketByteBufs.create();
                        Graphics.writeScreenToPacketBuf(temp);
                        ServerPlayNetworking.send((ServerPlayerEntity) p, NeetComputers.SCREEN_PACKET_ID, temp);
                    }
                }
            }
            if (clock%10==0 && doesBinaryGraphics) refreshBinaryGraphics();
            clock += 1;
            clock %= 100;
        }
    }

    //steps the runtime forward (tick with less protection)
    private void step(){
        if (loaded){
            if (isOn && runtime !=null){
                if (runtime.isDead()){
                    Stop();
                }else{
                    if (isOn) {
                        runtime.tick();
                    }
                }
            }
        }
    }

    //IO methods
    public void attachPeripheral(Peripheral peripheral){
        for (Peripheral Peripheral : peripheralWrappers) {
            if (Peripheral.uuid.equals(peripheral.uuid)) {
                return;
            }
        }
        peripheralWrappers.add(peripheral);
    }
    //adds a context sensitive
    public void attachPeripheral(API peripheral){
        if (peripheralWrappers == null){
            peripheralWrappers = new LinkedList<>();
        }
        unwrappedPeripherals.add(peripheral);
    }
    //removes a peripheral based of its uuid
    public boolean detachPeripheral(UUID uuid){
        for (int i = 0; i < peripheralWrappers.size(); i++){
            if (peripheralWrappers.get(i).uuid.equals(uuid)){
                peripheralWrappers.remove(i);
                return true;
            }
        }
        return false;
    }
    //que's an event to the computer if server-side or sends event to server to be que'd if not
    public void queueEvent(EventGeneric event) {
        if (getWorld().isClient()){
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(uuid);
            event.writeToPacket(buf);
            ClientPlayNetworking.send(NeetComputers.EVENT_PACKET, buf);
        }else{
            eventQue.add(event);
        }
    }

    //writes current state to NBT tag
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

    //maintenance function that detects a difference in the computers state and its actual state and corrects it
    private void maintainState(){
        if (isOn && runtime ==null && loaded && FS!=null){
            peripheralBuffer = new LinkedList<>();
            peripheralBuffer.addAll(peripheralWrappers);
            Computer com = this;
            runtime = new Runtime(this, FS, pointer, ROM) {
                @Override
                public LinkedList<Peripheral> getPeripherals() {
                    return com.getPeripherals();
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return com.eventQue;
                }

                @Override
                public ComputerSpecs getSpecifications() {
                    return specs;
                }
            };
            for (API api : unwrappedPeripherals){
                peripheralBuffer.add(APILoader.WrapAPI(api, runtime));
            }
        }
    }
}
