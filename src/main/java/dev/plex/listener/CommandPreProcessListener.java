package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPreProcessListener extends PlexListener
{
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        PlexPlayer plexPlayer = plugin.getPlayerCache().getPlexPlayer(player.getUniqueId());
        String msg = event.getMessage(); // full command including leading slash

        // Remove the leading slash
        String content = msg.startsWith("/") ? msg.substring(1) : msg;

        // Check the entire command, NOT just args
        FilterResult result = FilterEngine.check(content);
        if (!result.matched())
        {
            return; // do nothing
        }

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () ->
        {
            if (!player.isOnline())
            {
                return; // do nothing... cause what else could you do
            }

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Anvil);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Anvil);
            ChatFilterModule.logFilteredMessage(PlexUtils.mmDeserialize(
                    "<red>Player " + player + " has been permanently banned for executing /" + content));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Anvil);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Anvil));
        });
    }
}
