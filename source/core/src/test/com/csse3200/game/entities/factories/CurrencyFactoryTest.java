package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.currencysystem.CollectibleComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class CurrencyFactoryTest {

    @BeforeEach
    void setUp() {
        // Mock ResourceService so TextureRenderComponent doesn't fail
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        ServiceLocator.registerResourceService(resourceService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void createCurrency() {
        Entity currency = CurrencyFactory.createCurrency(CurrencyComponent.CurrencyType.METAL_SCRAP, 5, 2f, 3f, "images/metal-scrap-currency.png");

        assertNotNull(currency);
        assertNotNull(currency.getComponent(TextureRenderComponent.class));
        assertNotNull(currency.getComponent(CurrencyComponent.class));
        assertNotNull(currency.getComponent(CollectibleComponent.class));

        // Verify currency values
        CurrencyComponent currencyComponent = currency.getComponent(CurrencyComponent.class);
        assertEquals(CurrencyComponent.CurrencyType.METAL_SCRAP, currencyComponent.getType());
        assertEquals(5, currencyComponent.getValue());

        // Verify position
        assertEquals(2f, currency.getPosition().x);
        assertEquals(3f, currency.getPosition().y);
    }

    @Test
    void createMetalScrap() {
        Entity scrap = CurrencyFactory.createMetalScrap(4f, 6f);

        assertNotNull(scrap);
        assertNotNull(scrap.getComponent(TextureRenderComponent.class));
        assertNotNull(scrap.getComponent(CurrencyComponent.class));
        assertNotNull(scrap.getComponent(CollectibleComponent.class));

        CurrencyComponent currencyComponent = scrap.getComponent(CurrencyComponent.class);
        assertEquals(CurrencyComponent.CurrencyType.METAL_SCRAP, currencyComponent.getType());
        assertEquals(1, currencyComponent.getValue());

        assertEquals(4f, scrap.getPosition().x);
        assertEquals(6f, scrap.getPosition().y);
    }
}