package com.redtoast.neet;

import com.redtoast.Connections.CableRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.DesktopComputer.DesktopComputerRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopEntityComputer;
import com.redtoast.blocks.DynamicLight.DynamicLightBlock;
import com.redtoast.blocks.DynamicLight.DynamicLightBlockEntity;
import com.redtoast.blocks.LargeComputer.LargeBlockComputer;
import com.redtoast.blocks.LargeComputer.LargeComputerRenderer;
import com.redtoast.blocks.LargeComputer.LargeEntityComputer;
import com.redtoast.blocks.OfficeComputer.OfficeBlockComputer;
import com.redtoast.blocks.OfficeComputer.OfficeComputerRenderer;
import com.redtoast.blocks.OfficeComputer.OfficeEntityComputer;
import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.screens.RGBGraphicsScreen;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.items.networkingCable;
import com.redtoast.items.peripheralCable;
import com.redtoast.neet.Networking.BinaryGraphicsPayload;
import com.redtoast.neet.Networking.RGBComputerPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
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

		BlockEntityType<LargeEntityComputer> largeType = (BlockEntityType<LargeEntityComputer>) BulkRegistery.fetchBlockEntityType("large_computer");
		BulkRegistery.register(largeType, LargeComputerRenderer::new);

		BlockEntityType<DesktopEntityComputer> desktopType = (BlockEntityType<DesktopEntityComputer>) BulkRegistery.fetchBlockEntityType("desktop_computer");
		BulkRegistery.register(largeType, LargeComputerRenderer::new);

		BlockEntityType<OfficeEntityComputer> officeType = (BlockEntityType<OfficeEntityComputer>) BulkRegistery.fetchBlockEntityType("office_computer");
		BulkRegistery.register(largeType, LargeComputerRenderer::new);

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

		ClientPlayNetworking.registerGlobalReceiver(RGBComputerPayload.ID, (payload, context) -> {
            if (context.client().player.currentScreenHandler instanceof RGBScreenHandler) ((RGBScreenHandler) context.client().player.currentScreenHandler).updateGraphics(payload.graphicsArray());
		});

        ClientPlayNetworking.registerGlobalReceiver(BinaryGraphicsPayload.ID, (payload, context) -> {
			BlockPos pos = payload.blockPos();
			BinaryGraphicsArray graphics = payload.graphicsArray();
			context.client().execute(() -> {
				if (context.client().world == null) return;
				if (context.client().world.getBlockEntity(pos) == null) return;
				BlockEntity be = context.client().world.getBlockEntity(pos);
				if (be instanceof ComputerBlockEntity computer) {
					computer.getComputer().setBinaryGraphics(graphics);
					Objects.requireNonNull(computer.getWorld()).updateListeners(pos, computer.getCachedState(), computer.getCachedState(), 3);
				}
			});
		});
	}
}