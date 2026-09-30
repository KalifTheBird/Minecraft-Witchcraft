package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BloodBottleItem extends Item {
    public static final String TAG_NAME = "BloodName";
    public static final String TAG_ENTITY_TYPE = "BloodEntityType";
    public static final String TAG_ENTITY_UUID = "BloodEntityUuid";
    public static final String TAG_IS_PLAYER = "BloodIsPlayer";
    public static final String TAG_HEALTH = "BloodHealth";
    public static final String TAG_MAX_HEALTH = "BloodMaxHealth";
    public static final String TAG_COLLECTOR = "BloodCollector";
    public static final String TAG_COLLECTED_AT = "BloodCollectedAt";

    public BloodBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String sourceName = stack.getOrCreateTag().getString(TAG_NAME);
        if (!sourceName.isBlank()) {
            return Component.literal(sourceName + "'s Blood");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_NAME)) {
            tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        String sourceName = tag.getString(TAG_NAME);
        String entityTypeId = tag.getString(TAG_ENTITY_TYPE);
        ResourceLocation entityId = ResourceLocation.tryParse(entityTypeId);
        Component typeName = entityId == null
                ? Component.literal(entityTypeId)
                : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getOptional(entityId)
                .map(EntityType::getDescription)
                .orElse(Component.literal(entityTypeId));

        tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.source", sourceName)
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.type", typeName)
                .withStyle(ChatFormatting.GRAY));

        if (tag.getBoolean(TAG_IS_PLAYER)) {
            tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.player")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (tag.contains(TAG_HEALTH) && tag.contains(TAG_MAX_HEALTH)) {
            tooltip.add(Component.translatable(
                            "tooltip.witchcraft.blood_bottle.health",
                            formatNumber(tag.getFloat(TAG_HEALTH)),
                            formatNumber(tag.getFloat(TAG_MAX_HEALTH)))
                    .withStyle(ChatFormatting.GRAY));
        }

        String uuid = tag.getString(TAG_ENTITY_UUID);
        if (!uuid.isBlank() && flag.isAdvanced()) {
            tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.uuid", uuid)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        String collector = tag.getString(TAG_COLLECTOR);
        if (!collector.isBlank()) {
            tooltip.add(Component.translatable("tooltip.witchcraft.blood_bottle.collected_by", collector)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String formatNumber(float value) {
        if (value == Math.floor(value)) {
            return Integer.toString((int) value);
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
