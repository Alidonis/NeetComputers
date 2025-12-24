package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.screens.BoilerplateScreen;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.Runtime;

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
        return NeetComputersServer.version;
    }

    @Exposed
    public int ToAsciiFromGLFW(int code, int mod){
        return BoilerplateScreen.mapGlfwKeyToAsciiCode(code, mod);
    }

    @Exposed
    public int ToAsciiFromGLFW(int code){
        return BoilerplateScreen.mapGlfwKeyToAsciiCode(code, 0);
    }

    @Override
    public String getLabel() {
        return "chip";
    }
}