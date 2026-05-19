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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener extends PlexListener
{
    @EventHandler @SuppressWarnings("deprecation")
    public void onAnvilRename(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory inventory)) return;
        if (event.getSlot() != 2) return; // we only want the result slot

        Player player = (Player) event.getWhoClicked();
        PlexPlayer plexPlayer = plugin.getPlayerCache().getPlexPlayer(player.getUniqueId());
        ItemStack item = inventory.getItem(2);

        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = meta.getDisplayName();

        FilterResult result = FilterEngine.check(name);

        if (!result.matched()) return; // you're safe....for now

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return; // awe man :(

            ChatFilterModule.punishPlayer(plexPlayer, ViolationSource.Anvil);
            FilterUtils.filterTriggeredAlert(plexPlayer, ViolationSource.Anvil);
            ChatFilterModule.logFilteredMessage(PlexUtils.mmDeserialize(
                    "<red>Player " + player + " has been permanently banned for renaming their item to " + name));
            FilterUtils.discordAlert(plexPlayer, ViolationSource.Anvil);
            FilterUtils.crashPlayer(player);
            player.kick(FilterUtils.kickMessage(ViolationSource.Anvil));
        });
    }
}
