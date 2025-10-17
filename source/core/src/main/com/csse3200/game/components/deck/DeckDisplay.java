package com.csse3200.game.components.deck;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.EnumMap;
import java.util.Map;

/**
 * A UI component for displaying detailed deck info (tower/enemy stats)
 * when an entity with a {@link DeckComponent} is clicked.
 * Usage:
 *      // Example inside TowerComponent or EnemyComponent click handler
 *      playerEntity.getEvents().trigger("displayDeck", towerEntity.getComponent(DeckComponent.class));
 */
public class DeckDisplay extends UIComponent {
    private Table table;
    private DeckComponent deck;
    private Drawable backgroundDrawable;

    /**
     * Initializes this UI component, creating the UI table and registering listeners
     * for deck display and clear events.
     */
    @Override
    public void create() {
        super.create();
        addActors();
        entity.getEvents().addListener("displayDeck", this::displayDeck);
        entity.getEvents().addListener("clearDeck", this::hide);
    }

    /**
     * Updates the deck UI each frame. If the underlying deck stats change,
     * the display is refreshed.
     */
    @Override
    public void update() {
        if (deck != null) {
            Map<DeckComponent.StatType, String> lastStats = new EnumMap<>(deck.getStats());
            deck.updateStats();
            if (!deck.getStats().equals(lastStats)) {
                displayDeck(deck);
            }
        }
    }

    /**
     * Adds the table actor to the stage and positions it.
     */
    private void addActors() {
        // create texture for the background that matches other UI
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.15f, 0.15f, 0.18f, 0.6f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        backgroundDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Table layout = new Table();
        layout.setFillParent(true);
        layout.top().right().padTop(200f).padRight(20f); // align with upgrade UI

        table = new Table();
        table.pad(10f);

        layout.add(table).pad(10f);

        stage.addActor(layout);
    }

    /**
     * Renders a deck’s information in the UI. This includes:
     * <ul>
     *   <li>The deck image (if {@link DeckComponent.StatType#TEXTURE_PATH} is present)</li>
     *   <li>The name of the entity (as a large title)</li>
     *   <li>Other stats with associated icons or text values</li>
     * </ul>
     *
     * @param deck the {@link DeckComponent} containing the stats to display
     */
    public void displayDeck(DeckComponent deck) {
        if (deck == null) {
            return;
        }

        this.deck = deck;
        deck.updateStats();

        table.clear();
        table.setBackground(backgroundDrawable); // set background when table is populated

        // 1. If a TEXTURE_PATH stat exists, render the image first
        String texturePath = deck.getStats().get(DeckComponent.StatType.TEXTURE_PATH);
        if (texturePath != null && !texturePath.isEmpty()) {
            Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
            if (texture != null) {
                Image icon = new Image(texture);
                table.add(icon).size(64f).padBottom(10f);  // size and padding can be tweaked
                table.row();
            }
        }

        // 2. Render NAME as a big title row
        String name = deck.getStats().get(DeckComponent.StatType.NAME);
        if (name != null) {
            Label title = new Label(name, skin, "default");
            title.setColor(Color.WHITE);
            table.add(title).padBottom(15f);
            table.row();
        }

        // 3. Render other stats (with icons if available)
        for (Map.Entry<DeckComponent.StatType, String> entry : deck.getStats().entrySet()) {
            DeckComponent.StatType type = entry.getKey();
            String value = entry.getValue();

            // Skip TEXTURE_PATH and NAME (handled separately)
            if (type == DeckComponent.StatType.TEXTURE_PATH || type == DeckComponent.StatType.NAME) {
                continue;
            }

            Table rowTable = new Table();

            // If this stat type has a texture path → render icon + value only
            if (!type.getTexturePath().isEmpty()) {
                Texture statTexture = ServiceLocator.getResourceService()
                        .getAsset(type.getTexturePath(), Texture.class);

                if (statTexture != null) {
                    Image statIcon = new Image(statTexture);
                    rowTable.add(statIcon).size(32f).padRight(10f);

                    Label valueLabel = new Label(value, skin, "default");
                    rowTable.add(valueLabel).left();
                }
            }
            else { // Otherwise → render key + value as text
                Label statLabel = new Label(type.getDisplayName() + ": " + value, skin, "default");
                rowTable.add(statLabel).left();
            }

            table.add(rowTable).left().pad(5f);
            table.row();
        }
    }

    /**
     * Hides the deck UI by clearing the table and removing the current deck reference.
     */
    public void hide() {
        table.clear();
        // remove background on deck clear as the background would still be visible if not
        table.setBackground((Drawable) null);
        this.deck = null;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles drawing
    }

    /**
     * Disposes of this component by clearing and removing the table.
     * Ensures that UI resources are properly cleaned up when the component is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        table.clear();
        table.remove();
    }
}
