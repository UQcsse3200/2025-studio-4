package com.csse3200.game.components.book;

import com.csse3200.game.extensions.GameExtension;
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

    @BeforeEach
    void setUp() {
        towerBook = new BookComponent.TowerBookComponent();
        enemyBook = new BookComponent.EnemyBookComponent();
        currencyBook = new BookComponent.CurrencyBookComponent();
    }

    @AfterEach
    void tearDown() {
        towerBook = null;
        enemyBook = null;
        currencyBook = null;
    }

    @Test
    void shouldGetTowerTitle() {
        assertEquals("TOWER", towerBook.getTitle());
    }

    @Test
    void shouldGetEnemyTitle() {
        assertEquals("ENEMY", enemyBook.getTitle());
    }

    @Test
    void shouldGetCurrencyTitle() {
        assertEquals("CURRENCY", currencyBook.getTitle());
    }

    @Test
    void shouldGetTowerDecks() {
        assertEquals(3, towerBook.getDecks().size());
    }

    @Test
    void shouldGetEnemyDecks() {
        assertEquals(6, enemyBook.getDecks().size());
    }

    @Test
    void shouldGetCurrencyDecks() {
        assertEquals(3, currencyBook.getDecks().size());
    }
}