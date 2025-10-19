package com.redtoast.neet;

import com.redtoast.Connections.CableRenderer;
import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.screens.RGBGraphicsScreen;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class NeetComputersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Setup pipe renderer
		WorldRenderEvents.AFTER_ENTITIES.register(CableRenderer::eventCallback);

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(NeetComputers.GRAPHICS_SCREEN_HANDLER, RGBGraphicsScreen::new);

		try {
			Class<?> reiScreenRegistryClass = Class.forName("me.shedaniel.rei.api.client.gui.screen.REIScreenRegistry");
			Object reiScreenRegistryInstance = reiScreenRegistryClass.getMethod("getInstance").invoke(null);

			reiScreenRegistryClass
				.getMethod("registerExclusionZones", Class.class, Function.class)
				.invoke(reiScreenRegistryInstance, RGBGraphicsScreen.class, (Function<RGBGraphicsScreen, List<Rectangle>>) screen -> {
					int x = screen.screenPos1.x;
					int y = screen.screenPos1.y;
					int w = screen.screenPos2.x - screen.screenPos1.x;
					int h = screen.screenPos2.y - screen.screenPos1.y;
					return List.of(new Rectangle(x, y, w, h));
				});
		} catch (ClassNotFoundException e) {
			Logger LOGGER = LoggerFactory.getLogger("NeetComputers");
			LOGGER.warn("REI not installed");
		} catch (Throwable t) {
			t.printStackTrace();
		}
        assert NeetComputers.SCREEN_PACKET_ID != null;
        ClientPlayNetworking.registerGlobalReceiver(NeetComputers.SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
            assert client.player != null;
            if (client.player.currentScreenHandler instanceof RGBScreenHandler) {
				((RGBScreenHandler) client.player.currentScreenHandler).updateGraphics(RGBGraphicsArray.fromPacket(buf));
			}
			client.execute(() -> {
				// Everything in this lambda is run on the render thread
			});
		});
        assert NeetComputers.BINARY_SCREEN_PACKET != null;

        ClientPlayNetworking.registerGlobalReceiver(NeetComputers.BINARY_SCREEN_PACKET, (client, handler, buf, responseSender) -> {
			assert client.player != null;
			BlockPos pos = buf.readBlockPos();
			BinaryGraphicsArray graphics = BinaryGraphicsArray.fromPacket(buf);
			client.execute(() -> {
				if (client.world == null) return;
				if (client.world.getBlockEntity(pos) == null) return;
				BlockEntity be = client.world.getBlockEntity(pos);
				if (be instanceof ComputerBlockEntity computer) {
					computer.getComputer().setBinaryGraphics(graphics);
					Objects.requireNonNull(computer.getWorld()).updateListeners(pos, computer.getCachedState(), computer.getCachedState(), 3);
				}
			});
		});
	}
}