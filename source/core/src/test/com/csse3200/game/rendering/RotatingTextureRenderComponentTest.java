package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class RotatingTextureRenderComponentTest {
    private Texture mockTexture;
    private SpriteBatch mockBatch;

    @BeforeEach
    void setup() {
        mockTexture = Mockito.mock(Texture.class);
        mockBatch = Mockito.mock(SpriteBatch.class);
        Mockito.when(mockTexture.getWidth()).thenReturn(64);
        Mockito.when(mockTexture.getHeight()).thenReturn(64);
    }

    @Test
    void testSetAndGetRotation() {
        RotatingTextureRenderComponent comp = new RotatingTextureRenderComponent(mockTexture);
        comp.setRotation(45f);
        assertEquals(45f, comp.getRotation(), 0.01f);
    }

    @Test
    void testDrawCallsBatchDraw() {
        RotatingTextureRenderComponent comp = new RotatingTextureRenderComponent(mockTexture);

        TestEntity fakeEntity = new TestEntity(10f, 20f, 32f, 32f);
        comp.setEntity(fakeEntity);

        comp.setRotation(90f);
        comp.draw(mockBatch);

        Mockito.verify(mockBatch, Mockito.times(1))
                .draw(Mockito.eq(mockTexture),
                        Mockito.eq(10f), Mockito.eq(20f),
                        Mockito.eq(16f), Mockito.eq(16f), // origin
                        Mockito.eq(32f), Mockito.eq(32f),
                        Mockito.eq(1f), Mockito.eq(1f),
                        Mockito.eq(90f),
                        Mockito.eq(0), Mockito.eq(0),
                        Mockito.eq(64), Mockito.eq(64),
                        Mockito.eq(false), Mockito.eq(false));
    }

    private static class TestEntity extends com.csse3200.game.entities.Entity {
        private final com.badlogic.gdx.math.Vector2 pos;
        private final com.badlogic.gdx.math.Vector2 size;

        TestEntity(float x, float y, float w, float h) {
            this.pos = new com.badlogic.gdx.math.Vector2(x, y);
            this.size = new com.badlogic.gdx.math.Vector2(w, h);
        }

        @Override
        public com.badlogic.gdx.math.Vector2 getPosition() {
            return pos;
        }

        @Override
        public com.badlogic.gdx.math.Vector2 getScale() {
            return size;
        }
    }
}
