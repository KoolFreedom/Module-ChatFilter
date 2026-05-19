package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class AnvilListener extends PlexListener
{
    @EventHandler
    @SuppressWarnings("deprecation")
    public void onAnvilRename(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Optional<? extends PlexPlayerView> plexPlayerOpt = ChatFilterModule.getApi().players().byUuid(player.getUniqueId());
        if (plexPlayerOpt.isEmpty()) return;
        PlexPlayerView plexPlayer = plexPlayerOpt.get();

        ItemStack item = ((AnvilInventory) event.getInventory()).getItem(2);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = meta.getDisplayName();
        FilterResult result = FilterEngine.check(name);
        if (!result.matched()) return;

        event.setCancelled(true);

        ChatFilterModule.getApi().scheduler().runEntity(player, () ->
        {
            if (!player.isOnline()) return;

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Anvil);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Anvil);
            ChatFilterModule.logFilteredMessage(ChatFilterModule.getApi().messages().miniMessage(
                    "<red>Player " + player.getName() + " has been permanently banned for renaming their item to: " + name));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Anvil);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Anvil));
        });
    }
}