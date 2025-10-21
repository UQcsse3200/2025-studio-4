package com.csse3200.game.entities.factories;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;

import java.util.HashMap;
import java.util.Map;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.effects.SlowEffectComponent;
import com.csse3200.game.components.npc.EnemySoundComponent;


public class BossEnemyFactory {
    // Default boss configuration
    // IF YOU WANT TO MAKE A NEW ENEMY, THIS IS THE VARIABLE STUFF YOU CHANGE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_HEALTH = 300;
    private static final int DEFAULT_DAMAGE = 20;
    private static final DamageTypeConfig DEFAULT_RESISTANCE = DamageTypeConfig.None;
    private static final DamageTypeConfig DEFAULT_WEAKNESS = DamageTypeConfig.None;
    private static final Vector2 DEFAULT_SPEED = new Vector2(0.5f, 0.5f);
    private static final String DEFAULT_TEXTURE = "images/boss_enemy.png";
    private static final String DEFAULT_NAME = "Boss Enemy";
    private static final String BOSS_WALK_SOUND_1 = "sounds/Enemy Sounds/boss/Boss_Walk_1.wav";
    private static final String BOSS_WALK_SOUND_2 = "sounds/Enemy Sounds/boss/Boss_Walk_2.wav";
    private static final String BOSS_ATTACK_SOUND = "sounds/Enemy Sounds/boss/Boss_Attack.mp3";
    private static final String BOSS_DEATH_SOUND = "sounds/Enemy Sounds/boss/Boss_Death.wav";
    private static final String BOSS_AMBIENT_SOUND = "sounds/Enemy Sounds/boss/Boss_Random_Noise.mp3";
    private static final String BOSS_THEME = "sounds/Enemy Sounds/boss/Boss Music.mp3";
private static final float BOSS_WALK_1_DURATION = 0.5f; // Duration before second walk sound plays
    private static final float DEFAULT_CLICKRADIUS = 1.2f;
    private static final Map<CurrencyType, Integer> DEFAULT_CURRENCY_DROPS = Map.of(
    CurrencyType.METAL_SCRAP, 500,
    CurrencyType.TITANIUM_CORE, 300,
    CurrencyType.NEUROCHIP, 150
    );
    private static final int DEFAULT_POINTS = 600;
    private static final float SPEED_EPSILON = 0.001f;
    private static final String DEFAULT_DEATH_SOUND_PATH = "sounds/mixkit-arcade-game-explosion-2759.wav";
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Configurable properties
    private static int health = DEFAULT_HEALTH;
    private static int damage = DEFAULT_DAMAGE;
    private static DamageTypeConfig resistance = DEFAULT_RESISTANCE;
    private static DamageTypeConfig weakness = DEFAULT_WEAKNESS;
    private static Vector2 speed = new Vector2(DEFAULT_SPEED);
    private static String texturePath = DEFAULT_TEXTURE;
    private static String displayName = DEFAULT_NAME;
    private static float clickRadius = DEFAULT_CLICKRADIUS;
    private static Map<CurrencyType, Integer> currencyDrops = new HashMap<>(DEFAULT_CURRENCY_DROPS);
    private static int points = DEFAULT_POINTS;
    private static String deathSoundPath = DEFAULT_DEATH_SOUND_PATH;


