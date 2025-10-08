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
import com.badlogic.gdx.InputAdapter;

import com.csse3200.game.entities.Entity;

import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.components.towers.TowerCostComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.towers.OrbitComponent;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.maingame.TowerUpgradeData.UpgradeStats;

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

    private static final CurrencyType UPGRADE_CURRENCY = CurrencyType.METAL_SCRAP;

    private final Map<String, Map<Integer, UpgradeStats>> pathAUpgradesPerTower = TowerUpgradeData.getPathAUpgrades();
    private final Map<String, Map<Integer, UpgradeStats>> pathBUpgradesPerTower = TowerUpgradeData.getPathBUpgrades();

    /**
     * Creates the UI for the tower upgrade menu and sets up listeners.
     */
    @Override
    public void create() {
        super.create();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().right().pad(20); // restore outer padding so menu is offset from edges
        rootTable.setVisible(false);

        // Background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.3f, 0.0f, 1f));
        pixmap.fill();
        Texture backgroundTexture = new Texture(pixmap);
        pixmap.dispose();
        Drawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(backgroundTexture));

        Container<Table> container = new Container<>();
        container.setBackground(backgroundDrawable);
        container.pad(15); // restore inner padding

        Label title = new Label("Tower Upgrades", skin, "title");
        title.setColor(Color.WHITE);

        // Path A
        Table pathATable = new Table(skin);
        pathATable.defaults().pad(5);
        Label pathALabel = new Label("Damage & Range", skin);
        pathALabel.setColor(Color.ORANGE);
        pathALevelLabel = new Label("Level: 1", skin);
        pathAButton = new TextButton("", skin);
        pathATable.add(pathALabel).row();
        pathATable.add(pathALevelLabel).row();
        pathATable.add(pathAButton).width(180).row();

        // Path B
        Table pathBTable = new Table(skin);
        pathBTable.defaults().pad(5);
        Label pathBLabel = new Label("Cooldown & Speed", skin);
        pathBLabel.setColor(Color.SKY);
        pathBLevelLabel = new Label("Level: 1", skin);
        pathBButton = new TextButton("", skin);
        pathBTable.add(pathBLabel).row();
        pathBTable.add(pathBLevelLabel).row();
        pathBTable.add(pathBButton).width(180).row();

        // Combine sections
        Table content = new Table(skin);
        content.defaults().pad(10);
        content.add(pathATable).padRight(20);
        content.add(pathBTable);

        Table containerContent = new Table(skin);
        containerContent.add(title).colspan(2).center().padBottom(15).row();
        containerContent.add(content);
        container.setActor(containerContent);

        // --- Sell button ---
        TextButton sellButton = new TextButton("Sell", skin);
        sellButton.setColor(Color.RED);  // optional: make it stand out
        containerContent.row().padTop(15);
        containerContent.add(sellButton).colspan(2).width(180).center();

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

                // --- Calculate total spent ---
                int totalSpent = 0;
                // choose refund currency based on tower type
                CurrencyType refundCurrencyType = currencyForTowerType(towerComp.getType());

                // Base cost (uses chosen currency)
                totalSpent += costComp.getCostForCurrency(refundCurrencyType);

                // Path A upgrades
                Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(towerComp.getType() == null ? "" : towerComp.getType().toLowerCase());
                int levelA = statsComp.getLevel_A();
                for (int lvl = 2; lvl <= levelA; lvl++) {
                    if (upgradesA != null && upgradesA.containsKey(lvl)) {
                        totalSpent += upgradesA.get(lvl).cost;
                    }
                }

                // Path B upgrades
                Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(towerComp.getType() == null ? "" : towerComp.getType().toLowerCase());
                int levelB = statsComp.getLevel_B();
                for (int lvl = 2; lvl <= levelB; lvl++) {
                    if (upgradesB != null && upgradesB.containsKey(lvl)) {
                        totalSpent += upgradesB.get(lvl).cost;
                    }
                }

                // Refund 75% of total spent — use EnumMap for compatibility
                Map<CurrencyType, Integer> refundMap = new EnumMap<>(CurrencyType.class);
                refundMap.put(refundCurrencyType, totalSpent);
                currencyManager.refundCurrency(refundMap, 0.75f);

                // Remove the tower's head entity if present
                if (towerComp.hasHead()) {
                    Entity head = towerComp.getHeadEntity();
                    if (head != null) {
                        head.dispose();
                        com.csse3200.game.services.ServiceLocator.getEntityService().unregister(head);
                    }
                }
                // Remove the tower entity
                selectedTower.dispose();
                com.csse3200.game.services.ServiceLocator.getEntityService().unregister(selectedTower);
                selectedTower = null;
                rootTable.setVisible(false);
            }
        });

        // Replace the original add call so the container is anchored bottom-right of the stage
        rootTable.add(container).expand().bottom().right().pad(20); // restore previous padding/placement

        stage.addActor(rootTable);

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
        // Store canonical tower type key so lookups always hit the upgrade maps
        this.currentTowerType = canonicalTowerType(towerType);
        rootTable.setVisible(tower != null);
        updateLabels();
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

    // Helper: choose upgrade/refund currency per tower type
    private static CurrencyType currencyForTowerType(String towerType) {
        String key = canonicalTowerType(towerType);
        if (key == null) return UPGRADE_CURRENCY;
        if ("pteradactyl".equalsIgnoreCase(key)) {
            return CurrencyType.TITANIUM_CORE;
        }
        // Totem uses METAL_SCRAP (default)
        return UPGRADE_CURRENCY;
    }

    private static String canonicalTowerType(String towerType) {
        if (towerType == null) return "";
        String s = towerType.toLowerCase().trim();
        if (s.equals("pteradactyl") || s.equals("pterodactyl") || s.startsWith("ptero")) {
            return "pteradactyl";
        }
        if (s.equals("totem")) {
            return "totem";
        }
        if (s.equals("supercavemen")) {
            return "supercavemen";
        }
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

    // Overload: show currency icon based on currency type
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
     * Attempts to upgrade the selected tower along the specified path.
     *
     * @param isLevelA True for Path A, false for Path B.
     */
    private void attemptUpgrade(boolean isLevelA) {
        if (selectedTower == null || currentTowerType == null) return;

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) return;

        int currentLevel = isLevelA ? stats.getLevel_A() : stats.getLevel_B();
        if (currentLevel >= 5) return;
        int nextLevel = currentLevel + 1;

        Map<Integer, UpgradeStats> upgrades = isLevelA
                ? pathAUpgradesPerTower.get(currentTowerType)
                : pathBUpgradesPerTower.get(currentTowerType);
        if (upgrades == null) return;

        UpgradeStats upgrade = upgrades.get(nextLevel);
        if (upgrade == null) return;

        Entity player = findPlayerEntity();
        if (player == null) return;
        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) return;

        // choose currency for this tower type
        CurrencyType costCurrency = currencyForTowerType(currentTowerType);

        Map<CurrencyType, Integer> costMap = new EnumMap<>(CurrencyType.class);
        costMap.put(costCurrency, upgrade.cost);

        if (!currencyManager.canAffordAndSpendCurrency(costMap)) {
            System.out.println("Not enough " + costCurrency + " for upgrade!");
            return;
        }

        // 🔹 Apply stat upgrades
        if (isLevelA) {
            stats.incrementLevel_A();
            stats.setDamage(upgrade.damage);
            stats.setRange(upgrade.range);
        } else {
            stats.incrementLevel_B();
            stats.setAttackCooldown(upgrade.cooldown);
            stats.setProjectileSpeed(upgrade.speed);
        }

        // 🔹 Determine the higher level between A and B
        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();
        int highestLevel = Math.max(levelA, levelB);

        // 🔹 Decide which upgrade path to pull the image from
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
                OrbitComponent orbit = head.getComponent(OrbitComponent.class);
                if (orbit != null) {
                    // use the updated stats range so orbit follows upgrades
                    orbit.setRadius(stats.getRange());
                }
            }
        }

        updateLabels();
        System.out.println("Upgrade successful!");
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
        if (selectedTower == null || currentTowerType == null) return;

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            // clear/disable UI if stats missing
            pathALevelLabel.setText("Level: 0");
            pathBLevelLabel.setText("Level: 0");
            pathAButton.setDisabled(true);
            pathBButton.setDisabled(true);
            return;
        }

        int levelA = stats.getLevel_A();
        int levelB = stats.getLevel_B();

        pathALevelLabel.setText("Level: " + (levelA >= 5 ? "MAX" : levelA));
        pathBLevelLabel.setText("Level: " + (levelB >= 5 ? "MAX" : levelB));

        int nextLevelA = levelA + 1;
        int nextLevelB = levelB + 1;

        Map<Integer, UpgradeStats> upgradesA = pathAUpgradesPerTower.get(currentTowerType);
        Map<Integer, UpgradeStats> upgradesB = pathBUpgradesPerTower.get(currentTowerType);

        // determine currency for current tower type
        CurrencyType displayCurrency = currencyForTowerType(currentTowerType);

        // --- Path A button ---
        pathAButton.clearChildren();
        if (levelA >= 5) {
            Label maxLabel = new Label("MAX", skin);
            pathAButton.add(maxLabel).expand().fill().center();
            pathAButton.setDisabled(true);
        } else {
            int costA = (upgradesA != null && upgradesA.containsKey(nextLevelA)) ? upgradesA.get(nextLevelA).cost : 0;
            setupButtonContent(pathAButton, costA, displayCurrency);
            pathAButton.setDisabled(false);
        }

        // --- Path B button ---
        pathBButton.clearChildren();
        if (levelB >= 5) {
            Label maxLabel = new Label("MAX", skin);
            pathBButton.add(maxLabel).expand().fill().center();
            pathBButton.setDisabled(true);
        } else {
            int costB = (upgradesB != null && upgradesB.containsKey(nextLevelB)) ? upgradesB.get(nextLevelB).cost : 0;
            setupButtonContent(pathBButton, costB, displayCurrency);
            pathBButton.setDisabled(false);
        }

    }

    // Remove manual updateUpgradeData and handleUpgradeA/B for supercavemen.
    // The upgrade menu uses the upgrade maps, so supercavemen upgrades will now work automatically.
}
