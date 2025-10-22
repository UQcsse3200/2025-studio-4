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
 * Test the health regeneration functionality of HomebaseRegenerationComponent
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
        
        // Create homebase entity and components
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
     * Test component initialization
     */
    @Test
    void testComponentInitialization() {
        assertNotNull(regenComponent);
        assertFalse(regenComponent.isRegenerating());
    }
    
    /**
     * Test regeneration starts after 5 seconds
     */
    @Test
    void testRegenerationStartsAfter5Seconds() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Take damage
        combatStats.setHealth(90);
        
        // After 5 seconds
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // Should enter regeneration state
        assertTrue(regenComponent.isRegenerating());
    }
    
    /**
     * Test regeneration amount and interval (5 health every 4 seconds)
     */
    @Test
    void testRegenerationAmountAndInterval() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Take damage
        combatStats.setHealth(80);
        
        // After 5 seconds, start regeneration state
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        assertTrue(regenComponent.isRegenerating());
        
        // After 9 seconds (5+4), should regenerate once
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        assertEquals(85, combatStats.getHealth()); // 80 + 5 = 85
        
        // After 13 seconds (5+4+4), should regenerate again
        when(gameTime.getTime()).thenReturn(13000L);
        regenComponent.update();
        assertEquals(90, combatStats.getHealth()); // 85 + 5 = 90
    }
    
    /**
     * Test health does not exceed maximum value
     */
    @Test
    void testRegenerationDoesNotExceedMaxHealth() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Take small damage (only lose 3 health)
        combatStats.setHealth(97);
        
        // After 5 seconds, start regeneration state
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // After 9 seconds (5+4), should regenerate but not exceed maximum
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        assertEquals(100, combatStats.getHealth()); // Should be 100, not 97+5=102
    }
    
    /**
     * Test no regeneration at full health
     */
    @Test
    void testNoRegenerationAtFullHealth() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Full health state
        combatStats.setHealth(100);
        
        // After 5 seconds
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // After 9 seconds
        when(gameTime.getTime()).thenReturn(9000L);
        regenComponent.update();
        
        // Health should remain unchanged
        assertEquals(100, combatStats.getHealth());
    }
    
    /**
     * Test getting time since last damage
     */
    @Test
    void testGetTimeSinceLastDamage() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Take damage
        combatStats.setHealth(80);
        
        // After 3 seconds
        when(gameTime.getTime()).thenReturn(3000L);
        float timeSinceDamage = regenComponent.getTimeSinceLastDamage();
        assertEquals(3.0f, timeSinceDamage, 0.01f);
    }
    
    /**
     * Test getting time until next regeneration
     */
    @Test
    void testGetTimeUntilNextRegen() {
        // Set initial time
        when(gameTime.getTime()).thenReturn(0L);
        
        // Take damage
        combatStats.setHealth(80);
        
        // After 5 seconds, start regeneration state
        when(gameTime.getTime()).thenReturn(5000L);
        regenComponent.update();
        
        // After 7 seconds (2 seconds since last regeneration)
        when(gameTime.getTime()).thenReturn(7000L);
        float timeUntilNext = regenComponent.getTimeUntilNextRegen();
        assertEquals(2.0f, timeUntilNext, 0.01f); // 4-2 = 2 seconds
    }
}

