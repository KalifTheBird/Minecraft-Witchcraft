package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.item.client.model.WitchesHatModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;


public class WitchesHatItem extends ArmorItem {
    public static final String HAT_COLOR_TAG = "WitchcraftHatColor";
    public static final String RIBBON_COLOR_TAG = "WitchcraftHatRibbonColor";

    private static final Set<String> VALID_COLORS = Set.of(
            "white", "orange", "magenta", "light_blue",
            "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue",
            "brown", "green", "red", "black"
    );

    public WitchesHatItem(Properties properties) {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, properties);
    }

    public static void setHatColor(ItemStack stack, String color) {
        if (VALID_COLORS.contains(color)) {
            stack.getOrCreateTag().putString(HAT_COLOR_TAG, color);
        }
    }

    public static void setRibbonColor(ItemStack stack, String color) {
        if (VALID_COLORS.contains(color)) {
            stack.getOrCreateTag().putString(RIBBON_COLOR_TAG, color);
        }
    }

    @Nullable
    public static String getHatColor(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(HAT_COLOR_TAG)) {
            return null;
        }
        String color = stack.getTag().getString(HAT_COLOR_TAG);
        return VALID_COLORS.contains(color) ? color : null;
    }

    @Nullable
    public static String getRibbonColor(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(RIBBON_COLOR_TAG)) {
            return null;
        }
        String color = stack.getTag().getString(RIBBON_COLOR_TAG);
        return VALID_COLORS.contains(color) ? color : null;
    }


    @Nullable
    public static ResourceLocation getRibbonTexture(ItemStack stack) {
        String ribbonColor = getRibbonColor(stack);
        if (ribbonColor == null) {
            return null;
        }

        return new ResourceLocation(
                Witchcraft.MOD_ID,
                "textures/models/armor/witches_hat/trims/witches_hat_trim_"
                        + textureColorName(ribbonColor) + ".png"
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private WitchesHatModel<LivingEntity> model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity livingEntity,
                    ItemStack itemStack,
                    EquipmentSlot equipmentSlot,
                    HumanoidModel<?> original
            ) {
                if (model == null) {
                    model = new WitchesHatModel<>(
                            Minecraft.getInstance()
                                    .getEntityModels()
                                    .bakeLayer(ModModelLayers.WITCHES_HAT_LAYER)
                    );
                }

                ForgeHooksClient.copyModelProperties(original, model);
                return model;
            }
        });
    }

    @Override
    public String getArmorTexture(
            ItemStack stack,
            Entity entity,
            EquipmentSlot slot,
            String type
    ) {
        String hatColor = getHatColor(stack);


        if (hatColor == null) {
            return Witchcraft.MOD_ID + ":textures/models/armor/witches_hat.png";
        }


        return Witchcraft.MOD_ID + ":textures/models/armor/witches_hat/base/witches_hat_"
                + textureColorName(hatColor) + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        String hatColor = getHatColor(stack);
        String ribbonColor = getRibbonColor(stack);
        if (hatColor != null) {
            tooltip.add(Component.literal("Hat: " + displayColorName(hatColor)).withStyle(ChatFormatting.GRAY));
        }
        if (ribbonColor != null) {
            tooltip.add(Component.literal("Ribbon: " + displayColorName(ribbonColor)).withStyle(ChatFormatting.GRAY));
        }
    }

    public static String textureColorName(@Nullable String color) {
        if (color == null) {
            return "default";
        }

        return color.equals("lime") ? "lime_green" : color;
    }

    private static String displayColorName(String color) {
        String[] words = color.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(word.substring(1));
        }
        return result.toString();
    }
}
