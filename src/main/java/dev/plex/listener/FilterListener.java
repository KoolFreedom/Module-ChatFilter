package dev.plex.listener;

import dev.plex.ChatFilterModule;
import dev.plex.cache.DataUtils;
import dev.plex.filter.FilterEngine;
import dev.plex.filter.FilterResult;
import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;
import dev.plex.utilities.FilterUtils;
import dev.plex.utilities.ViolationSource;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FilterListener extends PlexListener
{
    @EventHandler @SuppressWarnings("deprecation")
    public void onAnvilRename(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory inventory)) return;
        if (event.getSlot() != 2) return; // we only want the result slot

        PlexPlayer player = (PlexPlayer) event.getWhoClicked();
        ItemStack item = inventory.getItem(2);

        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = meta.getDisplayName();

        FilterResult result = FilterEngine.check(name);

        if (!result.matched()) return; // you're safe....for now

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.getPlayer().isOnline()) return; // awe man :(

            ChatFilterModule.punishPlayer(player, ViolationSource.Anvil);
            FilterUtils.filterTriggeredAlert(player, ViolationSource.Anvil);
            ChatFilterModule.logFilteredMessage(PlexUtils.mmDeserialize(
                    "<red>Player " + player + " has been permanently banned for renaming their item to " + name));
            FilterUtils.discordAlert(player, ViolationSource.Anvil);
            FilterUtils.crashPlayer(player.getPlayer());
            player.getPlayer().kick(FilterUtils.kickMessage(ViolationSource.Anvil));
        });
    }
}
