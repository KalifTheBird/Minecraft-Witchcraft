package net.toxicteddie.witchcraft.recipe.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

public class MortarAndPestleRecipe implements Recipe<SimpleContainer> {

    public static final int MAX_INGREDIENTS = 3;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public MortarAndPestleRecipe(
            ResourceLocation id,
            NonNullList<Ingredient> ingredients,
            ItemStack result
    ) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        NonNullList<ItemStack> present = NonNullList.create();

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                present.add(stack);
            }
        }

        if (present.size() != this.ingredients.size()) {
            return false;
        }

        boolean[] usedStacks = new boolean[present.size()];

        for (Ingredient ingredient : this.ingredients) {
            boolean matched = false;

            for (int index = 0; index < present.size(); index++) {
                if (!usedStacks[index] && ingredient.test(present.get(index))) {
                    usedStacks[index] = true;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MORTAR_AND_PESTLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MORTAR_AND_PESTLE_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<MortarAndPestleRecipe> {

        @Override
        public MortarAndPestleRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients");

            if (ingredientArray.isEmpty() || ingredientArray.size() > MAX_INGREDIENTS) {
                throw new IllegalArgumentException(
                        "Mortar and pestle recipes require between 1 and 3 ingredients: " + id
                );
            }

            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredientArray.forEach(element -> ingredients.add(Ingredient.fromJson(element)));

            JsonObject resultObject = GsonHelper.getAsJsonObject(json, "result");
            ResourceLocation resultId = new ResourceLocation(
                    GsonHelper.getAsString(resultObject, "item")
            );

            var resultItem = ForgeRegistries.ITEMS.getValue(resultId);
            if (resultItem == null) {
                throw new IllegalArgumentException("Unknown mortar recipe result: " + resultId);
            }

            int count = GsonHelper.getAsInt(resultObject, "count", 1);
            if (count < 1) {
                throw new IllegalArgumentException("Mortar recipe result count must be positive: " + id);
            }

            return new MortarAndPestleRecipe(
                    id,
                    ingredients,
                    new ItemStack(resultItem, count)
            );
        }

        @Override
        public @Nullable MortarAndPestleRecipe fromNetwork(
                ResourceLocation id,
                FriendlyByteBuf buffer
        ) {
            int ingredientCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(
                    ingredientCount,
                    Ingredient.EMPTY
            );

            for (int index = 0; index < ingredientCount; index++) {
                ingredients.set(index, Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            return new MortarAndPestleRecipe(id, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MortarAndPestleRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }
}
