package com.csse3200.game.components.deck;

import com.csse3200.game.entities.configs.EngineerConfig;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.extensions.GameExtension;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class HeroBookDeckComponentTest {

    @Test
    public void testBaseHeroStats() {
        HeroConfig cfg = new HeroConfig();
        HeroBookDeckComponent deck = HeroBookDeckComponent.from("Hero", cfg);
        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("HERO", stats.get(DeckComponent.StatType.NAME));
        assertEquals(String.valueOf(cfg.health), stats.get(DeckComponent.StatType.MAX_HEALTH));
        assertEquals(String.valueOf(cfg.baseAttack), stats.get(DeckComponent.StatType.DAMAGE));
        assertEquals(String.valueOf(cfg.attackCooldown), stats.get(DeckComponent.StatType.COOLDOWN));
        assertEquals(String.valueOf(cfg.bulletSpeed), stats.get(DeckComponent.StatType.PROJECTILE_SPEED));
        assertEquals(String.valueOf(cfg.bulletLife), stats.get(DeckComponent.StatType.PROJECTILE_LIFE));
        assertEquals(cfg.heroTexture, stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertTrue(stats.get(DeckComponent.StatType.LORE).contains("ranged fighter"));
    }

    @Test
    public void testEngineerStats() {
        EngineerConfig cfg = new EngineerConfig();
        HeroBookDeckComponent deck = HeroBookDeckComponent.from("Engineer", cfg);
        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("ENGINEER", stats.get(DeckComponent.StatType.NAME));
        assertEquals(String.valueOf(cfg.health), stats.get(DeckComponent.StatType.MAX_HEALTH));
        assertEquals(String.valueOf(cfg.baseAttack), stats.get(DeckComponent.StatType.DAMAGE));
        assertEquals(String.valueOf(cfg.attackCooldown), stats.get(DeckComponent.StatType.COOLDOWN));
        assertEquals(String.valueOf(cfg.bulletSpeed), stats.get(DeckComponent.StatType.PROJECTILE_SPEED));
        assertEquals(String.valueOf(cfg.bulletLife), stats.get(DeckComponent.StatType.PROJECTILE_LIFE));
        assertEquals(cfg.heroTexture, stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertTrue(stats.get(DeckComponent.StatType.LORE).contains("tactical builder"));
    }

    @Test
    public void testSamuraiStats() {
        SamuraiConfig cfg = new SamuraiConfig();
        HeroBookDeckComponent deck = HeroBookDeckComponent.from("Samurai", cfg);
        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("SAMURAI", stats.get(DeckComponent.StatType.NAME));
        assertEquals(String.valueOf(cfg.health), stats.get(DeckComponent.StatType.MAX_HEALTH));
        assertEquals(cfg.heroTexture, stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertTrue(stats.get(DeckComponent.StatType.LORE).contains("close-range warrior"));
    }
}
