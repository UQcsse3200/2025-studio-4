package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;


/**
 * A component that marks an entity as collectible by the player.
 *
 * Entities with this component can be picked up or collected during gameplay.
 */
public class CollectibleComponent extends Component {
    private boolean isCollected;
    private float clickRadius = 0.6f;
    private float lifetimeSeconds = 0f; // <= 0 means infinite
    // Enable hover pickup with a slightly larger radius
    private float hoverRadius = 0.75f;

    /**
     * Creates a new collectible component, initially not collected.
     */
    public CollectibleComponent() {
        isCollected = false;
    }

    /**
     * Creates a collectible component with a finite lifetime.
     * @param lifetimeSeconds time to live; <= 0 means infinite
     */
    public CollectibleComponent(float lifetimeSeconds) {
        this();
        this.lifetimeSeconds = lifetimeSeconds;
    }

    /**
     * Handles player interaction for collecting the entity.
     *
     * When the player clicks within the defined click radius of the entity,
     * this method marks the entity as collected, triggers a "collectCurrency" event,
     * and schedules the entity for disposal.
     */
    @Override
    public void update () {
        if (isCollected) {
            return; // already processed
        }

        float dt = ServiceLocator.getTimeSource() != null ? ServiceLocator.getTimeSource().getDeltaTime() : 0f;

        // Auto-despawn if lifetime is set
        if (lifetimeSeconds > 0f) {
            lifetimeSeconds -= dt;
            if (lifetimeSeconds <= 0f) {
                Gdx.app.postRunnable(entity::dispose);
                return;
            }
        }

        Camera camera = this.getCamera();
        if (camera == null) {
            return; // cannot unproject without a camera
        }

        // Current mouse position in world coords
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        camera.unproject(worldPos);

        Vector2 entityPos = entity.getPosition();
        // Hover pickup
        if (Math.abs(worldPos.x - entityPos.x) < hoverRadius &&
            Math.abs(worldPos.y - entityPos.y) < hoverRadius) {
            isCollected = true;
            entity.getEvents().trigger("collectCurrency", this.entity);
            Gdx.app.postRunnable(entity::dispose);
            return;
        }

        // Click pickup (kept for players who still click)
        if (Gdx.input.justTouched()) {
            if (Math.abs(worldPos.x - entityPos.x) < this.clickRadius &&
                Math.abs(worldPos.y - entityPos.y) < this.clickRadius) {
                isCollected = true;
                entity.getEvents().trigger("collectCurrency", this.entity);
                Gdx.app.postRunnable(entity::dispose);
            }
        }
    }

    /**
     * Get the camera from the current Renderer.
     *
     * @return the active Camera, or null if no renderer/camera is available
     */
    private Camera getCamera() {
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.getCamera() != null) {
            return renderer.getCamera().getCamera(); // CameraComponent.getCamera() returns the LibGDX Camera
        }
        return null;
    }

    /**
     * Returns true if it has been collected, false otherwise.
     *
     * @return isCollected
     */
    public boolean isCollected() {
        return isCollected;
    }
}
