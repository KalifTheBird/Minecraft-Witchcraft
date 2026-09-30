package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import net.toxicteddie.witchcraft.recipe.custom.MortarAndPestleRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MortarAndPestleBlockEntity extends BlockEntity {

    public static final int SLOT_COUNT = 3;


    public static final int LIFT_AND_SETUP_TICKS = 25;


    public static final int ACTIVE_GRINDING_TICKS = 120;


    public static final int FINISH_AND_RETURN_TICKS = 24;

    public static final int GRINDING_DURATION =
            LIFT_AND_SETUP_TICKS
                    + ACTIVE_GRINDING_TICKS
                    + FINISH_AND_RETURN_TICKS;

    private final ItemStackHandler items =
            new ItemStackHandler(SLOT_COUNT) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }

                @Override
                protected void onContentsChanged(int slot) {
                    MortarAndPestleBlockEntity.this.setChanged();
                    MortarAndPestleBlockEntity.this.sendUpdate();
                }
            };

    private LazyOptional<ItemStackHandler> itemCapability =
            LazyOptional.of(() -> this.items);

    private final NonNullList<ItemStack> grindingIngredients =
            NonNullList.withSize(
                    SLOT_COUNT,
                    ItemStack.EMPTY
            );

    private ItemStack pendingResult =
            ItemStack.EMPTY;

    private boolean grinding;
    private long grindingStartTick;

    public MortarAndPestleBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.MORTAR_AND_PESTLE_BE.get(),
                pos,
                state
        );
    }

    public ItemStack getDisplayedItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        if (this.grinding) {
            return this.grindingIngredients.get(slot);
        }

        return this.items.getStackInSlot(slot);
    }

    public boolean insertIngredient(
            ItemStack heldStack,
            Player player
    ) {
        if (this.grinding || heldStack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!this.items
                    .getStackInSlot(slot)
                    .isEmpty()) {
                continue;
            }

            ItemStack inserted =
                    heldStack.copy();

            inserted.setCount(1);

            this.items.setStackInSlot(
                    slot,
                    inserted
            );

            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            this.playInsertSound();
            return true;
        }

        return false;
    }

    public boolean removeLastIngredient(
            Player player
    ) {
        if (this.grinding) {
            return false;
        }

        for (int slot = SLOT_COUNT - 1;
             slot >= 0;
             slot--) {

            ItemStack stack =
                    this.items.extractItem(
                            slot,
                            1,
                            false
                    );

            if (stack.isEmpty()) {
                continue;
            }

            if (!player.getInventory().add(stack)) {
                Containers.dropItemStack(
                        player.level(),
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.65D,
                        this.worldPosition.getZ() + 0.5D,
                        stack
                );
            }

            this.playRemoveSound();
            return true;
        }

        return false;
    }

    public boolean tryStartGrinding() {
        if (this.level == null
                || this.level.isClientSide()
                || this.grinding) {
            return false;
        }

        SimpleContainer container =
                this.createRecipeContainer();

        Optional<MortarAndPestleRecipe> recipe =
                this.level
                        .getRecipeManager()
                        .getRecipeFor(
                                ModRecipes
                                        .MORTAR_AND_PESTLE_TYPE
                                        .get(),
                                container,
                                this.level
                        );

        if (recipe.isEmpty()) {
            return false;
        }

        this.pendingResult =
                recipe.get()
                        .assemble(
                                container,
                                this.level.registryAccess()
                        )
                        .copy();

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            ItemStack removed =
                    this.items.extractItem(
                            slot,
                            1,
                            false
                    );

            this.grindingIngredients.set(
                    slot,
                    removed.copy()
            );
        }

        this.grinding = true;
        this.grindingStartTick =
                this.level.getGameTime();

        this.setChanged();
        this.sendUpdate();


        return true;
    }

    private SimpleContainer createRecipeContainer() {
        SimpleContainer container =
                new SimpleContainer(SLOT_COUNT);

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            container.setItem(
                    slot,
                    this.items
                            .getStackInSlot(slot)
                            .copy()
            );
        }

        return container;
    }

    public boolean isGrinding() {
        return this.grinding;
    }


    public float getGrindingElapsedTicks(
            float partialTick
    ) {
        if (!this.grinding || this.level == null) {
            return 0.0F;
        }

        float elapsed =
                this.level.getGameTime()
                        + partialTick
                        - this.grindingStartTick;

        return Mth.clamp(
                elapsed,
                0.0F,
                GRINDING_DURATION
        );
    }


    public float getGrindingProgress(
            float partialTick
    ) {
        return this.getGrindingElapsedTicks(partialTick)
                / GRINDING_DURATION;
    }

    public float getSetupProgress(
            float partialTick
    ) {
        return Mth.clamp(
                this.getGrindingElapsedTicks(partialTick)
                        / LIFT_AND_SETUP_TICKS,
                0.0F,
                1.0F
        );
    }

    public float getActiveGrindingProgress(
            float partialTick
    ) {
        float elapsed =
                this.getGrindingElapsedTicks(partialTick)
                        - LIFT_AND_SETUP_TICKS;

        return Mth.clamp(
                elapsed / ACTIVE_GRINDING_TICKS,
                0.0F,
                1.0F
        );
    }

    public float getFinishProgress(
            float partialTick
    ) {
        float elapsed =
                this.getGrindingElapsedTicks(partialTick)
                        - LIFT_AND_SETUP_TICKS
                        - ACTIVE_GRINDING_TICKS;

        return Mth.clamp(
                elapsed / FINISH_AND_RETURN_TICKS,
                0.0F,
                1.0F
        );
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            MortarAndPestleBlockEntity blockEntity
    ) {
        if (!blockEntity.grinding) {
            return;
        }

        long elapsed =
                level.getGameTime()
                        - blockEntity.grindingStartTick;

        long grindingStart =
                LIFT_AND_SETUP_TICKS;

        long grindingEnd =
                LIFT_AND_SETUP_TICKS
                        + ACTIVE_GRINDING_TICKS;


        boolean activelyGrinding =
                elapsed >= grindingStart
                        && elapsed < grindingEnd;

        if (activelyGrinding
                && level instanceof ServerLevel serverLevel) {

            long activeElapsed =
                    elapsed - grindingStart;

            if (activeElapsed % 3L == 0L) {
                blockEntity.spawnGrindingParticles(
                        serverLevel
                );
            }

            if (activeElapsed % 10L == 0L) {
                level.playSound(
                        null,
                        pos,
                        SoundEvents.GRINDSTONE_USE,
                        SoundSource.BLOCKS,
                        0.20F,
                        1.50F
                                + level.random.nextFloat()
                                * 0.16F
                );
            }


            if (activeElapsed % 20L == 5L) {
                level.playSound(
                        null,
                        pos,
                        SoundEvents.DEEPSLATE_HIT,
                        SoundSource.BLOCKS,
                        0.12F,
                        1.35F
                                + level.random.nextFloat()
                                * 0.10F
                );
            }
        }

        if (elapsed >= GRINDING_DURATION) {
            blockEntity.finishGrinding();
        }
    }

    private void spawnGrindingParticles(
            ServerLevel level
    ) {
        for (ItemStack stack
                : this.grindingIngredients) {

            if (stack.isEmpty()) {
                continue;
            }

            level.sendParticles(
                    new ItemParticleOption(
                            ParticleTypes.ITEM,
                            stack
                    ),
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.48D,
                    this.worldPosition.getZ() + 0.5D,
                    2,
                    0.08D,
                    0.035D,
                    0.08D,
                    0.015D
            );
        }
    }

    private void finishGrinding() {
        if (this.level == null
                || this.level.isClientSide()) {
            return;
        }

        if (!this.pendingResult.isEmpty()) {
            ItemEntity resultEntity =
                    new ItemEntity(
                            this.level,
                            this.worldPosition.getX() + 0.5D,
                            this.worldPosition.getY() + 0.72D,
                            this.worldPosition.getZ() + 0.5D,
                            this.pendingResult.copy()
                    );

            resultEntity.setDefaultPickUpDelay();

            resultEntity.setDeltaMovement(
                    (this.level.random.nextDouble() - 0.5D)
                            * 0.08D,
                    0.18D,
                    (this.level.random.nextDouble() - 0.5D)
                            * 0.08D
            );

            this.level.addFreshEntity(
                    resultEntity
            );
        }

        this.level.playSound(
                null,
                this.worldPosition,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                0.65F,
                1.65F
        );

        this.level.playSound(
                null,
                this.worldPosition,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                0.35F,
                1.75F
        );

        this.pendingResult =
                ItemStack.EMPTY;

        this.grindingIngredients.replaceAll(
                stack -> ItemStack.EMPTY
        );

        this.grinding = false;

        this.setChanged();
        this.sendUpdate();
    }

    private void playInsertSound() {
        if (this.level == null) {
            return;
        }

        this.level.playSound(
                null,
                this.worldPosition,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                0.35F,
                1.45F
        );
    }

    private void playRemoveSound() {
        if (this.level == null) {
            return;
        }

        this.level.playSound(
                null,
                this.worldPosition,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                0.45F,
                0.85F
        );
    }

    public void dropAllContents() {
        if (this.level == null) {
            return;
        }

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            ItemStack stored =
                    this.items.extractItem(
                            slot,
                            1,
                            false
                    );

            if (!stored.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D,
                        stored
                );
            }

            ItemStack grindingStack =
                    this.grindingIngredients.get(slot);

            if (!grindingStack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D,
                        grindingStack.copy()
                );

                this.grindingIngredients.set(
                        slot,
                        ItemStack.EMPTY
                );
            }
        }

        this.pendingResult =
                ItemStack.EMPTY;

        this.grinding = false;
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
    protected void saveAdditional(
            CompoundTag tag
    ) {
        super.saveAdditional(tag);

        tag.put(
                "Items",
                this.items.serializeNBT()
        );

        tag.putBoolean(
                "Grinding",
                this.grinding
        );

        tag.putLong(
                "GrindingStartTick",
                this.grindingStartTick
        );

        tag.put(
                "PendingResult",
                this.pendingResult.save(
                        new CompoundTag()
                )
        );

        ListTag grindingList =
                new ListTag();

        for (int slot = 0;
             slot < SLOT_COUNT;
             slot++) {

            CompoundTag stackTag =
                    new CompoundTag();

            stackTag.putByte(
                    "Slot",
                    (byte) slot
            );

            this.grindingIngredients
                    .get(slot)
                    .save(stackTag);

            grindingList.add(stackTag);
        }

        tag.put(
                "GrindingIngredients",
                grindingList
        );
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.items.deserializeNBT(
                tag.getCompound("Items")
        );

        this.grinding =
                tag.getBoolean("Grinding");

        this.grindingStartTick =
                tag.getLong("GrindingStartTick");

        this.pendingResult =
                ItemStack.of(
                        tag.getCompound(
                                "PendingResult"
                        )
                );

        this.grindingIngredients.replaceAll(
                stack -> ItemStack.EMPTY
        );

        ListTag grindingList =
                tag.getList(
                        "GrindingIngredients",
                        Tag.TAG_COMPOUND
                );

        for (int index = 0;
             index < grindingList.size();
             index++) {

            CompoundTag stackTag =
                    grindingList.getCompound(index);

            int slot =
                    stackTag.getByte("Slot")
                            & 255;

            if (slot >= 0 && slot < SLOT_COUNT) {
                this.grindingIngredients.set(
                        slot,
                        ItemStack.of(stackTag)
                );
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(
            CompoundTag tag
    ) {
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable net.minecraft.core.Direction side
    ) {
        if (capability
                == ForgeCapabilities.ITEM_HANDLER) {
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
