package de.mcmdev.antifreecam.structures;

import de.mcmdev.antifreecam.api.PlayerCacheHolder;
import de.mcmdev.antifreecam.config.Config;
import de.mcmdev.antifreecam.config.ConfigHolder;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.math.Position;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructurePiece;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class StructureHider implements Listener {

    private final ConfigHolder configHolder;
    private final PlayerCacheHolder<StructureCache> playerCacheHolder;

    public StructureHider(final ConfigHolder configHolder, final PlayerCacheHolder<StructureCache> playerCacheHolder) {
        this.configHolder = configHolder;
        this.playerCacheHolder = playerCacheHolder;
    }

    @EventHandler
    private void onPlayerChunkLoad(final PlayerChunkLoadEvent event) {
        final Optional<Config> configOptional = configHolder.getConfig(event.getWorld().getName());
        if (configOptional.isEmpty()) {
            return;
        }
        final Config config = configOptional.get();
        if (!config.isEnableStructureHiding()) {
            return;
        }

        final BoundingBox chunkBoundingBox = createChunkBoundingBox(event.getChunk());
        final Set<GeneratedStructure> structuresToHide = findStructuresToHide(event.getChunk(), config.getHiddenStructures());
        if (structuresToHide.isEmpty()) return;

        final StructureCache structureCache = playerCacheHolder.get(event.getPlayer());
        for (final GeneratedStructure structure : structuresToHide) {
            final Set<BoundingBox> boundingBoxesOfPiecesInChunk = extractStructurePieceBoundingBoxesInChunk(chunkBoundingBox, structure);
            structureCache.add(structure.getBoundingBox(), boundingBoxesOfPiecesInChunk);

            final Map<Position, BlockData> blockDataMap = createBlockDataMap(event.getWorld(), boundingBoxesOfPiecesInChunk);
            event.getPlayer().sendMultiBlockChange(blockDataMap);
        }
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        final Optional<Config> configOptional = configHolder.getConfig(event.getTo().getWorld().getName());
        if (configOptional.isEmpty()) {
            return;
        }
        final Config config = configOptional.get();
        if (!config.isEnableStructureHiding()) {
            return;
        }
        playerCacheHolder.get(event.getPlayer()).checkUpdates(event.getPlayer());
    }

    private Position toPosition(final Vector vector) {
        return Position.block(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    private @NotNull Set<BoundingBox> extractStructurePieceBoundingBoxesInChunk(final BoundingBox chunkBoundingBox, final GeneratedStructure generatedStructure) {
        return generatedStructure.getPieces().stream()
                .map(StructurePiece::getBoundingBox)
                .filter(boundingBox -> boundingBox.overlaps(chunkBoundingBox))
                .map(boundingBox -> boundingBox.intersection(chunkBoundingBox))
                .collect(Collectors.toSet());
    }

    private Set<GeneratedStructure> findStructuresToHide(final Chunk chunk, final Set<Structure> hiddenStructures) {
        return chunk.getStructures()
                .stream()
                .filter(generatedStructure -> hiddenStructures.contains(generatedStructure.getStructure()))
                .collect(Collectors.toSet());
    }

    private BoundingBox createChunkBoundingBox(final Chunk chunk) {
        final int worldMin = chunk.getWorld().getMinHeight();
        final int worldMax = chunk.getWorld().getMaxHeight();
        return new BoundingBox(chunk.getX() << 4, worldMin, chunk.getZ() << 4, (chunk.getX() << 4) + 15, worldMax, (chunk.getZ() << 4) + 15);
    }

    private Map<Position, BlockData> createBlockDataMap(final World world, final Collection<BoundingBox> boundingBoxes) {
        final Map<Position, BlockData> blockDataMap = new HashMap<>();
        for (final BoundingBox boundingBox : boundingBoxes) {
            final BoundingBoxIterator boundingBoxIterator = new BoundingBoxIterator(boundingBox);
            for (final Vector vector : boundingBoxIterator) {
                blockDataMap.put(toPosition(vector), getReplacementBlockData(world, vector));
            }
        }
        return blockDataMap;
    }

    private static final BlockData DEEPSLATE = Material.DEEPSLATE.createBlockData();
    private static final BlockData STONE = Material.STONE.createBlockData();
    private static final BlockData NETHERRACK = Material.NETHERRACK.createBlockData();
    private static final BlockData AIR = Material.AIR.createBlockData();

    private BlockData getReplacementBlockData(final World world, final Vector vector) {
        return switch (world.getEnvironment()) {
            case NORMAL -> vector.getBlockY() < 0 ? DEEPSLATE : STONE;
            case NETHER -> NETHERRACK;
            case THE_END -> AIR;
            default -> AIR;
        };
    }

}
