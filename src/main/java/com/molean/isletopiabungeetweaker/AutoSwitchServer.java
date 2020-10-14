package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoSwitchServer implements Listener {
    public AutoSwitchServer() {
        Bukkit.getPluginManager().registerEvents(this, IsletopiaBungeeTweaker.getPlugin());
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(IsletopiaBungeeTweaker.getPlugin(), () -> {
            Player player = event.getPlayer();
            if (player.isOp()) {
                Bukkit.getLogger().info(event.getPlayer().getName() + " is OP, skip dispatch.");
                return;
            }
            String server = getParameter(player.getName(), "server");
            if (server == null) {
                List<String> servers = new ArrayList<>(IsletopiaBungeeTweaker.getServers());
                servers.remove(IsletopiaBungeeTweaker.getServerName());
                String defaultServer = servers.get(new Random().nextInt(servers.size()));
                setParameter(player.getName(), "server", defaultServer);
                server = defaultServer;
                Bukkit.getLogger().info(event.getPlayer().getName() + "'s server is null, use random server: " + server);
            }
            String lastServer = getParameter(player.getName(), "lastServer");
            if (lastServer != null) {
                Bukkit.getLogger().info(event.getPlayer().getName() + "'s lastServer is not null, try to dispatch.");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(lastServer);
                player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
            } else {
                Bukkit.getLogger().info(event.getPlayer().getName() + "'s lastServer is null, try to dispatch to server.");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server);
                player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    public static String getParameter(String player, String key) {
        return ParameterDao.get(player, key);
    }

    public static void setParameter(String player, String key, String value) {
        ParameterDao.set(player, key, value);
    }
}
