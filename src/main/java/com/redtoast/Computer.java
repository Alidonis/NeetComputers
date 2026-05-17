package com.redtoast;

import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.Connections.PeripheralReceiver;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.blocks.Generics.Displays.BinaryGraphicsProvider;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
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
public abstract class Computer implements PeripheralReceiver, BinaryGraphicsProvider {
    //logger used for debugging
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    //the instance representing a computers runtime, cycles with computer restarts
    private Runtime runtime;
    //pointer for the folder that contains this computer's files
    private int pointer = 0;
    //determines if a 'load' function has been called, providing important information to computer, most methods won't run if this is false
    private boolean loaded = false;
    //stores the computers state
    private ComputerState state = ComputerState.OFF;
    private String crashMessage = null;
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
    private boolean graphicsDirty = false;
    //specify computer specifications
    private final ComputerConfig computerConfig;
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
        BinGraphics = computerConfig.doesBinaryGraphics() ? new BinaryGraphicsArray(computerConfig.BinaryGraphicsSize().x(), computerConfig.BinaryGraphicsSize().y()) : null;
        tickTime = System.currentTimeMillis();
    }

    //Generics load function all other load functions call after implementing data
    private void load(){
        loaded = true;
        if (uuid==null) uuid = UUID.randomUUID();
        if (doesBinaryGraphics) refreshBinaryGraphics();
        maintainState();
    }
    //loads computer from NBT data
    public void load(NbtCompound nbt){
        if (!loaded){
            pointer = nbt.getInt("Address");
            state = ComputerState.OFF;
            if (nbt.contains("State")){
                state = ComputerState.values()[nbt.getShort("State")];
            }
            if (nbt.contains("ComputerID")){
                uuid = nbt.getUuid("ComputerID");
            }else{
                uuid = UUID.randomUUID();
            }
            if (nbt.contains("build")){
                build = new SystemBuild(nbt.getCompound("build"));
            }else{
                build = new SystemBuild(SystemPreset.NEETOS);
                debug.warn("Failed to load computer build [address: {}]", pointer);
            }
            if (nbt.contains("crashMessage") && state == ComputerState.CRASHED) crashMessage = nbt.getString("crashMessage");
            load();
        }
    }
    //loads computer from data component
    public void load(ComputerDataComponent dataComponent){
        if (!loaded){
            pointer = dataComponent.address();
            state = dataComponent.isOn() ? ComputerState.ON : ComputerState.OFF;
            uuid = dataComponent.id();
            build = dataComponent.build();
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
            state = ComputerState.OFF;
            build = new SystemBuild(SystemPreset.NEETOS);
            load();
        }
    }

    /*'starts' the computer if its off, does nothing if its on
     * starts referring to building a new Runtime instance and marking its state as on
     */
    public void start(){
        if (loaded && fs!=null && (state == ComputerState.OFF || state == ComputerState.PAUSED)){
            state = ComputerState.ON;
            maintainState();
        }
    }

    //marks computer as off and overrides the runtime with null
    public void stop(){
        if (loaded && fs!=null && state != ComputerState.OFF){
            state = ComputerState.OFF;
            maintainState();
        }
    }

    //marks computer as off and overrides the runtime with null
    public void pause(){
        if (loaded && fs!=null && state != ComputerState.PAUSED){
            state = ComputerState.PAUSED;
            maintainState();
        }
    }

    //sets the computer to a crashed state
    public void crash(String message){
        if (loaded && fs!=null && state != ComputerState.CRASHED){
            state = ComputerState.CRASHED;
            if (runtime==null || runtime.isDead()){
                crashMessage = message;
            }else{
                crashMessage = runtime.getCurrentSource() == null ? message : runtime.getCurrentSource() + " " + message;
            }
            this.yield();
            maintainState();
        }
    }

    //yields the computer
    public void yield(){
        if (runtime!=null && runtime.getThread()!=null) runtime.getThread().yield();
    }

    //one line fetch methods
    public boolean isOn(){return state == ComputerState.ON;}
    public boolean isDead(){return state == ComputerState.OFF;}
    public boolean isCrashed(){return state == ComputerState.CRASHED;}
    public boolean isPaused(){return state == ComputerState.PAUSED;}
    public String getCrashMessage(){return isCrashed() ? crashMessage : null;}
    public boolean isLoaded(){return loaded;}
    public boolean hasBinaryGraphics() {return doesBinaryGraphics;}
    public FileSystem getFs() {return fs;}
    public EventManager getEventManager() {return eventManager;}
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

    public ComputerState getStatus(){
        return state;
    }
    public @Nullable BinaryGraphicsArray getBinaryGraphics() {
        if (!doesBinaryGraphics) return null;
        return BinGraphics;
    }
    public @Nullable GlobalManager getGlobals(){
        if (runtime!=null && !runtime.isDead()){
            return runtime.getGlobals();
        }else{
            return null;
        }
    }
    public void renderColorGraphics(){graphicsDirty = true;}

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
            maintainState();
            if (fs!=null && runtime!=null && !runtime.isDead() && state == ComputerState.ON)
                step(delta);
            if (graphicsDirty && state == ComputerState.ON && clock%2==0) {
                ArrayList<PlayerEntity> players = new ArrayList<>();
                for (PlayerEntity p : world.getPlayers()) if (p.currentScreenHandler instanceof RGBScreenHandler g && g.comp == this) players.add(p);
                if (!players.isEmpty()){
                    RGBComputerPayload payload = new RGBComputerPayload(getGraphics());
                    for (PlayerEntity p : players) ServerPlayNetworking.send((ServerPlayerEntity) p, payload);
                }
                graphicsDirty = false;
            }
            if (clock%10==0 && doesBinaryGraphics) refreshBinaryGraphics();
            if (clock%30==0) saveNBT();
            clock += 1;
            clock %= 100;
        }
    }

    //steps the runtime forward (tick with less protection)
    public void step(short delta){
        ProcessManager.queComputerTick(this);
    }

    //gets a list of all peripheral providers on the system
    public List<PeripheralProvider> getPeripheralProviders(){
        return scanForPeripherals();
    }

    //queues an event to the event manager
    public void queueEvent(EventGeneric event, EventLabel queue){
        if (state == ComputerState.ON) eventManager.queueEvent(event, queue);
    }

    //writes current state to NBT tag
    public NbtCompound saveNBT(NbtCompound nbt){
        nbt.putInt("Address", pointer);
        nbt.putShort("State", (short) state.ordinal());
        if (build!=null) nbt.put("build", build.save());
        if (uuid!=null) nbt.putUuid("ComputerID",uuid);
        if (isCrashed() && crashMessage!=null) nbt.putString("crashMessage", crashMessage);
        return nbt;
    }

    //writes current state to item
    public ComputerDataComponent saveToItem(){
        return new ComputerDataComponent(
                pointer,
                isOn(),
                uuid,
                build
        );
    }

    //maintenance function that detects a difference in the computers state and its actual state and corrects it
    private void maintainState(){
        boolean save = false;
        if (state != ComputerState.ON) graphicsDirty = false;
        if (state != ComputerState.CRASHED && crashMessage != null) {
            crashMessage = null;
            save = true;
        }
        if (state == ComputerState.ON && runtime==null && fs!=null) {
            if (doesBinaryGraphics){
                for (int x = 0; x < BinGraphics.getSize().x; x++){
                    for (int y = 0; y < BinGraphics.getSize().y; y++){
                        BinGraphics.set(x,y,false);
                    }
                }
            }

            Graphics.clear();
            eventManager.reset();

            runtime = new Runtime(this) {
                @Override
                public boolean shouldDie() {
                    boolean temp = killFlag;
                    if (temp) killFlag = false;
                    return temp;
                }

            };

            new APILoader(this);
            runtime.load();
            save = true;
        }
        if (state == ComputerState.ON && runtime!=null && runtime.isDead()) {
            state = ComputerState.OFF;
            if (doesBinaryGraphics){
                for (int x = 0; x < BinGraphics.getSize().x; x++){
                    for (int y = 0; y < BinGraphics.getSize().y; y++){
                        BinGraphics.set(x,y,false);
                    }
                }
            }

            Graphics.clear();
            eventManager.reset();
            save = true;
        }
        if ((state == ComputerState.OFF || state == ComputerState.CRASHED) && runtime!=null) {
            if (!runtime.isDead() && runtime.isInTick()){
                killFlag = true;
            }else{
                eventManager.reset();
                runtime=null;
                save = true;
            }
        }
        if (state == ComputerState.CRASHED && crashMessage == null) {
            crashMessage = "Unknown error [No Message Provided]";
            save = true;
        }
        if (save) saveNBT();
    }
}