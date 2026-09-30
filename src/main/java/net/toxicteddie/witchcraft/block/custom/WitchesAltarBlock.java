package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.toxicteddie.witchcraft.block.entity.ModBlockEntities;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;
import net.toxicteddie.witchcraft.path.PathMessages;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.wand.AltarDrawManager;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WitchesAltarBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public static final EnumProperty<WitchesAltarPart> PART =
            EnumProperty.create(
                    "part",
                    WitchesAltarPart.class
            );


    private static final VoxelShape CENTER_NORTH =
            box(
                    0.0D, 13.0D, 2.0D,
                    16.0D, 16.0D, 16.0D
            );

    private static final VoxelShape LEFT_NORTH =
            box(
                    8.0D, 0.0D, 2.0D,
                    16.0D, 16.0D, 16.0D
            );

    private static final VoxelShape RIGHT_NORTH =
            box(
                    0.0D, 0.0D, 2.0D,
                    8.0D, 16.0D, 16.0D
            );

    private static final VoxelShape CENTER_EAST =
            box(
                    0.0D, 13.0D, 0.0D,
                    14.0D, 16.0D, 16.0D
            );

    private static final VoxelShape LEFT_EAST =
            box(
                    0.0D, 0.0D, 8.0D,
                    14.0D, 16.0D, 16.0D
            );

    private static final VoxelShape RIGHT_EAST =
            box(
                    0.0D, 0.0D, 0.0D,
                    14.0D, 16.0D, 8.0D
            );

    private static final VoxelShape CENTER_SOUTH =
            box(
                    0.0D, 13.0D, 0.0D,
                    16.0D, 16.0D, 14.0D
            );

    private static final VoxelShape LEFT_SOUTH =
            box(
                    0.0D, 0.0D, 0.0D,
                    8.0D, 16.0D, 14.0D
            );

    private static final VoxelShape RIGHT_SOUTH =
            box(
                    8.0D, 0.0D, 0.0D,
                    16.0D, 16.0D, 14.0D
            );

    private static final VoxelShape CENTER_WEST =
            box(
                    2.0D, 13.0D, 0.0D,
                    16.0D, 16.0D, 16.0D
            );

    private static final VoxelShape LEFT_WEST =
            box(
                    2.0D, 0.0D, 0.0D,
                    16.0D, 16.0D, 8.0D
            );

    private static final VoxelShape RIGHT_WEST =
            box(
                    2.0D, 0.0D, 8.0D,
                    16.0D, 16.0D, 16.0D
            );


    private static final Vec3[] ALTAR_SLOT_POSITIONS = {


            new Vec3(0.10D, 1.00D, 0.75D),
            new Vec3(0.90D, 1.00D, 0.75D),


            new Vec3(0.50D, 1.05D, 0.30D),
            new Vec3(0.80D, 1.05D, 0.43D),
            new Vec3(1.25D, 1.05D, 0.60D),
            new Vec3(0.20D, 1.05D, 0.43D),
            new Vec3(-0.25D, 1.00D, 0.60D)
    };

    private static final double SLOT_CLICK_DISTANCE_SQR =
            0.30D * 0.30D;

    public WitchesAltarBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(
                                PART,
                                WitchesAltarPart.CENTER
                        )
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        if (state.getValue(PART)
                != WitchesAltarPart.CENTER) {
            return null;
        }

        return new WitchesAltarBlockEntity(
                pos,
                state
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide()
                || state.getValue(PART)
                != WitchesAltarPart.CENTER) {
            return null;
        }

        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.WITCHES_ALTAR_BE.get(),
                WitchesAltarBlockEntity::serverTick
        );
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        Level level =
                context.getLevel();

        BlockPos centerPos =
                context.getClickedPos();

        Direction facing =
                context.getHorizontalDirection()
                        .getOpposite();

        BlockPos leftPos =
                getLeftPos(centerPos, facing);

        BlockPos rightPos =
                getRightPos(centerPos, facing);

        if (!canReplace(level, centerPos, context)
                || !canReplace(level, leftPos, context)
                || !canReplace(level, rightPos, context)) {
            return null;
        }

        if (!hasSupport(level, centerPos)
                || !hasSupport(level, leftPos)
                || !hasSupport(level, rightPos)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(
                        PART,
                        WitchesAltarPart.CENTER
                );
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos centerPos,
            BlockState centerState,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(
                level,
                centerPos,
                centerState,
                placer,
                stack
        );

        if (level.isClientSide()) {
            return;
        }

        Direction facing =
                centerState.getValue(FACING);

        level.setBlock(
                getLeftPos(centerPos, facing),
                centerState.setValue(
                        PART,
                        WitchesAltarPart.LEFT
                ),
                3
        );

        level.setBlock(
                getRightPos(centerPos, facing),
                centerState.setValue(
                        PART,
                        WitchesAltarPart.RIGHT
                ),
                3
        );
    }


    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        BlockPos centerPos =
                getCenterPos(
                        pos,
                        state
                );

        BlockState centerState =
                level.getBlockState(centerPos);

        if (!centerState.is(this)
                || centerState.getValue(PART)
                != WitchesAltarPart.CENTER) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(centerPos)
                instanceof WitchesAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack =
                player.getItemInHand(hand);


        if (heldStack.getItem()
                instanceof BlockItem blockItem) {

            BlockState heldBlockState =
                    blockItem.getBlock()
                            .defaultBlockState();

            if (isValidAltarCarpet(heldBlockState)) {
                if (!level.isClientSide()) {
                    if (altar.hasCarpet()) {
                        BlockState oldCarpet =
                                altar.getCarpetState();

                        if (oldCarpet.is(
                                heldBlockState.getBlock()
                        )) {
                            return InteractionResult.CONSUME;
                        }

                        if (!player.isCreative()) {
                            giveOrDropItem(
                                    level,
                                    centerPos,
                                    player,
                                    new ItemStack(
                                            oldCarpet.getBlock()
                                    )
                            );
                        }
                    }

                    altar.setCarpetState(
                            heldBlockState
                    );

                    if (!player.isCreative()) {
                        heldStack.shrink(1);
                    }
                }

                return InteractionResult.sidedSuccess(
                        level.isClientSide()
                );
            }
        }


        if (player.isShiftKeyDown()
                && heldStack.isEmpty()
                && altar.hasCarpet()) {

            if (!level.isClientSide()) {
                BlockState removedCarpet =
                        altar.removeCarpet();

                if (!player.isCreative()
                        && !removedCarpet.isAir()) {

                    giveOrDropItem(
                            level,
                            centerPos,
                            player,
                            new ItemStack(
                                    removedCarpet.getBlock()
                            )
                    );
                }
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide()
            );
        }


        if (!altar.hasCarpet()) {
            if (level.isClientSide()) {
                PathMessages.sendBareAltar(
                        player
                );
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide()
            );
        }


        if (!PlayerPathManager.canUseAltarMagic(player)) {
            if (level.isClientSide()) {
                PathMessages.sendAltarUnreadable(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && !altar.hasOwner()) {
            altar.claimOwnership(player);
            player.displayClientMessage(
                    Component.literal("The altar recognizes you as its witch."),
                    true
            );
        }


        if (heldStack.getItem() instanceof BloodBottleItem) {
            if (!level.isClientSide()) {
                if (!altar.isOwner(player)) {
                    player.displayClientMessage(
                            Component.literal("Only the altar's owner can form a blood binding."),
                            true
                    );
                    return InteractionResult.CONSUME;
                }

                var bloodTag = heldStack.getTag();
                if (bloodTag == null || !bloodTag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)) {
                    player.displayClientMessage(
                            Component.literal("Only a witch's blood can be bound to this altar."),
                            true
                    );
                    return InteractionResult.CONSUME;
                }

                String uuidText = bloodTag.getString(BloodBottleItem.TAG_ENTITY_UUID);
                try {
                    UUID witchUuid = UUID.fromString(uuidText);
                    String witchName = bloodTag.getString(BloodBottleItem.TAG_NAME);
                    if (altar.bindWitch(player, witchUuid, witchName)) {
                        player.displayClientMessage(
                                Component.literal(witchName + " is now blood-bound to this altar."),
                                true
                        );
                    } else {
                        player.displayClientMessage(
                                Component.literal("That blood cannot be bound to this altar."),
                                true
                        );
                    }
                } catch (IllegalArgumentException ignored) {
                    player.displayClientMessage(
                            Component.literal("The blood has lost its identity."),
                            true
                    );
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && !altar.isAuthorized(player)) {
            player.displayClientMessage(
                    Component.literal("This altar is bound to another witch."),
                    true
            );
            return InteractionResult.CONSUME;
        }


        if (heldStack.getItem() instanceof WandItem) {

            if (PlayerMagicManager.isMagicallyExhausted(player)) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.literal("Your exhausted body cannot draw magic."),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (altar.getStoredPower() <= 0) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.literal("The altar has no magic to give."),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                if (!(player instanceof ServerPlayer serverPlayer)
                        || !AltarDrawManager.begin(serverPlayer, centerPos, hand)) {
                    return InteractionResult.CONSUME;
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        Direction facing =
                centerState.getValue(FACING);

        int clickedSlot =
                getClickedAltarSlot(
                        centerPos,
                        facing,
                        hit.getLocation()
                );


        if (!heldStack.isEmpty()) {
            boolean candle =
                    WitchesAltarBlockEntity
                            .isValidCandle(heldStack);

            int targetSlot =
                    findClosestSlotOfType(
                            centerPos,
                            facing,
                            hit.getLocation(),
                            candle
                    );

            if (targetSlot < 0) {
                if (level.isClientSide()) {
                    PathMessages.sendWrongAltarSlot(
                            player
                    );
                }

                return InteractionResult.sidedSuccess(
                        level.isClientSide()
                );
            }

            if (!level.isClientSide()) {
                boolean placed =
                        altar.placeDisplayedItem(
                                targetSlot,
                                heldStack,
                                player
                        );

                if (!placed) {
                    PathMessages.sendWrongAltarSlot(
                            player
                    );
                }
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide()
            );
        }


        if (clickedSlot >= 0
                && !altar.isSlotEmpty(clickedSlot)) {


            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    altar.removeDisplayedItem(
                            clickedSlot,
                            player
                    );
                }

                return InteractionResult.sidedSuccess(
                        level.isClientSide()
                );
            }


            if (WitchesAltarBlockEntity
                    .isTrinketSlot(clickedSlot)) {

                if (!level.isClientSide()) {
                    altar.rotateTrinket(
                            clickedSlot
                    );
                }

                return InteractionResult.sidedSuccess(
                        level.isClientSide()
                );
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide()
            );
        }

        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer) {

            NetworkHooks.openScreen(
                    serverPlayer,
                    altar,
                    buffer -> {
                        buffer.writeBlockPos(centerPos);
                        buffer.writeBoolean(altar.isOwner(serverPlayer));
                        buffer.writeUtf(altar.getOwnerName());
                        var witches = altar.getWhitelistedWitches();
                        buffer.writeVarInt(witches.size());
                        for (var witch : witches) {
                            buffer.writeUUID(witch.getKey());
                            buffer.writeUtf(witch.getValue());
                        }
                    }
            );
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide()
        );
    }


    @Override
    public void playerWillDestroy(
            Level level,
            BlockPos brokenPos,
            BlockState brokenState,
            Player player
    ) {
        if (level.isClientSide()) {
            super.playerWillDestroy(
                    level,
                    brokenPos,
                    brokenState,
                    player
            );

            return;
        }

        BlockPos centerPos =
                getCenterPos(
                        brokenPos,
                        brokenState
                );

        BlockState centerState =
                level.getBlockState(centerPos);

        if (!centerState.is(this)
                || centerState.getValue(PART)
                != WitchesAltarPart.CENTER) {

            level.removeBlock(
                    brokenPos,
                    false
            );

            return;
        }

        BlockState installedCarpet =
                null;

        if (level.getBlockEntity(centerPos)
                instanceof WitchesAltarBlockEntity altar) {

            if (altar.hasCarpet()) {
                installedCarpet =
                        altar.getCarpetState();
            }

            altar.dropAllDisplayedItems();
        }

        Direction facing =
                centerState.getValue(FACING);

        removePart(
                level,
                getLeftPos(centerPos, facing)
        );

        removePart(
                level,
                getRightPos(centerPos, facing)
        );

        removePart(
                level,
                centerPos
        );

        if (!player.isCreative()) {
            popResource(
                    level,
                    centerPos,
                    new ItemStack(this)
            );

            if (installedCarpet != null
                    && !installedCarpet.isAir()) {

                popResource(
                        level,
                        centerPos,
                        new ItemStack(
                                installedCarpet.getBlock()
                        )
                );
            }
        }
    }


    @Override
    public BlockState updateShape(
            BlockState state,
            Direction changedSide,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!isCompleteStructure(
                level,
                pos,
                state
        )) {
            return net.minecraft.world.level.block.Blocks.AIR
                    .defaultBlockState();
        }

        return super.updateShape(
                state,
                changedSide,
                neighborState,
                level,
                pos,
                neighborPos
        );
    }

    private boolean isCompleteStructure(
            LevelAccessor level,
            BlockPos currentPos,
            BlockState currentState
    ) {
        WitchesAltarPart currentPart =
                currentState.getValue(PART);

        Direction currentFacing =
                currentState.getValue(FACING);


        if (currentPart == WitchesAltarPart.CENTER) {
            BlockPos leftPos =
                    getLeftPos(
                            currentPos,
                            currentFacing
                    );

            BlockPos rightPos =
                    getRightPos(
                            currentPos,
                            currentFacing
                    );

            boolean leftMissing =
                    !level.getBlockState(leftPos)
                            .is(this);

            boolean rightMissing =
                    !level.getBlockState(rightPos)
                            .is(this);

            if (leftMissing && rightMissing) {
                return true;
            }
        }

        BlockPos centerPos =
                getCenterPos(
                        currentPos,
                        currentState
                );

        BlockState centerState =
                level.getBlockState(centerPos);

        if (!centerState.is(this)
                || centerState.getValue(PART)
                != WitchesAltarPart.CENTER) {
            return false;
        }

        Direction facing =
                centerState.getValue(FACING);

        BlockState leftState =
                level.getBlockState(
                        getLeftPos(centerPos, facing)
                );

        BlockState rightState =
                level.getBlockState(
                        getRightPos(centerPos, facing)
                );

        return leftState.is(this)
                && leftState.getValue(PART)
                == WitchesAltarPart.LEFT
                && leftState.getValue(FACING)
                == facing

                && rightState.is(this)
                && rightState.getValue(PART)
                == WitchesAltarPart.RIGHT
                && rightState.getValue(FACING)
                == facing;
    }


    private static int getClickedAltarSlot(
            BlockPos centerPos,
            Direction facing,
            Vec3 worldHit
    ) {
        Vec3 relativeHit = new Vec3(
                worldHit.x - centerPos.getX(),
                worldHit.y - centerPos.getY(),
                worldHit.z - centerPos.getZ()
        );

        int closestSlot = -1;
        double closestDistance = SLOT_CLICK_DISTANCE_SQR;

        for (int slot = 0;
             slot < ALTAR_SLOT_POSITIONS.length;
             slot++) {

            Vec3 visibleSlotPosition = rotateSlotPosition(
                    ALTAR_SLOT_POSITIONS[slot],
                    facing
            );

            double deltaX =
                    relativeHit.x - visibleSlotPosition.x;

            double deltaZ =
                    relativeHit.z - visibleSlotPosition.z;

            double distance =
                    deltaX * deltaX + deltaZ * deltaZ;

            if (distance < closestDistance) {
                closestDistance = distance;
                closestSlot = slot;
            }
        }

        return closestSlot;
    }

    private static int findClosestSlotOfType(
            BlockPos centerPos,
            Direction facing,
            Vec3 worldHit,
            boolean candle
    ) {
        Vec3 relativeHit = new Vec3(
                worldHit.x - centerPos.getX(),
                worldHit.y - centerPos.getY(),
                worldHit.z - centerPos.getZ()
        );

        int start =
                candle
                        ? WitchesAltarBlockEntity.CANDLE_LEFT_SLOT
                        : WitchesAltarBlockEntity.FIRST_TRINKET_SLOT;

        int end =
                candle
                        ? WitchesAltarBlockEntity.CANDLE_RIGHT_SLOT
                        : WitchesAltarBlockEntity.LAST_TRINKET_SLOT;

        int closestSlot = -1;
        double closestDistance = Double.MAX_VALUE;

        for (int slot = start;
             slot <= end;
             slot++) {

            Vec3 visibleSlotPosition = rotateSlotPosition(
                    ALTAR_SLOT_POSITIONS[slot],
                    facing
            );

            double deltaX =
                    relativeHit.x - visibleSlotPosition.x;

            double deltaZ =
                    relativeHit.z - visibleSlotPosition.z;

            double distance =
                    deltaX * deltaX + deltaZ * deltaZ;

            if (distance < closestDistance) {
                closestDistance = distance;
                closestSlot = slot;
            }
        }

        return closestSlot;
    }


    private static Vec3 rotateSlotPosition(
            Vec3 position,
            Direction facing
    ) {
        double x = position.x;
        double y = position.y;


        double z =
                facing == Direction.EAST
                        || facing == Direction.WEST
                        ? 1.0D - position.z
                        : position.z;

        return switch (facing) {
            case NORTH ->
                    new Vec3(x, y, z);

            case SOUTH ->
                    new Vec3(
                            1.0D - x,
                            y,
                            1.0D - z
                    );

            case EAST ->
                    new Vec3(
                            z,
                            y,
                            1.0D - x
                    );

            case WEST ->
                    new Vec3(
                            1.0D - z,
                            y,
                            x
                    );

            default ->
                    new Vec3(x, y, z);
        };
    }


    private static boolean isValidAltarCarpet(
            BlockState state
    ) {
        return state.is(
                ModTags.Blocks.ALTAR_CARPETS
        ) || state.getBlock() instanceof CarpetBlock;
    }

    private static void giveOrDropItem(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return;
        }

        if (!player.getInventory().add(stack)) {
            popResource(
                    level,
                    pos,
                    stack
            );
        }
    }

    private void removePart(
            Level level,
            BlockPos pos
    ) {
        if (level.getBlockState(pos).is(this)) {
            level.removeBlock(
                    pos,
                    false
            );
        }
    }

    private static boolean canReplace(
            Level level,
            BlockPos pos,
            BlockPlaceContext context
    ) {
        BlockState state =
                level.getBlockState(pos);

        return state.isAir()
                || state.canBeReplaced(context);
    }

    private static boolean hasSupport(
            BlockGetter level,
            BlockPos pos
    ) {
        BlockPos below =
                pos.below();

        return level.getBlockState(below)
                .isFaceSturdy(
                        level,
                        below,
                        Direction.UP
                );
    }


    public static BlockPos getLeftPos(
            BlockPos centerPos,
            Direction facing
    ) {
        return centerPos.relative(
                facing.getCounterClockWise()
        );
    }

    public static BlockPos getRightPos(
            BlockPos centerPos,
            Direction facing
    ) {
        return centerPos.relative(
                facing.getClockWise()
        );
    }

    public static BlockPos getCenterPos(
            BlockPos currentPos,
            BlockState currentState
    ) {
        Direction facing =
                currentState.getValue(FACING);

        return switch (
                currentState.getValue(PART)
                ) {
            case CENTER ->
                    currentPos;

            case LEFT ->
                    currentPos.relative(
                            facing.getClockWise()
                    );

            case RIGHT ->
                    currentPos.relative(
                            facing.getCounterClockWise()
                    );
        };
    }


    private static VoxelShape getAltarShape(
            BlockState state
    ) {
        Direction facing =
                state.getValue(FACING);

        WitchesAltarPart part =
                state.getValue(PART);

        return switch (facing) {
            case NORTH -> switch (part) {
                case CENTER -> CENTER_NORTH;
                case LEFT -> LEFT_NORTH;
                case RIGHT -> RIGHT_NORTH;
            };

            case EAST -> switch (part) {
                case CENTER -> CENTER_EAST;
                case LEFT -> LEFT_EAST;
                case RIGHT -> RIGHT_EAST;
            };

            case SOUTH -> switch (part) {
                case CENTER -> CENTER_SOUTH;
                case LEFT -> LEFT_SOUTH;
                case RIGHT -> RIGHT_SOUTH;
            };

            case WEST -> switch (part) {
                case CENTER -> CENTER_WEST;
                case LEFT -> LEFT_WEST;
                case RIGHT -> RIGHT_WEST;
            };

            default ->
                    CENTER_NORTH;
        };
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getAltarShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getAltarShape(state);
    }

    @Override
    public BlockState rotate(
            BlockState state,
            Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    public BlockState mirror(
            BlockState state,
            Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                FACING,
                PART
        );
    }
}
