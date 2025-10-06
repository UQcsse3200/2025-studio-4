package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.rendering.Renderer;


/**
 * A component that marks an entity as collectible by the player.
 *
 * Entities with this component can be picked up or collected during gameplay.
 */
public class CollectibleComponent extends Component {
    private boolean isCollected;
    private float clickRadius = 0.6f;

    /**
     * Creates a new collectible component, initially not collected.
     */
    public CollectibleComponent() {
        isCollected = false;
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
        if (Gdx.input.justTouched()) {
            Vector2 entityPos = entity.getPosition();
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();

            Vector3 worldClickPos = new Vector3(screenX, screenY, 0);
            Camera camera = this.getCamera();
            camera.unproject(worldClickPos);

            if (Math.abs(worldClickPos.x - entityPos.x) < this.clickRadius &&
                    Math.abs(worldClickPos.y - entityPos.y) < this.clickRadius) {
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
