package com.csse3200.game.wavesystem;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.utils.Difficulty;
import com.badlogic.gdx.math.GridPoint2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a wave of enemies with configurable composition.
 */
public class Wave {
    private static final Logger logger = LoggerFactory.getLogger(Wave.class);
    
    private final int waveNumber;
    private final int numDrones;
    private final int numGrunts;
    private final int numTanks;
    private final int numBosses;
    private final int numDividers;
    private final float spawnDelay; // Delay between enemy spawns in this wave
    
    /**
     * Creates a wave with specified enemy composition.
     */
    public Wave(int waveNumber, int numDrones, int numGrunts, int numTanks, 
                int numBosses, int numDividers, float spawnDelay) {
        this.waveNumber = waveNumber;
        this.numDrones = numDrones;
        this.numGrunts = numGrunts;
        this.numTanks = numTanks;
        this.numBosses = numBosses;
        this.numDividers = numDividers;
        this.spawnDelay = spawnDelay;
    }
    
    /**
     * Builds a spawn queue for this wave.
     * @param spawnCallbacks Contains spawn methods for each enemy type
     * @return List of spawn actions in order
     */
    public List<Runnable> buildSpawnQueue(WaveSpawnCallbacks spawnCallbacks) {
        List<Runnable> queue = new ArrayList<>();
        
        // Add drones
        for (int i = 0; i < numDrones; i++) {
            queue.add(spawnCallbacks.spawnDrone);
        }
        
        // Add grunts
        for (int i = 0; i < numGrunts; i++) {
            queue.add(spawnCallbacks.spawnGrunt);
        }
        
        // Add tanks
        for (int i = 0; i < numTanks; i++) {
            queue.add(spawnCallbacks.spawnTank);
        }
        
        // Add dividers
        for (int i = 0; i < numDividers; i++) {
            queue.add(spawnCallbacks.spawnDivider);
        }
        
        // Add bosses (usually last)
        for (int i = 0; i < numBosses; i++) {
            queue.add(spawnCallbacks.spawnBoss);
        }
        
        return queue;
    }
    
    /**
     * Gets the total number of enemies in this wave.
     */
    public int getTotalEnemies() {
        return numDrones + numGrunts + numTanks + numBosses + (numDividers * 4);
    }
    
    public int getWaveNumber() { 
        return waveNumber;
    }

    public float getSpawnDelay() { 
        return spawnDelay;
    }
    
    /**
     * Container for spawn method callbacks.
     */
    public static class WaveSpawnCallbacks {
        public final Runnable spawnDrone;
        public final Runnable spawnGrunt;
        public final Runnable spawnTank;
        public final Runnable spawnBoss;
        public final Runnable spawnDivider;
        
        public WaveSpawnCallbacks(Runnable spawnDrone, Runnable spawnGrunt, 
                                  Runnable spawnTank, Runnable spawnBoss, 
                                  Runnable spawnDivider) {
            this.spawnDrone = spawnDrone;
            this.spawnGrunt = spawnGrunt;
            this.spawnTank = spawnTank;
            this.spawnBoss = spawnBoss;
            this.spawnDivider = spawnDivider;
        }
    }
}