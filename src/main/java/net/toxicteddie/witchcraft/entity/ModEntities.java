package net.toxicteddie.witchcraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.custom.DemonEntity;
import net.toxicteddie.witchcraft.entity.custom.ImpEntity;
import net.toxicteddie.witchcraft.entity.custom.ShadowManEntity;
import net.toxicteddie.witchcraft.entity.custom.MandrakeEntity;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Witchcraft.MOD_ID);


    public static final RegistryObject<EntityType<BookOfShadowsFollowingEntity>> BOOK_OF_SHADOWS_FOLLOWING =
            ENTITY_TYPES.register("book_of_shadows_following",
                    () -> EntityType.Builder
                            .<BookOfShadowsFollowingEntity>of(BookOfShadowsFollowingEntity::new, MobCategory.MISC)


                            .sized(0.9375F, 0.9375F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("book_of_shadows_following")
            );

    public static final RegistryObject<EntityType<MandrakeEntity>> MANDRAKE =
            ENTITY_TYPES.register("mandrake",
                    () -> EntityType.Builder
                            .of(MandrakeEntity::new, MobCategory.MONSTER)
                            .sized(0.45F, 0.85F)
                            .build("mandrake")
            );

    public static final RegistryObject<EntityType<DemonEntity>> DEMON =
            ENTITY_TYPES.register("demon",
                    () -> EntityType.Builder
                            .of(DemonEntity::new, MobCategory.MONSTER)
                            .sized(0.95F, 2.65F)
                            .build("demon")
            );

    public static final RegistryObject<EntityType<ShadowManEntity>> SHADOW_MAN =
            ENTITY_TYPES.register("shadow_man",
                    () -> EntityType.Builder
                            .of(ShadowManEntity::new, MobCategory.CREATURE)
                            .sized(1.0F, 2.9F)
                            .clientTrackingRange(10)
                            .build("shadow_man")
            );

    public static final RegistryObject<EntityType<ImpEntity>> IMP =
            ENTITY_TYPES.register("imp",
                    () -> EntityType.Builder
                            .of(ImpEntity::new, MobCategory.MONSTER)
                            .sized(0.45F, 0.75F)
                            .build("imp")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
