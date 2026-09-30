package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
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
import net.toxicteddie.witchcraft.item.client.model.SatchelModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;


public class SatchelItem extends ArmorItem {
    public static final String SATCHEL_COLOR_TAG = "WitchcraftSatchelColor";

    private static final Set<String> VALID_COLORS = Set.of(
            "white", "orange", "magenta", "light_blue",
            "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue",
            "brown", "green", "red", "black"
    );

    public SatchelItem(Properties properties) {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, properties);
    }

    public static void setSatchelColor(ItemStack stack, String color) {
        if (VALID_COLORS.contains(color)) {
            stack.getOrCreateTag().putString(SATCHEL_COLOR_TAG, color);
        }
    }

    @Nullable
    public static String getSatchelColor(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(SATCHEL_COLOR_TAG)) {
            return null;
        }

        String color = stack.getTag().getString(SATCHEL_COLOR_TAG);
        return VALID_COLORS.contains(color) ? color : null;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SatchelModel<LivingEntity> model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity,
                    ItemStack stack,
                    EquipmentSlot slot,
                    HumanoidModel<?> original
            ) {
                if (model == null) {
                    model = new SatchelModel<>(
                            Minecraft.getInstance()
                                    .getEntityModels()
                                    .bakeLayer(ModModelLayers.SATCHEL_LAYER)
                    );
                }

                ForgeHooksClient.copyModelProperties(original, model);
                return model;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String color = getSatchelColor(stack);

        if (color == null) {
            return Witchcraft.MOD_ID + ":textures/models/armor/satchel.png";
        }

        return Witchcraft.MOD_ID + ":textures/models/armor/satchel/base/satchel_"
                + textureColorName(color) + ".png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        String color = getSatchelColor(stack);
        if (color != null) {
            tooltip.add(Component.literal("Color: " + displayColorName(color))
                    .withStyle(ChatFormatting.GRAY));
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
