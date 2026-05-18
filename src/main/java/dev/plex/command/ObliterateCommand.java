package dev.plex.command;

import dev.plex.cache.DataUtils;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.player.PlexPlayer;
import dev.plex.punishment.Punishment;
import dev.plex.punishment.PunishmentType;
import dev.plex.util.PlexUtils;
import dev.plex.utilities.FilterUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        PlexPlayer plexPlayer = DataUtils.getPlayer(strings[0]);
        if (plexPlayer == null)
        {
            throw new PlayerNotFoundException();
        }

        Player target = getNonNullPlayer(plexPlayer.getName());

        for (int i = 0; i < 30; i++)
        {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        target.setFireTicks(200);
        target.setGameMode(GameMode.ADVENTURE);

        PlexUtils.broadcast(PlexUtils.messageComponent("castingOblivion", commandSender, target));

        Bukkit.getScheduler().runTaskLater(plugin, () -> PlexUtils.broadcast(messageComponent("playerEviscerated", target)), 2);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target + " clear");
            if (target.isOp()) target.setOp(false);
            if (target.isWhitelisted()) target.setWhitelisted(false);
        }, 2);

        Bukkit.getScheduler().runTaskLater(plugin, () -> target.setHealth(0), 10);

        Bukkit.getScheduler().runTaskLater(plugin, () -> PlexUtils.broadcast(messageComponent("playerEradicated", target)), 30);

        FilterUtils.crashPlayer(target);

        Punishment punishment = new Punishment(plexPlayer.getUuid(), getUUID(commandSender));
        punishment.setType(PunishmentType.BAN);
        punishment.setPunishedUsername(plexPlayer.getName());
        punishment.setEndDate(null);
        punishment.setCustomTime(false);
        punishment.setActive(true);
        punishment.setReason(messageString("obliterateReason"));
        punishment.setIp(player != null ? player.getAddress().getAddress().getHostAddress().trim() : plexPlayer.getIps().getLast());

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                plugin.getPunishmentManager().punish(plexPlayer, punishment), 38);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                PlexUtils.broadcast(messageComponent("targetPermBanned")), 38);
        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (silentCheckPermission(sender, this.getPermission()))
        {
            if (args.length == 1)
            {
                return Arrays.asList("option1", "option2", "option3");
            }

            if (args.length == 2)
            {
                return Arrays.asList("option3", "option4");
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
