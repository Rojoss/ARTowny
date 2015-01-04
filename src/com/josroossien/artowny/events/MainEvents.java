package com.josroossien.artowny.events;

import com.clashwars.cwcore.dependencies.CWWorldGuard;
import com.clashwars.cwcore.utils.CWUtil;
import com.josroossien.artowny.ARTowny;
import com.josroossien.artowny.Duel;
import com.josroossien.artowny.Trigger;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

public class MainEvents implements Listener {

    private final ARTowny art;

    public MainEvents(ARTowny art) {
        this.art = art;
    }


    @EventHandler
    private void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getZ() > 0) {
            player.sendMessage(CWUtil.integrateColor("&8>> &4&lPvP side &8<< &7You are on the &4&l+Z axis &7which is pvp."));
        } else {
            player.sendMessage(CWUtil.integrateColor("&8>> &2&lNo PvP side &8<< &7You are on the &2&l-Z axis &7which is no pvp."));
        }
    }

    @EventHandler
    private void playerMove(PlayerMoveEvent event) {
        if (event.getFrom().getZ() <= 0 && event.getTo().getZ() > 0) {
            event.getPlayer().sendMessage(CWUtil.integrateColor("&8>> &4&lPvP side &8<< &7You are on the &4&l+Z axis &7which is pvp."));
        } else if (event.getFrom().getZ() > 0 && event.getTo().getZ() <= 0) {
            event.getPlayer().sendMessage(CWUtil.integrateColor("&8>> &2&lNo PvP side &8<< &7You are on the &2&l-Z axis &7which is no pvp."));
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    private void damage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player damager = (Player)event.getDamager();
        Player damaged = (Player)event.getEntity();
        Location loc = damaged.getLocation();

        //If towny allows pvp (town with pvp toggled on) then allow pvp. [even on -z axis]
        TownBlock tb = TownyUniverse.getTownBlock(loc);
        if (tb != null && tb.hasTown()) {
            if (!CombatUtil.preventPvP(tb.getWorld(), tb)) {
                event.setCancelled(false);
                return;
            }
        }

        //If worldguard allows pvp (region with pvp allowed) then allow pvp. [even on -z axis]
        if (CWWorldGuard.isInRegion(damaged) && CWWorldGuard.isInRegion(damager) && CWWorldGuard.getFlag(damaged, DefaultFlag.PVP) && CWWorldGuard.getFlag(damager, DefaultFlag.PVP)) {
            event.setCancelled(false);
            return;
        }


        if (loc.getZ() <= 0) {
            Duel duel = art.getDM().getDuel(damager.getUniqueId());
            if (duel == null) {
                duel = art.getDM().findDuel(damager.getUniqueId());
            }

            if (duel == null || !duel.isStarted()) {
                damager.sendMessage(CWUtil.integrateColor("&8[&4&lAR&8] &cThere is no pvp on the negative Z axis! &7Use /duel to fight someone."));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN) {
            return;
        }

        Sign sign = (Sign)block.getState();
        if (!CWUtil.removeColour(sign.getLine(0)).equalsIgnoreCase("&9[trigger]")) {
            return;
        }
        if (sign.getLine(1).isEmpty() || sign.getLine(3).isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        if (art.getEconomy() == null) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cNo economy found."));
            return;
        }

        String owner = CWUtil.stripAllColor(sign.getLine(3));

        String priceStr = CWUtil.stripAllColor(sign.getLine(1)).replace("$", "");
        int price = 0;
        if (!priceStr.equalsIgnoreCase("free")) {
            price = Math.max(CWUtil.getInt(priceStr), 0);
        }

        int ticks = 15;
        if (!sign.getLine(2).isEmpty()) {
            ticks = Math.max(CWUtil.getInt(CWUtil.stripAllColor(sign.getLine(2))), 1);
        }

        if (art.getEconomy().getBalance(player.getName()) < price) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cYou don't have enough money."));
            return;
        }

        Block attachedBlock = getAttachedBlock(sign.getBlock());
        Trigger trigger = art.getTM().getTrigger(attachedBlock.getLocation());
        if (trigger.hasSignal()) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cThis sign is already triggered."));
            return;
        }
        trigger.sendSignal(ticks);
        player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &6Signal triggered!"));

        art.getEconomy().withdrawPlayer(player.getName(), price);
        art.getEconomy().depositPlayer(owner, price);
        if (Bukkit.getPlayer(owner) != null && Bukkit.getPlayer(owner).isOnline()) {
            Bukkit.getPlayer(owner).sendMessage(CWUtil.integrateColor("&8[&4AR&8] &6Your trigger sign was used by " + player.getName() + ", &6you received &5" + price + "$"));
        }
    }

    @EventHandler
    private void signCreate(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!event.getLine(0).equalsIgnoreCase("[trigger]")) {
            return;
        }
        if (!event.getLine(3).isEmpty()) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cLeave the last line blank!"));
            event.getBlock().breakNaturally();
            return;
        }
        if (event.getLine(1).isEmpty()) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cSpecify a price on the second line or 'free'."));
            event.getBlock().breakNaturally();
            return;
        }
        int price = CWUtil.getInt(event.getLine(1).replace("$", ""));
        if (price < 0) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cInvalid price specified!"));
            event.getBlock().breakNaturally();
            return;
        }
        if (!event.getLine(2).isEmpty() && CWUtil.getInt(event.getLine(2)) <= 0) {
            player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &cInvalid tick amount specified on the third line!"));
            event.getBlock().breakNaturally();
            return;
        }

        player.sendMessage(CWUtil.integrateColor("&8[&4AR&8] &6Trigger sign created!"));
        event.setLine(0, CWUtil.integrateColor("&9[trigger]"));
        event.setLine(1, price + "$");
        event.setLine(3, player.getName());
    }

    @EventHandler
    private void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.REDSTONE_BLOCK) {
            return;
        }

        if (art.getTM().hasTrigger(block.getLocation())) {
            if (art.getTM().getTrigger(block.getLocation()).hasSignal()) {
                event.setCancelled(true);
            }
        }
    }


    private Block getAttachedBlock(Block b) {
        MaterialData m = b.getState().getData();
        BlockFace face = BlockFace.DOWN;
        if (m instanceof Attachable) {
            face = ((Attachable) m).getAttachedFace();
        }
        return b.getRelative(face);
    }



}
