package net.toxicteddie.witchcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.toxicteddie.witchcraft.block.custom.WitchesAltarBlock;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WitchesAltarMenu extends AbstractContainerMenu {

    public record BoundWitch(UUID uuid, String name) {}

    private final WitchesAltarBlockEntity altar;
    private final ContainerData data;
    private final BlockPos altarPos;
    private final boolean ownerView;
    private final String ownerName;
    private final List<BoundWitch> boundWitches;

    public WitchesAltarMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                getAltar(inventory, buffer.readBlockPos()),
                new SimpleContainerData(4),
                buffer.readBoolean(),
                buffer.readUtf(),
                readBoundWitches(buffer)
        );
    }

    public WitchesAltarMenu(
            int containerId,
            Inventory inventory,
            WitchesAltarBlockEntity altar,
            ContainerData data
    ) {
        this(
                containerId,
                inventory,
                altar,
                data,
                altar.isOwner(inventory.player),
                altar.getOwnerName(),
                altar.getWhitelistedWitches().stream()
                        .map(entry -> new BoundWitch(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    private WitchesAltarMenu(
            int containerId,
            Inventory inventory,
            WitchesAltarBlockEntity altar,
            ContainerData data,
            boolean ownerView,
            String ownerName,
            List<BoundWitch> boundWitches
    ) {
        super(ModMenuTypes.WITCHES_ALTAR_MENU.get(), containerId);
        this.altar = altar;
        this.data = data;
        this.altarPos = altar.getBlockPos();
        this.ownerView = ownerView;
        this.ownerName = ownerName;
        this.boundWitches = new ArrayList<>(boundWitches);
        this.addDataSlots(data);
    }

    private static List<BoundWitch> readBoundWitches(FriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 64);
        List<BoundWitch> witches = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            witches.add(new BoundWitch(buffer.readUUID(), buffer.readUtf()));
        }
        return witches;
    }

    private static WitchesAltarBlockEntity getAltar(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof WitchesAltarBlockEntity altar) {
            return altar;
        }
        throw new IllegalStateException("Witches' altar block entity was missing.");
    }

    public BlockPos getAltarPos() { return this.altarPos; }
    public boolean isOwnerView() { return this.ownerView; }
    public String getOwnerName() { return this.ownerName; }
    public List<BoundWitch> getBoundWitches() { return this.boundWitches; }
    public void removeClientBoundWitch(UUID uuid) { this.boundWitches.removeIf(witch -> witch.uuid().equals(uuid)); }

    public int getStoredPower() { return this.data.get(0); }
    public int getCapacity() { return this.data.get(1); }
    public int getNaturalDraw() { return this.data.get(2); }
    public int getModifierDraw() { return this.data.get(3); }
    public int getTotalDraw() { return this.getNaturalDraw() + this.getModifierDraw(); }

    public float getPowerFraction() {
        int capacity = this.getCapacity();
        return capacity <= 0 ? 0.0F : Math.min(1.0F, this.getStoredPower() / (float) capacity);
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = player.level();
        return this.altar.hasCarpet()
                && this.altar.isAuthorized(player)
                && level.getBlockState(this.altar.getBlockPos()).getBlock() instanceof WitchesAltarBlock
                && player.distanceToSqr(
                        this.altar.getBlockPos().getX() + 0.5D,
                        this.altar.getBlockPos().getY() + 0.5D,
                        this.altar.getBlockPos().getZ() + 0.5D
                ) <= 64.0D;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
