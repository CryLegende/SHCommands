package org.Cry_Legende.sHCommands;

import org.Cry_Legende.sHCommands.network.RemoteTargets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * /ega &lt;commande&gt; : la commande est exécutée par la console pour chaque joueur connecté, {@code %player%} devenant
 * son pseudo et {@code %me%} celui du staff. Avec SHNetwork, les joueurs des autres serveurs la reçoivent de la
 * console de leur serveur, une fois chacun ; ceux de ce serveur la reçoivent d'ici.
 */
final class GiveAll {

    private final SHCommands plugin;
    private final Messages messages;
    /** Joueurs des autres serveurs du réseau ; null sans SHNetwork. */
    private final RemoteTargets remote;

    GiveAll(SHCommands plugin, Messages messages, RemoteTargets remote) {
        this.plugin = plugin;
        this.messages = messages;
        this.remote = remote;
    }

    /** La commande console telle qu'elle sera lancée : sans les / de tête que la console n'attend pas. */
    static String clean(String command) {
        String line = command.trim();
        while (line.startsWith("/")) {
            line = line.substring(1).stripLeading();
        }
        return line;
    }

    /** Fil principal. {@code args} compte au moins deux éléments. */
    void run(Player staff, String[] args) {
        String typed = String.join(" ", args);
        String me = staff.getName();
        if (clean(typed).isEmpty()) {
            staff.sendMessage(messages.get("ega.unknown"));
            return;
        }

        // Copie : une commande peut faire partir un joueur pendant la boucle
        List<Player> here = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player target : here) {
            dispatch(line(typed, target.getName(), me));
        }

        if (remote == null) {
            staff.sendMessage(messages.get("ega.giveall", "%command%", typed));
            return;
        }
        int elsewhere;
        try {
            elsewhere = remote.broadcast(name -> line(typed, name, me));
        } catch (RuntimeException | LinkageError e) {
            plugin.getLogger().log(Level.WARNING, "/ega : SHNetwork n'a pas pu relayer \"" + typed + "\", seuls les joueurs de ce serveur l'ont reçue", e);
            staff.sendMessage(messages.get("ega.giveall", "%command%", typed));
            return;
        }
        plugin.getLogger().info(me + " : /ega " + typed + " (" + here.size() + " joueur(s) ici, " + elsewhere + " ailleurs)");
        staff.sendMessage(messages.get("ega.giveall-network", "%command%", typed, "%count%", String.valueOf(here.size() + elsewhere)));
    }

    private static String line(String typed, String player, String me) {
        return clean(typed.replace("%player%", player).replace("%me%", me));
    }

    private void dispatch(String command) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "/ega : échec de \"" + command + "\"", e);
        }
    }
}
