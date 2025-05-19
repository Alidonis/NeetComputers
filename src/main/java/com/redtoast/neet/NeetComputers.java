package com.redtoast.neet;

import com.redtoast.blocks.SolidBlockComputer;
import com.redtoast.blocks.SolidBlockEntityComputer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NeetComputers implements ModInitializer {
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
		Block largeComputer = new SolidBlockComputer(Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(BlockSoundGroup.METAL));
		BlockRegistery.register("large_computer",largeComputer, SolidBlockEntityComputer::new,true);
	}

	public static void updateServer(MinecraftServer server) {
		worldPath = server.getSavePath(WorldSavePath.ROOT);
		if (!NeetComputers.worldPath.resolve("neetcomputers").toFile().exists()){
			LOGGER.info("Generating neetcomputers world directory");
			NeetComputers.worldPath.resolve("neetcomputers").toFile().mkdir();
		}
	}
}