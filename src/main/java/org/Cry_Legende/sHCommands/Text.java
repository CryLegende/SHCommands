package org.Cry_Legende.sHCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

/**
 * Texte de /sm et /sbc : codes & (ou §), couleurs &#rrggbb, \n ou %nl% pour un retour à la ligne, et [/commande] ou
 * [adresse.fr] rendus cliquables, comme dans les messages des mini-jeux.
 */
final class Text {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build();
    private static final Pattern COMMAND = Pattern.compile("\\[(/[^\\[\\]]+)]");
    /** [discord.skyhand.fr], [https://skyhand.fr/vote] : l'adresse ouverte est en https quand elle n'a pas de protocole. */
    private static final Pattern LINK = Pattern.compile("\\[((?:https?://)?(?:[a-z0-9-]+\\.)+[a-z]{2,}(?:/[^\\[\\]\\s]*)?)]",
        Pattern.CASE_INSENSITIVE);

    private Text() {
    }

    /**
     * @param text      message, placeholders déjà résolus
     * @param hover     texte affiché au survol d'une commande cliquable (%command% : la commande), vide pour aucun
     * @param linkHover texte affiché au survol d'une adresse cliquable (%url% : l'adresse), vide pour aucun
     */
    static Component parse(String text, String hover, String linkHover) {
        String source = text.replace("\\n", "\n").replace("%nl%", "\n").replace('§', '&');
        Component component = LEGACY.deserialize(source);
        if (source.indexOf('[') < 0) {
            return component;
        }
        return component.replaceText(TextReplacementConfig.builder()
            .match(COMMAND)
            .replacement((match, builder) -> {
                String command = match.group(1).trim();
                builder.clickEvent(ClickEvent.runCommand(command));
                if (!hover.isEmpty()) {
                    builder.hoverEvent(HoverEvent.showText(LEGACY.deserialize(hover.replace("%command%", command))));
                }
                return builder;
            })
            .build())
            .replaceText(TextReplacementConfig.builder()
                .match(LINK)
                .replacement((match, builder) -> {
                    String url = url(match.group(1));
                    builder.clickEvent(ClickEvent.openUrl(url));
                    if (!linkHover.isEmpty()) {
                        builder.hoverEvent(HoverEvent.showText(LEGACY.deserialize(linkHover.replace("%url%", match.group(1)))));
                    }
                    return builder;
                })
                .build());
    }

    private static String url(String address) {
        return address.regionMatches(true, 0, "http", 0, 4) && address.contains("://") ? address : "https://" + address;
    }
}
