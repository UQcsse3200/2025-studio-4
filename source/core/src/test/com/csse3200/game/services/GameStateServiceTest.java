package com.csse3200.game.services;

import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;

@ExtendWith(GameExtension.class)
class GameStateServiceTest {
    GameStateService gameState;
    // helper to create a base with current and max health
    private Entity makeBaseWithHealth(int currentHealth, int maxHealth) {
        Entity base =  new Entity().addComponent(new PlayerCombatStatsComponent(maxHealth,1));
        base.create();
        base.getComponent(PlayerCombatStatsComponent.class).setHealth(currentHealth);
        return base;
    }

    @BeforeEach
    void beforeAll() {
        gameState = new GameStateService();
    }

    @Test
    void shouldGet() {
        assertEquals(0, gameState.getStars());
    }

    @Test
    void shouldSet() {
        gameState.setStars(345);
        assertEquals(345, gameState.getStars());
    }

    @Test
    void shouldIncrement() {
        gameState.updateStars(5);
        assertEquals(5, gameState.getStars());
    }

    @Test
    void multipleIncrement() {
        for (int i = 0; i < 10; i++) {
            gameState.updateStars(1);
            assertEquals(i + 1, gameState.getStars());
        }
    }

    @Test
    void rewardStarsByHealth_tiers_and_updates_total() {
        // 80% -> 3
        Entity base = makeBaseWithHealth(80,100);
        gameState.setBase(base);
        gameState.rewardStarsOnWin();
        assertEquals(3, gameState.getStars());

        // 55% -> 2
        base.getComponent(PlayerCombatStatsComponent.class).setHealth(55);
        gameState.rewardStarsOnWin();
        assertEquals(5, gameState.getStars());

        //10% -> 1
        base.getComponent(PlayerCombatStatsComponent.class).setHealth(10);
        gameState.rewardStarsOnWin();
        assertEquals(6, gameState.getStars());
    }

    @Test
    void rewardStarsByHealth_clamps_out_of_range_inputs() {
        gameState.setStars(0);
        // >1 should clamp to 1.0 -> 3★
        gameState.rewardStarsByHealth(1.5);
        assertEquals(3,gameState.getStars() );

        // <0 should clamp to 0.0 -> 0★
        gameState.rewardStarsByHealth(-0.2);
        assertEquals(3,gameState.getStars() );

    }

    @Test
    void rewardStarsOnWin_returnsZero_when_no_player_cached() {
        gameState.setStars(4);
        gameState.rewardStarsOnWin();
        assertEquals("Total unchanged when no player",4, gameState.getStars());
    }

    @Test
    void rewardStarsOnWin_returnsZero_when_missing_stats_component() {
        // Cache an entity with no PlayerCombatStatsComponent
        Entity bare = new Entity();
        bare.create();
        gameState.setBase(bare);

        gameState.setStars(2);
        gameState.rewardStarsOnWin();
        assertEquals(2, gameState.getStars());
    }

}
