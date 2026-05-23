package de.mcmdev.antifreecam.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigHolder {

    private final Map<String, Config> configs;

    public ConfigHolder(final File dataFolder) {
        this.configs = new ConcurrentHashMap<>();
        discoverConfigs(dataFolder);
    }

    public Optional<Config> getConfig(final String worldName) {
        return Optional.ofNullable(configs.get(worldName));
    }

    private void loadConfig(final File file) {
        final String worldName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        configs.put(worldName, Config.from(YamlConfiguration.loadConfiguration(file)));
    }

    private void discoverConfigs(final File dataFolder) {
        for (final File file : dataFolder.listFiles()) {
            if (file.isDirectory()) continue;
            loadConfig(file);
        }
    }
}
