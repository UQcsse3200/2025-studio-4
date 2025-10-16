package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that adds floating decorative elements to the background
 * such as stars, clouds, and particles
 */
public class BackgroundDecorator extends RenderComponent {
    private static final int NUM_STARS = 20;
    private static final int NUM_CLOUDS = 8;
    private static final float STAR_SIZE = 0.3f;
    private static final float CLOUD_SIZE = 1.5f;
    
    private List<FloatingElement> stars;
    private List<FloatingElement> clouds;
    private Texture starTexture;
    private Texture cloudTexture;
    
    private static class FloatingElement {
        Vector2 position;
        Vector2 velocity;
        float size;
        float rotation;
        float rotationSpeed;
        float opacity;
        float pulseSpeed;
        float pulsePhase;
        
        public FloatingElement(Vector2 position, Vector2 velocity, float size) {
            this.position = position;
            this.velocity = velocity;
            this.size = size;
            this.rotation = MathUtils.random(0f, 360f);
            this.rotationSpeed = MathUtils.random(-20f, 20f);
            this.opacity = MathUtils.random(0.3f, 1.0f);
            this.pulseSpeed = MathUtils.random(0.5f, 2f);
            this.pulsePhase = MathUtils.random(0f, MathUtils.PI2);
        }
    }
    
    @Override
    public void create() {
        super.create();
        
        // Load textures
        starTexture = ServiceLocator.getResourceService()
            .getAsset("images/star.png", Texture.class);
        cloudTexture = ServiceLocator.getResourceService()
            .getAsset("images/sun.png", Texture.class);
        
        // Initialize stars
        stars = new ArrayList<>();
        for (int i = 0; i < NUM_STARS; i++) {
            Vector2 pos = new Vector2(
                MathUtils.random(0f, 20f),
                MathUtils.random(0f, 15f)
            );
            Vector2 vel = new Vector2(
                MathUtils.random(-0.5f, 0.5f),
                MathUtils.random(-0.3f, 0.3f)
            );
            stars.add(new FloatingElement(pos, vel, STAR_SIZE));
        }
        
        // Initialize clouds
        clouds = new ArrayList<>();
        for (int i = 0; i < NUM_CLOUDS; i++) {
            Vector2 pos = new Vector2(
                MathUtils.random(0f, 20f),
                MathUtils.random(0f, 15f)
            );
            Vector2 vel = new Vector2(
                MathUtils.random(0.2f, 0.8f),
                MathUtils.random(-0.1f, 0.1f)
            );
            clouds.add(new FloatingElement(pos, vel, CLOUD_SIZE));
        }
    }
    
    @Override
    public void update() {
        super.update();
        
        float deltaTime = ServiceLocator.getTimeSource() != null ? 
            ServiceLocator.getTimeSource().getDeltaTime() : 0.016f;
        
        // Update stars
        for (FloatingElement star : stars) {
            updateElement(star, deltaTime, 20f, 15f);
        }
        
        // Update clouds
        for (FloatingElement cloud : clouds) {
            updateElement(cloud, deltaTime, 20f, 15f);
        }
    }
    
    private void updateElement(FloatingElement element, float deltaTime, float maxX, float maxY) {
        // Update position
        element.position.add(element.velocity.x * deltaTime, element.velocity.y * deltaTime);
        
        // Wrap around screen edges
        if (element.position.x < -1) {
            element.position.x = maxX + 1;
        } else if (element.position.x > maxX + 1) {
            element.position.x = -1;
        }
        
        if (element.position.y < -1) {
            element.position.y = maxY + 1;
        } else if (element.position.y > maxY + 1) {
            element.position.y = -1;
        }
        
        // Update rotation
        element.rotation += element.rotationSpeed * deltaTime;
        
        // Update pulse phase for twinkling effect
        element.pulsePhase += element.pulseSpeed * deltaTime;
    }
    
    @Override
    public void draw(SpriteBatch batch) {
        // Draw clouds first (background layer)
        for (FloatingElement cloud : clouds) {
            drawElement(batch, cloudTexture, cloud, 0.3f);
        }
        
        // Draw stars on top
        for (FloatingElement star : stars) {
            // Calculate twinkling opacity
            float twinkle = (MathUtils.sin(star.pulsePhase) + 1f) / 2f;
            float opacity = star.opacity * (0.5f + 0.5f * twinkle);
            drawElement(batch, starTexture, star, opacity);
        }
    }
    
    private void drawElement(SpriteBatch batch, Texture texture, FloatingElement element, float opacity) {
        float originX = element.size / 2f;
        float originY = element.size / 2f;
        
        // Set color with opacity
        batch.setColor(1f, 1f, 1f, opacity);
        
        batch.draw(
            texture,
            element.position.x - originX,
            element.position.y - originY,
            originX,
            originY,
            element.size,
            element.size,
            1f,
            1f,
            element.rotation,
            0,
            0,
            texture.getWidth(),
            texture.getHeight(),
            false,
            false
        );
        
        // Reset color
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    @Override
    public int getLayer() {
        return 0; // Background layer
    }
    
    @Override
    public float getZIndex() {
        return -10f; // Behind everything else
    }
}

