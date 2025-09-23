package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.heroselect.HeroSelectActions;
import com.csse3200.game.components.heroselect.HeroSelectDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class HeroSelectScreen extends ScreenAdapter {
    private final GdxGame game;
    private Renderer renderer;

    public HeroSelectScreen(GdxGame game) {
        this.game = game;

        // 主菜单在 dispose() 里可能清过服务，这里重新注册本页所需服务
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        renderer = RenderFactory.createRenderer();

        // 如有本页专用资源，可在这里 ResourceService.loadXxx() 后再 loadAll()
        ServiceLocator.getResourceService().loadAll();

        createUI();
    }

    private void createUI() {
        Stage stage = ServiceLocator.getRenderService().getStage();

        Entity ui = new Entity()
                .addComponent(new HeroSelectDisplay())
                .addComponent(new HeroSelectActions(game))
                .addComponent(new InputDecorator(stage, 10));

        ServiceLocator.getEntityService().register(ui);
    }

    @Override
    public void render(float delta) {
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void dispose() {
        // 清理本页资源；不要 ServiceLocator.clear() 以免清掉 SelectedHeroService
        if (renderer != null) renderer.dispose();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
    }
}
