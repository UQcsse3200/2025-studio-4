package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地回血特效组件
 * 当基地回血时显示绿色闪烁特效和回血动画
 */
public class HomebaseHealingEffectComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseHealingEffectComponent.class);
    
    // 回血效果持续时间（秒）
    private static final float HEALING_EFFECT_DURATION = 0.5f;

    // 可选：回血视频资源路径（存在则优先使用）
    private static final String HEALING_VIDEO_PATH = "images/re.mp4";
    
    // 回血时的颜色（绿色闪烁效果）
    private static final Color HEALING_COLOR = new Color(0.3f, 1f, 0.3f, 1f); // 绿色
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f); // 白色（正常）
    
    private SwitchableTextureRenderComponent textureComponent;
    private HealingVideoOverlay videoOverlay;
    private boolean isShowingHealingEffect = false;
    private float healingEffectTimer = 0f;
    private int previousHealth = -1;
    
    /**
     * 创建基地回血特效组件
     */
    public HomebaseHealingEffectComponent() {
    }
    
    @Override
    public void create() {
        super.create();
        
        // 获取纹理渲染组件
        textureComponent = entity.getComponent(SwitchableTextureRenderComponent.class);
        if (textureComponent == null) {
            logger.error("HomebaseHealingEffectComponent requires SwitchableTextureRenderComponent");
            return;
        }

        // 尝试创建回血视频叠加渲染（若环境支持 gdx-video 且资源存在）
        try {
            FileHandle vh = Gdx.files.internal(HEALING_VIDEO_PATH);
            if (vh.exists()) {
                videoOverlay = new HealingVideoOverlay(HEALING_VIDEO_PATH);
                entity.addComponent(videoOverlay);
                logger.debug("Healing video overlay initialised for {}", HEALING_VIDEO_PATH);
            } else {
                logger.info("Healing video not found at {} — will use color pulse fallback", HEALING_VIDEO_PATH);
            }
        } catch (Exception e) {
            // 无法访问文件系统或其他异常，回退到颜色效果
            logger.warn("Failed to init healing video overlay: {}", e.getMessage());
        }
        
        // 获取初始生命值
        PlayerCombatStatsComponent combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats != null) {
            previousHealth = combatStats.getHealth();
            logger.debug("Initial health set to: {}", previousHealth);
        }
        
        // 监听生命值更新事件
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        // 监听伤害显示事件来检测回血（负数表示回血）
        entity.getEvents().addListener("showDamage", this::onDamageShow);
        
        logger.debug("HomebaseHealingEffectComponent created successfully");
    }
    
    /**
     * 当生命值更新时触发
     */
    private void onHealthUpdate(int newHealth) {
        // 检查是否回血（生命值增加）
        if (previousHealth > 0 && newHealth > previousHealth) {
            int healAmount = newHealth - previousHealth;
            showHealingEffect(healAmount);
        }
        
        // 更新previousHealth
        previousHealth = newHealth;
    }
    
    /**
     * 当显示伤害数字时触发（负数表示回血）
     */
    private void onDamageShow(int amount, Vector2 position) {
        // 如果amount是负数，表示回血
        if (amount < 0) {
            showHealingEffect(-amount);
        }
    }
    
    /**
     * 显示回血特效
     */
    private void showHealingEffect(int healAmount) {
        if (isShowingHealingEffect) {
            // 如果已经在显示回血效果，重置计时器以延长效果时间
            healingEffectTimer = HEALING_EFFECT_DURATION;
            return;
        }
        
        isShowingHealingEffect = true;
        healingEffectTimer = HEALING_EFFECT_DURATION;
        
        // 应用回血特效
        if (textureComponent != null) {
            // 如果有回血特效贴图，可以选择切换贴图或只改变颜色
            // 这里我们使用颜色叠加的方式，保持原贴图
            textureComponent.setColor(HEALING_COLOR);
            logger.debug("Applied healing color effect");
        }

        // 如果视频叠加可用，开始播放视频
        if (videoOverlay != null) {
            videoOverlay.show();
        }
        
        logger.debug("Showing homebase healing effect with heal amount: {}", healAmount);
    }
    
    /**
     * 恢复正常状态
     */
    private void returnToNormal() {
        if (!isShowingHealingEffect) {
            return;
        }
        
        isShowingHealingEffect = false;
        healingEffectTimer = 0f;
        
        // 恢复正常颜色
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
            logger.debug("Homebase returned to normal color");
        }

        // 停止视频叠加
        if (videoOverlay != null) {
            videoOverlay.hide();
        }
    }
    
    @Override
    public void update() {
        super.update();
        
        // 更新回血特效计时器
        if (isShowingHealingEffect) {
            healingEffectTimer -= ServiceLocator.getTimeSource().getDeltaTime();
            
            // 添加脉冲效果：根据剩余时间调整颜色透明度
            if (textureComponent != null) {
                float progress = healingEffectTimer / HEALING_EFFECT_DURATION;
                float alpha = 0.5f + 0.5f * (float)Math.sin(progress * Math.PI * 4); // 脉冲效果
                Color pulseColor = new Color(
                    0.3f + alpha * 0.3f,  // R: 0.3 - 0.6
                    1f,                     // G: 1.0 (保持绿色)
                    0.3f + alpha * 0.3f,  // B: 0.3 - 0.6
                    1f                      // A: 1.0 (完全不透明)
                );
                textureComponent.setColor(pulseColor);
            }
            
            if (healingEffectTimer <= 0f) {
                returnToNormal();
            }
        }
    }
    
    /**
     * 检查是否正在显示回血特效
     * @return true如果正在显示回血特效，false否则
     */
    public boolean isShowingHealingEffect() {
        return isShowingHealingEffect;
    }
    
    /**
     * 获取回血特效持续时间
     * @return 持续时间（秒）
     */
    public float getHealingEffectDuration() {
        return HEALING_EFFECT_DURATION;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        // 确保在销毁时恢复正常颜色
        if (textureComponent != null) {
            textureComponent.setColor(NORMAL_COLOR);
        }
        if (videoOverlay != null) {
            try {
                videoOverlay.hide();
            } catch (Exception ignored) {
            }
        }
    }
}


