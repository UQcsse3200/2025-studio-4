package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import com.csse3200.game.entities.Entity;

import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.components.towers.TowerCostComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.towers.OrbitComponent;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.maingame.TowerUpgradeData.UpgradeStats;
import com.csse3200.game.components.towers.BeamAttackComponent;
import com.csse3200.game.components.towers.StatsBoostComponent;

import java.util.Map;
import java.util.EnumMap;

/**
 * UI component for displaying and upgrading towers.
 * Shows separate upgrade paths for each tower type.
 */
public class TowerUpgradeMenu extends UIComponent {
    // Periodic refresh task so the menu updates affordability dynamically
    private Task refreshTask;

    private Table rootTable;
    private Entity selectedTower = null;
    private String currentTowerType;

    private Label pathALevelLabel;
    private Label pathBLevelLabel;
    private TextButton pathAButton;
    private TextButton pathBButton;
    // Add title labels so we can change them per tower type
    private Label pathATitleLabel;
    private Label pathBTitleLabel;
    private Label sellRefundLabel; // <-- shows refund amount under "Sell"
    private Image sellRefundIcon;  // <-- currency icon under "Sell"
    private Table sellRefundRow;   // <-- container for icon + amount

    private static final CurrencyType UPGRADE_CURRENCY = CurrencyType.METAL_SCRAP;
    private static final Color GREYED_OUT_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.6f);
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color INSUFFICIENT_FUNDS_COLOR = new Color(1f, 0.3f, 0.3f, 1f); // Red tint for cost label

    private final Map<String, Map<Integer, UpgradeStats>> pathAUpgradesPerTower;
    private final Map<String, Map<Integer, UpgradeStats>> pathBUpgradesPerTower;

    // 防重复升级标志
    private boolean upgradeInProgress = false;

    // background texture to dispose
    private Texture bgTexture;

    private final EnumMap<CurrencyType, Texture> currencyIconCache = new EnumMap<>(CurrencyType.class);
    // Colored button backgrounds
    // private Texture pathAButtonBg; // removed: keep skin 9-patch shape
    // private Texture pathBButtonBg; // removed: keep skin 9-patch shape

    // NEW: cached snapshot of last rendered UI state
    private UiSnapshot lastUiSnapshot;

    /**
     * Creates the UI for the tower upgrade menu and sets up listeners.
     */
    public TowerUpgradeMenu() {
        // Initialize upgrade data
        this.pathAUpgradesPerTower = TowerUpgradeData.getPathAUpgrades();
        this.pathBUpgradesPerTower = TowerUpgradeData.getPathBUpgrades();

        // Debug: Print loaded upgrade data
        System.out.println("DEBUG: Loaded upgrade data for towers: " + pathAUpgradesPerTower.keySet());
    }

    /**
     * Creates the upgrade menu UI and sets up listeners.
     */
    @Override
    public void create() {
        super.create();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().padBottom(0); // stick to bottom
        rootTable.setVisible(false);

        // Background (requested color, 90% opacity)
        bgTexture = buildSolidTexture(new Color(0.10f, 0.10f, 0.12f, 0.75f));
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(10);

        /*Label title = new Label("Tower Upgrades", skin, "title");
        title.setColor(Color.WHITE);*/

        // Path A
        Table pathATable = new Table(skin);
        pathATable.defaults().pad(5);
        pathATitleLabel = new Label("Damage & Range", skin); // was local 'pathALabel'
        pathATitleLabel.setColor(Color.ORANGE);
        pathALevelLabel = new Label("Level: 1", skin);
        pathAButton = new TextButton("", skin);
        pathATable.add(pathATitleLabel).row();
        pathATable.add(pathALevelLabel).row();
        pathATable.add(pathAButton).width(180).row();

        // Make Path A button background match its label color, preserving shape
        {
            TextButton.TextButtonStyle base = new TextButton.TextButtonStyle(pathAButton.getStyle());
            TextButton.TextButtonStyle tinted = tintedStyle(base, new Color(pathATitleLabel.getColor()));
            pathAButton.setStyle(tinted);
        }

        // Path B
        Table pathBTable = new Table(skin);
        pathBTable.defaults().pad(5);
        pathBTitleLabel = new Label("Cooldown & Speed", skin); // was local 'pathBLabel'
        pathBTitleLabel.setColor(Color.SKY);
        pathBLevelLabel = new Label("Level: 1", skin);
        pathBButton = new TextButton("", skin);
        pathBTable.add(pathBTitleLabel).row();
        pathBTable.add(pathBLevelLabel).row();
        pathBTable.add(pathBButton).width(180).row();

        // Make Path B button background match its label color, preserving shape
        {
            TextButton.TextButtonStyle base = new TextButton.TextButtonStyle(pathBButton.getStyle());
            TextButton.TextButtonStyle tinted = tintedStyle(base, new Color(pathBTitleLabel.getColor()));
            pathBButton.setStyle(tinted);
        }

        // --- Sell button (placed to the right of upgrades) ---
        TextButton sellButton = new TextButton("Sell", skin);
        sellButton.setColor(Color.RED);
        Table sellTable = new Table(skin);
        sellTable.defaults().pad(5);
        Label sellLabel = new Label("Sell Tower", skin);
        sellLabel.setColor(Color.RED);
        sellTable.add(sellLabel).row();

        // Refund row: icon + amount
        sellRefundLabel = new Label("0", skin);
        sellRefundLabel.setColor(Color.WHITE);
        sellRefundLabel.setAlignment(Align.center);
        sellRefundIcon = new Image(getCurrencyDrawable(CurrencyType.METAL_SCRAP)); // default icon
        sellRefundRow = new Table(skin);
        sellRefundRow.add(sellRefundIcon).size(24, 24).padRight(5);
        sellRefundRow.add(sellRefundLabel);
        sellTable.add(sellRefundRow).row();

        sellTable.add(sellButton).width(160).row();

        // --- Sell button listener ---
        sellButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedTower == null) return;

                TowerComponent towerComp = selectedTower.getComponent(TowerComponent.class);
                TowerCostComponent costComp = selectedTower.getComponent(TowerCostComponent.class);
                TowerStatsComponent statsComp = selectedTower.getComponent(TowerStatsComponent.class);
                if (towerComp == null || costComp == null || statsComp == null) return;

                Entity player = findPlayerEntity();
                if (player == null) return;
                CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
                if (currencyManager == null) return;

                // Calculate total spent: base cost + all purchased upgrades (A & B)
                int totalSpent = 0;
                String towerTypeKey = canonicalTowerType(towerComp.getType());
                CurrencyType refundCurrencyType = currencyForTowerType(towerTypeKey);

                // Base cost in that currency
                totalSpent += costComp.getCostForCurrency(refundCurrencyType);

                // Path A upgrades up to current level
                Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(towerTypeKey);
                int levelA = statsComp.getLevel_A();
                for (int lvl = 2; lvl <= levelA; lvl++) {
                    if (upgradesA != null && upgradesA.containsKey(lvl)) {
                        totalSpent += upgradesA.get(lvl).cost;
                    }
                }

                // Path B upgrades up to current level
                Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(towerTypeKey);
                int levelB = statsComp.getLevel_B();
                for (int lvl = 2; lvl <= levelB; lvl++) {
                    if (upgradesB != null && upgradesB.containsKey(lvl)) {
                        totalSpent += upgradesB.get(lvl).cost;
                    }
                }

                // Refund 75% in the appropriate currency
                Map<CurrencyType, Integer> refundMap = new EnumMap<>(CurrencyType.class);
                refundMap.put(refundCurrencyType, totalSpent);
                currencyManager.refundCurrency(refundMap, 0.75f);

                // Despawn head (if present), then the tower
                if (towerComp.hasHead()) {
                    Entity head = towerComp.getHeadEntity();
                    if (head != null) {
                        head.dispose();
                        com.csse3200.game.services.ServiceLocator.getEntityService().unregister(head);
                    }
                }
                selectedTower.dispose();
                com.csse3200.game.services.ServiceLocator.getEntityService().unregister(selectedTower);

                selectedTower = null;
                rootTable.setVisible(false);
            }
        });

        // Combine sections into a single row: Path A | Path B | Sell (Sell on the right)
        Table content = new Table(skin);
        content.defaults().pad(10).top();
        content.add(pathATable).padRight(20).top();
        content.add(pathBTable).padRight(20).top();
        content.add(sellTable).top();

        Table containerContent = new Table(skin);
        containerContent.add(content).center();
        container.setActor(containerContent);

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Narrower and thinner
        float desiredWidth = Math.max(480f, Math.min(screenW * 0.45f, 700f));
        float desiredHeight = Math.min(screenH * 0.18f, 170f);

        container.pad(8); // reduce internal padding
        rootTable.clearChildren();
        rootTable.add(container)
                .width(desiredWidth)
                .height(desiredHeight)
                .center()
                .bottom()
                .padBottom(12f);


        stage.addActor(rootTable);

        // Start a small periodic refresh so the UI updates when player currency changes
        // Only run updateLabels when the menu is visible to avoid unnecessary work.
        refreshTask = Timer.schedule(new Task() {
            @Override
            public void run() {
                if (rootTable != null && rootTable.isVisible()) {
                    // Gate UI updates so labels aren't rebuilt every 0.25s
                    updateLabelsIfChanged();
                }
            }
        }, 0.15f /* delay */, 0.25f /* interval */);

        // Button listeners
        pathAButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(true);
            }
        });
        pathBButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(false);
            }
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Draws the UI component. (No custom drawing needed here.)
     *
     * @param batch The SpriteBatch used for rendering.
     */
    @Override
    protected void draw(SpriteBatch batch) { }

    /**
     * Sets the currently selected tower for upgrades.
     *
     * @param tower The tower entity.
     * @param towerType Tower type string.
     */
    public void setSelectedTower(Entity tower, String towerType) {
        this.selectedTower = tower;
        // Defensive: fallback to TowerComponent type if towerType is null
        if (towerType == null && tower != null) {
            TowerComponent tc = tower.getComponent(TowerComponent.class);
            towerType = tc != null ? tc.getType() : "";
        }
        this.currentTowerType = canonicalTowerType(towerType);
        rootTable.setVisible(tower != null);
        // CHANGED: only update UI now (when opened/changed)
        updateLabelsIfChanged();
    }

    /**
     * Returns true if the upgrade menu is touched at the given screen coordinates.
     *
     * @param screenX The X coordinate on the screen.
     * @param screenY The Y coordinate on the screen.
     * @return True if the menu is touched, false otherwise.
     */
    public boolean isTouched(int screenX, int screenY) {
        Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return rootTable.hit(stageCoords.x, stageCoords.y, true) != null;
    }

    /**
     * Helper: choose upgrade/refund currency per tower type.
     *
     * @param towerType The tower type string.
     * @return The currency type for upgrades/refunds.
     */
    private static CurrencyType currencyForTowerType(String towerType) {
        String key = canonicalTowerType(towerType);
        if (key == null || key.isEmpty()) return UPGRADE_CURRENCY;

        // Titanium Core towers
        if ("pteradactyl".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("totem".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("bank".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE; // added: bank uses titanium core
        if ("raft".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("frozenmamoothskull".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("bouldercatapult".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("cavemenvillage".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;

        // Neurochip (“neurocore”) towers
        if ("villageshaman".equalsIgnoreCase(key)) return CurrencyType.NEUROCHIP;
        if ("supercavemen".equalsIgnoreCase(key)) return CurrencyType.NEUROCHIP;

        return UPGRADE_CURRENCY;
    }

    /**
     * Helper: canonicalizes the tower type string.
     *
     * @param towerType The tower type string.
     * @return The canonical tower type string.
     */
    private static String canonicalTowerType(String towerType) {
        if (towerType == null) return "";
        String s = towerType.trim().toLowerCase();
        if (s.isEmpty()) return "";
        if (s.equals("pteradactyl") || s.equals("pterodactyl") || s.startsWith("ptero")) return "pteradactyl";
        if (s.equals("totem")) return "totem";
        if (s.equals("supercavemen")) return "supercavemen";
        if (s.equals("cavemenvillage")) return "cavemenvillage";
        if (s.equals("raft")) return "raft";
        if (s.equals("frozenmamoothskull")) return "frozenmamoothskull";
        if (s.equals("bouldercatapult")) return "bouldercatapult";
        if (s.equals("villageshaman")) return "villageshaman";
        return s;
    }

    /**
     * Sets up the content of an upgrade button with the cost and icon.
     *
     * @param button The button to set up.
     * @param cost   The cost to display.
     */
    private void setupButtonContent(TextButton button, int cost) {
        Table content = new Table(skin);
        // Ensure button text cleared before replacing children
        button.setText("");
        Image scrapIcon = new Image(new TextureRegionDrawable(
                new TextureRegion(new Texture(CurrencyType.METAL_SCRAP.getTexturePath()))));
        Label costLabel = new Label(String.valueOf(cost), skin);
        content.add(scrapIcon).size(24, 24).padRight(5);
        content.add(costLabel);
        button.clearChildren();
        button.add(content).expand().fill();
    }

    /**
     * Overload: show currency icon based on currency type.
     *
     * @param button The button to set up.
     * @param cost The cost to display.
     * @param currency The currency type.
     */
    private void setupButtonContent(TextButton button, int cost, CurrencyType currency) {
        Table content = new Table(skin);
        // Ensure button text cleared before replacing children
        button.setText("");
        String texPath = currency != null ? currency.getTexturePath() : CurrencyType.METAL_SCRAP.getTexturePath();
        Image scrapIcon = new Image(new TextureRegionDrawable(new TextureRegion(new Texture(texPath))));
        Label costLabel = new Label(String.valueOf(cost), skin);
        content.add(scrapIcon).size(24, 24).padRight(5);
        content.add(costLabel);
        button.clearChildren();
        button.add(content).expand().fill();
    }

    /**
     * Setup button content with affordability indication
     */
    private void applyButtonAffordabilityState(TextButton button, boolean canAfford) {
        if (canAfford) {
            button.setColor(NORMAL_COLOR);
            button.setDisabled(false);
        } else {
            button.setColor(GREYED_OUT_COLOR);
            button.setDisabled(true);
        }
    }

    /**
     * Setup button content with affordability indication
     */
    private void setupButtonContentWithAffordability(TextButton button, int cost, CurrencyType currency, boolean canAfford) {
        Table content = new Table(skin);
        button.setText("");
        String texPath = currency != null ? currency.getTexturePath() : CurrencyType.METAL_SCRAP.getTexturePath();
        Image icon = new Image(new TextureRegionDrawable(new TextureRegion(new Texture(texPath))));
        Label costLabel = new Label(String.valueOf(cost), skin);

        // Tint the cost label red if player can't afford it
        if (!canAfford) {
            costLabel.setColor(INSUFFICIENT_FUNDS_COLOR);
        } else {
            costLabel.setColor(Color.WHITE);
        }

        content.add(icon).size(24, 24).padRight(5);
        content.add(costLabel);
        button.clearChildren();
        button.add(content).expand().fill();
    }

    /**
     * Attempts to upgrade the selected tower along the specified path.
     *
     * @param isLevelA True for Path A, false for Path B.
     */
    private void attemptUpgrade(boolean isLevelA) {
        if (selectedTower == null || currentTowerType == null) return;

        // 防止重复升级
        if (upgradeInProgress) {
            System.out.println("Upgrade already in progress, ignoring duplicate request");
            return;
        }

        upgradeInProgress = true;

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            upgradeInProgress = false;
            return;
        }

        int currentLevel = isLevelA ? stats.getLevel_A() : stats.getLevel_B();
        int maxLevel = getMaxLevelForPath(currentTowerType, isLevelA); // <-- derive max from data
        if (currentLevel >= maxLevel) {
            upgradeInProgress = false;
            return;
        }
        int nextLevel = currentLevel + 1;

        Map<Integer, UpgradeStats> upgrades = isLevelA
                ? pathAUpgradesPerTower.get(currentTowerType)
                : pathBUpgradesPerTower.get(currentTowerType);
        if (upgrades == null) {
            upgradeInProgress = false;
            return;
        }

        UpgradeStats upgrade = upgrades.get(nextLevel);
        if (upgrade == null) {
            upgradeInProgress = false;
            return;
        }

        Entity player = findPlayerEntity();
        if (player == null) {
            upgradeInProgress = false;
            return;
        }
        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) {
            upgradeInProgress = false;
            return;
        }

        // choose currency for this tower type
        CurrencyType costCurrency = currencyForTowerType(currentTowerType);

        Map<CurrencyType, Integer> costMap = new EnumMap<>(CurrencyType.class);
        costMap.put(costCurrency, upgrade.cost);

        if (!currencyManager.canAffordAndSpendCurrency(costMap)) {
            System.out.println("Not enough " + costCurrency + " for upgrade!");
            upgradeInProgress = false;
            return;
        }

        // Apply stat upgrades
        if (isLevelA) {
            stats.incrementLevel_A();
            stats.setDamage(upgrade.damage);
            if ("bank".equalsIgnoreCase(currentTowerType)) {
                // Bank Path A: increase speed, keep range at 0
                stats.setRange(0f);
                stats.setProjectileSpeed(upgrade.speed);
            } else {
                stats.setRange(upgrade.range);
            }
        } else {
            stats.incrementLevel_B();
            // Frozen mammoth skull uses Path B to control freeze time (projectileLife),
            // rather than attack cooldown / projectile speed.
            if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
                // Increase freeze duration by 0.3 seconds per Path B upgrade (stacking)
                float increment = 0.3f;
                float current = stats.getProjectileLife() > 0f ? stats.getProjectileLife() : 1.0f;
                stats.setProjectileLife(current + increment);
            } else if ("bank".equalsIgnoreCase(currentTowerType)) {
                // Bank Path B: unlock currencies; do not alter range/speed here
                stats.setRange(0f); // ensure stays zero
            } else {
                stats.setAttackCooldown(upgrade.cooldown);
                stats.setProjectileSpeed(upgrade.speed);
            }
        }

        //  Determine the higher level between A and B
        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();
        int highestLevel = Math.max(levelA, levelB);

        //  Decide which upgrade path to pull the image from
        Map<Integer, UpgradeStats> highestUpgrades =
                (levelA >= levelB) ? pathAUpgradesPerTower.get(currentTowerType)
                        : pathBUpgradesPerTower.get(currentTowerType);

        if (highestUpgrades != null) {
            UpgradeStats levelData = highestUpgrades.get(highestLevel);
            if (levelData != null && levelData.atlasPath != null) {
                TowerComponent towerComp = selectedTower.getComponent(TowerComponent.class);
                if (towerComp != null) {
                    towerComp.changeHeadTexture(levelData.atlasPath, currentLevel);
                    System.out.println("Tower texture set from path " +
                            (levelA >= levelB ? "A" : "B") +
                            " at level " + highestLevel +
                            ": " + levelData.atlasPath);
                }
            }
        }

        // If this tower has an orbiting head, update its orbit radius to match the new range
        TowerComponent towerCompForOrbit = selectedTower.getComponent(TowerComponent.class);
        if (towerCompForOrbit != null && towerCompForOrbit.hasHead()) {
            Entity head = towerCompForOrbit.getHeadEntity();
            if (head != null) {
                com.csse3200.game.components.towers.OrbitComponent orbit = head.getComponent(com.csse3200.game.components.towers.OrbitComponent.class);
                if (orbit != null) {
                    orbit.setRadius(stats.getRange());
                }
                // also update beam range so visuals/target checks match upgraded stats
                BeamAttackComponent beam = head.getComponent(BeamAttackComponent.class);
                if (beam != null) {
                    beam.setRange(stats.getRange());
                }
            }
        }

        // NEW: notify all active totem boosters to reapply their boost to this upgraded tower
        notifyTotemBoostersOfUpgrade(selectedTower);

        // CHANGED: gate the label refresh
        updateLabelsIfChanged();
        System.out.println("Upgrade successful!");

        // 重置升级标志
        upgradeInProgress = false;
    }

    // NEW: inform all totems to refresh boost for the given tower
    private void notifyTotemBoostersOfUpgrade(Entity target) {
        if (target == null) return;
        var entities = safeEntities();
        if (entities == null) return;
        for (Entity e : entities) {
            if (e == null) continue;
            StatsBoostComponent boost = e.getComponent(StatsBoostComponent.class);
            if (boost != null) {
                boost.onTargetStatsChanged(target);
            }
        }
    }

    /**
     * Finds and returns the player entity that has a CurrencyManagerComponent.
     *
     * @return The player entity, or null if not found.
     */
    private Entity findPlayerEntity() {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities) {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        }
        return null;
    }

    /**
     * Safely retrieves a copy of all entities from the entity service.
     *
     * @return An array of entities, or null if retrieval fails.
     */
    private Array<Entity> safeEntities() {
        try {
            return com.csse3200.game.services.ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Updates the labels and button states for the upgrade menu based on the selected tower's levels.
     */
    private void updateLabels() {
        if (selectedTower == null || currentTowerType == null) {
            if (sellRefundLabel != null) sellRefundLabel.setText("");
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(null);
            // Reset titles to defaults when nothing selected
            if (pathATitleLabel != null) pathATitleLabel.setText("Damage & Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Cooldown & Speed");
            return;
        }

        // Set path titles based on tower type
        if ("totem".equalsIgnoreCase(currentTowerType)) {
            if (pathATitleLabel != null) pathATitleLabel.setText("Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Increase Multiplier");
        } else if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
            // Ice tower: Path A = Range, Path B = Freeze Time
            if (pathATitleLabel != null) pathATitleLabel.setText("Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Freeze Time");
        } else if ("bank".equalsIgnoreCase(currentTowerType)) {
            // Bank: Path A = Speed, Path B title set below based on current level_B
            if (pathATitleLabel != null) pathATitleLabel.setText("Speed");
            // pathBTitleLabel set later using current level_B
        } else {
            if (pathATitleLabel != null) pathATitleLabel.setText("Damage & Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Cooldown & Speed");
        }

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            // clear/disable UI if stats missing
            pathALevelLabel.setText("Level: 0");
            pathBLevelLabel.setText("Level: 0");
            pathAButton.setDisabled(true);
            pathBButton.setDisabled(true);
            if (sellRefundLabel != null) sellRefundLabel.setText("");
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(null);
            return;
        }

        // --- Compute and display refund amount in the Sell row (icon + amount) ---
        TowerComponent towerComp = selectedTower.getComponent(TowerComponent.class);
        TowerCostComponent costComp = selectedTower.getComponent(TowerCostComponent.class);
        if (towerComp != null && costComp != null) {
            String towerTypeKey = canonicalTowerType(towerComp.getType());
            CurrencyType refundCurrencyType = currencyForTowerType(towerTypeKey);

            int totalSpent = 0;
            totalSpent += costComp.getCostForCurrency(refundCurrencyType);

            int levelA = stats.getLevel_A();
            int levelB = stats.getLevel_B();

            Map<Integer, UpgradeStats> upgradesAForRefund = pathAUpgradesPerTower.get(towerTypeKey);
            Map<Integer, UpgradeStats> upgradesBForRefund = pathBUpgradesPerTower.get(towerTypeKey);

            for (int lvl = 2; lvl <= levelA; lvl++) {
                if (upgradesAForRefund != null && upgradesAForRefund.containsKey(lvl)) {
                    totalSpent += upgradesAForRefund.get(lvl).cost;
                }
            }
            for (int lvl = 2; lvl <= levelB; lvl++) {
                if (upgradesBForRefund != null && upgradesBForRefund.containsKey(lvl)) {
                    totalSpent += upgradesBForRefund.get(lvl).cost;
                }
            }

            int refundAmount = Math.round(totalSpent * 0.75f);
            if (sellRefundLabel != null) sellRefundLabel.setText(String.valueOf(refundAmount));
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(getCurrencyDrawable(refundCurrencyType));
        } else {
            if (sellRefundLabel != null) sellRefundLabel.setText("");
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(null);
        }

        // --- existing upgrade labels and buttons ---
        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();
        int maxLevelA = getMaxLevelForPath(currentTowerType, true);   // <-- max per-path
        int maxLevelB = getMaxLevelForPath(currentTowerType, false);  // <-- max per-path

        // Bank Path B label text based on current level
        if ("bank".equalsIgnoreCase(currentTowerType) && pathBTitleLabel != null) {
            if (levelB == 1) {
                pathBTitleLabel.setText("Unlock Titanium Core");
            } else if (levelB == 2) {
                pathBTitleLabel.setText("Unlock Neuro Core");
            } else {
                pathBTitleLabel.setText("Unlock Currency");
            }
        }

        pathALevelLabel.setText("Level: " + (levelA >= maxLevelA ? "MAX" : levelA));
        // For frozen mammoth skull show the freeze duration instead of a generic label
        if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
            float freezeTime = stats.getProjectileLife() > 0f ? stats.getProjectileLife() : 1.0f;
            String freezeText = String.format("Freeze: %.1fs", freezeTime);
            String levelText = (levelB >= maxLevelB) ? "MAX" : String.valueOf(levelB);
            pathBLevelLabel.setText(freezeText + "  (Level: " + levelText + ")");
        } else {
            pathBLevelLabel.setText("Level: " + (levelB >= maxLevelB ? "MAX" : levelB));
        }

        int nextLevelA = levelA + 1;
        int nextLevelB = levelB + 1;

        // Debug: Print tower type and available upgrades
        System.out.println("DEBUG: Tower type = '" + currentTowerType + "'");
        System.out.println("DEBUG: Available upgrade types: " + pathAUpgradesPerTower.keySet());
        System.out.println("DEBUG: Current levels - A: " + levelA + ", B: " + levelB);
        System.out.println("DEBUG: Next levels - A: " + nextLevelA + ", B: " + nextLevelB);

        Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(currentTowerType);
        Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(currentTowerType);

        System.out.println("DEBUG: upgradesA = " + (upgradesA != null ? "found" : "null"));
        System.out.println("DEBUG: upgradesB = " + (upgradesB != null ? "found" : "null"));

        if (upgradesA != null) {
            System.out.println("DEBUG: upgradesA keys: " + upgradesA.keySet());
            System.out.println("DEBUG: upgradesA contains nextLevelA(" + nextLevelA + "): " + upgradesA.containsKey(nextLevelA));
            if (upgradesA.containsKey(nextLevelA)) {
                System.out.println("DEBUG: upgradesA[" + nextLevelA + "].cost = " + upgradesA.get(nextLevelA).cost);
            }
        }

        if (upgradesB != null) {
            System.out.println("DEBUG: upgradesB keys: " + upgradesB.keySet());
            System.out.println("DEBUG: upgradesB contains nextLevelB(" + nextLevelB + "): " + upgradesB.containsKey(nextLevelB));
            if (upgradesB.containsKey(nextLevelB)) {
                System.out.println("DEBUG: upgradesB[" + nextLevelB + "].cost = " + upgradesB.get(nextLevelB).cost);
            }
        }

        // determine currency for current tower type
        CurrencyType displayCurrency = currencyForTowerType(currentTowerType);

        pathAButton.clearChildren();
        if (levelA >= maxLevelA) {
            // Center "MAX" content
            setButtonCenteredText(pathAButton, "MAX");
            pathAButton.setDisabled(true);
            pathAButton.setColor(NORMAL_COLOR);
        } else {
            int costA = (upgradesA != null && upgradesA.containsKey(nextLevelA)) ? upgradesA.get(nextLevelA).cost : 0;
            boolean canAffordA = canAffordCost(costA, displayCurrency);
            setupButtonContentWithAffordability(pathAButton, costA, displayCurrency, canAffordA);
            applyButtonAffordabilityState(pathAButton, canAffordA);
        }

        // Path B button
        pathBButton.clearChildren();
        if (levelB >= maxLevelB) {
            setButtonCenteredText(pathBButton, "MAX");
            pathBButton.setDisabled(true);
            pathBButton.setColor(NORMAL_COLOR);
        } else {
            if ("bank".equalsIgnoreCase(currentTowerType)) {
                // Use the already-declared upgradesB; don't redeclare it here
                int costB = (upgradesB != null && upgradesB.containsKey(nextLevelB))
                        ? upgradesB.get(nextLevelB).cost : 0;

                CurrencyType unlockCurrency = nextLevelB == 2 ? CurrencyType.TITANIUM_CORE
                        : nextLevelB == 3 ? CurrencyType.NEUROCHIP
                        : displayCurrency;

                boolean canAffordB = canAffordCost(costB, displayCurrency);
                setupButtonContentWithAffordability(pathBButton, costB, unlockCurrency, canAffordB);
                applyButtonAffordabilityState(pathBButton, canAffordB);
            } else {
                int costB = (upgradesB != null && upgradesB.containsKey(nextLevelB))
                        ? upgradesB.get(nextLevelB).cost : 0;
                boolean canAffordB = canAffordCost(costB, displayCurrency);
                setupButtonContentWithAffordability(pathBButton, costB, displayCurrency, canAffordB);
                applyButtonAffordabilityState(pathBButton, canAffordB);
            }
        }
    }

    // Remove manual updateUpgradeData and handleUpgradeA/B for supercavemen.
    // The upgrade menu uses the upgrade maps, so supercavemen upgrades will now work automatically.

    @Override
    public void dispose() {
        // Cancel the periodic refresh task to avoid leaks
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        // Dispose the background texture if it was created
        if (bgTexture != null) {
            bgTexture.dispose();
            bgTexture = null;
        }
        // Dispose cached currency icons
        for (Texture tex : currencyIconCache.values()) {
            if (tex != null) tex.dispose();
        }
        currencyIconCache.clear();

        super.dispose();
    }

    // Returns a drawable for the given currency, with simple caching
    private Drawable getCurrencyDrawable(CurrencyType currency) {
        if (currency == null) currency = CurrencyType.METAL_SCRAP;
        Texture tex = currencyIconCache.get(currency);
        if (tex == null) {
            tex = new Texture(currency.getTexturePath());
            currencyIconCache.put(currency, tex);
        }
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    // Utility: build a 1x1 solid texture with the given color
    private static Texture buildSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    // Helper: replace a button's content with centered text
    private void setButtonCenteredText(TextButton button, String text) {
        button.setText(""); // avoid using internal TextButton label
        button.clearChildren();
        Table content = new Table(skin);
        Label label = new Label(text, skin);
        label.setAlignment(Align.center);
        content.add(label).expand().fill().center();
        button.add(content).expand().fill();
    }

    // Helper: set content to "Unlock" + currency icon (centered)
    private void setupButtonUnlockContent(TextButton button, CurrencyType currency) {
        button.setText("");
        Table content = new Table(skin);
        if (currency != null) {
            Image icon = new Image(getCurrencyDrawable(currency));
            content.add(icon).size(24, 24).padRight(5);
        }
        Label label = new Label("Unlock", skin);
        content.add(label);
        button.clearChildren();
        button.add(content).expand().fill().center();
    }

    // Helper: tint an existing drawable while preserving its 9-patch/sprite shape
    private static Drawable tintDrawable(Drawable base, Color color) {
        if (base == null || color == null) return base;
        if (base instanceof NinePatchDrawable) {
            return ((NinePatchDrawable) base).tint(color);
        } else if (base instanceof TextureRegionDrawable) {
            return ((TextureRegionDrawable) base).tint(color);
        } else if (base instanceof SpriteDrawable) {
            return ((SpriteDrawable) base).tint(color);
        }
        return base; // fallback: leave as-is
    }

    // Helper: build a tinted TextButtonStyle from an existing one
    private static TextButton.TextButtonStyle tintedStyle(TextButton.TextButtonStyle base, Color color) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle(base);
        // Use the same tint on all states to keep a consistent look; preserves original shapes
        s.up = tintDrawable(base.up, color);
        s.down = tintDrawable(base.down != null ? base.down : base.up, color);
        s.over = tintDrawable(base.over != null ? base.over : base.up, color);
        s.checked = tintDrawable(base.checked != null ? base.checked : base.up, color);
        s.disabled = tintDrawable(base.disabled != null ? base.disabled : base.up, color);
        return s;
    }

    // NEW: Check if the player can afford a given cost in a currency without spending it.
    private boolean canAffordCost(int cost, CurrencyType currency) {
        if (cost <= 0) return true;
        if (currency == null) currency = CurrencyType.METAL_SCRAP;

        Entity player = findPlayerEntity();
        if (player == null) return false;
        CurrencyManagerComponent cm = player.getComponent(CurrencyManagerComponent.class);
        if (cm == null) return false;

        // Try common getter: getCurrencyAmount(CurrencyType)
        try {
            java.lang.reflect.Method m = cm.getClass().getMethod("getCurrencyAmount", CurrencyType.class);
            Object res = m.invoke(cm, currency);
            if (res instanceof Number) {
                return ((Number) res).intValue() >= cost;
            }
        } catch (Exception ignored) {
            // fall through
        }

        // Try alternative: getAmount(CurrencyType)
        try {
            java.lang.reflect.Method m2 = cm.getClass().getMethod("getAmount", CurrencyType.class);
            Object res2 = m2.invoke(cm, currency);
            if (res2 instanceof Number) {
                return ((Number) res2).intValue() >= cost;
            }
        } catch (Exception ignored) {
            // fall through
        }

        // Try a read-only canAffordCurrency(Map<CurrencyType, Integer>)
        try {
            java.util.Map<CurrencyType, Integer> map = new java.util.EnumMap<>(CurrencyType.class);
            map.put(currency, cost);
            java.lang.reflect.Method m3 = cm.getClass().getMethod("canAffordCurrency", java.util.Map.class);
            Object res3 = m3.invoke(cm, map);
            if (res3 instanceof Boolean) {
                return (Boolean) res3;
            }
        } catch (Exception ignored) {
            // fall through
        }

        // Unknown API: don't block the UI
        return true;
    }

    // Helper: derive max level for a tower path from its upgrade map (highest key)
    private int getMaxLevelForPath(String towerType, boolean isLevelA) {
        String key = canonicalTowerType(towerType);
        Map<Integer, UpgradeStats> map = isLevelA ? pathAUpgradesPerTower.get(key) : pathBUpgradesPerTower.get(key);
        if (map == null || map.isEmpty()) return 5; // default cap
        int max = 1; // base level
        for (Integer lvl : map.keySet()) {
            if (lvl != null && lvl > max) max = lvl;
        }
        return max;
    }

    // NEW: lightweight value object for comparing UI state
    private static final class UiSnapshot {
        String towerType;
        int levelA, levelB, maxA, maxB;
        int nextCostA, nextCostB;
        int refundAmount;
        boolean canAffordA, canAffordB;
        float freezeTime; // only used for frozen skull
        boolean visible;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UiSnapshot)) return false;
            UiSnapshot s = (UiSnapshot) o;
            return visible == s.visible
                    && levelA == s.levelA
                    && levelB == s.levelB
                    && maxA == s.maxA
                    && maxB == s.maxB
                    && nextCostA == s.nextCostA
                    && nextCostB == s.nextCostB
                    && refundAmount == s.refundAmount
                    && canAffordA == s.canAffordA
                    && canAffordB == s.canAffordB
                    && Float.compare(freezeTime, s.freezeTime) == 0
                    && java.util.Objects.equals(towerType, s.towerType);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    towerType, levelA, levelB, maxA, maxB,
                    nextCostA, nextCostB, refundAmount,
                    canAffordA, canAffordB, freezeTime, visible
            );
        }
    }

    // NEW: build the current snapshot without touching UI widgets
    private UiSnapshot buildUiSnapshot() {
        UiSnapshot snap = new UiSnapshot();
        snap.visible = rootTable != null && rootTable.isVisible();

        if (!snap.visible || selectedTower == null || currentTowerType == null) {
            snap.towerType = "";
            return snap;
        }

        snap.towerType = currentTowerType;
        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);

        if (stats != null) {
            snap.levelA = stats.getLevel_A();
            snap.levelB = stats.getLevel_B();
            snap.maxA = getMaxLevelForPath(currentTowerType, true);
            snap.maxB = getMaxLevelForPath(currentTowerType, false);
            if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
                float ft = stats.getProjectileLife() > 0f ? stats.getProjectileLife() : 1.0f;
                snap.freezeTime = ft;
            }
        }

        // Costs and affordability
        int nextLevelA = snap.levelA + 1;
        int nextLevelB = snap.levelB + 1;
        var upgradesA = pathAUpgradesPerTower.get(currentTowerType);
        var upgradesB = pathBUpgradesPerTower.get(currentTowerType);
        snap.nextCostA = (upgradesA != null && upgradesA.containsKey(nextLevelA)) ? upgradesA.get(nextLevelA).cost : 0;
        snap.nextCostB = (upgradesB != null && upgradesB.containsKey(nextLevelB)) ? upgradesB.get(nextLevelB).cost : 0;

        CurrencyType displayCurrency = currencyForTowerType(currentTowerType);
        snap.canAffordA = canAffordCost(snap.nextCostA, displayCurrency);
        snap.canAffordB = canAffordCost(snap.nextCostB, displayCurrency);

        // Refund amount
        TowerComponent towerComp = selectedTower.getComponent(TowerComponent.class);
        TowerCostComponent costComp = selectedTower.getComponent(TowerCostComponent.class);
        if (towerComp != null && costComp != null) {
            String towerKey = canonicalTowerType(towerComp.getType());
            CurrencyType refundCur = currencyForTowerType(towerKey);
            int total = costComp.getCostForCurrency(refundCur);

            if (upgradesA != null) {
                for (int lvl = 2; lvl <= snap.levelA; lvl++) {
                    var u = upgradesA.get(lvl);
                    if (u != null) total += u.cost;
                }
            }
            if (upgradesB != null) {
                for (int lvl = 2; lvl <= snap.levelB; lvl++) {
                    var u = upgradesB.get(lvl);
                    if (u != null) total += u.cost;
                }
            }
            snap.refundAmount = Math.round(total * 0.75f);
        }

        return snap;
    }

    // NEW: only applies UI if something actually changed
    private void updateLabelsIfChanged() {
        // If the selected tower was despawned or is inactive, hide the menu and clear cache
        if (selectedTower != null && (!isEntityRegistered(selectedTower) || !selectedTower.isActive())) {
            selectedTower = null;
            if (rootTable != null) {
                rootTable.setVisible(false);
            }
            lastUiSnapshot = null; // clear cache so next open renders fresh
            return;
        }

        UiSnapshot snap = buildUiSnapshot();
        if (lastUiSnapshot != null && lastUiSnapshot.equals(snap)) {
            return; // nothing changed, skip any UI work
        }
        lastUiSnapshot = snap;
        updateLabels(); // reuse existing method to render the new state
    }

    // Helper: check if an entity is still registered with the entity service
    private boolean isEntityRegistered(Entity target) {
        if (target == null) return false;
        Array<Entity> list = safeEntities();
        if (list == null) return false;
        for (Entity e : list) {
            if (e == target) {
                return true;
            }
        }
        return false;
    }
}