package com.molean.isletopiabungeetweaker;

import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import com.molean.isletopianetwork.Response;
import com.molean.isletopianetwork.Server;
import net.mamoe.mirai.contact.Group;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.molean.playerparameter.PlayerParameter.*;

public final class IsletopiaBungeeTweaker extends JavaPlugin {

    private static IsletopiaBungeeTweaker plugin;

    public static IsletopiaBungeeTweaker getPlugin() {
        return plugin;
    }

    public static List<String> getSubServers() {
        return List.of("server1","server2");
    }

    private final Function<Request, Response> function = request -> {
        Response response = new Response();
        if (request.getType().equalsIgnoreCase("getPlayerServer")) {
            String player = request.get("player");
            String playerServer = null;

            for (String subServer : IsletopiaBungeeTweaker.getSubServers()) {
                Request playerRequest = new Request(subServer, "getPlotNumber");
                playerRequest.set("player", player);
                Response playerResponse = Client.send(playerRequest);
                int plotNumber = 0;
                if (playerResponse.getStatus().equalsIgnoreCase("successfully")) {
                    plotNumber = Integer.parseInt(playerResponse.get("return"));
                }
                if (plotNumber > 0) {
                    playerServer = subServer;
                    break;
                }
            }

            if (playerServer == null) {
                response.setStatus("no plot");
            } else {
                response.setStatus("successfully");
                response.set("return", playerServer);
            }
        } else if (request.getType().equalsIgnoreCase("getOnlinePlayers")) {
            Request broadcastRequest = new Request();
            broadcastRequest.setType("getOnlinePlayers");
            List<String> allPlayers = new ArrayList<>();
            for (String subServer : getSubServers()) {
                broadcastRequest.setTarget(subServer);
                Response playersResponse = Client.send(broadcastRequest);
                String[] players = playersResponse.get("players").split(",");
                allPlayers.addAll(Arrays.asList(players));
            }
            response.set("players", String.join(",", allPlayers));
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("chat")) {
            String message = request.get("message");
            String player = request.get("player");
            Request broadcastRequest = new Request();
            broadcastRequest.setType("chat");
            broadcastRequest.set("message", message);
            broadcastRequest.set("player", player);
            for (String subServer : getSubServers()) {
                broadcastRequest.setTarget(subServer);
                Response chatResponse = Client.send(broadcastRequest);
                if (chatResponse == null) {
                    getLogger().severe("Send chat to " + subServer + " wrongly.");
                }
            }
            Group group = Neon.getBot().getGroup(483653595);
            group.sendMessageAsync("<" + player + "> " + message);
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("broadcast")) {
            String message = request.get("message");
            Bukkit.broadcastMessage(message);
            Request broadcastRequest = new Request();
            broadcastRequest.setType("broadcast");
            broadcastRequest.set("message", message);
            for (String subServer : getSubServers()) {
                broadcastRequest.setTarget(subServer);
                Response broadcastResponse = Client.send(broadcastRequest);
                if (broadcastResponse == null) {
                    getLogger().severe("Broadcast message to " + subServer + " wrongly.");
                }
            }
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("getParameter")) {
            String player = request.getData().get("player");
            String key = request.getData().get("key");
            response.setStatus("successfully");
            response.getData().put("return", getParameter(player, key));
        } else if (request.getType().equalsIgnoreCase("setParameter")) {
            String player = request.getData().get("player");
            String key = request.getData().get("key");
            String value = request.getData().get("value");
            setParameter(player, key, value);
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("addParameter")) {
            String player = request.getData().get("player");
            String key = request.getData().get("key");
            String value = request.getData().get("value");
            addParameter(player, key, value);
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("unsetParameter")) {
            String player = request.getData().get("player");
            String key = request.getData().get("key");
            unsetParameter(player, key);
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("removeParameter")) {
            String player = request.getData().get("player");
            String key = request.getData().get("key");
            String value = request.getData().get("value");
            removeParameter(player, key, value);
            response.setStatus("successfully");
        }
        return response;
    };

    @Override
    public void onEnable() {
        plugin = this;
        ConfigUtils.setupConfig(this);
        ConfigUtils.configOuput("parameters.yml");
        new Server().setupNetwork();
        new Client(function).register("dispatcher");
        new AutoSwitchServer();
//        new Neon();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }
}
