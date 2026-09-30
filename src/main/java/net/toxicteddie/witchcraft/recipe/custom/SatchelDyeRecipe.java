package net.toxicteddie.witchcraft.recipe.custom;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.item.custom.SatchelItem;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import org.jetbrains.annotations.NotNull;


public class SatchelDyeRecipe extends CustomRecipe {
    public SatchelDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack satchel = ItemStack.EMPTY;
        DyeItem dye = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof SatchelItem) {
                if (!satchel.isEmpty()) {
                    return false;
                }
                satchel = stack;
            } else if (stack.getItem() instanceof DyeItem foundDye) {
                if (dye != null) {
                    return false;
                }
                dye = foundDye;
            } else {
                return false;
            }
        }

        return !satchel.isEmpty() && dye != null;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack satchel = ItemStack.EMPTY;
        DyeItem dye = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof SatchelItem) {
                satchel = stack;
            } else if (stack.getItem() instanceof DyeItem foundDye) {
                dye = foundDye;
            }
        }

        if (satchel.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = satchel.copy();
        result.setCount(1);
        SatchelItem.setSatchelColor(result, dye.getDyeColor().getName());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.SATCHEL.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.SATCHEL_DYE_SERIALIZER.get();
    }
}
