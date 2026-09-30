package net.toxicteddie.witchcraft.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SyncAltarDrawPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class AltarDrawManager {
    private static final int TRANSFER_INTERVAL_TICKS = 5;
    private static final int TRANSFER_PER_INTERVAL = 2;
    private static final double MAX_DRAW_DISTANCE = 6.0D;

    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private AltarDrawManager() {}

    public static boolean begin(
            ServerPlayer player,
            BlockPos altarPos,
            InteractionHand hand
    ) {
        if (PlayerMagicManager.isMagicallyExhausted(player)) return false;

        ItemStack wand = player.getItemInHand(hand);
        if (!(wand.getItem() instanceof WandItem)) {
            return false;
        }

        if (!(player.serverLevel().getBlockEntity(altarPos)
                instanceof WitchesAltarBlockEntity altar)) {
            return false;
        }

        if (!altar.isAuthorized(player) || altar.getStoredPower() <= 0) {
            return false;
        }

        if (player.distanceToSqr(
                altarPos.getX() + 0.5D,
                altarPos.getY() + 0.8D,
                altarPos.getZ() + 0.5D
        ) > MAX_DRAW_DISTANCE * MAX_DRAW_DISTANCE) {
            return false;
        }

        Session existing = ACTIVE.get(player.getUUID());


        if (existing != null
                && existing.altarPos().equals(altarPos)
                && existing.hand() == hand) {
            return true;
        }

        ACTIVE.put(player.getUUID(), new Session(altarPos.immutable(), hand));
        ModNetworking.sendToPlayer(
                new SyncAltarDrawPacket(true, altarPos.immutable()),
                player
        );
        return true;
    }

    public static void tick(ServerPlayer player) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null) return;

        ItemStack wand = player.getItemInHand(session.hand());
        if (!(wand.getItem() instanceof WandItem)
                || PlayerMagicManager.isMagicallyExhausted(player)) {
            stop(player);
            return;
        }

        BlockPos altarPos = session.altarPos();
        if (player.distanceToSqr(
                altarPos.getX() + 0.5D,
                altarPos.getY() + 0.8D,
                altarPos.getZ() + 0.5D
        ) > MAX_DRAW_DISTANCE * MAX_DRAW_DISTANCE) {
            stop(player);
            return;
        }

        if (!(player.serverLevel().getBlockEntity(altarPos)
                instanceof WitchesAltarBlockEntity altar)
                || !altar.isAuthorized(player)) {
            stop(player);
            return;
        }

        if (PlayerMagicManager.getMagic(player) >= MagicConstants.ABSOLUTE_LIMIT
                || altar.getStoredPower() <= 0) {
            stop(player);
            return;
        }

        if (player.tickCount % TRANSFER_INTERVAL_TICKS != 0) return;

        int room = MagicConstants.ABSOLUTE_LIMIT - PlayerMagicManager.getMagic(player);
        int requested = Math.min(TRANSFER_PER_INTERVAL, room);
        int extracted = altar.extractPower(requested);

        if (extracted <= 0) {
            stop(player);
            return;
        }

        int accepted = PlayerMagicManager.addMagic(player, extracted);


        if (accepted < extracted) {
            altar.addPower(extracted - accepted);
        }
    }

    public static void stop(ServerPlayer player) {
        Session removed = ACTIVE.remove(player.getUUID());
        if (removed != null) {
            ModNetworking.sendToPlayer(
                    new SyncAltarDrawPacket(false, BlockPos.ZERO),
                    player
            );
        }
    }

    public static boolean isDrawing(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    private record Session(BlockPos altarPos, InteractionHand hand) {}
}
