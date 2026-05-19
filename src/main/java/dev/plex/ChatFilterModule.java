package dev.plex;

import dev.plex.api.PlexApi;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.api.punishment.PunishmentRequest;
import dev.plex.command.ObliterateCommand;
import dev.plex.config.ModuleConfig;
import dev.plex.filter.FilterEngine;
import dev.plex.listener.AnvilListener;
import dev.plex.listener.ChatListener;
import dev.plex.listener.CommandPreProcessListener;
import dev.plex.listener.SignListener;
import dev.plex.module.PlexModule;
import dev.plex.api.punishment.PunishmentType;
import dev.plex.utilities.ViolationSource;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class ChatFilterModule extends PlexModule
{
    @Getter
    private static ChatFilterModule module;

    @Getter
    private ModuleConfig config;

    @Override
    public void load()
    {
        module = this;
        config = new ModuleConfig(this, "chatfilter/config.yml", "config.yml");
        config.load();
    }

    public static PlexApi getApi()
    {
        return module.api();
    }

    @Override
    public void enable()
    {
        registerCommand(new ObliterateCommand());
        registerListener(new AnvilListener());
        registerListener(new ChatListener());
        registerListener(new CommandPreProcessListener());
        registerListener(new SignListener());

        FilterEngine.reload();

        addDefaultMessage("castingOblivion", "<red>{0} is casting oblivion over {1}", "0 - The command sender", "1 - The target");
        addDefaultMessage("playerEviscerated", "<red>{1} will be completely eviscerated", "1 - The target");
        addDefaultMessage("playerEradicated", "<red>{1} has been eradicated from existence!", "1 - The target");
        addDefaultMessage("obliterateReason", "You've met with a terrible fate, haven't you?");
        addDefaultMessage("targetPermBanned", "<red>{0} - Permanently banning {1}", "0 - The command sender", "1 - The target");
        addDefaultMessage("filterTriggered", "<red>{0} - {1} filter has been triggered by {2}",
                "0 - The server's console (this needs to be here)", "1- The filter source that was triggered", "2 - Who triggered it");
    }

    @Override
    public void disable()
    {
        // Unregistering listeners / commands is handled by Plex
    }

    public static void punishPlayer(PlexPlayerView player, ViolationSource source)
    {
        PunishmentRequest request = new PunishmentRequest(
                player.uuid(),
                player.uuid(),        // punisher = self, since it's an auto-ban
                "ChatFilter",         // punisher display name
                player.ips().getLast(),
                player.name(),
                PunishmentType.BAN,
                "Hate Speech (" + source + ")",
                false,                // customTime
                true,                 // active
                null                  // endDate — null = permanent
        );
        getApi().punishments().punish(player, request);
    }

    public static void logFilteredMessage(Component message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("plex.chatfilter.admin")) continue;

            player.sendMessage(Component.newline()
                    .append(Component.text("[", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Chat Filter", NamedTextColor.RED))
                    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                    .append(message)
                    .appendNewline());
        }

        String plain = PlainTextComponentSerializer
                .plainText().serialize(message);
        getApi().logging().info(plain);
    }
}
