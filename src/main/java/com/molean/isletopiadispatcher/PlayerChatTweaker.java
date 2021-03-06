package com.molean.isletopiadispatcher;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatTweaker implements Listener {
    public PlayerChatTweaker() {
        Bukkit.getPluginManager().registerEvents(this, IsletopiaDispatcher.getPlugin());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        BungeeUtils.universalChat(event.getPlayer(), event.getMessage());
    }
}
