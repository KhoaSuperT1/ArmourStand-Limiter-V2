package com.xSavior_of_God.ArmorStandLimiter.notifications;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;

import com.xSavior_of_God.ArmorStandLimiter.Main;

public class Notifications {
    private static volatile boolean
            notificationsEnabled,
            telegramEnabled,
            discordEnabled,
            consoleEnabled,
            onlinePlayerEnabled;
    private static volatile String
            telegramAPI,
            telegramToken,
            telegramMessage,
            telegramChatID,
            discordColor,
            discordWebhook,
            discordTitle,
            discordDescription,
            discordMessage,
            consoleMessage,
            onlinePlayerMessage,
            onlinePlayerPermission;

    public Notifications() {
        notificationsEnabled = Main.instance.getConfig().getBoolean("Notifications.Enabled");
        telegramEnabled = Main.instance.getConfig().getBoolean("Notifications.Telegram.Enabled");
        telegramAPI = Main.instance.getConfig().getString("Notifications.Telegram.API");
        telegramToken = Main.instance.getConfig().getString("Notifications.Telegram.Token");
        telegramMessage = Main.instance.getConfig().getString("Notifications.Telegram.Message");
        telegramChatID = Main.instance.getConfig().getString("Notifications.Telegram.ChatID");
        if (telegramEnabled)
            new Telegram(telegramAPI, telegramToken, telegramMessage, telegramChatID);
        discordColor = Main.instance.getConfig().getString("Notifications.Discord.Color");
        discordEnabled = Main.instance.getConfig().getBoolean("Notifications.Discord.Enabled");
        discordWebhook = Main.instance.getConfig().getString("Notifications.Discord.Webhook");
        discordTitle = Main.instance.getConfig().getString("Notifications.Discord.Title");
        discordDescription = Main.instance.getConfig().getString("Notifications.Discord.Description");
        discordMessage = Main.instance.getConfig().getString("Notifications.Discord.Message");
        if (discordEnabled)
            new Discord(discordWebhook, discordTitle, discordDescription, discordMessage, discordColor);
        consoleEnabled = Main.instance.getConfig().getBoolean("Notifications.Console.Enabled");
        consoleMessage = Main.instance.getConfig().getString("Notifications.Console.Message");
        onlinePlayerEnabled = Main.instance.getConfig().getBoolean("Notifications.OnlinePlayer.Enabled");
        onlinePlayerPermission = Main.instance.getConfig().getString("Notifications.OnlinePlayer.Permission");
        onlinePlayerMessage = Main.instance.getConfig().getString("Notifications.OnlinePlayer.Message");
    }

    private static boolean hasNotifyPermission(org.bukkit.entity.Player player, String perm) {
        if (player.isOp()) {
            return true;
        }
        if (perm == null || perm.isEmpty()) {
            return false;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        if ("armostandlimiter.notify".equals(perm)) {
            return player.hasPermission("armorstandlimiter.notify");
        }
        if ("armorstandlimiter.notify".equals(perm)) {
            return player.hasPermission("armostandlimiter.notify");
        }
        return false;
    }

    public static void send(final Chunk CHUNK, final int COUNTER) {
        if (!notificationsEnabled || CHUNK == null || CHUNK.getWorld() == null) {
            return;
        }
        final String worldName = CHUNK.getWorld().getName();
        final int chunkX = CHUNK.getX();
        final int chunkZ = CHUNK.getZ();

        if (consoleEnabled && consoleMessage != null) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.translateAlternateColorCodes('&',
                            consoleMessage.replace("{x}", chunkX + "").replace("{z}", chunkZ + "")
                                    .replace("{world}", worldName).replace("{counter}", COUNTER + "")
                                    .replace("{max}", Main.armorStandLimitChunkTrigger + "").replace("{type}", "chunk")));
        }

        if (onlinePlayerEnabled && onlinePlayerMessage != null && Main.instance != null && Main.instance.isEnabled() && Main.scheduler != null) {
            final String perm = onlinePlayerPermission;
            final String msgTemplate = onlinePlayerMessage;
            Main.scheduler.runTask(Main.instance, () -> {
                String formatted = ChatColor.translateAlternateColorCodes('&',
                        msgTemplate.replace("{x}", chunkX + "").replace("{z}", chunkZ + "")
                                .replace("{world}", worldName).replace("{counter}", COUNTER + "")
                                .replace("{max}", Main.armorStandLimitChunkTrigger + "").replace("{type}", "chunk"));
                Bukkit.getServer().getOnlinePlayers().forEach(p -> {
                    if (hasNotifyPermission(p, perm)) {
                        p.sendMessage(formatted);
                    }
                });
            });
        }

        if ((telegramEnabled || discordEnabled) && Main.instance != null && Main.instance.isEnabled() && Main.scheduler != null) {
            final Location loc = new Location(CHUNK.getWorld(), chunkX, 0, chunkZ);
            Main.scheduler.runTaskAsynchronously(Main.instance, () -> {
                if (telegramEnabled) {
                    Telegram.messageBuilder(loc, COUNTER, Main.armorStandLimitChunkTrigger, true);
                }
                if (discordEnabled) {
                    Discord.messageBuilder(loc, COUNTER, Main.armorStandLimitChunkTrigger, true);
                }
            });
        }
    }

    public static void send(final Location LOC, final int COUNTER) {
        if (!notificationsEnabled || LOC == null || LOC.getWorld() == null) {
            return;
        }
        final String worldName = LOC.getWorld().getName();
        final double locX = LOC.getX();
        final double locZ = LOC.getZ();

        if (consoleEnabled && consoleMessage != null) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.translateAlternateColorCodes('&',
                            consoleMessage.replace("{x}", locX + "").replace("{z}", locZ + "")
                                    .replace("{world}", worldName).replace("{counter}", COUNTER + "")
                                    .replace("{max}", Main.armorStandLimitBlockTrigger + "").replace("{type}", "xyz")));
        }

        if (onlinePlayerEnabled && onlinePlayerMessage != null && Main.instance != null && Main.instance.isEnabled() && Main.scheduler != null) {
            final String perm = onlinePlayerPermission;
            final String msgTemplate = onlinePlayerMessage;
            Main.scheduler.runTask(Main.instance, () -> {
                String formatted = ChatColor.translateAlternateColorCodes('&',
                        msgTemplate.replace("{x}", locX + "").replace("{z}", locZ + "")
                                .replace("{world}", worldName).replace("{counter}", COUNTER + "")
                                .replace("{max}", Main.armorStandLimitBlockTrigger + "").replace("{type}", "xyz"));
                Bukkit.getServer().getOnlinePlayers().forEach(p -> {
                    if (hasNotifyPermission(p, perm)) {
                        p.sendMessage(formatted);
                    }
                });
            });
        }

        if ((telegramEnabled || discordEnabled) && Main.instance != null && Main.instance.isEnabled() && Main.scheduler != null) {
            final Location locCopy = LOC.clone();
            Main.scheduler.runTaskAsynchronously(Main.instance, () -> {
                if (telegramEnabled) {
                    Telegram.messageBuilder(locCopy, COUNTER, Main.armorStandLimitBlockTrigger);
                }
                if (discordEnabled) {
                    Discord.messageBuilder(locCopy, COUNTER, Main.armorStandLimitBlockTrigger);
                }
            });
        }
    }
}
