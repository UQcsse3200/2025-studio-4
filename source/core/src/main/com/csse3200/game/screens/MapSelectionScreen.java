package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.mainmenu.MapSelectionActions;
import com.csse3200.game.components.mainmenu.MapSelectionDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapSelectionScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MapSelectionScreen.class);

    private static final String[] PRELOAD_TEXTURES = new String[] {
            // Background used by menu screens
            "images/main_menu_background.png",
            // 🔶 Orange button texture used by UIStyleHelper.orangeButtonStyle()
            "images/Main_Menu_Button_Background.png"
    };

    private final GdxGame game;
    private Stage stage;

    public MapSelectionScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        logger.debug("Initialising MapSelectionScreen services");

        // --- Core services (register only if not present to avoid clobbering) ---
        if (ServiceLocator.getRenderService() == null) {
            ServiceLocator.registerRenderService(new RenderService());
        }
        if (ServiceLocator.getEntityService() == null) {
            ServiceLocator.registerEntityService(new EntityService());
        }
        if (ServiceLocator.getInputService() == null) {
            ServiceLocator.registerInputService(new InputService());
        }
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }

        // Preload required textures so UIStyleHelper can fetch them safely
        ServiceLocator.getResourceService().loadTextures(PRELOAD_TEXTURES);
        ServiceLocator.getResourceService().loadAll();

        // Stage (simple stage via RenderService, consistent with other menus)
        stage = new Stage();
        ServiceLocator.getRenderService().setStage(stage);

        // UI Entity (view + actions)
        Entity ui = new Entity()
                .addComponent(new MapSelectionDisplay())   // handles layout & button placement
                .addComponent(new MapSelectionActions(game));
        ServiceLocator.getEntityService().register(ui);

        // Route input to the stage
        ServiceLocator.getInputService().register(new InputDecorator(stage, 10));
    }

    @Override
    public void render(float delta) {
        // Update ECS (drives the display) then draw the stage
        ServiceLocator.getEntityService().update();
        stage.act(delta > 0 ? delta : Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing MapSelectionScreen");

        if (stage != null) {
            stage.dispose();
            stage = null;
        }

        // Dispose only what we created in show(); other global services are shared
        if (ServiceLocator.getRenderService() != null) {
            ServiceLocator.getRenderService().dispose();
        }
        if (ServiceLocator.getEntityService() != null) {
            ServiceLocator.getEntityService().dispose();
        }
        // Intentionally not clearing InputService/ResourceService here (shared across screens)
    }
}
