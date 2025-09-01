package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

public class CollectibleComponentTest {
    private Input mockInput;
    private OrthographicCamera mockCamera;
    private CollectibleComponent collectibleComponent;
    private Entity entity;
    private CameraComponent cameraComponent;
    private Renderer renderer;
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

        mockCamera = mock(OrthographicCamera.class);
        cameraComponent = new CameraComponent(mockCamera);
        mockBatch = mock(SpriteBatch.class);
        mockStage = mock(Stage.class);
        mockRenderService = mock(RenderService.class);
        mockDebugRenderer = mock(DebugRenderer.class);
        renderer = new Renderer(
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



        when(Gdx.input.justTouched()).thenReturn(true);
        when(Gdx.input.getX()).thenReturn(5);
        when(Gdx.input.getY()).thenReturn(5);
        when(mockCamera.unproject(new Vector3(5,5, 0))).thenReturn(new Vector3(5, 5, 0));

        doNothing().when(mockApp).postRunnable(any(Runnable.class));


        entity.setPosition(5,5);

        entity.update();


        verify(collectibleComponent).update();
        assertEquals(true, collectibleComponent.isCollected());
    }

    @Test
    public void ShouldNotCollectCurrency() {

        when(Gdx.input.justTouched()).thenReturn(true);
        when(Gdx.input.getX()).thenReturn(5);
        when(Gdx.input.getY()).thenReturn(5);
        when(mockCamera.unproject(new Vector3(5,5, 0))).thenReturn(new Vector3(5, 5, 0));

        doNothing().when(mockApp).postRunnable(any(Runnable.class));


        entity.setPosition(6,6);

        entity.update();


        verify(collectibleComponent).update();
        assertEquals(false, collectibleComponent.isCollected());
    }
}
