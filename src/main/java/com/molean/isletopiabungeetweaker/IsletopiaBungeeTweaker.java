package com.molean.isletopiabungeetweaker;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.Bukkit.getScheduler;


public final class IsletopiaBungeeTweaker extends JavaPlugin implements Listener, PluginMessageListener {

    private static IsletopiaBungeeTweaker plugin;

    public static IsletopiaBungeeTweaker getPlugin() {
        return plugin;
    }

    private static String serverName;

    public static String getServerName() {
        return serverName;
    }

    private static final List<String> onlinePlayers = new ArrayList<>();

    public static List<String> getOnlinePlayers() {
        return onlinePlayers;
    }

    private static final List<String> servers = new ArrayList<>();

    public static List<String> getServers() {
        return servers;
    }


    @Override
    public void onEnable() {
        plugin = this;
        new AutoSwitchServer();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        getScheduler().runTaskTimerAsynchronously(this, this::updates, 20, 20);
    }

    public void updates() {
        updateOnlinePlayers();
        updateServerName();
        updateServers();
    }

    public void updateOnlinePlayers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");
        out.writeUTF("PlayerList");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public void updateServerName() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        out.writeUTF("GetServer");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public void updateServers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        out.writeUTF("GetServers");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
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
            getServer().sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
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
