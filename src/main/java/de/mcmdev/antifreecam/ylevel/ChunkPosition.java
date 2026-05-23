package de.mcmdev.antifreecam.ylevel;

import org.bukkit.Location;

public record ChunkPosition(int x, int z) {

    public static ChunkPosition fromLocation(final Location location) {
        return new ChunkPosition(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

}