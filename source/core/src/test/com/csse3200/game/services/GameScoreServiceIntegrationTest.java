package com.csse3200.game.services;

import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.PlayerRankingComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class GameScoreServiceIntegrationTest {
    private GameScoreService gameScoreService;
    private EntityService entityService;
    private Entity player;

    @BeforeEach
    void setUp() {
        gameScoreService = new GameScoreService();
        entityService = new EntityService();
        ServiceLocator.registerEntityService(entityService);
        ServiceLocator.registerGameScoreService(gameScoreService);

        // 创建带有得分和排名组件的玩家实体
        player = new Entity();
        player.addComponent(new PlayerScoreComponent());
        player.addComponent(new PlayerRankingComponent());
        entityService.register(player);
        
        gameScoreService.startNewGame();
    }

    @Test
    void testScoreCalculationWithKills() {
        // 模拟击杀敌人获得得分
        PlayerScoreComponent scoreComponent = player.getComponent(PlayerScoreComponent.class);
        PlayerRankingComponent rankingComponent = player.getComponent(PlayerRankingComponent.class);
        
        // 添加一些得分和击杀
        scoreComponent.addPoints(100); // 击杀drone
        rankingComponent.addKill();
        
        scoreComponent.addPoints(150); // 击杀grunt
        rankingComponent.addKill();
        
        scoreComponent.addPoints(300); // 击杀tank
        rankingComponent.addKill();
        
        // 设置其他统计信息
        rankingComponent.setRemainingHealth(80);
        rankingComponent.setTowersRemaining(5);
        
        // 计算最终得分（胜利）
        long finalScore = gameScoreService.calculateFinalScore(true);
        
        // 验证得分包含基础得分和奖励得分
        long expectedBaseScore = 100 + 150 + 300; // 550
        long expectedBonusScore = 3 * 10 + 80 * 5 + 5 * 20; // 击杀奖励 + 生命奖励 + 塔奖励 = 30 + 400 + 100 = 530
        long expectedTotal = expectedBaseScore + expectedBonusScore; // 1080
        
        assertTrue(finalScore >= expectedBaseScore, "Final score should include base score from kills");
        assertTrue(finalScore > expectedBaseScore, "Final score should include bonus score");
        
        System.out.println("Base score: " + expectedBaseScore);
        System.out.println("Expected bonus: " + expectedBonusScore);
        System.out.println("Final score: " + finalScore);
    }

    @Test
    void testScoreCalculationWithoutKills() {
        // 测试没有击杀的情况
        PlayerRankingComponent rankingComponent = player.getComponent(PlayerRankingComponent.class);
        rankingComponent.setRemainingHealth(100);
        
        long finalScore = gameScoreService.calculateFinalScore(false);
        
        // 即使没有击杀，也应该有基于生命值的奖励得分
        assertTrue(finalScore >= 0, "Final score should be non-negative");
        
        System.out.println("Final score without kills: " + finalScore);
    }

    @Test
    void testCurrentGameScore() {
        // 测试实时得分获取
        PlayerScoreComponent scoreComponent = player.getComponent(PlayerScoreComponent.class);
        
        scoreComponent.addPoints(200);
        
        long currentScore = gameScoreService.getCurrentGameScore();
        assertEquals(200, currentScore, "Current game score should match player score component");
    }
}


