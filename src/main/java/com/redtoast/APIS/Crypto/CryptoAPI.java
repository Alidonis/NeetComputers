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
    AES aesInstance;
    LuaCryptoHashing hashInstance;
    LuaCryptoSecureRNG rngInstance;
    LuaCryptoBase64 base64Instance;

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
            rsaInstance = new RSA();
        } catch (Exception e) {
            rsaInstance = null;
        }
        aesInstance = new AES();
        hashInstance = new LuaCryptoHashing();
        rngInstance = new LuaCryptoSecureRNG();
        base64Instance = new LuaCryptoBase64();
    }

    @Override
    public Table postProcessing(Table self) {
        self.put("RSA", APILoader.TableizeAPI(rsaInstance, vm).asValue());
        self.put("AES", APILoader.TableizeAPI(aesInstance, vm).asValue());
        self.put("Hash", APILoader.TableizeAPI(hashInstance, vm).asValue());
        self.put("SecureRNG", APILoader.TableizeAPI(rngInstance, vm).asValue());
        self.put("Base64", APILoader.TableizeAPI(base64Instance, vm).asValue());
        return self;
    }

    @Override
    public String getLabel() {
        return "crypto";
    }
}