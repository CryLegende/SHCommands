package org.Cry_Legende.sHCommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Ce que le staff doit donner pour remplir un %paramètre% de la commande d'un item du menu /ega. */
record Modifier(Type type, int min, int max, List<String> values) {

    enum Type { SELF, NONE, ANY, RANGE, CONNECTED, VALUES }

    private static final Pattern RANGE = Pattern.compile("(-?\\d+)-(\\d+)");

    /** Lit la valeur du paramètre dans easygiveall.yml ; null si elle n'a aucun sens. */
    static Modifier parse(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        switch (value) {
            case "SELF": return new Modifier(Type.SELF, 0, 0, List.of());
            case "NONE": return new Modifier(Type.NONE, 0, 0, List.of());
            case "ANY": return new Modifier(Type.ANY, 0, 0, List.of());
            case "CONNECTED": return new Modifier(Type.CONNECTED, 0, 0, List.of());
            default: break;
        }
        Matcher range = RANGE.matcher(value);
        if (range.matches()) {
            try {
                int min = Integer.parseInt(range.group(1));
                int max = Integer.parseInt(range.group(2));
                return min <= max ? new Modifier(Type.RANGE, min, max, List.of()) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (value.contains("/")) {
            List<String> values = Arrays.stream(value.split("/")).filter(v -> !v.isEmpty()).toList();
            return values.isEmpty() ? null : new Modifier(Type.VALUES, 0, 0, values);
        }
        return null;
    }

    /** Valeurs permises, séparées par des /, telles qu'écrites dans la config. */
    String allowed() {
        return String.join("/", values);
    }

    /**
     * Valide ce que le staff a tapé dans le chat et rend le texte à mettre dans la commande, ou null s'il est refusé.
     * À appeler sur le fil principal (CONNECTED consulte les joueurs du serveur).
     */
    String accept(String input) {
        String text = input.trim();
        switch (type) {
            case NONE:
                return text.isEmpty() ? null : text;
            case ANY:
                return number(text);
            case RANGE: {
                String number = number(text);
                if (number == null) return null;
                int n = Integer.parseInt(number);
                return n >= min && n <= max ? number : null;
            }
            case CONNECTED: {
                Player player = Bukkit.getPlayer(text);
                return player == null ? null : player.getName();
            }
            case VALUES:
                return values.contains(text) ? text : null;
            default:
                return null;
        }
    }

    private static String number(String text) {
        try {
            return String.valueOf(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
