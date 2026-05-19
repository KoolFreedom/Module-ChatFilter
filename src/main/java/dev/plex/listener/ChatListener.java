package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.cache.PlayerCache;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener extends PlexListener
{
    @EventHandler
    @SuppressWarnings("deprecation")
    private void onPlayerChatMessageThatIsASlur(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlexPlayer plexPlayer = plugin.getPlayerCache().getPlexPlayer(player.getUniqueId());
        String rawMessage = event.getMessage();

        FilterResult result = FilterEngine.check(rawMessage);
        if (!result.matched()) return; // safe message

        // Cancel message for all viewers
        event.setCancelled(true);

        // Echo back the "unfiltered format" so players see what they said
        player.sendMessage(String.format(event.getFormat(), player.getDisplayName(), rawMessage));

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Chat);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Chat);
            ChatFilterModule.logFilteredMessage(PlexUtils.mmDeserialize(
                    "<red>Player " + player + " has been permanently banned for saying " + rawMessage));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Chat);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Chat));
        });
    }
}
