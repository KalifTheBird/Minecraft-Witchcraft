package net.toxicteddie.witchcraft.client.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class AltarDrawClientState {
    private static BlockPos altarPos;
    private static long startedAtGameTick;

    private AltarDrawClientState() {}

    public static void begin(BlockPos pos) {
        BlockPos next = pos == null ? null : pos.immutable();


        if (next != null && next.equals(altarPos)) {
            return;
        }

        altarPos = next;
        Minecraft minecraft = Minecraft.getInstance();
        startedAtGameTick = minecraft.level == null
                ? 0L
                : minecraft.level.getGameTime();


        if (minecraft.player != null && next != null) {
            if (minecraft.player.getMainHandItem().getItem()
                    instanceof net.toxicteddie.witchcraft.item.custom.WandItem) {
                minecraft.player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
            } else if (minecraft.player.getOffhandItem().getItem()
                    instanceof net.toxicteddie.witchcraft.item.custom.WandItem) {
                minecraft.player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
            }
        }
    }

    public static void clear() {
        altarPos = null;
        startedAtGameTick = 0L;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isUsingItem()
                && minecraft.player.getUseItem().getItem()
                instanceof net.toxicteddie.witchcraft.item.custom.WandItem) {
            minecraft.player.stopUsingItem();
        }
    }

    public static BlockPos altarPos() {
        return altarPos;
    }

    public static boolean active() {
        return altarPos != null;
    }

    public static float ageTicks(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active() || minecraft.level == null) return 0.0F;
        return Math.max(
                0.0F,
                (minecraft.level.getGameTime() - startedAtGameTick) + partialTick
        );
    }


    public static float connectionProgress(float partialTick) {
        return Math.min(1.0F, ageTicks(partialTick) / 12.0F);
    }
}
