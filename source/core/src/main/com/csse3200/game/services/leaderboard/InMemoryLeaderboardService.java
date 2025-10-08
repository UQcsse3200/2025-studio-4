package com.csse3200.game.services.leaderboard;

import java.util.ArrayList;
import java.util.List;
import com.csse3200.game.services.ServiceLocator;
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
        
        // 查找玩家的最佳成绩
        LeaderboardEntry myBest = null;
        for (LeaderboardEntry entry : all) {
            if (entry.playerId.equals(myId)) {
                if (myBest == null || entry.score > myBest.score) {
                    myBest = entry;
                }
            }
        }
        
        // 如果没有找到玩家的成绩，返回默认值
        if (myBest == null) {
            return new LeaderboardEntry(all.size() + 1, myId, playerName, 0, System.currentTimeMillis());
        }
        
        return myBest;
    }

    @Override
    public void submitScore(long score) {
        String playerName = "Player";
        if (ServiceLocator.getPlayerNameService() != null) {
            playerName = ServiceLocator.getPlayerNameService().getPlayerName();
        }
        
        // 创建新的排行榜条目
        LeaderboardEntry newEntry = new LeaderboardEntry(
            all.size() + 1, // 临时排名，稍后会重新计算
            myId,
            playerName,
            score,
            System.currentTimeMillis()
        );
        
        // 添加到列表中
        all.add(newEntry);
        
        // 按得分降序排序并重新分配排名
        all.sort((a, b) -> Long.compare(b.score, a.score));
        
        // 重新分配排名
        for (int i = 0; i < all.size(); i++) {
            LeaderboardEntry oldEntry = all.get(i);
            all.set(i, new LeaderboardEntry(
                i + 1, // 新排名
                oldEntry.playerId,
                oldEntry.displayName,
                oldEntry.score,
                oldEntry.achievedAtMs
            ));
        }
    }
}
