package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Homebase治疗效果组件
 * 当homebase在3秒内没有受到伤害时，在其周围显示半透明且播放的re.gif动画
 */
public class HomebaseHealingEffectComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(HomebaseHealingEffectComponent.class);
    
    // 配置常量
    private static final float TIME_WITHOUT_DAMAGE_REQUIRED = 3.0f; // 需要多少秒不受伤才显示效果
    private static final String EFFECT_GIF_PATH = "images/re.gif"; // GIF路径
    private static final float EFFECT_ALPHA = 0.4f; // 半透明度（0.0-1.0）
    private static final float BASE_SCALE = 0.4f; // 基础缩放大小
    private static final float ROTATION_SPEED = 0f; // 旋转速度（度/秒）
    private static final float FRAME_DURATION = 0.05f; // 每帧持续时间（秒）
    
    // 状态追踪
    private long lastDamageTime; // 最后一次受伤的时间（毫秒）
    private int previousHealth = -1; // 上一次的生命值，用于检测受伤
    private boolean isEffectActive = false; // 当前效果是否激活
    private float rotationAngle = 0f; // 当前旋转角度
    private float animationTime = 0f; // 动画时间
    
    // 资源
    private Animation<TextureRegion> gifAnimation;
    private GameTime gameTime;
    private PlayerCombatStatsComponent combatStats;
    private Array<Texture> frameTextures; // 保存所有帧的纹理以便释放
    
    @Override
    public void create() {
        super.create();
        
        frameTextures = new Array<>();
        
        // 加载GIF动画
        try {
            loadGifAnimation();
            logger.info("Loaded GIF animation: {}", EFFECT_GIF_PATH);
        } catch (Exception e) {
            logger.error("Failed to load GIF animation: {}", e.getMessage(), e);
            gifAnimation = null;
        }
        
        // 获取游戏时间服务
        gameTime = ServiceLocator.getTimeSource();
        if (gameTime == null) {
            logger.error("GameTime service not available");
            return;
        }
        
        // 获取战斗状态组件
        combatStats = entity.getComponent(PlayerCombatStatsComponent.class);
        if (combatStats == null) {
            logger.error("HomebaseHealingEffectComponent requires PlayerCombatStatsComponent");
            return;
        }
        
        // 初始化时间
        lastDamageTime = gameTime.getTime();
        previousHealth = combatStats.getHealth();
        
        // 监听生命值变化事件
        entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
        
        logger.info("HomebaseHealingEffectComponent created");
    }
    
    /**
     * 加载GIF动画
     */
    private void loadGifAnimation() {
        try {
            FileHandle gifFile = Gdx.files.internal(EFFECT_GIF_PATH);
            InputStream inputStream = gifFile.read();
            
            // 读取GIF的所有帧
            Array<BufferedImage> frames = readGifFrames(inputStream);
            inputStream.close();
            
            if (frames.size == 0) {
                logger.error("No frames found in GIF");
                return;
            }
            
            // 将BufferedImage转换为TextureRegion
            Array<TextureRegion> textureRegions = new Array<>();
            for (BufferedImage bufferedImage : frames) {
                Pixmap pixmap = convertToPixmap(bufferedImage);
                Texture texture = new Texture(pixmap);
                frameTextures.add(texture);
                textureRegions.add(new TextureRegion(texture));
                pixmap.dispose();
            }
            
            // 创建动画
            gifAnimation = new Animation<>(FRAME_DURATION, textureRegions, Animation.PlayMode.LOOP);
            
            logger.info("Loaded {} frames from GIF", frames.size);
        } catch (Exception e) {
            logger.error("Error loading GIF: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 读取GIF的所有帧
     */
    private Array<BufferedImage> readGifFrames(InputStream inputStream) throws IOException {
        Array<BufferedImage> frames = new Array<>();
        
        ImageInputStream stream = ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        
        if (!readers.hasNext()) {
            logger.error("No GIF readers found");
            return frames;
        }
        
        ImageReader reader = readers.next();
        reader.setInput(stream);
        
        int frameCount = reader.getNumImages(true);
        for (int i = 0; i < frameCount; i++) {
            BufferedImage image = reader.read(i);
            frames.add(image);
        }
        
        reader.dispose();
        stream.close();
        
        return frames;
    }
    
    /**
     * 将BufferedImage转换为Pixmap
     */
    private Pixmap convertToPixmap(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int a = (argb >> 24) & 0xFF;
                
                // 将ARGB转换为RGBA并写入Pixmap
                pixmap.setColor((r << 24) | (g << 16) | (b << 8) | a);
                pixmap.drawPixel(x, y);
            }
        }
        
        return pixmap;
    }
    
    /**
     * 当生命值更新时触发，用于检测是否受到伤害
     */
    private void onHealthUpdate(int newHealth) {
        // 检查是否受到伤害（生命值减少）
        if (previousHealth > 0 && newHealth < previousHealth) {
            // 受到伤害，重置最后受伤时间
            lastDamageTime = gameTime.getTime();
            if (isEffectActive) {
                isEffectActive = false;
                animationTime = 0f; // 重置动画时间
                logger.debug("Healing effect deactivated due to damage");
            }
        }
        
        // 更新previousHealth
        previousHealth = newHealth;
    }
    
    @Override
    public void update() {
        if (gameTime == null || combatStats == null) {
            return;
        }
        
        long currentTime = gameTime.getTime();
        float timeSinceLastDamage = (currentTime - lastDamageTime) / 1000.0f;
        
        // 检查是否应该激活效果
        if (timeSinceLastDamage >= TIME_WITHOUT_DAMAGE_REQUIRED) {
            if (!isEffectActive) {
                isEffectActive = true;
                animationTime = 0f;
                logger.debug("Healing effect activated");
            }
        } else {
            if (isEffectActive) {
                isEffectActive = false;
                logger.debug("Healing effect deactivated");
            }
        }
        
        // 更新动画
        if (isEffectActive) {
            float deltaTime = gameTime.getDeltaTime();
            
            // 更新旋转角度
            rotationAngle += ROTATION_SPEED * deltaTime;
            if (rotationAngle >= 360f) {
                rotationAngle -= 360f;
            }
            
            // 更新动画时间
            animationTime += deltaTime;
        }
    }
    
    @Override
    protected void draw(SpriteBatch batch) {
        if (!isEffectActive || entity == null || gifAnimation == null) {
            return;
        }
        
        // 获取homebase中心位置
        Vector2 centerPos = entity.getCenterPosition();
        if (centerPos == null) {
            centerPos = entity.getPosition();
        }
        
        // 获取当前帧
        TextureRegion currentFrame = gifAnimation.getKeyFrame(animationTime);
        if (currentFrame == null) {
            return;
        }
        
        // 保存当前颜色
        Color oldColor = batch.getColor();
        
        // 设置半透明颜色
        Color animatedColor = new Color(1f, 1f, 1f, EFFECT_ALPHA);
        batch.setColor(animatedColor);
        
        // 计算绘制位置和大小
        float width = currentFrame.getRegionWidth() / 100f * BASE_SCALE; // 转换为世界单位
        float height = currentFrame.getRegionHeight() / 100f * BASE_SCALE;
        
        // 绘制带旋转的动画帧（居中）
        batch.draw(
            currentFrame,
            centerPos.x - width / 2f,       // x位置（居中）
            centerPos.y - height / 2f,      // y位置（居中）
            width / 2f,                     // 旋转中心x（相对于x,y）
            height / 2f,                    // 旋转中心y（相对于x,y）
            width,                          // 宽度
            height,                         // 高度
            1f,                             // x缩放
            1f,                             // y缩放
            rotationAngle                   // 旋转角度
        );
        
        // 恢复颜色
        batch.setColor(oldColor);
    }
    
    @Override
    public void dispose() {
        // 释放所有帧纹理
        if (frameTextures != null) {
            for (Texture texture : frameTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            frameTextures.clear();
        }
        super.dispose();
    }
    
    /**
     * 获取效果是否激活
     * @return true如果效果激活，false否则
     */
    public boolean isEffectActive() {
        return isEffectActive;
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
    
    @Override
    public float getZIndex() {
        // 设置较高的Z-index，使效果在homebase上方渲染
        return 2f;
    }
}
