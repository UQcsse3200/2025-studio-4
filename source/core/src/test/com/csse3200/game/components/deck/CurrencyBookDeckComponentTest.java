package com.csse3200.game.components.deck;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class CurrencyBookDeckComponentTest {
    @Test
    public void testCurrencyBookDeckStats() {
        CurrencyBookDeckComponent deck = new CurrencyBookDeckComponent(
                "Metal Scrap", "Basic scrap from battlefield", "images/metal_scrap.png", "sound/collect.mp3"
        );

        Map<DeckComponent.StatType, String> stats = deck.getStats();

        assertEquals("METAL SCRAP", stats.get(DeckComponent.StatType.NAME));
        assertEquals("Basic scrap from battlefield", stats.get(DeckComponent.StatType.LORE));
        assertEquals("images/metal_scrap.png", stats.get(DeckComponent.StatType.TEXTURE_PATH));
        assertEquals("sound/collect.mp3", stats.get(DeckComponent.StatType.SOUND));
    }
}