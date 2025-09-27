package com.csse3200.game.components.deck;

import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

@ExtendWith(GameExtension.class)
class DeckComponentTest {
    private DeckComponent deck;

    @BeforeEach
    void setUp() {
        deck = new DeckComponent.EnemyDeckComponent("Drone", 100, 100, DamageTypeConfig.None, DamageTypeConfig.Electricity,"image/drone.png");
    }

    @AfterEach
    void tearDown() {
        deck = null;
    }

    @Test
    void getStats() {
        Map<DeckComponent.StatType, String> stats = deck.getStats();
        assertEquals("DRONE", stats.get(DeckComponent.StatType.NAME));
        assertEquals("100", stats.get(DeckComponent.StatType.MAX_HEALTH));
        assertEquals("100", stats.get(DeckComponent.StatType.DAMAGE));
        assertEquals("NONE", stats.get(DeckComponent.StatType.RESISTANCE));
        assertEquals("ELECTRICITY", stats.get(DeckComponent.StatType.WEAKNESS));
        assertEquals("image/drone.png", stats.get(DeckComponent.StatType.TEXTURE_PATH));
    }
}