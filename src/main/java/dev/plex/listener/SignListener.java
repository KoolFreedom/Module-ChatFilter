package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Optional;

public class SignListener extends PlexListener
{
    private final ChatFilterModule module;

    public SignListener(ChatFilterModule module)
    {
        this.module = module;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onSignWrite(SignChangeEvent event)
    {
        Player player = event.getPlayer();
        Optional<? extends PlexPlayerView> plexPlayerOpt = module.api().players().byUuid(player.getUniqueId());
        if (plexPlayerOpt.isEmpty()) return;
        PlexPlayerView plexPlayer = plexPlayerOpt.get();

        for (String line : event.getLines())
        {
            if (line == null) continue;

            FilterResult result = FilterEngine.check(line);
            if (!result.matched()) continue;

            event.setCancelled(true);
            final String matchedLine = line;

            module.api().scheduler().runEntity(player, () ->
            {
                if (!player.isOnline()) return;

                ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Sign);
                FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Sign);
                ChatFilterModule.logFilteredMessage(module.api().messages().miniMessage(
                        "<red>Player " + player.getName() + " has been permanently banned for writing '" + matchedLine + "' on a sign"));
                FilterUtils.discordAlert(plexPlayer, ViolationSource.Sign);
                FilterUtils.crashPlayer(player);
                player.kick(FilterUtils.kickMessage(ViolationSource.Sign));
            });
            return;
        }
    }
}