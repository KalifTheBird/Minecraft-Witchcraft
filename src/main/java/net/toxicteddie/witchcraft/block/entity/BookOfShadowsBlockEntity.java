package net.toxicteddie.witchcraft.block.entity;

import net.toxicteddie.witchcraft.book.BookPageTurnTiming;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.toxicteddie.witchcraft.block.custom.BookOfShadowsBlock;
import net.toxicteddie.witchcraft.entity.ModEntities;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


public class BookOfShadowsBlockEntity extends BlockEntity {
    public enum Phase {
        CLOSED_IDLE(false, false, false),
        CLOSED_OPENING(false, true, true),
        OPEN_IDLE(false, true, false),
        OPEN_CLOSING(false, true, true),
        FLOAT_OPENING(true, true, true),
        FLOAT_IDLE(true, true, false),
        FLOAT_CLOSING(true, true, true);

        private final boolean floating;
        private final boolean open;
        private final boolean transition;

        Phase(boolean floating, boolean open, boolean transition) {
            this.floating = floating;
            this.open = open;
            this.transition = transition;
        }

        public boolean floating() {
            return floating;
        }

        public boolean open() {
            return open;
        }

        public boolean transition() {
            return transition;
        }
    }

    private static final int TRANSITION_TICKS = 22;

    private Phase phase = Phase.CLOSED_IDLE;
    private int phaseTicks = 0;
    private boolean floatAfterOpening = false;
    private boolean followAfterFloating = false;
    private UUID pendingFollowOwner;
    private UUID boundOwnerUUID;
    private String boundOwnerName = "";


    private int pageTurnSerial = 0;
    private int pageTurnDirection = 1;
    private int readerPage = 0;
    private boolean readerActive = false;


    public final AnimationState animationState = new AnimationState();
    private Phase clientAnimationPhase = null;
    public final AnimationState pageTurnAnimationState = new AnimationState();
    private int clientSeenPageTurnSerial = 0;
    private int clientPageTurnTicks = 0;

