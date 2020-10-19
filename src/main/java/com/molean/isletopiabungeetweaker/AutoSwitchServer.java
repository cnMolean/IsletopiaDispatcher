package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.FailedLoginEvent;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;
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
    public void onFailedLogin(FailedLoginEvent event) {
        if (event.getPlayer().getLocale().toLowerCase().startsWith("zh")) {
            event.getPlayer().sendMessage("§c登录失败, 请检查密码是否正确.");
        } else {
            event.getPlayer().sendMessage("§cLogin failed, please check your password.");
        }
    }

    @EventHandler
    public void onRegister(RegisterEvent event) {
        if (event.getPlayer().getLocale().toLowerCase().startsWith("zh")) {
            event.getPlayer().sendMessage("§c注册成功, 使用 `/login 密码` 以登录.");
        } else {
            event.getPlayer().sendMessage("§cRegister successfully, then use `/login [password]` to login.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        if (AuthMeApi.getInstance().isRegistered(event.getPlayer().getName())) {
            if (event.getPlayer().getLocale().toLowerCase().startsWith("zh")) {
                event.getPlayer().sendMessage("§c此用户名已注册, 请输入`/login 密码`以登录.");
                event.getPlayer().sendMessage("§c如果此用户名并非你注册的, 请更换一个用户名.");
            } else {
                event.getPlayer().sendMessage("§cThis username is registered.");
                event.getPlayer().sendMessage("§cType `/login [password]` to login.");
                event.getPlayer().sendMessage("§cIf this is not your registration, ");
                event.getPlayer().sendMessage("§c please consider use another username.");
            }
        }else{
            if (event.getPlayer().getLocale().toLowerCase().startsWith("zh")) {
                event.getPlayer().sendMessage("§c此用户名未注册, 请输入`/register 密码 确认密码`以注册.");
                event.getPlayer().sendMessage("§c密码长度为5~30, 不要忘记输入确认密码.");
            } else {
                event.getPlayer().sendMessage("§cThis username is not registered.");
                event.getPlayer().sendMessage("§cPlease use`/register [password] [password]` to register.");
                event.getPlayer().sendMessage("§cPassword length should between 5~12.");
                event.getPlayer().sendMessage("§cRemember input second confirm password.");
            }
        }
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
