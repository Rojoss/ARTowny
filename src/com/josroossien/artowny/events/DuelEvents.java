package com.josroossien.artowny.events;

import com.josroossien.artowny.ARTowny;
import com.josroossien.artowny.Duel;
import com.josroossien.artowny.DuelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DuelEvents implements Listener {

    private DuelManager dm;

    public DuelEvents(ARTowny art) {
        dm = art.getDM();
    }

    @EventHandler
    private void death(PlayerDeathEvent event) {
        UUID player = event.getEntity().getUniqueId();
        Duel duel = dm.getDuel(player);
        if (duel == null) {
            duel = dm.findDuel(player);
        }
        if (duel != null) {
            if (duel.getPlayer() == player) {
                duel.end(duel.getTarget());
            } else {
                duel.end(player);
            }
        }
    }

    //Always allow damaging players when in duel.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    private void duelDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player damager = (Player)event.getDamager();
        Player damaged = (Player)event.getEntity();

        Duel duel = dm.getDuel(damager.getUniqueId());
        if (duel == null) {
            duel = dm.findDuel(damager.getUniqueId());
        }

        if (duel != null && duel.isStarted()) {
            if ((duel.getPlayer() == damager.getUniqueId() && duel.getTarget() == damaged.getUniqueId()) || (duel.getTarget() == damager.getUniqueId() && duel.getPlayer() == damaged.getUniqueId())) {
                event.setCancelled(false);
            }
        }
    }
}
