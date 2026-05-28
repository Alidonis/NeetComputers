package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.InternetManager;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Bytes;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InternetAPI implements API {
    private InternetManager internetAccess;

    public InternetAPI(Computer computer) {
        this.internetAccess = computer.getInternetManager();
    }

    @Exposed
    public int GET(String URL, Table headers) {
        try {
            Map<String, String> table = new HashMap<>();
            headers.foreach((key, value) -> table.put(Objects.requireNonNull(key.toString()), Objects.requireNonNull(value.toString())));
            return internetAccess.httpGet(new URI(URL), table);
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }catch (NullPointerException exception){
            throw new ExposedError("Invalid Header (All values must be strings)");
        }
    }

    @Exposed
    public int GET(String URL) {
        try {
            return internetAccess.httpGet(new URI(URL), Map.of());
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }
    }

    @Exposed
    public int POST(String URL, Table headers, Bytes body) {
        try {
            Map<String, String> table = new HashMap<>();
            headers.foreach((key, value) -> table.put(Objects.requireNonNull(key.toString()), Objects.requireNonNull(value.toString())));
            return internetAccess.httpPost(new URI(URL), table, body.getData());
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }catch (NullPointerException exception){
            throw new ExposedError("Invalid Header (All values must be strings)");
        }
    }

    @Exposed
    public int POST(String URL, Table headers) {
        try {
            Map<String, String> table = new HashMap<>();
            headers.foreach((key, value) -> table.put(Objects.requireNonNull(key.toString()), Objects.requireNonNull(value.toString())));
            return internetAccess.httpPost(new URI(URL), table, new byte[0]);
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }catch (NullPointerException exception){
            throw new ExposedError("Invalid Header (All values must be strings)");
        }
    }

    @Exposed
    public int POST(String URL, Bytes body) {
        try {
            return internetAccess.httpPost(new URI(URL), Map.of(), body.getData());
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }
    }

    @Exposed
    public int POST(String URL) {
        try {
            return internetAccess.httpPost(new URI(URL), Map.of(), new byte[0]);
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }
    }

    @Exposed
    public int CreateWebsocket(String URL, Table headers) {
        try {
            Map<String, String> table = new HashMap<>();
            headers.foreach((key, value) -> table.put(Objects.requireNonNull(key.toString()), Objects.requireNonNull(value.toString())));
            return internetAccess.requestWebsocket(new URI(URL), table);
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }catch (NullPointerException exception){
            throw new ExposedError("Invalid Header (All values must be strings)");
        }
    }

    @Exposed
    public int CreateWebsocket(String URL) {
        try {
            return internetAccess.requestWebsocket(new URI(URL), Map.of());
        }catch (URISyntaxException exception){
            throw new ExposedError("Invalid URL");
        }
    }

    @Exposed
    public boolean hasAccess() {
        return InternetManager.hasAccess();
    }

    @Exposed
    public boolean isReady(){
        return internetAccess.ready() && hasAccess();
    }

    @Override
    public String getLabel() {
        return "internet";
    }
}
