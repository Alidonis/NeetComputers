package com.redtoast.APIS.Crypto;

import com.redtoast.Computer;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.ValueTypes.Table;

public class CryptoAPI implements API {
    Computer computer;
    Runtime vm;

    RSA rsaInstance;

    public CryptoAPI(Computer parent) {
        computer = parent;
        vm = computer.getRuntime();
        /* fun HACK here!
        * the java error handling makes it mandatory to handle all errors, including the most odd fuckery
        * in this case, nonexistent algorithms. But RSA is also among the required ciphers for any java implementation,
        * so what gives ?
        * Just in case though, setting it to null will result in a null table entry, which to Lua code basically means
        * nonexistent anyway.
        */
        try {
            rsaInstance = new RSA(computer);
        } catch (Exception e) {
            rsaInstance = null;
        }
    }

    @Override
    public Table postProcessing(Table self) {
        self.put("RSA", APILoader.TableizeAPI(rsaInstance, vm).asValue());
        self.put("AES", "TODO: AES instance");
        return self;
    }

    @Override
    public String getLabel() {
        return "crypto";
    }
}