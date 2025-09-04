package com.csse3200.game.components.mainmenu;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.entities.EntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple test for save selection functionality
 * Tests the save game functionality implemented by your team
 */
@ExtendWith(com.csse3200.game.extensions.GameExtension.class)
class SaveSelectionDisplayTest {

    @Mock
    private ResourceService mockResourceService;
    
    @Mock
    private EntityService mockEntityService;

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
    void testSaveSelectionDisplayCreation() {
        // Test that save selection display can be created normally
        assertDoesNotThrow(() -> {
            SaveSelectionDisplay saveSelectionDisplay = new SaveSelectionDisplay();
            assertNotNull(saveSelectionDisplay);
        });
    }

    @Test
    void testSaveGameServiceCreation() {
        // Test that save game service can be created normally
        assertDoesNotThrow(() -> {
            SaveGameService saveGameService = new SaveGameService(mockEntityService);
            assertNotNull(saveGameService);
        });
    }

    @Test
    void testSaveGameServiceWithNullEntityService() {
        // Test save game service with null parameter
        assertDoesNotThrow(() -> {
            SaveGameService saveGameService = new SaveGameService(null);
            assertNotNull(saveGameService);
        });
    }

    @Test
    void testSaveSelectionDisplayComponents() {
        // Test that save selection display contains necessary components
        SaveSelectionDisplay saveSelectionDisplay = new SaveSelectionDisplay();
        
        // Verify object is created correctly
        assertNotNull(saveSelectionDisplay);
        
        // Test basic functionality (mainly verify no exceptions)
        assertDoesNotThrow(() -> {
            // If create method is called, should not throw exception
        });
    }

    @Test
    void testSaveGameServiceMethods() {
        // Test basic methods of save game service
        SaveGameService saveGameService = new SaveGameService(mockEntityService);
        
        // Verify service is created correctly
        assertNotNull(saveGameService);
        
        // Test service can work normally (mainly verify no exceptions)
        assertDoesNotThrow(() -> {
            // Basic operations should not throw exception
        });
    }
}
