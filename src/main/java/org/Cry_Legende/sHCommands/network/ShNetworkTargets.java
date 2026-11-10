package org.Cry_Legende.sHCommands.network;

import org.Cry_Legende.sHNetwork.api.NetPlayer;
import org.Cry_Legende.sHNetwork.api.SHNetworkAPI;

import java.util.Optional;
import java.util.function.UnaryOperator;

/** Pont vers SHNetwork. Cette classe n'est chargée que si SHNetwork est installé. */
public final class ShNetworkTargets implements RemoteTargets {

    private final SHNetworkAPI api;

    private ShNetworkTargets() {
        this.api = SHNetworkAPI.get();
    }

    /** Point d'entrée : le plugin ne résout cette classe qu'à cet appel, donc jamais sans SHNetwork. */
    public static RemoteTargets open() {
        return new ShNetworkTargets();
    }

    @Override
    public boolean relay(String player, UnaryOperator<String> command) {
        Optional<NetPlayer> found = elsewhere(player);
        if (found.isEmpty()) {
            return false;
        }
        api.runFor(found.get().uuid().toString(), command.apply(found.get().name()));
        return true;
    }

    @Override
    public boolean knows(String player) {
        return elsewhere(player).isPresent();
    }

    private Optional<NetPlayer> elsewhere(String player) {
        return api.find(player).filter(p -> !p.server().equals(api.serverName()));
    }
}
