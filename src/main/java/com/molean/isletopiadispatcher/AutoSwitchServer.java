package com.molean.isletopiadispatcher;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class AutoSwitchServer implements Listener {
    public AutoSwitchServer() {
        Bukkit.getPluginManager().registerEvents(this, IsletopiaDispatcher.getPlugin());
    }

    @EventHandler
    public void onLogin(LoginEvent event) {

        Bukkit.getScheduler().runTaskAsynchronously(IsletopiaDispatcher.getPlugin(), () -> {
            Player player = event.getPlayer();
            if (player.isOp()) {
                return;
            }
            String server = getParameter(player.getName(), "server");
            if (server == null) {
                String defaultServer = IsletopiaDispatcher.getMinTimeServer();
                setParameter(player.getName(), "server", defaultServer);
                server = defaultServer;
            }
            String lastServer = getParameter(player.getName(), "lastServer");
            if (lastServer != null) {
                server = lastServer;
            }
            String finalServer = server;
            Bukkit.getScheduler().runTaskTimerAsynchronously(IsletopiaDispatcher.getPlugin(), (task) -> {
                if (!player.isOnline()) {
                    task.cancel();
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(finalServer);
                player.sendPluginMessage(IsletopiaDispatcher.getPlugin(), "BungeeCord", out.toByteArray());

            }, 0, 20 * 10);


        });
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    public static String getParameter(String player, String key) {
        return ParameterDao.get(player, key);
    }

    public static void setParameter(String player, String key, String value) {
        ParameterDao.set(player, key, value);
    }
}
