package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import com.molean.isletopianetwork.Response;
import com.molean.playerparameter.PlayerParameter;
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
        Player player = event.getPlayer();
        if (player.isOp())
            return;
        String lastServer = PlayerParameter.getParameter(player.getName(), "lastServer");
        if (lastServer != null) {
            if (!IsletopiaBungeeTweaker.getSubServers().contains(lastServer)) {
                player.kickPlayer("服务器异常, 请稍后进入.");
                return;
            }
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(lastServer);
            player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
        } else {
            String server = PlayerParameter.getParameter(player.getName(), "server");
            if (server == null) {
                server = chooseServer(player);
                PlayerParameter.setParameter(player.getName(), "server", server);
            }
            if (!IsletopiaBungeeTweaker.getSubServers().contains(server)) {
                player.kickPlayer("服务器异常, 请稍后进入.");
                return;
            }
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
        }
    }

    public String chooseServer(Player player) {
        List<String> subServers = IsletopiaBungeeTweaker.getSubServers();
        for (String subServer : subServers) {
            Request request = new Request(subServer, "getPlotNumber");
            request.set("player", player.getName());
            Response response = Client.send(request);
            int plotNumber = 0;
            if (response.getStatus().equalsIgnoreCase("successfully")) {
                plotNumber = Integer.parseInt(response.get("return"));
            }
            if (plotNumber > 0) {
                return subServer;
            }
        }
        return PlayerParameter.getParameter("Molean", "defaultServer");
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
                    String message = "Update uuid to " + subServer + " wrongly";
                    IsletopiaBungeeTweaker.getPlugin().getLogger().severe(message);
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}
