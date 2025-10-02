package com.csse3200.game.services.leaderboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.csse3200.game.services.leaderboard.LeaderboardService.*;

/**
 * 会话期间的排行榜服务
 * 在游戏运行期间保存数据到临时文件，程序关闭后自动清除
 */
public class SessionLeaderboardService implements LeaderboardService {
    private static final Logger logger = LoggerFactory.getLogger(SessionLeaderboardService.class);
    private static final String TEMP_LEADERBOARD_FILE = "temp/session_leaderboard.json";
    
    private final List<LeaderboardEntry> all = new ArrayList<>();
    private final String myId;
    private final Json json;
    private boolean isShuttingDown = false;
    
    public SessionLeaderboardService(String myId) {
        this.myId = myId;
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
        
        // 启动时加载临时数据（如果存在）
        loadSessionData();
        
        // 注册关闭钩子，程序关闭时清除临时文件
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
        
        logger.info("SessionLeaderboardService initialized for player: {}", myId);
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
        
        // 保存到临时文件
        saveSessionData();
        
        logger.info("Score {} submitted for player {} ({}). Total entries: {}", 
                   score, playerName, myId, all.size());
    }
    
    /**
     * 加载会话数据
     */
    private void loadSessionData() {
        try {
            FileHandle file = Gdx.files.local(TEMP_LEADERBOARD_FILE);
            if (file.exists()) {
                SessionData sessionData = json.fromJson(SessionData.class, file.readString());
                if (sessionData != null && sessionData.entries != null) {
                    all.clear();
                    all.addAll(sessionData.entries);
                    logger.info("Loaded {} leaderboard entries from session file", all.size());
                }
            } else {
                logger.debug("No existing session leaderboard file found");
            }
        } catch (Exception e) {
            logger.warn("Failed to load session leaderboard data: {}", e.getMessage());
            // 如果加载失败，继续使用空列表
        }
    }
    
    /**
     * 保存会话数据到临时文件
     */
    private void saveSessionData() {
        if (isShuttingDown) {
            return; // 如果正在关闭，不保存数据
        }
        
        try {
            // 确保临时目录存在
            FileHandle tempDir = Gdx.files.local("temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            SessionData sessionData = new SessionData();
            sessionData.entries = new ArrayList<>(all);
            sessionData.lastUpdated = System.currentTimeMillis();
            
            FileHandle file = Gdx.files.local(TEMP_LEADERBOARD_FILE);
            file.writeString(json.toJson(sessionData), false);
            
            logger.debug("Saved {} leaderboard entries to session file", all.size());
        } catch (Exception e) {
            logger.error("Failed to save session leaderboard data: {}", e.getMessage());
        }
    }
    
    /**
     * 清理临时文件
     */
    private void cleanup() {
        isShuttingDown = true;
        try {
            FileHandle file = Gdx.files.local(TEMP_LEADERBOARD_FILE);
            if (file.exists()) {
                file.delete();
                logger.info("Cleaned up session leaderboard file");
            }
            
            // 清理临时目录（如果为空）
            FileHandle tempDir = Gdx.files.local("temp");
            if (tempDir.exists() && tempDir.list().length == 0) {
                tempDir.delete();
                logger.debug("Cleaned up empty temp directory");
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup session leaderboard file: {}", e.getMessage());
        }
    }
    
    /**
     * 手动清理方法（可在游戏退出时调用）
     */
    public void dispose() {
        cleanup();
    }
    
    /**
     * 获取当前会话的所有记录数量
     */
    public int getTotalEntries() {
        return all.size();
    }
    
    /**
     * 清除当前会话的所有记录
     */
    public void clearSession() {
        all.clear();
        saveSessionData();
        logger.info("Cleared all session leaderboard entries");
    }
    
    /**
     * 会话数据存储类
     */
    public static class SessionData {
        public List<LeaderboardEntry> entries;
        public long lastUpdated;
        
        public SessionData() {
            this.entries = new ArrayList<>();
            this.lastUpdated = System.currentTimeMillis();
        }
    }
}


