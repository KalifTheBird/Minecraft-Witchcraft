package net.toxicteddie.witchcraft.entity.custom;

import net.toxicteddie.witchcraft.book.BookPageTurnTiming;
import net.toxicteddie.witchcraft.book.BookContentLayout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.custom.BookOfShadowsBlock;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeManager;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;

import java.util.Optional;
import java.util.UUID;


public class BookOfShadowsFollowingEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> COMMAND_MODE =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> READER_ACTIVE =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> READER_PAGE =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PAGE_TURN_SERIAL =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PAGE_TURN_DIRECTION =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> CONTENT_SOURCE_POS =
            SynchedEntityData.defineId(BookOfShadowsFollowingEntity.class, EntityDataSerializers.LONG);


    private static final double FOLLOW_WAKE_DISTANCE = 4.0D;
    private static final double FOLLOW_SETTLE_DISTANCE = 1.10D;


    private static final double ARRIVAL_LATCH_DISTANCE = 2.35D;
    private static final double ARRIVAL_RELEASE_DISTANCE = 3.15D;
    private static final double ARRIVAL_OWNER_SPEED = 0.095D;


    private static final double TARGET_FORWARD = 0.48D;
    private static final double TARGET_SIDE = 1.18D;
    private static final double TRAIL_BEHIND = 1.45D;
    private static final double TRAIL_SIDE = 0.82D;
    private static final double OWNER_MOVING_THRESHOLD = 0.075D;
    private static final double TARGET_SMOOTHING = 0.12D;
    private static final double FOLLOW_POSITION_GAIN = 0.18D;
    private static final double FOLLOW_VELOCITY_GAIN = 0.42D;
    private static final double FOLLOW_MAX_ACCEL = 0.035D;
    private static final double FOLLOW_MAX_SPEED = 0.24D;


    private static final int LANDING_SEARCH_RADIUS = 6;
    private static final int LANDING_VERTICAL_RADIUS = 4;
    private static final double RETURN_SPRING = 0.034D;
    private static final double RETURN_DAMPING = 0.86D;
    private static final double RETURN_MAX_SPEED = 0.26D;
    private static final double RETURN_FINISH_DISTANCE = 0.10D;
    private static final double RETURN_FINISH_SPEED = 0.035D;


    private static final int COMMAND_SEARCH_RADIUS = 64;
    private static final double COMMAND_SPRING = 0.030D;
    private static final double COMMAND_DAMPING = 0.89D;
    private static final double COMMAND_MAX_SPEED = 0.25D;


    private static final float WAND_PICK_FORGIVENESS = 0.35F;

    public final AnimationState floatingIdleAnimation = new AnimationState();
    public final AnimationState pageTurnAnimationState = new AnimationState();
    private int clientSeenPageTurnSerial = -1;
    private int clientPageTurnTicks = 0;

    private boolean catchingUp;
    private boolean returningToSurface;
    private boolean returningToCommandSurface;
    private BlockPos landingPos;


    private Direction commandLandingFacing;
    private UUID boundBookOwnerUUID;
    private String boundBookOwnerName = "";
    private boolean clientAnimationStarted;
    private Vec3 smoothedFollowTarget;


    private Vec3 latchedArrivalTarget;


    private double preferredSideSign = 1.0D;


    private int clientLerpSteps;
    private double clientLerpX;
    private double clientLerpY;
    private double clientLerpZ;
    private float clientLerpYRot;
    private float clientLerpXRot;

    public BookOfShadowsFollowingEntity(EntityType<? extends BookOfShadowsFollowingEntity> type, Level level) {
        super(type, level);
        noPhysics = true;


        floatingIdleAnimation.start(0);
        clientAnimationStarted = true;
    }


    public void ensureFloatingAnimationStarted() {
        if (!floatingIdleAnimation.isStarted()) {
            floatingIdleAnimation.start(Math.max(0, tickCount));
        }
        clientAnimationStarted = true;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        if (teleport || position().distanceToSqr(new Vec3(x, y, z)) > 64.0D) {
            setPos(x, y, z);
            setRot(yRot, xRot);
            clientLerpSteps = 0;
            return;
        }

        clientLerpX = x;
        clientLerpY = y;
        clientLerpZ = z;
        clientLerpYRot = yRot;
        clientLerpXRot = xRot;

        clientLerpSteps = Math.max(2, steps);
    }

    private void tickClientInterpolation() {
        if (clientLerpSteps <= 0) {
            return;
        }

        double t = 1.0D / clientLerpSteps;
        double x = Mth.lerp(t, getX(), clientLerpX);
        double y = Mth.lerp(t, getY(), clientLerpY);
        double z = Mth.lerp(t, getZ(), clientLerpZ);
        float yaw = getYRot() + Mth.wrapDegrees(clientLerpYRot - getYRot()) * (float) t;
        float pitch = Mth.lerp((float) t, getXRot(), clientLerpXRot);

        setPos(x, y, z);
        setRot(yaw, pitch);
        clientLerpSteps--;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(COMMAND_MODE, false);
        entityData.define(READER_ACTIVE, false);
        entityData.define(READER_PAGE, 0);
        entityData.define(PAGE_TURN_SERIAL, 0);
        entityData.define(PAGE_TURN_DIRECTION, 1);
        entityData.define(CONTENT_SOURCE_POS, BlockPos.ZERO.asLong());
    }

    public void setOwner(Player player) {
        entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
    }

    public void setBookBinding(UUID owner, String ownerName) {
        this.boundBookOwnerUUID = owner;
        this.boundBookOwnerName = ownerName == null ? "" : ownerName;
    }

    public boolean isBookBound() { return boundBookOwnerUUID != null; }

    public boolean canReadBoundBook(Player player) {
        return boundBookOwnerUUID != null && boundBookOwnerUUID.equals(player.getUUID());
    }


    public void setReaderState(BlockPos sourcePos, boolean active, int page) {
        entityData.set(CONTENT_SOURCE_POS, sourcePos.asLong());
        entityData.set(READER_ACTIVE, active);
        entityData.set(READER_PAGE, Math.max(0, page & ~1));
    }

    public boolean isReaderActive() { return entityData.get(READER_ACTIVE); }
    public int getReaderPage() { return entityData.get(READER_PAGE); }
    public BlockPos getContentSourcePos() { return BlockPos.of(entityData.get(CONTENT_SOURCE_POS)); }
    public int getPageTurnDirection() { return entityData.get(PAGE_TURN_DIRECTION); }
    public boolean isPageTurning() { return clientPageTurnTicks > 0 && pageTurnAnimationState.isStarted(); }
    public float getClientPageTurnProgress() { return isPageTurning() ? BookPageTurnTiming.progress(pageTurnAnimationState) : 1.0F; }

    public void turnReaderPage(int direction) {
        int d = direction < 0 ? -1 : 1;
        entityData.set(READER_ACTIVE, true);
        entityData.set(READER_PAGE, Math.max(0, getReaderPage() + d * 2));
        entityData.set(PAGE_TURN_DIRECTION, d);
        entityData.set(PAGE_TURN_SERIAL, entityData.get(PAGE_TURN_SERIAL) + 1);
    }

    public void jumpReaderPage(int targetPage) {
        int current = getReaderPage();
        int targetSpread = Math.max(0, targetPage & ~1);
        if (targetSpread == current) return;
        entityData.set(READER_ACTIVE, true);
        entityData.set(READER_PAGE, targetSpread);
        entityData.set(PAGE_TURN_DIRECTION, targetSpread > current ? 1 : -1);
        entityData.set(PAGE_TURN_SERIAL, entityData.get(PAGE_TURN_SERIAL) + 1);
    }

    private void ensureReadableSnapshot(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        entityData.set(READER_ACTIVE, true);
        ModNetworking.sendToPlayer(canReadBoundBook(player)
                ? OpenBookOfShadowsPacket.forPlayer(serverPlayer, getContentSourcePos())
                : OpenBookOfShadowsPacket.gibberish(getContentSourcePos()), serverPlayer);
    }

    public Optional<UUID> getOwnerUUID() {
        return entityData.get(OWNER_UUID);
    }

    public boolean isCommandMode() {
        return entityData.get(COMMAND_MODE);
    }

    public void setCommandMode(boolean commandMode) {
        entityData.set(COMMAND_MODE, commandMode);
    }

    public boolean isOwnedBy(Player player) {
        return getOwnerUUID().map(player.getUUID()::equals).orElse(false);
    }


    public static BookOfShadowsFollowingEntity findReadableBook(Level level, Player player, BlockPos sourcePos) {
        AABB box = new AABB(player.blockPosition()).inflate(COMMAND_SEARCH_RADIUS);
        BookOfShadowsFollowingEntity nearest = null;
        double nearestSqr = Double.MAX_VALUE;
        for (BookOfShadowsFollowingEntity book : level.getEntitiesOfClass(
                BookOfShadowsFollowingEntity.class, box,
                candidate -> candidate.isOwnedBy(player)
                        && candidate.isReaderActive()
                        && candidate.getContentSourcePos().equals(sourcePos))) {
            double distanceSqr = book.distanceToSqr(player);
            if (distanceSqr < nearestSqr) {
                nearestSqr = distanceSqr;
                nearest = book;
            }
        }
        return nearest;
    }

    public static BookOfShadowsFollowingEntity findCommandBook(Level level, Player player) {
        AABB box = new AABB(player.blockPosition()).inflate(COMMAND_SEARCH_RADIUS);
        BookOfShadowsFollowingEntity nearest = null;
        double nearestSqr = Double.MAX_VALUE;

        for (BookOfShadowsFollowingEntity book :
                level.getEntitiesOfClass(BookOfShadowsFollowingEntity.class, box,
                        candidate -> candidate.isCommandMode() && candidate.isOwnedBy(player)
                                && !candidate.returningToSurface && !candidate.returningToCommandSurface)) {
            double distanceSqr = book.distanceToSqr(player);
            if (distanceSqr < nearestSqr) {
                nearestSqr = distanceSqr;
                nearest = book;
            }
        }
        return nearest;
    }

    public Player getOwner() {
        Optional<UUID> uuid = getOwnerUUID();
        if (uuid.isEmpty()) {
            return null;
        }

        if (level() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(uuid.get());
        }

        return level().getPlayerByUUID(uuid.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            int serial = entityData.get(PAGE_TURN_SERIAL);
            if (serial != clientSeenPageTurnSerial) {
                clientSeenPageTurnSerial = serial;
                if (serial > 0) {
                    pageTurnAnimationState.stop();
                    pageTurnAnimationState.start(tickCount);
                    clientPageTurnTicks = BookPageTurnTiming.TURN_TICKS;
                }
            }
            if (clientPageTurnTicks > 0) {
                clientPageTurnTicks--;
                if (clientPageTurnTicks <= 0) pageTurnAnimationState.stop();
            }
        }
        noPhysics = true;
        setNoGravity(true);

        if (level().isClientSide) {
            tickClientInterpolation();
            if (!clientAnimationStarted || !floatingIdleAnimation.isStarted()) {
                ensureFloatingAnimationStarted();
            }
            return;
        }

        if (returningToSurface) {
            tickReturnToSurface();
            return;
        }

        if (returningToCommandSurface) {
            tickReturnToCommandSurface();
            return;
        }

        Player owner = getOwner();
        if (owner == null || owner.isRemoved()) {

            setDeltaMovement(getDeltaMovement().scale(0.82D));
            move(MoverType.SELF, getDeltaMovement());
            return;
        }

        updateFacingSmoothly(owner.getX(), owner.getZ(), 0.12F);

        double ownerDistanceSqr = distanceToSqr(owner);
        if (!catchingUp && ownerDistanceSqr > FOLLOW_WAKE_DISTANCE * FOLLOW_WAKE_DISTANCE) {
            catchingUp = true;
            smoothedFollowTarget = position();
            latchedArrivalTarget = null;
            choosePreferredSide(owner);
        }

        if (!catchingUp) {
            Vec3 velocity = getDeltaMovement().scale(0.70D);
            if (velocity.lengthSqr() < 0.000002D) {
                velocity = Vec3.ZERO;
            }
            setDeltaMovement(velocity);
            if (velocity.lengthSqr() > 0.0D) {
                move(MoverType.SELF, velocity);
            }
            return;
        }

        Vec3 dynamicTarget = getFollowTarget(owner);
        Vec3 ownerHorizontalVelocity = new Vec3(owner.getDeltaMovement().x, 0.0D, owner.getDeltaMovement().z);


        if (latchedArrivalTarget == null
                && ownerHorizontalVelocity.length() < ARRIVAL_OWNER_SPEED
                && position().distanceTo(dynamicTarget) < ARRIVAL_LATCH_DISTANCE) {
            latchedArrivalTarget = dynamicTarget;
        }


        if (latchedArrivalTarget != null
                && (ownerHorizontalVelocity.length() > ARRIVAL_OWNER_SPEED * 1.45D
                || owner.position().distanceTo(latchedArrivalTarget) > ARRIVAL_RELEASE_DISTANCE)) {
            latchedArrivalTarget = null;
        }

        Vec3 desiredTarget = latchedArrivalTarget != null ? latchedArrivalTarget : dynamicTarget;
        if (smoothedFollowTarget == null) {
            smoothedFollowTarget = desiredTarget;
        } else {


            double smoothing = latchedArrivalTarget != null ? 0.20D : TARGET_SMOOTHING;
            smoothedFollowTarget = smoothedFollowTarget.lerp(desiredTarget, smoothing);
        }

        Vec3 toTarget = smoothedFollowTarget.subtract(position());
        Vec3 ownerVelocity = owner.getDeltaMovement();
        Vec3 desiredVelocity = toTarget.scale(FOLLOW_POSITION_GAIN)
                .add(new Vec3(ownerVelocity.x, ownerVelocity.y * 0.25D, ownerVelocity.z).scale(0.55D));

        Vec3 velocity = getDeltaMovement();
        Vec3 accel = desiredVelocity.subtract(velocity).scale(FOLLOW_VELOCITY_GAIN);
        double accelLen = accel.length();
        if (accelLen > FOLLOW_MAX_ACCEL) {
            accel = accel.scale(FOLLOW_MAX_ACCEL / accelLen);
        }

        velocity = velocity.add(accel);
        double speed = velocity.length();
        if (speed > FOLLOW_MAX_SPEED) {
            velocity = velocity.scale(FOLLOW_MAX_SPEED / speed);
        }

        double distance = toTarget.length();
        if (distance < 1.35D) {
            double taper = Mth.clamp(distance / 1.35D, 0.18D, 1.0D);
            velocity = velocity.scale(0.82D + 0.18D * taper);
        }

        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        if (distance < FOLLOW_SETTLE_DISTANCE
                && velocity.lengthSqr() < 0.00045D
                && owner.getDeltaMovement().horizontalDistanceSqr() < 0.0035D) {
            catchingUp = false;
            smoothedFollowTarget = null;
            latchedArrivalTarget = null;
        }
    }


    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);


        if (held.getItem() instanceof BloodBottleItem) {
            if (!level().isClientSide) {
                if (isBookBound()) {
                    player.displayClientMessage(Component.literal(canReadBoundBook(player)
                            ? "Your name is already written in this Book of Shadows."
                            : "The ink darkens, but the book refuses your blood.")
                            .withStyle(canReadBoundBook(player) ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_RED), true);
                } else {
                    CompoundTag tag = held.getTag();
                    UUID bloodUuid = null;
                    if (tag != null && tag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)) {
                        try {
                            String raw = tag.getString(BloodBottleItem.TAG_ENTITY_UUID);
                            if (!raw.isBlank()) bloodUuid = UUID.fromString(raw);
                        } catch (IllegalArgumentException ignored) {}
                    }
                    if (player.getUUID().equals(bloodUuid)) {
                        setBookBinding(player.getUUID(), player.getGameProfile().getName());
                        setOwner(player);
                        setReaderState(getContentSourcePos(), true, 0);
                        BookKnowledgeManager.unlockInitiationEntries(player);
                        if (player instanceof ServerPlayer serverPlayer) {
                            ModNetworking.sendToPlayer(OpenBookOfShadowsPacket.forPlayer(serverPlayer, getContentSourcePos()), serverPlayer);
                        }
                        player.displayClientMessage(Component.literal("Your blood sinks into the page. The Book of Shadows knows your name.")
                                .withStyle(ChatFormatting.DARK_RED), true);
                        if (!player.getAbilities().instabuild) {
                            held.shrink(1);
                            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("The book will not accept another's blood as your signature.")
                                .withStyle(ChatFormatting.DARK_RED), true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }


        if (held.isEmpty()) {
            if (!level().isClientSide) ensureReadableSnapshot(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (!(held.getItem() instanceof WandItem)) return InteractionResult.PASS;


        if (isBookBound() && !canReadBoundBook(player)) {
            if (!level().isClientSide) player.displayClientMessage(
                    Component.literal("The Book of Shadows ignores your command.").withStyle(ChatFormatting.DARK_RED), true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        Optional<UUID> owner = getOwnerUUID();
        if (owner.isPresent() && !owner.get().equals(player.getUUID())) return InteractionResult.PASS;

        if (!level().isClientSide) {
            if (player.isShiftKeyDown()) {
                setCommandMode(false);
                beginReturnToSurface();
            } else {
                setCommandMode(true);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Book command: choose a surface."),
                        true);
            }
        }

        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hit, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) return interact(player, hand);


        if (!level().isClientSide) ensureReadableSnapshot(player);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }


    private void beginReturnToSurface() {
        if (returningToSurface) {
            return;
        }

        BlockPos found = findNearestLandingPos();
        if (found == null) {
            return;
        }

        landingPos = found;
        returningToSurface = true;
        returningToCommandSurface = false;
        setCommandMode(false);
        catchingUp = false;
        smoothedFollowTarget = null;
    }

    public boolean commandToSurface(Player player, BlockPos supportPos, Direction clickedFace) {
        if (!isOwnedBy(player) || !isCommandMode() || clickedFace != Direction.UP) {
            return false;
        }

        BlockPos target = supportPos.above();
        if (!isValidLandingPos(target)) {
            return false;
        }

        landingPos = target.immutable();
        commandLandingFacing = horizontalDirectionToward(
                Vec3.atBottomCenterOf(target),
                player.position());
        returningToSurface = false;
        returningToCommandSurface = true;
        catchingUp = false;
        smoothedFollowTarget = null;
        latchedArrivalTarget = null;
        setCommandMode(false);
        return true;
    }

    private void tickReturnToCommandSurface() {
        if (!(level() instanceof ServerLevel serverLevel) || landingPos == null) {
            returningToCommandSurface = false;
            landingPos = null;
            return;
        }

        if (!isValidLandingPos(landingPos)) {
            returningToCommandSurface = false;
            landingPos = null;
            return;
        }

        Vec3 target = Vec3.atBottomCenterOf(landingPos);
        Vec3 toTarget = target.subtract(position());
        double distance = toTarget.length();

        updateFacingSmoothly(target.x, target.z, 0.14F);

        Vec3 velocity = getDeltaMovement()
                .scale(COMMAND_DAMPING)
                .add(toTarget.scale(COMMAND_SPRING));

        double speed = velocity.length();
        if (speed > COMMAND_MAX_SPEED) {
            velocity = velocity.scale(COMMAND_MAX_SPEED / speed);
        }

        if (distance < 0.85D) {
            velocity = velocity.scale(Mth.clamp(0.70D + distance * 0.18D, 0.70D, 0.85D));
        }

        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        if (distance <= RETURN_FINISH_DISTANCE && velocity.length() <= RETURN_FINISH_SPEED) {
            settleAsFloatingBlock(serverLevel);
        }
    }

    private void tickReturnToSurface() {
        if (!(level() instanceof ServerLevel serverLevel) || landingPos == null) {
            returningToSurface = false;
            landingPos = null;
            return;
        }


        if (!isValidLandingPos(landingPos)) {
            BlockPos replacement = findNearestLandingPos();
            if (replacement == null) {
                returningToSurface = false;
                landingPos = null;
                return;
            }
            landingPos = replacement;
        }

        Vec3 target = Vec3.atBottomCenterOf(landingPos);
        Vec3 toTarget = target.subtract(position());
        double distance = toTarget.length();

        updateFacingSmoothly(target.x, target.z, 0.18F);

        Vec3 velocity = getDeltaMovement()
                .scale(RETURN_DAMPING)
                .add(toTarget.scale(RETURN_SPRING));

        double speed = velocity.length();
        if (speed > RETURN_MAX_SPEED) {
            velocity = velocity.scale(RETURN_MAX_SPEED / speed);
        }

        if (distance < 0.85D) {
            velocity = velocity.scale(Mth.clamp(0.68D + distance * 0.20D, 0.68D, 0.85D));
        }

        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        if (distance <= RETURN_FINISH_DISTANCE && velocity.length() <= RETURN_FINISH_SPEED) {
            settleAsBlock(serverLevel);
        }
    }


    private BlockPos findNearestLandingPos() {
        BlockPos origin = BlockPos.containing(getX(), getY(), getZ());
        BlockPos best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (int dy = -LANDING_VERTICAL_RADIUS; dy <= LANDING_VERTICAL_RADIUS; dy++) {
            for (int dx = -LANDING_SEARCH_RADIUS; dx <= LANDING_SEARCH_RADIUS; dx++) {
                for (int dz = -LANDING_SEARCH_RADIUS; dz <= LANDING_SEARCH_RADIUS; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!isValidLandingPos(candidate)) {
                        continue;
                    }

                    double distanceSqr = Vec3.atBottomCenterOf(candidate).distanceToSqr(position());
                    if (distanceSqr < bestDistanceSqr) {
                        bestDistanceSqr = distanceSqr;
                        best = candidate.immutable();
                    }
                }
            }
        }

        return best;
    }

    private boolean isValidLandingPos(BlockPos candidate) {
        if (!level().isInWorldBounds(candidate)) {
            return false;
        }

        BlockState targetState = level().getBlockState(candidate);
        if (!targetState.canBeReplaced() || !targetState.getFluidState().isEmpty()) {
            return false;
        }

        BlockPos supportPos = candidate.below();
        BlockState support = level().getBlockState(supportPos);
        return support.getFluidState().isEmpty()
                && support.isFaceSturdy(level(), supportPos, Direction.UP);
    }


    private static Direction horizontalDirectionToward(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx >= 0.0D ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private void refreshOwnerSnapshotAt(ServerLevel serverLevel, BlockPos newPos) {
        if (boundBookOwnerUUID == null || !isReaderActive()) return;
        ServerPlayer owner = (ServerPlayer) serverLevel.getPlayerByUUID(boundBookOwnerUUID);
        if (owner != null) {
            ModNetworking.sendToPlayer(OpenBookOfShadowsPacket.forPlayer(owner, newPos), owner);
        }
    }

    private void settleAsFloatingBlock(ServerLevel serverLevel) {
        if (landingPos == null || !isValidLandingPos(landingPos)) {
            returningToCommandSurface = false;
            landingPos = null;
            return;
        }

        Direction facing = commandLandingFacing != null
                ? commandLandingFacing
                : Direction.fromYRot(getYRot());
        BlockState bookState = ModBlocks.BOOK_OF_SHADOWS.get().defaultBlockState()
                .setValue(BookOfShadowsBlock.FACING, facing);

        if (!serverLevel.setBlock(landingPos, bookState, 3)) {
            return;
        }

        if (serverLevel.getBlockEntity(landingPos) instanceof BookOfShadowsBlockEntity book) {
            book.setBinding(boundBookOwnerUUID, boundBookOwnerName);
            book.setReaderState(isReaderActive(), getReaderPage());
            book.landAndFloatFromCommand();
            refreshOwnerSnapshotAt(serverLevel, landingPos);
        }

        commandLandingFacing = null;
        discard();
    }

    private void settleAsBlock(ServerLevel serverLevel) {
        if (landingPos == null || !isValidLandingPos(landingPos)) {
            returningToSurface = false;
            landingPos = null;
            return;
        }

        Direction facing = Direction.fromYRot(getYRot());
        BlockState bookState = ModBlocks.BOOK_OF_SHADOWS.get().defaultBlockState()
                .setValue(BookOfShadowsBlock.FACING, facing);

        if (!serverLevel.setBlock(landingPos, bookState, 3)) {
            return;
        }

        if (serverLevel.getBlockEntity(landingPos) instanceof BookOfShadowsBlockEntity book) {
            book.setBinding(boundBookOwnerUUID, boundBookOwnerName);
            book.setReaderState(isReaderActive(), getReaderPage());
            refreshOwnerSnapshotAt(serverLevel, landingPos);


            book.landAndCloseFromFollow();
        }

        discard();
    }


    private void choosePreferredSide(Player owner) {
        float yawRadians = owner.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(yawRadians), 0.0D, Mth.cos(yawRadians));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 ownerToBook = position().subtract(owner.position());
        double side = ownerToBook.dot(right);


        if (Math.abs(side) > 0.22D) {
            preferredSideSign = side >= 0.0D ? 1.0D : -1.0D;
        }
    }


    private Vec3 getFollowTarget(Player owner) {
        float yawRadians = owner.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 lookForward = new Vec3(-Mth.sin(yawRadians), 0.0D, Mth.cos(yawRadians));
        Vec3 right = new Vec3(lookForward.z, 0.0D, -lookForward.x);

        Vec3 horizontalMotion = new Vec3(owner.getDeltaMovement().x, 0.0D, owner.getDeltaMovement().z);
        double motionSpeed = horizontalMotion.length();
        Vec3 bookToOwner = owner.position().subtract(position());

        boolean movingAway = motionSpeed > OWNER_MOVING_THRESHOLD
                && horizontalMotion.dot(bookToOwner) > 0.0D;

        Vec3 offset;
        if (movingAway) {
            Vec3 moveDir = horizontalMotion.normalize();
            Vec3 moveRight = new Vec3(moveDir.z, 0.0D, -moveDir.x);
            offset = moveDir.scale(-TRAIL_BEHIND)
                    .add(moveRight.scale(TRAIL_SIDE * preferredSideSign));
        } else {
            offset = lookForward.scale(TARGET_FORWARD)
                    .add(right.scale(TARGET_SIDE * preferredSideSign));
        }

        return new Vec3(owner.getX(), owner.getY(), owner.getZ()).add(offset);
    }

    private void updateFacingSmoothly(double targetX, double targetZ, float smoothing) {
        double dx = targetX - getX();
        double dz = targetZ - getZ();
        if (dx * dx + dz * dz < 0.0001D) {
            return;
        }

        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float delta = Mth.wrapDegrees(targetYaw - getYRot());

        setYRot(getYRot() + delta * smoothing);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            entityData.set(OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        }
        boundBookOwnerUUID = tag.hasUUID("BoundBookOwner") ? tag.getUUID("BoundBookOwner") : null;
        boundBookOwnerName = tag.getString("BoundBookOwnerName");
        catchingUp = tag.getBoolean("CatchingUp");
        returningToSurface = tag.getBoolean("ReturningToSurface");
        returningToCommandSurface = tag.getBoolean("ReturningToCommandSurface");
        setCommandMode(tag.getBoolean("CommandMode"));
        entityData.set(READER_ACTIVE, tag.getBoolean("ReaderActive"));
        entityData.set(READER_PAGE, Math.max(0, tag.getInt("ReaderPage")));
        entityData.set(PAGE_TURN_SERIAL, tag.getInt("PageTurnSerial"));
        entityData.set(PAGE_TURN_DIRECTION, tag.getInt("PageTurnDirection") < 0 ? -1 : 1);
        if (tag.contains("ContentSourcePos")) entityData.set(CONTENT_SOURCE_POS, tag.getLong("ContentSourcePos"));
        if (tag.contains("LandingX") && tag.contains("LandingY") && tag.contains("LandingZ")) {
            landingPos = new BlockPos(tag.getInt("LandingX"), tag.getInt("LandingY"), tag.getInt("LandingZ"));
        }
        if (tag.contains("CommandLandingFacing")) {
            commandLandingFacing = Direction.from2DDataValue(tag.getInt("CommandLandingFacing"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("Owner", uuid));
        if (boundBookOwnerUUID != null) {
            tag.putUUID("BoundBookOwner", boundBookOwnerUUID);
            tag.putString("BoundBookOwnerName", boundBookOwnerName == null ? "" : boundBookOwnerName);
        }
        tag.putBoolean("CatchingUp", catchingUp);
        tag.putBoolean("ReturningToSurface", returningToSurface);
        tag.putBoolean("ReturningToCommandSurface", returningToCommandSurface);
        tag.putBoolean("CommandMode", isCommandMode());
        tag.putBoolean("ReaderActive", isReaderActive());
        tag.putInt("ReaderPage", getReaderPage());
        tag.putInt("PageTurnSerial", entityData.get(PAGE_TURN_SERIAL));
        tag.putInt("PageTurnDirection", getPageTurnDirection());
        tag.putLong("ContentSourcePos", entityData.get(CONTENT_SOURCE_POS));
        if (landingPos != null) {
            tag.putInt("LandingX", landingPos.getX());
            tag.putInt("LandingY", landingPos.getY());
            tag.putInt("LandingZ", landingPos.getZ());
        }
        if (commandLandingFacing != null) {
            tag.putInt("CommandLandingFacing", commandLandingFacing.get2DDataValue());
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return WAND_PICK_FORGIVENESS;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
