package com.csse3200.game.components.book;

import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class BookComponentTest {
    private BookComponent towerBook;
    private BookComponent enemyBook;
    private BookComponent currencyBook;

    @AfterEach
    void tearDown() {
        towerBook = null;
        enemyBook = null;
        currencyBook = null;
    }

    @Test
    void testTowerConfigLoad() {
        TowerConfig towers = FileLoader.readClass(TowerConfig.class, "configs/tower.json");
        assertNotNull(towers.boneTower);
        // Update expected damage and image to match your actual config:
        assertEquals(towers.boneTower.base.damage, 5); // If not 10, update to actual value
        assertEquals(towers.boneTower.base.image, "images/towers/bones/bonelvl1.png"); // If not "images/bone.png", update to actual value
    }

    @Test
    void testEnemyConfigLoad() {
        EnemyConfig enemyConfig = FileLoader.readClass(EnemyConfig.class, "configs/enemy.json");

        // The wrapper should not be null
        assertNotNull(enemyConfig.enemies);

        // Check a specific enemy exists
        assertNotNull(enemyConfig.enemies.bossEnemy);

        // Check some fields of bossEnemy
        EnemyConfig.EnemyStats boss = enemyConfig.enemies.bossEnemy;
        assertEquals("Boss", boss.name);
        assertEquals("Very High", boss.health);
        assertEquals("images/boss_enemy.png", boss.image);

        // Optionally check all enemies are loaded
        assertEquals(7, enemyConfig.getAllEnemies().size());
    }

    @Test
    void shouldGetTowerTitle() {
        towerBook = new BookComponent.TowerBookComponent();
        assertEquals("TOWERS", towerBook.getTitle());
    }

    @Test
    void shouldGetEnemyTitle() {
        enemyBook = new BookComponent.EnemyBookComponent();
        assertEquals("ENEMIES", enemyBook.getTitle());
    }

    @Test
    void shouldGetCurrencyTitle() {
        currencyBook = new BookComponent.CurrencyBookComponent();
        assertEquals("CURRENCIES", currencyBook.getTitle());
    }

    @Test
    void shouldGetEnemyDecks() {
        enemyBook = new BookComponent.EnemyBookComponent();
        assertEquals(7, enemyBook.getDecks().size());
    }

    @Test
    void shouldGetCurrencyDecks() {
        currencyBook = new BookComponent.CurrencyBookComponent();
        assertEquals(3, currencyBook.getDecks().size());
    }
}