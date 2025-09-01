package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.List;
import static com.csse3200.game.services.leaderboard.LeaderboardService.*;

public class InMemoryLeaderboardService implements LeaderboardService {
    private final List<LeaderboardEntry> all = new ArrayList<>();
    private final String myId;

    public InMemoryLeaderboardService(String myId) {
        this.myId = myId;
        // 造点假数据，方便先看 UI
        for (int i = 1; i <= 200; i++) {
            all.add(new LeaderboardEntry(i, "p"+i, "Player"+i,
                    10000 - i * 37, System.currentTimeMillis() - i * 60000L));
        }
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
        // TODO: 插入并重排；示例略
    }
}
