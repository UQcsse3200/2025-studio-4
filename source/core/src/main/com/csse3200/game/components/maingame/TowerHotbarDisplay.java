package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.ui.UIComponent;

public class TowerHotbarDisplay extends UIComponent {
    private Table table;

    @Override
    public void create() {
        super.create();
        table = new Table();
        table.bottom().left();
        table.setFillParent(true);

        TextureRegionDrawable dinoImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/dino.png")));
        TextureRegionDrawable boneImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/bone.png")));
        TextureRegionDrawable cavemenImage = new TextureRegionDrawable(new TextureRegion(new Texture("images/cavemen.png")));

        ImageButton boneBtn = new ImageButton(boneImage);
        boneBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementBone fired");
                entity.getEvents().trigger("startPlacementBone");
            }
        });


        ImageButton dinoBtn = new ImageButton(dinoImage);
        dinoBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementDino fired");
                entity.getEvents().trigger("startPlacementDino");
            }
        });

        // Cavemen Tower button
        ImageButton cavemenBtn = new ImageButton(cavemenImage);
        cavemenBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println(">>> startPlacementCavemen fired");
                entity.getEvents().trigger("startPlacementCavemen"); // <-- trigger new event
            }
        });

        dinoBtn.getImageCell().size(100, 100);
        boneBtn.getImageCell().size(100, 100);
        cavemenBtn.getImageCell().size(100, 100);

        table.add(boneBtn).pad(8f);
        table.row();
        table.add(dinoBtn).pad(8f);
        table.row();
        table.add(cavemenBtn).pad(8f);
        stage.addActor(table);

    }

    @Override public void draw(SpriteBatch batch) { /* stage draws */ }
    @Override public void dispose() { table.clear(); super.dispose(); }
}
