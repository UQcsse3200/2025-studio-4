package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;

public class SimplePlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;     // debounce so button click can’t also place
    private String pendingType = "base";
    private OrthographicCamera camera;

    @Override
    public void create() {
        // listen to two separate events (no payloads needed)
        entity.getEvents().addListener("startPlacementBase", this::armBase);
        entity.getEvents().addListener("startPlacementSun", this::armSun);

        // find a world camera once
        ServiceLocator.getEntityService().getEntitiesCopy().forEach(e -> {
            CameraComponent cc = e.getComponent(CameraComponent.class);
            if (cc != null && cc.getCamera() instanceof OrthographicCamera && camera == null) {
                camera = (OrthographicCamera) cc.getCamera();
            }
        });
        if (camera == null) System.out.println("!!! No world camera found (CameraComponent).");
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
        if (!placementActive || camera == null) return;

        // wait until the mouse/finger is released after pressing the UI button
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(v);
            Vector2 world = new Vector2(v.x, v.y);
            System.out.println(">>> placing " + pendingType + " tower at " + world);

            Entity tower = "sun".equalsIgnoreCase(pendingType)
                    ? TowerFactory.createSunTower()
                    : TowerFactory.createBaseTower();

            tower.setPosition(world);
            ServiceLocator.getEntityService().register(tower);

            placementActive = false; // one placement per button tap
        }
    }
}
