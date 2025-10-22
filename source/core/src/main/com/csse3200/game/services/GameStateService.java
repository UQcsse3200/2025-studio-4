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
    public enum SkinSlot { BODY, WEAPON /*, SLASH */ }

    private Entity base;
    private int stars;
    private Map<HeroType, Boolean> heroUnlocks;
    private HeroType selectedHero = HeroType.HERO;
    private String currentMapId;  // NEW: Track which map is currently active
    private final java.util.List<java.util.function.BiConsumer<HeroType, String>> skinChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final java.util.List<java.util.function.Consumer<HeroType>> selectedHeroChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    // ===== 新增：武器皮肤 =====
    /** 仅对需要“武器独立皮肤”的英雄建表；目前只做 SAMURAI */
    private final Map<HeroType, String> selectedWeaponSkins = new HashMap<>();
    private final Map<HeroType, String[]> availableWeaponSkins = new HashMap<>();
    private final java.util.List<java.util.function.BiConsumer<HeroType, String>> weaponSkinChangedListeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    // ====== 新增：皮肤状态 ======
    /**
     * 每个英雄当前选择的皮肤 key（如 "default", "crimson", ...）
     */
    private final Map<HeroType, String> selectedSkins = new HashMap<>();

    /**
     * 每个英雄可用皮肤列表（如需换成从 JSON 读取，也能很方便替换）
     */
    private final Map<HeroType, String[]> availableSkins = new HashMap<>();

    public GameStateService() {
        // should load from save file later
        logger.info("Loading GameStateService");
        stars = 0;
        heroUnlocks = new HashMap<>();

        heroUnlocks.put(HeroType.HERO, true);
        heroUnlocks.put(HeroType.ENGINEER, false);
        heroUnlocks.put(HeroType.SAMURAI, false);

        availableSkins.put(HeroType.HERO, new String[]{"default", "purple", "neo"});
        availableSkins.put(HeroType.ENGINEER, new String[]{"default", "khaki", "steel"});
        availableSkins.put(HeroType.SAMURAI, new String[]{"default", "purple", "azure"});

        availableWeaponSkins.put(HeroType.SAMURAI, new String[]{"default", "crimson", "azure"});
        selectedWeaponSkins.put(HeroType.SAMURAI, "default");

        // 默认全部使用 "default"
        for (HeroType ht : HeroType.values()) {
            selectedSkins.put(ht, "default");
        }
    }

    // 可用武器皮肤列表
    public String[] getAvailableWeaponSkins(HeroType hero) {
        String[] arr = availableWeaponSkins.get(hero);
        return (arr != null && arr.length > 0) ? arr : new String[]{"default"};
    }

    // 获取/设置当前武器皮肤
    public String getSelectedWeaponSkin(HeroType hero) {
        return selectedWeaponSkins.getOrDefault(hero, "default");
    }

    public void setSelectedWeaponSkin(HeroType hero, String skinKey) {
        if (hero == null || skinKey == null || skinKey.isBlank()) return;
        String[] allow = availableWeaponSkins.get(hero);
        if (allow != null) {
            boolean ok = false;
            for (String s : allow)
                if (s.equals(skinKey)) {
                    ok = true;
                    break;
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


        /** 订阅：武器皮肤变化 */
        public AutoCloseable onWeaponSkinChanged (java.util.function.BiConsumer < HeroType, String > l){
            weaponSkinChangedListeners.add(l);
            return () -> weaponSkinChangedListeners.remove(l);
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

        // ★ 通知所有监听者
        for (var l : selectedHeroChangedListeners) {
            try { l.accept(selectedHero); } catch (Exception ignore) {}
        }
    }

    /**
     * Gets the current selected hero
     *
     * @return the HeroType of the selected hero
     */
    public HeroType getSelectedHero() {
        return selectedHero;
    }


    // ====== 新增：皮肤 API ======

    /**
     * 返回某个英雄的可用皮肤列表
     */
    public String[] getAvailableSkins(HeroType hero) {
        String[] arr = availableSkins.get(hero);
        return (arr != null && arr.length > 0) ? arr : new String[]{"default"};
        // 如果想保护不可变性，可返回 Arrays.copyOf(arr, arr.length)
    }

    /**
     * 获取某个英雄当前选择的皮肤 key（默认 "default"）
     */
    public String getSelectedSkin(HeroType hero) {
        return selectedSkins.getOrDefault(hero, "default");
    }

    /**
     * 设置某个英雄当前皮肤（UI 下拉调用这里）
     * 可在这里做合法性校验（是否在 availableSkins 里）
     */
    public void setSelectedSkin(HeroType hero, String skinKey) {
        if (hero == null || skinKey == null || skinKey.isBlank()) return;
        // 合法性（如需严格校验）：
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
        logger.info("Set skin for {} -> {}", hero, skinKey);

        // ★ 通知所有监听者
        for (var l : skinChangedListeners) {
            try { l.accept(hero, skinKey); } catch (Exception ignore) {}
        }

    }

    /** 订阅：皮肤变化（返回一个 AutoCloseable，方便在 dispose() 里注销） */
    public AutoCloseable onSkinChanged(java.util.function.BiConsumer<HeroType, String> l) {
        skinChangedListeners.add(l);
        return () -> skinChangedListeners.remove(l);
    }

    /** 订阅：选中英雄变化 */
    public AutoCloseable onSelectedHeroChanged(java.util.function.Consumer<HeroType> l) {
        selectedHeroChangedListeners.add(l);
        return () -> selectedHeroChangedListeners.remove(l);
    }

    /**
     * Sets the current map ID
     * @param mapId the map identifier (e.g., "MapTwo" for level 2, null for level 1)
     */
    public void setCurrentMapId(String mapId) {
        this.currentMapId = mapId;
        logger.info("Current map set to: {}", mapId == null ? "Map 1 (default)" : mapId);
    }

    /**
     * Gets the current map ID
     * @return the current map identifier, or null for default map (Map 1)
     */
    public String getCurrentMapId() {
        return currentMapId;
    }
}
