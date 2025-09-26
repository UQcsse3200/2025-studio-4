package com.csse3200.game.services.leaderboard;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntry> getEntries(LeaderboardQuery query);
    LeaderboardEntry getMyBest();
    void submitScore(long score);

    void addEntry(String playerName, int finalScore2);
    
    void addEntry(String playerName, int finalScore, int level, int enemiesKilled, long gameDuration, int wavesSurvived);

    // --- 数据模型 ---
    final class LeaderboardEntry {
        public final int rank;
        public final String playerId;
        public final String displayName;
        public final long score;
        public final long achievedAtMs;
        public final int level;
        public final int enemiesKilled;
        public final long gameDuration;
        public final int wavesSurvived;

        public LeaderboardEntry(int rank, String playerId, String displayName, long score, long achievedAtMs) {
            this(rank, playerId, displayName, score, achievedAtMs, 1, 0, 0, 0);
        }

        public LeaderboardEntry(int rank, String playerId, String displayName, long score, long achievedAtMs, 
                               int level, int enemiesKilled, long gameDuration, int wavesSurvived) {
            this.rank = rank;
            this.playerId = playerId;
            this.displayName = displayName;
            this.score = score;
            this.achievedAtMs = achievedAtMs;
            this.level = level;
            this.enemiesKilled = enemiesKilled;
            this.gameDuration = gameDuration;
            this.wavesSurvived = wavesSurvived;
        }

        /**
         * Gets formatted game duration as a string (MM:SS)
         */
        public String getFormattedGameDuration() {
            long seconds = gameDuration / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
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
