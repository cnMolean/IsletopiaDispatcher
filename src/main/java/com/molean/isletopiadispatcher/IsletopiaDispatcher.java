package com.molean.isletopiadispatcher;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getScheduler;


public final class IsletopiaDispatcher extends JavaPlugin implements Listener, PluginMessageListener {

    private static IsletopiaDispatcher plugin;

    public static IsletopiaDispatcher getPlugin() {
        return plugin;
    }

    private static String serverName;

    public static String getServerName() {
        return serverName;
    }

    private static final Map<String, Long> serverPlayTime = new HashMap<>();

    public static Map<String, Long> getServerPlayTime() {
        return new HashMap<>(serverPlayTime);
    }

    public static String getMinTimeServer() {
        Long min = Long.MAX_VALUE;
        String minKey = null;
        for (String s : serverPlayTime.keySet()) {
            Long aLong = serverPlayTime.get(s);
            if (aLong < min) {
                min = aLong;
                minKey = s;
            }
        }
        return minKey;
    }

    private static final List<String> onlinePlayers = new ArrayList<>();

    public static List<String> getOnlinePlayers() {
        return new ArrayList<>(onlinePlayers);
    }

    private static final List<String> servers = new ArrayList<>();

    public static List<String> getServers() {
        return new ArrayList<>(servers);
    }


    @Override
    public void onEnable() {
        serverName = new File(System.getProperty("user.dir")).getName();

        plugin = this;
        new AutoSwitchServer();
        new PlayerChatTweaker();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        getScheduler().runTaskTimerAsynchronously(this, this::updates, 0, 20);
        getScheduler().runTaskTimerAsynchronously(this, this::updatePlayTime, 0, 20 * 60);
    }

    public void updates() {
        updateOnlinePlayers();
        updateServerName();
        updateServers();
    }

    public void updatePlayTime() {

        long start = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000;
        for (String server : getServers()) {
            if (!getServers().contains(server)){
                continue;
            }
            if(!server.startsWith("server")){
                continue;
            }

            long serverRecentPlayTime = PlayTimeStatisticsDao.getServerRecentPlayTime(server, start);
            serverPlayTime.put(server, serverRecentPlayTime);
        }

    }

    public void updateOnlinePlayers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");
        out.writeUTF("PlayerList");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaDispatcher.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public void updateServerName() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        out.writeUTF("GetServer");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaDispatcher.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public void updateServers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        out.writeUTF("GetServers");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaDispatcher.getPlugin(), "BungeeCord", out.toByteArray());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, this::updates);
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("updateUUID");
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            msgout.writeUTF(event.getPlayer().getName());
            msgout.writeUTF(event.getPlayer().getUniqueId().toString());
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
            getServer().sendPluginMessage(IsletopiaDispatcher.getPlugin(), "BungeeCord", out.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase("PlayerList")) {
            String server = in.readUTF();
            String[] playerList = in.readUTF().split(", ");
            onlinePlayers.clear();
            onlinePlayers.addAll(Arrays.asList(playerList));
        } else if (subChannel.equalsIgnoreCase("GetServer")) {
            serverName = in.readUTF();
        } else if (subChannel.equalsIgnoreCase("GetServers")) {
            String[] serverList = in.readUTF().split(", ");
            servers.clear();
            servers.addAll(Arrays.asList(serverList));
        }
    }
}
