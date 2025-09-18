package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that renders a health bar above an entity.
 * The health bar shows current health as a percentage of max health.
 */
public class HealthBarComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HealthBarComponent.class);
    
    // 血条尺寸
    private static final float HEALTH_BAR_WIDTH = 2.0f;
    private static final float HEALTH_BAR_HEIGHT = 0.2f;
    private static final float HEALTH_BAR_OFFSET_Y = 1.5f; // 在实体上方的偏移
    
    // 血条颜色
    private static final Color HEALTH_BAR_BACKGROUND = new Color(0.3f, 0.3f, 0.3f, 0.8f); // 深灰色背景
    private static final Color HEALTH_BAR_FULL = new Color(0.2f, 0.8f, 0.2f, 0.9f); // 绿色（满血）
    private static final Color HEALTH_BAR_MEDIUM = new Color(1.0f, 0.8f, 0.0f, 0.9f); // 黄色（中等血量）
    private static final Color HEALTH_BAR_LOW = new Color(0.8f, 0.2f, 0.2f, 0.9f); // 红色（低血量）
    
    private PlayerCombatStatsComponent combatStats;
    private int maxHealth;
    private int currentHealth;
    private boolean isVisible = true;
    private ShapeRenderer shapeRenderer;
    
    @Override
    public void create() {
        super.create();
        
        // 初始化 ShapeRenderer
        shapeRenderer = new ShapeRenderer();
        
        // 获取战斗统计组件
        combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HealthBarComponent requires PlayerCombatStatsComponent");
            return;
        }
        
        // 获取初始血量
        currentHealth = combatStats.getHealth();
        maxHealth = currentHealth; // 假设初始血量为最大血量
        
        // 设置血条组件的最大血量
        setMaxHealth(maxHealth);
        
        // 监听血量变化事件
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        logger.debug("HealthBarComponent created for entity with max health: {}", maxHealth);
    }
    
    /**
     * 当血量更新时调用
     */
    private void onHealthUpdate(int newHealth) {
        currentHealth = newHealth;
        logger.debug("Health updated: {}/{}", currentHealth, maxHealth);
    }
    
    /**
     * 设置血条是否可见
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    /**
     * 获取血条是否可见
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * 设置最大血量（用于初始化）
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        logger.debug("Max health set to: {}", maxHealth);
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        if (!isVisible || combatStats == null || maxHealth <= 0) {
            return;
        }
        
        // 计算血量百分比
        float healthPercentage = Math.max(0f, Math.min(1f, (float) currentHealth / maxHealth));
        
        // 获取实体位置
        Vector2 entityPos = entity.getCenterPosition();
        if (entityPos == null) {
            return;
        }
        
        // 计算血条位置（在实体上方）
        float barX = entityPos.x - HEALTH_BAR_WIDTH / 2f;
        float barY = entityPos.y + HEALTH_BAR_OFFSET_Y;
        
        // 保存当前渲染状态
        batch.end();
        
        // 启用混合以支持透明度
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 绘制血条背景
        shapeRenderer.setColor(HEALTH_BAR_BACKGROUND);
        shapeRenderer.rect(barX, barY, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // 绘制血量条
        Color healthColor = getHealthColor(healthPercentage);
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(barX, barY, HEALTH_BAR_WIDTH * healthPercentage, HEALTH_BAR_HEIGHT);
        
        shapeRenderer.end();
        
        // 恢复渲染状态
        batch.begin();
    }
    
    /**
     * 根据血量百分比获取血条颜色
     */
    private Color getHealthColor(float percentage) {
        if (percentage > 0.6f) {
            return HEALTH_BAR_FULL; // 绿色
        } else if (percentage > 0.3f) {
            return HEALTH_BAR_MEDIUM; // 黄色
        } else {
            return HEALTH_BAR_LOW; // 红色
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        logger.debug("HealthBarComponent disposed");
    }
}
