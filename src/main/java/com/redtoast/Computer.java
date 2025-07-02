package com.redtoast;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.*;
import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.builder.SystemBuild;
import com.redtoast.simulation.FS.builder.SystemPreset;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.value.NVTable;
import com.redtoast.simulation.value.ValueTypes.Table;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

/**
 * the {@link Computer} class represents the entirty of a NeetComputers computer and its subclasses.
 * <p>
 *     The {@link Computer} class has multiple steps required before it can start ticking
 * </p>
 *
 * <p>
 *     First initialize the computer instance, provide a {@link computerSpecs} instance and implement abstract methods
 *     you can think of this as creating the <i>model</i> that the computer with use
 * </p>
 *
 * <p>
 *     then run {@code computer.load(params)}, there are multiple load function that load the computer differently, but this stage is critical
 *     to give the computer the data it needs to operate
 * </p>
 *
 * <p>
 *     from there you can use {@code computer.start()} or {@code computer.stop()} to control the computers state, and progress the computer using
 *     {@code computer.tick()}
 * </p>
 *
 * @see computerSpecs
 * @see Runtime
 * @see GlobalManager
 * @see FileSystem
 */
public abstract class Computer {
    //logger used for debugging
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    //the instance representing a computers runtime, cycles with computer restarts
    private Runtime runtime;
    //pointer for the folder that contains this computer's files
    private int pointer = 0;
    //determines if a 'load' function has been called, providing important information to computer, most methods won't run if this is false
    private boolean loaded = false;
    //determines if the computer is on
    private boolean IsOn = false;
    //uuid representing the computer, acquired by chip.getUUID() in runtime. generated during loading
    private UUID uuid = null;
    //object representing the computers file interpreter
    private FileSystem fs = null;
    //object representing the systems build
    private SystemBuild build = null;
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
    private Vector2i mousePos;
    //state defining if the computer instance is crashed
    private boolean IsCrashed = false;
    //state for defining if the computer is paused
    private boolean paused = false;
    //crash message for crash events
    private String message = null;
    //represents que for events
    private @Deprecated final LinkedList<EventGeneric> eventQue = new LinkedList<>();
    //specify computer specifications
    private computerSpecs specs;
    //table storing NVRam
    private NVTable NVRam;
    //value holding last time computer ticked
    private long tickTime = 0;

    //abstract methods
    /**
     * commands parent object to register a saved nbt (mostly useful in blocks)
     * typically you should have this call markDirty() if it's a blockEntity parent or be otherwise trigger NBT saving
     */
    public abstract void saveNBT();

    /**
     * gets a World object from parent
     * @return World
     */
    public abstract World getWorld();
    /**
     * triggers a reloading of the drawing of binary graphics
     * imagine this as say sending the updated graphics array to all clients to be rendered
     */
    public abstract void refreshBinaryGraphics();

    /**
     * this function should return whatever class is parent to the computer instance,
     * the output of this class is never used internally and is only meant for modders.
     * @return Object or null
     */
    public abstract @Nullable Object getParentEntity();

    //constructor
    public Computer(computerSpecs specifications){
        Graphics = new RGBGraphicsArray(specifications.ColorGraphicsSizeX,specifications.ColorGraphicsSizeY);
        specs = specifications;
        doesBinaryGraphics = specifications.doesBinaryGraphics;
        BinGraphics = new BinaryGraphicsArray(specifications.GraphicsSizeX, specifications.GraphicsSizeY);
        tickTime = System.currentTimeMillis();
    }

