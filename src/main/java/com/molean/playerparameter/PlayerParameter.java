package com.molean.playerparameter;

import com.molean.isletopiabungeetweaker.ConfigUtils;
import com.molean.isletopianetwork.Client;
import com.molean.isletopianetwork.Request;
import com.molean.isletopianetwork.Response;
import com.molean.isletopianetwork.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PlayerParameter {

    public static void setParameter(String player, String key, String value) {
        ConfigUtils.reloadConfig("parameters.yml");
        ConfigUtils.getConfig("parameters.yml")
                .set(player + "." + key, value);
        ConfigUtils.saveConfig("parameters.yml");
    }


    public static void unsetParameter(String player, String key) {
        ConfigUtils.getConfig("parameters.yml")
                .set(player + "." + key, null);
        ConfigUtils.saveConfig("parameters.yml");
    }

    public static String getParameter(String player, String key) {
        ConfigUtils.reloadConfig("parameters.yml");
        return ConfigUtils.getConfig("parameters.yml")
                .getString(player + "." + key);
    }

    public static void addParameter(String player, String key, String value) {
        ConfigUtils.reloadConfig("parameters.yml");
        String s = ConfigUtils.getConfig("parameters.yml")
                .getString(player + "." + key);
        if (s == null || "".equalsIgnoreCase(s)) {
            s = value;
        } else {
            s = s + "," + value;
        }
        ConfigUtils.getConfig("parameters.yml")
                .set(player + "." + key, s);
        ConfigUtils.saveConfig("parameters.yml");
    }


    public static void removeParameter(String player, String key, String value) {
        ConfigUtils.reloadConfig("parameters.yml");
        String s = ConfigUtils.getConfig("parameters.yml").
                getString(player + "." + key);
        if (s == null | "".equalsIgnoreCase(s))
            return;
        List<String> strings = new ArrayList<>(Arrays.asList(s.split(",")));
        strings.remove(value);
        if (!strings.isEmpty()) {
            ConfigUtils.getConfig("parameters.yml").
                    set(player + "." + key, String.join(",", strings));
        } else {
            ConfigUtils.getConfig("parameters.yml").
                    set(player + "." + key, null);
        }
        ConfigUtils.saveConfig("parameters.yml");
    }
}
