package org.Cry_Legende.sHCommands.network;

import java.util.function.UnaryOperator;

/**
 * Joueurs connectés sur un autre serveur du réseau. Seule {@link ShNetworkTargets} connaît SHNetwork : sans lui,
 * aucune classe de ce paquet n'est chargée.
 */
public interface RemoteTargets {

    /**
     * Si le joueur est connecté sur un autre serveur, y fait exécuter la commande par la console, une seule fois,
     * et rend vrai. Rend faux s'il n'est connecté nulle part ailleurs.
     *
     * @param player  pseudo tapé par l'émetteur (casse ignorée)
     * @param command commande sans {@code /} à partir du pseudo exact du joueur
     */
    boolean relay(String player, UnaryOperator<String> command);

    /** Vrai si le joueur est connecté sur un autre serveur du réseau. */
    boolean knows(String player);
}