/**
 * 通过反射优先使用 gdx-video 播放 MP4 作为叠加特效；若库缺失则静默不绘制（回退到颜色脉冲）。
 * 该组件独立于逻辑组件，渲染于同一实体位置之上。
 */
class HealingVideoOverlay extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HealingVideoOverlay.class);

    private final String videoPath;
    private boolean active = false;

    // 反射缓存
    private boolean reflectionReady = false;
    private Object videoPlayer; // com.badlogic.gdx.video.VideoPlayer
    private java.lang.reflect.Method mPlay;        // play(FileHandle)
    private java.lang.reflect.Method mStop;        // stop()
    private java.lang.reflect.Method mSetLooping;  // setLooping(boolean)
    private java.lang.reflect.Method mGetTexture;  // getTexture() -> Texture or TextureRegion

    HealingVideoOverlay(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public void create() {
        super.create();
        initReflection();
    }

    private void initReflection() {
        try {
            Class<?> creatorClz = Class.forName("com.badlogic.gdx.video.VideoPlayerCreator");
            java.lang.reflect.Method create = creatorClz.getMethod("createVideoPlayer");
            this.videoPlayer = create.invoke(null);

            Class<?> playerClz = Class.forName("com.badlogic.gdx.video.VideoPlayer");
            this.mPlay = playerClz.getMethod("play", FileHandle.class);
            this.mStop = playerClz.getMethod("stop");
            this.mSetLooping = playerClz.getMethod("setLooping", boolean.class);

            // getTexture could return Texture or TextureRegion depending on implementation
            // We will try getTexture() first and handle both types at call site
            try {
                this.mGetTexture = playerClz.getMethod("getTexture");
            } catch (NoSuchMethodException nsme) {
                // Fallback: some forks expose getTextureRegion()
                this.mGetTexture = playerClz.getMethod("getTextureRegion");
            }

            // 默认循环关闭：按计时控制显示时长
            mSetLooping.invoke(videoPlayer, false);
            reflectionReady = true;
            logger.info("gdx-video detected, healing MP4 overlay enabled");
        } catch (Throwable t) {
            reflectionReady = false;
            logger.info("gdx-video not available, using color-only healing effect ({}).", t.getClass().getSimpleName());
        }
    }

    public void show() {
        active = true;
        if (reflectionReady) {
            try {
                FileHandle fh = Gdx.files.internal(videoPath);
                if (fh.exists()) {
                    mPlay.invoke(videoPlayer, fh);
                } else {
                    logger.warn("Healing video file not found: {}", videoPath);
                }
            } catch (Exception e) {
                logger.warn("Failed to play healing video: {}", e.getMessage());
            }
        }
    }

    public void hide() {
        active = false;
        if (reflectionReady) {
            try {
                mStop.invoke(videoPlayer);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (!active) return;
        if (!reflectionReady) return; // 没有视频库则不绘制（逻辑组件依旧做颜色效果）

        try {
            Object frameObj = mGetTexture.invoke(videoPlayer);
            if (frameObj == null) return;

            Vector2 pos = entity.getPosition();
            Vector2 size = entity.getScale();
            float w = (size == null ? 1f : size.x);
            float h = (size == null ? 1f : size.y);

            if (frameObj instanceof TextureRegion region) {
                batch.draw(region, pos.x, pos.y, w, h);
            } else if (frameObj instanceof Texture tex) {
                batch.draw(tex, pos.x, pos.y, w, h);
            } else {
                // 未知类型，跳过绘制
            }
        } catch (Exception e) {
            logger.debug("Video frame render failed: {}", e.getMessage());
        }
    }

    @Override
    public float getZIndex() {
        // 略微提升 Z 顺序以覆盖同一位置的基础贴图
        return -entity.getPosition().y - 0.001f;
    }
}
