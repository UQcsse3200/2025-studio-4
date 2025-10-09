package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that handles visual damage effects for the homebase.
 * Changes the texture and color when the homebase takes damage to provide visual feedback.
 */
public class HomebaseDamageEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseDamageEffectComponent.class);
    
    // 默认贴图路径
    private static final String DEFAULT_NORMAL_TEXTURE_PATH = "images/basement.png";
    private static final String DEFAULT_DAMAGED_TEXTURE_PATH = "images/basement_damaged.png";
    
    // 音效路径
    private static final String HIT_SOUND_PATH = "sounds/homebase_hit_sound.mp3";
    
    // 受击效果持续时间（秒）
    private static final float DAMAGE_EFFECT_DURATION = 0.3f;
    
    // 受击时的颜色（红色闪烁效果）
    private static final Color DAMAGE_COLOR = new Color(1f, 0.3f, 0.3f, 1f); // 红色
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f); // 白色（正常）
    
    // 实例变量用于存储贴图路径
    private final String normalTexturePath;
    private final String damagedTexturePath;
    
    private SwitchableTextureRenderComponent textureComponent;
    private Texture normalTexture;
    private Texture damagedTexture;
    private Sound hitSound;
    private boolean isShowingDamageEffect = false;
    private float damageEffectTimer = 0f;
    private int previousHealth = -1;
    private boolean hasDamagedTexture = false;
    
    /**
     * Create with default texture paths
     */
    public HomebaseDamageEffectComponent() {
        this(DEFAULT_NORMAL_TEXTURE_PATH);
    }
    
    /**
     * Create with custom texture path
     * @param normalTexturePath path to the normal homebase texture
     */
    public HomebaseDamageEffectComponent(String normalTexturePath) {
        this.normalTexturePath = normalTexturePath;
        // Generate damaged texture path by appending "_damaged" before file extension
        int dotIndex = normalTexturePath.lastIndexOf('.');
        if (dotIndex > 0) {
            this.damagedTexturePath = normalTexturePath.substring(0, dotIndex) + "_damaged" + normalTexturePath.substring(dotIndex);
        } else {
            this.damagedTexturePath = normalTexturePath + "_damaged";
        }
    }
    
    @Override
    public void create() {
        super.create();
        
        // 获取纹理渲染组件
        textureComponent = entity.getComponent(SwitchableTextureRenderComponent.class);
        if (textureComponent == null) {
            logger.error("HomebaseDamageEffectComponent requires SwitchableTextureRenderComponent");
            return;
        }
        
        // 加载贴图和音效
        loadTextures();
        loadHitSound();
        
        // 获取初始生命值
        PlayerCombatStatsComponent combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats != null) {
            previousHealth = combatStats.getHealth();
            logger.debug("Initial health set to: {}", previousHealth);
        }
        
        // 监听伤害事件
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        logger.debug("Added updateHealth event listener");
        
        logger.debug("HomebaseDamageEffectComponent created with damaged texture: {}", hasDamagedTexture);
    }
    
    /**
     * 检查是否有受击贴图可用
     * @return true如果有受击贴图，false如果只有颜色效果
     */
    public boolean hasDamagedTexture() {
        return hasDamagedTexture;
    }
    
    /**
     * 获取受击效果持续时间
     * @return 持续时间（秒）
     */
    public float getDamageEffectDuration() {
        return DAMAGE_EFFECT_DURATION;
    }
    
    /**
     * 检查是否正在显示受击效果
     * @return true如果正在显示受击效果，false否则
     */
    public boolean isShowingDamageEffect() {
        return isShowingDamageEffect;
    }
    
    /**
     * 加载正常和受击状态的贴图
     */
    private void loadTextures() {
        try {
            normalTexture = ServiceLocator.getResourceService().getAsset(normalTexturePath, Texture.class);
            logger.debug("Loaded normal homebase texture: {}", normalTexturePath);
        } catch (Exception e) {
            logger.error("Could not load normal homebase texture: {}", e.getMessage());
            return;
        }
        
        try {
            damagedTexture = ServiceLocator.getResourceService().getAsset(damagedTexturePath, Texture.class);
            hasDamagedTexture = true;
            logger.debug("Loaded damaged homebase texture: {}", damagedTexturePath);
        } catch (Exception e) {
            logger.warn("Could not load damaged texture, will use color-only effect: {}", e.getMessage());
            damagedTexture = null; // 设置为null，表示只使用颜色效果
            hasDamagedTexture = false;
        }
    }
    
    /**
     * 加载受击音效
     */
    private void loadHitSound() {
        try {
            hitSound = ServiceLocator.getResourceService().getAsset(HIT_SOUND_PATH, Sound.class);
            logger.debug("Loaded homebase hit sound: {}", HIT_SOUND_PATH);
        } catch (Exception e) {
            logger.warn("Could not load hit sound, damage effect will be visual only: {}", e.getMessage());
            hitSound = null;
        }
    }
    
    /**
     * 当生命值更新时触发
     */
    private void onHealthUpdate(int newHealth) {
        logger.debug("Health update: previous={}, new={}", previousHealth, newHealth);
        
        // 检查是否受到伤害（生命值减少）
        if (previousHealth > 0 && newHealth < previousHealth) {
            int damageAmount = previousHealth - newHealth;
            logger.debug("Damage detected! Damage amount: {}", damageAmount);
            showDamageEffect(damageAmount);
        }
        
        // 更新previousHealth
        previousHealth = newHealth;
    }
    
    /**
     * 显示受击效果
     */
    private void showDamageEffect(int damageAmount) {
        if (isShowingDamageEffect) {
            return; // 已经在显示受击效果
        }
        
        isShowingDamageEffect = true;
        damageEffectTimer = DAMAGE_EFFECT_DURATION;
        
        // 切换到受击贴图和颜色
        if (textureComponent != null) {
            // 如果有受击贴图，则切换贴图；否则只改变颜色
            if (hasDamagedTexture && damagedTexture != null) {
                textureComponent.setTexture(damagedTexture);
                logger.debug("Switched to damaged texture");
            } else {
                logger.debug("Using color-only damage effect");
            }
            textureComponent.setColor(DAMAGE_COLOR);
        }
        // 显示伤害数字
        showDamageNumber(damageAmount);

        // 播放受击音效
        if (hitSound != null) {
            hitSound.play();
            logger.debug("Playing homebase hit sound");
        }
        
        logger.debug("Showing homebase damage effect with damage: {}", damageAmount);
    }
    
    /**
     * 显示伤害数字
     */
    private void showDamageNumber(int damageAmount) {
        try {
            // 触发伤害数字显示事件，让UI系统处理
            entity.getEvents().trigger("showDamage", damageAmount, entity.getCenterPosition().cpy());
            logger.debug("Triggered damage number display: {} damage", damageAmount);
        } catch (Exception e) {
            logger.error("Failed to show damage number: {}", e.getMessage());
        }
    }
    
    /**
     * 恢复正常状态
     */
    private void returnToNormal() {
        if (!isShowingDamageEffect) {
            return;
        }
        
        isShowingDamageEffect = false;
        damageEffectTimer = 0f;
        
        // 恢复正常贴图和颜色
        if (textureComponent != null) {
            textureComponent.setTexture(normalTexture);
            textureComponent.setColor(NORMAL_COLOR);
        }
        
        logger.debug("Homebase returned to normal state");
    }
    
    @Override
    public void update() {
        super.update();
        
        // 更新受击效果计时器
        if (isShowingDamageEffect) {
            damageEffectTimer -= ServiceLocator.getTimeSource().getDeltaTime();
            
            if (damageEffectTimer <= 0f) {
                returnToNormal();
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // 确保在销毁时恢复正常颜色
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
        }
    }
}
