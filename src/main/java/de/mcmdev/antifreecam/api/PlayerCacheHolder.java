package de.mcmdev.antifreecam.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlayerCacheHolder<Cache> {

    private final Supplier<Cache> cacheFactory;
    private final Map<UUID, Cache> caches;

    public PlayerCacheHolder(final Supplier<Cache> cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.caches = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(new InnerListener(), JavaPlugin.getProvidingPlugin(getClass()));
    }

    public void prepare(final Player player) {
        caches.put(player.getUniqueId(), cacheFactory.get());
    }

    public void clear(final Player player) {
        caches.remove(player.getUniqueId());
    }

    public Cache get(final Player player) {
        return caches.computeIfAbsent(player.getUniqueId(), uuid -> cacheFactory.get());
    }

    public final class InnerListener implements org.bukkit.event.Listener {

        @EventHandler
        public void onJoin(final PlayerJoinEvent event) {
            PlayerCacheHolder.this.prepare(event.getPlayer());
        }

        @EventHandler
        public void onQuit(final PlayerQuitEvent event) {
            PlayerCacheHolder.this.clear(event.getPlayer());
        }

    }

}
