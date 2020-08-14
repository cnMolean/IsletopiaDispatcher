package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import com.molean.isletopianetwork.Response;
import com.molean.isletopianetwork.Server;
import net.mamoe.mirai.contact.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class IsletopiaBungeeTweaker extends JavaPlugin implements @NotNull PluginMessageListener {

    private static IsletopiaBungeeTweaker plugin;

    public static IsletopiaBungeeTweaker getPlugin() {
        return plugin;
    }

    public static List<String> getSubServers() {
        List<String> subServers = new ArrayList<>();
        for (String registration : Client.getRegistrations()) {
            if (!registration.equalsIgnoreCase("dispatcher")) {
                subServers.add(registration);
            }
        }
        return subServers;
    }

    private final Function<Request, Response> function = request -> {
        Response response = new Response();
        Bukkit.getLogger().info("Received request " + request.getType());
        if (request.getType().equalsIgnoreCase("getPlayerServer")) {
            String player = request.get("player");
            String playerServer = null;

            for (String subServer : IsletopiaBungeeTweaker.getSubServers()) {
                Request playerRequest = new Request(subServer, "getPlotNumber");
                playerRequest.set("player", player);
                Response playerResponse = Client.send(playerRequest);
                if (playerResponse == null) {
                    response.setStatus("no response");
                    Bukkit.getLogger().warning("Request " + player + "'s plot number in " + subServer + " failed.");
                }
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
                if (playersResponse == null) {
                    Bukkit.getLogger().warning("Request online player list in " + subServer + " failed.");
                    continue;
                }
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
            if (Neon.getBot() != null) {
                Group group = Neon.getBot().getGroup(483653595);
                group.sendMessageAsync("<" + player + "> " + message);
                response.setStatus("successfully");
            } else {
                response.setStatus("bot not started");
            }

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
                    getLogger().severe("Broadcast message to " + subServer + " failed.");
                }
            }
            response.setStatus("successfully");
        } else if (request.getType().equalsIgnoreCase("sendMessage")) {
            for (String subServer : getSubServers()) {
                request.setTarget(subServer);
                Response messageResponse = Client.send(request);
                if (messageResponse.getStatus().equals("successfully")) {
                    return messageResponse;
                }
            }
            response.setStatus("not online");
        }
        return response;
    };

    @Override
    public void onEnable() {
        plugin = this;
        new Server().setupNetwork();
        new Client(function).register("dispatcher");
        new AutoSwitchServer();
        new Neon();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equalsIgnoreCase("chat")) {
            try {
                short len = in.readShort();
                byte[] msgbytes = new byte[len];
                in.readFully(msgbytes);
                DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
                String p = msgin.readUTF();
                String m = msgin.readUTF();
                if (Neon.getBot() != null) {
                    Group group = Neon.getBot().getGroup(483653595);
                    group.sendMessageAsync("<" + player + "> " + message);
                }


            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
