package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.core.registries.Registries;
import net.toxicteddie.witchcraft.Witchcraft;

public class ModTreeGrower extends AbstractTreeGrower {
    public static final ModTreeGrower ROWAN = new ModTreeGrower("rowan");
    public static final ModTreeGrower ELDER = new ModTreeGrower("elder");

    private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;

    private ModTreeGrower(String name) {
        this.featureKey = ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                new ResourceLocation(Witchcraft.MOD_ID, name)
        );
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return featureKey;
    }
}
