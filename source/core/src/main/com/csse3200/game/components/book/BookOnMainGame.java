package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;

/**
 * Fullscreen overlay with a background image and a single "Back to game" button.
 * It pauses the game on show and resumes on close.
 */
public class BookOnMainGame extends Window {
    private final Skin skin;
    private TextButton backBtn;
    private Stage stage = ServiceLocator.getRenderService().getStage();

    /** Background images for buttons: enemies, currencies, towers, back button. */
    private final String[] buttonBackGround = {
            "images/book/enemies_book.png",
            "images/book/currencies_book.png",
            "images/book/towers_book.png",
            "images/book/hologram.png",
            "images/book/heroes_book.png"
    };

    String mainBackgroundPathName = "images/book/encyclopedia_theme.png";
    String bookBackgroundPathName = "images/book/open_book_theme.png";

    /**
     * @param skin UI skin
     */
    public BookOnMainGame(Skin skin) {
        super("", skin);
        this.skin = skin;

        setModal(true);
        setMovable(false);
        setFillParent(true);
        pad(0);

        this.renderBackGround(this.mainBackgroundPathName);
        this.setPage(buildHomePage());
        this.renderExitButton();
    }

    private void renderBackGround(String pathName) {
        String backgroundPath = pathName;
        // String backgroundPath = "images/book/encyclopedia_theme.png";

        // Background image
        try {
            Texture bg = ServiceLocator.getResourceService().getAsset(backgroundPath, Texture.class);
            setBackground(new TextureRegionDrawable(new TextureRegion(bg)));
        } catch (Exception ignored) {
            // No background found, continue without it
        }
    }

