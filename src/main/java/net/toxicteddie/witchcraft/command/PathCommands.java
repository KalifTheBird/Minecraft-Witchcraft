package net.toxicteddie.witchcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.toxicteddie.witchcraft.path.PlayerPath;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;

import java.util.Collection;
import java.util.List;

public final class PathCommands {

    private PathCommands() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("witchcraft")
                        .then(
                                Commands.literal("path")


                                        .then(
                                                Commands.literal("get")
                                                        .executes(
                                                                PathCommands::getOwnPath
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "player",
                                                                                EntityArgument.player()
                                                                        )
                                                                        .requires(
                                                                                source ->
                                                                                        source.hasPermission(2)
                                                                        )
                                                                        .executes(
                                                                                PathCommands::getOtherPath
                                                                        )
                                                        )
                                        )


                                        .then(
                                                createPathCommand(
                                                        PlayerPath.LIGHT
                                                )
                                        )


                                        .then(
                                                createPathCommand(
                                                        PlayerPath.NIGHT
                                                )
                                        )


                                        .then(
                                                createPathCommand(
                                                        PlayerPath.BLOOD
                                                )
                                        )


                                        .then(
                                                createPathCommand(
                                                        PlayerPath.WILD
                                                )
                                        )


                                        .then(
                                                Commands.literal("clear")
                                                        .requires(
                                                                source ->
                                                                        source.hasPermission(2)
                                                        )
                                                        .executes(
                                                                context ->
                                                                        setPath(
                                                                                context.getSource(),
                                                                                List.of(
                                                                                        context.getSource()
                                                                                                .getPlayerOrException()
                                                                                ),
                                                                                PlayerPath.LIGHT,
                                                                                true
                                                                        )
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "players",
                                                                                EntityArgument.players()
                                                                        )
                                                                        .executes(
                                                                                context ->
                                                                                        setPath(
                                                                                                context.getSource(),
                                                                                                EntityArgument.getPlayers(
                                                                                                        context,
                                                                                                        "players"
                                                                                                ),
                                                                                                PlayerPath.LIGHT,
                                                                                                true
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            CommandSourceStack
            > createPathCommand(PlayerPath path) {

        return Commands.literal(
                        path.getSerializedName()
                )
                .requires(
                        source ->
                                source.hasPermission(2)
                )
                .executes(
                        context ->
                                setPath(
                                        context.getSource(),
                                        List.of(
                                                context.getSource()
                                                        .getPlayerOrException()
                                        ),
                                        path,
                                        false
                                )
                )
                .then(
                        Commands.argument(
                                        "players",
                                        EntityArgument.players()
                                )
                                .executes(
                                        context ->
                                                setPath(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(
                                                                context,
                                                                "players"
                                                        ),
                                                        path,
                                                        false
                                                )
                                )
                );
    }

    private static int getOwnPath(
            CommandContext<CommandSourceStack> context
    ) throws com.mojang.brigadier.exceptions.CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        PlayerPath path =
                PlayerPathManager.getPath(player);

        context.getSource().sendSuccess(
                () -> Component.literal(
                                "You walk the "
                        )
                        .append(
                                path.getDisplayComponent()
                        )
                        .append(
                                Component.literal(
                                        " and are known as a "
                                )
                        )
                        .append(
                                path.getFollowerComponent()
                        )
                        .append(
                                Component.literal(".")
                        ),
                false
        );

        return 1;
    }

    private static int getOtherPath(
            CommandContext<CommandSourceStack> context
    ) throws com.mojang.brigadier.exceptions.CommandSyntaxException {

        ServerPlayer player =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        PlayerPath path =
                PlayerPathManager.getPath(player);

        context.getSource().sendSuccess(
                () -> Component.literal(
                                player.getGameProfile().getName()
                                        + " walks the "
                        )
                        .append(
                                path.getDisplayComponent()
                        )
                        .append(
                                Component.literal(".")
                        ),
                false
        );

        return 1;
    }

    private static int setPath(
            CommandSourceStack source,
            Collection<ServerPlayer> players,
            PlayerPath path,
            boolean cleared
    ) {
        for (ServerPlayer player : players) {
            PlayerPathManager.setPath(
                    player,
                    path
            );

            if (path != PlayerPath.NIGHT) {
                PlayerMagicManager.clearMagic(player);
            } else {
                PlayerMagicManager.sync(player);
            }

            sendPathMessage(
                    player,
                    path,
                    cleared
            );
        }

        source.sendSuccess(
                () -> Component.literal(
                                cleared
                                        ? "Returned "
                                        : "Set "
                        )
                        .append(
                                Component.literal(
                                        players.size() == 1
                                                ? players.iterator()
                                                .next()
                                                .getGameProfile()
                                                .getName()
                                                : players.size()
                                                  + " players"
                                ).withStyle(
                                        ChatFormatting.AQUA
                                )
                        )
                        .append(
                                Component.literal(
                                        cleared
                                                ? " to the Path of Light."
                                                : " upon the "
                                )
                        )
                        .append(
                                cleared
                                        ? Component.empty()
                                        : path.getDisplayComponent()
                        )
                        .append(
                                cleared
                                        ? Component.empty()
                                        : Component.literal(".")
                        ),
                true
        );

        return players.size();
    }

    private static void sendPathMessage(
            ServerPlayer player,
            PlayerPath path,
            boolean cleared
    ) {
        Component message =
                switch (path) {
                    case LIGHT ->
                            Component.literal(
                                            cleared
                                                    ? "The old path fades behind you. "
                                                    : "Warm light settles over your spirit. "
                                    )
                                    .append(
                                            Component.literal(
                                                    "You now walk the Path of Light."
                                            ).withStyle(
                                                    ChatFormatting.GOLD
                                            )
                                    );

                    case NIGHT ->
                            Component.literal(
                                            "The dark between the stars answers your name. "
                                    )
                                    .append(
                                            Component.literal(
                                                    "You now walk the Path of Night."
                                            ).withStyle(
                                                    ChatFormatting.DARK_PURPLE
                                            )
                                    );

                    case BLOOD ->
                            Component.literal(
                                            "An ancient hunger quickens in your veins. "
                                    )
                                    .append(
                                            Component.literal(
                                                    "You now walk the Path of Blood."
                                            ).withStyle(
                                                    ChatFormatting.DARK_RED
                                            )
                                    );

                    case WILD ->
                            Component.literal(
                                            "The untamed world recognizes one of its own. "
                                    )
                                    .append(
                                            Component.literal(
                                                    "You now walk the Path of the Wild."
                                            ).withStyle(
                                                    ChatFormatting.DARK_GREEN
                                            )
                                    );
                };

        player.sendSystemMessage(message);
    }
}
