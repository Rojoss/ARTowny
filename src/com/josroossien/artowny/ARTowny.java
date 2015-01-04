package com.josroossien.artowny;

import com.clashwars.cwcore.CWCore;
import com.clashwars.cwcore.utils.CWUtil;
import com.josroossien.artowny.events.DuelEvents;
import com.josroossien.artowny.events.MainEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ARTowny extends JavaPlugin {

    private static ARTowny instance;
    private Economy econ;

    private DuelManager duelManager;
    private TriggerManager triggerManager;

    private final Logger log = Logger.getLogger("Minecraft");


    @Override
    public void onDisable() {
        log("disabled");

        triggerManager.cancelAll();

        getServer().getScheduler().cancelAllTasks();
    }

    @Override
    public void onEnable() {
        instance = this;

        econ = CWCore.inst().GetDM().getEconomy();
        if (econ == null) {
            log("Vault couldn't be loaded! Trigger signs won't work.");
        }

        duelManager = new DuelManager();
        triggerManager = new TriggerManager(this);

        registerEvents();

        log("loaded successfully");
    }


    public static ARTowny inst() {
        return instance;
    }

    public Economy getEconomy() {
        return econ;
    }

    public void log(Object msg) {
        log.info("[ARTowny " + getDescription().getVersion() + "] " + msg.toString());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("artowny")) {
            sender.sendMessage(CWUtil.integrateColor("&8===== &4&lArchaicRealms towny pvp plugin &8====="));
            sender.sendMessage(CWUtil.integrateColor("&6Author&8: &5worstboy32(jos)"));
            sender.sendMessage(CWUtil.integrateColor("&7At the &c&lnegative Z-axis&c PvP is disabled&7 in the wild. " +
                    "Towns can still toggle pvp on/off for their town or for plots. At the positive Z axis there is just regular pvp in the wild. " +
                    "This doesn't mean griefing and raiding is allowed. With this plugin you can also do &4/duel &7to duel someone in no pvp zone."));
            sender.sendMessage(CWUtil.integrateColor("&6More info&8: &9http://archaicrealms.com/towny#PvP"));
            return true;
        }

        if (label.equalsIgnoreCase("duel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("[Duel] This is a player command only.");
                return true;
            }

            Player player = (Player)sender;
            Player target = null;

            if (args.length < 1) {
                target = CWUtil.getTargetedPlayer(player, 20);
                if (target == null) {
                    player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cSpecify a player or look at a player when using this."));
                    return true;
                }
            }

            if (args.length >= 1 && args[0].equalsIgnoreCase("accept")) {
                Duel duel = getDM().findDuel(player.getUniqueId());
                if (duel != null && !duel.isStarted()) {
                    if (duel.start()) {
                        duel.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &6The duel has started!"));
                        duel.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &6You have 5 minutes to kill the other player."));
                        duel.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &6If you need to surrender type &5/surrender"));
                    } else {
                        player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cThe duel couldn't be started!"));
                    }
                } else {
                    player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cThere is no pending duel!"));
                }
                return true;
            }

            if (getDM().getDuel(player.getUniqueId()) != null || getDM().findDuel(player.getUniqueId()) != null) {
                player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cYou're already in a duel or you have a duel pending!"));
                return true;
            }

            if (args.length >= 1) {
                target = Bukkit.getPlayer(args[0]);
            }
            if (target == null) {
                player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cInvalid player specified."));
                return true;
            }

            if (getDM().getDuel(target.getUniqueId()) != null || getDM().findDuel(target.getUniqueId()) != null) {
                player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cThat player is already in a duel!"));
                return true;
            }


            getDM().createDuel(player.getUniqueId(), target.getUniqueId());
            player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &6Request sent to &5" + target.getName() + "&6!"));
            target.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &5" + player.getName() + " &6wants to duel with you!"));
            target.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &6Type &5/duel accept &6to accept it!"));
            return true;
        }

        if (label.equalsIgnoreCase("surrender")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("[Duel] This is a player command only.");
                return true;
            }

            Player player = (Player)sender;
            Duel duel = getDM().getDuel(player.getUniqueId());
            if (duel == null) {
                duel = getDM().findDuel(player.getUniqueId());
            }

            if (duel == null) {
                player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cYou're not in a duel!"));
                return true;
            }

            if (player.getHealth() < 5) {
                player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &cYou need at least 25% health to surrender!"));
                return true;
            }

            if (duel.getPlayer() == player.getUniqueId()) {
                duel.end(duel.getTarget());
            } else {
                duel.end(duel.getPlayer());
            }
            player.sendMessage(CWUtil.integrateColor("&8[&4Duel&8] &c&lYou surrendered!"));
            return true;
        }

        return false;
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MainEvents(this), this);
        pm.registerEvents(new DuelEvents(this), this);
    }

    public DuelManager getDM() {
        return duelManager;
    }

    public TriggerManager getTM() {
        return triggerManager;
    }
}