    //generic load function all other load functions call after implementing data
    private void load(){
        loaded = true;
        if (uuid==null) uuid = UUID.randomUUID();
        NeetComputers.computerMap.put(uuid, this);
        if (doesBinaryGraphics) refreshBinaryGraphics();
    }
    //loads computer from NBT data
    public void load(NbtCompound nbt){
        if (!loaded){
            pointer = nbt.getInt("Address");
            IsOn = false;
            if (nbt.contains("IsOn")){
                IsOn = nbt.getBoolean("IsOn");
                if (IsOn && doesBinaryGraphics && nbt.contains("screen")){
                    BinGraphics = BinaryGraphicsArray.fromNbt(nbt.getCompound("screen"));
                }
                if (nbt.contains("ComputerID")){
                    uuid = nbt.getUuid("ComputerID");
                }
            }
            if (nbt.contains("build")){
                build = new SystemBuild(nbt.getCompound("build"));
            }else{
                build = new SystemBuild(SystemPreset.NEETOS);
                debug.warn("Failed to load computer build [address: {}]", pointer);
            }
            if (nbt.contains("crashed")) IsCrashed = nbt.getBoolean("crashed");
            if (nbt.contains("crashMessage")) message = nbt.getString("crashMessage");
            if (!IsOn && IsCrashed && doesBinaryGraphics && nbt.contains("screen")) BinGraphics = BinaryGraphicsArray.fromNbt(nbt.getCompound("screen"));
            if (nbt.contains("NVRam")){
                NVRam = NVTable.deserialize(nbt.getCompound("NVRam"), this);
            }else{
                NVRam = new NVTable(this);
            }
            load();
        }else{
            if (nbt.contains("IsOn")){
                if (nbt.getBoolean("IsOn")!=IsOn){
                    start();
                }else{
                    stop();
                }
            }
        }
    }
    //generates a new computer from scratch
    public void load(MinecraftServer GameServer){
        if (!loaded){
            assert GameServer != null;
            IDFactory.getServerState(GameServer);
            IDFactory.PointerIteration++;
            pointer = IDFactory.PointerIteration;
            IsOn = false;
            build = new SystemBuild(SystemPreset.NEETOS);
            NVRam = new NVTable(this);
            load();
        }
    }

    /*'starts' the computer if its off, does nothing if its on
     * starts referring to building a new Runtime instance and marking its state as on
     */
    public void start(){
        if (!IsOn && loaded && fs!=null && !IsCrashed){
            //wipes binary graphics
            for (int x = 0; x < BinGraphics.getSize().x; x++){
                for (int y = 0; y < BinGraphics.getSize().y; y++){
                    BinGraphics.set(x,y,false);
                }
            }
            Graphics.clear();
            //starts assembling peripherals
            peripheralBuffer = new LinkedList<>();
            peripheralBuffer.addAll(peripheralWrappers);
            //overwrites runtime with a new instance
            Computer com = this;
            runtime = new Runtime(this, NVRam) {
                @Override
                public LinkedList<Peripheral> getPeripherals() {
                    return com.getPeripherals();
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return com.getEventQue();
                }
            };

            //adds context-sensitive peripherals
            new APILoader(this);
            for (API api : unwrappedPeripherals){
                peripheralBuffer.add(APILoader.WrapAPI(api, runtime));
            }

            //load the runtime (create entry thread)
            runtime.load();

            //marks state as on
            IsOn = true;
            saveNBT();
        }
    }

    //un-crashes the computer
    public void reset(){
        stop();
        IsCrashed=false;
        saveNBT();
    }

    //marks computer as off and overrides the runtime with null
    public void stop(){
        if (IsOn){
            runtime=null;
            IsOn =false;
            saveNBT();
        }
    }

    //sets the computer to a crashed state
    public void crash(String message){
        if (isOn() && !IsCrashed){
            this.message = runtime.getCurrentSource() + " " + message;
            IsCrashed = true;
            this.yield();
            saveNBT();
        }
    }

    //yields the computer
    public void yield(){
        if (isOn()) Objects.requireNonNull(runtime.getRunningThread()).yield();
    }

    //one line fetch methods
    public boolean isOn(){return IsOn;}
    public boolean isCrashed(){return IsCrashed;}
    public String getCrashMessage(){return IsCrashed ? message : null;}
    public boolean isLoaded(){return loaded;}
    public boolean hasBinaryGraphics() {return doesBinaryGraphics;}
    public FileSystem getFs() {return fs;}
    public NVTable getNVRam(){return NVRam;}
    public RGBGraphicsArray getGraphics() {
        return Graphics;
    }
    public @Nullable Runtime getRuntime() {
        return runtime;
    }
    public int getAddress(){return fs.pointer;}
    private LinkedList<Peripheral> getPeripherals() {return peripheralBuffer;}
    private LinkedList<EventGeneric> getEventQue() {return eventQue;}
    public UUID getUuid() {return uuid;}
    public computerSpecs getSpecifications(){
        return specs;
    }

