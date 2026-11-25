package com.xSavior_of_God.ArmorStandLimiter.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerFolia implements Scheduler {
    private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public SchedulerFolia(Plugin plugin) {
    }

    private void pruneCompletedTasks() {
        tasks.entrySet().removeIf(entry -> {
            ScheduledTask task = entry.getValue();
            if (task == null || task.isCancelled()) {
                return true;
            }
            ScheduledTask.ExecutionState state = task.getExecutionState();
            return state == ScheduledTask.ExecutionState.FINISHED
                    || state == ScheduledTask.ExecutionState.CANCELLED
                    || state == ScheduledTask.ExecutionState.CANCELLED_RUNNING;
        });
    }

    private int registerTask(ScheduledTask task) {
        pruneCompletedTasks();
        int id = counter.incrementAndGet();
        if (task != null) {
            tasks.put(id, task);
        }
        return id;
    }

    public boolean isCurrentlyRunning(int taskId) {
        ScheduledTask task = tasks.get(taskId);
        return task != null && task.getExecutionState() == ScheduledTask.ExecutionState.RUNNING;
    }

    public void cancelTask(int taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
        pruneCompletedTasks();
    }

    public void cancelTasks(Plugin plugin) {
        for (ScheduledTask task : tasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        tasks.clear();
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }

    public boolean isQueued(int taskId) {
        ScheduledTask task = tasks.get(taskId);
        return task != null && task.getExecutionState() == ScheduledTask.ExecutionState.IDLE;
    }

    public List<?> getActiveWorkers() {
        return Collections.emptyList();
    }

    public List<?> getPendingTasks() {
        return Collections.emptyList();
    }

    public int runTask(Plugin plugin, Runnable task) throws IllegalArgumentException {
        return registerTask(Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run()));
    }

    public int runTaskAsynchronously(Plugin plugin, Runnable task) throws IllegalArgumentException {
        return registerTask(Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run()));
    }

    public int runTaskLater(Plugin plugin, Runnable task, long delay) throws IllegalArgumentException {
        long safeDelay = Math.max(1L, delay);
        return registerTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), safeDelay));
    }

    public int runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) throws IllegalArgumentException {
        long delayMs = Math.max(1L, delay) * 50L;
        return registerTask(Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), delayMs, TimeUnit.MILLISECONDS));
    }

    public int runTaskTimer(Plugin plugin, Runnable task, long delay, long period) throws IllegalArgumentException {
        long safeDelay = Math.max(1L, delay);
        long safePeriod = Math.max(1L, period);
        return registerTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), safeDelay, safePeriod));
    }

    public int runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) throws IllegalArgumentException {
        long delayMs = Math.max(1L, delay) * 50L;
        long periodMs = Math.max(1L, period) * 50L;
        return registerTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), delayMs, periodMs, TimeUnit.MILLISECONDS));
    }

    public int runAsRegionScheduler(Plugin plugin, Location location, Runnable task, boolean isAsync) {
        return registerTask(Bukkit.getRegionScheduler().run(plugin, location, t -> task.run()));
    }

    public int runAsRegionScheduler(Plugin plugin, Chunk chunk, Runnable task, boolean isAsync) {
        return registerTask(Bukkit.getRegionScheduler().run(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), t -> task.run()));
    }

    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }
}
