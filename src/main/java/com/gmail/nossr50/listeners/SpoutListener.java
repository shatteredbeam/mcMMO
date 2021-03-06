package com.gmail.nossr50.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.HUDmmo;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.spout.SpoutStuff;
import com.gmail.nossr50.util.Users;

public class SpoutListener implements Listener {
    //Why do we have this here? We never use it...
    mcMMO plugin = null;

    public SpoutListener(mcMMO pluginx) {
        plugin = pluginx;
    }

    /**
     * Monitor SpoutCraftEnable events.
     *
     * @param event The event to watch
     */
    @EventHandler
    public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
        SpoutPlayer sPlayer = event.getPlayer();
        PlayerProfile PPs = Users.getProfile(sPlayer);

        //TODO: Add custom titles based on skills
        if (Config.getInstance().getShowPowerLevelForSpout()) {
            sPlayer.setTitle(sPlayer.getName()+ "\n" + ChatColor.YELLOW + "P" + ChatColor.GOLD + "lvl"
        + ChatColor.WHITE+"." + ChatColor.GREEN + String.valueOf(PPs.getPowerLevel()));
        }

        if (sPlayer.isSpoutCraftEnabled()) {
            SpoutStuff.playerHUDs.put(sPlayer, new HUDmmo(sPlayer, plugin)); //Setup Party HUD stuff
            
            PPs.toggleSpoutEnabled();
        }
    }
}