package com.redtoast.neet;

import com.mojang.serialization.Codec;
import com.redtoast.blocks.ComputerDataComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.component.ComponentType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.function.Function;

public class BulkRegistery {
    private static class Registered{
        public Block block;
        public BlockEntityType<?> blockEntity;
        public Item item;
        public Registered(Block b, BlockItem bi, BlockEntityType<?> be){
            block = b;
            item = bi;
            blockEntity = be;
        }
        public Registered(Block b, BlockEntityType<?> be){
            block = b;
            blockEntity = be;
        }
        public Registered(Block b, BlockItem bi){
            block = b;
            item = bi;
        }
        public Registered(Block b){
            block = b;
        }
        public Registered(Item i){item = i;}
    }
    private static final LinkedList<String> keys = new LinkedList<>();
    private static final LinkedList<Registered> value = new LinkedList<>();
    private static String Namespace = "minecraft";

    public static void setNamespace(String namespace){
        Namespace = namespace;
    }
    public static String getNamespace(){
        return Namespace;
    }

    public static boolean find(String index){
        for (int i = 0; i < value.size(); i++){
            if (keys.get(i).equals(index)){
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Registered get(String index){
        for (int i = 0; i < value.size(); i++){
            if (keys.get(i).equals(index)){
                return value.get(i);
            }
        }
        return null;
    }

    @Nullable
    public static BlockEntityType<?> fetchBlockEntityType(String address){
        Registered result = get(address);
        if (result==null){
            return null;
        }
        return result.blockEntity;
    }
    @Nullable
    public static Item fetchItemObject(String address){
        Registered result = get(address);
        if (result==null){
            return null;
        }
        return result.item;
    }
    @Nullable
    public static Block fetchBlockObject(String address){
        Registered result = get(address);
        if (result==null){
            return null;
        }
        return result.block;
    }

    private static void add(String address, Registered registered){
        keys.add(address);
        value.add(registered);
    }

    public static <BlockClass extends Block, BlockEntityClass extends BlockEntity> void register(String address, BlockClass block, BlockEntityConstructor<BlockEntityClass> constructor, boolean AutoRegisterItem){
        Registry.register(Registries.BLOCK, Identifier.tryParse(Namespace, address), block);
        BlockItem blockItem = null;
        if (AutoRegisterItem){
            blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.tryParse(Namespace, address), blockItem);
        }
        BlockEntityType<?> blockEntity = FabricBlockEntityTypeBuilder.create((FabricBlockEntityTypeBuilder.Factory<BlockEntity>) constructor::create,block).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.tryParse(Namespace, address+"_entity"), blockEntity);
        if (AutoRegisterItem){
            add(address,new Registered(block, blockItem, blockEntity));
        }else{
            add(address,new Registered(block, blockEntity));
        }
    }
    public static <BlockClass extends Block, BlockEntityClass extends BlockEntity> void register(String address, BlockClass block, BlockEntityConstructor<BlockEntityClass> constructor, RendererConstructor<BlockEntityClass> renderer, boolean AutoRegisterItem){
        Registry.register(Registries.BLOCK, Identifier.tryParse(Namespace, address), block);
        BlockItem blockItem = null;
        if (AutoRegisterItem){
            blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.tryParse(Namespace, address), blockItem);
        }
        BlockEntityType<BlockEntityClass> blockEntity = FabricBlockEntityTypeBuilder.create(constructor::create,block).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.tryParse(Namespace, address+"_entity"), blockEntity);
        if (AutoRegisterItem){
            add(address,new Registered(block, blockItem, blockEntity));
        }else{
            add(address,new Registered(block, blockEntity));
        }
        BlockEntityRendererFactories.register(blockEntity, renderer::create);
    }
    public static <BlockClass extends Block, BlockEntityClass extends BlockEntity> void register(String address, BlockClass block, BlockEntityConstructor<BlockEntityClass> constructor, RendererConstructor<BlockEntityClass> renderer){
        register(address,block,constructor,renderer,false);
    }
    public static <BlockClass extends Block, BlockEntityClass extends BlockEntity> void register(String address, BlockClass block, BlockEntityConstructor<BlockEntityClass> constructor){
        register(address,block,constructor,false);
    }
    public static <BlockClass extends Block> void register(String address, BlockClass block, boolean AutoRegisterItem){
        Registry.register(Registries.BLOCK, Identifier.tryParse(Namespace, address), block);
        if (AutoRegisterItem){
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.tryParse(Namespace, address), blockItem);
            add(address, new Registered(block, blockItem));
        }else{
            add(address, new Registered(block));
        }
    }
    public static <BlockClass extends Block> void register(String address, BlockClass block){
        register(address,block,false);
    }

    public static <BaseHandler, CustomHandler extends BaseHandler> CustomHandler register(String address, @NotNull Registry<BaseHandler> base, CustomHandler custom ){
        return Registry.register(base, Identifier.tryParse(Namespace, address), custom);
    }

    public static <ItemClass extends Item> void register(String address, ItemClass item){
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.tryParse(Namespace, address));
        Registry.register(Registries.ITEM, itemKey, item);
        add(address, new Registered(item));
    }

    public static RegistryKey<ItemGroup> registerGroup(String address, Item item){
        Identifier id = Identifier.of(Namespace, address);
        RegistryKey<ItemGroup> groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, id);

        ItemGroup group = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup."+Namespace+'.' + address))
            .icon(() -> new ItemStack(item))
            .entries((enabledFeatures, entries) -> {
                entries.add(item);
            })
            .build();

        Registry.register(Registries.ITEM_GROUP, groupKey, group);

        return groupKey;
    }

    public static <ItemClass extends Item> void register(ItemClass item, RegistryKey<ItemGroup> group){
        ItemGroupEvents.modifyEntriesEvent(group).register((itemGroup) -> itemGroup.add(item));
    }

    @FunctionalInterface
    public interface BlockEntityConstructor<BlockEntityClass extends BlockEntity> {
        BlockEntityClass create(BlockPos blockPos, BlockState blockState);
    }
    @FunctionalInterface
    public interface RendererConstructor<BlockEntityClass extends BlockEntity>{
        BlockEntityRenderer<BlockEntityClass> create(BlockEntityRendererFactory.Context ctx);
    }
}