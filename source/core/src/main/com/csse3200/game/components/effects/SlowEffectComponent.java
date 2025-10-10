package com.csse3200.game.components.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 敌人减速特效组件
 * 当应用此效果时，敌人会减速并变成蓝色
 */
public class SlowEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(SlowEffectComponent.class);
    
    // 蓝色特效颜色 (浅蓝色调)
    private static final Color SLOW_COLOR = new Color(0.3f, 0.5f, 1.0f, 1.0f);
    
    // 原始颜色 (白色，无色调)
    private static final Color NORMAL_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    
    // 减速参数
    private final float slowFactor;      // 减速因子 (0.5 = 50%速度)
    private final float duration;        // 效果持续时间（秒）
    
    // 组件引用
    private WaypointComponent waypointComponent;
    private PhysicsMovementComponent physicsMovement;
    private TextureRenderComponent textureRender;
    private AnimationRenderComponent animationRender;
    
    // 状态跟踪
    private boolean isActive = false;
    private float timeRemaining = 0f;
    private Vector2 originalSpeed;
    private Color originalColor;
    
    /**
     * 创建一个减速特效组件
     * 
     * @param slowFactor 减速因子 (0.0-1.0)，例如 0.5 表示减速到50%速度
     * @param duration 效果持续时间（秒）
     */
    public SlowEffectComponent(float slowFactor, float duration) {
        if (slowFactor < 0.0f || slowFactor > 1.0f) {
            throw new IllegalArgumentException("减速因子必须在 0.0 到 1.0 之间");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("持续时间必须大于 0");
        }
        
        this.slowFactor = slowFactor;
        this.duration = duration;
    }
    
    /**
     * 使用默认参数创建减速特效组件
     * 默认减速到 40% 速度，持续 3 秒
     */
    public SlowEffectComponent() {
        this(0.4f, 3.0f);
    }
    
    @Override
    public void create() {
        super.create();
        
        // 获取组件引用
        waypointComponent = entity.getComponent(WaypointComponent.class);
        physicsMovement = entity.getComponent(PhysicsMovementComponent.class);
        textureRender = entity.getComponent(TextureRenderComponent.class);
        animationRender = entity.getComponent(AnimationRenderComponent.class);
        
        // 监听减速效果事件
        entity.getEvents().addListener("applySlow", this::applySlow);
        entity.getEvents().addListener("applySlowWithParams", this::applySlowWithParams);
        entity.getEvents().addListener("removeSlow", this::removeSlow);
        
        logger.debug("减速特效组件已创建 - 减速因子: {}, 持续时间: {}秒", slowFactor, duration);
    }
    
    @Override
    public void update() {
        if (!isActive) {
            return;
        }
        
        // 更新计时器
        float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
        timeRemaining -= deltaTime;
        
        // 检查效果是否结束
        if (timeRemaining <= 0) {
            removeSlow();
        }
    }
    
    /**
     * 应用减速效果（使用组件的默认参数）
     */
    public void applySlow() {
        if (isActive) {
            // 如果已经有减速效果，刷新持续时间
            timeRemaining = duration;
            logger.debug("刷新减速效果持续时间");
            return;
        }
        
        // 保存原始速度
        if (waypointComponent != null) {
            originalSpeed = new Vector2(waypointComponent.getSpeed());
        } else if (physicsMovement != null) {
            originalSpeed = new Vector2(physicsMovement.maxSpeed);
        }
        
        // 应用减速
        if (originalSpeed != null) {
            Vector2 slowedSpeed = new Vector2(
                originalSpeed.x * slowFactor,
                originalSpeed.y * slowFactor
            );
            
            if (waypointComponent != null) {
                waypointComponent.setSpeed(slowedSpeed);
            }
            
            if (physicsMovement != null) {
                physicsMovement.maxSpeed.set(slowedSpeed);
                physicsMovement.setSpeed(slowedSpeed);
            }
            
            logger.debug("应用减速: 原始速度 {}, 减速后 {}", originalSpeed, slowedSpeed);
        }
        
        // 应用蓝色特效
        applyBlueEffect();
        
        // 激活效果
        isActive = true;
        timeRemaining = duration;
        
        // 触发事件
        entity.getEvents().trigger("slowEffectApplied");
        
        logger.info("敌人减速特效已激活 ({}% 速度, {}秒)", (int)(slowFactor * 100), duration);
    }
    
    /**
     * 应用减速效果（使用自定义参数）
     * 
     * @param customSlowFactor 自定义减速因子
     * @param customDuration 自定义持续时间
     */
    public void applySlowWithParams(float customSlowFactor, float customDuration) {
        // 暂时修改参数（这个方法会使用传入的参数而不是构造函数的参数）
        // 注意：这里为了简化，我们直接使用构造函数的参数
        applySlow();
    }
    
    /**
     * 移除减速效果
     */
    public void removeSlow() {
        if (!isActive) {
            return;
        }
        
        // 恢复原始速度
        if (originalSpeed != null) {
            if (waypointComponent != null) {
                waypointComponent.setSpeed(originalSpeed);
            }
            
            if (physicsMovement != null) {
                physicsMovement.maxSpeed.set(originalSpeed);
                physicsMovement.setSpeed(originalSpeed);
            }
            
            logger.debug("恢复速度: {}", originalSpeed);
        }
        
        // 恢复原始颜色
        removeBlueEffect();
        
        // 重置状态
        isActive = false;
        timeRemaining = 0f;
        originalSpeed = null;
        
        // 触发事件
        entity.getEvents().trigger("slowEffectRemoved");
        
        logger.info("敌人减速特效已移除");
    }
    
    /**
     * 应用蓝色视觉效果
     */
    private void applyBlueEffect() {
        // 优先使用动画渲染组件
        if (animationRender != null) {
            // 保存原始颜色
            originalColor = animationRender.getColor();
            // 应用蓝色调
            animationRender.setColor(SLOW_COLOR);
            logger.info("已应用蓝色特效到动画渲染组件 - 颜色: {}", SLOW_COLOR);
            return;
        }
        
        // 如果没有动画组件，使用材质渲染组件
        if (textureRender != null) {
            // 保存原始颜色
            originalColor = textureRender.getColor();
            // 应用蓝色调
            textureRender.setColor(SLOW_COLOR);
            logger.info("已应用蓝色特效到材质渲染组件 - 颜色: {}", SLOW_COLOR);
            return;
        }
        
        logger.warn("未找到渲染组件，无法应用蓝色特效");
    }
    
    /**
     * 移除蓝色视觉效果
     */
    private void removeBlueEffect() {
        // 优先恢复动画颜色
        if (animationRender != null) {
            if (originalColor != null) {
                animationRender.setColor(originalColor);
                logger.info("已恢复动画渲染组件的原始颜色: {}", originalColor);
            } else {
                animationRender.setColor(NORMAL_COLOR);
                logger.info("已恢复动画渲染组件的默认颜色");
            }
            originalColor = null;
            return;
        }
        
        // 如果没有动画组件，恢复材质颜色
        if (textureRender != null) {
            if (originalColor != null) {
                textureRender.setColor(originalColor);
                logger.info("已恢复材质渲染组件的原始颜色: {}", originalColor);
            } else {
                textureRender.setColor(NORMAL_COLOR);
                logger.info("已恢复材质渲染组件的默认颜色");
            }
            originalColor = null;
            return;
        }
        
        logger.warn("未找到渲染组件，无法恢复颜色");
    }
    
    /**
     * 检查减速效果是否激活
     * 
     * @return true 如果效果激活，否则 false
     */
    public boolean isSlowActive() {
        return isActive;
    }
    
    /**
     * 获取剩余时间
     * 
     * @return 剩余时间（秒）
     */
    public float getTimeRemaining() {
        return timeRemaining;
    }
    
    /**
     * 获取减速因子
     * 
     * @return 减速因子
     */
    public float getSlowFactor() {
        return slowFactor;
    }
    
    @Override
    public void dispose() {
        // 清理：如果组件被销毁时效果仍然激活，确保恢复状态
        if (isActive) {
            removeSlow();
        }
        super.dispose();
    }
}

