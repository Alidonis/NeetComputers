package com.redtoast.simulation.APIS;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.LangAPI.*;
import com.redtoast.simulation.LangThread;
import com.redtoast.simulation.Runtime;

import java.util.LinkedList;
import java.util.UUID;

public class ChipAPI implements API {
    Computer computer;
    Runtime vm;

    public ChipAPI(Computer parent, Runtime VM) {
        computer = parent;
        vm = VM;
    }

    @Exposed
    public int getTime(){
        return (int) System.currentTimeMillis();
    }

    @Exposed
    public String getUUID(){
        return computer.getUuid().toString();
    }

    @Exposed
    public int getThreadCount(){
        return vm.getThreads().size();
    }

    @Exposed
    public int getMaxThreadCount(){
        return computer.getSpecifications().MaxCores;
    }

    @Exposed
    public String createThread(String script){
        if (vm.getThreads().size()>=computer.getSpecifications().MaxCores) throw new LangError("Thread cap for this machine reached, cant make more threads");
        UUID uuid = vm.MakeThread(script, "Lua 5.2");
        return uuid.toString();
    }

    @Exposed
    public String getCurrentThread(){
        UUID uuid = vm.getThread().getUuid();
        return uuid.toString();
    }

    @Exposed
    public boolean killThread(String uuid){
        LinkedList<LangThread> threads = vm.getThreads();
        for (LangThread thread : threads){
            if (thread.getUuid().toString().equals(uuid)){
                thread.kill();
                return true;
            }
        }
        return false;
    }

    @Exposed
    public void shutdown(){
        computer.Stop();
    }

    @Exposed
    public String version(){
        return NeetComputers.version;
    }

    @Override
    public String getLabel() {
        return "chip";
    }
}