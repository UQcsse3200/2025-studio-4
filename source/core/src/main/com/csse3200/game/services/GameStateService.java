package com.csse3200.game.services;

import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Central runtime game state holder.
 * <p>
 * Persists session-scoped data such as:
 * <ul>
 *   <li>Star currency</li>
 *   <li>Hero unlock flags</li>
 *   <li>Currently selected hero</li>
 *   <li>Per-hero appearance (body skin and — where applicable — weapon skin)</li>
 *   <li>Current map identifier</li>
 * </ul>
 * <p>
 * This service is not responsible for saving to disk; it only keeps state while the game runs.
 */
public class GameStateService {
    private static final Logger logger = LoggerFactory.getLogger(GameStateService.class);

    /**
     * All playable hero archetypes available in the game.
     */
    public enum HeroType {
        HERO,
        ENGINEER,
        SAMURAI
    }

    /**
     * Skin slots supported by the appearance system.
     * BODY applies to all heroes; WEAPON is for heroes with separate weapon skins (e.g., SAMURAI).
     */
    public enum SkinSlot {BODY, WEAPON /*, SLASH*/}

    /**
     * Player/base entity used to evaluate win rewards.
     */
    private Entity base;

    /**
     * Current amount of star currency.
     */
    private int stars;

    /**
     * Unlock flags per hero type.
     */
    private Map<HeroType, Boolean> heroUnlocks;

    /**
     * Currently selected hero in menus / for spawning.
     */
    private HeroType selectedHero = HeroType.HERO;

    /**
     * Identifier of the currently active map (null implies default map, e.g., Map 1).
     */
    private String currentMapId;

    // --- Appearance events (BODY skins) ---
    /**
     * Listeners notified when a hero's BODY skin changes: (hero, skinKey).
     */
    private final java.util.List<java.util.function.BiConsumer<HeroType, String>> skinChangedListeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Listeners notified when the selected hero changes: (hero).
     */
    private final java.util.List<java.util.function.Consumer<HeroType>> selectedHeroChangedListeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    // --- Weapon skin system (per-hero; currently only SAMURAI supports weapon skins) ---
    /**
     * Current WEAPON skin per hero (only populated for heroes that support weapon skins).
     */
    private final Map<HeroType, String> selectedWeaponSkins = new HashMap<>();

    /**
     * Allowed WEAPON skins per hero.
     */
    private final Map<HeroType, String[]> availableWeaponSkins = new HashMap<>();

    /**
     * Listeners notified when a hero's WEAPON skin changes: (hero, weaponSkinKey).
     */
    private final java.util.List<java.util.function.BiConsumer<HeroType, String>> weaponSkinChangedListeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    // --- BODY skin system ---
    /**
     * Current BODY skin per hero (e.g., "default", "purple", ...).
     */
    private final Map<HeroType, String> selectedSkins = new HashMap<>();

    /**
     * Allowed BODY skins per hero.
     */
    private final Map<HeroType, String[]> availableSkins = new HashMap<>();
    private volatile boolean readyPromptFinished = false;

    /**
     * Constructs the runtime game state with default values.
     * <ul>
     *   <li>Starts with 10 stars.</li>
     *   <li>HERO unlocked; ENGINEER and SAMURAI locked by default.</li>
     *   <li>Default available skins registered per hero.</li>
     *   <li>SAMURAI has separate weapon skin support.</li>
     * </ul>
     */
    public GameStateService() {
        logger.info("Loading GameStateService");

        stars = 0;
        heroUnlocks = new HashMap<>();
        heroUnlocks.put(HeroType.HERO, true);
        heroUnlocks.put(HeroType.ENGINEER, false);
        heroUnlocks.put(HeroType.SAMURAI, false);

        // BODY skins
        availableSkins.put(HeroType.HERO, new String[]{"default", "purple", "neo"});
        availableSkins.put(HeroType.ENGINEER, new String[]{"default", "khaki", "steel"});
        availableSkins.put(HeroType.SAMURAI, new String[]{"default", "purple", "azure"});

        // WEAPON skins (only SAMURAI for now)
        availableWeaponSkins.put(HeroType.SAMURAI, new String[]{"default", "crimson", "azure"});
        selectedWeaponSkins.put(HeroType.SAMURAI, "default");

        // Initialize BODY skins to "default"
        for (HeroType ht : HeroType.values()) {
            selectedSkins.put(ht, "default");
        }
    }

