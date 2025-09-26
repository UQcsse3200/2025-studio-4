package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryLeaderboardService implements LeaderboardService {
    private final List<LeaderboardService.LeaderboardEntry> all = new ArrayList<>();
    private final String myId;

    public InMemoryLeaderboardService(String myId) {
        this.myId = myId;
    }

    @Override
    public List<LeaderboardService.LeaderboardEntry> getEntries(LeaderboardService.LeaderboardQuery q) {
        // Sort entries by score in descending order
        sortEntriesByScore();
        
        int from = Math.max(0, q.offset);
        int to = Math.min(all.size(), from + q.limit);
        return all.subList(from, to);
    }

    @Override
    public LeaderboardService.LeaderboardEntry getMyBest() {
        // Find the best score for the current player
        LeaderboardService.LeaderboardEntry myBest = null;
        for (LeaderboardService.LeaderboardEntry entry : all) {
            if (entry.playerId.equals(myId)) {
                if (myBest == null || entry.score > myBest.score) {
                    myBest = entry;
                }
            }
        }
        
        if (myBest != null) {
            return myBest;
        } else {
            // Return default entry if no scores found
            return new LeaderboardService.LeaderboardEntry(0, myId, "You", 0, System.currentTimeMillis());
        }
    }

    @Override
    public void submitScore(long score) {
        // Submit score with default player name
        addEntry("Player", (int)score);
    }

    @Override
    public void addEntry(String playerName, int finalScore) {
        addEntry(playerName, finalScore, 1, 0, 0, 0);
    }

    @Override
    public void addEntry(String playerName, int finalScore, int level, int enemiesKilled, long gameDuration, int wavesSurvived) {
        // Validate input
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        playerName = playerName.trim();
        
        // Create new entry with extended data
        long currentTime = System.currentTimeMillis();
        LeaderboardService.LeaderboardEntry newEntry = new LeaderboardService.LeaderboardEntry(
            0, // rank will be calculated when sorting
            myId, // use current player ID
            playerName,
            finalScore,
            currentTime,
            level,
            enemiesKilled,
            gameDuration,
            wavesSurvived
        );
        
        // Add to list
        all.add(newEntry);
        
        // Sort and update ranks
        sortEntriesByScore();
        updateRanks();
    }
    
    /**
     * Sort entries by score in descending order
     */
    private void sortEntriesByScore() {
        all.sort(new Comparator<LeaderboardService.LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardService.LeaderboardEntry a, LeaderboardService.LeaderboardEntry b) {
                // Sort by score descending, then by time ascending (earlier is better)
                int scoreCompare = Long.compare(b.score, a.score);
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                return Long.compare(a.achievedAtMs, b.achievedAtMs);
            }
        });
    }
    
    /**
     * Update rank numbers after sorting
     */
    private void updateRanks() {
        for (int i = 0; i < all.size(); i++) {
            LeaderboardService.LeaderboardEntry oldEntry = all.get(i);
            LeaderboardService.LeaderboardEntry newEntry = new LeaderboardService.LeaderboardEntry(
                i + 1, // rank starts from 1
                oldEntry.playerId,
                oldEntry.displayName,
                oldEntry.score,
                oldEntry.achievedAtMs,
                oldEntry.level,
                oldEntry.enemiesKilled,
                oldEntry.gameDuration,
                oldEntry.wavesSurvived
            );
            all.set(i, newEntry);
        }
    }
    
    /**
     * Get all entries (for saving to file)
     */
    public List<LeaderboardService.LeaderboardEntry> getAllEntries() {
        sortEntriesByScore();
        updateRanks();
        return new ArrayList<>(all);
    }
    
    /**
     * Load entries from external source
     */
    public void loadEntries(List<LeaderboardService.LeaderboardEntry> entries) {
        all.clear();
        if (entries != null) {
            all.addAll(entries);
            sortEntriesByScore();
            updateRanks();
        }
    }
}
