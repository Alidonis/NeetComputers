package com.redtoast.neet;

import com.redtoast.Computer;
import com.redtoast.Lua.LuaMaster;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.DesktopComputer.DesktopComputerRenderer;
import com.redtoast.blocks.DesktopComputer.DesktopEntityComputer;
import com.redtoast.blocks.DynamicLight.DynamicLightBlock;
import com.redtoast.blocks.DynamicLight.DynamicLightBlockEntity;
import com.redtoast.blocks.LargeComputer.LargeBlockComputer;
import com.redtoast.blocks.LargeComputer.LargeEntityComputer;
import com.redtoast.blocks.OfficeComputer.OfficeBlockComputer;
import com.redtoast.blocks.OfficeComputer.OfficeComputerRenderer;
import com.redtoast.blocks.OfficeComputer.OfficeEntityComputer;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.blocks.LargeComputer.LargeComputerRenderer;
import com.redtoast.items.networkingCable;
import com.redtoast.items.peripheralCable;
import com.redtoast.APIS.*;
import com.redtoast.Connections.CableManager;
import com.redtoast.neet.Networking.*;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.APIRegistry;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.base.LanguageGeneric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
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
	private static final ExtendedScreenHandlerType<RGBScreenHandler, ComputerScreenInitPayload> HANDLER = new ExtendedScreenHandlerType<>(RGBScreenHandler::new, ComputerScreenInitPayload.CODEC);
	public static final ScreenHandlerType<RGBScreenHandler> GRAPHICS_SCREEN_HANDLER = BulkRegistery.register("graphics", Registries.SCREEN_HANDLER, HANDLER);
	public static CableManager cableManager = null;

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
		ServerLifecycleEvents.SERVER_STARTING.register(NeetComputers::updateServer);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> cableManager = CableManager.getServerState(server));

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of("neetcomputers", "");
			}

			@Override
			public void reload(ResourceManager manager) {
				datahandling = manager;
			}
		});

		LOGGER.info("Hello From Neet Computers!");

		//register stuff

		BlockSoundGroup computerSound = new BlockSoundGroup(
				1.0F,
				1.0F,
				SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK,
				SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK,
				SoundEvents.BLOCK_COPPER_BULB_PLACE,
				SoundEvents.BLOCK_COPPER_BULB_HIT,
				SoundEvents.BLOCK_ANVIL_FALL
		);

		ComputerDataComponent.TYPE = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, Identifier.of("neetcomputers", "computerdata")),
				ComponentType.<ComputerDataComponent>builder().codec(ComputerDataComponent.CODEC).build()
		);

		BulkRegistery.setNamespace("neetcomputers");
		Block largeComputer = new LargeBlockComputer(Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(computerSound).luminance(state -> state.get(LargeBlockComputer.ON) ? 8 : 0));
		BulkRegistery.register("large_computer",largeComputer, LargeEntityComputer::new,LargeComputerRenderer::new,true);
		RegistryKey<ItemGroup> group = BulkRegistery.registerGroup("main_item_group", BulkRegistery.fetchItemObject("large_computer"));
		BulkRegistery.register(BulkRegistery.fetchItemObject("large_computer"), group);

		Block desktopComputer = new DesktopBlockComputer(Block.Settings.create().strength(2.0f).hardness(1.5f).sounds(computerSound).nonOpaque().luminance(state -> state.get(DesktopBlockComputer.ON) ? 4 : 0));
		BulkRegistery.register("desktop_computer",desktopComputer, DesktopEntityComputer::new, DesktopComputerRenderer::new,true);
		BulkRegistery.register(BulkRegistery.fetchItemObject("desktop_computer"), group);

		Block officeComputer = new OfficeBlockComputer(Block.Settings.create().strength(2.0f).hardness(1.5f).sounds(computerSound).nonOpaque().luminance(state -> state.get(DesktopBlockComputer.ON) ? 5 : 0));
		BulkRegistery.register("office_computer",officeComputer, OfficeEntityComputer::new, OfficeComputerRenderer::new,true);
		BulkRegistery.register(BulkRegistery.fetchItemObject("office_computer"), group);

		Block dynamicLight = new DynamicLightBlock(Block.Settings.create().strength(1.0f).hardness(0.1f).sounds(BlockSoundGroup.GLASS).luminance(state -> state.get(DynamicLightBlock.LUMINANCE)));
		BulkRegistery.register("dynamic_light",dynamicLight, DynamicLightBlockEntity::new,true);
		BulkRegistery.register(BulkRegistery.fetchItemObject("dynamic_light"), group);

		Item peripheralCableItem = new peripheralCable(new Item.Settings().maxCount(1));
		BulkRegistery.register("peripheral_cable", peripheralCableItem);
		BulkRegistery.register(peripheralCableItem, group);

		Item networkingCableItem = new networkingCable(new Item.Settings().maxCount(1));
		BulkRegistery.register("networking_cable", networkingCableItem);
		BulkRegistery.register(networkingCableItem, group);

		PayloadTypeRegistry.playC2S().register(EventTransferPayload.ID, EventTransferPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(EventUploadPayload.ID, EventUploadPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BinaryGraphicsPayload.ID, BinaryGraphicsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(RGBComputerPayload.ID, RGBComputerPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(EventTransferPayload.ID, (payload, context) -> computerMap.get(payload.uuid()).queueEvent(payload.event()));
		ServerPlayNetworking.registerGlobalReceiver(EventUploadPayload.ID, (payload, context) -> {
			if ((context.player().currentScreenHandler!=null && context.player().currentScreenHandler.syncId == payload.syncId() && context.player().currentScreenHandler instanceof RGBScreenHandler handler)){
				Computer computer = handler.comp;
				computer.queueEvent(payload.event());
			}
		});

        registerLanguage(new LuaMaster());

		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new ChipAPI(computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new IOAPI(computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new ScreenAPI(computer.getGraphics(), computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new EventAPI(computer);
			}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new Flash(computer);
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