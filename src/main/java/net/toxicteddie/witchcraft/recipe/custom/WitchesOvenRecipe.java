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

public class WitchesOvenRecipe implements Recipe<SimpleContainer> {
    public static final int DEFAULT_WATER_COST = 1;
    public static final int DEFAULT_BREW_TIME = 200;
    public static final int MAX_BYPRODUCTS = 3;
    public static final float DEFAULT_RESULT_CHANCE = 0.65F;

    private final ResourceLocation id;
    private final Ingredient bottle;
    private final Ingredient reagent;
    private final ItemStack result;
    private final NonNullList<ItemStack> byproducts;
    private final int waterCost;
    private final int brewTime;
    private final float resultChance;

    public WitchesOvenRecipe(ResourceLocation id, Ingredient bottle, Ingredient reagent,
                              ItemStack result, NonNullList<ItemStack> byproducts,
                              int waterCost, int brewTime, float resultChance) {
        this.id = id;
        this.bottle = bottle;
        this.reagent = reagent;
        this.result = result;
        this.byproducts = byproducts;
        this.waterCost = waterCost;
        this.brewTime = brewTime;
        this.resultChance = resultChance;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return bottle.test(container.getItem(0)) && reagent.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(bottle);
        ingredients.add(reagent);
        return ingredients;
    }

    public Ingredient getBottle() { return bottle; }
    public Ingredient getReagent() { return reagent; }
    public ItemStack getOutput() { return result.copy(); }
    public NonNullList<ItemStack> getByproducts() {
        NonNullList<ItemStack> copy = NonNullList.create();
        byproducts.forEach(stack -> copy.add(stack.copy()));
        return copy;
    }
    public int getWaterCost() { return waterCost; }
    public int getBrewTime() { return brewTime; }
    public float getResultChance() { return resultChance; }

    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.WITCHES_OVEN_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.WITCHES_OVEN_TYPE.get(); }

    private static ItemStack readStack(JsonObject object, ResourceLocation recipeId, String fieldName) {
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(object, "item"));
        var item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) throw new IllegalArgumentException("Unknown item in " + fieldName + " for " + recipeId + ": " + itemId);
        int count = GsonHelper.getAsInt(object, "count", 1);
        if (count < 1) throw new IllegalArgumentException(fieldName + " count must be positive for " + recipeId);
        return new ItemStack(item, count);
    }

    public static final class Serializer implements RecipeSerializer<WitchesOvenRecipe> {
        @Override
        public WitchesOvenRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient bottle = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "bottle"));
            Ingredient reagent = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "reagent"));
            ItemStack result = readStack(GsonHelper.getAsJsonObject(json, "result"), id, "result");

            NonNullList<ItemStack> byproducts = NonNullList.create();
            if (json.has("byproducts")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "byproducts");
                if (array.size() > MAX_BYPRODUCTS) {
                    throw new IllegalArgumentException("Witches' Oven recipes support at most 3 byproducts: " + id);
                }
                array.forEach(element -> byproducts.add(readStack(element.getAsJsonObject(), id, "byproduct")));
            }

            int waterCost = GsonHelper.getAsInt(json, "water", DEFAULT_WATER_COST);
            int brewTime = GsonHelper.getAsInt(json, "cooking_time", DEFAULT_BREW_TIME);
            float resultChance = GsonHelper.getAsFloat(json, "result_chance", DEFAULT_RESULT_CHANCE);
            if (waterCost < 1 || waterCost > 3) throw new IllegalArgumentException("Water cost must be 1-3 for " + id);
            if (brewTime < 1) throw new IllegalArgumentException("Cooking time must be positive for " + id);
            if (resultChance < 0.0F || resultChance > 1.0F) {
                throw new IllegalArgumentException("Result chance must be between 0 and 1 for " + id);
            }

            return new WitchesOvenRecipe(id, bottle, reagent, result, byproducts, waterCost, brewTime, resultChance);
        }

        @Override
        public @Nullable WitchesOvenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient bottle = Ingredient.fromNetwork(buffer);
            Ingredient reagent = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int byproductCount = buffer.readVarInt();
            NonNullList<ItemStack> byproducts = NonNullList.create();
            for (int i = 0; i < byproductCount; i++) byproducts.add(buffer.readItem());
            int waterCost = buffer.readVarInt();
            int brewTime = buffer.readVarInt();
            float resultChance = buffer.readFloat();
            return new WitchesOvenRecipe(id, bottle, reagent, result, byproducts, waterCost, brewTime, resultChance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WitchesOvenRecipe recipe) {
            recipe.bottle.toNetwork(buffer);
            recipe.reagent.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.byproducts.size());
            recipe.byproducts.forEach(buffer::writeItem);
            buffer.writeVarInt(recipe.waterCost);
            buffer.writeVarInt(recipe.brewTime);
            buffer.writeFloat(recipe.resultChance);
        }
    }
}
