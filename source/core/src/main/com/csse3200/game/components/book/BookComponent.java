package com.csse3200.game.components.book;

import com.csse3200.game.components.deck.AchievementBookDeckComponent;
import com.csse3200.game.components.deck.CurrencyBookDeckComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.deck.EnemyBookDeckComponent;
import com.csse3200.game.components.deck.TowerBookDeckComponent;
import com.csse3200.game.entities.configs.CurrencyConfig;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.services.AchievementService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a book that contains multiple deck entries for different
 * categories such as towers, enemies, and currencies. Each deck stores detailed
 * stats or information for display in the book UI.
 */
public class BookComponent {
    private static final Logger logger = LoggerFactory.getLogger(BookComponent.class);
    /** The title of this book (e.g., "TOWERS", "ENEMIES", "CURRENCIES"). */
    private final String title;
    /** The list of deck entries (pages) contained in this book. */
    private final List<DeckComponent> decks;

    /**
     * Constructs a new book with a title and a list of deck entries.
     *
     * @param title the title of the book
     * @param decks the list of deck components (pages) to include
     */
    public BookComponent(String title, List<DeckComponent> decks) {
        this.title = title;
        this.decks = decks;
    }

    /**
     * Returns the title of the book.
     *
     * @return the book title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the list of deck components contained in the book.
     *
     * @return a list of {@link DeckComponent} entries
     */
    public List<DeckComponent> getDecks() {
        return decks;
    }

    /**
     * A book component containing tower decks loaded from configuration.
     */
    public static class TowerBookComponent extends BookComponent {
        /** The tower configuration loaded from JSON. */
        private static final TowerConfig towerConfig = FileLoader.readClass(TowerConfig.class, "configs/tower.json");

        /** Constructs a tower book component with all towers loaded. */
        public TowerBookComponent() {
            super("TOWERS", createDecks());
        }

        /**
         * Creates a list of tower deck components from the tower configuration.
         *
         * @return a list of {@link DeckComponent} for all towers
         */
        private static List<DeckComponent> createDecks() {
            List<DeckComponent> decks = new ArrayList<>();

            try {
                // Loop through all fields of TowerConfig (boneTower, dinoTower, etc.)
                for (Field field : TowerConfig.class.getDeclaredFields()) {
                    if (!TowerConfig.TowerWrapper.class.isAssignableFrom(field.getType())) continue;

                    TowerConfig.TowerWrapper wrapper = (TowerConfig.TowerWrapper) field.get(towerConfig);
                    TowerConfig.TowerStats baseStats = wrapper.base;

                    String safeName = (wrapper.name == null || wrapper.name.isEmpty()) ? "Unknown" : wrapper.name;
                    decks.add(new TowerBookDeckComponent(
                            safeName,
                            baseStats.damage,
                            baseStats.range,
                            baseStats.cooldown,
                            baseStats.projectileSpeed,
                            baseStats.projectileLife,
                            baseStats.metalScrapCost,
                            baseStats.titaniumCoreCost,
                            baseStats.neurochipCost,
                            wrapper.lore,
                            baseStats.image,
                            wrapper.locked
                    ));
                }
            } catch (IllegalAccessException e) {
                logger.debug("Error with loop through tower config");
            }

            return decks;
        }
    }

    /**
     * A book component containing enemy decks loaded from configuration.
     */
    public static class EnemyBookComponent extends BookComponent {
        /** The enemy configuration loaded from JSON. */
        private static final EnemyConfig enemyConfig = FileLoader.readClass(EnemyConfig.class, "configs/enemy.json");

        /** Constructs an enemy book component with all enemies loaded. */
        public EnemyBookComponent() {
            super("ENEMIES", createDecks());
        }

        /**
         * Creates a list of enemy deck components from the enemy configuration.
         *
         * @return a list of {@link DeckComponent} for all enemies
         */
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

    /**
     * A book component containing currency decks loaded from configuration.
     */
    public static class CurrencyBookComponent extends BookComponent {
        /** The currency configuration loaded from JSON. */
        private static final CurrencyConfig currencyConfig = FileLoader.readClass(CurrencyConfig.class, "configs/currency.json");

        /** Constructs a currency book component with all currencies loaded. */
        public CurrencyBookComponent() {
            super("CURRENCIES", createDecks());
        }

        /**
         * Creates a list of currency deck components from the currency configuration.
         *
         * @return a list of {@link DeckComponent} for all currencies
         */
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

    /**
     * A book component containing achievement decks.
     */
    public static class AchievementBookComponent extends BookComponent {
        /** Constructs an achievement book component with all achievements. */
        public AchievementBookComponent() {
            super("ACHIEVEMENTS", createDecks());
        }

        /**
         * Creates a list of achievement deck components.
         *
         * @return a list of {@link DeckComponent} for all achievements
         */
        private static List<DeckComponent> createDecks() {
            List<DeckComponent> decks = new ArrayList<>();
            
            // Get achievement service
            AchievementService achievementService = ServiceLocator.getAchievementService();
            
            // Achievement definitions with descriptions and images
            addAchievement(decks, achievementService, 
                AchievementService.TOUGH_SURVIVOR,
                "Tough Survivor",
                "Survive with minimal damage and prove your resilience.",
                "images/tough survivor.jpg");
            
            addAchievement(decks, achievementService,
                AchievementService.SPEED_RUNNER,
                "Speed Runner",
                "Complete the level in record time.",
                "images/speed runner.jpg");
            
            addAchievement(decks, achievementService,
                AchievementService.SLAYER,
                "Slayer",
                "Defeat a massive number of enemies.",
                "images/slayer.jpg");
            
            addAchievement(decks, achievementService,
                AchievementService.PERFECT_CLEAR,
                "Perfect Clear",
                "Complete a level without taking any damage.",
                "images/perfect clear.jpg");
            
            addAchievement(decks, achievementService,
                AchievementService.PARTICIPATION,
                "Participation",
                "Join the game and start your journey.",
                "images/participation.jpg");

            return decks;
        }

        /**
         * Helper method to add an achievement deck to the list.
         */
        private static void addAchievement(List<DeckComponent> decks, AchievementService service,
                                          String achievementId, String name, String description, String image) {
            boolean locked = service == null || !service.isUnlocked(achievementId);
            decks.add(new AchievementBookDeckComponent(name, description, image, locked));
        }
    }
}
