package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
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
    private TextButton.TextButtonStyle unlockBtnStyle;
    private TextButton.TextButtonStyle selectedBtnStyle;
    private Label.LabelStyle nameStyle;
    // === Customization boxes per hero (shell only) ===
    private Map<GameStateService.HeroType, SelectBox<String>> weaponBoxes = new HashMap<>();
    private Map<GameStateService.HeroType, SelectBox<String>> soundBoxes  = new HashMap<>();

    private static final String HERO_IMG = "images/hero/Heroshoot.png";
    private static final String ENG_IMG = "images/engineer/Engineer.png";

    private static final String Sum_IMG = "images/samurai/Samurai.png";

    private final float IMG_SIZE = 96f;

    private static final int ENGINEER_COST = 3;

    private static final int SAMURAI_COST = 5;

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
                // 普通样式（与你现有主菜单一致）
                        btnStyle = createCustomButtonStyle();
                // 点击/状态样式：需要把 up/over/down 都染色以便呈现纯色按钮
                        Color green = new Color(0.20f, 1.00f, 0.20f, 1f); // 绿色：点击/刚解锁
                Color red   = new Color(1.00f, 0.30f, 0.30f, 1f); // 红色：Selected
                unlockBtnStyle   = createSolidTintStyle(green);
                selectedBtnStyle = createSolidTintStyle(red);
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
        stage.addActor(makeInstructionBanner());
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

        // 初始样式：Selected 用红色，其它用普通棕色（未解锁不变绿）
        TextButton heroBtn = new TextButton(
                heroBtnText,
                "Selected".equals(heroBtnText) ? selectedBtnStyle : btnStyle
        );
