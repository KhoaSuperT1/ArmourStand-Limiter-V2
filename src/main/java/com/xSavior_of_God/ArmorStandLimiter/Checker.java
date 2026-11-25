package com.xSavior_of_God.ArmorStandLimiter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.xSavior_of_God.ArmorStandLimiter.notifications.Notifications;

public class Checker {
    long myTime = System.currentTimeMillis();
    final AtomicBoolean isChunkRunning = new AtomicBoolean(false);
    final AtomicBoolean isBlockRunning = new AtomicBoolean(false);
    private volatile boolean tpsLookupInitialized = false;
    private volatile boolean tpsUnavailable = false;
    private volatile Method serverGetTpsMethod;
    private volatile Object minecraftServerInstance;
    private volatile Field recentTpsField;
    private volatile int tpsTaskId = -1;

    public Checker() {
        if (Main.TPSMeterEnabled) TPSMeterTask();
        if (Main.armorStandLimitBlockTaskEnabled) taskBlock();
        if (Main.armorStandLimitChunkTaskEnabled) taskChunk();
    }

    private double readTps() {
        if (tpsUnavailable) {
            return 20.0;
        }
        if (!tpsLookupInitialized) {
            synchronized (this) {
                if (!tpsLookupInitialized) {
                    try {
                        Method m = Bukkit.getServer().getClass().getMethod("getTPS");
                        m.setAccessible(true);
                        serverGetTpsMethod = m;
                    } catch (Throwable ignored) {
                        try {
                            Class<?> craftServer = null;
                            try {
                                craftServer = Class.forName("org.bukkit.craftbukkit.CraftServer");
                            } catch (Throwable inner) {
                                String pkg = Bukkit.getServer().getClass().getPackage().getName();
                                String[] parts = pkg.split("\\.");
                                if (parts.length > 3) {
                                    craftServer = Class.forName("org.bukkit.craftbukkit." + parts[3] + ".CraftServer");
                                }
                            }
                            if (craftServer != null) {
                                Object mcServer = craftServer.getMethod("getServer").invoke(Bukkit.getServer());
                                Field field = mcServer.getClass().getField("recentTps");
                                field.setAccessible(true);
                                minecraftServerInstance = mcServer;
                                recentTpsField = field;
                            } else {
                                disableTpsMeter("TPS meter unavailable on this server implementation.");
                            }
                        } catch (Throwable t) {
                            disableTpsMeter("TPS meter disabled: unable to access server TPS (" + t.getClass().getSimpleName() + ").");
                        }
                    }
                    tpsLookupInitialized = true;
                }
            }
        }
        if (tpsUnavailable) {
            return 20.0;
        }
        try {
            if (serverGetTpsMethod != null) {
                double[] tps = (double[]) serverGetTpsMethod.invoke(Bukkit.getServer());
                if (tps != null && tps.length > 0) {
                    return tps[0];
                }
            }
            if (recentTpsField != null && minecraftServerInstance != null) {
                double[] tps = (double[]) recentTpsField.get(minecraftServerInstance);
                if (tps != null && tps.length > 0) {
                    return tps[0];
                }
            }
        } catch (Throwable t) {
            disableTpsMeter("TPS meter disabled after read error: " + t.getClass().getSimpleName());
        }
        return 20.0;
    }

    private void disableTpsMeter(String reason) {
        if (!tpsUnavailable) {
            tpsUnavailable = true;
            if (Main.instance != null) {
                Main.instance.getLogger().warning(reason);
            }
            if (tpsTaskId > 0 && Main.scheduler != null) {
                Main.scheduler.cancelTask(tpsTaskId);
                tpsTaskId = -1;
            }
        }
    }

    public void TPSMeterTask() {
        tpsTaskId = Main.scheduler.runTaskTimer(Main.instance, () -> {
            if (Main.instance == null || !Main.instance.isEnabled() || tpsUnavailable) {
                return;
            }
            double TPS = readTps();
            if (tpsUnavailable) {
                return;
            }
            if (TPS < Main.TPSMeterTrigger && myTime < System.currentTimeMillis()) {
                myTime = System.currentTimeMillis() + 1000L * 60L * 2L;
                clearBlock();
                clearChunk();
            }
        }, 1L, 1L);
    }

