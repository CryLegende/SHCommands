package org.Cry_Legende.sHCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** /shcommands : administration du plugin. Une seule sous-commande, reload (shcommands.admin.reload). */
final class AdminCommand implements TabExecutor {

    private static final String RELOAD_PERMISSION = "shcommands.admin.reload";

    private final SHCommands plugin;
    private final Messages messages;
    private final EasyGive easyGive;

    AdminCommand(SHCommands plugin, Messages messages, EasyGive easyGive) {
        this.plugin = plugin;
        this.messages = messages;
        this.easyGive = easyGive;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reload(sender);
        } else {
            sender.sendMessage(messages.get("admin-usage", "%subcommands%", "reload"));
        }
        return true;
    }

    /**
     * Relit config.yml, messages.yml et easygiveall.yml. Les trois sont d'abord lus en mode strict : si l'un est
     * invalide, rien n'est remplacé. reseau.actif n'est lu qu'au démarrage.
     */
    private void reload(CommandSender sender) {
        for (String name : new String[] {"config.yml", "messages.yml", "easygiveall.yml"}) {
            File file = new File(plugin.getDataFolder(), name);
            if (!file.exists()) {
                continue;
            }
            try {
                new YamlConfiguration().load(file);
            } catch (IOException | InvalidConfigurationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                String text = String.valueOf(cause.getMessage()).replaceAll("\\s+", " ").trim();
                if (text.length() > 200) {
                    text = text.substring(0, 200) + "...";
                }
                plugin.getLogger().warning("Rechargement refusé, configuration précédente gardée (" + name + " : " + text + ").");
                sender.sendMessage(messages.get("admin-reload-error", "%file%", name, "%error%", text));
                return;
            }
        }
        int items;
        try {
            items = easyGive.reloadSettings();
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().warning("Rechargement refusé, ancien menu gardé (easygiveall.yml : " + e.getMessage() + ").");
            sender.sendMessage(messages.get("admin-reload-error", "%file%", "easygiveall.yml", "%error%", String.valueOf(e.getMessage())));
            return;
        }
        plugin.reloadConfig();
        messages.reload();
        sender.sendMessage(messages.get("admin-reloaded", "%count%", String.valueOf(items)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission(RELOAD_PERMISSION)) {
            return List.of();
        }
        return "reload".startsWith(args[0].toLowerCase(Locale.ROOT)) ? List.of("reload") : List.of();
    }
}
