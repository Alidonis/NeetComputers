package com.redtoast.neet;

import com.redtoast.Connections.CableRenderer;
import com.redtoast.Connections.PipeType;
import com.redtoast.blocks.ColorDisplay.ColorDisplayBlockEntity;
import com.redtoast.blocks.ColorDisplay.ColorDisplayRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopComputerRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopEntityComputer;
import com.redtoast.blocks.DriveBay.DriveBayBlockEntity;
import com.redtoast.blocks.DynamicLight.DynamicLightBlockEntity;
import com.redtoast.blocks.Generics.Displays.BinaryGraphicsProvider;
import com.redtoast.blocks.Generics.Displays.PipeSourceBlockRenderer;
import com.redtoast.blocks.Keyboard.KeyboardBlockEntity;
import com.redtoast.blocks.LargeComputer.LargeComputerRenderer;
import com.redtoast.blocks.LargeComputer.LargeEntityComputer;
import com.redtoast.blocks.OfficeComputer.OfficeComputerRenderer;
import com.redtoast.blocks.OfficeComputer.OfficeEntityComputer;
import com.redtoast.blocks.RedstoneController.RedstoneControllerBlockEntity;
import com.redtoast.blocks.SimpleDisplay.SimpleDisplayBlockEntity;
import com.redtoast.blocks.SimpleDisplay.SimpleDisplayRenderer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.graphics.screens.*;
import com.redtoast.neet.Networking.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class NeetComputersClient implements ClientModInitializer {
	public static BlockPos[] positionsForPipeRendering = new BlockPos[0];
	public static PipeType lastTypeSent = PipeType.PERIPHERAL;

	public static void updateClient(MinecraftClient server) {
		//ConfigLoader.loadClientConfig(server);
	}

	@Override
	public void onInitializeClient() {
		// Setup pipe renderer
		WorldRenderEvents.AFTER_ENTITIES.register(CableRenderer::eventCallback);
		ClientLifecycleEvents.CLIENT_STARTED.register(NeetComputersClient::updateClient);

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(NeetComputersServer.GRAPHICS_SCREEN_HANDLER, RGBGraphicsScreen::new);
		HandledScreens.register(NeetComputersServer.PERIPHERAL_TOOL_SCREEN_HANDLER, PeripheralToolScreen::new);
		HandledScreens.register(NeetComputersServer.KEYBOARD_SCREEN_HANDLER, KeyboardScreen::new);
		HandledScreens.register(NeetComputersServer.DRIVE_BAY_SCREEN_HANDLER, DriveBayScreen::new);

		BlockEntityType<LargeEntityComputer> largeType = (BlockEntityType<LargeEntityComputer>) BulkRegistry.fetchBlockEntityType("large_computer");
		BulkRegistry.register(largeType, LargeComputerRenderer::new);

		BlockEntityType<DesktopEntityComputer> desktopType = (BlockEntityType<DesktopEntityComputer>) BulkRegistry.fetchBlockEntityType("desktop_computer");
		BulkRegistry.register(desktopType, DesktopComputerRenderer::new);

		BlockEntityType<OfficeEntityComputer> officeType = (BlockEntityType<OfficeEntityComputer>) BulkRegistry.fetchBlockEntityType("office_computer");
		BulkRegistry.register(officeType, OfficeComputerRenderer::new);

		BlockEntityType<RedstoneControllerBlockEntity> redstoneController = (BlockEntityType<RedstoneControllerBlockEntity>) BulkRegistry.fetchBlockEntityType("redstone_controller");
		BulkRegistry.register(redstoneController, PipeSourceBlockRenderer::new);

		BlockEntityType<DynamicLightBlockEntity> dynamicLightType = (BlockEntityType<DynamicLightBlockEntity>) BulkRegistry.fetchBlockEntityType("dynamic_light");
		BulkRegistry.register(dynamicLightType, PipeSourceBlockRenderer::new);

		BlockEntityType<DriveBayBlockEntity> driveBayType = (BlockEntityType<DriveBayBlockEntity>) BulkRegistry.fetchBlockEntityType("drive_bay");
		BulkRegistry.register(driveBayType, PipeSourceBlockRenderer::new);

		BlockEntityType<SimpleDisplayBlockEntity> simpleDisplayType = (BlockEntityType<SimpleDisplayBlockEntity>) BulkRegistry.fetchBlockEntityType("simple_display");
		BulkRegistry.register(simpleDisplayType, SimpleDisplayRenderer::new);

		BlockEntityType<ColorDisplayBlockEntity> colorDisplayType = (BlockEntityType<ColorDisplayBlockEntity>) BulkRegistry.fetchBlockEntityType("color_display");
		BulkRegistry.register(colorDisplayType, ColorDisplayRenderer::new);

		BlockEntityType<KeyboardBlockEntity> keyboardType = (BlockEntityType<KeyboardBlockEntity>) BulkRegistry.fetchBlockEntityType("keyboard");
		BulkRegistry.register(keyboardType, PipeSourceBlockRenderer::new);

//		try {
//			Class<?> reiScreenRegistryClass = Class.forName("me.shedaniel.rei.api.client.registry.screen.ScreenRegistry");
//			Object reiScreenRegistryInstance = reiScreenRegistryClass.getMethod("getInstance").invoke(null);
//
//			reiScreenRegistryClass
//				.getMethod("registerExclusionZones", Class.class, Function.class)
//				.invoke(reiScreenRegistryInstance, RGBGraphicsScreen.class, (Function<RGBGraphicsScreen, List<Rectangle>>) screen -> {
//					int x = screen.screenPos1.x;
//					int y = screen.screenPos1.y;
//					int w = screen.screenPos2.x - screen.screenPos1.x;
//					int h = screen.screenPos2.y - screen.screenPos1.y;
//					return List.of(new Rectangle(x, y, w, h));
//				});
//			reiScreenRegistryClass
//					.getMethod("registerExclusionZones", Class.class, Function.class)
//					.invoke(reiScreenRegistryInstance, KeyboardScreen.class, (Function<KeyboardScreen, List<Rectangle>>) screen -> {
//						return List.of(new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
//					});
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//			//Logger LOGGER = LoggerFactory.getLogger("NeetComputers");
//			//LOGGER.warn("REI not installed");
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}

		ClientPlayNetworking.registerGlobalReceiver(PipeBufferPayload.ID, ((payload, context) -> {
			positionsForPipeRendering = payload.buffer();
			lastTypeSent = payload.type();
		}));

		ClientPlayNetworking.registerGlobalReceiver(RGBComputerPayload.ID, (payload, context) -> {
            if (context.client().player.currentScreenHandler instanceof RGBScreenHandler) ((RGBScreenHandler) context.client().player.currentScreenHandler).updateGraphics((SectoredGraphics) payload.graphics());
		});

        ClientPlayNetworking.registerGlobalReceiver(BinaryGraphicsPayload.ID, (payload, context) -> {
			BlockPos pos = payload.blockPos();
			BinaryGraphicsArray graphics = payload.graphicsArray();
			context.client().execute(() -> {
				if (context.client().world == null) return;
				if (context.client().world.getBlockEntity(pos) == null) return;
				BlockEntity be = context.client().world.getBlockEntity(pos);
				if (be instanceof BinaryGraphicsProvider provider) {
					provider.setBinaryGraphics(graphics);
					Objects.requireNonNull(be.getWorld()).updateListeners(pos, be.getCachedState(), be.getCachedState(), 3);
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(ColorDisplayGraphicsPayload.ID, (payload, context) -> {
			BlockPos pos = payload.blockPos();
			SectoredGraphics graphics = (SectoredGraphics) payload.graphics();
			context.client().execute(() -> {
				if (context.client().world == null) return;
				if (context.client().world.getBlockEntity(pos) == null) return;
				BlockEntity be = context.client().world.getBlockEntity(pos);
				if (be instanceof ColorDisplayBlockEntity provider) {
					provider.setGraphics(graphics);
					Objects.requireNonNull(be.getWorld()).updateListeners(pos, be.getCachedState(), be.getCachedState(), 3);
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(ReturnMessagePayload.ID, (payload, context) -> {
			if (context.client().player!=null && context.client().player.currentScreenHandler instanceof PeripheralToolScreenHandler handler) handler.setReturn(payload.message(), payload.type());
		});
	}
}