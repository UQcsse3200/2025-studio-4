package com.csse3200.game.components.book;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.print.Book;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BookDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplay.class);
    private BookComponent book;
    private List<DeckComponent> decks;
    private Table rightTable;
    private final String eventName = "changeData";
    private int maxWordsLore;

    public BookDisplay(GdxGame game, BookPage bookPage) {
        super();
        if (bookPage == BookPage.CURRENCY_PAGE) {
            this.book = new BookComponent.CurrencyBookComponent();
            maxWordsLore = 30;
        } else if (bookPage == BookPage.ENEMY_PAGE) {
            this.book = new BookComponent.EnemyBookComponent();
            maxWordsLore = 15;
        } else if (bookPage == BookPage.TOWER_PAGE) {
            this.book = new BookComponent.TowerBookComponent();
            maxWordsLore = 25;
        }
        this.decks = book.getDecks();
    }

    @Override
    public void create() {
        super.create();
        rightTable = new Table();
        rightTable.setFillParent(true);
        rightTable.top().right()
                .padLeft(stage.getViewport().getWorldWidth() * 0.2f)
                .padTop(stage.getViewport().getWorldHeight() * 0.08f)
                .padRight(stage.getViewport().getWorldWidth()* 0.17f);
        addActors();
        stage.addActor(rightTable);
        this.entity.getEvents().addListener(eventName, this::renderRightDeck);

    }

    void addActors() {
        this.renderBackGround();
        this.renderContentList();
        // Default selection
        if (!decks.isEmpty()) {
            this.renderRightDeck(decks.getFirst());
        }
        this.renderExitButton();
    }

    private void renderBackGround() {
        Image backgroundImage =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/book/open_book_theme.png", Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
    }

    private void renderContentList() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        Table table = new Table();
        table.setFillParent(true);

        // Left content list
        table.top().left().padLeft(stageWidth * 0.2f).padTop(stageHeight * 0.2f);

        // Book title
        Label labelTitle = new Label(this.book.getTitle(), skin, "large");
        labelTitle.setFontScale(stageWidth * 0.001f);
        Table titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.top().left()
                .padLeft(stageWidth * 0.25f)
                .padTop(stageHeight * 0.12f);
        titleTable.add(labelTitle);

        // Scale buttons relative to screen width
        float buttonW = stageWidth * 0.12f;
        float buttonH = stageHeight * 0.12f;

        int imagesPerRow = 2;
        int count = 0;

        for (DeckComponent deck : decks) {
            Map<DeckComponent.StatType, String> stats = deck.getStats();

            // start a new row
            if (count % imagesPerRow == 0) {
                table.row().padTop(stageHeight * 0.01f).padLeft(stageWidth * 0.01f);
            }
            count++;

            TextButton.TextButtonStyle buttonStyle;
            TextButton button;
            String lockedValue = stats.get(DeckComponent.StatType.LOCKED);
            if (lockedValue != null && lockedValue.equals("true")) {
                buttonStyle = createCustomButtonStyle(DeckComponent.StatType.LOCKED.getTexturePath(), false);
                button = new TextButton("", buttonStyle);
            } else {
                buttonStyle = createCustomButtonStyle(stats.get(DeckComponent.StatType.TEXTURE_PATH), true);
                button = new TextButton("", buttonStyle);
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Button inside bookPage clicked");
                        entity.getEvents().trigger(eventName, deck);
                    }
                });
            }
            table.add(button).size(buttonW, buttonH).padRight(stageWidth * 0.01f);
        }

        stage.addActor(titleTable);
        stage.addActor(table);
    }


    private void renderRightDeck(DeckComponent deck) {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        Map<DeckComponent.StatType, String> stats = deck.getStats();

        // Clear the whole right panel before re-rendering
        rightTable.clear();

        // --- IMAGE ---
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(stats.get(DeckComponent.StatType.TEXTURE_PATH), Texture.class);
        Image rightImage = new Image(new TextureRegionDrawable(new TextureRegion(tex)));

        float imageW = stageWidth * 0.15f;
        float imageH = stageHeight * 0.25f;
        rightTable.add(rightImage).size(imageW, imageH).center();
        rightTable.row().padTop(stageHeight * 0.01f);

        // --- NAME ---
        String name = stats.get(DeckComponent.StatType.NAME);
        if (name != null) {
            Label nameLabel = new Label(name, skin, "large");
            nameLabel.setFontScale(stageWidth * 0.001f);
            rightTable.add(nameLabel).center();
            rightTable.row().padTop(stageHeight * 0.01f);
        }

        // --- LORE ---
        String lore = stats.get(DeckComponent.StatType.LORE);
        if (lore != null && !lore.isEmpty()) {
            String trimmedLore = trimWords(lore, maxWordsLore);
            Label loreLabel = new Label(trimmedLore, skin, "small"); // use a smaller style
            loreLabel.setFontScale(stageWidth * 0.001f);
            loreLabel.setWrap(true);
            rightTable.add(loreLabel)
                    .width(stageWidth * 0.3f)
                    .center();
            rightTable.row().padTop(stageHeight * 0.02f);
        }

        // --- OTHER STATS ---
        int statsPerRow = 2;
        int statCount = 0;

        Table rowTable = new Table(); // temporary row container
        for (Map.Entry<DeckComponent.StatType, String> entry : stats.entrySet()) {
            DeckComponent.StatType type = entry.getKey();
            String value = entry.getValue();

            if (type == DeckComponent.StatType.TEXTURE_PATH
                    || type == DeckComponent.StatType.NAME
                    || type == DeckComponent.StatType.LORE
                    || type == DeckComponent.StatType.SOUND
                    || type == DeckComponent.StatType.LOCKED) {
                continue;
            }

            if (!type.getTexturePath().isEmpty()) {
                Table statCell = new Table();

                Texture statTexture = ServiceLocator.getResourceService()
                        .getAsset(type.getTexturePath(), Texture.class);

                if (statTexture != null) {
                    Image statIcon = new Image(statTexture);
                    statCell.add(statIcon).size(stageWidth * 0.04f).padRight(stageWidth * 0.01f);

                    Label valueLabel = new Label(value, skin, "small");
                    valueLabel.setFontScale(stageWidth * 0.001f);
                    valueLabel.setWrap(true);
                    statCell.add(valueLabel).width(stageWidth * 0.08f).left();
                }

                // Add statCell to temporary row table
                if (statCount % statsPerRow == 1) { // second cell in the row
                    rowTable.add(statCell).padLeft(stageWidth * 0.01f);
                } else {
                    rowTable.add(statCell);
                }

                statCount++;

                // After 2 stats, add the row to rightTable
                if (statCount % statsPerRow == 0) {
                    rightTable.add(rowTable).fillX().padTop(stageHeight * 0.01f).row();
                    rowTable = new Table(); // start new row
                }
            }
        }

        // Add leftover stats (if any) in the last row
        if (statCount % statsPerRow != 0) {
            rightTable.add(rowTable).fillX().padTop(stageHeight * 0.01f).row();
        }
    }

    /**
     * Helper method to trim a string to a max number of words.
     */
    private String trimWords(String text, int maxWords) {
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, maxWords)) + "...";
    }

    private void renderExitButton() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        // Scale button size relative to stage size
        float buttonWidth = stageWidth * 0.15f;
        float buttonHeight = stageHeight * 0.24f;

        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle("images/book/bookmark.png", true);
        TextButton exitButton = new TextButton("", exitButtonStyle);

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Back button clicked");
                entity.getEvents().trigger("backToMain");
            }
        });

        Table exitTable = new Table();
        exitTable.top().right().padRight(stageWidth * 0.125f);
        exitTable.setFillParent(true);

        exitTable.add(exitButton).size(buttonWidth, buttonHeight).pad(stageWidth * 0.01f); // padding scaled too
        stage.addActor(exitTable);
    }


    private TextButton.TextButtonStyle createCustomButtonStyle(String backGround, boolean canClick) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.font = skin.getFont("segoe_ui");

        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset(backGround, Texture.class);
        TextureRegion buttonRegion = new TextureRegion(buttonTexture);

        NinePatch buttonPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);

        NinePatch pressedPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        pressedPatch.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        NinePatch hoverPatch = new NinePatch(buttonRegion, 10, 10, 10, 10);
        hoverPatch.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(buttonPatch);
        if (canClick == true) {
            style.down = new NinePatchDrawable(pressedPatch);
            style.over = new NinePatchDrawable(hoverPatch);

            style.fontColor = Color.WHITE;
            style.downFontColor = Color.LIGHT_GRAY;
            style.overFontColor = Color.WHITE;
        }

        return style;
    }

    @Override
    protected void draw(SpriteBatch batch) {

    }
}
