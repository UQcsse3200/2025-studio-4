package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

public class clickable extends Component{
    private float clickRadius;

    public clickable(float clickRadius) {
        this.clickRadius = clickRadius;
    }
    
    @Override
    public void update() {
        if (Gdx.input.justTouched()) {
            Vector2 entityPos = entity.getPosition();
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
                        
            // Get camera from Renderer
            Camera camera = getCamera();
            if (camera != null) {
                // Convert screen coordinates to world coordinates
                Vector3 worldClickPos = new Vector3(screenX, screenY, 0);
                camera.unproject(worldClickPos);
                
                Entity player = findPlayerEntity();

                // Check if click is close to enemy
                if (Math.abs(worldClickPos.x - (entityPos.x + clickRadius/2)) < clickRadius &&
                    Math.abs(worldClickPos.y - (entityPos.y + clickRadius)) < clickRadius) {
                    //entity.getComponent(CombatStatsComponent.class).addHealth(-10, DamageTypeConfig.None);

                    DeckComponent deck = entity.getComponent(DeckComponent.EnemyDeckComponent.class);

                    if (player != null) {
                       player.getEvents().trigger("displayDeck", deck); 
                    }                    
                }
            }
        }
    }
    
    /**
     * Get the camera from the current Renderer.
     */
    private Camera getCamera() {
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null) {
            return renderer.camera.getCamera(); // CameraComponent.getCamera() returns the LibGDX Camera
        }
        return null;
    }

    /**
     * Gets a safe copy of all entities.
     *
     * @return array of entities, or null if unavailable
     */
    private Array<Entity> safeEntities()
    {
        try
        {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Finds the player entity (with a currency manager).
     *
     * @return the player entity, or null if not found
     */
    private Entity findPlayerEntity()
    {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities)
        {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        }
        return null;
    }

    public float getClickRadius() {
        return this.clickRadius;
    }
}