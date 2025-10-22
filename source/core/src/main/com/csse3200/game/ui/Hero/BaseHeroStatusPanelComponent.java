package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.SimpleUI;
import com.csse3200.game.ui.UltimateButtonComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;


/**
 * Generic base class for the hero status panel (percentage-based positioning/sizing):
 * - Includes: name, HP, energy, damage, level, upgrade cost, and ULT button
 * - Subclasses can override buildExtraSections()/bindExtraListeners() to add custom sections
 * - The panel is placed automatically: directly below the Hotbar → and below the summon toolbar
 */
public class BaseHeroStatusPanelComponent extends Component {
    protected final Entity hero;
    protected final String heroName;

    // Color/layout parameters (specified by subclasses via constructor)
    protected final Color bgColor;
    protected final Color textColor;
    protected final Color accentColor;

    // ====== Shared percentage parameters for the right-side vertical UI stack (keep consistent with your Hotbar/Toolbar) ======
    /**
     * Hotbar: height as a fraction of screen height (your final value is 0.28f)
     */
    protected static final float HOTBAR_HEIGHT_PCT = 0.28f;
    /**
     * Hotbar bottom edge: vertically centered → 0.5 + half of the Hotbar height
     */
    protected static final float HOTBAR_BOTTOM_PCT = 0.5f + HOTBAR_HEIGHT_PCT * 0.5f;

    /**
     * Summon toolbar height (your final value is 0.06f)
     */
    protected static final float TOOLBAR_HEIGHT_PCT = 0.06f;
    /**
     * Spacing between the toolbar and the status panel
     */
    protected static final float GAP_BELOW_TOOLBAR_PCT = 0.0f; // Set to 0f if you want them to touch

    /**
     * Unified width for these right-side panels (same as your Hotbar: 0.195f)
     */
    protected static final float COMMON_PANEL_WIDTH_PCT = 0.195f;
    /**
     * The status panel's own height (adjustable)
     */
    protected final float panelHeightPct; // Replaces the old panelHeightScale (fraction of screen height)

    /**
     * Right margin from the screen edge (same as your Hotbar: 0f, flush to the edge)
     */
    protected static final float RIGHT_MARGIN_PCT = 0.0f;

    protected Table costRow;
    protected Label costTitleLabel, costNumLabel;
    protected Image costIcon;

    // 舞台与容器
    protected Stage stage;
    protected Table root;
    protected Table card;

    // Shared widgets
    protected Label nameLabel, hpLabel, energyLabel, levelLabel, costLabel, damageLabel;
    protected Button ultBtn;
    protected TextButton upgradeBtn;

