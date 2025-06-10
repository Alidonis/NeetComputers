package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.LangError;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.Runtime;

import java.util.LinkedList;
import java.util.UUID;

public class ChipAPI implements API {
    Computer computer;
    Runtime vm;

    public ChipAPI(Computer parent) {
        computer = parent;
        vm = computer.getRuntime();
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
    public String getMachine(){
        return computer.getSpecifications().MachineName;
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
        UUID uuid = vm.getRunningThread().getUuid();
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
        computer.stop();
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