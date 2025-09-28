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
            super("TOWER", createDecks());
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
            super("ENEMY", createDecks());
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
                        enemy.image
                );
                decks.add(deck);
            }

            return decks;
        }
    }

    public static class CurrencyBookComponent extends BookComponent {
        private static final CurrencyConfig currencyConfig = FileLoader.readClass(CurrencyConfig.class, "configs/currency.json");

        public CurrencyBookComponent() {
            super("CURRENCY", createDecks());
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

    private String[] currencyBackGround = {
            "images/currency/metal_scrap.png",
            "images/currency/neurochip.png",
            "images/currency/titanium_core.png",
    };

    private String[] currencyTitle = {
            "Metal scrap",
            "Neurochip",
            "Titanium"
    };

    private String[] currencyData = {
            "A basic and plentiful resource, scavenged from the battlefield. Useful for early upgrades and essential constructions.",
            "A highly advanced implant once designed to enhance human intelligence, now repurposed by AI to control and outsmart its creators. Extremely rare and vital for high-level upgrades",
            "A rare and valuable resource, often obtained from tougher enemies. Needed for powerful upgrades and advanced technology."
    };

    private String[] enemyBackGround = {
            "images/grunt_enemy.png",
            "images/tank_enemy.png",
            "images/drone_enemy.png",
            "images/boss_enemy.png",
            "images/divider_enemy.png"
    };

    private String[] enemyTitle = {
            "Grunt enemy",
            "Tank enemy",
            "Drone enemy",
            "Boss enemy",
            "Divider enemy"
    };

    private String gruntInfo =
            "The grunt is the most common enemy unit in the cyberpunk wastelands. Medium weight and balanced, "
                    + "they lack any spectacular traits, but their sheer numbers make them a constant threat. "
                    + "Do not underestimate them—when ignored, they can swarm defences and drain your resources.\n\n"
                    + "+ Health: Moderate\n"
                    + "+ Damage: Low\n"
                    + "+ Speed: Medium\n"
                    + "+ Special Traits: None\n"
                    + "+ Tactical Role: Fodder unit, designed to soak up early firepower and overwhelm with numbers.";

    private String tankInfo =
            "The tank is a slow-moving heavyweight unit, armoured to withstand enormous amounts of punishment. "
                    + "While sluggish, its heavy cannon can devastate defences if it gets into range. "
                    + "Defeating a tank often requires concentrated firepower or high-damage towers.\n\n"
                    + "+ Health: Very High\n"
                    + "+ Damage: High\n"
                    + "+ Speed: Very Slow\n"
                    + "+ Special Traits: Resistant to small-arms fire, weak to armour-piercing or explosive weapons.\n"
                    + "+ Tactical Role: Siege unit, designed to break strongholds and soak tower fire.";

    private String droneInfo =
            "The drone is a lightweight, fast-moving aerial enemy. Agile and difficult to hit, "
                    + "drones dart across the battlefield, bypassing ground-based obstacles and exploiting weak anti-air defences. "
                    + "While fragile, their speed makes them a dangerous distraction.\n\n"
                    + "+ Health: Low\n"
                    + "+ Damage: Low to Medium\n"
                    + "+ Speed: Very Fast\n"
                    + "+ Special Traits: Flying unit, ignores ground traps and walls.\n"
                    + "+ Tactical Role: Harassment unit, designed to force the player to build anti-air towers and split defences.";

    private String bossBotInfo =
            "The boss bot is a huge mechanical beast, capable of inflicting severe damage in short bursts. "
                    + "Its armour provides a significant health boost, and upon defeat the bot splits into three smaller grunt enemies "
                    + "(to be implemented...). Be sure to target this enemy early, defeating it near the base might let grunts through!\n\n"
                    + "+ Health: Very High\n"
                    + "+ Damage: Very High\n"
                    + "+ Speed: Very Slow\n"
                    + "+ Special Traits: Weak to armour-piercing or explosive weapons. Splits into three grunt enemies when defeated.\n"
                    + "+ Tactical Role: Designed to be a primary target that allows faster, weaker enemies to pass.";

    private String dividerInfo =
            "The Divider enemy is a formidable foe, with its tankier than usual health and slightly lower damage "
                    + "and speed it might seem like an easy target, but don't be fooled. Once killed, the Divider releases "
                    + "its children Dividers who are faster in speed and pack more damage. One mistake and this one will surely "
                    + "pass all your defences.\n\n"
                    + "+ Health: Very High, then Very Low\n"
                    + "+ Damage: Very Low, then Very High\n"
                    + "+ Speed: Very Slow, then Very Fast\n"
                    + "+ Special Traits: Spawns little children Dividers upon death.\n"
                    + "+ Tactical Role: Designed to be able to catch people off guard who focus too much on weaker enemies.";


    private String[] enemyData = {
            gruntInfo,
            tankInfo,
            droneInfo,
            bossBotInfo,
            dividerInfo
    };

    private String[] towerBackGround = {
            "images/bone.png",
            "images/dino.png",
            "images/cavemen.png"
    };

    private String[] towerTitle = {
            "Bone tower",
            "Dino tower",
            "Cavemen tower"
    };

    private String boneTowerInfo =
            "The Bone Tower is a sturdy, reliable defender made from ancient fossilized bones. "
                    + "It may not be the strongest, but its steady damage and decent range make it perfect for holding "
                    + "choke points and supporting other towers. Its projectiles are jagged bones hurled with deadly "
                    + "precision at approaching enemies.\n\n"
                    + "+ Damage: 5\n"
                    + "+ Range: 3.0 units\n"
                    + "+ Cooldown: 1.0 seconds\n"
                    + "+ Projectile Speed: 2.0 units/sec\n"
                    + "+ Projectile Life: range / speed\n"
                    + "+ Metal Scrap Cost: 500\n"
                    + "+ Role & Traits: Balanced all-rounder; dependable early-game tower.\n"
                    + "+ Lore: Crafted by ancient tribes, Bone Towers channel the primal energy of the land. "
                    + "Their flying bone projectiles carry the spirits of long-dead guardians, sending a chilling "
                    + "warning to any who dare approach.";

    private String dinoTowerInfo =
            "The Dino Tower channels the raw power of prehistoric predators. "
                    + "Its short range forces it to fight up close, but its fiery projectiles can take down even the toughest enemies in seconds. "
                    + "This tower is perfect for high-risk, high-reward positions where enemies cluster together.\n\n"
                    + "+ Damage: 8\n"
                    + "+ Range: 2.0 units\n"
                    + "+ Cooldown: 0.8 seconds\n"
                    + "+ Projectile Speed: 2.0 units/sec\n"
                    + "+ Projectile Life: range / speed\n"
                    + "+ Metal Scrap Cost: 1000\n"
                    + "+ Role & Traits: Close-range powerhouse; ideal for taking down tough enemies quickly.\n"
                    + "+ Lore: Forged from the bones and scales of ancient beasts, Dino Towers launch fireballs that scorch everything in their path. "
                    + "Their ferocious roars echo across the battlefield, intimidating foes before they even reach your defenses.";

    private String cavemenTowerInfo =
            "The Cavemen Tower is a long-range sniper manned by resourceful hunters. "
                    + "Though its damage is modest, it picks off enemies from a distance using firearms scavenged from fallen AI bots. "
                    + "It excels at softening enemies before they reach the front lines.\n\n"
                    + "+ Damage: 3\n"
                    + "+ Range: 5.0 units\n"
                    + "+ Cooldown: 0.8 seconds\n"
                    + "+ Projectile Speed: 2.0 units/sec\n"
                    + "+ Projectile Life: range / speed\n"
                    + "+ Metal Scrap Cost: 750\n"
                    + "+ Role & Traits: Sniper/support; excellent for controlling wide areas and picking off enemies early.\n"
                    + "+ Lore: Cavemen Towers are staffed by expert marksmen armed with guns salvaged from defeated AI bots. "
                    + "With sharp eyes and steady hands, they make sure nothing slips through their sights.";


    private String[] towerData = {
            boneTowerInfo,
            dinoTowerInfo,
            cavemenTowerInfo
    };

    public String[] getCurrencyBackGround() {
        return currencyBackGround;
    }

    public String[] getCurrencyTitle() {
        return currencyTitle;
    }

    public String[] getCurrencyData() {
        return currencyData;
    }

    public String[] getEnemyBackGround() {
        return enemyBackGround;
    }

    public String[] getEnemyTitle() {
        return enemyTitle;
    }

    public String[] getEnemyData() {
        return enemyData;
    }

    public String[] getTowerTitle() {
        return towerTitle;
    }

    public String[] getTowerBackGround() {
        return towerBackGround;
    }

    public String[] getTowerData() {
        return towerData;
    }
}
