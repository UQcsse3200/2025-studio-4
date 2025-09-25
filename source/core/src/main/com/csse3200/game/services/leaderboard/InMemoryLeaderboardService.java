package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static com.csse3200.game.services.leaderboard.LeaderboardService.*;

public class InMemoryLeaderboardService implements LeaderboardService {
    private final List<LeaderboardEntry> all = new ArrayList<>();
    private final String myId;

    public InMemoryLeaderboardService(String myId) {
        this.myId = myId;
    }

    @Override
    public List<LeaderboardEntry> getEntries(LeaderboardQuery q) {
        // Sort entries by score in descending order
        sortEntriesByScore();
        
        int from = Math.max(0, q.offset);
        int to = Math.min(all.size(), from + q.limit);
        return all.subList(from, to);
    }

    @Override
    public LeaderboardEntry getMyBest() {
        // Find the best score for the current player
        LeaderboardEntry myBest = null;
        for (LeaderboardEntry entry : all) {
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
            return new LeaderboardEntry(0, myId, "You", 0, System.currentTimeMillis());
        }
    }

    @Override
    public void submitScore(long score) {
        // Submit score with default player name
        addEntry("Player", (int)score);
    }

    @Override
    public void addEntry(String playerName, int finalScore) {
        // Validate input
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        playerName = playerName.trim();
        
        // Create new entry
        long currentTime = System.currentTimeMillis();
        LeaderboardEntry newEntry = new LeaderboardEntry(
            0, // rank will be calculated when sorting
            myId, // use current player ID
            playerName,
            finalScore,
            currentTime
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
        all.sort(new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
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
            LeaderboardEntry oldEntry = all.get(i);
            LeaderboardEntry newEntry = new LeaderboardEntry(
                i + 1, // rank starts from 1
                oldEntry.playerId,
                oldEntry.displayName,
                oldEntry.score,
                oldEntry.achievedAtMs
            );
            all.set(i, newEntry);
        }
    }
    
    /**
     * Get all entries (for saving to file)
     */
    public List<LeaderboardEntry> getAllEntries() {
        sortEntriesByScore();
        updateRanks();
        return new ArrayList<>(all);
    }
    
    /**
     * Load entries from external source
     */
    public void loadEntries(List<LeaderboardEntry> entries) {
        all.clear();
        if (entries != null) {
            all.addAll(entries);
            sortEntriesByScore();
            updateRanks();
        }
    }
}
