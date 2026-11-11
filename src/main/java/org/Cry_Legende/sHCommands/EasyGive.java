package org.Cry_Legende.sHCommands;

import org.Cry_Legende.sHCommands.network.RemoteTargets;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * /easygiveall (alias /ega) :
 * <ul>
 *   <li>sans argument, le menu où le staff se donne des items (permission easygiveall.givemenu) ;</li>
 *   <li>{@code reload}, relit easygiveall.yml et messages.yml (easygiveall.reload) ;</li>
 *   <li>une commande à deux mots ou plus, lancée pour chaque joueur connecté (easygiveall.giveall).</li>
 * </ul>
 */
final class EasyGive implements CommandExecutor {

    private final SHCommands plugin;
    private final Messages messages;
    private final GiveAll giveAll;
    private final GiveMenu menu;

    EasyGive(SHCommands plugin, Messages messages, RemoteTargets remote) {
        this.plugin = plugin;
        this.messages = messages;
        this.giveAll = new GiveAll(plugin, messages, remote);
        this.menu = new GiveMenu(plugin, messages, loadSettings());
    }

    /** Enregistre la commande et l'écoute du menu. */
    void register() {
        PluginCommand command = plugin.getCommand("easygiveall");
        command.setExecutor(this);
        Bukkit.getPluginManager().registerEvents(menu, plugin);
        plugin.getLogger().info("/ega : " + menu.itemCount() + " item(s) dans le menu.");

        // Si un autre plugin a déjà pris /ega, la commande ne répond que sous shcommands:ega
        Bukkit.getScheduler().runTask(plugin, () -> {
            PluginCommand owner = Bukkit.getPluginCommand("ega");
            if (owner != null && owner.getPlugin() != plugin) {
                plugin.getLogger().warning("/ega appartient encore à " + owner.getPlugin().getName()
                    + " : retirer ce plugin pour que SHCommands le reprenne.");
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("ega.console"));
            return true;
        }
        if (args.length == 0) {
            if (!player.hasPermission("easygiveall.givemenu")) {
                player.sendMessage(messages.get("ega.noperm"));
            } else if (menu.busy(player)) {
                player.sendMessage(messages.get("ega.already-give"));
            } else {
                menu.open(player);
            }
        } else if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("reload")) {
                player.sendMessage(messages.get("ega.unknown"));
            } else if (!player.hasPermission("easygiveall.reload")) {
                player.sendMessage(messages.get("ega.noperm"));
            } else {
                reload(player);
            }
        } else if (player.hasPermission("easygiveall.giveall")) {
            giveAll.run(player, args);
        } else {
            player.sendMessage(messages.get("ega.noperm"));
        }
        return true;
    }

    private void reload(Player player) {
        try {
            menu.setSettings(GiveSettings.load(plugin));
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "easygiveall.yml illisible, l'ancien menu reste en place", e);
            player.sendMessage(messages.get("ega.reload-failed"));
            return;
        }
        messages.reload();
        player.sendMessage(messages.get("ega.reloaded", "%count%", String.valueOf(menu.itemCount())));
    }

    /** Au démarrage : un easygiveall.yml illisible laisse un menu vide plutôt que d'empêcher le plugin de se charger. */
    private GiveSettings loadSettings() {
        try {
            return GiveSettings.load(plugin);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "easygiveall.yml illisible, le menu /ega est vide", e);
            return new GiveSettings("&lStaff &8| Menu de give", 30, "Annuler", true, List.of());
        }
    }
}
