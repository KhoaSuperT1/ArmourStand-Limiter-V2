package com.xSavior_of_God.ArmorStandLimiter.utils;

import com.xSavior_of_God.ArmorStandLimiter.Checker;
import com.xSavior_of_God.ArmorStandLimiter.CheckerFolia;
import com.xSavior_of_God.ArmorStandLimiter.Main;
import com.xSavior_of_God.ArmorStandLimiter.notifications.Notifications;

public class Reload {
    public static void reload() {
        Main plugin = Main.instance;
        if (plugin == null) {
            return;
        }
        if (Main.scheduler != null) {
            Main.scheduler.cancelTasks(plugin);
        }
        Main.trackFallEntity.clear();
        plugin.reloadConfig();
        plugin.loadConfig();
        plugin.registerExternalHooks();
        new Notifications();
        if (Main.isFolia) {
            Main.checkerFolia = new CheckerFolia();
        } else {
            Main.checker = new Checker();
        }
    }
}