    private void taskBlock() {
        long periodTicks = Math.max(1L, Main.armorStandLimitBlockTaskRefresh * 20L);
        Main.scheduler.runTaskTimer(Main.instance, this::clearBlock, periodTicks, periodTicks);
    }

    private void taskChunk() {
        long periodTicks = Math.max(1L, Main.armorStandLimitChunkTaskRefresh * 20L);
        Main.scheduler.runTaskTimer(Main.instance, this::clearChunk, periodTicks, periodTicks);
    }

    public void clearBlock() {
        if (Main.instance == null || !Main.instance.isEnabled()) return;
        if (!isBlockRunning.compareAndSet(false, true)) return;

        try {
            for (final World w : Bukkit.getServer().getWorlds()) {
                if (Main.ChecksDisabledWorlds.contains(w.getName())) continue;
                Chunk[] loadedChunks = w.getLoadedChunks();
                if (loadedChunks.length < 1) continue;

                Map<Location, List<Entity>> counterBlock = new HashMap<Location, List<Entity>>();
                for (final Chunk c : loadedChunks) {
                    try {
                        Entity[] entities = c.getEntities();
                        if (entities == null || entities.length < 1) continue;

                        for (final Entity e : entities) {
                            if (e instanceof ArmorStand && Utils.checkArmorStand((ArmorStand) e)) {
                                Location loc = e.getLocation();

                                loc.setY(0);
                                int x = (int) loc.getX();
                                int z = (int) loc.getZ();
                                loc.setX(x);
                                loc.setZ(z);
                                loc.setPitch(0);
                                loc.setYaw(0);
                                Vector vec = new Vector();
                                loc.setDirection(vec);

                                counterBlock.computeIfAbsent(loc, k -> new ArrayList<Entity>()).add(e);
                            }
                        }

                        counterBlock.forEach((location, counter) -> {
                            if (counter.size() >= Main.armorStandLimitBlockTrigger) {
                                Notifications.send(location, counter.size());

                                for (Entity e : counter) {
                                    Main.scheduler.runTask(Main.instance, () -> {
                                        try {
                                            ArmorStand arm = (ArmorStand) e;
                                            arm.remove();
                                        } catch (Exception ignored) {
                                        }
                                    });
                                }
                            }
                        });

                        counterBlock.clear();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            isBlockRunning.set(false);
        }
    }

    public void clearChunk() {
        if (Main.instance == null || !Main.instance.isEnabled()) return;
        if (!isChunkRunning.compareAndSet(false, true)) return;

        try {
            for (final World w : Bukkit.getServer().getWorlds()) {
                if (Main.ChecksDisabledWorlds.contains(w.getName())) continue;
                Chunk[] loadedChunks = w.getLoadedChunks();
                if (loadedChunks.length < 1) {
                    continue;
                }

                Map<Chunk, List<Entity>> counterChunk = new HashMap<Chunk, List<Entity>>();
                for (final Chunk c : loadedChunks) {
                    try {
                        Entity[] entities = c.getEntities();
                        if (entities == null || entities.length < 1) continue;
                        for (final Entity e : entities) {
                            if (e instanceof ArmorStand && Utils.checkArmorStand((ArmorStand) e)) {
                                counterChunk.computeIfAbsent(c, k -> new ArrayList<Entity>()).add(e);
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }

                    counterChunk.forEach((chunk, counter) -> {
                        if (counter.size() >= Main.armorStandLimitChunkTrigger) {
                            Notifications.send(chunk, counter.size());
                            for (Entity e : counter) {
                                Main.scheduler.runTask(Main.instance, () -> {
                                    try {
                                        ArmorStand arm = (ArmorStand) e;
                                        arm.remove();
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        }
                    });
                    counterChunk.clear();
                }
            }
        } finally {
            isChunkRunning.set(false);
        }
    }
}