    // -------------------------------
    // Weapon skin API (per-hero)
    // -------------------------------

    /**
     * Returns the available WEAPON skins for the given hero.
     * If the hero does not define a list, returns a single-item array {"default"}.
     */
    public String[] getAvailableWeaponSkins(HeroType hero) {
        String[] arr = availableWeaponSkins.get(hero);
        return (arr != null && arr.length > 0) ? arr : new String[]{"default"};
    }

    /**
     * Returns the currently selected WEAPON skin for the given hero.
     * If not set, returns "default".
     */
    public String getSelectedWeaponSkin(HeroType hero) {
        return selectedWeaponSkins.getOrDefault(hero, "default");
    }

    /**
     * Sets the WEAPON skin for the given hero, validating against the allowed list if present.
     * Notifies weapon skin listeners on success.
     *
     * @param hero    target hero
     * @param skinKey weapon skin key to apply (e.g., "default", "crimson")
     */
    public void setSelectedWeaponSkin(HeroType hero, String skinKey) {
        if (hero == null || skinKey == null || skinKey.isBlank()) return;

        String[] allow = availableWeaponSkins.get(hero);
        if (allow != null) {
            boolean ok = false;
            for (String s : allow) {
                if (s.equals(skinKey)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                logger.warn("Weapon skin '{}' is not in available list for {}. Ignored.", skinKey, hero);
                return;
            }
        }

        selectedWeaponSkins.put(hero, skinKey);
        logger.info("Set WEAPON skin for {} -> {}", hero, skinKey);

        for (var l : weaponSkinChangedListeners) {
            try {
                l.accept(hero, skinKey);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Subscribes to WEAPON skin changes.
     * The returned {@link AutoCloseable} removes the subscription on close/dispose.
     */
    public AutoCloseable onWeaponSkinChanged(java.util.function.BiConsumer<HeroType, String> l) {
        weaponSkinChangedListeners.add(l);
        return () -> weaponSkinChangedListeners.remove(l);
    }

    // -------------------------------
    // Base / stars
    // -------------------------------

    /**
     * Sets the current base (player base) entity.
     * This is used to compute end-of-level star rewards based on remaining HP.
     *
     * @param player the base entity of the current map
     */
    public void setBase(Entity player) {
        this.base = player;
    }

    /**
     * Returns the current star balance.
     */
    public int getStars() {
        return stars;
    }

    /**
     * Overwrites the current star balance.
     *
     * @param newStars new star amount
     */
    public void setStars(int newStars) {
        stars = newStars;
    }

    /**
     * Adjusts stars by the given delta (positive or negative).
     *
     * @param increment amount to add (can be negative if you want to subtract)
     */
    public void updateStars(int increment) {
        stars += increment;
    }

    /**
     * Attempts to spend the given amount of stars.
     *
     * @param amount stars to spend (must be non-negative)
     * @return {@code true} if the spend succeeded; {@code false} if insufficient funds or invalid amount
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
     * Rewards stars at the end of a stage based on the base's remaining health.
     * <p>
     * Requires {@link #setBase(Entity)} to have been called earlier.
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
     * Rewards stars based on remaining health percentage:
     * <pre>
     *   80–100% -> 3 stars
     *   50–79%  -> 2 stars
     *   1–49%   -> 1 star
     *   0%      -> 0 star
     * </pre>
     *
     * @param healthPercent value in [0, 1]; out-of-range inputs are clamped
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

    // -------------------------------
    // Hero unlocks / selection
    // -------------------------------

    /**
     * Returns the current unlock flags for all heroes.
     */
    public Map<HeroType, Boolean> getHeroUnlocks() {
        return heroUnlocks;
    }

    /**
     * Marks a hero as unlocked.
     */
    public void setHeroUnlocked(HeroType hero) {
        heroUnlocks.put(hero, true);
    }

    /**
     * Sets a hero's unlock state.
     *
     * @param hero     the hero
     * @param unlocked {@code true} to unlock, {@code false} to lock
     */
    public void setHeroUnlocked(HeroType hero, boolean unlocked) {
        heroUnlocks.put(hero, unlocked);
    }

    /**
     * Sets the currently selected hero and notifies subscribers.
     *
     * @param type hero to select
     */
    public void setSelectedHero(HeroType type) {
        if (type == null) return;
        selectedHero = type;
        logger.info("Set hero to {}", type);

        for (var l : selectedHeroChangedListeners) {
            try {
                l.accept(selectedHero);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Returns the currently selected hero.
     */
    public HeroType getSelectedHero() {
        return selectedHero;
    }

    /**
     * Subscribes to selected-hero change events.
     */
    public AutoCloseable onSelectedHeroChanged(java.util.function.Consumer<HeroType> l) {
        selectedHeroChangedListeners.add(l);
        return () -> selectedHeroChangedListeners.remove(l);
    }

    // -------------------------------
    // BODY skin API (per-hero)
    // -------------------------------

    /**
     * Returns the available BODY skins for the given hero.
     * If none registered, returns {"default"}.
     */
    public String[] getAvailableSkins(HeroType hero) {
        String[] arr = availableSkins.get(hero);
        return (arr != null && arr.length > 0) ? arr : new String[]{"default"};
        // To enforce immutability, consider returning a copy.
        // return Arrays.copyOf(arr, arr.length);
    }

    /**
     * Returns the currently selected BODY skin for the given hero.
     * Defaults to "default" if none is set.
     */
    public String getSelectedSkin(HeroType hero) {
        return selectedSkins.getOrDefault(hero, "default");
    }

    /**
     * Sets the BODY skin for the given hero, validating against the allowed list if present.
     * Notifies skin change listeners on success.
     *
     * @param hero    target hero
     * @param skinKey body skin key to apply (e.g., "default", "purple")
     */
    public void setSelectedSkin(HeroType hero, String skinKey) {
        if (hero == null || skinKey == null || skinKey.isBlank()) return;

        String[] allow = availableSkins.get(hero);
        if (allow != null) {
            boolean ok = false;
            for (String s : allow) {
                if (s.equals(skinKey)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                logger.warn("Skin '{}' is not in available list for {}. Ignored.", skinKey, hero);
                return;
            }
        }

        selectedSkins.put(hero, skinKey);
        logger.info("Set BODY skin for {} -> {}", hero, skinKey);

        for (var l : skinChangedListeners) {
            try {
                l.accept(hero, skinKey);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Subscribes to BODY skin changes.
     */
    public AutoCloseable onSkinChanged(java.util.function.BiConsumer<HeroType, String> l) {
        skinChangedListeners.add(l);
        return () -> skinChangedListeners.remove(l);
    }

    // -------------------------------
    // Map tracking
    // -------------------------------

    public void resetReadyPromptFinished() {
        readyPromptFinished = false;
    }

    public void markReadyPromptFinished() {
        readyPromptFinished = true;
    }

    public boolean isReadyPromptFinished() {
        return readyPromptFinished;
    }
    /**
     * Sets the identifier of the current map.
     * Example: "MapTwo" for level 2; {@code null} can represent the default map (level 1).
     */
    public void setCurrentMapId(String mapId) {
        this.currentMapId = mapId;
        logger.info("Current map set to: {}", mapId == null ? "Map 1 (default)" : mapId);
    }

    /**
     * Returns the current map identifier, or {@code null} for the default map.
     */
    public String getCurrentMapId() {
        return currentMapId;
    }
}

