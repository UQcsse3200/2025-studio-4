package com.csse3200.game.services.leaderboard;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntry> getEntries(LeaderboardQuery query);
    LeaderboardEntry getMyBest();
    void submitScore(long score);

    // --- 数据模型 ---
    final class LeaderboardEntry {
        public final int rank;
        public final String playerId;
        public final String displayName;
        public final long score;
        public final long achievedAtMs;

        public LeaderboardEntry(int rank, String playerId, String displayName, long score, long achievedAtMs) {
            this.rank = rank;
            this.playerId = playerId;
            this.displayName = displayName;
            this.score = score;
            this.achievedAtMs = achievedAtMs;
        }
    }

    final class LeaderboardQuery {
        public final int offset;
        public final int limit;
        public final boolean friendsOnly;

        public LeaderboardQuery(int offset, int limit, boolean friendsOnly) {
            this.offset = offset;
            this.limit = limit;
            this.friendsOnly = friendsOnly;
        }
    }
}
