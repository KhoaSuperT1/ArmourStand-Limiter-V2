package com.xSavior_of_God.ArmorStandLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.json.JSONObject;

import com.xSavior_of_God.ArmorStandLimiter.api.events.onArmorStandRemoveEvent;

public class Utils {

    public static void apiRequest(final Map<String, Object> PAR, final URL URL) throws IOException {
        if (URL == null) {
            return;
        }
        StringBuilder postData = new StringBuilder();

        for (Map.Entry<String, Object> param : PAR.entrySet()) {
            if (postData.length() != 0)
                postData.append('&');

            postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8.name()));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8.name()));
        }

        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
        try {
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            try (OutputStream stream = conn.getOutputStream()) {
                stream.write(postDataBytes);
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                while (bufferedReader.readLine() != null) {
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    public static void apiRequestDiscord(URL url, JSONObject json) throws IOException {
        if (url == null || json == null) {
            return;
        }
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        try {
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-by-xSavior_of_God");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }
            connection.getInputStream().close();
        } finally {
            connection.disconnect();
        }
    }

    public static boolean checkArmorStand_for113PLUS(ArmorStand arm) {
        return Main.ChecksDisableIfIsInvulnerable && arm.isInvulnerable();
    }

    /**
     * Returns true if the armor stand should be removed
     *
     * @param  arm Armor stand
     * @return boolean
     */
    public static boolean checkArmorStand(ArmorStand arm) {
        if (arm == null) {
            return false;
        }
        onArmorStandRemoveEvent event = new onArmorStandRemoveEvent(arm, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        String customName = arm.getCustomName();
        if (customName != null && arm.isCustomNameVisible() && Main.ChecksDisableIfNameContains != null
                && !Main.ChecksDisableIfNameContains.isEmpty()
                && Main.ChecksDisableIfNameContains.contains(customName))
            return false;
        if (Main.ChecksDisableIfNamed && (customName != null && !customName.isEmpty() && arm.isCustomNameVisible()))
            return false;
        if (Main.ChecksDisableIfHasArms && arm.hasArms())
            return false;
        if (Main.ChecksDisableIfHasNotBasePlate && !arm.hasBasePlate())
            return false;
        EntityEquipment equipment = arm.getEquipment();
        if (equipment != null) {
            if (Main.ChecksDisableIfHasHelmet && equipment.getHelmet() != null && equipment.getHelmet().getType() != Material.AIR)
                return false;
            if (Main.ChecksDisableIfHasChestPlate && equipment.getChestplate() != null && equipment.getChestplate().getType() != Material.AIR)
                return false;
            if (Main.ChecksDisableIfHasLeggings && equipment.getLeggings() != null && equipment.getLeggings().getType() != Material.AIR)
                return false;
            if (Main.ChecksDisableIfHasBoots && equipment.getBoots() != null && equipment.getBoots().getType() != Material.AIR)
                return false;
        }
        if (!Main.LEGACY && checkArmorStand_for113PLUS(arm))
            return false;
        if (Main.ChecksDisableIfIsSmall && arm.isSmall())
            return false;
        if (Main.ChecksDisableIfIsInvisible && !arm.isVisible())
            return false;
        return true;
    }
}