    //muli-line fetch methods
    public ComputerStatus getStatus(){
        if (IsOn && paused){
            return ComputerStatus.PAUSED;
        }else if (IsOn){
            return ComputerStatus.ON;
        }
        if (isCrashed()) return ComputerStatus.CRASHED;
        return ComputerStatus.OFF;
    }
    public @Nullable BinaryGraphicsArray getBinaryGraphics() {
        if (!doesBinaryGraphics) return null;
        return BinGraphics;
    }
    public @Nullable GlobalManager getGlobals(){
        if (IsOn){
            return runtime.globalManager;
        }else{
            return null;
        }
    }

    //set methods
    public void setBinaryGraphics(BinaryGraphicsArray graphics) {
        if (!doesBinaryGraphics) return;
        BinGraphics = graphics;
        refreshBinaryGraphics();
    }

    //ticks the computer
    public void tick(World world){
        short delta = (short) (System.currentTimeMillis() - tickTime);
        tickTime = System.currentTimeMillis();
        if (loaded){
            if (NeetComputers.worldPath!=null && fs==null && build!=null){
                fs = new FileSystem(build, pointer, null);
                attachPeripheral(fs);
            }
            if (fs!=null) maintainState();
            if (NeetComputers.worldPath!=null && !IsCrashed){
                step(delta);
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.currentScreenHandler instanceof GraphicsScreenHandler g && g.comp == this) {
                        PacketByteBuf temp = PacketByteBufs.create();
                        Graphics.writeScreenToPacketBuf(temp);
                        ServerPlayNetworking.send((ServerPlayerEntity) p, NeetComputers.SCREEN_PACKET_ID, temp);
                    }
                }
            }else if (IsCrashed){
                stop();
            }
            if (clock%10==0 && doesBinaryGraphics) refreshBinaryGraphics();
            if (clock%30==0) saveNBT();
            clock += 1;
            clock %= 100;
        }
    }

    //steps the runtime forward (tick with less protection)
    private void step(short delta){
        if (loaded){
            if (IsOn && runtime !=null && !IsCrashed){
                if (runtime.isDead()){
                    stop();
                }else{
                    if (IsOn) {
                        runtime.TTL += delta;
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
        nbt.putInt("Address", pointer);
        nbt.putBoolean("IsOn", IsOn);
        if ((IsOn || IsCrashed) && doesBinaryGraphics){
            nbt.put("screen", BinGraphics.writeScreenToNBT());
        }
        if (build!=null) nbt.put("build", build.save());
        if (uuid!=null) nbt.putUuid("ComputerID",uuid);
        if (IsCrashed) nbt.putBoolean("crashed", true);
        if (IsCrashed) nbt.putString("crashMessage", message);
        try{
            if (NVRam!=null && !IsCrashed) nbt.put("NVRam", NVRam.serialize());
        }catch (Throwable ignored){
            debug.info("Failed to save NVRam: {}", ignored.toString());
        }
        return nbt;
    }

    //maintenance function that detects a difference in the computers state and its actual state and corrects it
    private void maintainState(){
        if (IsOn && runtime ==null && loaded && fs!=null && !IsCrashed){
            Graphics.clear();
            peripheralBuffer = new LinkedList<>();
            peripheralBuffer.addAll(peripheralWrappers);
            Computer com = this;
            runtime = new Runtime(this, NVRam) {
                @Override
                public LinkedList<Peripheral> getPeripherals() {
                    return com.getPeripherals();
                }

                @Override
                public LinkedList<EventGeneric> getEvents() {
                    return com.getEventQue();
                }

            };
            new APILoader(this);
            for (API api : unwrappedPeripherals){
                peripheralBuffer.add(APILoader.WrapAPI(api, runtime));
            }
            runtime.load();
        }
    }
}