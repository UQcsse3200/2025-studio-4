package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.ui.UIComponent;

public class TowerHotbarDisplay extends UIComponent {
    private Table rootTable;
    private SimplePlacementController placementController;
    private Skin hotbarSkin;

    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);
        hotbarSkin = skin;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // 根容器：左下角
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left();

        // 背景
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(screenWidth * 0.0025f);

        Label title = new Label("TOWERS", skin, "title");
        title.setAlignment(Align.center);

        // 图标资源（如路径不同，请按资源实际位置调整）
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/cavemenicon.png")));
        TextureRegionDrawable superCavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/supercavemenicon.png")));
        TextureRegionDrawable placeholderImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/placeholder.png")));
        TextureRegionDrawable pteroImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/pterodactylicon.png")));
        TextureRegionDrawable totemImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/totemicon.png")));
        TextureRegionDrawable bankImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bankicon.png")));
        TextureRegionDrawable raftImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/rafticon.png")));
        TextureRegionDrawable frozenmamoothskullImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/frozenmamoothskullicon.png")));
        TextureRegionDrawable bouldercatapultImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bouldercatapulticon.png")));
        TextureRegionDrawable villageshamanImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/villageshamanicon.png")));

        // 按钮
        ImageButton boneBtn = new ImageButton(boneImage);
        ImageButton dinoBtn = new ImageButton(dinoImage);
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        ImageButton pteroBtn = new ImageButton(pteroImage);
        ImageButton totemBtn = new ImageButton(totemImage);
        ImageButton bankBtn = new ImageButton(bankImage);
        ImageButton raftBtn = new ImageButton(raftImage);
        ImageButton frozenmamoothskullBtn = new ImageButton(frozenmamoothskullImage);
        ImageButton bouldercatapultBtn = new ImageButton(bouldercatapultImage);
        ImageButton villageshamanBtn = new ImageButton(villageshamanImage);
        ImageButton superCavemenBtn = new ImageButton(superCavemenImage);
        ImageButton placeholderBtn = new ImageButton(placeholderImage);

        ImageButton[] allButtons = {
            boneBtn, dinoBtn, cavemenBtn, pteroBtn, totemBtn, bankBtn,
            raftBtn, frozenmamoothskullBtn, bouldercatapultBtn, villageshamanBtn,
            superCavemenBtn, placeholderBtn
        };

        addPlacementListener(boneBtn, "bone");
        addPlacementListener(dinoBtn, "dino");
        addPlacementListener(cavemenBtn, "cavemen");
        addPlacementListener(pteroBtn, "pterodactyl");
        addPlacementListener(totemBtn, "totem");
        addPlacementListener(bankBtn, "bank");
        addPlacementListener(raftBtn, "raft");
        addPlacementListener(frozenmamoothskullBtn, "frozenmamoothskull");
        addPlacementListener(bouldercatapultBtn, "bouldercatapult");
        addPlacementListener(villageshamanBtn, "villageshaman");
        addPlacementListener(superCavemenBtn, "supercavemen");

        // 按钮网格
        Table buttonTable = new Table();
        float BUTTON_W = screenWidth * 0.072f;
        float BUTTON_H = screenHeight * 0.112f;
        float BUTTON_PAD = screenWidth * 0.001f;
        buttonTable.defaults().pad(BUTTON_PAD).center();

        for (int i = 0; i < allButtons.length; i++) {
            buttonTable.add(allButtons[i]).size(BUTTON_W, BUTTON_H);
            if ((i + 1) % 3 == 0) buttonTable.row();
        }

        // 标题 + 滚动容器
        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(screenHeight * 0.006f).row();

        ScrollPane scrollPane = new ScrollPane(buttonTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).colspan(3).center().expand().fill();

        container.setActor(content);

        // 放入根容器
        rootTable.add(container)
            .width(screenWidth * 0.232f)
            .height(screenHeight * 0.55f);
        stage.addActor(rootTable);

        // 输入多路复用
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);

        // 应用 UI 缩放（来源于用户设置）
        applyUiScale();
    }

    private void addPlacementListener(ImageButton button, String towerType) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placementController != null) {
                    if (placementController.isPlacementActive()) {
                        if (towerType.equalsIgnoreCase(placementController.getPendingType())) {
                            placementController.cancelPlacement();
                            return;
                        } else {
                            placementController.cancelPlacement();
                        }
                    }
                    placementController.requestPlacement(towerType);
                } else {
                    handlePlacement("startPlacement" + capitalize(towerType));
                }
            }
        });
    }

    private void applyUiScale() {
        UserSettings.Settings settings = UserSettings.get();
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.validate();
            rootTable.setOrigin(0f, 0f);
            rootTable.setScale(settings.uiScale);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void handlePlacement(String eventName) {
        if (placementController != null && placementController.isPlacementActive()) {
            placementController.cancelPlacement();
            return;
        }
        entity.getEvents().trigger(eventName);
    }

    @Override
    public void draw(SpriteBatch batch) {}

    @Override
    public void dispose() {
        if (rootTable != null) {
            rootTable.clear();
        }
        super.dispose();
    }
}