package net.toxicteddie.witchcraft.client.book;

import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;


public final class BookOfShadowsClient {
    private BookOfShadowsClient() {}

    public static void open(OpenBookOfShadowsPacket packet) {

        PhysicalBookContent.accept(packet);
    }
}
