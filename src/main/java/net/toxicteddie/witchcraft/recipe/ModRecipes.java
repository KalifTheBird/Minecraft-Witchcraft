package net.toxicteddie.witchcraft.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.recipe.custom.MortarAndPestleRecipe;
import net.toxicteddie.witchcraft.recipe.custom.WitchesOvenRecipe;
import net.toxicteddie.witchcraft.recipe.custom.CauldronRecipe;
import net.toxicteddie.witchcraft.recipe.custom.WitchesHatDyeRecipe;
import net.toxicteddie.witchcraft.recipe.custom.SatchelDyeRecipe;

public final class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Witchcraft.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Witchcraft.MOD_ID);

    public static final RegistryObject<RecipeSerializer<MortarAndPestleRecipe>>
            MORTAR_AND_PESTLE_SERIALIZER = SERIALIZERS.register(
            "mortar_and_pestle",
            MortarAndPestleRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeType<MortarAndPestleRecipe>>
            MORTAR_AND_PESTLE_TYPE = TYPES.register(
            "mortar_and_pestle",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchcraft.MOD_ID + ":mortar_and_pestle";
                }
            }
    );

    public static final RegistryObject<RecipeSerializer<WitchesOvenRecipe>>
            WITCHES_OVEN_SERIALIZER = SERIALIZERS.register(
            "witches_oven",
            WitchesOvenRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeType<WitchesOvenRecipe>>
            WITCHES_OVEN_TYPE = TYPES.register(
            "witches_oven",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchcraft.MOD_ID + ":witches_oven";
                }
            }
    );


    public static final RegistryObject<RecipeSerializer<CauldronRecipe>>
            CAULDRON_SERIALIZER = SERIALIZERS.register("cauldron", CauldronRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<CauldronRecipe>>
            CAULDRON_TYPE = TYPES.register("cauldron", () -> new RecipeType<>() {
                @Override public String toString() { return Witchcraft.MOD_ID + ":cauldron"; }
            });

    public static final RegistryObject<SimpleCraftingRecipeSerializer<WitchesHatDyeRecipe>>
            WITCHES_HAT_DYE_SERIALIZER = SERIALIZERS.register(
            "witches_hat_dye",
            () -> new SimpleCraftingRecipeSerializer<>((id, category) ->
                    new WitchesHatDyeRecipe(id, category, false))
    );

    public static final RegistryObject<SimpleCraftingRecipeSerializer<WitchesHatDyeRecipe>>
            WITCHES_HAT_RIBBON_DYE_SERIALIZER = SERIALIZERS.register(
            "witches_hat_ribbon_dye",
            () -> new SimpleCraftingRecipeSerializer<>((id, category) ->
                    new WitchesHatDyeRecipe(id, category, true))
    );

    public static final RegistryObject<SimpleCraftingRecipeSerializer<SatchelDyeRecipe>>
            SATCHEL_DYE_SERIALIZER = SERIALIZERS.register(
            "satchel_dye",
            () -> new SimpleCraftingRecipeSerializer<>(SatchelDyeRecipe::new)
    );

    private ModRecipes() {
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
