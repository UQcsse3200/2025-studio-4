package com.csse3200.game.components.deck;

import com.csse3200.game.extensions.GameExtension;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class EnemyBookDeckComponentTest {
    @Test
    public void testEnemyBookDeckStats() {
        EnemyBookDeckComponent deck = new EnemyBookDeckComponent(
                "Goblin", "50", "10", "1.5", "Fast, Weak", "Melee", "Metal Scrap", "Small green creature", "5",
                "Fire", "Ice", "images/goblin.png", "false"
        );

        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("GOBLIN", stats.get(DeckComponent.StatType.NAME));
        assertEquals("50", stats.get(DeckComponent.StatType.MAX_HEALTH));
        assertEquals("10", stats.get(DeckComponent.StatType.DAMAGE));
        assertEquals("1.5", stats.get(DeckComponent.StatType.SPEED));
        assertEquals("Fast, Weak", stats.get(DeckComponent.StatType.TRAITS));
        assertEquals("Small green creature", stats.get(DeckComponent.StatType.LORE));
        assertEquals("Fire", stats.get(DeckComponent.StatType.WEAKNESS));
        assertEquals("Ice", stats.get(DeckComponent.StatType.RESISTANCE));
        assertEquals("images/goblin.png", stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertEquals("false", stats.get(DeckComponent.StatType.LOCKED));
    }
}