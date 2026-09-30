package net.toxicteddie.witchcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;

import java.util.Collection;
import java.util.List;

public final class MagicCommands {
    private MagicCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("witchcraft")
                        .then(Commands.literal("magic")
                                .then(Commands.literal("get")
                                        .executes(context -> showMagic(
                                                context.getSource(),
                                                context.getSource().getPlayerOrException()
                                        ))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .requires(source -> source.hasPermission(2))
                                                .executes(context -> showMagic(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player")
                                                ))))
                                .then(Commands.literal("state")
                                        .executes(context -> showState(
                                                context.getSource(),
                                                context.getSource().getPlayerOrException()
                                        )))
                                .then(Commands.literal("exhaust")
                                        .then(Commands.literal("get")
                                                .executes(context -> showMagicalExhaust(
                                                        context.getSource(),
                                                        context.getSource().getPlayerOrException()
                                                ))
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .requires(source -> source.hasPermission(2))
                                                        .executes(context -> showMagicalExhaust(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "player")
                                                        ))))
                                        .then(Commands.literal("clear")
                                                .requires(source -> source.hasPermission(2))
                                                .executes(context -> clearMagicalExhaust(
                                                        context.getSource(),
                                                        List.of(context.getSource().getPlayerOrException())
                                                ))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> clearMagicalExhaust(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "players")
                                                        )))))
                                .then(Commands.literal("set")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument(
                                                        "amount",
                                                        IntegerArgumentType.integer(
                                                                0,
                                                                MagicConstants.ABSOLUTE_LIMIT
                                                        )
                                                )
                                                .executes(context -> changeMagic(
                                                        context.getSource(),
                                                        List.of(context.getSource().getPlayerOrException()),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        Operation.SET
                                                ))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> changeMagic(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "players"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.SET
                                                        )))))
                                .then(Commands.literal("add")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument(
                                                        "amount",
                                                        IntegerArgumentType.integer(
                                                                1,
                                                                MagicConstants.ABSOLUTE_LIMIT
                                                        )
                                                )
                                                .executes(context -> changeMagic(
                                                        context.getSource(),
                                                        List.of(context.getSource().getPlayerOrException()),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        Operation.ADD
                                                ))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> changeMagic(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "players"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.ADD
                                                        )))))
                                .then(Commands.literal("remove")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument(
                                                        "amount",
                                                        IntegerArgumentType.integer(
                                                                1,
                                                                MagicConstants.ABSOLUTE_LIMIT
                                                        )
                                                )
                                                .executes(context -> changeMagic(
                                                        context.getSource(),
                                                        List.of(context.getSource().getPlayerOrException()),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        Operation.REMOVE
                                                ))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> changeMagic(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "players"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.REMOVE
                                                        )))))
                        )
                        .then(Commands.literal("altar")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("add")
                                        .then(Commands.argument(
                                                        "amount",
                                                        IntegerArgumentType.integer(1)
                                                )
                                                .executes(context -> addAltarMagic(
                                                        context.getSource(),
                                                        context.getSource().getPlayerOrException(),
                                                        IntegerArgumentType.getInteger(context, "amount")
                                                ))
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> addAltarMagic(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "player"),
                                                                IntegerArgumentType.getInteger(context, "amount")
                                                        )))))
                                .then(Commands.literal("get")
                                        .executes(context -> showAltarMagic(
                                                context.getSource(),
                                                context.getSource().getPlayerOrException()
                                        ))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> showAltarMagic(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player")
                                                ))))
                        )
        );
    }

    private static int showMagic(CommandSourceStack source, ServerPlayer player) {
        int magic = PlayerMagicManager.getMagic(player);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " has ")
                .append(Component.literal(Integer.toString(magic)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" magic.")), false);
        return magic;
    }

    private static int showState(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Your magic is ")
                .append(PlayerMagicManager.getState(player).getDisplayComponent())
                .append(Component.literal(" (" + PlayerMagicManager.getMagic(player) + ").")), false);
        return 1;
    }

    private static int showMagicalExhaust(CommandSourceStack source, ServerPlayer player) {
        boolean exhausted = PlayerMagicManager.isMagicallyExhausted(player);
        long ticks = PlayerMagicManager.getMagicalExhaustTicksRemaining(player);

        if (!exhausted) {
            source.sendSuccess(() -> Component.literal(
                    player.getGameProfile().getName() + " is not under Magical Exhaust."
            ).withStyle(ChatFormatting.GREEN), false);
            return 0;
        }

        long totalSeconds = ticks / 20L;
        long days = totalSeconds / 1200L;
        long remainingSeconds = totalSeconds % 1200L;
        long minutes = remainingSeconds / 60L;
        long seconds = remainingSeconds % 60L;

        source.sendSuccess(() -> Component.literal(
                player.getGameProfile().getName() + " is under Magical Exhaust. "
        ).withStyle(ChatFormatting.RED).append(
                Component.literal(
                        "Remaining: " + days + "d "
                                + minutes + "m "
                                + seconds + "s (" + ticks + " ticks)."
                ).withStyle(ChatFormatting.GRAY)
        ), false);

        return 1;
    }

    private static int clearMagicalExhaust(
            CommandSourceStack source,
            Collection<ServerPlayer> players
    ) {
        int cleared = 0;

        for (ServerPlayer player : players) {
            if (PlayerMagicManager.isMagicallyExhausted(player)) {
                PlayerMagicManager.clearMagicalExhaust(player);
                cleared++;
            }
        }

        final int count = cleared;

        source.sendSuccess(() -> Component.literal(
                count == 1
                        ? "Removed Magical Exhaust from 1 witch."
                        : "Removed Magical Exhaust from " + count + " witches."
        ).withStyle(ChatFormatting.GREEN), true);

        return count;
    }

    private static int addAltarMagic(CommandSourceStack source, ServerPlayer owner, int amount) {
        WitchesAltarBlockEntity altar = WitchesAltarBlockEntity.getLoadedOwnedAltar(owner.getUUID());
        if (altar == null) {
            source.sendFailure(Component.literal(
                    owner.getGameProfile().getName() + " has no loaded owned altar."
            ));
            return 0;
        }

        int added = altar.addPower(amount);
        int stored = altar.getStoredPower();
        int capacity = altar.getPowerCapacity();

        source.sendSuccess(() -> Component.literal("Added ")
                .append(Component.literal(Integer.toString(added)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(
                        " altar magic to " + owner.getGameProfile().getName()
                                + "'s altar (" + stored + "/" + capacity + ")."
                )), true);

        return added;
    }

    private static int showAltarMagic(CommandSourceStack source, ServerPlayer owner) {
        WitchesAltarBlockEntity altar = WitchesAltarBlockEntity.getLoadedOwnedAltar(owner.getUUID());
        if (altar == null) {
            source.sendFailure(Component.literal(
                    owner.getGameProfile().getName() + " has no loaded owned altar."
            ));
            return 0;
        }

        int stored = altar.getStoredPower();
        int capacity = altar.getPowerCapacity();

        source.sendSuccess(() -> Component.literal(owner.getGameProfile().getName() + "'s altar has ")
                .append(Component.literal(stored + "/" + capacity).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" magic.")), false);

        return stored;
    }

    private static int changeMagic(
            CommandSourceStack source,
            Collection<ServerPlayer> players,
            int amount,
            Operation operation
    ) {
        for (ServerPlayer player : players) {
            switch (operation) {
                case SET -> PlayerMagicManager.setMagic(player, amount);
                case ADD -> PlayerMagicManager.addMagic(player, amount);
                case REMOVE -> PlayerMagicManager.removeMagic(player, amount);
            }
        }

        source.sendSuccess(
                () -> Component.literal("Updated magic for " + players.size() + " player(s)."),
                true
        );

        return players.size();
    }

    private enum Operation {
        SET,
        ADD,
        REMOVE
    }
}
