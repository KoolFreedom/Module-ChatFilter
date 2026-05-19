package dev.plex.utilities;

import dev.plex.ChatFilterModule;
import dev.plex.api.player.PlexPlayerView;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class FilterUtils
{
    public static void filterTriggeredAlert(PlexPlayerView player, ViolationSource source)
    {
        ChatFilterModule.getApi().messages().broadcast(ChatFilterModule.getApi().messages().messageComponent("filterTriggered", Bukkit.getConsoleSender(), source, player));
    }

    public static void discordAlert(PlexPlayerView player, ViolationSource source)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **Player " + player.name() + " has been permanently banned for triggering the " + source.name().toLowerCase() + " filter**");
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
        String text = """
            <dark_red><b>!! CAUGHT !!</b></dark_red>
            
            <gray>Prohibited language detected.</gray>
            <gray>Source: <white><source></white></gray>
            <red>This server enforces a zero-tolerance policy for discrimination"""
                .replace("<source>", source.name());

        return ChatFilterModule.getApi().messages().miniMessage(text);
    }
}
