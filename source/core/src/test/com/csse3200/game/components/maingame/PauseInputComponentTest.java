package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Input;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class PauseInputComponentTest {

    @Test
    void escKeyShouldTogglePause() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp); // don't need entity.create()

        AtomicInteger calls = new AtomicInteger(0);
        e.getEvents().addListener("togglePause", (EventListener0) () -> calls.incrementAndGet());

        boolean handled = comp.keyDown(Input.Keys.ESCAPE);
        assertTrue(handled, "ESC should be handled by PauseInputComponent");
        assertEquals(1, calls.get(), "ESC should trigger togglePause exactly once");
    }

    @Test
    void pKeyShouldTogglePause() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp);

        AtomicInteger calls = new AtomicInteger(0);
        e.getEvents().addListener("togglePause", (EventListener0) () -> calls.incrementAndGet());

        boolean handled = comp.keyDown(Input.Keys.P);
        assertTrue(handled, "P should be handled by PauseInputComponent");
        assertEquals(1, calls.get(), "P should trigger togglePause exactly once");
    }

    @Test
    void otherKeysShouldBeIgnored() {
        Entity e = new Entity();
        PauseInputComponent comp = new PauseInputComponent();
        e.addComponent(comp);

        AtomicInteger calls = new AtomicInteger(0);
        e.getEvents().addListener("togglePause", (EventListener0) () -> calls.incrementAndGet());

        boolean handled = comp.keyDown(Input.Keys.SPACE);
        assertFalse(handled, "Non-pause keys should not be handled");
        assertEquals(0, calls.get(), "Non-pause keys must not trigger togglePause");
    }
}
