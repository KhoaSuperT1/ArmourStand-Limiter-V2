package com.xSavior_of_God.ArmorStandLimiter.notifications;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;

import com.xSavior_of_God.ArmorStandLimiter.Utils;

public class Telegram {
    private static volatile String message;
    private static volatile String chatID;
    private static volatile URL url;

    public Telegram(final String API, final String TOKEN, final String MESSAGE, final String CHAT_ID) {
        Telegram.message = MESSAGE != null ? MESSAGE : "";
        Telegram.chatID = CHAT_ID;
        if (API != null && TOKEN != null && !API.trim().isEmpty() && !TOKEN.trim().isEmpty()) {
            try {
                url = new URL(API + TOKEN + "/sendMessage");
            } catch (MalformedURLException e) {
                url = null;
                e.printStackTrace();
            }
        } else {
            url = null;
        }
    }

    public static void messageBuilder(final Location LOC, final int COUNTER, final int LIMIT, Boolean... TYPES) {
        if (LOC == null || LOC.getWorld() == null || message == null) {
            return;
        }
        final long Time_now = System.currentTimeMillis();
        DateFormat date_format = new SimpleDateFormat("yyyy");
        final String Year = date_format.format(new Date(Time_now));
        final double X = LOC.getX();
        final double Z = LOC.getZ();
        boolean TYPE = false;
        if (TYPES.length > 0 && TYPES[0] != null) TYPE = TYPES[0];
        final String WORLD = LOC.getWorld().getName();
        final String MESS = message.replace("%empty%", "\u200b").replace("{x}", X + "").replace("{z}", Z + "").replace("{world}", WORLD).replace("{counter}", COUNTER + "").replace("{max}", LIMIT + "").replace("{type}", TYPE ? "chunk" : "xyz");
        sendMessage(MESS + "\n \n<code>Created by xSavior_of_God @ 2020/" + Year + " HeroxPlugins</code>");
    }

    public static void sendMessage(String text) {
        if (url == null || chatID == null || chatID.trim().isEmpty() || text == null) {
            return;
        }
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("chat_id", chatID);
        parameters.put("text", text);
        parameters.put("parse_mode", "HTML");
        try {
            Utils.apiRequest(parameters, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
