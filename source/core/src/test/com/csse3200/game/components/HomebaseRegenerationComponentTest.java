package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 测试HomebaseRegenerationComponent的生命回复功能
 */
public class HomebaseRegenerationComponentTest {
    private Entity homebase;
    private HomebaseRegenerationComponent regenComponent;
    private PlayerCombatStatsComponent combatStats;
    private GameTime gameTime;
    private MockedStatic<ServiceLocator> serviceLocatorMock;
    
    @BeforeEach
    void setUp() {
        // Mock ServiceLocator
        serviceLocatorMock = mockStatic(ServiceLocator.class);
        
        // Mock GameTime
        gameTime = mock(GameTime.class);
        when(ServiceLocator.getTimeSource()).thenReturn(gameTime);
        
        // 创建homebase实体和组件
        homebase = new Entity();
        combatStats = new PlayerCombatStatsComponent(100, 10);
        regenComponent = new HomebaseRegenerationComponent();
        
        homebase.addComponent(combatStats);
        homebase.addComponent(regenComponent);
        homebase.create();
    }
    
    @AfterEach
    void tearDown() {
        if (serviceLocatorMock != null) {
            serviceLocatorMock.close();
        }
    }
    
    /**
     * 测试组件初始化
     */
    @Test
    void testComponentInitialization() {
        assertNotNull(regenComponent);
        assertFalse(regenComponent.isRegenerating());
    }
    
    /**
     * 测试受伤后不会立即回复
     */
    @Test
    void testNoRegenerationImmediatelyAfterDamage() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(90);
        
        // 3秒后（还没到5秒）
        when(gameTime.getTime()).thenReturn(3000L);
        regenComponent.update();
        
        // 不应该开始回复
        assertFalse(regenComponent.isRegenerating());
        assertEquals(90, combatStats.getHealth());
    }
    
    /**
     * 测试5秒后开始回复状态
     */
    @Test
    void testRegenerationStartsAfter5Seconds() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(90);
        
        // 5秒后
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // 应该进入回复状态
        assertTrue(regenComponent.isRegenerating());
    }
    
    /**
     * 测试每4秒回复5点生命
     */
    @Test
    void testRegenerationAmountAndInterval() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(80);
        
        // 5秒后，开始回复状态
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        assertTrue(regenComponent.isRegenerating());
        
        // 9秒后（5+4），应该回复一次
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        assertEquals(85, combatStats.getHealth()); // 80 + 5 = 85
        
        // 13秒后（5+4+4），应该再回复一次
        when(gameTime.getTime()).thenReturn(13000L);
        regenComponent.update();
        assertEquals(90, combatStats.getHealth()); // 85 + 5 = 90
    }
    
    /**
     * 测试受伤后回复被中断
     */
    @Test
    void testRegenerationStopsOnDamage() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(80);
        
        // 5秒后，开始回复状态
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        assertTrue(regenComponent.isRegenerating());
        
        // 7秒时再次受伤
        when(gameTime.getTime()).thenReturn(7000L);
        combatStats.setHealth(70);
        
        // 回复应该停止
        assertFalse(regenComponent.isRegenerating());
        
        // 8秒时（距离最后一次受伤才1秒）不应该回复
        when(gameTime.getTime()).thenReturn(8000L);
        regenComponent.update();
        assertFalse(regenComponent.isRegenerating());
        assertEquals(70, combatStats.getHealth());
    }
    
    /**
     * 测试生命值不会超过最大值
     */
    @Test
    void testRegenerationDoesNotExceedMaxHealth() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到少量伤害（只损失3点生命）
        combatStats.setHealth(97);
        
        // 5秒后，开始回复状态
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // 9秒后（5+4），应该回复，但不超过最大值
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        assertEquals(100, combatStats.getHealth()); // 应该是100，不是97+5=102
    }
    
    /**
     * 测试满血时不回复
     */
    @Test
    void testNoRegenerationAtFullHealth() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 满血状态
        combatStats.setHealth(100);
        
        // 5秒后
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // 9秒后
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        
        // 生命值应该保持不变
        assertEquals(100, combatStats.getHealth());
    }
    
    /**
     * 测试获取距离上次受伤的时间
     */
    @Test
    void testGetTimeSinceLastDamage() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(80);
        
        // 3秒后
        when(gameTime.getTime()).thenReturn(3000L);
        float timeSinceDamage = regenComponent.getTimeSinceLastDamage();
        assertEquals(3.0f, timeSinceDamage, 0.01f);
    }
    
    /**
     * 测试获取距离下次回复的时间
     */
    @Test
    void testGetTimeUntilNextRegen() {
        // 设置初始时间
        when(gameTime.getTime()).thenReturn(0L);
        
        // 受到伤害
        combatStats.setHealth(80);
        
        // 5秒后，开始回复状态
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // 7秒后（距离上次回复2秒）
        when(gameTime.getTime()).thenReturn(7000L);
        float timeUntilNext = regenComponent.getTimeUntilNextRegen();
        assertEquals(2.0f, timeUntilNext, 0.01f); // 4-2 = 2秒
    }
}

