package net.toxicteddie.witchcraft.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;


public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Witchcraft.MOD_ID);


    public static final RegistryObject<SoundEvent> RITUAL_FLAME_LOOP = registerVariableRange(
            "ritual_flame_loop"
    );


    public static final RegistryObject<SoundEvent> SPELL_IMPACT = registerVariableRange(
            "spell_impact"
    );

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> registerVariableRange(String name) {
        ResourceLocation id = new ResourceLocation(Witchcraft.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
