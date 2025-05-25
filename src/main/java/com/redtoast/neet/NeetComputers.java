package com.redtoast.neet;

import com.redtoast.Computer;
import com.redtoast.blocks.LargeBlockComputer;
import com.redtoast.blocks.LargeEntityComputer;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.blocks.LargeComputerRenderer;
import com.redtoast.items.networkingCable;
import com.redtoast.items.peripheralCable;
import com.redtoast.lua.events.MouseMoveEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NeetComputers implements ModInitializer {
	public static final ScreenHandlerType<GraphicsScreenHandler> GRAPHICS_SCREEN_HANDLER = BulkRegistery.register("graphics", Registries.SCREEN_HANDLER, new ExtendedScreenHandlerType<>(GraphicsScreenHandler::new));
	public static final Identifier SCREEN_PACKET_ID = Identifier.of("neetcomputers", "graphics_update");
	public static final Identifier MOUSE_MOVE_PACKET_ID = Identifier.of("neetcomputers","mouse_packet");
	public static final Identifier BINARY_SCREEN_PACKET = Identifier.of("neetcomputers", "bianary_update");
	public static final Logger LOGGER = LoggerFactory.getLogger("NeetComputers");
	public static ResourceManager datahandling;
	public static Path worldPath;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(NeetComputers::updateServer);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("neetcomputers", "");
			}

			@Override
			public void reload(ResourceManager manager) {
				datahandling = manager;
			}
		});

		LOGGER.info("Hello From Neet Computers!");

		//register stuff
		BulkRegistery.setNamespace("neetcomputers");
		Block largeComputer = new LargeBlockComputer(Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(BlockSoundGroup.METAL).luminance(state -> state.get(LargeBlockComputer.ON) ? 8 : 0));
		BulkRegistery.register("large_computer",largeComputer, LargeEntityComputer::new,LargeComputerRenderer::new,true);
		RegistryKey<ItemGroup> group = BulkRegistery.registerGroup("main_item_group", BulkRegistery.fetchItemObject("large_computer"));
		BulkRegistery.register(BulkRegistery.fetchItemObject("large_computer"), group);

		Item peripheralCableItem = new peripheralCable(new FabricItemSettings().maxCount(16));
		BulkRegistery.register("peripheral_cable", peripheralCableItem);
		BulkRegistery.register(peripheralCableItem, group);

		Item networkingCableItem = new networkingCable(new FabricItemSettings().maxCount(16));
		BulkRegistery.register("networking_cable", networkingCableItem);
		BulkRegistery.register(networkingCableItem, group);

		ServerPlayNetworking.registerGlobalReceiver(MOUSE_MOVE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			if (player.currentScreenHandler instanceof GraphicsScreenHandler) {
				Computer c = ((GraphicsScreenHandler) player.currentScreenHandler).comp.computer;
				int mouseX = buf.readInt();
				int mouseY = buf.readInt();
				c.mousePos = new Vector2i(mouseX,mouseY);
				c.queueEvent(new MouseMoveEvent(mouseX,mouseY));
			}
		});
	}

	public static void updateServer(MinecraftServer server) {
		worldPath = server.getSavePath(WorldSavePath.ROOT);
		if (!NeetComputers.worldPath.resolve("neetcomputers").toFile().exists()){
			LOGGER.info("Generating neetcomputers world directory");
			NeetComputers.worldPath.resolve("neetcomputers").toFile().mkdir();
		}
	}
}