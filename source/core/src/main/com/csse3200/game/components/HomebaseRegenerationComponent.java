package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homebase生命回复组件
 * 当homebase在5秒内没有受到伤害时，每4秒回复5点生命值，并在周围显示回复数字
 */
public class HomebaseRegenerationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseRegenerationComponent.class);
    
    // 配置常量
    private static final float TIME_WITHOUT_DAMAGE_REQUIRED = 3.0f; // 需要多少秒不受伤才能开始回复
    private static final float REGENERATION_INTERVAL = 4.0f; // 每隔多少秒回复一次
    private static final int REGENERATION_AMOUNT = 5; // 每次回复的生命值
    
    // 状态追踪
    private long lastDamageTime; // 最后一次受伤的时间（毫秒）
    private long lastRegenerationTime; // 最后一次回复的时间（毫秒）
    private int previousHealth = -1; // 上一次的生命值，用于检测受伤
    private boolean isRegenerating = false; // 当前是否处于回复状态
    
    private GameTime gameTime;
    private PlayerCombatStatsComponent combatStats;
    
    @Override
    public void create() {
        super.create();
        
        // 获取游戏时间服务
        gameTime = ServiceLocator.getTimeSource();
        if (gameTime == null) {
            logger.error("GameTime service not available");
            return;
        }
        
        // 获取战斗状态组件
        combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HomebaseRegenerationComponent requires PlayerCombatStatsComponent");
            return;
        }
        
        // 初始化时间
        lastDamageTime = gameTime.getTime();
        lastRegenerationTime = gameTime.getTime();
        previousHealth = combatStats.getHealth();
        
        // 监听生命值变化事件
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        logger.info("HomebaseRegenerationComponent created - will regenerate {} HP every {} seconds after {} seconds without damage",
                REGENERATION_AMOUNT, REGENERATION_INTERVAL, TIME_WITHOUT_DAMAGE_REQUIRED);
    }
    
    /**
     * 当生命值更新时触发，用于检测是否受到伤害
     */
    private void onHealthUpdate(int newHealth) {
        // 检查是否受到伤害（生命值减少）
        if (previousHealth > 0 && newHealth < previousHealth) {
            // 受到伤害，重置最后受伤时间
            lastDamageTime = gameTime.getTime();
            isRegenerating = false;
            logger.debug("Homebase took damage. Regeneration paused.");
        }
        
        // 更新previousHealth
        previousHealth = newHealth;
    }
    
    @Override
    public void update() {
        if (combatStats == null || gameTime == null) {
            return;
        }
        
        long currentTime = gameTime.getTime();
        
        // 计算距离最后一次受伤的时间（秒）
        float timeSinceLastDamage = (currentTime - lastDamageTime) / 1000.0f;
        
        // 检查是否满足开始回复的条件
        if (timeSinceLastDamage >= TIME_WITHOUT_DAMAGE_REQUIRED) {
            if (!isRegenerating) {
                isRegenerating = true;
                lastRegenerationTime = currentTime;
                logger.debug("Homebase regeneration started");
            }
            
            // 计算距离最后一次回复的时间（秒）
            float timeSinceLastRegen = (currentTime - lastRegenerationTime) / 1000.0f;
            
            // 检查是否到了回复时间
            if (timeSinceLastRegen >= REGENERATION_INTERVAL) {
                // 执行回复
                regenerateHealth();
                lastRegenerationTime = currentTime;
            }
        } else {
            // 还没有达到开始回复的条件
            if (isRegenerating) {
                isRegenerating = false;
                logger.debug("Homebase regeneration stopped due to recent damage");
            }
        }
    }
    
    /**
     * 执行生命回复
     */
    private void regenerateHealth() {
        if (combatStats == null) {
            return;
        }
        
        int currentHealth = combatStats.getHealth();
        int maxHealth = combatStats.getMaxHealth();
        
        // 如果已经满血，不回复
        if (currentHealth >= maxHealth) {
            logger.debug("Homebase already at full health, no regeneration needed");
            return;
        }
        
        // 计算实际回复量（不超过最大生命值）
        int actualRegenAmount = Math.min(REGENERATION_AMOUNT, maxHealth - currentHealth);
        
        // 回复生命
        combatStats.addHealth(actualRegenAmount);
        
        // 显示回复数字（使用负数表示回复，绿色显示）
        showRegenerationNumber(actualRegenAmount);
        
        logger.info("Homebase regenerated {} HP (current: {}/{})", 
                actualRegenAmount, combatStats.getHealth(), maxHealth);
    }
    
    /**
     * 显示回复数字
     * @param regenAmount 回复的数量
     */
    private void showRegenerationNumber(int regenAmount) {
        try {
            // 触发显示事件，使用负数表示回复（在DamagePopupComponent中会显示为绿色）
            entity.getEvents().trigger("showDamage", -regenAmount, entity.getCenterPosition().cpy());
            logger.debug("Triggered regeneration number display: +{} HP", regenAmount);
        } catch (Exception e) {
            logger.error("Failed to show regeneration number: {}", e.getMessage());
        }
    }
    
    /**
     * 获取当前是否处于回复状态
     * @return true如果正在回复，false否则
     */
    public boolean isRegenerating() {
        return isRegenerating;
    }
    
    /**
     * 获取距离上次受伤的时间（秒）
     * @return 距离上次受伤的秒数
     */
    public float getTimeSinceLastDamage() {
        if (gameTime == null) {
            return 0;
        }
        return (gameTime.getTime() - lastDamageTime) / 1000.0f;
    }
    
    /**
     * 获取距离下次回复的剩余时间（秒）
     * @return 距离下次回复的秒数，如果不在回复状态则返回-1
     */
    public float getTimeUntilNextRegen() {
        if (!isRegenerating || gameTime == null) {
            return -1;
        }
        float timeSinceLastRegen = (gameTime.getTime() - lastRegenerationTime) / 1000.0f;
        return Math.max(0, REGENERATION_INTERVAL - timeSinceLastRegen);
    }
}

