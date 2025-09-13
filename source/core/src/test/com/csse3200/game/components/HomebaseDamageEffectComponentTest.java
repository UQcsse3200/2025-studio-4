package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple test for HomebaseDamageEffectComponent.
 * Uses lenient mocking to avoid UnnecessaryStubbingException.
 */
@ExtendWith(MockitoExtension.class)
class HomebaseDamageEffectComponentSimpleTest {
    
    @Mock
    private GameTime mockTimeService;
    
    @Mock
    private ResourceService mockResourceService;
    
    @Mock
    private Texture mockNormalTexture;
    
    @Mock
    private Texture mockDamagedTexture;
    
    @Mock
    private SwitchableTextureRenderComponent mockTextureComponent;
    
    @Mock
    private PlayerCombatStatsComponent mockCombatStats;
    
    private Entity entity;
    private HomebaseDamageEffectComponent damageEffectComponent;
    
    @BeforeEach
    void setUp() {
        // Setup ServiceLocator
        ServiceLocator.registerTimeSource(mockTimeService);
        ServiceLocator.registerResourceService(mockResourceService);
        
        // Setup resource service mocks with lenient stubbing
        lenient().when(mockResourceService.getAsset("images/basement.png", Texture.class)).thenReturn(mockNormalTexture);
        lenient().when(mockResourceService.getAsset("images/basement_damaged.png", Texture.class)).thenReturn(mockDamagedTexture);
        
        // Setup time service
        lenient().when(mockTimeService.getDeltaTime()).thenReturn(0.016f); // 60 FPS
        
        // Setup combat stats mock
        lenient().when(mockCombatStats.getHealth()).thenReturn(100);
        
        // Create entity and add components
        entity = new Entity();
        entity.addComponent(mockTextureComponent);
        entity.addComponent(mockCombatStats);
        entity.addComponent(new HomebaseDamageEffectComponent());
        
        // Get the damage effect component
        damageEffectComponent = entity.getComponent(HomebaseDamageEffectComponent.class);
        
        // Create the entity to initialize components
        entity.create();
    }
    
    @Test
    void shouldCreateComponent() {
        // Verify component exists
        assertNotNull(damageEffectComponent);
        assertNotNull(entity.getComponent(HomebaseDamageEffectComponent.class));
    }
    
    @Test
    void shouldHaveCorrectDuration() {
        // Check duration
        assertEquals(0.5f, damageEffectComponent.getDamageEffectDuration());
    }
    
    @Test
    void shouldCheckForDamagedTextureAvailability() {
        // Check if damaged texture is available
        boolean hasTexture = damageEffectComponent.hasDamagedTexture();
        assertTrue(hasTexture); // Should be true since we mocked it
    }
    
    @Test
    void shouldNotBeShowingDamageEffectInitially() {
        // Verify initial state
        assertFalse(damageEffectComponent.isShowingDamageEffect());
    }
    
    @Test
    void shouldTriggerDamageEffectOnHealthDecrease() {
        // Simulate health decrease
        entity.getEvents().trigger("updateHealth", 80);
        
        // Verify damage effect was triggered
        assertTrue(damageEffectComponent.isShowingDamageEffect());
        verify(mockTextureComponent).setColor(any(Color.class));
        verify(mockTextureComponent).setTexture(mockDamagedTexture);
    }
    
    @Test
    void shouldNotTriggerDamageEffectOnHealthIncrease() {
        // Simulate health increase
        entity.getEvents().trigger("updateHealth", 100);
        
        // Verify no damage effect was triggered
        assertFalse(damageEffectComponent.isShowingDamageEffect());
        verify(mockTextureComponent, never()).setColor(any(Color.class));
    }
    
    @Test
    void shouldNotTriggerMultipleDamageEffectsSimultaneously() {
        // Simulate multiple rapid damage
        entity.getEvents().trigger("updateHealth", 80);
        entity.getEvents().trigger("updateHealth", 60);
        entity.getEvents().trigger("updateHealth", 40);
        
        // Should only trigger once (subsequent calls should be ignored)
        verify(mockTextureComponent, times(1)).setColor(any(Color.class));
        verify(mockTextureComponent, times(1)).setTexture(mockDamagedTexture);
    }
    
    @Test
    void shouldReturnToNormalAfterDuration() {
        // Simulate taking damage
        entity.getEvents().trigger("updateHealth", 80);
        
        // Verify damage effect is active
        assertTrue(damageEffectComponent.isShowingDamageEffect());
        
        // Reset mock to clear previous calls
        reset(mockTextureComponent);
        
        // Simulate time passing (more than damage duration)
        for (int i = 0; i < 50; i++) { // 50 frames at 60 FPS = ~0.83 seconds
            entity.update();
        }
        
        // Verify return to normal was called
        assertFalse(damageEffectComponent.isShowingDamageEffect());
        verify(mockTextureComponent).setColor(any(Color.class));
        verify(mockTextureComponent).setTexture(mockNormalTexture);
    }
}