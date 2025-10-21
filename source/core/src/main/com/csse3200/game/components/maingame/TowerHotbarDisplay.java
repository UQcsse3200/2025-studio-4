package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class TowerHotbarDisplay extends UIComponent {
    private Table rootTable;
    private SimplePlacementController placementController;
    private Skin hotbarSkin;
    private boolean isVisible = true; // 默认可见
    private Texture bgTexture;
    private Texture transparentBtnTexture;

    // Cache UI for relayout
    private Container<Table> rootContainer;
    private Table buttonTable;
    private ScrollPane scrollPane;
    private Label title;
    private ImageButton placeholderBtn;
    private ImageButton[] allButtonsArr;
    private int lastScreenW = -1, lastScreenH = -1;

    private static final Color GREYED_OUT_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final float UPDATE_INTERVAL = 0.5f; // Update button states every 0.5 seconds
    private float timeSinceLastUpdate = 0f;

    // Map to store button references and their tower types
    private Map<String, ImageButton> towerButtons = new HashMap<>();

    // Tower costs from JSON - you may want to load this from a config file instead
    private static final Map<String, Map<CurrencyType, Integer>> TOWER_COSTS = new HashMap<>();

    static {
        // Initialize tower costs based on your JSON
        TOWER_COSTS.put("bone", createCostMap(500, 0, 0));
        TOWER_COSTS.put("dino", createCostMap(750, 0, 0));
        TOWER_COSTS.put("cavemen", createCostMap(750, 0, 0));
        TOWER_COSTS.put("pterodactyl", createCostMap(0, 50, 0));
        TOWER_COSTS.put("totem", createCostMap(0, 150, 0));
        TOWER_COSTS.put("bank", createCostMap(0, 200, 0));
        TOWER_COSTS.put("raft", createCostMap(0, 250, 0));
        TOWER_COSTS.put("frozenmamoothskull", createCostMap(0, 300, 0));
        TOWER_COSTS.put("bouldercatapult", createCostMap(0, 500, 0));
        TOWER_COSTS.put("villageshaman", createCostMap(0, 0, 500));
        TOWER_COSTS.put("supercavemen", createCostMap(0, 0, 1000));
    }

    private static Map<CurrencyType, Integer> createCostMap(int metalScrap, int titaniumCore, int neurochip) {
        Map<CurrencyType, Integer> costMap = new EnumMap<>(CurrencyType.class);
        if (metalScrap > 0) costMap.put(CurrencyType.METAL_SCRAP, metalScrap);
        if (titaniumCore > 0) costMap.put(CurrencyType.TITANIUM_CORE, titaniumCore);
        if (neurochip > 0) costMap.put(CurrencyType.NEUROCHIP, neurochip);
        return costMap;
    }

    @Override
    public void create() {
        super.create();
        placementController = entity.getComponent(SimplePlacementController.class);
        hotbarSkin = skin;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().left();

        bgTexture = buildSolidTexture(new Color(0.15f, 0.15f, 0.18f, 0.6f));
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        // Use field so we can resize later
        rootContainer = new Container<>();
        rootContainer.setBackground(backgroundDrawable);
        rootContainer.pad(screenWidth * 0.0025f);

        title = new Label("TOWERS", skin, "title");
        title.setAlignment(Align.center);
        title.getStyle().fontColor = Color.valueOf("#FFFFFF");

        // Tower icons
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bones/boneicon.png")));
        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/dino/dinoicon.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/cavemen/cavemenicon.png")));
        TextureRegionDrawable superCavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/super/supercavemenicon.png")));
        TextureRegionDrawable pteroImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/pteradactyl/pterodactylicon.png")));
        TextureRegionDrawable totemImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/totem/totemicon.png")));
        TextureRegionDrawable bankImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/bank/bankicon.png")));
        TextureRegionDrawable raftImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/viking/rafticon.png")));
        TextureRegionDrawable frozenmamoothskullImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/mammoth/frozenmamoothskullicon.png")));
        TextureRegionDrawable bouldercatapultImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/catapault/bouldercatapulticon.png")));
        TextureRegionDrawable villageshamanImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/towers/shaman/villageshamanicon.png")));

        // Create buttons
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

        // Transparent placeholder button
        transparentBtnTexture = buildSolidTexture(new Color(1f, 1f, 1f, 0f));
        TextureRegionDrawable transparentDrawable = new TextureRegionDrawable(new TextureRegion(transparentBtnTexture));
        ImageButton.ImageButtonStyle transparentStyle = new ImageButton.ImageButtonStyle();
        transparentStyle.up = transparentDrawable;
        transparentStyle.down = transparentDrawable;
        transparentStyle.over = transparentDrawable;
        transparentStyle.checked = transparentDrawable;
        placeholderBtn = new ImageButton(transparentStyle);

        // Store array for rebuilds
        allButtonsArr = new ImageButton[] {
                boneBtn, dinoBtn, cavemenBtn, pteroBtn, totemBtn, bankBtn,
                raftBtn, frozenmamoothskullBtn, bouldercatapultBtn, villageshamanBtn,
                superCavemenBtn, placeholderBtn
        };

        // Store button references
        towerButtons.put("bone", boneBtn);
        towerButtons.put("dino", dinoBtn);
        towerButtons.put("cavemen", cavemenBtn);
        towerButtons.put("pterodactyl", pteroBtn);
        towerButtons.put("totem", totemBtn);
        towerButtons.put("bank", bankBtn);
        towerButtons.put("raft", raftBtn);
        towerButtons.put("frozenmamoothskull", frozenmamoothskullBtn);
        towerButtons.put("bouldercatapult", bouldercatapultBtn);
        towerButtons.put("villageshaman", villageshamanBtn);
        towerButtons.put("supercavemen", superCavemenBtn);

        // Add listeners
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

        // Button grid table (kept as field for rebuild)
        buttonTable = new Table();
        rebuildButtonGrid(); // initial build with current size

        Table content = new Table();
        content.add(title).colspan(3).center().padBottom(screenHeight * 0.006f).row();

        scrollPane = new ScrollPane(buttonTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).colspan(3).center().expand().fill();

        rootContainer.setActor(content);

        // Initial layout based on current screen size
        rootTable.clearChildren();
        rootTable.add(rootContainer)
                .width(screenWidth * 0.232f)
                .height(screenHeight * 0.55f);
        stage.addActor(rootTable);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);

        applyUiScale();

        // 应用初始可见性状态
        rootTable.setVisible(isVisible);

        // Initial button state update
        updateButtonAffordability();

        lastScreenW = (int) screenWidth;
        lastScreenH = (int) screenHeight;
    }

    // Rebuild the 3-column button grid with sizes derived from current screen size
    private void rebuildButtonGrid() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (screenWidth <= 0 || screenHeight <= 0) return;

        float BUTTON_W = screenWidth * 0.072f;
        float BUTTON_H = screenHeight * 0.112f;
        float BUTTON_PAD = screenWidth * 0.001f;

        buttonTable.clearChildren();
        buttonTable.defaults().pad(BUTTON_PAD).center();
        for (int i = 0; i < allButtonsArr.length; i++) {
            buttonTable.add(allButtonsArr[i]).size(BUTTON_W, BUTTON_H);
            if ((i + 1) % 3 == 0) buttonTable.row();
        }
        buttonTable.invalidateHierarchy();
    }

    // Relayout sizes for container and title padding when the window size changes
    private void relayoutForScreen() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (screenWidth <= 0 || screenHeight <= 0) return;

        // Update container padding
        if (rootContainer != null) {
            rootContainer.pad(screenWidth * 0.0025f);
        }

        // Update title spacing
        if (title != null && title.getParent() instanceof Table) {
            Table parent = (Table) title.getParent();
            // Re-apply bottom pad on the title row
            parent.getCell(title).padBottom(screenHeight * 0.006f);
        }

        // Rebuild buttons with new sizes
        rebuildButtonGrid();

        // Re-apply root container size
        if (rootTable != null) {
            rootTable.clearChildren();
            rootTable.add(rootContainer)
                    .width(screenWidth * 0.232f)
                    .height(screenHeight * 0.55f);
            rootTable.invalidateHierarchy();
        }
    }

    /**
     * Update button states based on player's current currency
     */
    private void updateButtonAffordability() {
        Entity player = findPlayerEntity();
        if (player == null) return;

        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) return;

        for (Map.Entry<String, ImageButton> entry : towerButtons.entrySet()) {
            String towerType = entry.getKey();
            ImageButton button = entry.getValue();

            Map<CurrencyType, Integer> costs = TOWER_COSTS.get(towerType);
            if (costs == null || costs.isEmpty()) {
                // No cost data, assume affordable
                button.setColor(NORMAL_COLOR);
                continue;
            }

            boolean canAfford = true;
            for (Map.Entry<CurrencyType, Integer> cost : costs.entrySet()) {
                if (currencyManager.getCurrencyAmount(cost.getKey()) < cost.getValue()) {
                    canAfford = false;
                    break;
                }
            }

            if (canAfford) {
                button.setColor(NORMAL_COLOR);
            } else {
                button.setColor(GREYED_OUT_COLOR);
            }
        }
    }

    /**
     * Check if player can afford to build a specific tower
     */
    private boolean canAffordTower(String towerType) {
        Entity player = findPlayerEntity();
        if (player == null) return false;

        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) return false;

        Map<CurrencyType, Integer> costs = TOWER_COSTS.get(towerType);
        if (costs == null || costs.isEmpty()) return true;

        for (Map.Entry<CurrencyType, Integer> cost : costs.entrySet()) {
            if (currencyManager.getCurrencyAmount(cost.getKey()) < cost.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find the player entity with CurrencyManagerComponent
     */
    private Entity findPlayerEntity() {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities) {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * Safely get entities from the entity service
     */
    private Array<Entity> safeEntities() {
        try {
            return com.csse3200.game.services.ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    private void addPlacementListener(ImageButton button, String towerType) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Check affordability before allowing placement
                if (!canAffordTower(towerType)) {
                    System.out.println("Cannot afford " + towerType + " tower!");
                    return;
                }

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

    /**
     * 设置防御塔UI的可见性
     * @param visible true显示，false隐藏
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (rootTable != null) {
            rootTable.setVisible(visible);
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
    public void update() {
        super.update();

        // Periodically update button affordability states
        timeSinceLastUpdate += Gdx.graphics.getDeltaTime();
        if (timeSinceLastUpdate >= UPDATE_INTERVAL) {
            updateButtonAffordability();
            timeSinceLastUpdate = 0f;
        }

        // Responsive relayout on window resize/minimize/maximize
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        if (w != lastScreenW || h != lastScreenH) {
            lastScreenW = w;
            lastScreenH = h;
            relayoutForScreen();
        }
    }

    @Override
    public void dispose() {
        if (rootTable != null) {
            rootTable.clear();
        }
        if (bgTexture != null) {
            bgTexture.dispose();
            bgTexture = null;
        }
        if (transparentBtnTexture != null) {
            transparentBtnTexture.dispose();
            transparentBtnTexture = null;
        }
        towerButtons.clear();
        super.dispose();
    }

    private static Texture buildSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }
}

