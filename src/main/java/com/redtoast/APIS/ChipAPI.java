package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.ExposedError;
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
    public int getTimeAlive(){return vm.TTL;}

    @Exposed
    public String getUUID(){
        return computer.getUuid().toString();
    }

    @Exposed
    public String getMachine(){
        return computer.getSpecifications().MachineName;
    }

    @Exposed
    public void shutdown(){
        computer.stop();
    }

    @Exposed
    public void crash(String message){computer.crash(message);}

    @Exposed
    public String version(){
        return NeetComputers.version;
    }

    @Override
    public String getLabel() {
        return "chip";
    }
}