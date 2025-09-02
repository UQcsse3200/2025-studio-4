package com.csse3200.game.components.currencysystem;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class CurrencyComponentTest {
    private CurrencyComponent currencyComponent;

    @BeforeEach
    void setUp() {
       currencyComponent = new CurrencyComponent(
                METAL_SCRAP, 5
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldGetTypeMetalScrap() {
        assertEquals(METAL_SCRAP, currencyComponent.getType());
    }

    @Test
    void shouldGetValue5() {
        assertEquals(5, currencyComponent.getValue());
    }
}