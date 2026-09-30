package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.block.entity.ModBlockEntities;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeManager;
import net.toxicteddie.witchcraft.book.BookContentLayout;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;


public class BookOfShadowsBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    private static final VoxelShape CLOSED_NS = Shapes.or(
            Block.box(3.0D, 0.0D, 2.0D, 13.0D, 1.75D, 14.0D),
            Block.box(2.5D, 0.0D, 2.0D, 3.75D, 2.25D, 14.0D)
    );
    private static final VoxelShape CLOSED_EW = Shapes.or(
            Block.box(2.0D, 0.0D, 3.0D, 14.0D, 1.75D, 13.0D),
            Block.box(2.0D, 0.0D, 2.5D, 14.0D, 2.25D, 3.75D)
    );

    private static final VoxelShape OPEN_NS = Shapes.or(
            Block.box(0.75D, 0.0D, 3.0D, 7.75D, 1.35D, 13.0D),
            Block.box(8.25D, 0.0D, 3.0D, 15.25D, 1.35D, 13.0D),
            Block.box(7.5D, 0.0D, 3.0D, 8.5D, 2.0D, 13.0D)
    );
    private static final VoxelShape OPEN_EW = Shapes.or(
            Block.box(3.0D, 0.0D, 0.75D, 13.0D, 1.35D, 7.75D),
            Block.box(3.0D, 0.0D, 8.25D, 13.0D, 1.35D, 15.25D),
            Block.box(3.0D, 0.0D, 7.5D, 13.0D, 2.0D, 8.5D)
    );


    private static final VoxelShape FLOATING = Block.box(0.50D, 13.0D, 0.50D, 15.50D, 28.0D, 15.50D);

    public BookOfShadowsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BookOfShadowsBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BOOK_OF_SHADOWS_BE.get(), BookOfShadowsBlockEntity::tick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state, level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BookOfShadowsBlockEntity book) {


            if (book.getPhase().floating()) {
                return Shapes.empty();
            }
        }


        return shapeForState(state, level, pos);
    }

    private VoxelShape shapeForState(BlockState state, BlockGetter level, BlockPos pos) {
        boolean eastWest = state.getValue(FACING).getAxis() == Direction.Axis.X;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BookOfShadowsBlockEntity book) {
            if (book.isFloatingOrTransitioning()) {
                return FLOATING;
            }
            if (book.isOpenOrTransitioning()) {
                return eastWest ? OPEN_EW : OPEN_NS;
            }
        }
        return eastWest ? CLOSED_EW : CLOSED_NS;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BookOfShadowsBlockEntity book)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean wand = held.getItem() instanceof WandItem;
        boolean sneaking = player.isShiftKeyDown();


        if (held.getItem() instanceof BloodBottleItem) {
            if (!level.isClientSide) {
                if (book.isBound()) {
                    player.displayClientMessage(Component.literal(book.canRead(player)
                            ? "Your name is already written in this Book of Shadows."
                            : "The ink darkens, but the book refuses your blood.")
                            .withStyle(book.canRead(player) ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_RED), true);
                } else {
                    var tag = held.getTag();
                    UUID bloodUuid = null;
                    if (tag != null && tag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)) {
                        try {
                            String raw = tag.getString(BloodBottleItem.TAG_ENTITY_UUID);
                            if (!raw.isBlank()) bloodUuid = UUID.fromString(raw);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    if (player.getUUID().equals(bloodUuid)) {
                        book.bindTo(player);


                        book.setReaderState(true, 0);
                        BookKnowledgeManager.unlockInitiationEntries(player);
                        if (player instanceof ServerPlayer serverPlayer) {
                            ModNetworking.sendToPlayer(OpenBookOfShadowsPacket.forPlayer(serverPlayer, pos), serverPlayer);
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
            return InteractionResult.sidedSuccess(level.isClientSide);
        }


        if (wand) {
            if (!level.isClientSide) {


                if (book.isBound() && !book.canRead(player)) {
                    player.displayClientMessage(Component.literal("The Book of Shadows ignores your command.")
                            .withStyle(ChatFormatting.DARK_RED), true);
                    return InteractionResult.CONSUME;
                }


                beginPhysicalReadingIfAllowed(book, player, pos);
                if (sneaking) {
                    book.followWithWand(player);
                } else {
                    book.raiseWithWand();
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }


        if (!held.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (sneaking) {
                book.closeWithEmptyHand();
            } else if (book.getPhase() == BookOfShadowsBlockEntity.Phase.OPEN_IDLE
                    || book.getPhase() == BookOfShadowsBlockEntity.Phase.FLOAT_IDLE) {


                if (!book.isBound()) {
                    player.displayClientMessage(Component.literal("The pages are blank. The book has not been signed in blood.")
                            .withStyle(ChatFormatting.DARK_GRAY), true);
                } else if (!book.canRead(player) && player instanceof ServerPlayer serverPlayer) {


                    if (!book.isReaderActive()) book.beginReading();
                    ModNetworking.sendToPlayer(OpenBookOfShadowsPacket.gibberish(pos), serverPlayer);

                } else if (player instanceof ServerPlayer serverPlayer) {
                    if (!book.isReaderActive()) {


                        book.beginReading();
                        ModNetworking.sendToPlayer(OpenBookOfShadowsPacket.forPlayer(serverPlayer, pos), serverPlayer);
                    } else {


                    }
                }
            } else {


                beginPhysicalReadingIfAllowed(book, player, pos);
                book.openWithEmptyHand();
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    private static void beginPhysicalReadingIfAllowed(BookOfShadowsBlockEntity book, Player player, BlockPos pos) {
        if (!book.isBound() || !(player instanceof ServerPlayer serverPlayer)) return;
        if (!book.isReaderActive()) book.beginReading();
        ModNetworking.sendToPlayer(book.canRead(player)
                ? OpenBookOfShadowsPacket.forPlayer(serverPlayer, pos)
                : OpenBookOfShadowsPacket.gibberish(pos), serverPlayer);
    }
}
