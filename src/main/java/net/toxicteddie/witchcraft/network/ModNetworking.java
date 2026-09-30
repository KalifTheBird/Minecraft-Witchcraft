package net.toxicteddie.witchcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.network.packet.SyncMagicPacket;
import net.toxicteddie.witchcraft.network.packet.SyncElementPowerPacket;
import net.toxicteddie.witchcraft.network.packet.SyncConsecratedAreaPacket;
import net.toxicteddie.witchcraft.network.packet.RemoveAltarBindingPacket;
import net.toxicteddie.witchcraft.network.packet.SelectElementPowerPacket;
import net.toxicteddie.witchcraft.network.packet.TelekinesisStartPacket;
import net.toxicteddie.witchcraft.network.packet.TelekinesisReleasePacket;
import net.toxicteddie.witchcraft.network.packet.SpellImpactEffectPacket;
import net.toxicteddie.witchcraft.network.packet.AirPowerActionPacket;
import net.toxicteddie.witchcraft.network.packet.FirePowerActionPacket;
import net.toxicteddie.witchcraft.network.packet.AltarDrawActionPacket;
import net.toxicteddie.witchcraft.network.packet.SyncAltarDrawPacket;
import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;
import net.toxicteddie.witchcraft.network.packet.BookPageTurnPacket;
import net.toxicteddie.witchcraft.network.packet.BookButtonClickPacket;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Witchcraft.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetworking() {}

    public static void register() {
        CHANNEL.messageBuilder(SyncMagicPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncMagicPacket::encode)
                .decoder(SyncMagicPacket::decode)
                .consumerMainThread(SyncMagicPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncElementPowerPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncElementPowerPacket::encode)
                .decoder(SyncElementPowerPacket::decode)
                .consumerMainThread(SyncElementPowerPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncConsecratedAreaPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncConsecratedAreaPacket::encode)
                .decoder(SyncConsecratedAreaPacket::decode)
                .consumerMainThread(SyncConsecratedAreaPacket::handle)
                .add();

        CHANNEL.messageBuilder(RemoveAltarBindingPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RemoveAltarBindingPacket::encode)
                .decoder(RemoveAltarBindingPacket::decode)
                .consumerMainThread(RemoveAltarBindingPacket::handle)
                .add();

        CHANNEL.messageBuilder(SelectElementPowerPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectElementPowerPacket::encode)
                .decoder(SelectElementPowerPacket::decode)
                .consumerMainThread(SelectElementPowerPacket::handle)
                .add();

        CHANNEL.messageBuilder(TelekinesisStartPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TelekinesisStartPacket::encode)
                .decoder(TelekinesisStartPacket::decode)
                .consumerMainThread(TelekinesisStartPacket::handle)
                .add();

        CHANNEL.messageBuilder(TelekinesisReleasePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TelekinesisReleasePacket::encode)
                .decoder(TelekinesisReleasePacket::decode)
                .consumerMainThread(TelekinesisReleasePacket::handle)
                .add();

        CHANNEL.messageBuilder(AirPowerActionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AirPowerActionPacket::encode)
                .decoder(AirPowerActionPacket::decode)
                .consumerMainThread(AirPowerActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(FirePowerActionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FirePowerActionPacket::encode)
                .decoder(FirePowerActionPacket::decode)
                .consumerMainThread(FirePowerActionPacket::handle)
                .add();


        CHANNEL.messageBuilder(SyncAltarDrawPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAltarDrawPacket::encode)
                .decoder(SyncAltarDrawPacket::decode)
                .consumerMainThread(SyncAltarDrawPacket::handle)
                .add();

        CHANNEL.messageBuilder(AltarDrawActionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AltarDrawActionPacket::encode)
                .decoder(AltarDrawActionPacket::decode)
                .consumerMainThread(AltarDrawActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(SpellImpactEffectPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpellImpactEffectPacket::encode)
                .decoder(SpellImpactEffectPacket::decode)
                .consumerMainThread(SpellImpactEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenBookOfShadowsPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenBookOfShadowsPacket::encode)
                .decoder(OpenBookOfShadowsPacket::decode)
                .consumerMainThread(OpenBookOfShadowsPacket::handle)
                .add();

        CHANNEL.messageBuilder(BookPageTurnPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BookPageTurnPacket::encode)
                .decoder(BookPageTurnPacket::decode)
                .consumerMainThread(BookPageTurnPacket::handle)
                .add();

        CHANNEL.messageBuilder(BookButtonClickPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BookButtonClickPacket::encode)
                .decoder(BookButtonClickPacket::decode)
                .consumerMainThread(BookButtonClickPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
