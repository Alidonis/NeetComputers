package com.redtoast.neet;

import com.redtoast.graphics.GraphicsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class NeetComputersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(NeetComputers.GRAPHICS_SCREEN_HANDLER, GraphicsScreen::new);
	}
}