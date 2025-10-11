package com.csse3200.game.wavesystem;

import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a wave of enemies with configurable composition and multiple spawn points.
 */
public class Wave {
    private static final Logger logger = LoggerFactory.getLogger(Wave.class);
    
    private final int waveNumber;
    private final int numDrones;
    private final int numGrunts;
    private final int numTanks;
    private final int numBosses;
    private final int numDividers;
    private final int numSpeeders;
    private final float spawnDelay; // Delay between enemy spawns in this wave
    private final List<List<Entity>> waypointLists; // List of waypoint entity paths for different spawn points
    
    /**
     * Creates a wave with specified enemy composition and multiple spawn points.
     * @param waveNumber The wave number
     * @param numDrones Number of drone enemies
     * @param numGrunts Number of grunt enemies
     * @param numTanks Number of tank enemies
     * @param numBosses Number of boss enemies
     * @param numDividers Number of divider enemies
     * @param numSpeeders Number of speeder enemies
     * @param spawnDelay Delay between spawns
     * @param waypointLists List of waypoint entity paths - enemies will cycle through these spawn points
     */
    public Wave(int waveNumber, int numDrones, int numGrunts, int numTanks, 
                int numBosses, int numDividers, int numSpeeders, float spawnDelay,
                List<List<Entity>> waypointLists) {
        this.waveNumber = waveNumber;
        this.numDrones = numDrones;
        this.numGrunts = numGrunts;
        this.numTanks = numTanks;
        this.numBosses = numBosses;
        this.numDividers = numDividers;
        this.numSpeeders = numSpeeders;
        this.spawnDelay = spawnDelay;
        this.waypointLists = waypointLists;
        
        if (waypointLists == null || waypointLists.isEmpty()) {
            logger.error("Wave created with no waypoint lists!");
        }
    }
    
    /**
     * Builds a spawn queue for this wave.
     * Enemies are distributed across spawn points in round-robin fashion.
     * @param spawnCallbacks Contains spawn methods for each enemy type
     * @return List of spawn actions in order
     */
    public List<Runnable> buildSpawnQueue(WaveSpawnCallbacks spawnCallbacks) {
        List<Runnable> queue = new ArrayList<>();
        int waypointIndex = 0;
        
        // Add drones - cycle through waypoint lists
        for (int i = 0; i < numDrones; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnDrone.accept(waypoints));
            waypointIndex++;
        }
        
        // Add grunts - continue cycling
        for (int i = 0; i < numGrunts; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnGrunt.accept(waypoints));
            waypointIndex++;
        }
        
        // Add tanks - continue cycling
        for (int i = 0; i < numTanks; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnTank.accept(waypoints));
            waypointIndex++;
        }
        
        // Add dividers - continue cycling
        for (int i = 0; i < numDividers; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnDivider.accept(waypoints));
            waypointIndex++;
        }

        // Add speeders - continue cycling
        for (int i = 0; i < numSpeeders; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnSpeeder.accept(waypoints));
            waypointIndex++;
        }

        // Add bosses (usually last) - continue cycling
        for (int i = 0; i < numBosses; i++) {
            final List<Entity> waypoints = waypointLists.get(waypointIndex % waypointLists.size());
            queue.add(() -> spawnCallbacks.spawnBoss.accept(waypoints));
            waypointIndex++;
        }
        
        return queue;
    }
    
    /**
     * Gets the total number of enemies in this wave.
     */
    public int getTotalEnemies() {
        return numDrones + numGrunts + numTanks + numBosses + (numDividers * 4) + numSpeeders;
    }
    
    public int getWaveNumber() { 
        return waveNumber;
    }

    public float getSpawnDelay() { 
        return spawnDelay;
    }
    
    public List<List<Entity>> getWaypointLists() {
        return waypointLists;
    }
    
    /**
     * Container for spawn method callbacks.
     * Each callback now accepts a waypoint entity list to determine spawn location and path.
     */
    public static class WaveSpawnCallbacks {
        public final Consumer<List<Entity>> spawnDrone;
        public final Consumer<List<Entity>> spawnGrunt;
        public final Consumer<List<Entity>> spawnTank;
        public final Consumer<List<Entity>> spawnBoss;
        public final Consumer<List<Entity>> spawnDivider;
        public final Consumer<List<Entity>> spawnSpeeder;

        public WaveSpawnCallbacks(Consumer<List<Entity>> spawnDrone, 
                                  Consumer<List<Entity>> spawnGrunt, 
                                  Consumer<List<Entity>> spawnTank, 
                                  Consumer<List<Entity>> spawnBoss, 
                                  Consumer<List<Entity>> spawnDivider,
                                  Consumer<List<Entity>> spawnSpeeder) {
            this.spawnDrone = spawnDrone;
            this.spawnGrunt = spawnGrunt;
            this.spawnTank = spawnTank;
            this.spawnBoss = spawnBoss;
            this.spawnDivider = spawnDivider;
            this.spawnSpeeder = spawnSpeeder;
        }
    }
}