package de.mcmdev.antifreecam.structures;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class BoundingBoxIterator implements Iterator<Vector>, Iterable<Vector> {
    private final BoundingBox box;
    private int currentX, currentY, currentZ;

    public BoundingBoxIterator(final BoundingBox box) {
        this.box = box;
        this.currentX = (int) box.getMinX();
        this.currentY = (int) box.getMinY();
        this.currentZ = (int) box.getMinZ();
    }

    @Override
    public boolean hasNext() {
        return currentX <= box.getMaxX() && currentY <= box.getMaxY() && currentZ <= box.getMaxZ();
    }

    @Override
    public Vector next() {
        final Vector current = new Vector(currentX, currentY, currentZ);

        currentZ++;
        if (currentZ > box.getMaxZ()) {
            currentZ = (int) box.getMinZ();
            currentY++;
            if (currentY > box.getMaxY()) {
                currentY = (int) box.getMinY();
                currentX++;
            }
        }

        return current;
    }

    @Override
    public @NotNull Iterator<Vector> iterator() {
        return this;
    }
}