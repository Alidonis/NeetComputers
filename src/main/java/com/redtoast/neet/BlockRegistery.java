package com.redtoast.neet;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class BlockRegistery {
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
        Registry.register(Registries.BLOCK, Identifier.of(Namespace, address), block);
        BlockItem blockItem = null;
        if (AutoRegisterItem){
            blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.of(Namespace, address), blockItem);
        }
        BlockEntityType<?> blockEntity = FabricBlockEntityTypeBuilder.create((FabricBlockEntityTypeBuilder.Factory<BlockEntity>) constructor::create,block).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(Namespace, address+"_entity"), blockEntity);
        if (AutoRegisterItem){
            add(address,new Registered(block, blockItem, blockEntity));
        }else{
            add(address,new Registered(block, blockEntity));
        }
    }
    public static <BlockClass extends Block, BlockEntityClass extends BlockEntity> void register(String address, BlockClass block, BlockEntityConstructor<BlockEntityClass> constructor){
        register(address,block,constructor,false);
    }
    public static <BlockClass extends Block> void register(String address, BlockClass block, boolean AutoRegisterItem){
        Registry.register(Registries.BLOCK, Identifier.of(Namespace, address), block);
        if (AutoRegisterItem){
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.of(Namespace, address), blockItem);
            add(address, new Registered(block, blockItem));
        }else{
            add(address, new Registered(block));
        }
    }
    public static <BlockClass extends Block> void register(String address, BlockClass block){
        register(address,block,false);
    }

    @FunctionalInterface
    public interface BlockEntityConstructor<BlockEntityClass extends BlockEntity> {
        BlockEntityClass create(BlockPos blockPos, BlockState blockState);
    }
}
