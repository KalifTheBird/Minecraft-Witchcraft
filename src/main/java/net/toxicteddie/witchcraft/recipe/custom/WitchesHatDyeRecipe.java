package net.toxicteddie.witchcraft.recipe.custom;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.item.custom.WitchesHatItem;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import org.jetbrains.annotations.NotNull;


public class WitchesHatDyeRecipe extends CustomRecipe {
    private final boolean ribbonRecipe;

    public WitchesHatDyeRecipe(ResourceLocation id, CraftingBookCategory category, boolean ribbonRecipe) {
        super(id, category);
        this.ribbonRecipe = ribbonRecipe;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack hat = ItemStack.EMPTY;
        DyeItem dye = null;
        int stringCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof WitchesHatItem) {
                if (!hat.isEmpty()) {
                    return false;
                }
                hat = stack;
            } else if (stack.getItem() instanceof DyeItem foundDye) {
                if (dye != null) {
                    return false;
                }
                dye = foundDye;
            } else if (stack.is(Items.STRING)) {
                stringCount++;
                if (stringCount > 1) {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (hat.isEmpty() || dye == null) {
            return false;
        }

        return ribbonRecipe ? stringCount == 1 : stringCount == 0;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack hat = ItemStack.EMPTY;
        DyeItem dye = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof WitchesHatItem) {
                hat = stack;
            } else if (stack.getItem() instanceof DyeItem foundDye) {
                dye = foundDye;
            }
        }

        if (hat.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = hat.copy();
        result.setCount(1);

        String color = dye.getDyeColor().getName();
        if (ribbonRecipe) {
            WitchesHatItem.setRibbonColor(result, color);
        } else {
            WitchesHatItem.setHatColor(result, color);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= (ribbonRecipe ? 3 : 2);
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.WITCHES_HAT.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ribbonRecipe
                ? ModRecipes.WITCHES_HAT_RIBBON_DYE_SERIALIZER.get()
                : ModRecipes.WITCHES_HAT_DYE_SERIALIZER.get();
    }
}
