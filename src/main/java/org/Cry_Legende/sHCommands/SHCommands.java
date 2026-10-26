package org.Cry_Legende.sHCommands;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.Locale;

public class SHCommands extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("sm").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!sender.hasPermission("shcommands.use")) {
                    sender.sendMessage("§8§l« §f§lSH §8§l» §fCommande inconnue, essayer autre chose.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§8§l« §f§lSH §8§l» §cUsage: /sm <joueur> <message>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage("§8§l« §f§lSH §8§l» §cJoueur non trouvé !");
                    return true;
                }
                String message = String.join(" ", args).substring(args[0].length() + 1);

                // Appliquer les placeholders d'abord, au cas où ils contiendraient des marqueurs de nouvelle ligne
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    message = PlaceholderAPI.setPlaceholders(target, message);
                }

                // Remplacer les marqueurs de saut de ligne
                // L'utilisateur tape \n (qui devient \\n dans la chaîne Java) ou %nl%
                message = message.replace("\\n", "\n").replace("%nl%", "\n");

                // Remplacer les codes de couleur
                message = message.replace("&", "§");

                target.sendMessage(message);
                return true;
            }
        });

        this.getCommand("ps").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!sender.hasPermission("shcommands.use")) {
                    sender.sendMessage("§8§l« §f§lSH §8§l» §fCommande inconnue, essayer autre chose.");
                    return true;
                }
                if (args.length < 2) {
                    // J'ai renommé yaw en volume car playSound prend volume et pitch
                    sender.sendMessage("§8§l« §f§lSH §8§l» §cUsage: /ps <joueur> <son> [<volume>] [<pitch>]");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage("§8§l« §c§lSH §8§l» §7Joueur non trouvé !");
                    return true;
                }
                Sound sound = sound(args[1]);
                if (sound == null) {
                    sender.sendMessage("§8§l« §c§lSH §8§l» §7Son invalide.");
                    return true;
                }

                float volume = 1.0f; // Paramètre par défaut pour le volume
                float pitch = 1.0f;  // Paramètre par défaut pour le pitch

                if (args.length > 2) { // Argument pour le volume
                    try {
                        volume = Float.parseFloat(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§8§l« §c§lSH §8§l» §7Volume invalide (doit être un nombre, ex: 1.0).");
                        return true;
                    }
                }
                if (args.length > 3) { // Argument pour le pitch
                    try {
                        pitch = Float.parseFloat(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§8§l« §c§lSH §8§l» §7Pitch invalide (doit être un nombre, ex: 1.0).");
                        return true;
                    }
                }
                // target.playSound(location, sound, volume, pitch)
                target.playSound(target.getLocation(), sound, volume, pitch);
                return true;
            }
        });
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