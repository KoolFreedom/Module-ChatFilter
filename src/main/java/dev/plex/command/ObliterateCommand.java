package dev.plex.command;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.api.punishment.PunishmentRequest;
import dev.plex.api.punishment.PunishmentType;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.utilities.FilterUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommandParameters(name = "obliterate", description = "Unleash divine punishment upon someone", usage = "/<command> <player> [reason]")
@CommandPermissions(permission = "plex.chatfilter.obliterate")
public class ObliterateCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender commandSender, @Nullable Player player, @NotNull String[] strings)
    {
        if (strings.length == 0)
        {
            return usage();
        }

        PlexPlayerView plexPlayer = ChatFilterModule.getApi().players().byName(strings[0])
                .orElseThrow(PlayerNotFoundException::new);

        Player target = getNonNullPlayer(plexPlayer.name());

        for (int i = 0; i < 30; i++)
        {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        target.setFireTicks(200);
        target.setGameMode(GameMode.ADVENTURE);

        broadcast(messageComponent("castingOblivion", commandSender, target));

        ChatFilterModule.getApi().scheduler().runEntityLater(target, () ->
                broadcast(messageComponent("playerEviscerated", target)), 2);

        ChatFilterModule.getApi().scheduler().runEntityLater(target, () ->
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");
            if (target.isOp()) target.setOp(false);
            if (target.isWhitelisted()) target.setWhitelisted(false);
        }, 2);

        ChatFilterModule.getApi().scheduler().runEntityLater(target, () -> target.setHealth(0), 10);

        ChatFilterModule.getApi().scheduler().runEntityLater(target, () ->
                broadcast(messageComponent("playerEradicated", target)), 30);

        FilterUtils.crashPlayer(target);

        PunishmentRequest request = new PunishmentRequest(
                plexPlayer.uuid(),
                getUUID(commandSender),
                commandSender.getName(),
                player != null
                        ? player.getAddress().getAddress().getHostAddress().trim()
                        : plexPlayer.ips().getLast(),
                plexPlayer.name(),
                PunishmentType.BAN,
                messageString("obliterateReason"),
                false,
                true,
                null
        );

        ChatFilterModule.getApi().scheduler().runEntityLater(target, () ->
                ChatFilterModule.getApi().punishments().punish(plexPlayer, request), 38);
        ChatFilterModule.getApi().scheduler().runEntityLater(target, () ->
                broadcast(messageComponent("targetPermBanned", commandSender, target)), 38);

        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (!silentCheckPermission(sender, this.getPermission()))
        {
            return Collections.emptyList();
        }

        if (args.length == 1)
        {
            return ChatFilterModule.getApi().players().onlineNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}