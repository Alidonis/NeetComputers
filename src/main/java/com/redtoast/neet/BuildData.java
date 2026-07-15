package com.redtoast.neet;

import java.io.InputStream;
import java.util.Scanner;
import com.google.gson.*;

public class BuildData {
    public static String VERSION = "";
    public static String BUILD_TIME = "";
    public static String LUA_VERSION = "";

    public static void updateDat() {
        InputStream buildDatFile = BuildData.class.getClassLoader().getResourceAsStream("data/neetcomputers/builddat.json");
        Scanner read = new Scanner(buildDatFile);
        String buildDatJson = "";
        while (read.hasNextLine()) {
            buildDatJson += read.nextLine();
        }
        JsonObject buildDat = JsonParser.parseString(buildDatJson).getAsJsonObject();;

        VERSION = buildDat.get("version").getAsString();
        BUILD_TIME = buildDat.get("buildTime").getAsString();
        LUA_VERSION = buildDat.get("luaVersion").getAsString();
    }
}