    public BaseHeroStatusPanelComponent(Entity hero,
                                        String heroName,
                                        Color bgColor,
                                        Color textColor,
                                        Color accentColor,
                                        float panelHeightPct /* Panel height as a fraction of the screen, e.g., 0.12f~0.26f */) {
        this.hero = hero;
        this.heroName = (heroName != null) ? heroName : "Hero";
        this.bgColor = (bgColor != null) ? bgColor : new Color(0.15f, 0.15f, 0.18f, 0.90f);
        this.textColor = (textColor != null) ? textColor : Color.WHITE;
        this.accentColor = (accentColor != null) ? accentColor : new Color(0.35f, 0.75f, 1.00f, 1f);
        this.panelHeightPct = (panelHeightPct > 0f) ? panelHeightPct : 0.24f;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // Skin and text
        Skin skin = new Skin();
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), textColor);
        skin.add("default", ls);

        // Background
        TextureRegionDrawable darkBg = new TextureRegionDrawable(makeSolid(4, 4, bgColor));

        // ===== Root container: top-right; vertical position = Hotbar bottom + toolbar height + spacing =====
        root = new Table();
        root.setFillParent(true);
        root.align(Align.topRight);
        root.padTop(Value.percentHeight(HOTBAR_BOTTOM_PCT + TOOLBAR_HEIGHT_PCT + GAP_BELOW_TOOLBAR_PCT, root));
        root.padRight(Value.percentWidth(RIGHT_MARGIN_PCT, root));

        // ===== Card =====
        card = new Table(skin);
        card.setBackground(darkBg);
        // Padding & row spacing use percentages relative to the card itself for stability
        card.pad(Value.percentWidth(0.02f, card));             // Left/right inner padding
        card.defaults().left().padBottom(Value.percentHeight(0.02f, card));

        // Text and buttons
        nameLabel = new Label(heroName, skin);
        levelLabel = new Label("Lv. 1", skin);
        // ==== 升级花费行：Upgrade cost: [400] [icon] ==== //
        costTitleLabel = new Label("Upgrade cost: ", skin);
        costNumLabel = new Label("400", skin);

        costIcon = new Image(currencyIconDrawable(CurrencyType.METAL_SCRAP));
        costIcon.setScaling(Scaling.stretch);
        costIcon.setColor(1f, 1f, 1f, 1f);

        costRow = new Table(skin);
        costRow.add(costTitleLabel).left();
        costRow.add(costNumLabel).left().padRight(6f);
        costRow.add(costIcon).size(22f, 22f).left();


        var ultBtn = UltimateButtonComponent.createUltimateButton(hero, 200, CurrencyType.METAL_SCRAP);
        this.ultBtn = ultBtn;
        card.add(ultBtn).left().row();

        TextButton.TextButtonStyle upStyle = SimpleUI.primaryButton();
        upStyle.font = SimpleUI.font();
        upStyle.fontColor = Color.BLACK;
        upStyle.overFontColor = Color.BLACK;
        upStyle.downFontColor = Color.BLACK;
        upgradeBtn = new TextButton("Upgrade", upStyle);

        Table info = new Table();
        info.add(nameLabel).left().row();
        info.add(levelLabel).left();

        // Assemble shared section
        card.add(info).left().row();
        card.add(energyLabel).left().row();

        // —— Subclass extension area (e.g., capacity/cooldown) —— //
        buildExtraSections(card, skin,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        card.add(costRow).left().row();
        card.add(upgradeBtn)
                .left()
                .width(Value.percentWidth(0.45f, card))              // Upgrade button width = 45% of the card width
                .padTop(Value.percentHeight(0.02f, card))
                .row();
        card.add(ultBtn).left().row();

        // Put the card into the right-side layout with “equal width alignment & percentage height”
        root.add(card)
                .width(Value.percentWidth(COMMON_PANEL_WIDTH_PCT, root))
                .height(Value.percentHeight(panelHeightPct, root));
        stage.addActor(root);

        // ===== Shared events =====

        hero.getEvents().addListener("hero.level", (Integer lv) -> {
            if (lv == null) return;
            levelLabel.setText("Lv. " + lv);
            refreshUpgradeInfo();
        });

        upgradeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hero.getEvents().trigger("requestUpgrade", findPlayer());
                refreshUpgradeInfo();
            }
        });

        hero.getEvents().addListener("upgraded", (Integer level,
                                                  com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType currencyType,
                                                  Integer cost) -> refreshUpgradeInfo());

        hero.getEvents().addListener("upgradeFailed", (String msg) -> refreshUpgradeInfo());

        // Extra events for subclasses (e.g., capacity/cooldown)
        bindExtraListeners();

        refreshUpgradeInfo();
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
    }

    /**
     * Subclasses may override: build extra UI sections (e.g., capacity/cooldown). Default: none.
     */
    protected void buildExtraSections(Table card, Skin skin, float sw, float sh) {
    }

    /**
     * Subclasses may override: bind extra event listeners (e.g., capacity/cooldown). Default: none.
     */
    protected void bindExtraListeners() {
    }

    /**
     * Common progress bar style (dark gray background + accent-colored fill)
     */
    protected ProgressBar.ProgressBarStyle buildBarStyle() {
        ProgressBar.ProgressBarStyle s = new ProgressBar.ProgressBarStyle();
        s.background = new TextureRegionDrawable(makeSolid(8, 8, new Color(0.10f, 0.10f, 0.12f, 1f)));
        s.knobBefore = new TextureRegionDrawable(makeSolid(8, 8, accentColor));
        s.knob = new TextureRegionDrawable(makeSolid(1, 8, new Color(1f, 1f, 1f, 0f)));
        return s;
    }

    /**
     * Refresh upgrade info and button state based on the current level
     */
    protected void refreshUpgradeInfo() {
        var up = hero.getComponent(com.csse3200.game.components.hero.HeroUpgradeComponent.class);
        int lvl = (up != null) ? up.getLevel() : 1;

        if (lvl >= 2) {
            costTitleLabel.setText("MAX LEVEL");
            costTitleLabel.setColor(accentColor.cpy().lerp(Color.GRAY, 0.4f));
            costNumLabel.setText("");
            costIcon.setVisible(false);
            upgradeBtn.setDisabled(true);
            upgradeBtn.setText("Maxed");
            upgradeBtn.getStyle().fontColor = Color.GRAY;
            upgradeBtn.getStyle().overFontColor = Color.GRAY;
            upgradeBtn.getStyle().downFontColor = Color.GRAY;
            return;
        }

        int nextCost = 400;
        CurrencyType nextType = CurrencyType.METAL_SCRAP;

        // 更新 UI
        costTitleLabel.setText("Upgrade cost: ");
        costTitleLabel.setColor(textColor);

        costNumLabel.setText(String.valueOf(nextCost));
        costNumLabel.setColor(textColor);

        costIcon.setDrawable(currencyIconDrawable(nextType));
        costIcon.setVisible(true);
    }


    protected Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.currencysystem.CurrencyManagerComponent wallet =
                    e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    /**
     * Generate a solid-color texture region (Note: not centrally disposed here—if created frequently, consider using a resource manager)
     */
    protected static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }

    private Drawable currencyIconDrawable(CurrencyType t) {
        String path;
        switch (t) {
            case METAL_SCRAP:
                path = "images/currency/metal_scrap.png";
                break;
            case TITANIUM_CORE:
                path = "images/currency/titanium_core.png";
                break;
            case NEUROCHIP:
                path = "images/currency/neurochip.png";
                break;
            default:
                path = "images/currency/currency_unknown.png";
                break;
        }
        Texture tex;
        try {
            tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        } catch (Exception e) {
            Pixmap pm = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pm.setColor(Color.GRAY);
            pm.fill();
            tex = new Texture(pm);
            pm.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

}
