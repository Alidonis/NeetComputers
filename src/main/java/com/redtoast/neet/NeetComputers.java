package com.redtoast.neet;

import com.redtoast.Computer;
import com.redtoast.Lua.LuaMaster;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.DesktopComputer.DesktopComputerRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopEntityComputer;
import com.redtoast.blocks.LargeComputer.LargeBlockComputer;
import com.redtoast.blocks.LargeComputer.LargeEntityComputer;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.blocks.LargeComputer.LargeComputerRenderer;
import com.redtoast.items.mobileComputer;
import com.redtoast.items.networkingCable;
import com.redtoast.items.peripheralCable;
import com.redtoast.APIS.*;
import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.APIRegistry;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.base.LanguageGeneric;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class NeetComputers implements ModInitializer {

	//create packet id's and screen handler
	public static final ScreenHandlerType<GraphicsScreenHandler> GRAPHICS_SCREEN_HANDLER = BulkRegistery.register("graphics", Registries.SCREEN_HANDLER, new ExtendedScreenHandlerType<>(GraphicsScreenHandler::new));
	public static final Identifier SCREEN_PACKET_ID = Identifier.of("neetcomputers", "graphics_update");
	public static final Identifier EVENT_PACKET = Identifier.of("neetcomputers","event");
	public static final Identifier BINARY_SCREEN_PACKET = Identifier.of("neetcomputers", "bianary_update");

	//internal config
	public static final String version = "NeetComputers 0.1 beta";

	//important resources
	public static final Logger LOGGER = LoggerFactory.getLogger("NeetComputers");
	public static final Hashtable<UUID, Computer> computerMap = new Hashtable<>();
	public static ResourceManager datahandling;
	public static Path worldPath;

	//internal language processing
	private static boolean LangsLoaded = false;
	protected static LanguageGeneric[] LanguageCache;
	private static LanguageTranslater[] translaters;
	private final static LinkedList<LanguageGeneric> languageGenerics = new LinkedList<>();

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
		Block largeComputer = new LargeBlockComputer(Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(BlockSoundGroup.BONE).luminance(state -> state.get(LargeBlockComputer.ON) ? 8 : 0));
		BulkRegistery.register("large_computer",largeComputer, LargeEntityComputer::new,LargeComputerRenderer::new,true);
		RegistryKey<ItemGroup> group = BulkRegistery.registerGroup("main_item_group", BulkRegistery.fetchItemObject("large_computer"));
		BulkRegistery.register(BulkRegistery.fetchItemObject("large_computer"), group);

		Block desktopComputer = new DesktopBlockComputer(Block.Settings.create().strength(1.0f).hardness(1.0f).sounds(BlockSoundGroup.BONE).nonOpaque().luminance(state -> state.get(LargeBlockComputer.ON) ? 4 : 0));
		BulkRegistery.register("desktop_computer",desktopComputer, DesktopEntityComputer::new, DesktopComputerRenderer::new,true);
		BulkRegistery.register(BulkRegistery.fetchItemObject("desktop_computer"), group);

		Item modelComputer = new mobileComputer(new FabricItemSettings().maxCount(16));
		BulkRegistery.register("mobile_computer", modelComputer);
		BulkRegistery.register(modelComputer, group);

		Item peripheralCableItem = new peripheralCable(new FabricItemSettings().maxCount(16));
		BulkRegistery.register("peripheral_cable", peripheralCableItem);
		BulkRegistery.register(peripheralCableItem, group);

		Item networkingCableItem = new networkingCable(new FabricItemSettings().maxCount(16));
		BulkRegistery.register("networking_cable", networkingCableItem);
		BulkRegistery.register(networkingCableItem, group);

		ServerPlayNetworking.registerGlobalReceiver(EVENT_PACKET, (server, player, handler, buf, responseSender) -> {
			UUID uuid = buf.readUuid();
			EventGeneric event = EventGeneric.fromPacket(buf);
			computerMap.get(uuid).queueEvent(event);
		});

		registerLanguage(new LuaMaster());

		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer _computer) {
				return new GraphicsAPI(_computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer _computer) {
				return new ChipAPI(_computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer _computer) {
				return new PeripheralsAPI(_computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer _computer) {
				return new PaintAPI(_computer);
			}
		});
	}

	public void registerLanguage(LanguageGeneric language){
		for (LanguageGeneric lang : languageGenerics){
			if (lang.getVersion().equals(language.getVersion())){
				return;
			}
		}
		languageGenerics.add(language);
	}

	public static void updateServer(MinecraftServer server) {
		worldPath = server.getSavePath(WorldSavePath.ROOT);
		if (!worldPath.resolve("neetcomputers").toFile().exists()){
			LOGGER.info("Generating neetcomputers world directory");
			worldPath.resolve("neetcomputers").toFile().mkdir();
		}
		//process lang translaters
		if (!LangsLoaded){
			LangsLoaded = true;
			LanguageCache = new LanguageGeneric[languageGenerics.size()];
			translaters = new LanguageTranslater[languageGenerics.size()];
			for (int i = 0; i < languageGenerics.size(); i++){
				LanguageCache[i] = languageGenerics.get(i);
				translaters[i] = languageGenerics.get(i).generateTranslationClass();
			}
		}
	}

	public static LanguageTranslater getTranslater(String lang){
		for (int i = 0; i < LanguageCache.length; i++){
			if (LanguageCache[i].getVersion().equals(lang)){
				return translaters[i];
			}
		}
		return null;
	}

	public static LanguageTranslater[] getTranslaters(){
		return translaters;
	}

	public static String[] getLangs(){
		String[] output = new String[LanguageCache.length];
		for (int i = 0; i < LanguageCache.length; i++){
			output[i] = LanguageCache[i].getVersion();
		}
		return output;
	}

	public static LanguageGeneric getLanguage(String lang){
		for (int i = 0; i < LanguageCache.length; i++){
			if (LanguageCache[i].getVersion().equals(lang)){
				return LanguageCache[i];
			}
		}
		return null;
	}

	public static boolean hasLanguage(String lang){
		for (int i = 0; i < LanguageCache.length; i++){
			if (LanguageCache[i].getVersion().equals(lang)){
				return true;
			}
		}
		return false;
	}
}