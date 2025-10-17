package com.csse3200.game.components.book;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main UI component for the book display menu.
 * <p>
 * Renders buttons to navigate to different book pages (enemies, currencies, towers),
 * displays the background, and provides a back button to exit the book menu.
 */
public class MainBookDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplay.class);

    /** Background images for buttons: enemies, currencies, towers, back button. */
    private final String[] buttonBackGround = {
            "images/book/enemies_book.png",
            "images/book/currencies_book.png",
            "images/book/towers_book.png",
            "images/book/hologram.png",
            "images/book/heroes_book.png"
    };

    private Table contentTable;
    private Table exitTable;

    /**
     * Constructs the main book display UI component.
     */
    public MainBookDisplay() {
        super();
    }

    /**
     * Called when the component is created.
     * Initializes and renders all UI elements.
     */
    @Override
    public void create() {
        super.create();
        addActors();
    }

    /**
     * Adds all UI actors (background, buttons, exit button) to the stage.
     */
    void addActors() {
        this.renderBackGround();
        this.renderContentList();
        this.renderExitButton();
        //applyUiScale();
    }

    /**
     * Apply UI scale from user settings
     */
    private void applyUiScale() {
        UserSettings.Settings settings = UserSettings.get();

        // Scale content table (center buttons)
        if (contentTable != null) {
            contentTable.setTransform(true);
            contentTable.validate();
            contentTable.setOrigin(contentTable.getWidth() / 2f, contentTable.getHeight() / 2f);
            contentTable.setScale(settings.uiScale);
        }

        // Scale exit table (top-right)
        if (exitTable != null) {
            exitTable.setTransform(true);
            exitTable.validate();
            exitTable.setOrigin(exitTable.getWidth(), exitTable.getHeight());
            exitTable.setScale(settings.uiScale);
        }
    }

    /** Renders the main background image of the book menu. */
    private void renderBackGround() {
        Image backgroundImage =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/book/encyclopedia_theme.png", Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
    }

    /** Renders the navigation buttons for enemies, currencies, and towers. */
    private void renderContentList() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        // Scale buttons relative to stage size
        float buttonWidth = stageWidth * 0.3f;   // 8% of stage width
        float buttonHeight = stageHeight * 0.4f; // 12% of stage height

        Table table = new Table();
        table.setFillParent(true);

        TextButton.TextButtonStyle enemyButtonStyle = createCustomButtonStyle(buttonBackGround[0]);
        TextButton.TextButtonStyle currencyButtonStyle = createCustomButtonStyle(buttonBackGround[1]);
        TextButton.TextButtonStyle towerButtonStyle = createCustomButtonStyle(buttonBackGround[2]);
        TextButton.TextButtonStyle heroButtonStyle = createCustomButtonStyle(buttonBackGround[4]);

        TextButton enemyButton = new TextButton("", enemyButtonStyle);
        enemyButton.getLabel().setColor(Color.WHITE);
        enemyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Go to enemy clicked");
                entity.getEvents().trigger("goToEnemy");
            }
        });

        TextButton currencyButton = new TextButton("", currencyButtonStyle);
        currencyButton.getLabel().setColor(Color.WHITE);
        currencyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Go to currency clicked");
                entity.getEvents().trigger("goToCurrency");
            }
        });

        TextButton towerButton = new TextButton("", towerButtonStyle);
        towerButton.getLabel().setColor(Color.WHITE);
        towerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Go to tower clicked");
                entity.getEvents().trigger("goToTower");
            }
        });

        TextButton heroButton = new TextButton("", heroButtonStyle);
        heroButton.getLabel().setColor(Color.WHITE);
        heroButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                logger.debug("Go to hero clicked");
                entity.getEvents().trigger("goToHero");
            }
        });

        table.row().padTop(stageHeight * 0.02f);
        table.add(enemyButton).size(buttonWidth, buttonHeight).padRight(stageWidth * 0.01f);
        table.add(currencyButton).size(buttonWidth, buttonHeight).padRight(stageWidth * 0.01f);
        table.row().padTop(stageHeight * 0.02f);
        table.add(towerButton).size(buttonWidth, buttonHeight);
        table.add(heroButton).size(buttonWidth, buttonHeight);

        table.row().padTop(stageHeight * 0.01f).padBottom(stageHeight * 0.03f);

        stage.addActor(table);
    }

    /** Renders the back/exit button to return to the main menu. */
    private void renderExitButton() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        // Scale button size relative to stage size
        float buttonWidth = stageWidth * 0.15f;   // 5% of stage width
        float buttonHeight = stageHeight * 0.24f; // 8% of stage height

        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle(buttonBackGround[3]);
        TextButton exitButton = new TextButton("", exitButtonStyle);
        exitButton.getLabel().setColor(Color.WHITE);

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Back button clicked");
                entity.getEvents().trigger("backToMain");
            }
        });

        Table exitTable = new Table();
        exitTable.top().right();
        exitTable.setFillParent(true);

        // Add button with scaled size and smaller padding
        exitTable.add(exitButton).size(buttonWidth, buttonHeight).pad(stageWidth * 0.01f);

        stage.addActor(exitTable);
    }

    /**
     * Helper method to create a custom button style using a texture.
     *
     * @param backGround the texture path for the button
     * @return configured TextButtonStyle
     */
    private TextButton.TextButtonStyle createCustomButtonStyle(String backGround) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = skin.getFont("segoe_ui");

        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset(backGround, Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);

        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(buttonPatch);
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    /**
     * Draw method overridden from UIComponent.
     * Drawing is handled by the stage, so this is intentionally left empty.
     */
    @Override
    protected void draw(SpriteBatch batch) {
        // Empty
    }
}
