package dev.plex.command;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.api.punishment.PunishmentRequest;
import dev.plex.api.punishment.PunishmentType;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.utilities.FilterUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ObliterateCommand extends SimplePlexCommand
{
    private final ChatFilterModule module;

    public ObliterateCommand(ChatFilterModule module)
    {
        super(command("obliterate")
                .description("Unleash divine punishment upon someone")
                .usage("/<command> <player> [reason]")
                .aliases("doom")
                .permission("plex.chatfilter.obliterate")
                .build());
        this.module = module;
    }
    @Override
    protected Component execute(@NotNull CommandSender commandSender, @Nullable Player player, @NotNull String[] strings)
    {
        if (strings.length == 0)
        {
            return usage();
        }

        PlexPlayerView plexPlayer = module.api().players().byName(strings[0])
                .orElseThrow(PlayerNotFoundException::new);

        Player target = getNonNullPlayer(plexPlayer.name());

        for (int i = 0; i < 30; i++)
        {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        target.setFireTicks(200);
        target.setGameMode(GameMode.ADVENTURE);

        broadcast(messageComponent("castingOblivion", commandSender, target));

        module.api().scheduler().runEntityLater(target, () ->
                broadcast(messageComponent("playerEviscerated", target)), 2);

        module.api().scheduler().runEntityLater(target, () ->
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");
            if (target.isOp()) target.setOp(false);
            if (target.isWhitelisted()) target.setWhitelisted(false);
        }, 2);

        module.api().scheduler().runEntityLater(target, () -> target.setHealth(0), 10);

        module.api().scheduler().runEntityLater(target, () ->
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

        module.api().scheduler().runEntityLater(target, () ->
                module.api().punishments().punish(plexPlayer, request), 38);
        module.api().scheduler().runEntityLater(target, () ->
                broadcast(messageComponent("targetPermBanned", commandSender, target)), 38);

        return null;
    }
}