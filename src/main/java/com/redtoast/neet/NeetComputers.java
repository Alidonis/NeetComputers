package com.redtoast.neet;

import com.redtoast.blocks.LargeBlockComputer;
import com.redtoast.blocks.LargeEntityComputer;
import com.redtoast.graphics.GraphicsScreenHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NeetComputers implements ModInitializer {
	public static final ScreenHandlerType<GraphicsScreenHandler> GRAPHICS_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of("neetcomputers", "graphical"), new ExtendedScreenHandlerType<>(GraphicsScreenHandler::new));
	public static final Identifier SCREEN_PACKET_ID = Identifier.of("neetcomputers", "graphics_update");
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
		BlockRegistery.setNamespace("neetcomputers");
		Block largeComputer = new LargeBlockComputer(Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(BlockSoundGroup.METAL));
		BlockRegistery.register("large_computer",largeComputer, LargeEntityComputer::new,true);
	}

	public static void updateServer(MinecraftServer server) {
		worldPath = server.getSavePath(WorldSavePath.ROOT);
		if (!NeetComputers.worldPath.resolve("neetcomputers").toFile().exists()){
			LOGGER.info("Generating neetcomputers world directory");
			NeetComputers.worldPath.resolve("neetcomputers").toFile().mkdir();
		}
	}
}