    /**
     * Creates a boss enemy with current configuration.
     *
     * @param waypoints List of waypoint entities for the boss to follow
     * @param player Reference to the player entity
     * @return entity
     */
    public static Entity createBossEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty) {
        return createBossEnemy(waypoints, player, difficulty, 0);
    }

    /** Overload: start from specific waypoint index (for save/load resume). */
    public static Entity createBossEnemy(java.util.List<Entity> waypoints, Entity player, Difficulty difficulty, int startWaypointIndex) {
        int idx = Math.max(0, Math.min(waypoints.size() - 1, startWaypointIndex));
        Entity boss = EnemyFactory.createBaseEnemyAnimated(waypoints.get(idx), new Vector2(speed), waypoints,
        "images/boss_basic_spritesheet.atlas", 0.5f, 0.18f, idx);

        // Add waypoint component for independent waypoint tracking
        WaypointComponent waypointComponent = new WaypointComponent(waypoints, player, speed);
        waypointComponent.setCurrentWaypointIndex(idx);
        waypointComponent.setCurrentTarget(waypoints.get(idx));
        boss.addComponent(waypointComponent);
        applySpeedModifier(boss, waypointComponent, waypoints.get(idx));

        EnemySoundComponent bossSoundComponent = new EnemySoundComponent(
            ServiceLocator.getResourceService().getAsset(BOSS_WALK_SOUND_1, Sound.class),
            ServiceLocator.getResourceService().getAsset(BOSS_ATTACK_SOUND, Sound.class),
            ServiceLocator.getResourceService().getAsset(BOSS_DEATH_SOUND, Sound.class),
            ServiceLocator.getResourceService().getAsset(BOSS_AMBIENT_SOUND, Sound.class),
            ServiceLocator.getResourceService().getAsset(BOSS_THEME, Sound.class) // Boss music!
        );

        // Set the second walk sound
        bossSoundComponent.setSecondWalkSound(
            ServiceLocator.getResourceService().getAsset(BOSS_WALK_SOUND_2, Sound.class),
            BOSS_WALK_1_DURATION
        );

        boss
                .addComponent(new CombatStatsComponent(health * difficulty.getMultiplier(), damage * difficulty.getMultiplier(), resistance, weakness))
                .addComponent(new com.csse3200.game.components.enemy.EnemyTypeComponent("boss"))
                .addComponent(new DeckComponent.EnemyDeckComponent(DEFAULT_NAME, DEFAULT_HEALTH, DEFAULT_DAMAGE, DEFAULT_RESISTANCE, DEFAULT_WEAKNESS, DEFAULT_TEXTURE))
                .addComponent(new clickable(clickRadius))
                .addComponent(new com.csse3200.game.components.ReachedBaseComponent())
                .addComponent(bossSoundComponent);
                CombatStatsComponent combatStats = boss.getComponent(CombatStatsComponent.class);
                if (combatStats != null) combatStats.setIsEnemy(true);


        boss.getEvents().addListener("entityDeath", () -> destroyEnemy(boss));

        // Each boss handles its own waypoint progression
        boss.getEvents().addListener("chaseTaskFinished", () -> {
            WaypointComponent dwc = boss.getComponent(WaypointComponent.class);
            
            if (dwc == null) {
                return;
            }
            
            Entity currentTarget = dwc.getCurrentTarget();
            
            // Check if we've reached the final waypoint
            if (!dwc.hasMoreWaypoints()) {
                
                // If we're far from the final waypoint, keep chasing it
                if (currentTarget != null) {
                    float distanceToTarget = boss.getPosition().dst(currentTarget.getPosition());
                    
                    if (distanceToTarget > 0.5f) {
                        updateChaseTarget(boss, currentTarget);
                        return;
                    }
                }
                
                return;
            }
            
            if (currentTarget != null) {
                float distanceToTarget = boss.getPosition().dst(currentTarget.getPosition());
                
                // If we're far from current waypoint (happens after unpause), 
                // create a new task to continue toward CURRENT waypoint
                if (distanceToTarget > 0.5f) {
                    updateChaseTarget(boss, currentTarget);
                    return;
                }
            }
            
            // We're close to current waypoint, advance to next
            Entity nextTarget = dwc.getNextWaypoint();
            if (nextTarget != null) {
                applySpeedModifier(boss, dwc, nextTarget);
                updateChaseTarget(boss, nextTarget);
            }
        });

        // Set custom boss size
        var sz = boss.getScale(); 
        boss.setScale(sz.x * 1.8f, sz.y * 1.8f);

        return boss;
    }

    private static void destroyEnemy(Entity entity) {
        // Check which game area is active and use its counters
        if (com.csse3200.game.areas2.MapTwo.ForestGameArea2.currentGameArea != null) {
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.NUM_ENEMIES_DEFEATED += 1;
            com.csse3200.game.areas2.MapTwo.ForestGameArea2.checkEnemyCount();
        } else {
            ForestGameArea.NUM_ENEMIES_DEFEATED += 1;
            ForestGameArea.checkEnemyCount();
        }

        // Check if enemy reached the base
        com.csse3200.game.components.ReachedBaseComponent reachedBaseComp = 
            entity.getComponent(com.csse3200.game.components.ReachedBaseComponent.class);
        boolean reachedBase = (reachedBaseComp != null && reachedBaseComp.hasReachedBase());
        
        //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " died. ReachedBase: " + reachedBase);
        
        if (!reachedBase) {
            //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " - DROPPING CURRENCY (killed by player)");
            
            WaypointComponent wc = entity.getComponent(WaypointComponent.class);
            if (wc != null && wc.getPlayerRef() != null) {
                Entity player = wc.getPlayerRef();

                // Drop currency upon defeat
                CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
                if (currencyManager != null) {
                    player.getEvents().trigger("dropCurrency", currencyDrops);
                }

                // Add points to score
                PlayerScoreComponent psc = player.getComponent(PlayerScoreComponent.class);
                if (psc != null) {
                    psc.addPoints(points);
                }

                // Track kill for ranking component
                com.csse3200.game.components.PlayerRankingComponent prc = player.getComponent(com.csse3200.game.components.PlayerRankingComponent.class);
                if (prc != null) {
                    prc.addKill();
                }
            }
        } else {
            //Gdx.app.log("CURRENCY", "Enemy " + entity.getId() + " - NO CURRENCY (reached base)");
        }

        playDeathSound(deathSoundPath);
    }

    private static void playDeathSound(String soundPath) {
        ServiceLocator.getResourceService()
                .getAsset(soundPath, Sound.class)
                .play(2.0f);
    }

    private static void applySpeedModifier(Entity boss, WaypointComponent waypointComponent, Entity waypoint) {
        if (waypointComponent == null || waypoint == null) {
            return;
        }

        SpeedWaypointComponent speedMarker = waypoint.getComponent(SpeedWaypointComponent.class);
        Vector2 desiredSpeed = waypointComponent.getBaseSpeed();
        if (speedMarker != null) {
            float multiplier = speedMarker.getSpeedMultiplier();
            desiredSpeed.scl(multiplier);
            
            // 如果是减速区域（倍率小于1.0），触发蓝色减速特效
            if (multiplier < 1.0f) {
                boss.getEvents().trigger("applySlow");
            } else {
                // 如果是加速区域或正常区域，移除减速特效
                boss.getEvents().trigger("removeSlow");
            }
        } else {
            // 如果没有速度修改器，移除减速特效
            boss.getEvents().trigger("removeSlow");
        }

        if (!waypointComponent.getSpeed().epsilonEquals(desiredSpeed, SPEED_EPSILON)) {
            updateSpeed(boss, desiredSpeed);
        }
    }

    private static void updateSpeed(Entity boss, Vector2 newSpeed) {
        WaypointComponent wc = boss.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setSpeed(newSpeed);
            boss.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(wc.getCurrentTarget(), wc.getPriorityTaskCount(), 100f, 100f, newSpeed));
        }
    }

    private static void updateChaseTarget(Entity boss, Entity newTarget) {
        WaypointComponent wc = boss.getComponent(WaypointComponent.class);
        if (wc != null) {
            wc.incrementPriorityTaskCount();
            wc.setCurrentTarget(newTarget);
            boss.getComponent(AITaskComponent.class).addTask(
                new ChaseTask(newTarget, wc.getPriorityTaskCount(), 100f, 100f, wc.getSpeed()));
        }
    }

    // Getters
    public static DamageTypeConfig getResistance() {
        return resistance;
    }

    public static DamageTypeConfig getWeakness() {
        return weakness;
    }

    public static Vector2 getSpeed() {
        return new Vector2(speed); // Return copy to prevent external modification
    }

    public static String getTexturePath() {
        return texturePath;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static int getPoints() {
        return points;
    }

    // Setters
    public static void setResistance(DamageTypeConfig resistance) {
        BossEnemyFactory.resistance = (resistance != null) ? resistance : DEFAULT_RESISTANCE;
    }

    public static void setWeakness(DamageTypeConfig weakness) {
        BossEnemyFactory.weakness = (weakness != null) ? weakness : DEFAULT_WEAKNESS;
    }

    public static void setSpeed(Vector2 speed) {
        if (speed != null) {
            BossEnemyFactory.speed.set(speed);
        }
    }

    public static void setSpeed(float x, float y) {
        BossEnemyFactory.speed.set(x, y);
    }

    public static void setTexturePath(String texturePath) {
        BossEnemyFactory.texturePath = (texturePath != null && !texturePath.trim().isEmpty())
                ? texturePath : DEFAULT_TEXTURE;
    }

    public static void setDisplayName(String displayName) {
        BossEnemyFactory.displayName = (displayName != null && !displayName.trim().isEmpty())
                ? displayName : DEFAULT_NAME;
    }

    /**
     * Resets all boss configuration to default values.
     */
    public static void resetToDefaults() {
        health = DEFAULT_HEALTH;
        damage = DEFAULT_DAMAGE;
        resistance = DEFAULT_RESISTANCE;
        weakness = DEFAULT_WEAKNESS;
        speed.set(DEFAULT_SPEED);
        texturePath = DEFAULT_TEXTURE;
        displayName = DEFAULT_NAME;
        points = DEFAULT_POINTS;
    }

    private BossEnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}

