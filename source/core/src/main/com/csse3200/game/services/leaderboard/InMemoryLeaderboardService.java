package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLeaderboardService implements LeaderboardService {
    private final List<LeaderboardEntry> all = new ArrayList<>();
    private final String myId;

    public InMemoryLeaderboardService(String myId) {
        this.myId = myId;


    }

    @Override
    public List<LeaderboardEntry> getEntries(LeaderboardQuery q) {
        int from = Math.max(0, q.offset);
        int to = Math.min(all.size(), from + q.limit);
        return all.subList(from, to);
    }

    @Override
    public LeaderboardEntry getMyBest() {
        return new LeaderboardEntry(42, myId, "You", 8888, System.currentTimeMillis());
    }

    @Override
    public void submitScore(long score) {

    }

    @Override
    public void addEntry(String playerName, int finalScore2) {
        // Create a new leaderboard entry with the player name and score
        LeaderboardEntry entry = new LeaderboardEntry(
            all.size() + 1, // rank (simple implementation)
            myId, // player ID
            playerName, // display name
            finalScore2, // score
            System.currentTimeMillis() // timestamp
        );
        all.add(entry);
    }
}
