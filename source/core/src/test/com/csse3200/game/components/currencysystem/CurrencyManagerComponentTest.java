package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.entities.factories.CurrencyFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class CurrencyManagerComponentTest {
    private CurrencyManagerComponent currencyManagerComponent;

    @BeforeEach
    void setUp() {
        currencyManagerComponent = new CurrencyManagerComponent();
    }

    @AfterEach
    void tearDown() {
        currencyManagerComponent = null;
    }

    @Test
    void shouldAddCurrencyAmount() {
        // Initially should be 0
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));

        // Add 10 scraps
        currencyManagerComponent.addCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 10);
        assertEquals(10, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));

        // Add more scraps
        currencyManagerComponent.addCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 5);
        assertEquals(15, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
    }

    @Test
    void shouldSubtractCurrencyAmount() {
        // Start with 20 scraps
        currencyManagerComponent.addCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 20);

        // Subtract 5 scraps
        currencyManagerComponent.subtractCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 5);
        assertEquals(15, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));

        // Subtract more than available (should be 0, not negative)
        currencyManagerComponent.subtractCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 50);
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
    }

    @Test
    void shouldGetCurrencyAmount() {
        // Should return 0 if not set
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));

        // Add and verify
        currencyManagerComponent.addCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP, 7);
        assertEquals(7, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
    }

    @Test
    void shouldAddCurrencyEntity() {
        Entity mockEntity = mock(Entity.class);

        CurrencyComponent currencyComponent = new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 1);

        when(mockEntity.getComponent(CurrencyComponent.class)).thenReturn(currencyComponent);
        when(mockEntity.getEvents()).thenReturn(new com.csse3200.game.events.EventHandler());

        currencyManagerComponent.addCurrencyEntity(mockEntity);

        assertEquals(1, currencyManagerComponent.getCurrencyList().size());
    }

    @Test
    void shouldNotAddCurrencyEntityTwice() {
        Entity mockEntity = mock(Entity.class);

        CurrencyComponent currencyComponent = new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 1);

        when(mockEntity.getComponent(CurrencyComponent.class)).thenReturn(currencyComponent);
        when(mockEntity.getEvents()).thenReturn(new com.csse3200.game.events.EventHandler());

        // Should not add same entity twice
        currencyManagerComponent.addCurrencyEntity(mockEntity);
        currencyManagerComponent.addCurrencyEntity(mockEntity);

        assertEquals(1, currencyManagerComponent.getCurrencyList().size());
    }

    @Test
    void shouldGetCurrencyList() {
        // Mock ResourceService just for this test
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        ServiceLocator.registerResourceService(resourceService);

        Entity metalScrap1 = CurrencyFactory.createMetalScrap(3, 3);
        Entity metalScrap2 = CurrencyFactory.createMetalScrap(5, 6);

        currencyManagerComponent.addCurrencyEntity(metalScrap1);
        currencyManagerComponent.addCurrencyEntity(metalScrap2);

        assertEquals(2, currencyManagerComponent.getCurrencyList().size());

        // Clean up so it doesn’t leak into other tests
        ServiceLocator.clear();
    }

    @Test
    void shouldCollectCurrency() {
        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        // Mock a currency entity
        Entity mockEntity = mock(Entity.class);
        CurrencyComponent currencyComponent = new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 3);

        when(mockEntity.getComponent(CurrencyComponent.class)).thenReturn(currencyComponent);
        when(mockEntity.getEvents()).thenReturn(new com.csse3200.game.events.EventHandler());

        // Add to manager
        currencyManagerComponent.addCurrencyEntity(mockEntity);

        // Simulate collection
        mockEntity.getEvents().trigger("collectCurrency", mockEntity);

        // Verify currency was added
        assertEquals(3, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
    }
}