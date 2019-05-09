package net.johnbrooks.ports.ports;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

class PortShelf {

    private final HashMap<Chunk, List<Port>> portCollection;

    protected PortShelf() {
        this.portCollection = new HashMap<>();
    }

    /**
     * Inserts a port into the appropriate collection. If the port extends over multiple chunks,
     * the port will be inserted into each collection.
     * @param port The port that is being inserted into the collections.
     * @return Whether or not the port was inserted into at least 1 collection.
     */
    protected boolean insert(final Port port) {
        final AtomicBoolean insertedSuccessfully = new AtomicBoolean();
        calculateChunksInPortCoverage(port).forEach(chunk -> {
            if (insert(chunk, port) && !insertedSuccessfully.get()) {
                insertedSuccessfully.set(true);
            }
        });

        return insertedSuccessfully.get();
    }

    protected void remove(final Port port) {
        calculateChunksInPortCoverage(port).forEach(chunk -> remove(chunk, port));
    }

    /**
     * Retrieves a list of chunks in the parameter of the port's reach.
     * @param port
     * @return
     */
    protected List<Chunk> calculateChunksInPortCoverage(final Port port) {
        final Chunk chunkPointA = port.getPointA().getChunk();
        final Chunk chunkPointB = port.getPointB().getChunk();
        final int minX = Math.min(chunkPointA.getX(), chunkPointB.getX());
        final int minZ = Math.min(chunkPointA.getZ(), chunkPointB.getZ());
        final int distX = Math.abs(chunkPointA.getX() - chunkPointB.getX());
        final int distZ = Math.abs(chunkPointA.getZ() - chunkPointB.getZ());
        final int totalChunks = (distX + 1) * (distZ + 1);
        final List<Chunk> chunkList = new ArrayList<>(totalChunks);

        for (int x = minX; x < minX + distX; x++) {
            for (int z = minZ; z < minZ + distZ; z++) {
                chunkList.add(chunkPointA.getWorld().getChunkAt(x, z));
            }
        }

        return chunkList;
    }

    /**
     * Retrieve a port that covers the provided location.
     * @param location The target location.
     * @return an Optional of the searched for Port.
     */
    public Optional<Port> getPort(final Location location) {
        final List<Port> portList = getPorts(location.getChunk());
        final Vector locationVector = location.toVector();
        return portList.stream().filter(port -> locationVector
                .isInAABB(port.getPointA().toVector(), port.getPointB().toVector()))
                .findFirst();
    }

    private List<Port> getPorts(final Chunk chunk) {
        return portCollection.containsKey(chunk) ? portCollection.get(chunk) : new ArrayList<>();
    }

    private boolean insert(final Chunk chunk, final Port port) {
        List<Port> portList = getPorts(chunk);
        if (!portList.contains(port)) {
            portList.add(port);
            portCollection.put(chunk, portList);
            return true;
        } else {
            return false;
        }
    }

    private void remove(final Chunk chunk, final Port port) {
        List<Port> portList = getPorts(chunk);
        portList.remove(port);
        portCollection.put(chunk, portList);
    }

}