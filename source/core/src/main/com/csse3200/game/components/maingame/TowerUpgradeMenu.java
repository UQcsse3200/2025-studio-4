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

public class TowerUpgradeMenu extends UIComponent {
    private Table table;
    private Entity selectedTower = null;

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.bottom().right();   // Position menu in bottom-right corner
        table.setVisible(false); // Hide initially
        table.setFillParent(true);

        // Buttons for upgrades
        TextButton damageBtn = new TextButton("Upgrade Damage", skin);
        Container<TextButton> damageContainer = new Container<>(damageBtn);
        damageContainer.setTouchable(Touchable.enabled); // full area clickable
        table.add(damageContainer).pad(5).row();

        TextButton rangeBtn = new TextButton("Upgrade Range", skin);
        Container<TextButton> rangeContainer = new Container<>(rangeBtn);
        rangeContainer.setTouchable(Touchable.enabled);
        table.add(rangeContainer).pad(5).row();

        TextButton speedBtn = new TextButton("Upgrade Speed", skin);
        Container<TextButton> speedContainer = new Container<>(speedBtn);
        speedContainer.setTouchable(Touchable.enabled);
        table.add(speedContainer).pad(5).row();

        // Use ClickListener so upgrades only happen on click
        damageBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedTower != null) {
                    TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
                    stats.setDamage(stats.getDamage() + 1); // Increase damage
                    System.out.println(stats.getDamage());
                }
            }
        });

        rangeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedTower != null) {
                    TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
                    stats.setRange(stats.getRange() + 1); // Increase range
                    System.out.println(stats.getRange());
                }
            }
        });

        speedBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedTower != null) {
                    TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
                    stats.setAttackCooldown(Math.max(0.1f, stats.getAttackCooldown() - 0.1f)); // Decrease cooldown
                    System.out.println(stats.getAttackCooldown());
                }
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

    // Called by MapHighlighter to update which tower is selected
    public void setSelectedTower(Entity tower) {
        this.selectedTower = tower;
        table.setVisible(tower != null);
    }

    public boolean isTouched(int screenX, int screenY) {
        Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return table.hit(stageCoords.x, stageCoords.y, true) != null;
    }
}