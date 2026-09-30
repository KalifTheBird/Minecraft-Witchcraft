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

public class CauldronRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int magicCost;
    private final int altarCost;

    public CauldronRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result, int magicCost, int altarCost) {
        this.id = id; this.ingredients = ingredients; this.result = result; this.magicCost = magicCost; this.altarCost = altarCost;
    }

    @Override public boolean matches(SimpleContainer container, Level level) {
        if (container.getContainerSize() != ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) if (!ingredients.get(i).test(container.getItem(i))) return false;
        return true;
    }
    public boolean matchesPrefix(java.util.List<ItemStack> stacks) {
        if (stacks.size() > ingredients.size()) return false;
        for (int i = 0; i < stacks.size(); i++) if (!ingredients.get(i).test(stacks.get(i))) return false;
        return true;
    }
    public int size() { return ingredients.size(); }
    public int getMagicCost() { return magicCost; }
    public int getAltarCost() { return altarCost; }
    public ItemStack getOutput() { return result.copy(); }
    @Override public ItemStack assemble(SimpleContainer c, RegistryAccess r) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess r) { return result.copy(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CAULDRON_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.CAULDRON_TYPE.get(); }

    private static ItemStack readStack(JsonObject object, ResourceLocation id) {
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(object, "item"));
        var item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) throw new IllegalArgumentException("Unknown cauldron output " + itemId + " in " + id);
        return new ItemStack(item, GsonHelper.getAsInt(object, "count", 1));
    }

    public static class Serializer implements RecipeSerializer<CauldronRecipe> {
        @Override public CauldronRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            array.forEach(e -> ingredients.add(Ingredient.fromJson(e)));
            if (ingredients.isEmpty()) throw new IllegalArgumentException("Cauldron recipe must have ingredients: " + id);
            ItemStack result = readStack(GsonHelper.getAsJsonObject(json, "result"), id);
            return new CauldronRecipe(id, ingredients, result, GsonHelper.getAsInt(json, "magic_cost", 40), GsonHelper.getAsInt(json, "altar_cost", 80));
        }
        @Override public @Nullable CauldronRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf b) {
            int n=b.readVarInt(); NonNullList<Ingredient> list=NonNullList.create();
            for(int i=0;i<n;i++) list.add(Ingredient.fromNetwork(b));
            return new CauldronRecipe(id,list,b.readItem(),b.readVarInt(),b.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf b, CauldronRecipe r) {
            b.writeVarInt(r.ingredients.size()); r.ingredients.forEach(i->i.toNetwork(b)); b.writeItem(r.result); b.writeVarInt(r.magicCost); b.writeVarInt(r.altarCost);
        }
    }
}
