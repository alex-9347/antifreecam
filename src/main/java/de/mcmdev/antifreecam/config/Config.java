package de.mcmdev.antifreecam.config;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.structure.Structure;

import java.util.Set;
import java.util.stream.Collectors;

public final class Config {

    private final boolean enableYLevelCutoff;
    private final boolean enableStructureHiding;
    private final int revealHeight;
    private final int cutoffHeight;
    private final Set<Structure> hiddenStructures;

    private Config(final boolean enableYLevelCutoff, final boolean enableStructureHiding, final int revealHeight, final int cutoffHeight, final Set<Structure> hiddenStructures) {
        this.enableYLevelCutoff = enableYLevelCutoff;
        this.enableStructureHiding = enableStructureHiding;
        this.revealHeight = revealHeight;
        this.cutoffHeight = cutoffHeight;
        this.hiddenStructures = hiddenStructures;
    }

    public static Config from(final ConfigurationSection configurationSection) {
        final boolean enableYLevelCutoff = configurationSection.getBoolean("enable-y-level-cutoff", true);
        final boolean enableStructureHiding = configurationSection.getBoolean("enable-structure-hiding", true);
        final int revealHeight = configurationSection.getInt("reveal-height", 16);
        final int cutoffHeight = configurationSection.getInt("cutoff-height", 0);
        final Set<Structure> hiddenStructures = configurationSection.getStringList("hidden-structures")
                .stream()
                .map(name -> RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.STRUCTURE)
                        .getOrThrow(NamespacedKey.minecraft(name)))
                .collect(Collectors.toSet());
        return new Config(enableYLevelCutoff, enableStructureHiding, revealHeight, cutoffHeight, hiddenStructures);
    }

    public boolean isEnableYLevelCutoff() {
        return enableYLevelCutoff;
    }

    public boolean isEnableStructureHiding() {
        return enableStructureHiding;
    }

    public int getRevealHeight() {
        return revealHeight;
    }

    public int getCutoffHeight() {
        return cutoffHeight;
    }

    public Set<Structure> getHiddenStructures() {
        return hiddenStructures;
    }
}
