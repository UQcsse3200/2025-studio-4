package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.ui.UIComponent;

public class TowerHotbarDisplay extends UIComponent {
    private Table table;
    private Table table2;

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.bottom().left();
        table.setFillParent(true);

        TextureRegionDrawable sunImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/sun.png")));
        TextureRegionDrawable baseImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/base_tower.png")));

        ImageButton baseBtn = new ImageButton(baseImage);
        baseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementBase fired");
                entity.getEvents().trigger("startPlacementBase");
            }
        });

        ImageButton sunBtn = new ImageButton(sunImage);
        sunBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementSun fired");
                entity.getEvents().trigger("startPlacementSun");
            }
        });

        sunBtn.getImageCell().size(100, 100);
        baseBtn.getImageCell().size(100, 100);

        table.add(baseBtn).pad(8f);
        table.row();
        table.add(sunBtn).pad(8f);
        stage.addActor(table);
    }

    @Override public void draw(SpriteBatch batch) { /* stage draws */ }
    @Override public void dispose() { table.clear(); super.dispose(); }
}
