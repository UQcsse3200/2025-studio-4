package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.PlayerRankingComponent;

/**
 * 游戏得分服务，负责跟踪和管理当前游戏的得分
 */
public class GameScoreService {
    private static final Logger logger = LoggerFactory.getLogger(GameScoreService.class);
    
    private long currentGameScore = 0;
    private boolean gameInProgress = false;
    
    /**
     * 开始新游戏，重置得分
     */
    public void startNewGame() {
        currentGameScore = 0;
        gameInProgress = true;
        logger.info("Started new game, score reset to 0");
    }
    
    /**
     * 结束当前游戏
     */
    public void endGame() {
        gameInProgress = false;
        logger.info("Game ended with final score: {}", currentGameScore);
    }
    
    /**
     * 获取当前游戏的实时得分
     * 这个方法会尝试从多个来源获取最准确的得分
     */
    public long getCurrentGameScore() {
        if (!gameInProgress) {
            logger.debug("Game not in progress, returning cached score: {}", currentGameScore);
            return currentGameScore; // 游戏结束后返回最终得分
        }
        
        long score = 0;
        int entitiesChecked = 0;
        int scoreComponentsFound = 0;
        
        try {
            // 尝试从EntityService获取玩家得分组件
            var entityService = ServiceLocator.getEntityService();
            if (entityService != null) {
                var entities = entityService.getEntities();
                logger.debug("Checking entities for PlayerScoreComponent");
                
                for (var entity : entities) {
                    entitiesChecked++;
                    // 优先使用PlayerScoreComponent的实时得分
                    var scoreComponent = entity.getComponent(PlayerScoreComponent.class);
                    if (scoreComponent != null) {
                        scoreComponentsFound++;
                        int entityScore = scoreComponent.getTotalScore();
                        logger.debug("Found PlayerScoreComponent with score: {}", entityScore);
                        score = Math.max(score, entityScore);
                    }
                }
            } else {
                logger.warn("EntityService is null, cannot get player score");
            }
            
            // 只有在找到有效得分时才更新缓存（避免用0覆盖真实得分）
            if (score > 0 || scoreComponentsFound > 0) {
                currentGameScore = score;
                logger.debug("Updated current game score to: {}", currentGameScore);
            } else if (currentGameScore > 0) {
                // 如果没找到得分组件但缓存中有得分，使用缓存的得分
                score = currentGameScore;
                logger.debug("Using cached score: {}", currentGameScore);
            }
            
        } catch (Exception e) {
            logger.error("Error getting current game score", e);
        }
        
        logger.debug("Score check complete: entities={}, scoreComponents={}, finalScore={}", 
                    entitiesChecked, scoreComponentsFound, currentGameScore);
        return currentGameScore;
    }
    
    /**
     * 计算最终得分（游戏结束时调用）
     * 结合PlayerScoreComponent和PlayerRankingComponent的数据
     */
    public long calculateFinalScore(boolean isVictory) {
        long baseScore = getCurrentGameScore(); // 获取实时得分作为基础
        
        try {
            var entityService = ServiceLocator.getEntityService();
            if (entityService != null) {
                var entities = entityService.getEntities();
                for (var entity : entities) {
                    var rankingComponent = entity.getComponent(PlayerRankingComponent.class);
                    if (rankingComponent != null) {
                        // 基于游戏表现计算额外奖励得分
                        long bonusScore = calculateBonusScore(rankingComponent, isVictory);
                        baseScore += bonusScore;
                        break; // 只需要一个排名组件
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error calculating final score", e);
        }
        
        // 记录最终得分（包括0分，这是真实的游戏得分）
        logger.info("Using real game score: {} (victory: {})", baseScore, isVictory);
        
        currentGameScore = baseScore;
        logger.info("Final score calculated: {} (victory: {})", baseScore, isVictory);
        return baseScore;
    }
    
    /**
     * 根据游戏表现计算奖励得分
     */
    private long calculateBonusScore(PlayerRankingComponent ranking, boolean isVictory) {
        long bonusScore = 0;
        
        // 根据击杀敌人数量加分
        bonusScore += ranking.getEnemiesKilled() * (isVictory ? 10 : 5);
        
        // 根据剩余生命值加分
        bonusScore += ranking.getRemainingHealth() * (isVictory ? 5 : 2);
        
        // 根据剩余塔数量加分
        bonusScore += ranking.getTowersRemaining() * (isVictory ? 20 : 10);
        
        if (isVictory) {
            // 胜利时的时间奖励（越快完成得分越高）
            float timeBonus = Math.max(0, 300 - ranking.getTimeTakenInSeconds()); // 5分钟内完成有奖励
            bonusScore += (long)(timeBonus * 2);
            
            // 无伤通关奖励
            if (ranking.isNoDamageClear()) {
                bonusScore += 500;
            }
        } else {
            // 失败时根据存活时间计算得分
            bonusScore += (long)(ranking.getTimeTakenInSeconds() * 1);
        }
        
        logger.debug("Calculated bonus score: {} for {} (enemies: {}, health: {}, towers: {}, time: {})", 
                    bonusScore, isVictory ? "victory" : "defeat",
                    ranking.getEnemiesKilled(), ranking.getRemainingHealth(), 
                    ranking.getTowersRemaining(), ranking.getTimeTakenInSeconds());
        
        return bonusScore;
    }
    
    /**
     * 检查游戏是否正在进行
     */
    public boolean isGameInProgress() {
        return gameInProgress;
    }
    
    /**
     * 手动设置当前得分（用于测试或特殊情况）
     */
    public void setCurrentScore(long score) {
        currentGameScore = score;
        logger.debug("Score manually set to: {}", score);
    }
}
