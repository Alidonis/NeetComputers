package com.redtoast.neet;

import com.redtoast.graphics.GraphicsScreen;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class NeetComputersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(NeetComputers.GRAPHICS_SCREEN_HANDLER, GraphicsScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(NeetComputers.SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
			if (client.player.currentScreenHandler instanceof GraphicsScreenHandler) {
				((GraphicsScreenHandler) client.player.currentScreenHandler).updateGraphics(RGBGraphicsArray.fromPacket(buf));
			}
			client.execute(() -> {
				// Everything in this lambda is run on the render thread
			});
		});
	}
}