// ====== 悬停：进入→绿色；离开→按当前状态恢复 ======
        heroBtn.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // 鼠标移入，一律高亮为绿色
                heroBtn.setStyle(unlockBtnStyle);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // 鼠标移出，按状态恢复
                boolean unlocked = heroUnlocks.get(heroType);
                boolean isSelected = unlocked && gameState.getSelectedHero() == heroType;
                if (isSelected) {
                    heroBtn.setStyle(selectedBtnStyle);   // 红色（当前选中）
                } else {
                    heroBtn.setStyle(btnStyle);           // 普通（未选中 or 未解锁）
                }
            }
        });

        heroBtn.addListener(e -> {
            if (!heroBtn.isPressed()) return false;
            if (heroUnlocks.get(heroType)) {
                gameState.setSelectedHero(heroType);

                for (GameStateService.HeroType btnHeroType : heroButtons.keySet()) {
                    TextButton button = heroButtons.get(btnHeroType);
                    if (heroUnlocks.get(btnHeroType)) {
                        if (btnHeroType == heroType) {
                            button.setText("Selected");
                            // 当前选中的按钮变红
                            button.setStyle(selectedBtnStyle);
                        } else {
                            button.setText("Select");
                            // 其它已解锁按钮恢复普通样式
                             button.setStyle(btnStyle);
                        }
                    }
                }
            } else {
                if (gameState.spendStars(heroCost)) {
                    gameState.setHeroUnlocked(heroType);
                    heroBtn.setText("Select");
                    // 解锁成功：该按钮显示绿色，提示“已解锁但未选择”
                    heroBtn.setStyle(unlockBtnStyle);
                    starsLabel.setText(String.valueOf(gameState.getStars()));
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
        // 先把“Select/Unlock/Selected”按钮放上面
        heroCol.add(heroBtn).width(220f).height(56f).padBottom(8f).row();

                // ===== 所有英雄都显示两个下拉，放在按钮下面（外壳）=====
                Table customTable = buildCustomizationTable(heroType);
        heroCol.add(customTable).padBottom(6f).row();


        return heroCol;
    }
    /**
     +     * 为指定英雄构建两个下拉框（外壳）。始终显示，不依赖是否选中/解锁。
     +     */
    private Table buildCustomizationTable(GameStateService.HeroType heroType) {
                Table customTable = new Table();
             //   customTable.defaults().left().pad(2f).growX();
        // 去掉 growX，避免控件无上限扩展导致文字被挤
      //  customTable.defaults().left().pad(2f);
        // 去掉 growX，避免文字列被挤压；并固定两列宽度
                customTable.defaults().left().pad(2f);
                customTable.columnDefaults(0).width(150f).right().padRight(8f); // 标签列
                customTable.columnDefaults(1).width(240f).left();               // 选择框列
                        Label title = new Label("Hero Customization", new Label.LabelStyle(skin.getFont("segoe_ui"), Color.WHITE));
                title.setAlignment(Align.left);
              //  customTable.add(title).colspan(2).padTop(2f).padBottom(4f).row();
        customTable.add(title).colspan(2).padTop(2f).padBottom(4f).left().row();
                        // --- Weapon / Loadout 下拉 ---
                Label weaponLbl = new Label("Weapon:", nameStyle);
                SelectBox<String> weaponBox = new SelectBox<>(skin);
                switch (heroType) {
                        case SAMURAI:
                                weaponBox.setItems("Normal Sword", "Weapon 2", "Weapon 3");
                                break;
                        case ENGINEER:
                                // 外壳：名称随意，二阶段再接功能
                                        weaponBox.setItems("Loadout 1", "Loadout 2", "Loadout 3");
                                break;
                        case HERO:
                            default:
                                weaponBox.setItems("Weapon 1", "Weapon 2", "Weapon 3");
                                break;
                    }
                        weaponBox.setMaxListCount(5);
               // customTable.add(weaponLbl).width(110f).padRight(6f);
              //  customTable.add(weaponBox).height(36f).padBottom(4f).width(220f).row();
        // 标签列加宽并右对齐；选择框也稍加宽
             //       customTable.add(weaponLbl).width(150f).padRight(8f).right();
             //       customTable.add(weaponBox).height(36f).padBottom(4f).width(240f).row();
        // 列宽由 columnDefaults 控制，这里不再重复设置
                customTable.add(weaponLbl).right();
                customTable.add(weaponBox).height(36f).padBottom(4f).left().row();
                        // --- Attack Sound 下拉 ---
                        Label soundLbl = new Label("Attack Sound:", nameStyle);
                SelectBox<String> soundBox = new SelectBox<>(skin);
                soundBox.setItems("Sound 1", "Sound 2", "Sound 3");
           //     soundBox.setMaxListCount(5);
           //     customTable.add(soundLbl).width(150f).padRight(8f).right();
           //     customTable.add(soundBox).height(36f).padBottom(6f).width(240f).row();
        soundBox.setMaxListCount(5);
        customTable.add(soundLbl).right();
        customTable.add(soundBox).height(36f).padBottom(6f).left().row();
                        // 保存引用（之后二阶段可读）
                        weaponBoxes.put(heroType, weaponBox);
                soundBoxes.put(heroType, soundBox);
                return customTable;
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

        // =====  card =====
        Image sumImg = new Image(ServiceLocator.getResourceService().getAsset(Sum_IMG, Texture.class));
        sumImg.setSize(IMG_SIZE, IMG_SIZE);

        Table sumCol = makeHeroCard(
                GameStateService.HeroType. SAMURAI,
                "SAMURAI",
                sumImg,
                SAMURAI_COST
        );

        // ===== cards row (horizontal) =====
        Table cardsRow = new Table();
        cardsRow.defaults().pad(16f).top();
        cardsRow.add(heroCol).padRight(24f);
        cardsRow.add(engCol).padLeft(24f);
        cardsRow.add(sumCol).padLeft(24f);

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
    // 原默认样式（悬停更亮、按下更暗）——用于普通棕色按钮
    private TextButton.TextButtonStyle createCustomButtonStyle() {
               return createCustomButtonStyle(null, null);
            }

            /**
      * 可指定悬停/按下时的背景色叠加，用于 Unlock 绿色高亮。
      * @param hoverTint 悬停颜色叠加；null 使用默认更亮
      * @param downTint  按下颜色叠加；null 使用默认更暗
      */
            private TextButton.TextButtonStyle createCustomButtonStyle(Color hoverTint, Color downTint) {
                TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
                style.font = skin.getFont("segoe_ui");

                        Texture buttonTexture = ServiceLocator.getResourceService()
                                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
                TextureRegion buttonRegion = new TextureRegion(buttonTexture);

                        NinePatch upPatch   = new NinePatch(buttonRegion, 10, 10, 10, 10);
                NinePatch overPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
                NinePatch downPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

                        if (hoverTint == null) {
                        overPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f)); // 默认更亮
                    } else {
                        overPatch.setColor(hoverTint);                        // 自定义（绿色）
                    }
                if (downTint == null) {
                        downPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));  // 默认更暗
                    } else {
                        downPatch.setColor(downTint);                         // 自定义（绿色）
                    }

                style.up   = new NinePatchDrawable(upPatch);
                style.over = new NinePatchDrawable(overPatch);
                style.down = new NinePatchDrawable(downPatch);

                style.fontColor     = Color.WHITE;
                style.overFontColor = Color.WHITE;
                style.downFontColor = Color.LIGHT_GRAY;
                return style;
            }
    /**
     +     * 生成“纯色按钮”样式：up/over/down 全部按指定颜色染色。
     +     * 用于：绿色(解锁但未选择)、红色(已选择)。
     +     */
   private TextButton.TextButtonStyle createSolidTintStyle(Color tint) {
                TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
                style.font = skin.getFont("segoe_ui");

                        Texture buttonTexture = ServiceLocator.getResourceService()
                                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
                TextureRegion buttonRegion = new TextureRegion(buttonTexture);

                        NinePatch up   = new NinePatch(buttonRegion, 10, 10, 10, 10);
                NinePatch over = new NinePatch(buttonRegion, 10, 10, 10, 10);
                NinePatch down = new NinePatch(buttonRegion, 10, 10, 10, 10);
                up.setColor(tint);
                over.setColor(tint);
                down.setColor(tint);

                style.up   = new NinePatchDrawable(up);
                style.over = new NinePatchDrawable(over);
                style.down = new NinePatchDrawable(down);
                style.fontColor = Color.WHITE;
                return style;
            }

    @Override
    protected void draw(SpriteBatch batch) {
        // draw stage
    }
    /**
     +     * Generates an instructional banner with a semi-transparent background at the bottom of the screen.
     +     */
    private Table makeInstructionBanner() {
                // Use the existing main menu button basemap to create a semi-transparent background
                        Texture buttonTexture = ServiceLocator.getResourceService()
                                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
                NinePatch patch = new NinePatch(new TextureRegion(buttonTexture), 10, 10, 10, 10);
                NinePatchDrawable bg = new NinePatchDrawable(patch);
                bg = bg.tint(new Color(0, 0, 0, 0.45f)); // translucent black

        // Instructions (English)
                         String text =
                                        "[S] Place hero\\n" +
                                        "[Hero] 1: Default weapon | 2: Fast fire, low damage | 3: High damage, slow fire\\n" +
                                        "[Engineer] 1: Place tower | 2: Place cannon | 3: Generate currency\\n" +
                                        "[Samurai] 1: Stab | 2: Slash | 3: Spin slash"+
                                                "Note: Engineer structures can only be placed on roads, Engineers can only place three robots at a time";

                        Label.LabelStyle ls = new Label.LabelStyle(skin.getFont("segoe_ui"), Color.WHITE);
                Label label = new Label(text, ls);
                label.setAlignment(Align.center);
                label.setWrap(true);


        //Wrap a container and set the background and padding
                        Table banner = new Table();
                banner.setBackground(bg);
                banner.pad(10f, 16f, 10f, 16f);
                banner.add(label).width(Math.min(Gdx.graphics.getWidth() * 0.9f, 980f)).center();

                        // Wrap another full-screen table and position it at the bottom
                                Table root = new Table();
                root.setFillParent(true);
                root.bottom().padBottom(12f);
                root.add(banner).center();
              //  return root;
                return new Table();
            }

    private void exitMenu() {
        game.setScreen(GdxGame.ScreenType.MAP_SELECTION);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
