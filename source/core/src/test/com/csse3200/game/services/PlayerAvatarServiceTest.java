package com.csse3200.game.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerAvatarService implementation.
 */
class PlayerAvatarServiceTest {
    
    private PlayerAvatarService avatarService;
    
    @BeforeEach
    void setUp() {
        avatarService = new PlayerAvatarServiceImpl();
    }
    
    @Test
    void testDefaultAvatar() {
        assertEquals("avatar_1", avatarService.getPlayerAvatar());
    }
    
    @Test
    void testSetAndGetAvatar() {
        avatarService.setPlayerAvatar("avatar_2");
        assertEquals("avatar_2", avatarService.getPlayerAvatar());
        
        avatarService.setPlayerAvatar("avatar_3");
        assertEquals("avatar_3", avatarService.getPlayerAvatar());
    }
    
    @Test
    void testGetAvatarImagePath() {
        assertEquals("images/profile1.png", avatarService.getAvatarImagePath("avatar_1"));
        assertEquals("images/profile2.png", avatarService.getAvatarImagePath("avatar_2"));
        assertEquals("images/profile3.png", avatarService.getAvatarImagePath("avatar_3"));
        assertEquals("images/profile4.png", avatarService.getAvatarImagePath("avatar_4"));
    }
    
    @Test
    void testGetAvatarImagePathWithInvalidId() {
        // Should return default avatar path for invalid IDs
        assertEquals("images/profile1.png", avatarService.getAvatarImagePath("invalid_avatar"));
        assertEquals("images/profile1.png", avatarService.getAvatarImagePath(null));
        assertEquals("images/profile1.png", avatarService.getAvatarImagePath(""));
    }
    
    @Test
    void testGetAvatarDisplayName() {
        assertEquals("Knight", avatarService.getAvatarDisplayName("avatar_1"));
        assertEquals("Mage", avatarService.getAvatarDisplayName("avatar_2"));
        assertEquals("Warrior", avatarService.getAvatarDisplayName("avatar_3"));
        assertEquals("Archer", avatarService.getAvatarDisplayName("avatar_4"));
    }
    
    @Test
    void testGetAvatarDisplayNameWithInvalidId() {
        assertEquals("Knight", avatarService.getAvatarDisplayName("invalid_avatar"));
        assertEquals("Knight", avatarService.getAvatarDisplayName(null));
        assertEquals("Knight", avatarService.getAvatarDisplayName(""));
    }
    
    @Test
    void testGetAvailableAvatars() {
        String[] avatars = avatarService.getAvailableAvatars();
        assertNotNull(avatars);
        assertEquals(4, avatars.length);
        assertEquals("avatar_1", avatars[0]);
        assertEquals("avatar_2", avatars[1]);
        assertEquals("avatar_3", avatars[2]);
        assertEquals("avatar_4", avatars[3]);
    }
}
