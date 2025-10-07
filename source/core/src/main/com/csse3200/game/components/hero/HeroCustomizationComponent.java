package com.csse3200.game.components.hero;

import com.csse3200.game.components.Component;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeroCustomizationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HeroCustomizationComponent.class);
    
    private String currentSkin;
    private String currentWeapon;
    private String currentEffect;
    
    @Override
    public void create() {
        super.create();
        
        UserSettings.Settings settings = UserSettings.get();
        currentSkin = settings.heroSkin;
        currentWeapon = settings.heroWeapon;
        currentEffect = settings.heroEffect;
        
        applyCustomization();
        
        entity.getEvents().addListener("settingsChanged", this::onSettingsChanged);
    }
    
    private void applyCustomization() {
        applySkin();
        applyWeapon();
        applyEffect();
    }
    
    private void applySkin() {
        String skinPath = getSkinPath(currentSkin);
        if (skinPath != null) {
            updateTexture(skinPath);
            logger.debug("Applied hero skin: {}", currentSkin);
        }
    }
    
    private void applyWeapon() {
        String weaponPath = getWeaponPath(currentWeapon);
        if (weaponPath != null) {
            logger.debug("Applied hero weapon: {}", currentWeapon);
        }
    }
    
    private void applyEffect() {
        logger.debug("Applied hero effect: {}", currentEffect);
    }
    
    private String getSkinPath(String skin) {
        switch (skin.toLowerCase()) {
            case "warrior":
                return "images/hero/warrior_skin.png";
            case "mage":
                return "images/hero/mage_skin.png";
            case "archer":
                return "images/hero/archer_skin.png";
            case "assassin":
                return "images/hero/assassin_skin.png";
            case "samurai":
                return "images/hero/samurai_skin.png";
            case "default":
            default:
                return "images/hero/Heroshoot.png";
        }
    }
    
    private String getWeaponPath(String weapon) {
        switch (weapon.toLowerCase()) {
            case "sword":
                return "images/hero/sword_bullet.png";
            case "bow":
                return "images/hero/arrow_bullet.png";
            case "staff":
                return "images/hero/magic_bullet.png";
            case "dagger":
                return "images/hero/dagger_bullet.png";
            case "pistol":
                return "images/hero/pistol_bullet.png";
            case "rifle":
                return "images/hero/rifle_bullet.png";
            case "shotgun":
                return "images/hero/shotgun_bullet.png";
            case "default":
            default:
                return "images/hero/Bullet.png";
        }
    }
    
    private void updateTexture(String texturePath) {
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(texturePath);
            rot.setRotation(angle);
            return;
        }
        
        TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
        if (tex != null) {
            float rotDeg = tex.getRotation();
            try {
                com.badlogic.gdx.graphics.Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, com.badlogic.gdx.graphics.Texture.class);
                TextureRenderComponent newTex = new TextureRenderComponent(texturePath);
                entity.addComponent(newTex);
                newTex.setRotation(rotDeg);
            } catch (Exception e) {
                logger.warn("Failed to load texture: {}", texturePath);
            }
        }
    }
    
    private void onSettingsChanged() {
        UserSettings.Settings settings = UserSettings.get();
        
        boolean changed = false;
        
        if (!currentSkin.equals(settings.heroSkin)) {
            currentSkin = settings.heroSkin;
            applySkin();
            changed = true;
        }
        
        if (!currentWeapon.equals(settings.heroWeapon)) {
            currentWeapon = settings.heroWeapon;
            applyWeapon();
            changed = true;
        }
        
        if (!currentEffect.equals(settings.heroEffect)) {
            currentEffect = settings.heroEffect;
            applyEffect();
            changed = true;
        }
        
        if (changed) {
            logger.info("Hero customization updated - Skin: {}, Weapon: {}, Effect: {}", 
                       currentSkin, currentWeapon, currentEffect);
        }
    }
    
    public String getCurrentSkin() {
        return currentSkin;
    }
    
    public String getCurrentWeapon() {
        return currentWeapon;
    }
    
    public String getCurrentEffect() {
        return currentEffect;
    }
}
