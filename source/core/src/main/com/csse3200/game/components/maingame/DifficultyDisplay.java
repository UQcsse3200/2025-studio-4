package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;

public class DifficultyDisplay extends UIComponent {
    private Label difficultyLabel;
    private Table table;

    @Override
    public void create() {
        super.create();

        difficultyLabel = new Label("", skin, "title");

        table = new Table();
        table.setFillParent(true);
        table.top().center(); // position at top center
        table.add(difficultyLabel).padTop(20f);

        stage.addActor(table);

        // listen for difficulty updates
        entity.getEvents().addListener("setDifficulty", this::setDifficulty);
    }

    private void setDifficulty(String difficulty) {
        difficultyLabel.setText("Difficulty: " + difficulty);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Stage draws everything, nothing to do here
    }

    @Override
    public void dispose() {
        super.dispose();
        table.remove();
    }
}
