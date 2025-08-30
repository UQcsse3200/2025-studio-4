package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.terrain.TerrainComponent;
/**
 * Simple controller to place towers in the world by clicking. The controller listens for events
 * "startPlacementBase" and "startPlacementSun" to arm placement of the respective tower type. After
 * arming, the next left mouse click in the world will place the tower at the clicked location,
 * provided there is no other tower too close. Placement is then automatically disarmed.
 *
 * <p>This component must be added to an entity that is always active, e.g. the UI entity.
 */

public class SimplePlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;   // debounce after pressing UI button
    private String pendingType = "base";
    private OrthographicCamera camera;

    // Spacing to avoid overlap
    private float minSpacing = 1.0f;       // fallback if no terrain

    @Override
    public void create() {
        // Two separate events mapped to two tower types
        entity.getEvents().addListener("startPlacementBase", this::armBase);
        entity.getEvents().addListener("startPlacementSun", this::armSun);


        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    private void armBase() {
        pendingType = "base";
        placementActive = true;
        needRelease = true;
        System.out.println(">>> placement ON (Base)");
    }

    private void armSun() {
        pendingType = "sun";
        placementActive = true;
        needRelease = true;
        System.out.println(">>> placement ON (Sun)");
    }

    @Override
    public void update() {
        // If camera wasn’t found at create(), keep trying each frame (robust on startup order)
        if (camera == null) findWorldCamera();

        if (!placementActive || camera == null) return;

        // Debounce: wait until the button press is fully released
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(v);
            Vector2 world = new Vector2(v.x, v.y);

            // Overlap guard (null-safe)
            if (!isPositionFree(world, minSpacing)) {
                System.out.println(">>> blocked: cannot place " + pendingType + " at " + world);
                placementActive = false; // exit on failed attempt
                return;
            }

            // Create tower via factory
            Entity tower = "sun".equalsIgnoreCase(pendingType)
                    ? TowerFactory.createSunTower()
                    : TowerFactory.createBaseTower();

            tower.setPosition(world);
            ServiceLocator.getEntityService().register(tower);
            System.out.println(">>> placed " + pendingType + " at " + world);

            placementActive = false; // one placement per button tap
        }
    }

    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy(); // returns Array<Entity>
        } catch (Exception ex) {
            System.out.println("!!! getEntitiesCopy failed: " + ex.getMessage());
            return null;
        }
    }

    private void findWorldCamera() {
        Array<Entity> all = safeEntities();
        if (all == null) return;
        for (int i = 0; i < all.size; i++) {
            Entity e = all.get(i);
            if (e == null) continue;
            CameraComponent cc = e.getComponent(CameraComponent.class);
            if (cc != null && cc.getCamera() instanceof OrthographicCamera) {
                camera = (OrthographicCamera) cc.getCamera();
                System.out.println(">>> world camera found; vp=" +
                        camera.viewportWidth + "x" + camera.viewportHeight);
                return;
            }
        }
    }

    private boolean isPositionFree(Vector2 candidate, float spacing) {
        Array<Entity> all = safeEntities();
        if (all == null || candidate == null || !Float.isFinite(spacing)) return true;

        float spacing2 = spacing * spacing;
        for (int i = 0; i < all.size; i++) {
            Entity e = all.get(i);
            if (e == null || e == entity) continue; // skip our UI/controller entity
            if (e.getComponent(TowerComponent.class) == null) continue; // only compare with towers

            Vector2 pos = e.getPosition();
            if (pos == null || !Float.isFinite(pos.x) || !Float.isFinite(pos.y)) continue;

            float dx = pos.x - candidate.x;
            float dy = pos.y - candidate.y;
            if (dx * dx + dy * dy < spacing2) return false;
        }
        return true;
    }

}
