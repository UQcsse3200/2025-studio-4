package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UpgradeMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeMenuDisplay.class);
    private final GdxGame game;

    private Label starsLabel;
    private Map<GameStateService.HeroType, TextButton> heroButtons;

    private GameStateService gameState;
    private Map<GameStateService.HeroType, Boolean> heroUnlocks;

    private TextButton.TextButtonStyle btnStyle;
    private Label.LabelStyle nameStyle;

    private static final String HERO_IMG = "images/hero/Heroshoot.png";
    private static final String ENG_IMG = "images/engineer/Engineer.png";

    private final float IMG_SIZE = 96f;

    private static final int ENGINEER_COST = 3;

    public UpgradeMenuDisplay(GdxGame game) {
        super();
        this.game = game;
        logger.debug("Created upgrade screen");
    }

    @Override
    public void create() {
        super.create();

        gameState = ServiceLocator.getGameStateService();
        heroUnlocks = gameState.getHeroUnlocks();

        // ===== styles (match main menu) =====
        btnStyle = createCustomButtonStyle();
        nameStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), Color.WHITE);

        heroButtons = new HashMap<>();

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
        Label title = new Label("Unlocks", skin, "title");

        Table upgradeTable = makeUpgradeTable();
        Table menuBtns = makeMenuBtns();

        // stars display
        Image starImage = new Image(
                ServiceLocator.getResourceService().getAsset(
                        "images/star.png",
                        Texture.class
                )
        );
        starsLabel = new Label(
                Integer.toString(ServiceLocator.getGameStateService().getStars()),
                skin,
                "large"
        );
        logger.info("Number of stars: {}", ServiceLocator.getGameStateService().getStars());

        Table panel = new Table(skin);
        panel.add(upgradeTable).row();
        panel.add(menuBtns).fillX();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(title).expandX().top().padTop(20f).row();
        HorizontalGroup group = new HorizontalGroup();
        group.space(5);
        group.addActor(starImage);
        group.addActor(starsLabel);
        rootTable.add(group);
        rootTable.row().padTop(30f);
        rootTable.add(panel).center()
                .width(Math.min(Gdx.graphics.getWidth() * 0.55f, 720f));

        stage.addActor(rootTable);
    }

    private Table makeHeroCard(GameStateService.HeroType heroType, String heroName, Image heroImage, Integer heroCost) {
        String heroBtnText = "Unlock";
        if (heroUnlocks.get(heroType)) {
            if (gameState.getSelectedHero() == heroType) {
                heroBtnText = "Selected";
            } else {
                heroBtnText = "Select";
            }
        }

        TextButton heroBtn = new TextButton(heroBtnText, btnStyle);
        heroBtn.addListener(e -> {
            if (!heroBtn.isPressed()) return false;
            if (heroUnlocks.get(heroType)) {
                gameState.setSelectedHero(heroType);

                for (GameStateService.HeroType btnHeroType : heroButtons.keySet()) {
                    TextButton button = heroButtons.get(btnHeroType);
                    if (heroUnlocks.get(btnHeroType)) {
                        if (btnHeroType == heroType) {
                            button.setText("Selected");
                        } else {
                            button.setText("Select");
                        }
                    }
                }
            } else {
                if (gameState.spendStars(heroCost)) {
                    gameState.setHeroUnlocked(heroType);
                    heroBtn.setText("Select");
                    starsLabel.setText(gameState.getStars());
                }
            }
            return true;
        });

        heroButtons.put(heroType, heroBtn);

        Table heroCol = new Table();
        heroCol.defaults().pad(6f).center();
        heroCol.add(heroImage).size(IMG_SIZE).padBottom(6f).row();
        heroCol.add(new Label(heroName, nameStyle)).padBottom(6f).row();

        // star cost
        HorizontalGroup heroStarCost = new HorizontalGroup();
        heroStarCost.space(5);
        heroStarCost.addActor(new Image(
                ServiceLocator.getResourceService().getAsset(
                        "images/star.png",
                        Texture.class
                )
        ));
        heroStarCost.addActor(new Label(Integer.toString(heroCost), nameStyle));

        heroCol.add(heroStarCost).padBottom(6f).row();
        heroCol.add(heroBtn).width(220f).height(56f);

        return heroCol;
    }

    private Table makeUpgradeTable() {
        // ===== hero card =====
        Image heroImg = new Image(ServiceLocator.getResourceService().getAsset(HERO_IMG, Texture.class));
        heroImg.setSize(IMG_SIZE, IMG_SIZE);

        Table heroCol = makeHeroCard(
                GameStateService.HeroType.HERO,
                "Hero",
                heroImg,
                0
        );

        // ===== engineer card =====
        Image engImg = new Image(ServiceLocator.getResourceService().getAsset(ENG_IMG, Texture.class));
        engImg.setSize(IMG_SIZE, IMG_SIZE);

        Table engCol = makeHeroCard(
                GameStateService.HeroType.ENGINEER,
                "Engineer",
                engImg,
                ENGINEER_COST
        );

        // ===== cards row (horizontal) =====
        Table cardsRow = new Table();
        cardsRow.defaults().pad(16f).top();
        cardsRow.add(heroCol).padRight(24f);
        cardsRow.add(engCol).padLeft(24f);

        return cardsRow;
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
        table.add(backBtn).size(buttonWidth, buttonHeight).expandX().center().pad(0f, 15f, 15f, 0f);
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