    private void renderExitButton() {
        float screenW = 300;
        float screenH = 300;

        float buttonWidth = screenW;
        float buttonHeight = screenH;

        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle(buttonBackGround[3]);
        TextButton exitButton = new TextButton("", exitButtonStyle);
        exitButton.getLabel().setColor(Color.WHITE);
        exitButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { dismiss(); }
        });

        // Container that positions the button at top-right
        Table exitTable = new Table();
        exitTable.setFillParent(true);
        exitTable.top().right();
        exitTable.add(exitButton)
                .size(buttonWidth, buttonHeight)
                .pad(screenW * 0.01f);

        addActor(exitTable);
    }

    private void setPage(Actor page) {
        Actor old = findActor("pageRoot");
        if (old != null) old.remove();
        page.setName("pageRoot");
        addActor(page);
    }

    /** Renders the navigation buttons for enemies, currencies, and towers. */
    private Table buildHomePage() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        // Scale buttons relative to stage size
        float buttonWidth = stageWidth * 0.2f;   // 8% of stage width
        float buttonHeight = stageHeight * 0.26f; // 12% of stage height

        Table table = new Table();
        table.setFillParent(true);

        TextButton.TextButtonStyle enemyButtonStyle    = createCustomButtonStyle(buttonBackGround[0]);
        TextButton.TextButtonStyle currencyButtonStyle = createCustomButtonStyle(buttonBackGround[1]);
        TextButton.TextButtonStyle towerButtonStyle    = createCustomButtonStyle(buttonBackGround[2]);
        TextButton.TextButtonStyle heroButtonStyle     = createCustomButtonStyle(buttonBackGround[4]);

        TextButton enemyButton    = new TextButton("", enemyButtonStyle);
        TextButton currencyButton = new TextButton("", currencyButtonStyle);
        TextButton towerButton    = new TextButton("", towerButtonStyle);
        TextButton heroButton     = new TextButton("", heroButtonStyle);

        enemyButton.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ setPage(buildEnemyPage());    }});
        currencyButton.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ setPage(buildCurrencyPage()); }});
        towerButton.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ setPage(buildTowerPage());    }});
        heroButton.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ setPage(buildHeroPage());     }});

        table.row().padTop(stageHeight * 0.02f); // First row of books
        table.add(enemyButton).size(buttonWidth, buttonHeight);
        table.add(currencyButton).size(buttonWidth, buttonHeight);
        table.add(towerButton).size(buttonWidth, buttonHeight);
        table.row().padTop(stageHeight * 0.02f); // Second row of books
        table.add(heroButton).size(buttonWidth, buttonHeight);
        table.row().padTop(stageHeight * 0.01f).padBottom(stageHeight * 0.03f);

        return table;
    }

    private Table buildEnemyPage()    { return buildSectionPage("Enemies",    new Label("enemy details here", skin)); }
    private Table buildCurrencyPage() { return buildSectionPage("Currencies", new Label("currency details here", skin)); }
    private Table buildTowerPage()    { return buildSectionPage("Towers",     new Label("tower details here", skin)); }
    private Table buildHeroPage()     { return buildSectionPage("Heroes",     new Label("hero details here", skin)); }

    // Helper: build a drawable from your ResourceService-managed textures
    private Drawable makeBg(String path) {
        Texture tex = ServiceLocator.getResourceService().getAsset(path, Texture.class);
        if (tex != null) return new TextureRegionDrawable(new TextureRegion(tex));
        // fallback: plain color panel if not loaded
        return skin.newDrawable("white", new Color(0f,0f,0f,0.4f));
    }

    private TextButton renderBackButton() {
        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle("images/book/bookmark.png");
        TextButton back = new TextButton("", exitButtonStyle);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setPage(buildHomePage());
            }
        });
        return back;
    }

    private Table buildSectionPage(String title, Actor body) {
        Table page = new Table();
        page.setFillParent(true);
        page.setName("pageRoot");

        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();
        float buttonWidth = stageWidth * 0.15f;
        float buttonHeight = stageHeight * 0.24f;

        page.setBackground(this.makeBg(this.bookBackgroundPathName));
        TextButton back = renderBackButton();

        Table pin = new Table();
        pin.setFillParent(true);   // covers the whole page
        pin.top().right();         // anchor
        pin.add(back)
                .size(buttonWidth, buttonHeight)
                .padTop(stageHeight * 0.02f)
                .padRight(stageWidth * 0.125f);

        page.addActor(pin);

        return page;
    }

    public void showOn(Stage stage) {
        // Find the entity with PauseInputComponent and trigger its pause
        triggerPauseSystem(true);

        stage.addActor(this);
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f),
                Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void dismiss() {
        triggerPauseSystem(false);
        addAction(Actions.sequence(
                Actions.parallel(Actions.alpha(0f, 0.12f), Actions.scaleTo(0.98f, 0.98f, 0.12f)),
                Actions.removeActor()
        ));
    }

    /** Pause/resume via PauseInputComponent if present, else fall back to timeScale */
    private void triggerPauseSystem(boolean pause) {
        try {
            if (ServiceLocator.getEntityService() == null) {
                // Fallback to direct time scale manipulation
                ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
                return;
            }

            com.badlogic.gdx.utils.Array<com.csse3200.game.entities.Entity> all =
                    ServiceLocator.getEntityService().getEntities();

            // Find the entity with PauseInputComponent
            for (int i = 0; i < all.size; i++) {
                com.csse3200.game.entities.Entity entity = all.get(i);
                if (entity.getComponent(com.csse3200.game.components.maingame.PauseInputComponent.class) != null) {
                    // Found it! Trigger the event it listens for
                    if (pause) {
                        // Check if already paused by checking time scale
                        if (ServiceLocator.getTimeSource().getTimeScale() > 0) {
                            entity.getEvents().trigger("togglePause");
                        }
                    } else {
                        entity.getEvents().trigger("resume");
                    }
                    return;
                }
            }

            // If we didn't find PauseInputComponent, fall back to direct time scale manipulation
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);

        } catch (Exception e) {
            // Fallback to direct time scale manipulation if anything fails
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
        }
    }

    /**
     * Helper method to create a custom button style using a texture.
     *
     * @param backGround the texture path for the button
     * @return configured TextButtonStyle
     */
    private TextButton.TextButtonStyle createCustomButtonStyle(String backGround) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = skin.getFont("default-font");

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
}