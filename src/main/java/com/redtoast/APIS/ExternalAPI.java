package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.external.RuntimePeripheralContainer;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.Hashtable;
import java.util.UUID;

public class ExternalAPI implements API {
    private final Computer computer;

    public ExternalAPI(Computer computer){
        this.computer = computer;
    }

    @Override
    public String getLabel() {
        return "external";
    }

//    public void attached(RuntimePeripheralContainer peripheral){
//
//    }
//
//    public void detached(RuntimePeripheralContainer peripheral){
//        peripheral.discovered.set(false);
//    }
//
//    public void tick(short DeltaTime){
//        computer.getPeripheralTable().forEach((key, value) -> {
//            value.peripheral.tick(DeltaTime);
//        });
//    }
//
//    /**
//     * forces the computer to consider the peripheral removed, this may reset with reloads or power cycles
//     * @param uuid the UUID of the peripheral source
//     * @return weather the peripheral was successfully removed
//     */
//    @Exposed
//    public boolean detachPeripheral(String uuid){
//        UUID uuid2;
//        try{
//            uuid2 = UUID.fromString(uuid);
//        }catch (Throwable ignored){
//            return false;
//        }
//        if (computer.getPeripheralTable().containsKey(uuid2) && computer.getPeripheralTable().get(uuid2).peripheral.canBeDisabled()){
//            return computer.detachPeripheral(computer.getPeripheralTable().get(uuid2).getProviderUuid());
//        }
//        return false;
//    }
//
//    /**
//     * fetches the API of the peripheral targeted
//     * @param uuid the UUID of the peripherals source
//     * @return the API, as a callable table, of the peripheral
//     */
//    @Exposed
//    public Table getPeripheral(String uuid){
//        UUID uuid2;
//        try{
//            uuid2 = UUID.fromString(uuid);
//        }catch (Throwable ignored){
//            return null;
//        }
//        if (computer.getPeripheralTable().containsKey(uuid2)){
//            return computer.getPeripheralTable().get(uuid2).table;
//        }
//        return null;
//    }
//
//    /**
//     * generates a list of peripheral source UUID's that are available and of the given type
//     * @return a list of UUID's
//     */
//    @Exposed
//    public List findSources(String type){
//        List list = new List();
//        computer.getPeripheralTable().forEach((key, value) -> {
//            if (value.peripheral.getName().equalsIgnoreCase(type)){
//                list.add(Value.of(key.toString()));
//            }
//        });
//        return list;
//    }
//
//    /**
//     * checks to see if the peripheral associated with the provided UUID exists
//     * @param uuid the UUID of the peripheral
//     * @return the checked state
//     */
//    @Exposed
//    public boolean peripheralExists(String uuid){
//        UUID uuid2;
//        try{
//            uuid2 = UUID.fromString(uuid);
//        }catch (Throwable ignored){
//            return false;
//        }
//        return computer.getPeripheralTable().containsKey(uuid2);
//    }
//
//    /**
//     * gets a list of the UUID's of all attached peripherals
//     * @return list of UUID's
//     */
//    @Exposed
//    public List getAllSources(){
//        List list = new List();
//        computer.getPeripheralTable().forEach((key, value) -> {
//            list.add(Value.of(key.toString()));
//        });
//        return list;
//    }
//
//    /**
//     * fetches a list of all peripheral types attached to the computer
//     * @return list of types
//     */
//    @Exposed
//    public List getAllTypes(){
//        List list = new List();
//        computer.getPeripheralTable().forEach((key, value) -> {
//            if (!list.contains(Value.of(value.peripheral.getName()))) list.add(Value.of(value.peripheral.getName()));
//        });
//        return list;
//    }
//
//    /**
//     * finds the types of a peripheral based off its UUID, or null if the peripheral doesn't exist
//     * @return the types of the peripheral or null
//     */
//    @Exposed
//    public String getType(String uuid){
//        UUID uuid2;
//        try{
//            uuid2 = UUID.fromString(uuid);
//        }catch (Throwable ignored){
//            return null;
//        }
//        if (computer.getPeripheralTable().containsKey(uuid2)){
//            return computer.getPeripheralTable().get(uuid2).peripheral.getName();
//        }
//        return null;
//    }
}
