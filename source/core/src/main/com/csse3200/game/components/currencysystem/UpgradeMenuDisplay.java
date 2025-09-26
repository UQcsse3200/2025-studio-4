package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeMenuDisplay.class);
    private final GdxGame game;

    private Table rootTable;

    public UpgradeMenuDisplay(GdxGame game) {
        super();
        this.game = game;
        logger.debug("Created upgrade screen");
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        logger.debug("Adding actors to Upgrade Screen");
        // Background
        Texture bg = ServiceLocator.getResourceService().getAsset("images/main_menu_background.png", Texture.class);
        Image bgImage = new Image(bg);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Title
        Label title = new Label("Upgrades", skin, "title");

        Table upgradeTable = makeUpgradeTable();
        Table menuBtns = makeMenuBtns();

        Table panel = new Table(skin);
        panel.add(upgradeTable).row();
        panel.add(menuBtns).fillX();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(title).expandX().top().padTop(20f);
        rootTable.row().padTop(30f);
        rootTable.add(panel).center()
                .width(Math.min(Gdx.graphics.getWidth() * 0.55f, 720f));

        stage.addActor(rootTable);
    }

    private Table makeUpgradeTable() {
        Table table = new Table();

        Label label = new Label("hi", skin);

        table.add(label);
        table.padBottom(20f);

        return table;
    }

    private Table makeMenuBtns() {
        // Create custom button style
        TextButton.TextButtonStyle customButtonStyle = createCustomButtonStyle();

        TextButton backBtn = new TextButton("Back", customButtonStyle);

        // Set button size
        float buttonWidth = 150f;
        float buttonHeight = 50f;

        backBtn.getLabel().setColor(Color.WHITE);

        backBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Back button clicked");
                        exitMenu();
                    }
                });

        Table table = new Table();
        table.add(backBtn).size(buttonWidth, buttonHeight).expandX().left().pad(0f, 15f, 15f, 0f);
        return table;
    }

    /**
     * Create custom button style using button background image
     */
    private TextButton.TextButtonStyle createCustomButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        // Use Segoe UI font
        style.font = skin.getFont("segoe_ui");

        // Load button background image
        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);

        // Create NinePatch for scalable button background
        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

        // Create pressed state NinePatch (slightly darker)
        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        // Create hover state NinePatch (slightly brighter)
        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        // Set button states
        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);

        // Set font colors
        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // draw stage
    }

    private void exitMenu() {
        game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
