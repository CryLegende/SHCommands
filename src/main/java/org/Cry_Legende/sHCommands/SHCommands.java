package org.Cry_Legende.sHCommands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.Cry_Legende.sHCommands.network.RemoteTargets;
import org.Cry_Legende.sHCommands.network.ShNetworkTargets;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

public class SHCommands extends JavaPlugin {

    private Messages messages;
    /** Joueurs des autres serveurs du réseau ; null sans SHNetwork. */
    private RemoteTargets remote;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);
        enableNetwork();

        this.getCommand("sm").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!sender.hasPermission("shcommands.use")) {
                    sender.sendMessage(messages.get("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(messages.get("sm-usage"));
                    return true;
                }
                String message = String.join(" ", args).substring(args[0].length() + 1);
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    if (remote == null || !remote.relay(args[0], name -> "sm " + name + " " + message)) {
                        sender.sendMessage(messages.get("sm-player-not-found"));
                    }
                    return true;
                }
                String text = message;

                // Appliquer les placeholders d'abord, au cas où ils contiendraient des marqueurs de nouvelle ligne
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    text = PlaceholderAPI.setPlaceholders(target, text);
                }

                // Remplacer les marqueurs de saut de ligne
                // L'utilisateur tape \n (qui devient \n dans la chaîne Java) ou %nl%
                text = text.replace("\n", "\n").replace("%nl%", "\n");

                // Remplacer les codes de couleur
                text = text.replace("&", "§");

                target.sendMessage(text);
                return true;
            }
        });

        this.getCommand("ps").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!sender.hasPermission("shcommands.use")) {
                    sender.sendMessage(messages.get("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(messages.get("ps-usage"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                boolean here = target != null;
                if (!here && (remote == null || !remote.knows(args[0]))) {
                    sender.sendMessage(messages.get("ps-player-not-found"));
                    return true;
                }
                Sound sound = sound(args[1]);
                if (sound == null) {
                    sender.sendMessage(messages.get("ps-invalid-sound"));
                    return true;
                }

                float volume = 1.0f; // Paramètre par défaut pour le volume
                float pitch = 1.0f;  // Paramètre par défaut pour le pitch

                if (args.length > 2) { // Argument pour le volume
                    try {
                        volume = Float.parseFloat(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.get("ps-invalid-volume"));
                        return true;
                    }
                }
                if (args.length > 3) { // Argument pour le pitch
                    try {
                        pitch = Float.parseFloat(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.get("ps-invalid-pitch"));
                        return true;
                    }
                }
                if (!here) {
                    // Le son est contrôlé ici, joué par le serveur du joueur
                    String rest = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if (!remote.relay(args[0], name -> "ps " + name + " " + rest)) {
                        sender.sendMessage(messages.get("ps-player-not-found"));
                    }
                    return true;
                }
                // target.playSound(location, sound, volume, pitch)
                target.playSound(target.getLocation(), sound, volume, pitch);
                return true;
            }
        });
    }

    /**
     * Passe en mode réseau si SHNetwork est installé et 'reseau.actif' est vrai. La classe qui touche à SHNetwork
     * n'est résolue qu'ici : sans lui, rien n'est chargé et les commandes restent locales.
     */
    private void enableNetwork() {
        if (!getConfig().getBoolean("reseau.actif", true)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("SHNetwork")) return;
        try {
            remote = ShNetworkTargets.open();
            getLogger().info("SHNetwork détecté : /sm et /ps atteignent les joueurs de tout le réseau.");
        } catch (RuntimeException | LinkageError e) {
            remote = null;
            getLogger().log(Level.SEVERE, "SHNetwork présent mais inutilisable, commandes locales à ce serveur : " + e);
        }
    }

    /** Son lu en commande : ancien nom (ENTITY_PLAYER_LEVELUP) ou clé (minecraft:entity.player.levelup). */
    static Sound sound(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        if (s.indexOf(':') >= 0 || s.indexOf('.') >= 0) {
            NamespacedKey key = NamespacedKey.fromString(s.toLowerCase(Locale.ROOT));
            return key == null ? null : Registry.SOUND_EVENT.get(key);
        }
        try {
            return (Sound) Sound.class.getField(s.toUpperCase(Locale.ROOT)).get(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}
