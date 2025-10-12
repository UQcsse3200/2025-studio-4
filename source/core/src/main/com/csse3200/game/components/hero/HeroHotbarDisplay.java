package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class HeroHotbarDisplay extends UIComponent {
    private Table rootTable;
    private Skin uiSkin;
    private HeroPlacementComponent placement;
    private Image holoGradient;   // 渐变玻璃
    private Image scanlines;      // 扫描线
    private Texture texGradient;
    private Texture texScanlines;

    @Override
    public void create() {
        super.create();
        placement = entity.getComponent(HeroPlacementComponent.class);
        uiSkin = skin;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // === 放到右下角 ===
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center().right();

        // 背景
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.15f, 0.15f, 0.18f, 0.9f));
        pm.fill();
        Texture bg = new Texture(pm);
        pm.dispose();
        Drawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bg));

        Container<Table> container = new Container<>();
        container.setBackground(bgDrawable);
        container.pad(screenWidth * 0.006f);

        Label title = new Label("HERO", uiSkin, "title");
        title.setAlignment(Align.center);

        // 图标资源（可替换为独立Icon）
        TextureRegionDrawable engIcon   = new TextureRegionDrawable(new TextureRegion(new Texture("images/engineer/Engineer.png")));
        TextureRegionDrawable samIcon   = new TextureRegionDrawable(new TextureRegion(new Texture("images/samurai/Samurai.png")));
        TextureRegionDrawable defaultIc = new TextureRegionDrawable(new TextureRegion(new Texture("images/hero/Heroshoot.png")));

        // 只显示“已选英雄”的图标
        Table btnTable = new Table();
        float BUTTON_W = screenWidth * 0.072f;
        float BUTTON_H = screenHeight * 0.112f;
        float BUTTON_PAD = screenWidth * 0.006f;
        btnTable.defaults().pad(BUTTON_PAD).center();

        ImageButton chosenBtn;

        GameStateService gs = ServiceLocator.getGameStateService();
        GameStateService.HeroType chosen = (gs != null) ? gs.getSelectedHero() : GameStateService.HeroType.HERO;

        switch (chosen) {
            case ENGINEER -> {
                chosenBtn = new ImageButton(engIcon);
                addHeroClick(chosenBtn, "engineer");
            }
            case SAMURAI -> {
                chosenBtn = new ImageButton(samIcon);
                addHeroClick(chosenBtn, "samurai");
            }
            default -> {
                chosenBtn = new ImageButton(defaultIc);
                addHeroClick(chosenBtn, "default");
            }
        }

        btnTable.add(chosenBtn).size(BUTTON_W, BUTTON_H);

        // 标题 + 内容
        Table content = new Table();
        content.add(title).center().padBottom(screenHeight * 0.008f).row();

        ScrollPane sp = new ScrollPane(btnTable, uiSkin);
        sp.setScrollingDisabled(true, true); // 单个按钮，不需要滚动
        sp.setFadeScrollBars(false);
        content.add(sp).expand().fill();

        container.setActor(content);

        rootTable.add(container)
                .width(screenWidth * 0.20f)   // 右侧紧凑点
                .height(screenHeight * 0.20f);

        stage.addActor(rootTable);

        applyUiScale();
    }

    private void addHeroClick(ImageButton btn, String heroType) {
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placement != null) {
                    // 再次点击同类型会在 HeroPlacementComponent 内部切换为取消
                    placement.requestPlacement(heroType);
                } else {
                    // 兜底事件（如果没挂载组件）
                    entity.getEvents().trigger("heroPlacement:request", heroType);
                }
            }
        });
    }

    private void applyUiScale() {
        UserSettings.Settings st = UserSettings.get();
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.validate();
            rootTable.setOrigin(0f, 0f);
            rootTable.setScale(st.uiScale);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {}

    @Override
    public void dispose() {
        if (rootTable != null) rootTable.clear();
        super.dispose();
    }
}
