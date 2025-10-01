package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.List;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import static com.csse3200.game.services.leaderboard.LeaderboardService.*;

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
        String playerName = "Player";
        if (ServiceLocator.getPlayerNameService() != null) {
            playerName = ServiceLocator.getPlayerNameService().getPlayerName();
        }
        return new LeaderboardEntry(42, myId, playerName, 8888, System.currentTimeMillis());
    }

    @Override
    public void submitScore(long score) {

    }
}
