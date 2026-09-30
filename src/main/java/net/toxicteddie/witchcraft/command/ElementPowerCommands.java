package net.toxicteddie.witchcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.ElementPowerConstants;
import net.toxicteddie.witchcraft.power.PlayerElementPowerManager;

import java.util.Collection;
import java.util.List;

public final class ElementPowerCommands {
    private static final DynamicCommandExceptionType UNKNOWN_ELEMENT =
            new DynamicCommandExceptionType(value -> Component.literal("Unknown element '" + value
                    + "'. Use air, fire, water, or earth."));

    private ElementPowerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("witchcraft")
                .then(Commands.literal("power")
                        .then(Commands.literal("get")
                                .then(Commands.argument("element", StringArgumentType.word())
                                        .executes(ctx -> show(ctx, ctx.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .requires(src -> src.hasPermission(2))
                                                .executes(ctx -> show(ctx, EntityArgument.getPlayer(ctx, "player"))))))
                        .then(changeBranch("set", Operation.SET))
                        .then(changeBranch("add", Operation.ADD))
                        .then(changeBranch("remove", Operation.REMOVE))
                        .then(Commands.literal("fill")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> fill(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException())))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> fill(ctx.getSource(), EntityArgument.getPlayers(ctx, "players")))))
                        .then(Commands.literal("reset")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> reset(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException())))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayers(ctx, "players")))))
                ));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> changeBranch(
            String name, Operation operation) {
        int min = operation == Operation.SET ? 0 : 1;
        return Commands.literal(name)
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("element", StringArgumentType.word())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(min, ElementPowerConstants.MAX_POWER))
                                .executes(ctx -> change(ctx, List.of(ctx.getSource().getPlayerOrException()), operation))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> change(ctx, EntityArgument.getPlayers(ctx, "players"), operation)))));
    }

    private static ElementPower element(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String raw = StringArgumentType.getString(ctx, "element");
        return ElementPower.fromName(raw).orElseThrow(() -> UNKNOWN_ELEMENT.create(raw));
    }

    private static int show(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        ElementPower element = element(ctx);
        int amount = PlayerElementPowerManager.get(player, element);
        ctx.getSource().sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " has ")
                .append(Component.literal(Integer.toString(amount)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("/" + ElementPowerConstants.MAX_POWER + " " + element.id() + " power.")), false);
        return amount;
    }

    private static int change(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players,
                              Operation operation) throws CommandSyntaxException {
        ElementPower element = element(ctx);
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        for (ServerPlayer player : players) {
            switch (operation) {
                case SET -> PlayerElementPowerManager.set(player, element, amount);
                case ADD -> PlayerElementPowerManager.add(player, element, amount);
                case REMOVE -> PlayerElementPowerManager.remove(player, element, amount);
            }
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Updated " + element.id()
                + " power for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int fill(CommandSourceStack source, Collection<ServerPlayer> players) {
        players.forEach(PlayerElementPowerManager::fill);
        source.sendSuccess(() -> Component.literal("Filled elemental power for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int reset(CommandSourceStack source, Collection<ServerPlayer> players) {
        players.forEach(PlayerElementPowerManager::reset);
        source.sendSuccess(() -> Component.literal("Reset elemental power for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private enum Operation { SET, ADD, REMOVE }
}
