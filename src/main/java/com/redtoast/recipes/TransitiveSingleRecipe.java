package com.redtoast.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redtoast.neet.NeetComputersServer;
import net.minecraft.component.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class TransitiveSingleRecipe extends ShapelessRecipe {
    public TransitiveSingleRecipe(String group, CraftingRecipeCategory category, ItemStack result, DefaultedList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    public TransitiveSingleRecipe(String group, CraftingRecipeCategory category, ItemStack result, ItemStack ingredient) {
        this(group, category, result, parseIngredient(ingredient));
    }

    private static DefaultedList<Ingredient> parseIngredient(ItemStack ingredient) {
        DefaultedList<Ingredient> buffer = DefaultedList.of();
        buffer.add(Ingredient.ofStacks(ingredient));
        return buffer;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack output = super.craft(craftingRecipeInput, wrapperLookup);
        int i = 0;
        while (craftingRecipeInput.getStackInSlot(i).isEmpty()) {
            i++;
        }
        ItemStack input = craftingRecipeInput.getStackInSlot(i);
        for (Component<?> component : input.getComponents()) {
            set(output, component);
        }

        return output;
    }

    private <T> void set(ItemStack target, Component<T> component) {
        target.set(component.type(), component.value());
    }

    @Override
    public RecipeSerializer<? extends Recipe<CraftingRecipeInput>> getSerializer() {
        return NeetComputersServer.TRANSITIVE_SINGLE_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<TransitiveSingleRecipe> {
        private static final MapCodec<TransitiveSingleRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(TransitiveSingleRecipe::getGroup),
                                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(TransitiveSingleRecipe::getCategory),
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResult(null)),
                                ItemStack.VALIDATED_CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.getIngredients().getFirst().getMatchingStacks()[0])
                        )
                        .apply(instance, TransitiveSingleRecipe::new)
        );
        public static final PacketCodec<RegistryByteBuf, TransitiveSingleRecipe> PACKET_CODEC = PacketCodec.ofStatic(
                TransitiveSingleRecipe.Serializer::write, TransitiveSingleRecipe.Serializer::read
        );

        @Override
        public MapCodec<TransitiveSingleRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, TransitiveSingleRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static TransitiveSingleRecipe read(RegistryByteBuf buf) {
            String string = buf.readString();
            CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
            int i = buf.readVarInt();
            DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(i, Ingredient.EMPTY);
            defaultedList.replaceAll(empty -> Ingredient.PACKET_CODEC.decode(buf));
            ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
            return new TransitiveSingleRecipe(string, craftingRecipeCategory, itemStack, defaultedList);
        }

        private static void write(RegistryByteBuf buf, TransitiveSingleRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            buf.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.PACKET_CODEC.encode(buf, ingredient);
            }

            ItemStack.PACKET_CODEC.encode(buf, recipe.getResult(null));
        }
    }
}
