package com.csse3200.game.services.leaderboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A leaderboard service that persists data to a JSON file.
 * Extends InMemoryLeaderboardService to add file persistence.
 */
public class PersistentLeaderboardService extends InMemoryLeaderboardService {
    private static final Logger logger = LoggerFactory.getLogger(PersistentLeaderboardService.class);
    private static final String LEADERBOARD_FILE = "saves/leaderboard.json";
    private final Json json;

    public PersistentLeaderboardService(String myId) {
        super(myId);
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
        loadFromFile();
    }

    @Override
    public void addEntry(String playerName, int finalScore) {
        // Add entry to memory
        super.addEntry(playerName, finalScore);
        
        // Save to file
        saveToFile();
        
        logger.info("Added leaderboard entry: {} with score {}", playerName, finalScore);
    }

    @Override
    public void addEntry(String playerName, int finalScore, int level, int enemiesKilled, long gameDuration, int wavesSurvived) {
        // Add entry to memory with extended data
        super.addEntry(playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);
        
        // Save to file
        saveToFile();
        
        logger.info("Added leaderboard entry: {} with score {}, level {}, kills {}, duration {}ms, waves {}", 
                   playerName, finalScore, level, enemiesKilled, gameDuration, wavesSurvived);
    }

    /**
     * Save leaderboard entries to file
     */
    private void saveToFile() {
        try {
            List<LeaderboardEntry> entries = getAllEntries();
            LeaderboardData data = new LeaderboardData();
            data.entries = entries;
            data.version = 1;
            data.lastUpdated = System.currentTimeMillis();
            
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            String jsonString = json.toJson(data);
            file.writeString(jsonString, false);
            
            logger.debug("Leaderboard saved to file: {} entries", entries.size());
        } catch (Exception e) {
            logger.error("Failed to save leaderboard to file: {}", e.getMessage(), e);
        }
    }

    /**
     * Load leaderboard entries from file
     */
    private void loadFromFile() {
        try {
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            if (file.exists()) {
                String jsonString = file.readString();
                LeaderboardData data = json.fromJson(LeaderboardData.class, jsonString);
                
                if (data != null && data.entries != null) {
                    loadEntries(data.entries);
                    logger.info("Loaded {} leaderboard entries from file", data.entries.size());
                } else {
                    logger.warn("Leaderboard file exists but contains no valid data");
                }
            } else {
                logger.info("No existing leaderboard file found, starting with empty leaderboard");
            }
        } catch (Exception e) {
            logger.error("Failed to load leaderboard from file: {}", e.getMessage(), e);
            logger.info("Starting with empty leaderboard");
        }
    }

    /**
     * Clear all entries and delete the file
     */
    public void clearLeaderboard() {
        loadEntries(new ArrayList<>());
        
        try {
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            if (file.exists()) {
                file.delete();
            }
            logger.info("Leaderboard cleared");
        } catch (Exception e) {
            logger.error("Failed to delete leaderboard file: {}", e.getMessage(), e);
        }
    }

    /**
     * Get the top N entries
     */
    public List<LeaderboardEntry> getTopEntries(int limit) {
        LeaderboardQuery query = new LeaderboardQuery(0, limit, false);
        return getEntries(query);
    }

    /**
     * Data structure for JSON serialization
     */
    public static class LeaderboardData {
        public List<LeaderboardEntry> entries;
        public int version;
        public long lastUpdated;
    }

    /**
     * Serializable version of LeaderboardEntry for JSON
     */
    public static class SerializableLeaderboardEntry {
        public int rank;
        public String playerId;
        public String displayName;
        public long score;
        public long achievedAtMs;
        public int level;
        public int enemiesKilled;
        public long gameDuration;
        public int wavesSurvived;

        public SerializableLeaderboardEntry() {
            // Default constructor for JSON deserialization
        }

        public SerializableLeaderboardEntry(LeaderboardEntry entry) {
            this.rank = entry.rank;
            this.playerId = entry.playerId;
            this.displayName = entry.displayName;
            this.score = entry.score;
            this.achievedAtMs = entry.achievedAtMs;
            this.level = entry.level;
            this.enemiesKilled = entry.enemiesKilled;
            this.gameDuration = entry.gameDuration;
            this.wavesSurvived = entry.wavesSurvived;
        }

        public LeaderboardEntry toLeaderboardEntry() {
            return new LeaderboardEntry(rank, playerId, displayName, score, achievedAtMs, 
                                       level, enemiesKilled, gameDuration, wavesSurvived);
        }
    }
}
