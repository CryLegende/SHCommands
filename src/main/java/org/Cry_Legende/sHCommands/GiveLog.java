package org.Cry_Legende.sHCommands;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

/** Journal des gives faits depuis le menu : un fichier par jour dans plugins/SHCommands/logs, une ligne par give. */
final class GiveLog {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JavaPlugin plugin;
    private final Path directory;

    GiveLog(JavaPlugin plugin) {
        this.plugin = plugin;
        this.directory = plugin.getDataFolder().toPath().resolve("logs");
    }

    /** Écrit la ligne hors du fil principal. */
    void add(String staff, GiveItem item, String command, String reason) {
        LocalDateTime now = LocalDateTime.now();
        String line = "[" + TIME.format(now) + "] [" + staff + " | " + item.id() + " | " + item.name() + "] Commande : "
            + command + " | Raison : " + reason + System.lineSeparator();
        Path file = directory.resolve(DAY.format(now) + ".log");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Files.createDirectories(directory);
                Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Journal de /ega illisible, ligne perdue : " + line.trim(), e);
            }
        });
    }
}
