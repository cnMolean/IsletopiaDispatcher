package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import com.molean.isletopianetwork.Response;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

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
            String server = UniversalParameter.getParameter(player.getName(), "server");
            if (server == null) {
                Bukkit.getLogger().info(event.getPlayer().getName() + "'s server is null, try to get.");
                List<String> subServers = IsletopiaBungeeTweaker.getSubServers();
                for (String subServer : subServers) {
                    Request request = new Request(subServer, "getPlotNumber");
                    request.set("player", player.getName());
                    Response response = Client.send(request);
                    if (response == null) {
                        Bukkit.getLogger().warning(player.getName() + " request plot number failed, kick player.");
                        player.kickPlayer("[2]获取信息失败, 请重新进入服务器.");
                        return;
                    }
                    int plotNumber = 0;
                    if (response.getStatus().equalsIgnoreCase("successfully")) {
                        plotNumber = Integer.parseInt(response.get("return"));
                    }
                    if (plotNumber > 0) {
                        Bukkit.getLogger().warning(player.getName() + " in " + subServer + " has plot.");
                        server = subServer;
                    }
                }
                if (server == null) {
                    Bukkit.getLogger().info(player.getName() + " has no plot, try to use default server.");
                    server = UniversalParameter.getParameter("Molean", "defaultServer");
                }
                Bukkit.getLogger().info(event.getPlayer().getName() + "'s server is now " + server + ".");
                String finalServer = server;
                Bukkit.getScheduler().runTask(IsletopiaBungeeTweaker.getPlugin(), () -> {
                    UniversalParameter.setParameter(player.getName(), "server", finalServer);
                });

            }
            String lastServer = UniversalParameter.getParameter(player.getName(), "lastServer");
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
        Bukkit.getScheduler().runTaskAsynchronously(IsletopiaBungeeTweaker.getPlugin(), () -> {
            Request request = new Request();
            request.setType("updateUUID");
            request.set("player", event.getPlayer().getName());
            request.set("uuid", event.getPlayer().getUniqueId().toString());
            for (String subServer : IsletopiaBungeeTweaker.getSubServers()) {
                request.setTarget(subServer);
                Response send = Client.send(request);
                if (send == null) {
                    Bukkit.getLogger().warning("Update " + event.getPlayer().getName() + "'s UUID to " + subServer + " failed.");
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}
