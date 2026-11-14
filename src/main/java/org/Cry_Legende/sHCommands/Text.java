package org.Cry_Legende.sHCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

/**
 * Texte de /sm et /sbc : codes & (ou §), couleurs &#rrggbb, \n ou %nl% pour un retour à la ligne, et [/commande]
 * rendu cliquable, comme dans les messages des mini-jeux.
 */
final class Text {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build();
    private static final Pattern COMMAND = Pattern.compile("\\[(/[^\\[\\]]+)]");

    private Text() {
    }

    /**
     * @param text  message, placeholders déjà résolus
     * @param hover texte affiché au survol d'une commande cliquable (%command% : la commande), vide pour aucun
     */
    static Component parse(String text, String hover) {
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
            .build());
    }
}
