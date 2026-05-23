package de.mcmdev.antifreecam.structures;

import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import io.papermc.paper.math.Position;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class StructureCache {

    private final Map<BoundingBox, Set<BoundingBox>> structures;

    public StructureCache() {
        this.structures = new ConcurrentHashMap<>();
    }

    public void add(final BoundingBox structureBoundingBox, final Set<BoundingBox> boundingBoxesOfPiecesInChunk) {
        structures.computeIfAbsent(structureBoundingBox, boundingBox -> new HashSet<>()).addAll(boundingBoxesOfPiecesInChunk);
    }

    public void checkUpdates(final Player player) {
        final Iterator<Map.Entry<BoundingBox, Set<BoundingBox>>> iterator = structures.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<BoundingBox, Set<BoundingBox>> entry = iterator.next();
            if (entry.getKey().contains(player.getEyeLocation().toVector())) {
                update(player, entry.getValue());
                iterator.remove();
            }
        }
    }

    private void update(final Player player, final Set<BoundingBox> boundingBoxes) {
        final Map<Position, BlockData> blockDataMap = new HashMap<>();
        for (final BoundingBox boundingBox : boundingBoxes) {
            for (final Vector vector : new BoundingBoxIterator(boundingBox)) {
                blockDataMap.put(Position.block(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()), 
                        vector.toLocation(player.getWorld()).getBlock().getBlockData());
            }
        }
        player.sendMultiBlockChange(blockDataMap);
    }

}
