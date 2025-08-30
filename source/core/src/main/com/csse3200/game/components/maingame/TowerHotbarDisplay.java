package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.ui.UIComponent;

public class TowerHotbarDisplay extends UIComponent {
    private Table table;

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.bottom().left();
        table.setFillParent(true);

        TextButton baseBtn = new TextButton("Base", skin);
        baseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementBase fired");
                entity.getEvents().trigger("startPlacementBase");
            }
        });

        TextButton sunBtn = new TextButton("Sun", skin);
        sunBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementSun fired");
                entity.getEvents().trigger("startPlacementSun");
            }
        });

        table.add(baseBtn).pad(8f);
        table.add(sunBtn).pad(8f);
        stage.addActor(table);
    }

    @Override public void draw(SpriteBatch batch) { /* stage draws */ }
    @Override public void dispose() { table.clear(); super.dispose(); }
}
