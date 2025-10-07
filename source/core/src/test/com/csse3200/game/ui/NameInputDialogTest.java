package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.PlayerAvatarService;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NameInputDialog class.
 */
@ExtendWith(GameExtension.class)
class NameInputDialogTest {
    
    private PlayerNameService playerNameService;
    private PlayerAvatarService playerAvatarService;
    
    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        
        playerNameService = new com.csse3200.game.services.PlayerNameServiceImpl();
        playerAvatarService = new com.csse3200.game.services.PlayerAvatarServiceImpl();
        
        ServiceLocator.registerPlayerNameService(playerNameService);
        ServiceLocator.registerPlayerAvatarService(playerAvatarService);
    }
    
    @Test
    void testNameInputDialogCreation() {
        // Test that NameInputDialog can be created
        NameInputDialog dialog = new NameInputDialog("Test Title", SimpleUI.windowStyle());
        assertNotNull(dialog);
        assertTrue(dialog instanceof Window);
    }
    
    @Test
    void testNameInputDialogWithCallback() {
        // Test NameInputDialog with callback
        boolean[] callbackCalled = {false};
        String[] receivedName = {null};
        String[] receivedAvatar = {null};
        
        NameInputDialog.NameInputCallback callback = new NameInputDialog.NameInputCallback() {
            @Override
            public void onNameConfirmed(String name, String avatarId) {
                callbackCalled[0] = true;
                receivedName[0] = name;
                receivedAvatar[0] = avatarId;
            }
            
            @Override
            public void onNameCancelled() {
                callbackCalled[0] = true;
            }
        };
        
        NameInputDialog dialog = new NameInputDialog("Test Title", SimpleUI.windowStyle(), callback);
        assertNotNull(dialog);
    }
    
    @Test
    void testPlayerNameServiceIntegration() {
        // Test that NameInputDialog uses the registered PlayerNameService
        assertSame(playerNameService, ServiceLocator.getPlayerNameService());
        
        // Test default name
        assertEquals("Player", playerNameService.getPlayerName());
        assertFalse(playerNameService.hasPlayerName());
    }
    
    @Test
    void testPlayerAvatarServiceIntegration() {
        // Test that NameInputDialog uses the registered PlayerAvatarService
        assertSame(playerAvatarService, ServiceLocator.getPlayerAvatarService());
        
        // Test default avatar
        assertEquals("avatar_1", playerAvatarService.getPlayerAvatar());
        assertFalse(playerAvatarService.hasPlayerAvatar());
        
        // Test available avatars
        String[] avatars = playerAvatarService.getAvailableAvatars();
        assertEquals(4, avatars.length);
    }
    
    @Test
    void testAvatarServiceMethods() {
        // Test avatar display names
        assertEquals("Knight", playerAvatarService.getAvatarDisplayName("avatar_1"));
        assertEquals("Mage", playerAvatarService.getAvatarDisplayName("avatar_2"));
        assertEquals("Warrior", playerAvatarService.getAvatarDisplayName("avatar_3"));
        assertEquals("Archer", playerAvatarService.getAvatarDisplayName("avatar_4"));
        
        // Test avatar image paths
        assertEquals("images/profile1.png", playerAvatarService.getAvatarImagePath("avatar_1"));
        assertEquals("images/profile2.png", playerAvatarService.getAvatarImagePath("avatar_2"));
        assertEquals("images/profile3.png", playerAvatarService.getAvatarImagePath("avatar_3"));
        assertEquals("images/profile4.png", playerAvatarService.getAvatarImagePath("avatar_4"));
    }
    
    @Test
    void testServiceLocatorIntegration() {
        // Test that services are properly registered in ServiceLocator
        assertNotNull(ServiceLocator.getPlayerNameService());
        assertNotNull(ServiceLocator.getPlayerAvatarService());
        
        // Test that they are the same instances we registered
        assertSame(playerNameService, ServiceLocator.getPlayerNameService());
        assertSame(playerAvatarService, ServiceLocator.getPlayerAvatarService());
    }
}
