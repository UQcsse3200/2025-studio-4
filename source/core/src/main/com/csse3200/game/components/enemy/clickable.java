package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;

public class clickable extends Component{
    private String name;

    public clickable(String name) {
        this.name = name;
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
                float clickRadius = 1.0f; // Adjust this value as needed (world units)
                if (Math.abs(worldClickPos.x - entityPos.x) < clickRadius && 
                    Math.abs(worldClickPos.y - entityPos.y) < clickRadius) {
                    System.out.println("*** Target Clicked! ***");
                    printDebugInfo();
                }
            } else {
                System.out.println("Camera not found!");
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

    public void printDebugInfo() {
        System.out.println("=== Hit " + name + "===");
        entity.getComponent(CombatStatsComponent.class).addHealth(-25);
        System.out.println(name + "Health: " + entity.getComponent(CombatStatsComponent.class).getHealth());
        if (entity.getComponent(CombatStatsComponent.class).isDead()) {
            System.out.println("Killed " + name);
            
        }
    }
}