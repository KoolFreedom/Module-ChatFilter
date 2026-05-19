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
import org.bukkit.event.block.SignChangeEvent;

public class SignListener extends PlexListener
{
    @EventHandler
    @SuppressWarnings("deprecation")
    public void onSignWrite(SignChangeEvent event)
    {
        Player player = event.getPlayer();
        PlexPlayer plexPlayer = plugin.getPlayerCache().getPlexPlayer(player.getUniqueId());

        for (String line : event.getLines())
        {
            FilterResult result = FilterEngine.check(line);
            if (result.matched())
            {
                event.setCancelled(true);

                Bukkit.getScheduler().runTask(plugin, () ->
                {
                    if (!player.isOnline()) return;

                    ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Anvil);
                    FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Anvil);
                    ChatFilterModule.logFilteredMessage(PlexUtils.mmDeserialize(
                            "<red>Player " + player + " has been permanently banned writing  '" + line + "' on a sign"));
                    FilterUtils.discordAlert(plexPlayer, ViolationSource.Anvil);
                    FilterUtils.crashPlayer(player);
                    player.kick(FilterUtils.kickMessage(ViolationSource.Anvil));
                });
                return;
            }
        }
    }
}
