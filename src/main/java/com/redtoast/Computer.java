package com.redtoast;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PeripheralReceiver;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.Networking.RGBComputerPayload;
import com.redtoast.neet.ProcessManager;
import com.redtoast.simulation.*;
import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.builder.SystemBuild;
import com.redtoast.simulation.FS.builder.SystemPreset;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.config.ComputerConfig;
import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.events.EventLabel;
import com.redtoast.simulation.events.EventManager;
import com.redtoast.simulation.value.NVTable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;

/**
 * the {@link Computer} class represents the entirty of a NeetComputers computer and its subclasses.
 * <p>
 *     The {@link Computer} class has multiple steps required before it can start ticking
 * </p>
 *
 * <p>
 *     First initialize the computer instance, provide a {@link ComputerConfig} instance and implement abstract methods
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
 * @see ComputerConfig
 * @see Runtime
 * @see GlobalManager
 * @see FileSystem
 */
public abstract class Computer implements PeripheralReceiver {
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
    //object that handles the computers events
    private final EventManager eventManager = new EventManager();
    //object representing the systems build
    private SystemBuild build = null;
    //object representing the graphics render seen on some computer blocks/entity's
    private BinaryGraphicsArray BinGraphics;
    private final boolean doesBinaryGraphics;
    //increments every tick, loops back at 100
    private short clock = 0;
    //object representing colored graphics (gui)
    private final RGBGraphicsArray Graphics;
    //state defining if the computer instance is crashed
    private boolean IsCrashed = false;
    //state for defining if the computer is paused
    private boolean paused = false;
    //crash message for crash events
    private String message = null;
    //specify computer specifications
    private ComputerConfig computerConfig;
    //table storing NVRam
    private NVTable NVRam;
    //value holding last time computer ticked
    private long tickTime;
    //tells the computer to shut down at the end of a tick cycle
    private boolean killFlag = false;
    //stores hard library's
    private final Hashtable<String, API> hardLibrarys = new Hashtable<>();
    private final Hashtable<String, String> libraryAliases = new Hashtable<>();

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
    public abstract boolean isClient();
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
    public Computer(ComputerConfig computerConfig){
        Graphics = new RGBGraphicsArray(computerConfig.ColorGraphicsSize().x(), computerConfig.ColorGraphicsSize().y());
        this.computerConfig = computerConfig;
        doesBinaryGraphics = computerConfig.doesBinaryGraphics();
        BinGraphics = computerConfig.doesBinaryGraphics() ? new BinaryGraphicsArray(computerConfig.ColorGraphicsSize().x(), computerConfig.ColorGraphicsSize().y()) : null;
        tickTime = System.currentTimeMillis();
    }

