package com.josroossien.artowny;

import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Trigger {

    private World world;
    private Vector location;
    private boolean signal = false;
    private Material originalMaterial = Material.AIR;
    private BukkitTask runnable;

    public Trigger(Location location) {
        this.world = location.getWorld();
        this.location = location.toVector();
    }

    public boolean hasSignal() {
        return signal;
    }

    public void sendSignal(int ticks) {
        signal = true;
        originalMaterial = world.getBlockAt(location.toLocation(world)).getType();
        world.getBlockAt(location.toLocation(world)).setType(Material.REDSTONE_BLOCK);
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                world.getBlockAt(location.toLocation(world)).setType(originalMaterial);
                signal = false;
            }
        }.runTaskLater(ARTowny.inst(), ticks);
    }

    public void cancelSignal() {
        signal = false;
        if (originalMaterial != null) {
            world.getBlockAt(location.toLocation(world)).setType(originalMaterial);
        }
        if (runnable != null) {
            runnable.cancel();
        }
    }
}
