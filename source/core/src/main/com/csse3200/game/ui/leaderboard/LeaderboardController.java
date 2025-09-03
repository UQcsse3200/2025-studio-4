package com.csse3200.game.ui.leaderboard;

import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardEntry;
import com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardQuery;
import java.util.List;

public class LeaderboardController {
    private final LeaderboardService service;
    private final int pageSize = 20;
    private int offset = 0;
    private boolean friendsOnly = false;

    public LeaderboardController(LeaderboardService service) { this.service = service; }

    public List<LeaderboardEntry> loadPage() {
        return service.getEntries(new LeaderboardQuery(offset, pageSize, friendsOnly));
    }

    public LeaderboardEntry getMyBest() { return service.getMyBest(); }

    public void nextPage() { offset += pageSize; }
    public void prevPage() { offset = Math.max(0, offset - pageSize); }
    public boolean isFirstPage() { return offset == 0; }
    public void toggleFriends() { friendsOnly = !friendsOnly; offset = 0; }
    public boolean isFriendsOnly() { return friendsOnly; }
}
