package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Optional;

public class CommandPreProcessListener extends PlexListener
{
    private final ChatFilterModule module;

    public CommandPreProcessListener(ChatFilterModule module)
    {
        this.module = module;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        Optional<? extends PlexPlayerView> plexPlayerOpt = module.api().players().byUuid(player.getUniqueId());
        if (plexPlayerOpt.isEmpty()) return;
        PlexPlayerView plexPlayer = plexPlayerOpt.get();

        String msg = event.getMessage();
        String content = msg.startsWith("/") ? msg.substring(1) : msg;

        FilterResult result = FilterEngine.check(content);
        if (!result.matched()) return;

        event.setCancelled(true);

        module.api().scheduler().runEntity(player, () ->
        {
            if (!player.isOnline()) return;

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Command);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Command);
            ChatFilterModule.logFilteredMessage(module.api().messages().miniMessage(
                    "<red>Player " + player.getName() + " has been permanently banned for executing: /" + content));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Command);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Command));
        });
    }
}