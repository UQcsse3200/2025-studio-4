package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.rendering.Renderer;

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
                
                // Check if click is close to enemy
                if (Math.abs(worldClickPos.x - (entityPos.x + clickRadius/2)) < clickRadius && 
                    Math.abs(worldClickPos.y - (entityPos.y + clickRadius)) < clickRadius) {
                    entity.getComponent(CombatStatsComponent.class).addHealth(-25, DamageTypeConfig.None);
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
}