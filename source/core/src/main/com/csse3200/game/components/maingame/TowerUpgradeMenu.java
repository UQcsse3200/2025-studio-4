package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;

import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

public class TowerUpgradeMenu extends UIComponent {
    private Table table;
    private Entity selectedTower = null;

    // Upgrade costs
    private static final int DAMAGE_UPGRADE_COST = 50;
    private static final int RANGE_UPGRADE_COST = 40;
    private static final int SPEED_UPGRADE_COST = 60;
    private static final CurrencyType UPGRADE_CURRENCY = CurrencyType.METAL_SCRAP;

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.center().right();   // Position menu in bottom-right corner
        table.setVisible(false);  // Hide initially
        table.setFillParent(true);

        // Buttons for upgrades
        TextButton damageBtn = new TextButton("Upgrade Damage 50", skin);
        Container<TextButton> damageContainer = new Container<>(damageBtn);
        damageContainer.setTouchable(Touchable.enabled);
        table.add(damageContainer).pad(3).row();

        TextButton rangeBtn = new TextButton("Upgrade Range 40", skin);
        Container<TextButton> rangeContainer = new Container<>(rangeBtn);
        rangeContainer.setTouchable(Touchable.enabled);
        table.add(rangeContainer).pad(5).row();

        TextButton speedBtn = new TextButton("Upgrade Speed 60", skin);
        Container<TextButton> speedContainer = new Container<>(speedBtn);
        speedContainer.setTouchable(Touchable.enabled);
        table.add(speedContainer).pad(5).row();

        // Damage upgrade
        damageBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(DAMAGE_UPGRADE_COST, stats -> stats.setDamage(stats.getDamage() + 1));
            }
        });

        // Range upgrade
        rangeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(RANGE_UPGRADE_COST, stats -> stats.setRange(stats.getRange() + 1));
            }
        });

        // Speed upgrade
        speedBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptUpgrade(SPEED_UPGRADE_COST, stats -> stats.setAttackCooldown(Math.max(0.1f, stats.getAttackCooldown() - 0.1f)));
            }
        });

        stage.addActor(table);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {});
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        //
    }

    public void setSelectedTower(Entity tower) {
        this.selectedTower = tower;
        table.setVisible(tower != null);
    }

    public boolean isTouched(int screenX, int screenY) {
        Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return table.hit(stageCoords.x, stageCoords.y, true) != null;
    }

    // --- Helper methods ---

    private interface UpgradeAction {
        void apply(TowerStatsComponent stats);
    }

    private void attemptUpgrade(int cost, UpgradeAction action) {
        if (selectedTower == null) return;

        Entity player = findPlayerEntity();
        if (player == null) return;

        CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
        if (currencyManager == null) return;

        if (!currencyManager.canAffordAndSpendSingleCurrency(UPGRADE_CURRENCY, cost)) {
            System.out.println("Not enough " + UPGRADE_CURRENCY + " for upgrade!");
            return;
        }

        TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
        action.apply(stats);

        System.out.println("Upgrade successful! Remaining " + UPGRADE_CURRENCY + ": " +
                currencyManager.getCurrencyAmount(UPGRADE_CURRENCY));
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
}
