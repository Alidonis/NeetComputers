package com.redtoast.neet;

import com.redtoast.Computer;

import java.util.Hashtable;
import java.util.UUID;

public class ComputerStorage extends Hashtable<UUID, Computer> {
    public static ComputerStorage storage = new ComputerStorage();
}