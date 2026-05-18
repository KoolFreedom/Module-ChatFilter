package dev.plex;

import dev.plex.cache.DataUtils;
import dev.plex.command.ObliterateCommand;
import dev.plex.config.ModuleConfig;
import dev.plex.listener.FilterListener;
import dev.plex.module.PlexModule;
import dev.plex.player.PlexPlayer;
import dev.plex.punishment.Punishment;
import dev.plex.punishment.PunishmentType;
import dev.plex.util.PlexLog;
import dev.plex.utilities.ViolationSource;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
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

    @Override
    public void enable()
    {
        registerCommand(new ObliterateCommand());
        registerListener(new FilterListener());

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

    public static void punishPlayer(PlexPlayer plexPlayer, ViolationSource source)
    {
        Player player = DataUtils.getPlayer(plexPlayer.getUuid()).getPlayer();

        Punishment punishment = new Punishment(plexPlayer.getUuid(), Bukkit.getPlayerUniqueId(player.getName()));
        punishment.setType(PunishmentType.BAN);
        punishment.setPunishedUsername(plexPlayer.getName());
        punishment.setEndDate(null);
        punishment.setCustomTime(false);
        punishment.setActive(true);
        punishment.setReason("Hate Speech (" + source + ")");
        punishment.setIp(plexPlayer.getIps().getLast());
        Plex.get().getPunishmentManager().punish(plexPlayer, punishment);
    }

    public static void logFilteredMessage(Component message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("kfc.admin")) continue;

            player.sendMessage(Component.newline()
                    .append(Component.text("[", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Chat Filter", NamedTextColor.RED))
                    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                    .append(message)
                    .appendNewline());
        }
        PlexLog.log(message);
    }
}
