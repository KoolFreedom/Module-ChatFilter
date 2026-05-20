package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatListener extends PlexListener
{
    private final ChatFilterModule module;

    public ChatListener(ChatFilterModule module)
    {
        this.module = module;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    private void onPlayerChatMessageThatIsASlur(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        Optional<? extends PlexPlayerView> plexPlayerOpt = module.api().players().byUuid(player.getUniqueId());
        if (plexPlayerOpt.isEmpty()) return;
        PlexPlayerView plexPlayer = plexPlayerOpt.get();

        String rawMessage = event.getMessage();
        FilterResult result = FilterEngine.check(rawMessage);
        if (!result.matched()) return;

        event.setCancelled(true);
        player.sendMessage(String.format(event.getFormat(), player.getDisplayName(), rawMessage));

        module.api().scheduler().runEntity(player, () ->
        {
            if (!player.isOnline()) return;

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Chat);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Chat);
            ChatFilterModule.logFilteredMessage(module.api().messages().miniMessage(
                    "<red>Player " + player.getName() + " has been permanently banned for saying: " + rawMessage));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Chat);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Chat));
        });
    }
}