package com.csse3200.game.services;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.SessionLeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for game initialization and service registration.
 * Tests the functionality that would be called from main() function.
 */
@ExtendWith(GameExtension.class)
class GameInitializationIntegrationTest {
    
    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
    }
    
    @Test
    void testServiceRegistrationSequence() {
        // Test the sequence of service registrations that happen in GdxGame.create()
        
        // Register game state service
        GameStateService gameStateService = new GameStateService();
        ServiceLocator.registerGameStateService(gameStateService);
        assertSame(gameStateService, ServiceLocator.getGameStateService());
        
        // Register player name service
        PlayerNameService playerNameService = new PlayerNameServiceImpl();
        ServiceLocator.registerPlayerNameService(playerNameService);
        assertSame(playerNameService, ServiceLocator.getPlayerNameService());
        
        // Register player avatar service
        PlayerAvatarService playerAvatarService = new PlayerAvatarServiceImpl();
        ServiceLocator.registerPlayerAvatarService(playerAvatarService);
        assertSame(playerAvatarService, ServiceLocator.getPlayerAvatarService());
        
        // Register leaderboard service
        LeaderboardService leaderboardService = new SessionLeaderboardService("test-player");
        ServiceLocator.registerLeaderboardService(leaderboardService);
        assertSame(leaderboardService, ServiceLocator.getLeaderboardService());
        
        // Register game session manager
        GameSessionManager gameSessionManager = new GameSessionManager();
        ServiceLocator.registerGameSessionManager(gameSessionManager);
        assertSame(gameSessionManager, ServiceLocator.getGameSessionManager());
        
        // Register game score service
        GameScoreService gameScoreService = new GameScoreService();
        ServiceLocator.registerGameScoreService(gameScoreService);
        assertSame(gameScoreService, ServiceLocator.getGameScoreService());
    }
    
    @Test
    void testServiceInteractions() {
        // Test that services can interact with each other as they would in the real game
        
        // Register all services
        ServiceLocator.registerGameStateService(new GameStateService());
        ServiceLocator.registerPlayerNameService(new PlayerNameServiceImpl());
        ServiceLocator.registerPlayerAvatarService(new PlayerAvatarServiceImpl());
        ServiceLocator.registerLeaderboardService(new SessionLeaderboardService("test-player"));
        ServiceLocator.registerGameSessionManager(new GameSessionManager());
        ServiceLocator.registerGameScoreService(new GameScoreService());
        
        // Test player name and avatar interaction
        PlayerNameService nameService = ServiceLocator.getPlayerNameService();
        PlayerAvatarService avatarService = ServiceLocator.getPlayerAvatarService();
        
        nameService.setPlayerName("TestPlayer");
        avatarService.setPlayerAvatar("avatar_2");
        
        assertEquals("TestPlayer", nameService.getPlayerName());
        assertEquals("avatar_2", avatarService.getPlayerAvatar());
        
        // Test leaderboard service
        LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
        leaderboardService.submitScore(1000);
        
        var myBest = leaderboardService.getMyBest();
        assertNotNull(myBest);
        assertEquals(1000, myBest.score);
        
        // Test game session manager
        GameSessionManager sessionManager = ServiceLocator.getGameSessionManager();
        assertNotNull(sessionManager);
        
        // Test game score service
        GameScoreService scoreService = ServiceLocator.getGameScoreService();
        assertNotNull(scoreService);
    }
    
    @Test
    void testServiceLocatorClear() {
        // Test that ServiceLocator.clear() works properly
        ServiceLocator.registerGameStateService(new GameStateService());
        ServiceLocator.registerPlayerNameService(new PlayerNameServiceImpl());
        ServiceLocator.registerPlayerAvatarService(new PlayerAvatarServiceImpl());
        
        assertNotNull(ServiceLocator.getGameStateService());
        assertNotNull(ServiceLocator.getPlayerNameService());
        assertNotNull(ServiceLocator.getPlayerAvatarService());
        
        ServiceLocator.clear();
        
        assertNull(ServiceLocator.getGameStateService());
        assertNull(ServiceLocator.getPlayerNameService());
        assertNull(ServiceLocator.getPlayerAvatarService());
    }
    
    @Test
    void testGdxGameScreenTypes() {
        // Test that GdxGame can handle all screen types
        GdxGame game = new GdxGame();
        
        for (GdxGame.ScreenType screenType : GdxGame.ScreenType.values()) {
            try {
                game.setScreen(screenType);
                // If we get here without exception, the screen type is valid
                assertTrue(true, "Screen type " + screenType + " should be valid");
            } catch (Exception e) {
                fail("Screen type " + screenType + " should not throw exception: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testPlayerNameServiceIntegration() {
        // Test PlayerNameService with default values
        PlayerNameService nameService = new PlayerNameServiceImpl();
        ServiceLocator.registerPlayerNameService(nameService);
        
        // Test default name
        assertEquals("Player", nameService.getPlayerName());
        assertFalse(nameService.hasPlayerName());
        
        // Test setting name
        nameService.setPlayerName("TestPlayer");
        assertEquals("TestPlayer", nameService.getPlayerName());
        assertTrue(nameService.hasPlayerName());
        
        // Test clearing name
        nameService.clearPlayerName();
        assertEquals("Player", nameService.getPlayerName());
        assertFalse(nameService.hasPlayerName());
    }
    
    @Test
    void testPlayerAvatarServiceIntegration() {
        // Test PlayerAvatarService with default values
        PlayerAvatarService avatarService = new PlayerAvatarServiceImpl();
        ServiceLocator.registerPlayerAvatarService(avatarService);
        
        // Test default avatar
        assertEquals("avatar_1", avatarService.getPlayerAvatar());
        assertFalse(avatarService.hasPlayerAvatar());
        
        // Test setting avatar
        avatarService.setPlayerAvatar("avatar_2");
        assertEquals("avatar_2", avatarService.getPlayerAvatar());
        assertTrue(avatarService.hasPlayerAvatar());
        
        // Test available avatars
        String[] avatars = avatarService.getAvailableAvatars();
        assertEquals(4, avatars.length);
        assertEquals("avatar_1", avatars[0]);
        assertEquals("avatar_4", avatars[3]);
        
        // Test avatar display names
        assertEquals("Knight", avatarService.getAvatarDisplayName("avatar_1"));
        assertEquals("Mage", avatarService.getAvatarDisplayName("avatar_2"));
        assertEquals("Warrior", avatarService.getAvatarDisplayName("avatar_3"));
        assertEquals("Archer", avatarService.getAvatarDisplayName("avatar_4"));
        
        // Test avatar image paths
        assertEquals("images/profile1.png", avatarService.getAvatarImagePath("avatar_1"));
        assertEquals("images/profile2.png", avatarService.getAvatarImagePath("avatar_2"));
        assertEquals("images/profile3.png", avatarService.getAvatarImagePath("avatar_3"));
        assertEquals("images/profile4.png", avatarService.getAvatarImagePath("avatar_4"));
    }
}
