package com.csse3200.game.components.deck;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class TowerBookDeckComponentTest {
    @Test
    public void testTowerBookDeckStats() {
        TowerBookDeckComponent deck = new TowerBookDeckComponent(
                "Arrow Tower", 15, 5.0, 1.2, 3.0, 2.5, 50, 10, 1, "Basic arrow-shooting tower", "images/arrow_tower.png", false
        );

        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("ARROW TOWER", stats.get(DeckComponent.StatType.NAME));
        assertEquals("15", stats.get(DeckComponent.StatType.DAMAGE));
        assertEquals("5.0", stats.get(DeckComponent.StatType.RANGE));
        assertEquals("1.2", stats.get(DeckComponent.StatType.COOLDOWN));
        assertEquals("3.0", stats.get(DeckComponent.StatType.PROJECTILE_SPEED));
        assertEquals("Basic arrow-shooting tower", stats.get(DeckComponent.StatType.LORE));
        assertEquals("images/arrow_tower.png", stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertEquals("false", stats.get(DeckComponent.StatType.LOCKED));
    }
}