package com.redtoast.simulation.connectionManager;

import com.redtoast.simulation.networkInterfaces.NetworkAssociations;
import com.redtoast.simulation.networkInterfaces.NetworkConsumer;
import com.redtoast.simulation.networkInterfaces.NetworkProvider;
import com.redtoast.simulation.peripheralInterfaces.PeripheralAssociations;
import com.redtoast.simulation.peripheralInterfaces.PeripheralConsumer;
import com.redtoast.simulation.peripheralInterfaces.PeripheralProvider;

public class Session {
    public enum SessionType{
        INVALID,
        NETWORK,
        PERIPHERAL
    }
    private final SessionType type;
    private final ConnectionConsumer consumer;
    private final ConnectionProvider provider;
    private Session(NetworkConsumer pointA, NetworkProvider pointB){
        type = SessionType.NETWORK;
        consumer = pointA;
        provider = pointB;
    }
    private Session(PeripheralConsumer pointA, PeripheralProvider pointB){
        type = SessionType.PERIPHERAL;
        consumer = pointA;
        provider = pointB;
    }
    private Session(){
        type = SessionType.INVALID;
        consumer = null;
        provider = null;
    }
    public final SessionType getType(){
        return type;
    }
    private void forceValidity(){
        if (type==SessionType.INVALID) throw new RuntimeException("Session accessed while invalid");
    }
    public ConnectionConsumer getConsumer(){
        forceValidity();
        return consumer;
    }
    public ConnectionProvider getProvider(){
        forceValidity();
        return provider;
    }
    public boolean isValid(){return type!=SessionType.INVALID;}
    public static Session createNetworkingSession(Object pointA, Object pointB){
        if (NetworkAssociations.hasConsumer(pointA) && NetworkAssociations.hasProvider(pointB)){
            return new Session(NetworkAssociations.extractConsumer(pointA), NetworkAssociations.extractProvider(pointB));
        }
        if (NetworkAssociations.hasConsumer(pointB) && NetworkAssociations.hasProvider(pointA)){
            return new Session(NetworkAssociations.extractConsumer(pointB), NetworkAssociations.extractProvider(pointA));
        }
        return new Session();
    }
    public static Session createPeripheralSession(Object pointA, Object pointB){
        if (PeripheralAssociations.hasConsumer(pointA) && PeripheralAssociations.hasProvider(pointB)){
            return new Session(PeripheralAssociations.extractConsumer(pointA), PeripheralAssociations.extractProvider(pointB));
        }
        if (PeripheralAssociations.hasConsumer(pointB) && PeripheralAssociations.hasProvider(pointA)){
            return new Session(PeripheralAssociations.extractConsumer(pointB), PeripheralAssociations.extractProvider(pointA));
        }
        return new Session();
    }
}