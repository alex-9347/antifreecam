package de.mcmdev.antifreecam.ylevel;

import de.mcmdev.antifreecam.config.Config;
import de.mcmdev.antifreecam.config.ConfigHolder;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class NativeYLevelHider implements Listener {

    private final ConfigHolder configHolder;
    private final Plugin plugin;
    private final Map<UUID, Set<ChunkPosition>> hiddenChunks = new HashMap<>();
    private static final BlockData AIR = Material.AIR.createBlockData();

    public NativeYLevelHider(final ConfigHolder configHolder, final Plugin plugin) {
        this.configHolder = configHolder;
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(final PlayerChunkLoadEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final Optional<Config> configOptional = configHolder.getConfig(world.getName());
        
        if (configOptional.isEmpty()) return;
        final Config config = configOptional.get();
        if (!config.isEnableYLevelCutoff()) return;
        
        if (player.getLocation().getBlockY() < config.getRevealHeight()) return;
        
        hideChunk(player, event.getChunk(), world.getMinHeight(), config.getCutoffHeight());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final Optional<Config> configOptional = configHolder.getConfig(world.getName());
        
        if (configOptional.isEmpty()) return;
        final Config config = configOptional.get();
        if (!config.isEnableYLevelCutoff()) return;
        
        boolean wasAbove = event.getFrom().getBlockY() >= config.getRevealHeight();
        boolean isAbove = event.getTo().getBlockY() >= config.getRevealHeight();
        
        if (wasAbove && !isAbove) {
            // Player went below reveal height, reveal everything
            revealAllChunks(player, config.getCutoffHeight());
        } else if (!wasAbove && isAbove) {
            // Player went above reveal height, hide all loaded chunks
            hideAllChunks(player, config.getCutoffHeight());
        }
    }

    private void hideChunk(Player player, Chunk chunk, int minHeight, int cutoffHeight) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        
        hiddenChunks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(new ChunkPosition(chunkX, chunkZ));
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Position, BlockData> changes = new HashMap<>(16384);
            int cx = chunkX << 4;
            int cz = chunkZ << 4;
            
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minHeight; y < cutoffHeight; y++) {
                        changes.put(Position.block(cx + x, y, cz + z), AIR);
                    }
                }
            }
            
            if (player.isOnline()) {
                player.sendMultiBlockChange(changes);
            }
        });
    }

    private void revealChunkAsync(Player player, ChunkSnapshot snapshot, int chunkX, int chunkZ, int minHeight, int cutoffHeight) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Position, BlockData> changes = new HashMap<>(16384);
            int cx = chunkX << 4;
            int cz = chunkZ << 4;
            
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minHeight; y < cutoffHeight; y++) {
                        changes.put(Position.block(cx + x, y, cz + z), snapshot.getBlockData(x, y, z));
                    }
                }
            }
            
            if (player.isOnline()) {
                player.sendMultiBlockChange(changes);
            }
        });
    }

    private void revealAllChunks(Player player, int cutoffHeight) {
        Set<ChunkPosition> chunks = hiddenChunks.remove(player.getUniqueId());
        if (chunks == null) return;
        
        World world = player.getWorld();
        int minHeight = world.getMinHeight();
        
        for (ChunkPosition pos : chunks) {
            if (world.isChunkLoaded(pos.x(), pos.z())) {
                Chunk chunk = world.getChunkAt(pos.x(), pos.z());
                ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
                revealChunkAsync(player, snapshot, pos.x(), pos.z(), minHeight, cutoffHeight);
            }
        }
    }

    private void hideAllChunks(Player player, int cutoffHeight) {
        World world = player.getWorld();
        int minHeight = world.getMinHeight();
        Chunk[] loadedChunks = world.getLoadedChunks();
        
        // Only hide chunks within view distance
        int viewDistance = player.getClientViewDistance();
        int px = player.getLocation().getBlockX() >> 4;
        int pz = player.getLocation().getBlockZ() >> 4;
        
        for (Chunk chunk : loadedChunks) {
            if (Math.abs(chunk.getX() - px) <= viewDistance && Math.abs(chunk.getZ() - pz) <= viewDistance) {
                hideChunk(player, chunk, minHeight, cutoffHeight);
            }
        }
    }
}