    public BookOfShadowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOOK_OF_SHADOWS_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BookOfShadowsBlockEntity book) {
        if (level.isClientSide) {
            book.updateClientAnimationState(level);
            book.tickClientPageTurn(level);
        }

        if (!book.phase.transition()) {


            if (!level.isClientSide && book.phase == Phase.FLOAT_IDLE && book.followAfterFloating) {
                book.spawnFollowingEntity();
            }
            return;
        }

        book.phaseTicks++;
        if (book.phaseTicks < TRANSITION_TICKS) {
            return;
        }

        Phase next = switch (book.phase) {
            case CLOSED_OPENING -> book.floatAfterOpening ? Phase.FLOAT_OPENING : Phase.OPEN_IDLE;
            case OPEN_CLOSING -> Phase.CLOSED_IDLE;
            case FLOAT_OPENING -> Phase.FLOAT_IDLE;


            case FLOAT_CLOSING -> Phase.OPEN_CLOSING;
            default -> book.phase;
        };

        if (book.phase == Phase.CLOSED_OPENING) {
            book.floatAfterOpening = false;
        }

        if (!level.isClientSide && next == Phase.CLOSED_IDLE && book.readerActive) {
            book.endReading();
        }
        book.setPhase(next, !level.isClientSide);
    }

    public Phase getPhase() {
        return phase;
    }

    public int getReaderPage() { return readerPage; }

    public boolean isReaderActive() { return readerActive; }

    public void setReaderState(boolean active, int page) {
        readerActive = active;
        readerPage = Math.max(0, page & ~1);
        syncReaderState();
    }

    public void beginReading() {
        readerActive = true;
        readerPage = Math.max(0, readerPage & ~1);
        syncReaderState();
    }

    public void endReading() {
        readerActive = false;
        syncReaderState();
    }

    public void turnReaderPage(int direction) {
        int d = direction < 0 ? -1 : 1;
        readerPage = Math.max(0, readerPage + d * 2);
        triggerPageTurn(d);
    }


    public void jumpReaderPage(int targetPage) {
        int targetSpread = Math.max(0, targetPage & ~1);
        if (targetSpread == readerPage) return;
        int direction = targetSpread > readerPage ? 1 : -1;
        readerPage = targetSpread;
        triggerPageTurn(direction);
    }

    private void syncReaderState() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public int getPageTurnDirection() {
        return pageTurnDirection;
    }

    public boolean isPageTurning() {
        return clientPageTurnTicks > 0 && pageTurnAnimationState.isStarted();
    }


    public float getClientPageTurnProgress() {
        if (!isPageTurning()) return 1.0F;
        return BookPageTurnTiming.progress(pageTurnAnimationState);
    }


    public void triggerPageTurn(int direction) {
        pageTurnDirection = direction < 0 ? -1 : 1;
        pageTurnSerial++;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void tickClientPageTurn(Level level) {
        if (pageTurnSerial != clientSeenPageTurnSerial) {
            startClientPageTurn(level);
        } else if (clientPageTurnTicks > 0) {
            clientPageTurnTicks--;
            if (clientPageTurnTicks <= 0) pageTurnAnimationState.stop();
        }
    }

    private void startClientPageTurn(Level level) {
        clientSeenPageTurnSerial = pageTurnSerial;
        pageTurnAnimationState.stop();
        pageTurnAnimationState.start((int) level.getGameTime());
        clientPageTurnTicks = BookPageTurnTiming.TURN_TICKS;
    }

    public boolean isBound() {
        return boundOwnerUUID != null;
    }

    @Nullable
    public UUID getBoundOwnerUUID() {
        return boundOwnerUUID;
    }

    public String getBoundOwnerName() {
        return boundOwnerName == null ? "" : boundOwnerName;
    }

    public boolean canRead(Player player) {
        return boundOwnerUUID != null && boundOwnerUUID.equals(player.getUUID());
    }

    public void bindTo(Player player) {
        boundOwnerUUID = player.getUUID();
        boundOwnerName = player.getGameProfile().getName();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setBinding(@Nullable UUID uuid, @Nullable String name) {
        boundOwnerUUID = uuid;
        boundOwnerName = name == null ? "" : name;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void clearBinding() {
        setBinding(null, "");
    }

    public boolean isFloatingOrTransitioning() {
        return phase.floating() || phase == Phase.FLOAT_OPENING;
    }

    public boolean isOpenOrTransitioning() {
        return phase.open() || phase == Phase.CLOSED_OPENING;
    }


    public void openWithEmptyHand() {
        if (phase == Phase.CLOSED_IDLE) {
            floatAfterOpening = false;
            setPhase(Phase.CLOSED_OPENING, true);
        }
    }


    public void closeWithEmptyHand() {


        if (phase == Phase.OPEN_IDLE) {
            setPhase(Phase.OPEN_CLOSING, true);
        } else if (phase == Phase.FLOAT_IDLE) {
            setPhase(Phase.FLOAT_CLOSING, true);
        }
    }


    public void raiseWithWand() {
        if (phase == Phase.CLOSED_IDLE) {
            floatAfterOpening = true;
            setPhase(Phase.CLOSED_OPENING, true);
        } else if (phase == Phase.OPEN_IDLE) {
            setPhase(Phase.FLOAT_OPENING, true);
        }
    }


    public void followWithWand(Player player) {
        pendingFollowOwner = player.getUUID();
        followAfterFloating = true;

        if (phase == Phase.CLOSED_IDLE) {
            floatAfterOpening = true;
            setPhase(Phase.CLOSED_OPENING, true);
        } else if (phase == Phase.OPEN_IDLE) {
            setPhase(Phase.FLOAT_OPENING, true);
        } else if (phase == Phase.FLOAT_IDLE) {
            spawnFollowingEntity();
        }
    }


    public void landAndCloseFromFollow() {
        floatAfterOpening = false;
        followAfterFloating = false;
        pendingFollowOwner = null;
        setPhase(Phase.FLOAT_CLOSING, true);
    }


    public void landAndFloatFromCommand() {
        floatAfterOpening = false;
        followAfterFloating = false;
        pendingFollowOwner = null;
        setPhase(Phase.FLOAT_IDLE, true);
    }

    private void spawnFollowingEntity() {
        if (!(level instanceof ServerLevel serverLevel) || pendingFollowOwner == null) {
            followAfterFloating = false;
            return;
        }

        Player owner = serverLevel.getPlayerByUUID(pendingFollowOwner);
        if (owner == null) {

            followAfterFloating = false;
            pendingFollowOwner = null;
            return;
        }

        BookOfShadowsFollowingEntity movingBook =
                ModEntities.BOOK_OF_SHADOWS_FOLLOWING.get().create(serverLevel);
        if (movingBook == null) {
            followAfterFloating = false;
            return;
        }

        movingBook.setOwner(owner);
        movingBook.setBookBinding(boundOwnerUUID, boundOwnerName);
        movingBook.setReaderState(worldPosition, readerActive, readerPage);
        movingBook.setPos(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D);

        if (getBlockState().hasProperty(BookOfShadowsBlock.FACING)) {
            float yaw = getBlockState().getValue(BookOfShadowsBlock.FACING).toYRot();
            movingBook.setYRot(yaw);
        }

        serverLevel.addFreshEntity(movingBook);

        followAfterFloating = false;
        pendingFollowOwner = null;
        serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
    }

    private void setPhase(Phase newPhase, boolean sync) {
        if (phase == newPhase && phaseTicks == 0) {
            return;
        }

        phase = newPhase;
        phaseTicks = 0;
        setChanged();


        if (level != null && level.isClientSide) {
            restartClientAnimationState(level);
        }

        if (sync && level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateClientAnimationState(Level level) {
        if (clientAnimationPhase == phase) {
            return;
        }
        restartClientAnimationState(level);
    }


    private void restartClientAnimationState(Level level) {
        animationState.stop();


        animationState.start((int) level.getGameTime() - phaseTicks);
        clientAnimationPhase = phase;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Phase", phase.name());
        tag.putInt("PhaseTicks", phaseTicks);
        tag.putBoolean("FloatAfterOpening", floatAfterOpening);
        tag.putBoolean("FollowAfterFloating", followAfterFloating);
        if (pendingFollowOwner != null) {
            tag.putUUID("PendingFollowOwner", pendingFollowOwner);
        }
        if (boundOwnerUUID != null) {
            tag.putUUID("BoundOwner", boundOwnerUUID);
            tag.putString("BoundOwnerName", boundOwnerName == null ? "" : boundOwnerName);
        }
        tag.putInt("PageTurnSerial", pageTurnSerial);
        tag.putInt("PageTurnDirection", pageTurnDirection);
        tag.putInt("ReaderPage", readerPage);
        tag.putBoolean("ReaderActive", readerActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (IllegalArgumentException ignored) {
            phase = Phase.CLOSED_IDLE;
        }
        phaseTicks = Math.max(0, tag.getInt("PhaseTicks"));
        floatAfterOpening = tag.getBoolean("FloatAfterOpening");
        followAfterFloating = tag.getBoolean("FollowAfterFloating");
        pendingFollowOwner = tag.hasUUID("PendingFollowOwner") ? tag.getUUID("PendingFollowOwner") : null;
        boundOwnerUUID = tag.hasUUID("BoundOwner") ? tag.getUUID("BoundOwner") : null;
        boundOwnerName = tag.getString("BoundOwnerName");
        pageTurnSerial = tag.getInt("PageTurnSerial");
        pageTurnDirection = tag.getInt("PageTurnDirection") < 0 ? -1 : 1;
        readerPage = Math.max(0, tag.getInt("ReaderPage"));
        readerActive = tag.getBoolean("ReaderActive");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            Phase previousPhase = phase;
            int previousTurnSerial = pageTurnSerial;
            load(tag);
            if (level != null && level.isClientSide) {


                if (previousPhase != phase || clientAnimationPhase == null) {
                    restartClientAnimationState(level);
                }
                if (previousTurnSerial != pageTurnSerial) {
                    startClientPageTurn(level);
                }
            }
        }
    }
}
