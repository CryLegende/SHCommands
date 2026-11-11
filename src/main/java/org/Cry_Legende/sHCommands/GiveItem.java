package org.Cry_Legende.sHCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Un item du menu /ega : son icône, la commande console qu'il lance et ce qu'il faut saisir pour la compléter. */
record GiveItem(String id, String name, ItemStack icon, String command, Map<String, Modifier> modifiers) {

    private static final Pattern PLACEHOLDER = Pattern.compile("%([A-Za-z0-9_.-]+)%");

    /** Texte avec codes &amp; converti en codes §, pour un message de chat. */
    static String legacy(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /** Texte avec codes &amp; pour un nom ou une ligne de description d'item : pas d'italique par défaut. */
    static Component text(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Lit l'entrée {@code items.<id>} d'easygiveall.yml.
     *
     * @throws IllegalArgumentException si l'entrée est inutilisable ; le message dit pourquoi
     */
    static GiveItem load(String id, ConfigurationSection section) {
        String name = section.getString("name", "");
        String command = section.getString("command", "");
        if (name.isBlank()) throw new IllegalArgumentException("name manquant");
        if (command.isBlank()) throw new IllegalArgumentException("command manquante");

        String materialName = section.getString("item.material", "");
        Material material = Material.matchMaterial(materialName);
        if (material == null || material.isAir() || !material.isItem()) {
            throw new IllegalArgumentException("item.material \"" + materialName + "\" inconnu");
        }

        Map<String, Modifier> modifiers = new LinkedHashMap<>();
        ConfigurationSection declared = section.getConfigurationSection("modifiers");
        if (declared != null) {
            for (String key : declared.getKeys(false)) {
                Modifier modifier = Modifier.parse(declared.getString(key));
                if (modifier == null) {
                    throw new IllegalArgumentException("modifiers." + key + " \"" + declared.getString(key) + "\" invalide");
                }
                modifiers.put(key, modifier);
            }
        }

        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(text(name));
        meta.lore(section.getStringList("item.lore").stream().map(GiveItem::text).toList());
        if (section.getBoolean("item.enchanted", false)) {
            meta.setEnchantmentGlintOverride(true);
        }
        icon.setItemMeta(meta);
        return new GiveItem(id, name, icon, command, modifiers);
    }

    /** Les %paramètres% de la commande à demander, dans l'ordre où ils apparaissent, chacun une fois. */
    List<String> parameters() {
        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(command);
        while (matcher.find()) {
            if (modifiers.containsKey(matcher.group(1))) found.add(matcher.group(1));
        }
        return List.copyOf(found);
    }
}
