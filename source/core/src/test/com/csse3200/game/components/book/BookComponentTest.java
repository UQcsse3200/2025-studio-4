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
        assertEquals(10, towers.boneTower.base.damage);
        assertEquals("images/bone.png", towers.boneTower.base.image);
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
        assertEquals("Boss Enemy", boss.name);
        assertEquals("Very High", boss.health);
        assertEquals("images/boss_enemy.png", boss.image);

        // Optionally check all enemies are loaded
        assertEquals(6, enemyConfig.getAllEnemies().size());
    }

    @Test
    void shouldGetTowerTitle() {
        towerBook = new BookComponent.TowerBookComponent();
        assertEquals("TOWER", towerBook.getTitle());
    }

    @Test
    void shouldGetEnemyTitle() {
        enemyBook = new BookComponent.EnemyBookComponent();
        assertEquals("ENEMY", enemyBook.getTitle());
    }

    @Test
    void shouldGetCurrencyTitle() {
        currencyBook = new BookComponent.CurrencyBookComponent();
        assertEquals("CURRENCY", currencyBook.getTitle());
    }

    @Test
    void shouldGetTowerDecks() {
        towerBook = new BookComponent.TowerBookComponent();
        assertEquals(3, towerBook.getDecks().size());
    }

    @Test
    void shouldGetEnemyDecks() {
        enemyBook = new BookComponent.EnemyBookComponent();
        assertEquals(6, enemyBook.getDecks().size());
    }

    @Test
    void shouldGetCurrencyDecks() {
        currencyBook = new BookComponent.CurrencyBookComponent();
        assertEquals(3, currencyBook.getDecks().size());
    }
}