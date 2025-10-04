package com.csse3200.game.components.book;

import com.csse3200.game.components.deck.CurrencyBookDeckComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.deck.EnemyBookDeckComponent;
import com.csse3200.game.components.deck.TowerBookDeckComponent;
import com.csse3200.game.entities.configs.CurrencyConfig;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.files.FileLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BookComponent {
    private final String title;
    private final List<DeckComponent> decks;

    public BookComponent(String title, List<DeckComponent> decks) {
        this.title = title;
        this.decks = decks;
    }

    public String getTitle() {
        return title;
    }

    public List<DeckComponent> getDecks() {
        return decks;
    }

    public static class TowerBookComponent extends BookComponent {
        private static final TowerConfig towerConfig = FileLoader.readClass(TowerConfig.class, "configs/tower.json");

        public TowerBookComponent() {
            super("TOWERS", createDecks());
        }
        private static List<DeckComponent> createDecks() {
            List<DeckComponent> decks = new ArrayList<>();

            try {
                // Loop through all fields of TowerConfig (boneTower, dinoTower, etc.)
                for (Field field : TowerConfig.class.getDeclaredFields()) {
                    if (!TowerConfig.TowerWrapper.class.isAssignableFrom(field.getType())) continue;

                    TowerConfig.TowerWrapper wrapper = (TowerConfig.TowerWrapper) field.get(towerConfig);
                    TowerConfig.TowerStats baseStats = wrapper.base;

                    decks.add(new TowerBookDeckComponent(
                            wrapper.name,
                            baseStats.damage,
                            baseStats.range,
                            baseStats.cooldown,
                            baseStats.projectileSpeed,
                            baseStats.projectileLife,
                            baseStats.metalScrapCost,
                            baseStats.titaniumCoreCost,
                            baseStats.neurochipCost,
                            wrapper.lore,
                            baseStats.image
                    ));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return decks;
        }
    }

    public static class EnemyBookComponent extends BookComponent {
        private static final EnemyConfig enemyConfig = FileLoader.readClass(EnemyConfig.class, "configs/enemy.json");

        public EnemyBookComponent() {
            super("ENEMIES", createDecks());
        }

        private static List<DeckComponent> createDecks() {
            List<DeckComponent> decks = new ArrayList<>();

            // Loop through all enemies in the config
            for (EnemyConfig.EnemyStats enemy : enemyConfig.getAllEnemies()) {
                EnemyBookDeckComponent deck = new EnemyBookDeckComponent(
                        enemy.name,
                        enemy.health,
                        enemy.damage,
                        enemy.speed,
                        enemy.traits,
                        enemy.role,
                        enemy.currency,
                        enemy.lore,
                        enemy.points,
                        enemy.weakness,
                        enemy.resistance,
                        enemy.image,
                        enemy.locked
                );
                decks.add(deck);
            }

            return decks;
        }
    }

    public static class CurrencyBookComponent extends BookComponent {
        private static final CurrencyConfig currencyConfig = FileLoader.readClass(CurrencyConfig.class, "configs/currency.json");

        public CurrencyBookComponent() {
            super("CURRENCIES", createDecks());
        }

        private static List<DeckComponent> createDecks() {
            List<DeckComponent> decks = new ArrayList<>();

            for (CurrencyConfig.CurrencyStats c : currencyConfig.getAllCurrencies()) {
                decks.add(new CurrencyBookDeckComponent(
                        c.name, c.lore, c.image, c.sound
                ));
            }

            return decks;
        }
    }
}
