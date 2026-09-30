package net.toxicteddie.witchcraft.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>>
            MENU_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.MENU_TYPES,
                    Witchcraft.MOD_ID
            );

    public static final RegistryObject<MenuType<WitchesOvenMenu>> WITCHES_OVEN_MENU =
            MENU_TYPES.register(
                    "witches_oven",
                    () -> IForgeMenuType.create(WitchesOvenMenu::new)
            );

    public static final RegistryObject<
            MenuType<WitchesAltarMenu>
            > WITCHES_ALTAR_MENU =
            MENU_TYPES.register(
                    "witches_altar",
                    () -> IForgeMenuType.create(
                            WitchesAltarMenu::new
                    )
            );

    private ModMenuTypes() {
    }

    public static void register(
            IEventBus eventBus
    ) {
        MENU_TYPES.register(eventBus);
    }
}
