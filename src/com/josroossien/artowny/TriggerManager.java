package com.josroossien.artowny;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class TriggerManager {

    private ARTowny art;
    private HashMap<Vector, Trigger> triggers = new HashMap<Vector, Trigger>();


    public TriggerManager(ARTowny art) {
        this.art = art;
    }

    public Trigger getTrigger(Location location) {
        if (!triggers.containsKey(location.toVector())) {
            triggers.put(location.toVector(), new Trigger(location));
        }
        return triggers.get(location.toVector());
    }

    public boolean hasTrigger(Location location) {
        return triggers.containsKey(location.toVector());
    }

    public void cancelAll() {
        for (Trigger trigger : triggers.values()) {
            trigger.cancelSignal();
        }
    }

}
