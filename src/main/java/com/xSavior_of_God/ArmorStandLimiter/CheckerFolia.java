package com.xSavior_of_God.ArmorStandLimiter;

import com.xSavior_of_God.ArmorStandLimiter.notifications.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckerFolia {
    final AtomicBoolean isChunkRunning = new AtomicBoolean(false);
    final AtomicInteger chunkRegionTaskCounter = new AtomicInteger(0);
    final AtomicBoolean isBlockRunning = new AtomicBoolean(false);
    final AtomicInteger blockRegionTaskCounter = new AtomicInteger(0);

    public CheckerFolia() {
        if (Main.armorStandLimitBlockTaskEnabled) taskBlock();
        if (Main.armorStandLimitChunkTaskEnabled) taskChunk();
    }

    private void taskBlock() {
        long periodTicks = Math.max(1L, Main.armorStandLimitBlockTaskRefresh * 20L);
        Main.scheduler.runTaskTimer(Main.instance, () -> {
            if (Main.instance == null || !Main.instance.isEnabled()) return;
            if (blockRegionTaskCounter.get() > 0 || !isBlockRunning.compareAndSet(false, true)) return;

            try {
                for (final World w : Bukkit.getServer().getWorlds()) {
                    if (Main.ChecksDisabledWorlds.contains(w.getName())) continue;
                    Chunk[] loadedChunks = w.getLoadedChunks();
                    if (loadedChunks.length < 1) continue;

                    for (final Chunk c : loadedChunks) {
                        blockRegionTaskCounter.incrementAndGet();
                        try {
                            Main.scheduler.runAsRegionScheduler(Main.instance, c, () -> {
                                try {
                                    Entity[] entities = c.getEntities();
                                    if (entities == null || entities.length < 1) {
                                        return;
                                    }
                                    Map<Location, List<Entity>> counterBlock = new HashMap<Location, List<Entity>>();

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
                                                try {
                                                    ArmorStand arm = (ArmorStand) e;
                                                    arm.remove();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    });
                                    counterBlock.clear();
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                } finally {
                                    blockRegionTaskCounter.decrementAndGet();
                                }
                            }, false);
                        } catch (Throwable t) {
                            blockRegionTaskCounter.decrementAndGet();
                        }
                    }
                }
            } finally {
                isBlockRunning.set(false);
            }
        }, periodTicks, periodTicks);
    }

    private void taskChunk() {
        long periodTicks = Math.max(1L, Main.armorStandLimitChunkTaskRefresh * 20L);
        Main.scheduler.runTaskTimer(Main.instance, () -> {
            if (Main.instance == null || !Main.instance.isEnabled()) return;
            if (chunkRegionTaskCounter.get() > 0 || !isChunkRunning.compareAndSet(false, true)) return;

            try {
                for (final World w : Bukkit.getServer().getWorlds()) {
                    if (Main.ChecksDisabledWorlds.contains(w.getName())) continue;
                    Chunk[] loadedChunks = w.getLoadedChunks();
                    if (loadedChunks.length < 1) {
                        continue;
                    }

                    for (final Chunk c : loadedChunks) {
                        chunkRegionTaskCounter.incrementAndGet();
                        try {
                            Main.scheduler.runAsRegionScheduler(Main.instance, c, () -> {
                                Map<Chunk, List<Entity>> counterChunk = new HashMap<Chunk, List<Entity>>();
                                try {
                                    Entity[] entities = c.getEntities();
                                    if (entities == null || entities.length < 1) {
                                        return;
                                    }
                                    for (final Entity e : entities) {
                                        if (e instanceof ArmorStand && Utils.checkArmorStand((ArmorStand) e)) {
                                            counterChunk.computeIfAbsent(c, k -> new ArrayList<Entity>()).add(e);
                                        }
                                    }

                                    counterChunk.forEach((chunk, counter) -> {
                                        if (counter.size() >= Main.armorStandLimitChunkTrigger) {
                                            Notifications.send(chunk, counter.size());
                                            for (Entity e : counter) {
                                                try {
                                                    ArmorStand arm = (ArmorStand) e;
                                                    arm.remove();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    });

                                    counterChunk.clear();
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                } finally {
                                    chunkRegionTaskCounter.decrementAndGet();
                                }
                            }, false);
                        } catch (Throwable t) {
                            chunkRegionTaskCounter.decrementAndGet();
                        }
                    }
                }
            } finally {
                isChunkRunning.set(false);
            }
        }, periodTicks, periodTicks);
    }
}
