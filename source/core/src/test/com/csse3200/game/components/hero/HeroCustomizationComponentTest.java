package com.csse3200.game.components.hero;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class HeroCustomizationComponentTest {

    private Entity hero;
    private HeroCustomizationComponent customizationComponent;
    
    @Mock
    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        ServiceLocator.registerResourceService(resourceService);
        
        hero = new Entity();
        customizationComponent = new HeroCustomizationComponent();
        hero.addComponent(customizationComponent);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void shouldInitializeWithDefaultSettings() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("default", customizationComponent.getCurrentSkin());
        assertEquals("default", customizationComponent.getCurrentWeapon());
        assertEquals("default", customizationComponent.getCurrentEffect());
    }

    @Test
    void shouldGetCorrectSkinPath() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "warrior";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("warrior", customizationComponent.getCurrentSkin());
    }

    @Test
    void shouldGetCorrectWeaponPath() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "normal sword";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("normal sword", customizationComponent.getCurrentWeapon());
    }

    @Test
    void shouldGetCorrectEffectPath() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "sound 1";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("sound 1", customizationComponent.getCurrentEffect());
    }

    @Test
    void shouldUpdateSkinOnSettingsChanged() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("default", customizationComponent.getCurrentSkin());
        
        settings.heroSkin = "samurai";
        UserSettings.set(settings, false);
        hero.getEvents().trigger("settingsChanged");
        
        assertEquals("samurai", customizationComponent.getCurrentSkin());
    }

    @Test
    void shouldUpdateWeaponOnSettingsChanged() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("default", customizationComponent.getCurrentWeapon());
        
        settings.heroWeapon = "weapon 2";
        UserSettings.set(settings, false);
        hero.getEvents().trigger("settingsChanged");
        
        assertEquals("weapon 2", customizationComponent.getCurrentWeapon());
    }

    @Test
    void shouldUpdateEffectOnSettingsChanged() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        assertEquals("default", customizationComponent.getCurrentEffect());
        
        settings.heroEffect = "sound 3";
        UserSettings.set(settings, false);
        hero.getEvents().trigger("settingsChanged");
        
        assertEquals("sound 3", customizationComponent.getCurrentEffect());
    }

    @Test
    void shouldUpdateAllCustomizationsOnSettingsChanged() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "default";
        settings.heroWeapon = "default";
        settings.heroEffect = "default";
        UserSettings.set(settings, false);
        
        hero.create();
        
        settings.heroSkin = "archer";
        settings.heroWeapon = "weapon 3";
        settings.heroEffect = "sound 2";
        UserSettings.set(settings, false);
        
        hero.getEvents().trigger("settingsChanged");
        
        assertEquals("archer", customizationComponent.getCurrentSkin());
        assertEquals("weapon 3", customizationComponent.getCurrentWeapon());
        assertEquals("sound 2", customizationComponent.getCurrentEffect());
    }

    @Test
    void shouldHandleListenerRegistration() {
        hero.create();
        
        assertNotNull(hero.getEvents());
    }

    @Test
    void shouldNotUpdateWhenSettingsUnchanged() {
        UserSettings.Settings settings = UserSettings.get();
        settings.heroSkin = "warrior";
        settings.heroWeapon = "normal sword";
        settings.heroEffect = "sound 1";
        UserSettings.set(settings, false);
        
        hero.create();
        
        String initialSkin = customizationComponent.getCurrentSkin();
        String initialWeapon = customizationComponent.getCurrentWeapon();
        String initialEffect = customizationComponent.getCurrentEffect();
        
        hero.getEvents().trigger("settingsChanged");
        
        assertEquals(initialSkin, customizationComponent.getCurrentSkin());
        assertEquals(initialWeapon, customizationComponent.getCurrentWeapon());
        assertEquals(initialEffect, customizationComponent.getCurrentEffect());
    }

    @Test
    void shouldHandleAllSkinOptions() {
        String[] skins = {"default", "warrior", "mage", "archer", "assassin", "samurai"};
        
        for (String skin : skins) {
            UserSettings.Settings settings = UserSettings.get();
            settings.heroSkin = skin;
            settings.heroWeapon = "default";
            settings.heroEffect = "default";
            UserSettings.set(settings, false);
            
            hero = new Entity();
            customizationComponent = new HeroCustomizationComponent();
            hero.addComponent(customizationComponent);
            hero.create();
            
            assertEquals(skin, customizationComponent.getCurrentSkin());
        }
    }

    @Test
    void shouldHandleAllWeaponOptions() {
        String[] weapons = {"default", "normal sword", "weapon 2", "weapon 3"};
        
        for (String weapon : weapons) {
            UserSettings.Settings settings = UserSettings.get();
            settings.heroSkin = "default";
            settings.heroWeapon = weapon;
            settings.heroEffect = "default";
            UserSettings.set(settings, false);
            
            hero = new Entity();
            customizationComponent = new HeroCustomizationComponent();
            hero.addComponent(customizationComponent);
            hero.create();
            
            assertEquals(weapon, customizationComponent.getCurrentWeapon());
        }
    }

    @Test
    void shouldHandleAllEffectOptions() {
        String[] effects = {"default", "sound 1", "sound 2", "sound 3"};
        
        for (String effect : effects) {
            UserSettings.Settings settings = UserSettings.get();
            settings.heroSkin = "default";
            settings.heroWeapon = "default";
            settings.heroEffect = effect;
            UserSettings.set(settings, false);
            
            hero = new Entity();
            customizationComponent = new HeroCustomizationComponent();
            hero.addComponent(customizationComponent);
            hero.create();
            
            assertEquals(effect, customizationComponent.getCurrentEffect());
        }
    }
}

