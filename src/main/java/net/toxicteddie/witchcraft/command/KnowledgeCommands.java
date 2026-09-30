package net.toxicteddie.witchcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeManager;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeRegistry;

import java.util.Set;


public final class KnowledgeCommands {
    private KnowledgeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("witchcraft")
                .then(Commands.literal("knowledge").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("get")
                                .executes(c -> show(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> show(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("entry", StringArgumentType.word())
                                        .suggests((c,b) -> SharedSuggestionProvider.suggest(BookKnowledgeRegistry.INSTANCE.ids(), b))
                                        .executes(c -> unlock(c.getSource(), c.getSource().getPlayerOrException(), StringArgumentType.getString(c,"entry")))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> unlock(c.getSource(), EntityArgument.getPlayer(c,"player"), StringArgumentType.getString(c,"entry"))))))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("entry", StringArgumentType.word())
                                        .suggests((c,b) -> SharedSuggestionProvider.suggest(BookKnowledgeRegistry.INSTANCE.ids(), b))
                                        .executes(c -> lock(c.getSource(), c.getSource().getPlayerOrException(), StringArgumentType.getString(c,"entry")))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> lock(c.getSource(), EntityArgument.getPlayer(c,"player"), StringArgumentType.getString(c,"entry"))))))
                        .then(Commands.literal("unlockall")
                                .executes(c -> unlockAll(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> unlockAll(c.getSource(), EntityArgument.getPlayer(c,"player")))))
                        .then(Commands.literal("reset")
                                .executes(c -> reset(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> reset(c.getSource(), EntityArgument.getPlayer(c,"player")))))
                        .then(Commands.literal("initiate")
                                .executes(c -> initiate(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> initiate(c.getSource(), EntityArgument.getPlayer(c,"player")))))
                )
                .then(Commands.literal("book").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("owner").executes(c -> bookOwner(c.getSource())))
                        .then(Commands.literal("bind").executes(c -> bindLookedAtBook(c.getSource())))
                        .then(Commands.literal("unbind").executes(c -> unbindLookedAtBook(c.getSource()))))
        );
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        Set<String> known = BookKnowledgeManager.getKnownEntries(player);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " knows " + known.size() + " Book of Shadows entries")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (!known.isEmpty()) source.sendSuccess(() -> Component.literal(String.join(", ", known)).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Initiated: " + BookKnowledgeManager.isInitiated(player)).withStyle(ChatFormatting.DARK_GRAY), false);
        return known.size();
    }

    private static int unlock(CommandSourceStack source, ServerPlayer player, String id) {
        boolean changed = BookKnowledgeManager.unlock(player, id);
        source.sendSuccess(() -> Component.literal((changed ? "Unlocked " : "Already known: ") + id + " for " + player.getGameProfile().getName()), true);
        return changed ? 1 : 0;
    }

    private static int lock(CommandSourceStack source, ServerPlayer player, String id) {
        boolean changed = BookKnowledgeManager.lock(player, id);
        source.sendSuccess(() -> Component.literal((changed ? "Locked " : "Was not known: ") + id + " for " + player.getGameProfile().getName()), true);
        return changed ? 1 : 0;
    }

    private static int unlockAll(CommandSourceStack source, ServerPlayer player) {
        int count = BookKnowledgeManager.unlockAll(player, BookKnowledgeRegistry.INSTANCE.ids());
        BookKnowledgeManager.setInitiated(player, true);
        source.sendSuccess(() -> Component.literal("Unlocked " + count + " new entries for " + player.getGameProfile().getName()), true);
        return count;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) {
        BookKnowledgeManager.reset(player);
        source.sendSuccess(() -> Component.literal("Reset Book of Shadows knowledge for " + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int initiate(CommandSourceStack source, ServerPlayer player) {
        BookKnowledgeManager.unlockInitiationEntries(player);
        source.sendSuccess(() -> Component.literal("Marked " + player.getGameProfile().getName() + " as initiated and unlocked the opening chapter."), true);
        return 1;
    }

    private static BookOfShadowsBlockEntity lookedAtBook(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        HitResult hit = player.pick(6.0D, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && player.level().getBlockEntity(blockHit.getBlockPos()) instanceof BookOfShadowsBlockEntity book) return book;
        return null;
    }

    private static int bookOwner(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        BookOfShadowsBlockEntity book = lookedAtBook(source);
        if (book == null) { source.sendFailure(Component.literal("Look directly at a placed Book of Shadows.")); return 0; }
        String owner = book.getBoundOwnerName();
        source.sendSuccess(() -> Component.literal(book.isBound() ? "Bound to " + owner + " (" + book.getBoundOwnerUUID() + ")" : "This Book of Shadows is unbound."), false);
        return book.isBound() ? 1 : 0;
    }

    private static int bindLookedAtBook(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BookOfShadowsBlockEntity book = lookedAtBook(source);
        if (book == null) { source.sendFailure(Component.literal("Look directly at a placed Book of Shadows.")); return 0; }
        book.bindTo(player);
        BookKnowledgeManager.unlockInitiationEntries(player);
        source.sendSuccess(() -> Component.literal("Bound the Book of Shadows to " + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int unbindLookedAtBook(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        BookOfShadowsBlockEntity book = lookedAtBook(source);
        if (book == null) { source.sendFailure(Component.literal("Look directly at a placed Book of Shadows.")); return 0; }
        book.clearBinding();
        source.sendSuccess(() -> Component.literal("Unbound the Book of Shadows."), true);
        return 1;
    }
}
