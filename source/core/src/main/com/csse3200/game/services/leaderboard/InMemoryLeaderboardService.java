package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.List;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.services.ServiceLocator;

public class InMemoryLeaderboardService implements LeaderboardService {
    private final List<LeaderboardEntry> all = new ArrayList<>();
    private final String myId;

    public InMemoryLeaderboardService(String myId) {
        this.myId = myId;
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        SaveGameService saveGameService = ServiceLocator.getSaveGameService();
        if (saveGameService != null) {
            // Assuming SaveGameService has a load method that returns List<LeaderboardEntry>
            List<LeaderboardEntry> loadedEntries = saveGameService.loadLeaderboardEntries("leaderboard");
            if (loadedEntries != null) {
                all.addAll(loadedEntries);
                all.sort((e1, e2) -> Long.compare(e2.score, e1.score));
            }
        }
    }

    @Override
    public List<LeaderboardEntry> getEntries(LeaderboardQuery q) {
        int from = Math.max(0, q.offset);
        int to = Math.min(all.size(), from + q.limit);
        List<LeaderboardEntry> subList = all.subList(from, to);
        List<LeaderboardEntry> rankedList = new ArrayList<>();
        for (int i = 0; i < subList.size(); i++) {
            LeaderboardEntry entry = subList.get(i);
            rankedList.add(new LeaderboardEntry(from + i + 1, entry.playerId, entry.displayName, entry.score, entry.achievedAtMs));
        }
        return rankedList;
    }

    @Override
    public LeaderboardEntry getMyBest() {
        return all.stream()
                .filter(entry -> myId.equals(entry.playerId))
                .max((e1, e2) -> Long.compare(e1.score, e2.score))
                .orElse(null);
    }

    @Override
    public void submitScore(long score) {
        addEntry("Player", (int) score);
    }

    @Override
    public void addEntry(String playerName, int finalScore) {
        all.add(new LeaderboardEntry(0, playerName, playerName, finalScore, System.currentTimeMillis()));
        all.sort((e1, e2) -> Long.compare(e2.score, e1.score)); // Sort in descending order
    }
}