    //generic load function all other load functions call after implementing data
    private void load(){
        loaded = true;
        if (uuid==null) uuid = UUID.randomUUID();
        NeetComputersServer.computerMap.put(uuid, this);
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
                }else{
                    uuid = UUID.randomUUID();
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
    //loads computer from data component
    public void load(ComputerDataComponent dataComponent){
        if (!loaded){
            pointer = dataComponent.address();
            IsOn = dataComponent.isOn();
            uuid = dataComponent.id();
            build = dataComponent.build();
            if (dataComponent.getNVRam().isPresent()){
                NVRam = NVTable.deserialize(dataComponent.NVRam(), this);
            }else{
                NVRam = new NVTable(this);
            }
            load();
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
            if (doesBinaryGraphics){
                for (int x = 0; x < BinGraphics.getSize().x; x++){
                    for (int y = 0; y < BinGraphics.getSize().y; y++){
                        BinGraphics.set(x,y,false);
                    }
                }
            }

            Graphics.clear();
            eventManager.reset();

            //overwrites runtime with a new instance
            runtime = new Runtime(this, NVRam) {
                @Override
                public boolean shouldDie() {
                    boolean temp = killFlag;
                    if (temp) killFlag = false;
                    return temp;
                }
            };

            //adds context-sensitive peripherals
            new APILoader(this);

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
            if (runtime.isInTick()){
                killFlag = true;
            }else{
                eventManager.reset();
                runtime=null;
                IsOn =false;
                saveNBT();
            }
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
        if (isOn()) Objects.requireNonNull(runtime.getThread()).yield();
    }

    //one line fetch methods
    public boolean isOn(){return IsOn;}
    public boolean isDead(){return !IsOn;}
    public boolean isCrashed(){return IsCrashed;}
    public String getCrashMessage(){return IsCrashed ? message : null;}
    public boolean isLoaded(){return loaded;}
    public boolean hasBinaryGraphics() {return doesBinaryGraphics;}
    public FileSystem getFs() {return fs;}
    public EventManager getEventManager() {return eventManager;}
    public NVTable getNVRam(){return NVRam;}
    public SystemBuild getBuild() {return build;}
    public RGBGraphicsArray getGraphics() {
        return Graphics;
    }
    public @Nullable Runtime getRuntime() {
        return runtime;
    }
    public int getAddress(){return fs.pointer;}
    public UUID getUuid() {return uuid;}
    public ComputerConfig getConfiguration(){
        return computerConfig;
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
            return runtime.getGlobals();
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

    //sets internal non-mutable library
    public void setLibrary(String name, API api){
        hardLibrarys.put(name.toLowerCase(), api);
    }

    //creates a name alias in the library system
    public void createLibraryAlias(String alias, String rootName) {
        libraryAliases.put(alias.toLowerCase(), rootName.toLowerCase());
    }

    //tests to see if hard library exists
    public boolean libraryExists(String label){
        return libraryAliases.containsKey(label.toLowerCase()) || hardLibrarys.containsKey(label.toLowerCase());
    }

    //returns the library associated with label or its alias, or null if no such library exists
    public API getLibrary(String label){
        if (hardLibrarys.containsKey(label.toLowerCase())){
            return hardLibrarys.get(label.toLowerCase());
        }else if (libraryAliases.containsKey(label.toLowerCase())){
            return hardLibrarys.get(libraryAliases.get(label.toLowerCase()));
        }else{
            return null;
        }
    }

    //ticks the computer
    public void tick(World world){
        short delta = (short) (System.currentTimeMillis() - tickTime);
        tickTime = System.currentTimeMillis();
        if (loaded){
            if (NeetComputersServer.worldPath!=null && fs==null && build!=null){
                fs = new FileSystem(build, pointer, this);
                setLibrary("file system", fs);
                createLibraryAlias("filesystem", "file system");
                createLibraryAlias("fs", "file system");
            }
            if (fs!=null) maintainState();
            if (NeetComputersServer.worldPath!=null && !IsCrashed){
                step(delta);
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.currentScreenHandler instanceof RGBScreenHandler g && g.comp == this) ServerPlayNetworking.send((ServerPlayerEntity) p, new RGBComputerPayload(Graphics));
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
    public void step(short delta){
        if (loaded){
            if (IsOn && runtime !=null && !IsCrashed){
                if (runtime.isDead()){
                    stop();
                }else{
                    if (IsOn) {
                        ProcessManager.queComputerTick(this);
                    }
                }
            }
        }
    }

    //gets a list of all peripheral providers on the system
    public List<PeripheralProvider> getPeripheralProviders(){
        return scanForPeripherals();
    }

    //queues an event to the event manager
    public void queueEvent(EventGeneric event, EventLabel queue){
        eventManager.queueEvent(event, queue);
    }

    //writes current state to NBT tag
    public NbtCompound saveNBT(NbtCompound nbt){
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
            NVRam.clear();
        }
        return nbt;
    }

    //writes current state to item
    public ComputerDataComponent saveToItem(){
        NbtCompound NVRamObj = null;
        try{
            if (NVRam!=null && !IsCrashed) NVRamObj = NVRam.serialize();
        }catch (Throwable ignored){
            debug.info("Failed to save NVRam: {}", ignored.toString());
            NVRam.clear();
        }
        return new ComputerDataComponent(
                pointer,
                IsOn && !isCrashed(),
                uuid,
                build,
                NVRamObj
        );
    }

    //maintenance function that detects a difference in the computers state and its actual state and corrects it
    private void maintainState(){
        if (IsOn && runtime ==null && loaded && fs!=null && !IsCrashed){
            Graphics.clear();
            runtime = new Runtime(this, NVRam) {
                @Override
                public boolean shouldDie() {
                    boolean temp = killFlag;
                    if (temp) killFlag = false;
                    return temp;
                }

            };

            new APILoader(this);
            runtime.load();
        }
    }
}