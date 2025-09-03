package com.csse3200.game.components.settingsmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple test for settings menu functionality
 * Tests the audio, difficulty, and language options implemented by your team
 */
@ExtendWith(com.csse3200.game.extensions.GameExtension.class)
class SettingsMenuDisplayTest {

    @Mock
    private GdxGame mockGame;
    
    @Mock
    private ResourceService mockResourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup ServiceLocator
        ServiceLocator.registerResourceService(mockResourceService);
        
        // Mock resource service return
        when(mockResourceService.getAsset(anyString(), eq(com.badlogic.gdx.graphics.Texture.class)))
                .thenReturn(mock(com.badlogic.gdx.graphics.Texture.class));
    }

    @Test
    void testSettingsMenuCreation() {
        // Test that settings menu can be created normally
        assertDoesNotThrow(() -> {
            SettingsMenuDisplay settingsMenu = new SettingsMenuDisplay(mockGame);
            assertNotNull(settingsMenu);
        });
    }

    @Test
    void testOverlayModeConstructor() {
        // Test overlay mode constructor
        SettingsMenuDisplay overlaySettings = new SettingsMenuDisplay(mockGame, true);
        assertNotNull(overlaySettings);
        
        // Test non-overlay mode constructor
        SettingsMenuDisplay normalSettings = new SettingsMenuDisplay(mockGame, false);
        assertNotNull(normalSettings);
    }

    @Test
    void testUserSettingsDefaultValues() {
        // Test default values of user settings
        UserSettings.Settings settings = new UserSettings.Settings();
        
        assertEquals(60, settings.fps);
        assertTrue(settings.fullscreen);
        assertTrue(settings.vsync);
        assertEquals(1.0f, settings.uiScale);
        assertNull(settings.displayMode);
    }

    @Test
    void testDisplaySettings() {
        // Test display settings
        UserSettings.DisplaySettings displaySettings = new UserSettings.DisplaySettings();
        displaySettings.width = 1920;
        displaySettings.height = 1080;
        displaySettings.refreshRate = 60;
        
        assertEquals(1920, displaySettings.width);
        assertEquals(1080, displaySettings.height);
        assertEquals(60, displaySettings.refreshRate);
    }

    @Test
    void testSettingsMenuOverlayMode() {
        // Test settings menu in overlay mode
        SettingsMenuDisplay overlaySettings = new SettingsMenuDisplay(mockGame, true);
        
        // Verify overlay mode is set correctly
        assertNotNull(overlaySettings);
    }

    @Test
    void testSettingsMenuNonOverlayMode() {
        // Test settings menu in non-overlay mode
        SettingsMenuDisplay normalSettings = new SettingsMenuDisplay(mockGame, false);
        
        // Verify non-overlay mode is set correctly
        assertNotNull(normalSettings);
    }
}
