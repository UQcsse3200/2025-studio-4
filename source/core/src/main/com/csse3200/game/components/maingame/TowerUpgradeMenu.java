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

import java.util.Map;
import java.util.EnumMap;

/**
 * UI component for displaying and upgrading towers.
 * Shows separate upgrade paths for each tower type.
 */
public class TowerUpgradeMenu extends UIComponent {

    private Table rootTable;
    private Entity selectedTower = null;
    private String currentTowerType;

    private Label pathALevelLabel;
    private Label pathBLevelLabel;
    private TextButton pathAButton;
    private TextButton pathBButton;
    private Label pathATitleLabel;
    private Label pathBTitleLabel;
    private Label sellRefundLabel;
    private Image sellRefundIcon;
    private Table sellRefundRow;

    private static final CurrencyType UPGRADE_CURRENCY = CurrencyType.METAL_SCRAP;
    private static final Color GREYED_OUT_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.6f);
    private static final Color NORMAL_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color INSUFFICIENT_FUNDS_COLOR = new Color(1f, 0.3f, 0.3f, 1f); // Red tint for cost label

    private final Map<String, Map<Integer, UpgradeStats>> pathAUpgradesPerTower;
    private final Map<String, Map<Integer, UpgradeStats>> pathBUpgradesPerTower;

    private boolean upgradeInProgress = false;
    private Texture bgTexture;
    private final EnumMap<CurrencyType, Texture> currencyIconCache = new EnumMap<>(CurrencyType.class);

    public TowerUpgradeMenu() {
        this.pathAUpgradesPerTower = TowerUpgradeData.getPathAUpgrades();
        this.pathBUpgradesPerTower = TowerUpgradeData.getPathBUpgrades();

        System.out.println("DEBUG: Loaded upgrade data for towers: " + pathAUpgradesPerTower.keySet());
    }

    @Override
    public void create() {
        super.create();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().padBottom(0);
        rootTable.setVisible(false);

        bgTexture = buildSolidTexture(new Color(0.15f, 0.15f, 0.18f, 0.9f));
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(15);

        Label title = new Label("Tower Upgrades", skin, "title");
        title.setColor(Color.WHITE);

        // Path A
        Table pathATable = new Table(skin);
        pathATable.defaults().pad(5);
        pathATitleLabel = new Label("Damage & Range", skin);
        pathATitleLabel.setColor(Color.ORANGE);
        pathALevelLabel = new Label("Level: 1", skin);
        pathAButton = new TextButton("", skin);
        pathATable.add(pathATitleLabel).row();
        pathATable.add(pathALevelLabel).row();
        pathATable.add(pathAButton).width(180).row();

        {
            TextButton.TextButtonStyle base = new TextButton.TextButtonStyle(pathAButton.getStyle());
            TextButton.TextButtonStyle tinted = tintedStyle(base, new Color(pathATitleLabel.getColor()));
            pathAButton.setStyle(tinted);
        }

        // Path B
        Table pathBTable = new Table(skin);
        pathBTable.defaults().pad(5);
        pathBTitleLabel = new Label("Cooldown & Speed", skin);
        pathBTitleLabel.setColor(Color.SKY);
        pathBLevelLabel = new Label("Level: 1", skin);
        pathBButton = new TextButton("", skin);
        pathBTable.add(pathBTitleLabel).row();
        pathBTable.add(pathBLevelLabel).row();
        pathBTable.add(pathBButton).width(180).row();

        {
            TextButton.TextButtonStyle base = new TextButton.TextButtonStyle(pathBButton.getStyle());
            TextButton.TextButtonStyle tinted = tintedStyle(base, new Color(pathBTitleLabel.getColor()));
            pathBButton.setStyle(tinted);
        }

        // Sell button
        TextButton sellButton = new TextButton("Sell", skin);
        sellButton.setColor(Color.RED);
        Table sellTable = new Table(skin);
        sellTable.defaults().pad(5);
        Label sellLabel = new Label("Sell Tower", skin);
        sellLabel.setColor(Color.RED);
        sellTable.add(sellLabel).row();

        sellRefundLabel = new Label("0", skin);
        sellRefundLabel.setColor(Color.WHITE);
        sellRefundLabel.setAlignment(Align.center);
        sellRefundIcon = new Image(getCurrencyDrawable(CurrencyType.METAL_SCRAP));
        sellRefundRow = new Table(skin);
        sellRefundRow.add(sellRefundIcon).size(24, 24).padRight(5);
        sellRefundRow.add(sellRefundLabel);
        sellTable.add(sellRefundRow).row();
        sellTable.add(sellButton).width(160).row();

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

                int totalSpent = 0;
                String towerTypeKey = canonicalTowerType(towerComp.getType());
                CurrencyType refundCurrencyType = currencyForTowerType(towerTypeKey);

                totalSpent += costComp.getCostForCurrency(refundCurrencyType);

                Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(towerTypeKey);
                int levelA = statsComp.getLevel_A();
                for (int lvl = 2; lvl <= levelA; lvl++) {
                    if (upgradesA != null && upgradesA.containsKey(lvl)) {
                        totalSpent += upgradesA.get(lvl).cost;
                    }
                }

                Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(towerTypeKey);
                int levelB = statsComp.getLevel_B();
                for (int lvl = 2; lvl <= levelB; lvl++) {
                    if (upgradesB != null && upgradesB.containsKey(lvl)) {
                        totalSpent += upgradesB.get(lvl).cost;
                    }
                }

                Map<CurrencyType, Integer> refundMap = new EnumMap<>(CurrencyType.class);
                refundMap.put(refundCurrencyType, totalSpent);
                currencyManager.refundCurrency(refundMap, 0.75f);

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

        Table content = new Table(skin);
        content.defaults().pad(10).top();
        content.add(pathATable).padRight(20).top();
        content.add(pathBTable).padRight(20).top();
        content.add(sellTable).top();

        Table containerContent = new Table(skin);
        containerContent.add(title).colspan(3).center().padBottom(15).row();
        containerContent.add(content).center();
        container.setActor(containerContent);

        float screenW = Gdx.graphics.getWidth();
        float desiredWidth = Math.max(600f, Math.min(screenW * 0.55f, 900f));
        rootTable.add(container)
                .width(desiredWidth)
                .bottom()
                .center()
                .padBottom(0);

        stage.addActor(rootTable);

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

    @Override
    protected void draw(SpriteBatch batch) { }

    public void setSelectedTower(Entity tower, String towerType) {
        this.selectedTower = tower;
        if (towerType == null && tower != null) {
            TowerComponent tc = tower.getComponent(TowerComponent.class);
            towerType = tc != null ? tc.getType() : "";
        }
        this.currentTowerType = canonicalTowerType(towerType);
        rootTable.setVisible(tower != null);
        updateLabels();
    }

    public boolean isTouched(int screenX, int screenY) {
        Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return rootTable.hit(stageCoords.x, stageCoords.y, true) != null;
    }

    private static CurrencyType currencyForTowerType(String towerType) {
        String key = canonicalTowerType(towerType);
        if (key == null || key.isEmpty()) return UPGRADE_CURRENCY;

        if ("pteradactyl".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("totem".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("bank".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("raft".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("frozenmamoothskull".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("bouldercatapult".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("cavemenvillage".equalsIgnoreCase(key)) return CurrencyType.TITANIUM_CORE;
        if ("villageshaman".equalsIgnoreCase(key)) return CurrencyType.NEUROCHIP;
        if ("supercavemen".equalsIgnoreCase(key)) return CurrencyType.NEUROCHIP;

        return UPGRADE_CURRENCY;
    }

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

    private void setupButtonContent(TextButton button, int cost) {
        Table content = new Table(skin);
        button.setText("");
        Image scrapIcon = new Image(new TextureRegionDrawable(
                new TextureRegion(new Texture(CurrencyType.METAL_SCRAP.getTexturePath()))));
        Label costLabel = new Label(String.valueOf(cost), skin);
        content.add(scrapIcon).size(24, 24).padRight(5);
        content.add(costLabel);
        button.clearChildren();
        button.add(content).expand().fill();
    }

    private void setupButtonContent(TextButton button, int cost, CurrencyType currency) {
        Table content = new Table(skin);
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
     * Check if player can afford the given cost in the specified currency
     */
    private boolean canAffordCost(int cost, CurrencyType currency) {
        Entity player = findPlayerEntity();
        if (player == null) return false;
        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) return false;

        return currencyManager.getCurrencyAmount(currency) >= cost;
    }

    /**
     * Apply visual state to button based on affordability
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

    private void attemptUpgrade(boolean isLevelA) {
        if (selectedTower == null || currentTowerType == null) return;

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
        int maxLevel = getMaxLevelForPath(currentTowerType, isLevelA);
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

        CurrencyType costCurrency = currencyForTowerType(currentTowerType);

        Map<CurrencyType, Integer> costMap = new EnumMap<>(CurrencyType.class);
        costMap.put(costCurrency, upgrade.cost);

        if (!currencyManager.canAffordAndSpendCurrency(costMap)) {
            System.out.println("Not enough " + costCurrency + " for upgrade!");
            upgradeInProgress = false;
            return;
        }

        if (isLevelA) {
            stats.incrementLevel_A();
            stats.setDamage(upgrade.damage);
            if ("bank".equalsIgnoreCase(currentTowerType)) {
                stats.setRange(0f);
                stats.setProjectileSpeed(upgrade.speed);
            } else {
                stats.setRange(upgrade.range);
            }
        } else {
            stats.incrementLevel_B();
            if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
                float increment = 0.3f;
                float current = stats.getProjectileLife() > 0f ? stats.getProjectileLife() : 1.0f;
                stats.setProjectileLife(current + increment);
            } else if ("bank".equalsIgnoreCase(currentTowerType)) {
                stats.setRange(0f);
            } else {
                stats.setAttackCooldown(upgrade.cooldown);
                stats.setProjectileSpeed(upgrade.speed);
            }
        }

        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();
        int highestLevel = Math.max(levelA, levelB);

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

        TowerComponent towerCompForOrbit = selectedTower.getComponent(TowerComponent.class);
        if (towerCompForOrbit != null && towerCompForOrbit.hasHead()) {
            Entity head = towerCompForOrbit.getHeadEntity();
            if (head != null) {
                com.csse3200.game.components.towers.OrbitComponent orbit = head.getComponent(com.csse3200.game.components.towers.OrbitComponent.class);
                if (orbit != null) {
                    orbit.setRadius(stats.getRange());
                }
                BeamAttackComponent beam = head.getComponent(BeamAttackComponent.class);
                if (beam != null) {
                    beam.setRange(stats.getRange());
                }
            }
        }

        updateLabels();
        System.out.println("Upgrade successful!");

        upgradeInProgress = false;
    }

    private Entity findPlayerEntity() {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities) {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        }
        return null;
    }

    private Array<Entity> safeEntities() {
        try {
            return com.csse3200.game.services.ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    private void updateLabels() {
        if (selectedTower == null || currentTowerType == null) {
            if (sellRefundLabel != null) sellRefundLabel.setText("");
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(null);
            if (pathATitleLabel != null) pathATitleLabel.setText("Damage & Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Cooldown & Speed");
            return;
        }

        if ("totem".equalsIgnoreCase(currentTowerType)) {
            if (pathATitleLabel != null) pathATitleLabel.setText("Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Increase Multiplier");
        } else if ("frozenmamoothskull".equalsIgnoreCase(currentTowerType)) {
            if (pathATitleLabel != null) pathATitleLabel.setText("Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Freeze Time");
        } else if ("bank".equalsIgnoreCase(currentTowerType)) {
            if (pathATitleLabel != null) pathATitleLabel.setText("Speed");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Unlock Currency");
        } else {
            if (pathATitleLabel != null) pathATitleLabel.setText("Damage & Range");
            if (pathBTitleLabel != null) pathBTitleLabel.setText("Cooldown & Speed");
        }

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            pathALevelLabel.setText("Level: 0");
            pathBLevelLabel.setText("Level: 0");
            pathAButton.setDisabled(true);
            pathBButton.setDisabled(true);
            if (sellRefundLabel != null) sellRefundLabel.setText("");
            if (sellRefundIcon != null) sellRefundIcon.setDrawable(null);
            return;
        }

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

        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();
        int maxLevelA = getMaxLevelForPath(currentTowerType, true);
        int maxLevelB = getMaxLevelForPath(currentTowerType, false);

        pathALevelLabel.setText("Level: " + (levelA >= maxLevelA ? "MAX" : levelA));
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

        Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(currentTowerType);
        Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(currentTowerType);

        CurrencyType displayCurrency = currencyForTowerType(currentTowerType);

        pathAButton.clearChildren();
        if (levelA >= maxLevelA) {
            setButtonCenteredText(pathAButton, "MAX");
            pathAButton.setDisabled(true);
            pathAButton.setColor(NORMAL_COLOR);
        } else {
            int costA = (upgradesA != null && upgradesA.containsKey(nextLevelA)) ? upgradesA.get(nextLevelA).cost : 0;
            boolean canAffordA = canAffordCost(costA, displayCurrency);
            setupButtonContentWithAffordability(pathAButton, costA, displayCurrency, canAffordA);
            applyButtonAffordabilityState(pathAButton, canAffordA);
        }

        pathBButton.clearChildren();
        if (levelB >= maxLevelB) {
            setButtonCenteredText(pathBButton, "MAX");
            pathBButton.setDisabled(true);
            pathBButton.setColor(NORMAL_COLOR);
        } else {
            if ("bank".equalsIgnoreCase(currentTowerType)) {
                CurrencyType unlockCurrency = nextLevelB == 2 ? CurrencyType.TITANIUM_CORE
                        : nextLevelB == 3 ? CurrencyType.NEUROCHIP
                        : null;
                setupButtonUnlockContent(pathBButton, unlockCurrency);
                pathBButton.setDisabled(false);
                pathBButton.setColor(NORMAL_COLOR);
            } else {
                int costB = (upgradesB != null && upgradesB.containsKey(nextLevelB)) ? upgradesB.get(nextLevelB).cost : 0;
                boolean canAffordB = canAffordCost(costB, displayCurrency);
                setupButtonContentWithAffordability(pathBButton, costB, displayCurrency, canAffordB);
                applyButtonAffordabilityState(pathBButton, canAffordB);
            }
        }
    }

    @Override
    public void dispose() {
        if (bgTexture != null) {
            bgTexture.dispose();
            bgTexture = null;
        }
        for (Texture tex : currencyIconCache.values()) {
            if (tex != null) tex.dispose();
        }
        currencyIconCache.clear();

        super.dispose();
    }

    private Drawable getCurrencyDrawable(CurrencyType currency) {
        if (currency == null) currency = CurrencyType.METAL_SCRAP;
        Texture tex = currencyIconCache.get(currency);
        if (tex == null) {
            tex = new Texture(currency.getTexturePath());
            currencyIconCache.put(currency, tex);
        }
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private static Texture buildSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void setButtonCenteredText(TextButton button, String text) {
        button.setText("");
        button.clearChildren();
        Table content = new Table(skin);
        Label label = new Label(text, skin);
        label.setAlignment(Align.center);
        content.add(label).expand().fill().center();
        button.add(content).expand().fill();
    }

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

    private static Drawable tintDrawable(Drawable base, Color color) {
        if (base == null || color == null) return base;
        if (base instanceof NinePatchDrawable) {
            return ((NinePatchDrawable) base).tint(color);
        } else if (base instanceof TextureRegionDrawable) {
            return ((TextureRegionDrawable) base).tint(color);
        } else if (base instanceof SpriteDrawable) {
            return ((SpriteDrawable) base).tint(color);
        }
        return base;
    }

    private static TextButton.TextButtonStyle tintedStyle(TextButton.TextButtonStyle base, Color color) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle(base);
        s.up = tintDrawable(base.up, color);
        s.down = tintDrawable(base.down != null ? base.down : base.up, color);
        s.over = tintDrawable(base.over != null ? base.over : base.up, color);
        s.checked = tintDrawable(base.checked != null ? base.checked : base.up, color);
        s.disabled = tintDrawable(base.disabled != null ? base.disabled : base.up, color);
        return s;
    }

    private int getMaxLevelForPath(String towerType, boolean isLevelA) {
        String key = canonicalTowerType(towerType);
        Map<Integer, UpgradeStats> map = isLevelA ? pathAUpgradesPerTower.get(key) : pathBUpgradesPerTower.get(key);
        if (map == null || map.isEmpty()) return 5;
        int max = 1;
        for (Integer lvl : map.keySet()) {
            if (lvl != null && lvl > max) max = lvl;
        }
        return max;
    }
}
