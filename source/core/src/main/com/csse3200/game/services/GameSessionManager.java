package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏会话管理器，确保每次游戏只记录一次得分
 */
public class GameSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionManager.class);
    
    private boolean scoreSubmitted = false;
    private long sessionStartTime;
    
    public GameSessionManager() {
        startNewSession();
    }
    
    /**
     * 开始新的游戏会话
     */
    public void startNewSession() {
        scoreSubmitted = false;
        sessionStartTime = System.currentTimeMillis();
        logger.info("Started new game session at {}", sessionStartTime);
    }
    
    /**
     * 提交得分（如果本次会话还未提交）
     * 使用GameScoreService获取真实的游戏得分
     * @param isVictory 是否胜利
     * @return true 如果成功提交，false 如果已经提交过
     */
    public boolean submitScoreIfNotSubmitted(boolean isVictory) {
        if (scoreSubmitted) {
            logger.warn("Score already submitted for this session, ignoring duplicate submission");
            return false;
        }
        
        try {
            // 从GameScoreService获取真实的游戏得分
            var scoreService = ServiceLocator.getGameScoreService();
            long finalScore = 0;
            
            if (scoreService != null) {
                // 在计算最终得分之前更新PlayerRankingComponent的统计信息
                updatePlayerRankingStats(isVictory);
                
                finalScore = scoreService.calculateFinalScore(isVictory);
                scoreService.endGame(); // 结束游戏得分跟踪
            } else {
                // 如果得分服务不可用，使用默认值
                finalScore = isVictory ? 1000 : 100;
                logger.warn("GameScoreService not available, using default score: {}", finalScore);
            }
            
            ServiceLocator.getLeaderboardService().submitScore(finalScore);
            scoreSubmitted = true;
            logger.info("Successfully submitted real game score {} for current session (victory: {})", 
                       finalScore, isVictory);
            return true;
        } catch (Exception e) {
            logger.error("Failed to submit score", e);
            return false;
        }
    }
    
    /**
     * 检查本次会话是否已经提交过得分
     */
    public boolean isScoreSubmitted() {
        return scoreSubmitted;
    }
    
    /**
     * 获取会话开始时间
     */
    public long getSessionStartTime() {
        return sessionStartTime;
    }
    
    /**
     * 更新PlayerRankingComponent的统计信息
     */
    private void updatePlayerRankingStats(boolean isVictory) {
        try {
            var entityService = ServiceLocator.getEntityService();
            if (entityService != null) {
                var entities = entityService.getEntities();
                for (var entity : entities) {
                    var rankingComponent = entity.getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
                    if (rankingComponent != null) {
                        // 更新剩余生命值
                        var combatStats = entity.getComponent(com.csse3200.game.components.PlayerCombatStatsComponent.class);
                        if (combatStats != null) {
                            rankingComponent.setRemainingHealth(combatStats.getHealth());
                        }
                        
                        // 停止计时器
                        rankingComponent.stopTimer();
                        
                        // TODO: 可以在这里添加更多统计信息的更新，比如剩余塔数量等
                        
                        logger.debug("Updated player ranking stats: enemies={}, health={}, time={}s", 
                                   rankingComponent.getEnemiesKilled(), 
                                   rankingComponent.getRemainingHealth(),
                                   rankingComponent.getTimeTakenInSeconds());
                        break; // 只需要一个排名组件
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error updating player ranking stats", e);
        }
    }
}
