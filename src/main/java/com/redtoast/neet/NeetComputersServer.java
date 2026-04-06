package com.redtoast.neet;

import com.redtoast.Computer;
import com.redtoast.Connections.PipeType;
import com.redtoast.Lua.LuaMaster;
import com.redtoast.blocks.ComputerDataComponent;
import com.redtoast.blocks.DesktopComputer.DesktopBlockComputer;
import com.redtoast.blocks.DesktopComputer.DesktopEntityComputer;
import com.redtoast.blocks.DynamicLight.DynamicLightBlock;
import com.redtoast.blocks.DynamicLight.DynamicLightBlockEntity;
import com.redtoast.blocks.Generics.PeripheralBlockEntity;
import com.redtoast.blocks.LargeComputer.LargeBlockComputer;
import com.redtoast.blocks.LargeComputer.LargeEntityComputer;
import com.redtoast.blocks.OfficeComputer.OfficeBlockComputer;
import com.redtoast.blocks.OfficeComputer.OfficeEntityComputer;
import com.redtoast.blocks.Generics.ComputerBlock;
import com.redtoast.blocks.RedstoneController.RedstoneControllerBlock;
import com.redtoast.blocks.RedstoneController.RedstoneControllerBlockEntity;
import com.redtoast.blocks.SimpleDisplay.SimpleDisplayBlock;
import com.redtoast.blocks.SimpleDisplay.SimpleDisplayBlockEntity;
import com.redtoast.graphics.screens.PeripheralToolScreenHandler;
import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.items.PeripheralTool;
import com.redtoast.items.generics.DisplayPipes;
import com.redtoast.items.peripheralCable;
import com.redtoast.APIS.*;
import com.redtoast.Connections.CableManager;
import com.redtoast.neet.Networking.*;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.APIRegistry;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.base.LanguageGeneric;
import com.redtoast.simulation.events.EventLabel;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Lua;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class NeetComputersServer implements ModInitializer {

	//create packet id's and screen handler
	private static final ExtendedScreenHandlerType<RGBScreenHandler, ComputerScreenInitPayload> HANDLER = new ExtendedScreenHandlerType<>(RGBScreenHandler::new, ComputerScreenInitPayload.CODEC);
	private static final ExtendedScreenHandlerType<PeripheralToolScreenHandler, PeripheralToolScreenInitPayload> HANDLER2 = new ExtendedScreenHandlerType<>(PeripheralToolScreenHandler::new, PeripheralToolScreenInitPayload.CODEC);
	public static final ScreenHandlerType<RGBScreenHandler> GRAPHICS_SCREEN_HANDLER = BulkRegistry.register("graphics", Registries.SCREEN_HANDLER, HANDLER);
	public static final ScreenHandlerType<PeripheralToolScreenHandler> PERIPHERAL_TOOL_SCREEN_HANDLER = BulkRegistry.register("peripheral_tool", Registries.SCREEN_HANDLER, HANDLER2);
	public static CableManager cableManager = null;
	private static MinecraftServer server = null;

	//internal config
	public static String version = "NeetComputers ";

	//important resources
	public static final Logger LOGGER = LoggerFactory.getLogger("NeetComputers");
	public static final Hashtable<UUID, Computer> computerMap = new Hashtable<>();
	public static final ConcurrentLinkedQueue<PeripheralBlockEntity> peripheralUpdateQueue = new ConcurrentLinkedQueue<>();
	public static ResourceManager datahandling;
	public static Path worldPath = null;
	public static Long timeBenchMark = null;

	//internal language processing
	private static boolean LangsLoaded = false;
	protected static LanguageGeneric[] LanguageCache;
	private static LanguageTranslater[] translators;
	private final static LinkedList<LanguageGeneric> languageGenerics = new LinkedList<>();

	// https://wiki.fabricmc.net/tutorial:blocks#registering_blocks_in_1212
	private static Block registerBlock(String path, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
		final Identifier identifier = Identifier.of("neetcomputers", path);
		final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

		final Block block = Blocks.register(registryKey, factory, settings);
		return block;
	}
	// https://wiki.fabricmc.net/tutorial:blockentity
	public static <T extends BlockEntityType<?>> T registerBlockEntity(String path, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("neetcomputers", path), blockEntityType);
	}
	// https://wiki.fabricmc.net/tutorial:items#creating_items_in_1212
	public static Item registerItem(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
		final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("neetcomputers", path));
		return Items.register(registryKey, factory, settings);
	}

	public static BlockSoundGroup computerSound = new BlockSoundGroup(
			1.0F,
			1.0F,
			SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK,
			SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK,
			SoundEvents.BLOCK_COPPER_BULB_PLACE,
			SoundEvents.BLOCK_COPPER_BULB_HIT,
			SoundEvents.BLOCK_ANVIL_FALL
	);

	public static Block largeComputer = registerBlock("large_computer",LargeBlockComputer::new,Block.Settings.create().strength(3.0f).hardness(2.0f).sounds(computerSound).luminance(state -> state.get(ComputerBlock.STATE)!=0 && emitLight() ? 8 : 0));
	public static BlockEntityType<LargeEntityComputer> largeComputerType = registerBlockEntity("large_computer", FabricBlockEntityTypeBuilder.create(LargeEntityComputer::new,largeComputer).build());
	public static Item largeComputerItem = Items.register(largeComputer);

	public static Block desktopComputer = registerBlock("desktop_computer",DesktopBlockComputer::new,Block.Settings.create().strength(2.0f).hardness(1.5f).sounds(computerSound).nonOpaque().luminance(state -> state.get(ComputerBlock.STATE)!=0 && emitLight() ? 5 : 0));
	public static BlockEntityType<DesktopEntityComputer> desktopComputerType = registerBlockEntity("desktop_computer", FabricBlockEntityTypeBuilder.create(DesktopEntityComputer::new,desktopComputer).build());
	public static Item desktopComputerItem = Items.register(desktopComputer);

	public static Block officeComputer = registerBlock("office_computer",OfficeBlockComputer::new,Block.Settings.create().strength(2.0f).hardness(1.5f).sounds(computerSound).nonOpaque().luminance(state -> state.get(ComputerBlock.STATE)!=0 && emitLight() ? 5 : 0));
	public static BlockEntityType<OfficeEntityComputer> officeComputerType = registerBlockEntity("office_computer", FabricBlockEntityTypeBuilder.create(OfficeEntityComputer::new,officeComputer).build());
	public static Item officeComputerItem = Items.register(officeComputer);

	public static Block redstoneController = registerBlock("redstone_controller",RedstoneControllerBlock::new,Block.Settings.create().strength(3.0f).hardness(2f).sounds(BlockSoundGroup.METAL));
	public static BlockEntityType<RedstoneControllerBlockEntity> redstoneControllerType = registerBlockEntity("redstone_controller", FabricBlockEntityTypeBuilder.create(RedstoneControllerBlockEntity::new,redstoneController).build());
	public static Item redstoneControllerItem = Items.register(redstoneController);

	public static Block dynamicLight = registerBlock("dynamic_light",DynamicLightBlock::new,Block.Settings.create().strength(1.0f).hardness(0.1f).sounds(BlockSoundGroup.GLASS).luminance(state -> state.get(DynamicLightBlock.LUMINANCE)));
	public static BlockEntityType<DynamicLightBlockEntity> dynamicLightType = registerBlockEntity("dynamic_light", FabricBlockEntityTypeBuilder.create(DynamicLightBlockEntity::new,dynamicLight).build());
	public static Item dynamicLightItem = Items.register(dynamicLight);

	public static Block simpleDisplay = registerBlock("simple_display",SimpleDisplayBlock::new,Block.Settings.create().strength(1.0f).hardness(0.1f).sounds(computerSound).luminance(state -> emitLight() ? 7 : 0));
	public static BlockEntityType<SimpleDisplayBlockEntity> simpleDisplayType = registerBlockEntity("simple_display", FabricBlockEntityTypeBuilder.create(SimpleDisplayBlockEntity::new,simpleDisplay).build());
	public static Item simpleDisplayItem = Items.register(simpleDisplay);

	public static Item peripheralTool = registerItem("peripheral_tool",PeripheralTool::new,new Item.Settings().maxCount(1));
	public static Item peripheralCableItem = registerItem("peripheral_cable",peripheralCable::new,new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {
		peripheralUpdateQueue.clear();
		BuildData.updateDat();
		version += BuildData.VERSION;

		LOGGER.info(version+" running using "+ Lua._VERSION);
		LOGGER.info("mod build from "+BuildData.BUILD_TIME);

		ServerLifecycleEvents.SERVER_STARTING.register(NeetComputersServer::updateServer);
		ServerLifecycleEvents.SERVER_STARTED.register(server1 -> updateClientPipes());
		ServerTickEvents.START_SERVER_TICK.register(Identifier.of("neetcomputers:tick"), server -> {
			if (timeBenchMark!=null && timeBenchMark + 1000 < System.currentTimeMillis()) {
				updateClientPipes();
				timeBenchMark = System.currentTimeMillis();
			}
			while (!peripheralUpdateQueue.isEmpty()){
				peripheralUpdateQueue.poll().processEventQueue();
			}
		});
		ServerLifecycleEvents.AFTER_SAVE.register((server,a,b) -> {
			File file = worldPath.resolve("neet_data.bin").toFile();
			if (cableManager!=null){
                try {
					file.delete();
					file.createNewFile();
                    NbtIo.write(cableManager.writeNbt(new NbtCompound()), file.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
			}
		});

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

		//register stuff
		ComputerDataComponent.TYPE = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, Identifier.of("neetcomputers", "computerdata")),
				ComponentType.<ComputerDataComponent>builder().codec(ComputerDataComponent.CODEC).build()
		);

		BulkRegistry.setNamespace("neetcomputers");

		RegistryKey<ItemGroup> group = BulkRegistry.registerGroup("main_item_group", largeComputerItem);
		BulkRegistry.register(largeComputerItem, group);
		BulkRegistry.register(desktopComputerItem, group);
		BulkRegistry.register(officeComputerItem, group);
		BulkRegistry.register(redstoneControllerItem, group);
		BulkRegistry.register(dynamicLightItem, group);
		BulkRegistry.register(simpleDisplayItem, group);

		BulkRegistry.register(peripheralTool, group);
		BulkRegistry.register(peripheralCableItem, group);

//		Item networkingCableItem = new networkingCable(new Item.Settings().maxCount(1));
//		BulkRegistery.register("networking_cable", networkingCableItem);
//		BulkRegistery.register(networkingCableItem, group);

		PayloadTypeRegistry.playC2S().register(EventUploadPayload.ID, EventUploadPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetPeripheralTagPayload.ID, SetPeripheralTagPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SubmitCommandPayload.ID, SubmitCommandPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ReturnMessagePayload.ID, ReturnMessagePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BinaryGraphicsPayload.ID, BinaryGraphicsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(RGBComputerPayload.ID, RGBComputerPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(PipeBufferPayload.ID, PipeBufferPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(EventUploadPayload.ID, (payload, context) -> {
			if ((context.player().currentScreenHandler!=null && context.player().currentScreenHandler.syncId == payload.syncId() && context.player().currentScreenHandler instanceof RGBScreenHandler handler)){
				Computer computer = handler.comp;
				computer.queueEvent(payload.event(), EventLabel.USER);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(SetPeripheralTagPayload.ID, (payload, context) -> {
			if ((context.player().currentScreenHandler!=null && context.player().currentScreenHandler.syncId == payload.syncId() && context.player().currentScreenHandler instanceof PeripheralToolScreenHandler handler)){
				handler.setTag(payload.tag());
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(SubmitCommandPayload.ID, (payload, context) -> {
			if ((context.player().currentScreenHandler!=null && context.player().currentScreenHandler.syncId == payload.syncId() && context.player().currentScreenHandler instanceof PeripheralToolScreenHandler handler)){
				String[] parts = payload.command().split("(?!\\B\\\"[^\\\"]*)[, \\.\\(\\)](?![^\\\"]*\\\"\\B)");
				try{
					String functionname = "";
					ArrayList<Value<?>> parameters = new ArrayList<>();
					for (String part : parts){
						if (!part.isBlank()){
							if (functionname.isBlank()){
								if (part.matches("^[a-zA-Z]*\\z")){
									functionname = part;
								}else{
									throw new NumberFormatException();
								}
							}else if (part.equals("true")){
								parameters.add(Value.TRUE);
							}else if(part.equals("false")){
								parameters.add(Value.FALSE);
							}else if(part.matches("^\\\"[^\\\"]*\\\"\\z")){
								parameters.add(Value.of(part.substring(1, part.length()-1)));
							}else{
								parameters.add(Value.of(Integer.parseInt(part)));
							}
						}
                    }
					if (functionname.isBlank()) throw new NumberFormatException();
					Value<?> done = handler.call(functionname, parameters);
					ServerPlayNetworking.send(context.player(), new ReturnMessagePayload(done.isNull() ? "No result" : done.getValue().toString(), done.getType()));
				}catch (NumberFormatException e){
					ServerPlayNetworking.send(context.player(), new ReturnMessagePayload("Invalid Command", VarType.EXCEPTION));
				}
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
			public @NotNull API Create(Computer computer) {return new EventAPI(computer);}
		});
		APILoader.register(new APIRegistry() {
			@Override
			public @NotNull API Create(Computer computer) {
				return new Flash(computer);
			}
		});
    }

	public static boolean emitLight(){
		return true;//(boolean) ConfigLoader.getClientConfig("computers-emit-light");
	}

	public static void updateClientPipes(){
		if (server==null) return;
		PlayerManager playerManager = server.getPlayerManager();
		for (String name : server.getPlayerNames()){
			ServerPlayerEntity player = playerManager.getPlayer(name);
			boolean isHoldingConnector = false;
			PipeType type = null;
            assert player != null;
            if (player.getOffHandStack().getItem() instanceof DisplayPipes connectorItem){
				isHoldingConnector = true;
				type = connectorItem.getType();
			}else if (player.getMainHandStack().getItem() instanceof DisplayPipes connectorItem){
				isHoldingConnector = true;
				type = connectorItem.getType();
			}
			if (isHoldingConnector){
				sendPipeBufferToPlayer(player, type);
			}
		}
	}

	public static void sendPipeBufferToPlayer(ServerPlayerEntity player, PipeType type){
		CableManager cableManager = CableManager.getInstance();
		if (cableManager==null) return;
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		Position pos = new Position() {
			@Override
			public double getX() {
				return x;
			}

			@Override
			public double getY() {
				return y;
			}

			@Override
			public double getZ() {
				return z;
			}
		};
		BlockPos[] buffer = cableManager.getPipesForRendering(player.getWorld().getDimension(), type, (blockpos) -> BlockPos.fromLong(blockpos).isWithinDistance(pos, 40));
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new PipeBufferPayload(type, buffer));
		player.networkHandler.sendPacket(packet);
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
		NeetComputersServer.server = server;
		timeBenchMark = System.currentTimeMillis();
		worldPath = server.getSavePath(WorldSavePath.ROOT);
		ConfigLoader.loadServerConfig(server);
		if (!worldPath.resolve("neetcomputers").toFile().exists()){
			LOGGER.info("Generating neetcomputers world directory");
			worldPath.resolve("neetcomputers").toFile().mkdir();
		}

		File file = worldPath.resolve("neet_data.bin").toFile();
		if (file.exists() && !file.isDirectory()){
			try{
				cableManager = CableManager.createFromNbt(NbtIo.read(file.toPath()));
			} catch (IOException e) {
                cableManager = new CableManager();
            } catch (Throwable error) {
				throw new RuntimeException(error);
			}
        }else{
			cableManager = new CableManager();
		}

		//process lang translaters
		if (!LangsLoaded){
			LangsLoaded = true;
			LanguageCache = new LanguageGeneric[languageGenerics.size()];
			translators = new LanguageTranslater[languageGenerics.size()];
			for (int i = 0; i < languageGenerics.size(); i++){
				LanguageCache[i] = languageGenerics.get(i);
				translators[i] = languageGenerics.get(i).generateTranslationClass();
			}
		}

		ProcessManager.clear();
		for (int i = 0; i < (int) ConfigLoader.getServerConfig("processing-threads"); i++) ProcessManager.openNewThread();
	}

	public static LanguageTranslater getTranslater(String lang){
		for (int i = 0; i < LanguageCache.length; i++){
			if (LanguageCache[i].getVersion().equals(lang)){
				return translators[i];
			}
		}
		return null;
	}

	public static LanguageTranslater[] getTranslaters(){
		return translators;
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