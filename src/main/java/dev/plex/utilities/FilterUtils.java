package dev.plex.utilities;

import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class FilterUtils
{
    public static void filterTriggeredAlert(PlexPlayer player, ViolationSource source)
    {
        PlexUtils.broadcast(PlexUtils.messageComponent("filterTriggered", Bukkit.getConsoleSender(), source, player));
    }

    public static void discordAlert(PlexPlayer player, ViolationSource source)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **Player " + player.getName() + " has been permanently banned for triggering the " + source.name().toLowerCase() + " filter**");
    }

    public static void crashPlayer(Player victim)
    {
        if (victim == null) return;

        victim.spawnParticle(
                Particle.ASH,
                victim.getLocation(),
                Integer.MAX_VALUE,
                1, 1, 1, 1,
                null,
                true
        );
    }

    public static Component kickMessage(ViolationSource source)
    {
        return PlexUtils.mmCustomDeserialize(
                """
                        <dark_red><b>!! CAUGHT !!</b></dark_red>
                        
                        <gray>Prohibited language detected.</gray>
                        <gray>Source: <white>: <source>
                        <red>This server enforces a zero-tolerance policy for discrimination""",
                Placeholder.unparsed("source", source.name())
        );
    }
}
