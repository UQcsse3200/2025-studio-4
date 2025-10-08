package com.csse3200.game.components.towers;

import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyGeneratorComponentTest {

    @Test
    void testConstructorAndGetters() {
        CurrencyGeneratorComponent gen = new CurrencyGeneratorComponent(CurrencyType.TITANIUM_CORE, 5, 2.5f);
        assertEquals(CurrencyType.TITANIUM_CORE, gen.getCurrencyType());
        assertEquals(5, gen.getCurrencyAmount());
        assertEquals(2.5f, gen.getGenerationInterval());
    }

    @Test
    void testSetters() {
        CurrencyGeneratorComponent gen = new CurrencyGeneratorComponent(CurrencyType.NEUROCHIP, 1, 1f);
        gen.setCurrencyType(CurrencyType.TITANIUM_CORE);
        gen.setCurrencyAmount(10);
        gen.setGenerationInterval(3f);
        assertEquals(CurrencyType.TITANIUM_CORE, gen.getCurrencyType());
        assertEquals(10, gen.getCurrencyAmount());
        assertEquals(3f, gen.getGenerationInterval());
    }
}

