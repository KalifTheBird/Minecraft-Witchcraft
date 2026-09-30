package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.common.ForgeHooks;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.block.custom.WitchesOvenBlock;
import net.toxicteddie.witchcraft.menu.WitchesOvenMenu;
import net.toxicteddie.witchcraft.magic.MagicDeedManager;
import net.toxicteddie.witchcraft.magic.MagicDeedResult;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import net.toxicteddie.witchcraft.recipe.custom.WitchesOvenRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WitchesOvenBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BOTTLE_SLOT = 0;
    public static final int REAGENT_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
    public static final int BYPRODUCT_1_SLOT = 4;
    public static final int BYPRODUCT_2_SLOT = 5;
    public static final int BYPRODUCT_3_SLOT = 6;
    public static final int WATER_BUCKET_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    public static final int MAX_WATER = 64;
    public static final int DEFAULT_BREW_TIME = WitchesOvenRecipe.DEFAULT_BREW_TIME;

    private static final int BREWER_RANGE = 8;
    private static final int ALTAR_RANGE = 16;
    private static final int BODY_MAGIC_DRAW = 50;
    private static final float BODY_MAGIC_MAX_BONUS = 0.20F;
    private static final int ALTAR_MAGIC_DRAW = 100;
    private static final float ALTAR_MAGIC_MAX_BONUS = 0.25F;
    private static final float MAX_TINCTURE_CHANCE = 0.95F;

    private int waterLevel;
    private int brewProgress;
    private int burnTime;
    private int maxBurnTime;
    private int currentBrewTime = DEFAULT_BREW_TIME;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == WATER_BUCKET_SLOT ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case BOTTLE_SLOT -> stack.is(ModItems.TINCTURE_BOTTLE.get());
                case FUEL_SLOT -> ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
                case OUTPUT_SLOT, BYPRODUCT_1_SLOT, BYPRODUCT_2_SLOT, BYPRODUCT_3_SLOT -> false;
                case WATER_BUCKET_SLOT -> stack.is(Items.WATER_BUCKET);
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<ItemStackHandler> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> new RangedWrapper(items, 0, 3));
    private LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> new RangedWrapper(items, 3, 7));

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> waterLevel;
                case 1 -> brewProgress;
                case 2 -> burnTime;
                case 3 -> maxBurnTime;
                case 4 -> currentBrewTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> waterLevel = value;
                case 1 -> brewProgress = value;
                case 2 -> burnTime = value;
                case 3 -> maxBurnTime = value;
                case 4 -> currentBrewTime = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public WitchesOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCHES_OVEN_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.witchcraft.witches_oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WitchesOvenMenu(containerId, inventory, this, data);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WitchesOvenBlockEntity oven) {
        if (level.isClientSide()) return;

        boolean changed = oven.consumeWaterBucket();
        WitchesOvenRecipe recipe = oven.getRecipe();
        boolean canBrew = recipe != null && oven.waterLevel >= recipe.getWaterCost() && oven.canFit(recipe);
        oven.currentBrewTime = recipe == null ? DEFAULT_BREW_TIME : recipe.getBrewTime();

        if (oven.burnTime > 0) oven.burnTime--;

        if (canBrew && oven.burnTime <= 0) {
            ItemStack fuel = oven.items.getStackInSlot(FUEL_SLOT);
            int fuelTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
            if (fuelTime > 0) {
                oven.burnTime = fuelTime;
                oven.maxBurnTime = fuelTime;
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty()) oven.items.setStackInSlot(FUEL_SLOT, remainder);
                changed = true;
            }
        }

        if (canBrew && oven.burnTime > 0) {
            oven.brewProgress++;
            changed = true;
            if (oven.brewProgress >= oven.currentBrewTime) {
                oven.finishBrew(recipe);
                oven.brewProgress = 0;
            }
        } else if (oven.brewProgress != 0) {
            oven.brewProgress = 0;
            changed = true;
        }

        boolean isLit = oven.burnTime > 0;
        if (state.getValue(WitchesOvenBlock.LIT) != isLit) {
            level.setBlock(pos, state.setValue(WitchesOvenBlock.LIT, isLit), 3);
            changed = true;
        }

        if (changed) oven.setChanged();
    }

    private boolean consumeWaterBucket() {
        if (waterLevel >= MAX_WATER) return false;
        ItemStack stack = items.getStackInSlot(WATER_BUCKET_SLOT);
        if (!stack.is(Items.WATER_BUCKET)) return false;

        waterLevel++;
        items.setStackInSlot(WATER_BUCKET_SLOT, new ItemStack(Items.BUCKET));
        return true;
    }

    @Nullable
    private WitchesOvenRecipe getRecipe() {
        if (level == null) return null;
        SimpleContainer input = new SimpleContainer(2);
        input.setItem(0, items.getStackInSlot(BOTTLE_SLOT));
        input.setItem(1, items.getStackInSlot(REAGENT_SLOT));
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.WITCHES_OVEN_TYPE.get(), input, level)
                .orElse(null);
    }

    private boolean canFit(WitchesOvenRecipe recipe) {
        if (!canMerge(OUTPUT_SLOT, recipe.getOutput())) return false;
        var byproducts = recipe.getByproducts();
        for (int i = 0; i < 3; i++) {
            ItemStack stack = i < byproducts.size() ? byproducts.get(i) : ItemStack.EMPTY;
            if (!canMerge(BYPRODUCT_1_SLOT + i, stack)) return false;
        }
        return true;
    }

    private boolean canMerge(int slot, ItemStack addition) {
        if (addition.isEmpty()) return true;
        ItemStack existing = items.getStackInSlot(slot);
        return existing.isEmpty() || (ItemStack.isSameItemSameTags(existing, addition)
                && existing.getCount() + addition.getCount() <= existing.getMaxStackSize());
    }

    private void finishBrew(WitchesOvenRecipe recipe) {
        items.extractItem(REAGENT_SLOT, 1, false);
        waterLevel -= recipe.getWaterCost();


        float tinctureChance = getEmpoweredTinctureChance(recipe.getResultChance());
        boolean tinctureSucceeded = level != null && level.random.nextFloat() < tinctureChance;
        if (tinctureSucceeded) {
            items.extractItem(BOTTLE_SLOT, 1, false);
            mergeInto(OUTPUT_SLOT, recipe.getOutput());
        }

        var byproducts = recipe.getByproducts();
        for (int i = 0; i < byproducts.size() && i < 3; i++) {
            mergeInto(BYPRODUCT_1_SLOT + i, byproducts.get(i));
        }
    }


    private float getEmpoweredTinctureChance(float baseChance) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return baseChance;
        }

        ServerPlayer brewer = findNearestWitch(serverLevel);
        if (brewer == null) {
            return 0.0F;
        }

        int totalCost = BODY_MAGIC_DRAW + ALTAR_MAGIC_DRAW;
        MagicDeedResult payment = MagicDeedManager.perform(
                brewer,
                this.worldPosition,
                totalCost,
                ALTAR_RANGE
        );

        if (!payment.successful()) {
            return 0.0F;
        }

        float bodyContribution = Math.min(1.0F, payment.bodyUsed() / (float) BODY_MAGIC_DRAW);
        float altarContribution = Math.min(1.0F, payment.altarUsed() / (float) ALTAR_MAGIC_DRAW);

        float chance = baseChance
                + BODY_MAGIC_MAX_BONUS * bodyContribution
                + ALTAR_MAGIC_MAX_BONUS * altarContribution;

        return Math.min(MAX_TINCTURE_CHANCE, chance);
    }

    @Nullable
    private ServerPlayer findNearestWitch(ServerLevel serverLevel) {
        AABB searchArea = new AABB(this.worldPosition).inflate(BREWER_RANGE);
        return serverLevel.getEntitiesOfClass(
                        ServerPlayer.class,
                        searchArea,
                        player -> player.isAlive() && PlayerPathManager.isWitch(player)
                ).stream()
                .min(java.util.Comparator.comparingDouble(
                        player -> player.distanceToSqr(
                                this.worldPosition.getX() + 0.5D,
                                this.worldPosition.getY() + 0.5D,
                                this.worldPosition.getZ() + 0.5D
                        )
                ))
                .orElse(null);
    }

    private void mergeInto(int slot, ItemStack addition) {
        if (addition.isEmpty()) return;
        ItemStack existing = items.getStackInSlot(slot);
        if (existing.isEmpty()) items.setStackInSlot(slot, addition.copy());
        else existing.grow(addition.getCount());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putInt("WaterLevel", waterLevel);
        tag.putInt("BrewProgress", brewProgress);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putInt("CurrentBrewTime", currentBrewTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        waterLevel = Math.min(tag.getInt("WaterLevel"), MAX_WATER);
        brewProgress = tag.getInt("BrewProgress");
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        currentBrewTime = tag.contains("CurrentBrewTime") ? tag.getInt("CurrentBrewTime") : DEFAULT_BREW_TIME;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        itemHandler.invalidate();
        inputHandler.invalidate();
        outputHandler.invalidate();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) return outputHandler.cast();
            if (side != null) return inputHandler.cast();
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

}
