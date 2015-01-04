package com.josroossien.artowny;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager {

    private Map<UUID, Duel> duels = new HashMap<UUID, Duel>();


    public void createDuel(UUID player, UUID target) {
        duels.put(player, new Duel(player, target));
    }

    public void removeDuel(UUID player) {
        duels.remove(player);
    }

    public Duel getDuel(UUID player) {
        if (!duels.containsKey(player)) {
            return null;
        }
        return duels.get(player);
    }

    public Duel findDuel(UUID target) {
        for (Duel duel : duels.values()) {
            if (duel.getTarget() == target) {
                return duel;
            }
        }
        return null;
    }
}
