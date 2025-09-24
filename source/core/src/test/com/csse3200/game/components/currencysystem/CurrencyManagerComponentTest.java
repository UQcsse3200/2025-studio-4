package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.audio.Sound;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void shouldGetCurrencyAmount() {
        // Should return 0 if not set
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
    }

    @Test
    void shouldAddCurrencyEntity() {
        Entity mockEntity = mock(Entity.class);

        CurrencyComponent currencyComponent = new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 1);

        when(mockEntity.getComponent(CurrencyComponent.class)).thenReturn(currencyComponent);
        when(mockEntity.getEvents()).thenReturn(new com.csse3200.game.events.EventHandler());

        currencyManagerComponent.addCurrencyEntity(mockEntity);

        assertEquals(1, currencyManagerComponent.getCurrencyEntityList().size());
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

        assertEquals(1, currencyManagerComponent.getCurrencyEntityList().size());
    }

    @Test
    void shouldGetCurrencyList() {
        // Mock ResourceService just for this test
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        ServiceLocator.registerResourceService(resourceService);

        Entity metalScrap1 = CurrencyFactory.createCurrency(CurrencyComponent.CurrencyType.METAL_SCRAP, 3, 1, 1);
        Entity metalScrap2 = CurrencyFactory.createCurrency(CurrencyComponent.CurrencyType.METAL_SCRAP, 4, 0, 2);

        currencyManagerComponent.addCurrencyEntity(metalScrap1);
        currencyManagerComponent.addCurrencyEntity(metalScrap2);

        assertEquals(2, currencyManagerComponent.getCurrencyEntityList().size());

        // Clean up so it doesn’t leak into other tests
        ServiceLocator.clear();
    }

    @Test
    void shouldDropCurrency() {
        Entity player = new Entity();
        CurrencyManagerComponent manager = new CurrencyManagerComponent();
        player.addComponent(manager);
        player.create(); // Registers dropCurrency listener and preloads 2000

        int before = manager.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP);

        Map<CurrencyComponent.CurrencyType, Integer> drops = Map.of(CurrencyComponent.CurrencyType.METAL_SCRAP, 5);

        player.getEvents().trigger("dropCurrency", drops);

        int after = manager.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP);
        assertEquals(before + 5, after);
    }

    @Test
    void shouldCollectCurrency() {
        // Mock ResourceService to avoid loading actual sound files
        ResourceService resourceService = mock(ResourceService.class);
        Sound mockSound = mock(Sound.class);
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        // Mock a currency entity
        Entity mockEntity = mock(Entity.class);
        CurrencyComponent currencyComponent =
                new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 3);

        when(mockEntity.getComponent(CurrencyComponent.class)).thenReturn(currencyComponent);
        when(mockEntity.getEvents()).thenReturn(new com.csse3200.game.events.EventHandler());

        // Add to manager
        currencyManagerComponent.addCurrencyEntity(mockEntity);

        // Simulate collection
        mockEntity.getEvents().trigger("collectCurrency", mockEntity);

        // Verify currency was added
        assertEquals(3, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));

        // Verify that the sound was played
        verify(mockSound, never()).play();
    }

    @Test
    void shouldNotDeductCurrency() {
        // Create sample cost for testing
        Map<CurrencyComponent.CurrencyType,Integer> costMap = new EnumMap<>(CurrencyComponent.CurrencyType.class);
        costMap.put(CurrencyComponent.CurrencyType.METAL_SCRAP, 5);
        costMap.put(CurrencyComponent.CurrencyType.NEUROCHIP, 5);
        costMap.put(CurrencyComponent.CurrencyType.TITANIUM_CORE, 5);

        // Verify that the player can not afford and currencies are not deducted
        assertFalse(currencyManagerComponent.canAffordAndSpendCurrency(costMap));
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(0, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(0, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));
    }

    @Test
    void shouldNotDeductCurrency2() {
        // Mock ResourceService to avoid loading actual sound files
        ResourceService resourceService = mock(ResourceService.class);
        Sound mockSound = mock(Sound.class);
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        // Create sample cost map for testing
        Map<CurrencyComponent.CurrencyType,Integer> costMap = new EnumMap<>(CurrencyComponent.CurrencyType.class);
        costMap.put(CurrencyComponent.CurrencyType.METAL_SCRAP, 10);
        costMap.put(CurrencyComponent.CurrencyType.TITANIUM_CORE, 5);
        costMap.put(CurrencyComponent.CurrencyType.NEUROCHIP, 2);

        // Create sample currency entities
        Entity metalScrap = new Entity();
        metalScrap.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 10));

        Entity titaniumCore = new Entity();
        titaniumCore.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.TITANIUM_CORE, 3));

        Entity neuroChip = new Entity();
        neuroChip.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.NEUROCHIP, 10));
        currencyManagerComponent.addCurrencyEntity(metalScrap);
        currencyManagerComponent.addCurrencyEntity(titaniumCore);
        currencyManagerComponent.addCurrencyEntity(neuroChip);

        // Trigger events to collect currencies
        metalScrap.getEvents().trigger("collectCurrency", metalScrap);
        titaniumCore.getEvents().trigger("collectCurrency", titaniumCore);
        neuroChip.getEvents().trigger("collectCurrency", neuroChip);

        // Spend currencies
        boolean affordable = currencyManagerComponent.canAffordAndSpendCurrency(costMap);

        assertFalse(affordable);
        assertEquals(10, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(3, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(10, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));
    }

    @Test
    void shouldDeductCurrency() {
        // Mock ResourceService to avoid loading actual sound files
        ResourceService resourceService = mock(ResourceService.class);
        Sound mockSound = mock(Sound.class);
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        Map<CurrencyComponent.CurrencyType,Integer> costMap = new EnumMap<>(CurrencyComponent.CurrencyType.class);
        costMap.put(CurrencyComponent.CurrencyType.METAL_SCRAP, 10);
        costMap.put(CurrencyComponent.CurrencyType.TITANIUM_CORE, 5);
        costMap.put(CurrencyComponent.CurrencyType.NEUROCHIP, 2);

        Entity metalScrap = new Entity();
        metalScrap.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 10));

        Entity titaniumCore = new Entity();
        titaniumCore.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.TITANIUM_CORE, 10));

        Entity neuroChip = new Entity();
        neuroChip.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.NEUROCHIP, 10));

        currencyManagerComponent.addCurrencyEntity(metalScrap);
        currencyManagerComponent.addCurrencyEntity(titaniumCore);
        currencyManagerComponent.addCurrencyEntity(neuroChip);

        metalScrap.getEvents().trigger("collectCurrency", metalScrap);
        titaniumCore.getEvents().trigger("collectCurrency", titaniumCore);
        neuroChip.getEvents().trigger("collectCurrency", neuroChip);

        boolean affordable = currencyManagerComponent.canAffordAndSpendCurrency(costMap);

        assertTrue(affordable);
        assertEquals(0, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(5, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(8, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));
    }

    @Test
    void shouldRefundCorrectly() {
        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        Map<CurrencyComponent.CurrencyType,Integer> costMap = new EnumMap<>(CurrencyComponent.CurrencyType.class);
        costMap.put(CurrencyComponent.CurrencyType.METAL_SCRAP, 10);
        costMap.put(CurrencyComponent.CurrencyType.TITANIUM_CORE, 5);
        costMap.put(CurrencyComponent.CurrencyType.NEUROCHIP, 2);

        currencyManagerComponent.refundCurrency(costMap, 0.7f);


        assertEquals(7, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(3, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(1, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));
    }

    @Test
    void shouldRefundCorrectly2() {
        // Mock ResourceService to avoid loading actual sound files
        ResourceService resourceService = mock(ResourceService.class);
        Sound mockSound = mock(Sound.class);
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        // Create a dummy entity with the manager attached
        Entity testEntity = new Entity();
        testEntity.addComponent(currencyManagerComponent);

        // Create sample cost map for testing
        Map<CurrencyComponent.CurrencyType,Integer> costMap = new EnumMap<>(CurrencyComponent.CurrencyType.class);
        costMap.put(CurrencyComponent.CurrencyType.METAL_SCRAP, 10);
        costMap.put(CurrencyComponent.CurrencyType.TITANIUM_CORE, 5);
        costMap.put(CurrencyComponent.CurrencyType.NEUROCHIP, 2);

        // collect currencies to afford the cost
        Entity metalScrap = new Entity();
        metalScrap.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, 10));

        Entity titaniumCore = new Entity();
        titaniumCore.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.TITANIUM_CORE, 10));

        Entity neuroChip = new Entity();
        neuroChip.addComponent(
                new CurrencyComponent(CurrencyComponent.CurrencyType.NEUROCHIP, 10));
        currencyManagerComponent.addCurrencyEntity(metalScrap);
        currencyManagerComponent.addCurrencyEntity(titaniumCore);
        currencyManagerComponent.addCurrencyEntity(neuroChip);

        // Trigger to collect currencies
        metalScrap.getEvents().trigger("collectCurrency", metalScrap);
        titaniumCore.getEvents().trigger("collectCurrency", titaniumCore);
        neuroChip.getEvents().trigger("collectCurrency", neuroChip);
        // Spend currency
        boolean affordable = currencyManagerComponent.canAffordAndSpendCurrency(costMap);
        assertTrue(affordable);
        assertEquals(0, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(5, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(8, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));

        // Refund currency (for example when selling towers)
        currencyManagerComponent.refundCurrency(costMap, 0.7f);

        assertEquals(7, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(8, currencyManagerComponent
                .getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(9, currencyManagerComponent.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));
    }

    @Test
    void shouldInitializeCurrenciesOnCreate() {
        Entity player = new Entity();
        CurrencyManagerComponent manager = new CurrencyManagerComponent();
        player.addComponent(manager);

        List<String> triggeredEvents = new ArrayList<>();
        player.getEvents().addListener("updateCurrencyUI", (type, amount) -> {
            triggeredEvents.add(type + ":" + amount);
        });

        manager.create();

        assertEquals(100000, manager.getCurrencyAmount(CurrencyComponent.CurrencyType.METAL_SCRAP));
        assertEquals(300, manager.getCurrencyAmount(CurrencyComponent.CurrencyType.TITANIUM_CORE));
        assertEquals(50, manager.getCurrencyAmount(CurrencyComponent.CurrencyType.NEUROCHIP));

        // Assert: updateCurrencyUI was triggered for each type with correct amounts
        assertTrue(triggeredEvents.contains("METAL_SCRAP:100000"));
        assertTrue(triggeredEvents.contains("TITANIUM_CORE:300"));
        assertTrue(triggeredEvents.contains("NEUROCHIP:50"));
    }
}