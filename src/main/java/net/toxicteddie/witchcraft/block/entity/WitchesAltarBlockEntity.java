package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.toxicteddie.witchcraft.menu.WitchesAltarMenu;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WitchesAltarBlockEntity
        extends BlockEntity
        implements MenuProvider {

    public static final int CANDLE_LEFT_SLOT = 0;
    public static final int CANDLE_RIGHT_SLOT = 1;

    public static final int FIRST_TRINKET_SLOT = 2;
    public static final int LAST_TRINKET_SLOT = 6;

    public static final int SLOT_COUNT = 7;
    public static final int TRINKET_SLOT_COUNT = 5;

    public static final int BASE_POWER_CAPACITY = 500;


    private static final ConcurrentHashMap<UUID, WitchesAltarBlockEntity> LOADED_OWNED_ALTARS =
            new ConcurrentHashMap<>();


    private static final int POWER_SCAN_RADIUS = 6;
    private static final int POWER_SCAN_HEIGHT = 3;

    private static final int POWER_UPDATE_INTERVAL = 80;
    private static final int NATURAL_SCAN_INTERVAL = 40;

    private BlockState carpetState =
            Blocks.AIR.defaultBlockState();

    private int storedPower;
    private int cachedNaturalDraw;
    private int powerTickCounter;
    private int scanTickCounter;

    private UUID ownerUuid;
    private String ownerName = "";
    private final LinkedHashMap<UUID, String> whitelistedWitches = new LinkedHashMap<>();


    private final ItemStackHandler items =
            new ItemStackHandler(SLOT_COUNT) {

                @Override
                public boolean isItemValid(
                        int slot,
                        ItemStack stack
                ) {
                    if (isCandleSlot(slot)) {
                        return isValidCandle(stack);
                    }

                    return isTrinketSlot(slot)
                            && !(stack.getItem() instanceof BloodBottleItem);
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }

                @Override
                protected void onContentsChanged(int slot) {
                    WitchesAltarBlockEntity.this.setChanged();
                    WitchesAltarBlockEntity.this.sendUpdate();
                }
            };

    private LazyOptional<ItemStackHandler> itemCapability =
            LazyOptional.of(() -> this.items);


    private final int[] trinketAngles =
            new int[TRINKET_SLOT_COUNT];


    private final ContainerData altarData =
            new ContainerData() {

                @Override
                public int get(int index) {
                    return switch (index) {
                        case 0 -> storedPower;
                        case 1 -> getPowerCapacity();
                        case 2 -> cachedNaturalDraw;
                        case 3 -> getModifierDraw();
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                    switch (index) {
                        case 0 -> storedPower = value;
                        case 1 -> {

                        }
                        case 2 -> cachedNaturalDraw = value;
                        case 3 -> {

                        }
                    }
                }

                @Override
                public int getCount() {
                    return 4;
                }
            };

    public WitchesAltarBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.WITCHES_ALTAR_BE.get(),
                pos,
                state
        );
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerLoadedOwnedAltar();
    }

    @Override
    public void setRemoved() {
        unregisterLoadedOwnedAltar();
        super.setRemoved();
    }

    private void registerLoadedOwnedAltar() {
        if (this.level != null && !this.level.isClientSide && this.ownerUuid != null) {
            LOADED_OWNED_ALTARS.put(this.ownerUuid, this);
        }
    }

    private void unregisterLoadedOwnedAltar() {
        if (this.ownerUuid != null) {
            LOADED_OWNED_ALTARS.remove(this.ownerUuid, this);
        }
    }

    @Nullable
    public static WitchesAltarBlockEntity getLoadedOwnedAltar(UUID ownerUuid) {
        if (ownerUuid == null) return null;
        WitchesAltarBlockEntity altar = LOADED_OWNED_ALTARS.get(ownerUuid);
        if (altar != null && !altar.isRemoved() && altar.getLevel() != null) {
            return altar;
        }
        if (altar != null) LOADED_OWNED_ALTARS.remove(ownerUuid, altar);
        return null;
    }


    public boolean hasCarpet() {
        return !this.carpetState.isAir();
    }

    public BlockState getCarpetState() {
        return this.carpetState;
    }

    public void setCarpetState(BlockState state) {
        this.carpetState =
                state == null
                        ? Blocks.AIR.defaultBlockState()
                        : state;

        this.setChanged();
        this.sendUpdate();
    }

    public BlockState removeCarpet() {
        BlockState removed =
                this.carpetState;

        this.carpetState =
                Blocks.AIR.defaultBlockState();


        this.dropAllDisplayedItems();

        this.setChanged();
        this.sendUpdate();

        return removed;
    }


    public boolean hasOwner() {
        return this.ownerUuid != null;
    }

    public boolean claimOwnership(Player player) {
        if (this.ownerUuid != null) {
            return false;
        }
        this.ownerUuid = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        registerLoadedOwnedAltar();
        this.setChanged();
        this.sendUpdate();
        return true;
    }

    public boolean isOwner(Player player) {
        return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public boolean isAuthorized(Player player) {
        return this.isOwner(player) || this.whitelistedWitches.containsKey(player.getUUID());
    }

    public UUID getOwnerUuid() { return this.ownerUuid; }
    public String getOwnerName() { return this.ownerName; }

    public List<Map.Entry<UUID, String>> getWhitelistedWitches() {
        return new ArrayList<>(this.whitelistedWitches.entrySet());
    }

    public boolean bindWitch(Player owner, UUID witchUuid, String witchName) {
        if (!this.isOwner(owner) || witchUuid == null || witchUuid.equals(this.ownerUuid)) {
            return false;
        }
        this.whitelistedWitches.put(witchUuid, witchName == null || witchName.isBlank() ? "Unknown Witch" : witchName);
        this.setChanged();
        this.sendUpdate();
        return true;
    }

    public boolean removeBoundWitch(Player owner, UUID witchUuid) {
        if (!this.isOwner(owner) || witchUuid == null) {
            return false;
        }
        if (this.whitelistedWitches.remove(witchUuid) != null) {
            this.setChanged();
            this.sendUpdate();
            return true;
        }
        return false;
    }


    public static boolean isCandleSlot(int slot) {
        return slot == CANDLE_LEFT_SLOT
                || slot == CANDLE_RIGHT_SLOT;
    }

    public static boolean isTrinketSlot(int slot) {
        return slot >= FIRST_TRINKET_SLOT
                && slot <= LAST_TRINKET_SLOT;
    }

    public static boolean isValidCandle(
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.is(ModTags.Items.ALTAR_CANDLES)) {
            return true;
        }

        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof CandleBlock;
    }

    public ItemStack getDisplayedItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        return this.items.getStackInSlot(slot);
    }

    public boolean isSlotEmpty(int slot) {
        return this.getDisplayedItem(slot).isEmpty();
    }

    public boolean placeDisplayedItem(
            int slot,
            ItemStack heldStack,
            Player player
    ) {
        if (!this.hasCarpet()
                || heldStack.isEmpty()
                || slot < 0
                || slot >= SLOT_COUNT) {
            return false;
        }

        if (isCandleSlot(slot)
                && !isValidCandle(heldStack)) {
            return false;
        }

        if (isTrinketSlot(slot)
                && (isValidCandle(heldStack)
                || heldStack.getItem() instanceof BloodBottleItem)) {
            return false;
        }

        ItemStack previous =
                this.items.getStackInSlot(slot).copy();


        ItemStack placed =
                heldStack.copy();

        placed.setCount(1);

        this.items.setStackInSlot(
                slot,
                placed
        );

        if (!player.isCreative()) {
            heldStack.shrink(1);
        }

        if (!previous.isEmpty()) {
            giveOrDrop(
                    player,
                    previous
            );
        }

        return true;
    }

    public boolean removeDisplayedItem(
            int slot,
            Player player
    ) {
        if (!this.hasCarpet()
                || slot < 0
                || slot >= SLOT_COUNT) {
            return false;
        }

        ItemStack removed =
                this.items.extractItem(
                        slot,
                        1,
                        false
                );

        if (removed.isEmpty()) {
            return false;
        }

        giveOrDrop(
                player,
                removed
        );

        return true;
    }

    public boolean rotateTrinket(int slot) {
        if (!this.hasCarpet()
                || !isTrinketSlot(slot)
                || this.isSlotEmpty(slot)) {
            return false;
        }

        int index =
                slot - FIRST_TRINKET_SLOT;

        this.trinketAngles[index] =
                (this.trinketAngles[index] + 1) % 8;

        this.setChanged();
        this.sendUpdate();

        return true;
    }

    public int getTrinketAngle(int slot) {
        if (!isTrinketSlot(slot)) {
            return 0;
        }

        return this.trinketAngles[
                slot - FIRST_TRINKET_SLOT
                ];
    }

    private void giveOrDrop(
            Player player,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return;
        }

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(
                    player.level(),
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 1.0D,
                    this.worldPosition.getZ() + 0.5D,
                    stack
            );
        }
    }

    public void dropAllDisplayedItems() {
        if (this.level == null) {
            return;
        }

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            ItemStack removed =
                    this.items.extractItem(
                            slot,
                            1,
                            false
                    );

            if (!removed.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 1.0D,
                        this.worldPosition.getZ() + 0.5D,
                        removed
                );
            }
        }
    }


    public int getStoredPower() {
        return this.storedPower;
    }

    public int addPower(int amount) {
        if (amount <= 0) return 0;
        int before = this.storedPower;
        this.storedPower = Math.min(this.getPowerCapacity(), this.storedPower + amount);
        int added = this.storedPower - before;
        if (added > 0) {
            this.setChanged();
            this.sendUpdate();
        }
        return added;
    }

    public int setStoredPower(int amount) {
        int before = this.storedPower;
        this.storedPower = net.minecraft.util.Mth.clamp(amount, 0, this.getPowerCapacity());
        if (this.storedPower != before) {
            this.setChanged();
            this.sendUpdate();
        }
        return this.storedPower;
    }


    public int extractPower(int amount) {
        if (amount <= 0 || this.storedPower <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, this.storedPower);
        this.storedPower -= extracted;
        this.setChanged();
        this.sendUpdate();
        return extracted;
    }

    public int getNaturalDraw() {
        return this.cachedNaturalDraw;
    }

    public int getModifierDraw() {

        return 0;
    }

    public int getPowerCapacity() {

        return BASE_POWER_CAPACITY;
    }

    public int getTotalDraw() {


        return 1;
    }

    private int scanNaturalPower() {
        if (this.level == null) {
            return 0;
        }

        int power = 0;

        BlockPos.MutableBlockPos cursor =
                new BlockPos.MutableBlockPos();

        for (int x = -POWER_SCAN_RADIUS;
             x <= POWER_SCAN_RADIUS;
             x++) {

            for (int y = -POWER_SCAN_HEIGHT;
                 y <= POWER_SCAN_HEIGHT;
                 y++) {

                for (int z = -POWER_SCAN_RADIUS;
                     z <= POWER_SCAN_RADIUS;
                     z++) {

                    cursor.set(
                            this.worldPosition.getX() + x,
                            this.worldPosition.getY() + y,
                            this.worldPosition.getZ() + z
                    );

                    BlockState state =
                            this.level.getBlockState(cursor);

                    if (state.is(ModTags.Blocks.ALTAR_POWER_LARGE)) {
                        power += 4;
                    } else if (state.is(ModTags.Blocks.ALTAR_POWER_MEDIUM)) {
                        power += 2;
                    } else if (state.is(ModTags.Blocks.ALTAR_POWER_SMALL)) {
                        power += 1;
                    }


                    if (power >= 100) {
                        return 100;
                    }
                }
            }
        }

        return power;
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            WitchesAltarBlockEntity altar
    ) {

        if (!altar.hasCarpet()) {
            altar.cachedNaturalDraw = 0;
            return;
        }


        altar.cachedNaturalDraw = 0;
        altar.powerTickCounter++;

        if (altar.powerTickCounter >= POWER_UPDATE_INTERVAL) {
            altar.powerTickCounter = 0;
            altar.storedPower = Math.min(
                    altar.getPowerCapacity(),
                    altar.storedPower + 1
            );
            altar.setChanged();
            altar.sendUpdate();
        }

        altar.storedPower = Math.min(altar.storedPower, altar.getPowerCapacity());
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "screen.witchcraft.witches_altar"
        );
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new WitchesAltarMenu(
                containerId,
                inventory,
                this,
                this.altarData
        );
    }


    @Override
    protected void saveAdditional(
            CompoundTag tag
    ) {
        super.saveAdditional(tag);

        if (!this.carpetState.isAir()) {
            tag.put(
                    "CarpetState",
                    NbtUtils.writeBlockState(
                            this.carpetState
                    )
            );
        }

        tag.put(
                "DisplayedItems",
                this.items.serializeNBT()
        );

        tag.putIntArray(
                "TrinketAngles",
                this.trinketAngles
        );

        tag.putInt(
                "StoredPower",
                this.storedPower
        );

        tag.putInt(
                "NaturalDraw",
                this.cachedNaturalDraw
        );

        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUuid", this.ownerUuid);
            tag.putString("OwnerName", this.ownerName);
        }

        ListTag whitelistTag = new ListTag();
        for (Map.Entry<UUID, String> entry : this.whitelistedWitches.entrySet()) {
            CompoundTag witchTag = new CompoundTag();
            witchTag.putUUID("Uuid", entry.getKey());
            witchTag.putString("Name", entry.getValue());
            whitelistTag.add(witchTag);
        }
        tag.put("WhitelistedWitches", whitelistTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(
                "CarpetState",
                CompoundTag.TAG_COMPOUND
        )) {
            this.carpetState =
                    NbtUtils.readBlockState(
                            BuiltInRegistries.BLOCK.asLookup(),
                            tag.getCompound("CarpetState")
                    );
        } else {
            this.carpetState =
                    Blocks.AIR.defaultBlockState();
        }

        if (tag.contains(
                "DisplayedItems",
                CompoundTag.TAG_COMPOUND
        )) {
            this.items.deserializeNBT(
                    tag.getCompound("DisplayedItems")
            );
        }

        int[] savedAngles =
                tag.getIntArray("TrinketAngles");

        System.arraycopy(
                savedAngles,
                0,
                this.trinketAngles,
                0,
                Math.min(
                        savedAngles.length,
                        this.trinketAngles.length
                )
        );

        this.storedPower =
                tag.getInt("StoredPower");

        this.cachedNaturalDraw =
                tag.getInt("NaturalDraw");


        this.ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        this.ownerName = tag.getString("OwnerName");
        this.whitelistedWitches.clear();
        ListTag whitelistTag = tag.getList("WhitelistedWitches", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < whitelistTag.size(); i++) {
            CompoundTag witchTag = whitelistTag.getCompound(i);
            if (witchTag.hasUUID("Uuid")) {
                this.whitelistedWitches.put(witchTag.getUUID("Uuid"), witchTag.getString("Name"));
            }
        }
    }

    private void sendUpdate() {
        if (this.level == null) {
            return;
        }

        BlockState state =
                this.getBlockState();

        this.level.sendBlockUpdated(
                this.worldPosition,
                state,
                state,
                3
        );
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag =
                packet.getTag();

        if (tag != null) {
            this.load(tag);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                this.worldPosition
        ).inflate(
                2.0D,
                1.0D,
                2.0D
        );
    }


    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemCapability.cast();
        }

        return super.getCapability(
                capability,
                side
        );
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();

        this.itemCapability =
                LazyOptional.of(() -> this.items);
    }
}
