package com.josroossien.artowny;

import com.clashwars.cwcore.utils.CWUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class Duel {

    boolean started = false;
    boolean ended = false;
    private UUID player;
    private UUID target;
    private long startTime;
    private long endTime;
    private BukkitTask runnable;

    private final int duelTime = 5 * 60 * 1000; /* 5 mins */

    public Duel(UUID player, UUID target) {
        this.player = player;
        this.target = target;

        //10 second timer to accept the duel.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!started) {
                    Bukkit.getPlayer(player).sendMessage(CWUtil.integrateColor("&8[&4&lDuel&8] &6The duel didn't get accepted!"));
                    ARTowny.inst().getDM().removeDuel(player);
                }
            }
        }.runTaskLater(ARTowny.inst(), 200);
    }

    public boolean start() {
        Bukkit.broadcastMessage(CWUtil.integrateColor("&8[&4&lDuel&8] &c" + Bukkit.getPlayer(player).getName() + " &6VS &c" + Bukkit.getPlayer(target).getName()));
        if (Bukkit.getPlayer(player) == null || !Bukkit.getPlayer(player).isOnline()) {
            forceEnd();
            return false;
        }
        started = true;
        startTime = System.currentTimeMillis();
        endTime = startTime + duelTime;

        //Timer at end of time force end duel if someone is offline make the other player winner.
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ended) {
                    boolean playerOnline = false;
                    boolean targetOnline = false;
                    if (Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).isOnline()) {
                        playerOnline = true;
                    }
                    if (Bukkit.getPlayer(target) != null && Bukkit.getPlayer(target).isOnline()) {
                        targetOnline = true;
                    }

                    if (targetOnline && playerOnline) {
                        sendMessage(CWUtil.integrateColor("&8[&4&lDuel&8] &6The time ran out so there is no winner!"));
                        ARTowny.inst().getDM().removeDuel(player);
                    } else if (playerOnline) {
                        Bukkit.getPlayer(target).sendMessage("&8[&4&lDuel&8] &6Because " + Bukkit.getPlayer(player).getName() + " went offline you won!");
                        ARTowny.inst().getDM().removeDuel(player);
                    } else if (targetOnline) {
                        Bukkit.getPlayer(player).sendMessage("&8[&4&lDuel&8] &6Because " + Bukkit.getPlayer(target).getName() + " went offline you won!");
                        ARTowny.inst().getDM().removeDuel(player);
                    }
                }
            }
        }.runTaskLater(ARTowny.inst(), duelTime / 1000 * 20);
        return true;
    }

    public void end(UUID winner) {
        if (started && !ended) {
            sendMessage(CWUtil.integrateColor("&8[&4&lDuel&8] &6" + Bukkit.getPlayer(winner).getName() + " won the duel!"));
            forceEnd();
        }
    }

    public void forceEnd() {
        ended = true;
        if (runnable != null) {
            runnable.cancel();
        }
        ARTowny.inst().getDM().removeDuel(player);
    }

    public boolean isStarted() {
        return started;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public UUID getPlayer() {
        return player;
    }

    public UUID getTarget() {
        return target;
    }

    public void sendMessage(String message) {
        if (Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).isOnline()) {
            Bukkit.getPlayer(player).sendMessage(message);
        }
        if (Bukkit.getPlayer(target) != null && Bukkit.getPlayer(target).isOnline()) {
            Bukkit.getPlayer(target).sendMessage(message);
        }
    }

}
