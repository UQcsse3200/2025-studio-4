package com.csse3200.game.components.deck;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;

import java.util.Map;

/**
 * A UI component for displaying detailed deck info (tower/enemy stats)
 * when an entity with a {@link DeckComponent} is clicked.
 *
 * Usage:
 *      // Example inside TowerComponent or EnemyComponent click handler
 *      playerEntity.getEvents().trigger("displayDeck", towerEntity.getComponent(DeckComponent.class));
 */
public class DeckDisplay extends UIComponent {
    private Table table;
    // Set initial value to test
    private DeckComponent deck = new DeckComponent.TowerDeckComponent("<Tower Name>", 999, 999.0, 999.0);

    @Override
    public void create() {
        super.create();
        addActors();
        entity.getEvents().addListener("displayDeck", this::displayDeck);
    }

    private void addActors() {
        table = new Table();
        table.bottom().right();  // bottom-right corner
        table.setFillParent(true);
        table.pad(10f); // small margin

        displayDeck(deck);

        stage.addActor(table);
    }

    /**
     * Render a deck’s info in the UI.
     * @param deck the deck data from a tower or enemy
     */
    public void displayDeck(DeckComponent deck) {
        if (deck == null) {
            return;
        }

        table.clear();

        // First: display NAME stat as the title row (if present)
        String name = deck.getStats().get(DeckComponent.StatType.NAME);
        if (name != null) {
            Label title = new Label(name, skin, "large");
            table.add(title).padBottom(15f);  // extra spacing below
            table.row();
        }

        // Then display all other stats except NAME
        for (Map.Entry<DeckComponent.StatType, String> entry : deck.getStats().entrySet()) {
            if (entry.getKey() == DeckComponent.StatType.NAME) {
                continue; // skip NAME since it's already displayed
            }

            Label statLabel = new Label(
                    entry.getKey().getDisplayName() + ": " + entry.getValue(),
                    skin,
                    "default"
            );
            table.add(statLabel).left().pad(5f);
            table.row();
        }
    }


    /**
     * Hide the deck UI
     */
    public void hide() {
        table.clear();
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles drawing
    }

    @Override
    public void dispose() {
        super.dispose();
        table.clear();
        table.remove();
    }
}
