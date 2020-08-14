package com.molean.isletopiabungeetweaker;

import com.molean.isletopiabungeetweaker.DBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniversalParameter {
    public static String getParameter(String player, String key) {
        return DBUtils.get(player, key);
    }

    public static void setParameter(String player, String key, String value) {
        DBUtils.set(player, key, value);
    }

    public static void addParameter(String player, String key, String value) {
        String before = DBUtils.get(player, key);
        if (before == null || before.trim().equals("")) {
            DBUtils.set(player, key, value);
        } else {
            List<String> strings = Arrays.asList(before.split(","));
            List<String> newStrings = new ArrayList<>(strings);
            newStrings.add(value);
            String join = String.join(",", newStrings);
            DBUtils.set(player, key, join);
        }
    }

    public static void removeParameter(String player, String key, String value) {
        String before = DBUtils.get(player, key);
        if (before == null || before.trim().equals("")) {
            DBUtils.set(player, key, value);
        } else {
            List<String> strings = Arrays.asList(before.split(","));
            List<String> newStrings = new ArrayList<>(strings);
            newStrings.remove(value);
            if (newStrings.size() == 0) {
                DBUtils.set(player, key, null);
            } else {
                String join = String.join(",", newStrings);
                DBUtils.set(player, key, join);
            }

        }
    }

    public static void unsetParameter(String player, String key) {
        DBUtils.set(player, key, null);
    }

}
