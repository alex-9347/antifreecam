package de.mcmdev.antifreecam;

import de.mcmdev.antifreecam.api.PlayerCacheHolder;
import de.mcmdev.antifreecam.config.ConfigHolder;
import de.mcmdev.antifreecam.structures.StructureCache;
import de.mcmdev.antifreecam.structures.StructureHider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiFreecamPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveResource("world.yml", false);
        final ConfigHolder configHolder = new ConfigHolder(getDataFolder());
        loadYLevelHider(configHolder);
        loadStructureHider(configHolder);
    }

    private void loadYLevelHider(final ConfigHolder configHolder) {
        Bukkit.getPluginManager().registerEvents(new de.mcmdev.antifreecam.ylevel.NativeYLevelHider(configHolder, this), this);
    }

    private void loadStructureHider(final ConfigHolder configHolder) {
        final PlayerCacheHolder<StructureCache> structureCacheHolder = new PlayerCacheHolder<>(StructureCache::new);
        Bukkit.getPluginManager().registerEvents(new StructureHider(configHolder, structureCacheHolder), this);
    }
}
