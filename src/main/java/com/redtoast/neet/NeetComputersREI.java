package com.redtoast.neet;

import com.redtoast.graphics.GraphicsScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

import java.util.List;

public class NeetComputersREI  implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(GraphicsScreen.class, screen -> {
            // screen is OurScreen
            // returns the list of rectangle
            return List.of(new Rectangle(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE));
        });
    }
}
