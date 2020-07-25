package com.molean.isletopiabungeetweaker;

import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Events;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.BotConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Calendar;

public class Neon implements Listener {
    private static Bot bot;

    public static Bot getBot() {
        return bot;
    }

    public Neon() {
        Bukkit.getPluginManager().registerEvents(this, IsletopiaBungeeTweaker.getPlugin());
        Bukkit.getScheduler().runTaskAsynchronously(IsletopiaBungeeTweaker.getPlugin(), () -> {
            bot = BotFactoryJvm.newBot(1604249679, "123asd..", new BotConfiguration() {
                {
                    fileBasedDeviceInfo("deviceInfo.json");
                }
            });
            bot.login();
            Events.registerEvents(bot, new SimpleListenerHost() {
                @EventHandler
                public void onGroupMessage(GroupMessageEvent event) {
                    if (Calendar.getInstance().getTimeInMillis() / 1000 - event.getTime() > 30) {
                        return;
                    }
                    StringBuilder plainMessage = new StringBuilder();
                    String nameCard = event.getSender().getNameCard();
                    MessageChain message = event.getMessage();
                    for (int i = 0; i < message.size(); i++) {
                        SingleMessage singleMessage = message.get(i);
                        plainMessage.append(singleMessage.contentToString());
                    }
                    Request broadcastRequest = new Request();
                    broadcastRequest.setType("chat");
                    broadcastRequest.set("message", plainMessage.toString());
                    broadcastRequest.set("player", nameCard);
                    for (String subServer : IsletopiaBungeeTweaker.getSubServers()) {
                        broadcastRequest.setTarget(subServer);
                        Client.send(broadcastRequest);
                    }
                }
            });

            bot.join();
        });

    }
}

