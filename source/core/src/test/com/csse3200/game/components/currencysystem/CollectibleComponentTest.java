package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class CollectibleComponentTest {
    private Input mockInput;
    private OrthographicCamera mockCamera;
    private CollectibleComponent collectibleComponent;
    private Entity entity;
    private CameraComponent cameraComponent;
    SpriteBatch mockBatch;
    Stage mockStage;
    RenderService mockRenderService;
    DebugRenderer mockDebugRenderer;
    Graphics mockGraphics;
Application mockApp;
    @Before
    public void setUp() {

        mockInput = mock(Input.class);
        Gdx.input = mockInput;

        mockGraphics = mock(Graphics.class);
        Gdx.graphics = mockGraphics;

        mockApp = mock(Application.class);
        Gdx.app = mockApp;

        when(Gdx.graphics.getWidth()).thenReturn(0);
        when(Gdx.graphics.getHeight()).thenReturn(0);

        // Mock renderer and its dependencies
        mockCamera = mock(OrthographicCamera.class);
        cameraComponent = new CameraComponent(mockCamera);
        mockBatch = mock(SpriteBatch.class);
        mockStage = mock(Stage.class);
        mockRenderService = mock(RenderService.class);
        mockDebugRenderer = mock(DebugRenderer.class);
        Renderer renderer = new Renderer(
                cameraComponent,
                20f,
                mockBatch,
                mockStage,
                mockRenderService,
                mockDebugRenderer
        );


        entity = spy(Entity.class);
        collectibleComponent = spy(CollectibleComponent.class);
        entity.addComponent(collectibleComponent);
        entity.create();
    }

    @Test
    public void ShouldCollectCurrency() {
        // Stubbing behavior for Gdx.input
        when(Gdx.input.justTouched()).thenReturn(true);
        when(Gdx.input.getX()).thenReturn(5);
        when(Gdx.input.getY()).thenReturn(5);

        // Stubbing behavior for camera
        when(mockCamera.unproject(new Vector3(5,5, 0))).thenReturn(new Vector3(5, 5, 0));

        // Do nothing when dispose the entity
        doNothing().when(mockApp).postRunnable(any(Runnable.class));

        // Set position for the entity
        entity.setPosition(5,5);

        // Trigger update() function in collectibleComponent
        entity.update();

        // Check if update() called and the desired result
        verify(collectibleComponent).update();
        // Should be true
        assertEquals(true, collectibleComponent.isCollected());
    }

    @Test
    public void ShouldNotCollectCurrency() {
        // Stubbing behavior for Gdx.input
        when(Gdx.input.justTouched()).thenReturn(true);
        when(Gdx.input.getX()).thenReturn(5);
        when(Gdx.input.getY()).thenReturn(5);

        // Stubbing behavior for camera
        when(mockCamera.unproject(new Vector3(5,5, 0))).thenReturn(new Vector3(5, 5, 0));

        // Do nothing when dispose the entity
        doNothing().when(mockApp).postRunnable(any(Runnable.class));

        // Set position for the entity
        entity.setPosition(6,6);

        // Trigger update() function in collectibleComponent
        entity.update();

        // Check if update() called and the desired result
        verify(collectibleComponent).update();
        // Should be false as (6-5) > clickRadius == 1
        assertEquals(false, collectibleComponent.isCollected());
    }

    @Test
    public void ShouldNotCollectWhenNotTouched() {
        when(Gdx.input.justTouched()).thenReturn(false);
        entity.update();
        assertFalse(collectibleComponent.isCollected(),
                "Should not collect when screen not touched");
    }

    @Test
    public void ShouldNotCollectWhenYDifferenceTooLarge() {
        when(Gdx.input.justTouched()).thenReturn(true);
        when(Gdx.input.getX()).thenReturn(5);
        when(Gdx.input.getY()).thenReturn(5);

        doNothing().when(mockApp).postRunnable(any(Runnable.class));

        // Place entity far away in Y
        entity.setPosition(5, 20);

        entity.update();

        assertFalse(collectibleComponent.isCollected(),
                "Should not collect when Y difference > clickRadius");
    }
}
