package com.csse3200.game.components.currencysystem;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

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
}