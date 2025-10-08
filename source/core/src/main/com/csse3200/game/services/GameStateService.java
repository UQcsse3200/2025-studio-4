package com.csse3200.game.services;

import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that handles persistent state within the game runtime
 * Stores persistent data such as stars, unlocks, selected hero, and level progress
 */
public class GameStateService {
    private static final Logger logger = LoggerFactory.getLogger(GameStateService.class);

    public enum HeroType {
        HERO,
        ENGINEER,
        SAMURAI
    }

    private Entity base;
    private int stars;
    private Map<HeroType, Boolean> heroUnlocks;
    private HeroType selectedHero = HeroType.HERO;

    public GameStateService() {
        // should load from save file later
        logger.info("Loading GameStateService");
        stars = 10;
        heroUnlocks = new HashMap<>();

        heroUnlocks.put(HeroType.HERO, true);
        heroUnlocks.put(HeroType.ENGINEER, false);
        heroUnlocks.put(HeroType.SAMURAI, false);
    }

    /**
     * Sets the current Base entity to track health
     *
     * @param player the base of our current map
     */
    public void setBase(Entity player) {
        this.base = player;
    }

    /**
     * Gets the current number of stars
     *
     * @return current number of stars
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets the current number of stars to the given number
     *
     * @param newStars new number of stars
     */
    public void setStars(int newStars) {
        stars = newStars;
    }

    /**
     * Increments the current number of stars by the given number
     *
     * @param increment the number of stars to increment by
     */
    public void updateStars(int increment) {
        stars += increment;
    }

    /**
     * Attempts to spend the given number of stars.
     *
     * @param amount number of stars to spend
     * @return true if the stars were successfully spent, false if not enough stars
     */
    public boolean spendStars(int amount) {
        if (amount < 0) {
            logger.warn("Tried to spend a negative amount of stars: {}", amount);
            return false;
        }
        if (stars >= amount) {
            stars -= amount;
            logger.info("Spent {} stars. Remaining: {}", amount, stars);
            return true;
        } else {
            logger.info("Not enough stars to spend {}. Current: {}", amount, stars);
            return false;
        }
    }

    /**
     * Rewards stars at the end of a stage based on the player's remaining health.
     * Uses the player/base entity that was set in GameStateService.
     */
    public void rewardStarsOnWin() {
        if (base == null) {
            logger.warn("Base is not set in GameStateService");
            return;
        }
        PlayerCombatStatsComponent stats = base.getComponent(PlayerCombatStatsComponent.class);
        if (stats == null) {
            logger.warn("Base is missing PlayerCombatStatsComponent");
            return;
        }
        double hpPercent = (double) stats.getHealth() / stats.getMaxHealth();
        rewardStarsByHealth(hpPercent);
    }

    /**
     * Rewards stars based on remaining health percentage.
     * 80–100% -> 3★, 50–79% -> 2★, 1–49% -> 1★, 0% -> 0★
     *
     * @param healthPercent a value from 0.0 (dead) to 1.0 (full health)
     */
    public void rewardStarsByHealth(double healthPercent) {
        if (healthPercent < 0) healthPercent = 0;
        if (healthPercent > 1) healthPercent = 1;

        int starsAwarded =
                (healthPercent >= 0.80) ? 3 :
                        (healthPercent >= 0.50) ? 2 :
                                (healthPercent > 0.00) ? 1 : 0;

        updateStars(starsAwarded);
        logger.info("Awarded {} stars for health {}%. Total: {}",
                starsAwarded, Math.round(healthPercent * 100), getStars());
    }

    /**
     * Gets the current unlock flags for the heroes
     *
     * @return map of the current hero unlocks
     */
    public Map<HeroType, Boolean> getHeroUnlocks() {
        return heroUnlocks;
    }

    /**
     * Marks the given hero as unlocked
     *
     * @param hero hero to unlock
     */
    public void setHeroUnlocked(HeroType hero) {
        heroUnlocks.put(hero, true);
    }

    /**
     * Sets the current selected hero
     *
     * @param type the HeroType of the selected hero
     */
    public void setSelectedHero(HeroType type) {
        if (type == null) return;
        selectedHero = type;
        logger.info("Set hero to {}", type);
    }

    /**
     * Gets the current selected hero
     *
     * @return the HeroType of the selected hero
     */
    public HeroType getSelectedHero() {
        return selectedHero;
    }

}
