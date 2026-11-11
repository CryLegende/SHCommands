package org.Cry_Legende.sHCommands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Contenu d'easygiveall.yml : réglages du menu /ega et items proposés. */
record GiveSettings(String title, int timeLimit, String cancel, boolean logs, List<GiveItem> items) {

    private static final int MAX_ITEMS = 54;

    /**
     * Lit easygiveall.yml (créé au premier démarrage). Un item invalide est signalé dans la console et ignoré.
     *
     * @throws IOException                 fichier illisible
     * @throws InvalidConfigurationException YAML mal formé
     */
    static GiveSettings load(JavaPlugin plugin) throws IOException, InvalidConfigurationException {
        File file = new File(plugin.getDataFolder(), "easygiveall.yml");
        if (!file.exists()) {
            plugin.saveResource("easygiveall.yml", false);
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);

        List<GiveItem> items = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection("items");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection entry = section.getConfigurationSection(id);
                if (entry == null) continue;
                try {
                    items.add(GiveItem.load(id, entry));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("easygiveall.yml : l'item \"" + id + "\" est ignoré (" + e.getMessage() + ").");
                }
            }
        }
        if (items.size() > MAX_ITEMS) {
            plugin.getLogger().warning("easygiveall.yml : le menu tient " + MAX_ITEMS + " items, les suivants sont ignorés.");
            items = new ArrayList<>(items.subList(0, MAX_ITEMS));
        }
        return new GiveSettings(
            yaml.getString("menu.title", "&lStaff &8| Menu de give"),
            Math.max(1, yaml.getInt("menu.time-limit", 30)),
            yaml.getString("menu.cancel-word", "Annuler"),
            yaml.getBoolean("menu.logs", true),
            List.copyOf(items));
    }
}
