package com.csse3200.game.components.book;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * UI component responsible for displaying a book interface in the game.
 * <p>
 * The book can display towers, enemies, or currencies depending on the
 * {@link BookPage} provided. Each entry in the book is represented by a
 * {@link DeckComponent}, and selecting an entry shows detailed stats and
 * lore on the right-hand panel.
 */
public class BookDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplay.class);

    /** The book data (tower/enemy/currency) displayed by this component. */
    private BookComponent book;

    /** List of deck entries (pages) in the current book. */
    private final List<DeckComponent> decks;

    /** Right-hand panel for displaying detailed deck info. */
    private Table rightTable;

    /** Name of the event triggered when a deck is selected. */
    private static final String eventName = "changeData";

    /** Maximum number of words to display in lore before truncating. */
    private int maxWordsLore;

    private DeckComponent deck;
    private boolean hasJustOpenedBook = true;
    private static final Color borderColor =  new Color(0.38f, 0.26f, 0.04f, 1f);

    private Table contentListTable;
    private Table titleTable;
    private Table exitTable;

    /**
     * Constructs a BookDisplay for a specific page type.
     *
     * @param bookPage the type of book page to display (TOWER_PAGE, ENEMY_PAGE, or CURRENCY_PAGE)
     */
    public BookDisplay(BookPage bookPage) {
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
        } else if (bookPage == BookPage.HERO_PAGE) {
            this.book = new BookComponent.HeroBookComponent();
            maxWordsLore = 15;
        }
        this.decks = book == null ? null : book.getDecks();
    }

    /**
     * Creates the UI for the book, including the content list, right panel, and
     * exit button. Registers listeners for deck selection events.
     */
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
        //applyUiScale();
    }

    /**
     * Apply UI scale from user settings
     */
    private void applyUiScale() {
        UserSettings.Settings settings = UserSettings.get();

        // Scale right panel (top-right aligned)
        if (rightTable != null) {
            rightTable.setTransform(true);
            rightTable.validate();
            rightTable.setOrigin(rightTable.getWidth(), rightTable.getHeight());
            rightTable.setScale(settings.uiScale);
        }

        // Scale content list (left-aligned)
        if (contentListTable != null) {
            contentListTable.setTransform(true);
            contentListTable.validate();
            contentListTable.setOrigin(0f, contentListTable.getHeight());
            contentListTable.setScale(settings.uiScale);
        }

        // Scale title table (top-left)
        if (titleTable != null) {
            titleTable.setTransform(true);
            titleTable.validate();
            titleTable.setOrigin(0f, titleTable.getHeight());
            titleTable.setScale(settings.uiScale);
        }

        // Scale exit button (top-right)
        if (exitTable != null) {
            exitTable.setTransform(true);
            exitTable.validate();
            exitTable.setOrigin(exitTable.getWidth(), exitTable.getHeight());
            exitTable.setScale(settings.uiScale);
        }
    }


    /**
     * Adds all UI elements to the stage: background, content list, default
     * selection, and exit button.
     */
    void addActors() {
        this.renderBackGround();
        this.renderContentList();
        // Default selection
        if (!decks.isEmpty()) {
            this.renderRightDeck(decks.getFirst());
        }
        this.renderExitButton();
    }

    /**
     * Renders the background image for the book UI.
     */
    private void renderBackGround() {
        Image backgroundImage =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/book/open_book_theme.png", Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
    }

    /**
     * Renders the left-hand content list of the book with all available decks.
     * Adds clickable buttons for each deck, triggering the right panel update.
     */
    private void renderContentList() {
        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        Table table = new Table();
        table.setFillParent(true);

        // Left content list
        table.top().left().padLeft(stageWidth * 0.18f).padTop(stageHeight * 0.2f);

        // Book title
        Label labelTitle = new Label(this.book.getTitle(), skin, "large");
        labelTitle.setFontScale(stageWidth * 0.001f);
        Table titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.top().left()
                .padLeft(stageWidth * 0.25f)
                .padTop(stageHeight * 0.12f);
        titleTable.add(labelTitle);

        int imagesPerRow = 4;
        if (decks.size() <= 8) {
            imagesPerRow = 2;
        } else if (decks.size() <= 15) {
            imagesPerRow = 3;
        }
        
        // Scale buttons relative to screen width
        float buttonSize = stageWidth * 0.20f / imagesPerRow;

        int count = 0;

        for (DeckComponent currentDeck : decks) {
            Map<DeckComponent.StatType, String> stats = currentDeck.getStats();

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
                        entity.getEvents().trigger(eventName, currentDeck);
                    }
                });
            }

            // Wrap button with a bordered table
            Table borderedButton = new Table();
            borderedButton.setBackground(new TextureRegionDrawable(
                    new TextureRegion(createBorderTexture(borderColor, 2))
            ));
            borderedButton.add(button).size(buttonSize, buttonSize).pad(2);

            table.add(borderedButton).padRight(stageWidth * 0.1f / imagesPerRow);
        }

        stage.addActor(titleTable);
        stage.addActor(table);
    }

    private Texture createBorderTexture(Color color, int thickness) {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Make everything transparent first
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        // Set border color
        pixmap.setColor(color);

        // Draw multiple nested rectangles to create the desired thickness
        for (int i = 0; i < thickness; i++) {
            pixmap.drawRectangle(i, i, size - 2 * i, size - 2 * i);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Create a NinePatchDrawable that is a border (transparent center, colored frame)
    private NinePatchDrawable createBorderNinePatchDrawable(int size, int thickness, Color color) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        // Fill fully transparent first
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        // Draw border by drawing rectangles of 1px thickness inwards
        pixmap.setColor(color);
        for (int i = 0; i < thickness; i++) {
            pixmap.drawRectangle(i, i, size - 2 * i, size - 2 * i);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        // Create NinePatch so the center area stretches but border remains intact
        NinePatch patch = new NinePatch(new TextureRegion(texture),
                thickness, thickness, thickness, thickness);
        NinePatchDrawable drawable = new NinePatchDrawable(patch);

        // NOTE: texture will be owned by the patch; dispose later when screen is disposed
        return drawable;
    }


    /**
     * Renders the details of a selected deck on the right-hand panel.
     *
     * @param deck the deck to display in detail
     */
    private void renderRightDeck(DeckComponent deck) {
        if (this.deck == deck) {
            return;
        }

        this.deck = deck;

        if (!hasJustOpenedBook) {
            String pageFlipSoundPath = "sounds/page_flip.mp3";
            playSound(pageFlipSoundPath);
        } else {
            hasJustOpenedBook = false;
        }

        float stageWidth = stage.getViewport().getWorldWidth();
        float stageHeight = stage.getViewport().getWorldHeight();

        Map<DeckComponent.StatType, String> stats = deck.getStats();

        // Clear the whole right panel before re-rendering
        rightTable.clear();

        // --- IMAGE --- (inside renderRightDeck)
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(stats.get(DeckComponent.StatType.TEXTURE_PATH), Texture.class);
        Image rightImage = new Image(new TextureRegionDrawable(new TextureRegion(tex)));

        float imageW = stageWidth * 0.15f;
        float imageH = stageHeight * 0.25f;

        // Create border drawable (size 64, thickness 2 px)
        int borderSize = 64;
        int borderThicknessPx = 6;
        NinePatchDrawable borderDrawable = createBorderNinePatchDrawable(borderSize, borderThicknessPx, borderColor);

        // Wrap the image in a Table so the background is the border drawable
        Table borderedImage = new Table();
        borderedImage.setBackground(borderDrawable);

        // Add the image inside the bordered table with a small padding so it doesn't overlap the border
        float pad = borderThicknessPx; // Scale this using stageWidth/stageHeight if needed
        borderedImage.add(rightImage).size(imageW - pad * 2, imageH - pad * 2).center();
        rightTable.add(borderedImage).size(imageW, imageH).center();
        rightTable.row().padTop(stageHeight * 0.01f);


        // --- NAME ---
        String name = stats.get(DeckComponent.StatType.NAME);
        if (name == null || name.isEmpty()) {
            name = "Unknown";
        }
        Label nameLabel = new Label(name, skin, "large");
        nameLabel.setFontScale(stageWidth * 0.0008f);
        rightTable.add(nameLabel).center();
        rightTable.row().padTop(stageHeight * 0.01f);

        // --- LORE ---
        String lore = stats.get(DeckComponent.StatType.LORE);
        if (lore != null && !lore.isEmpty()) {
            String trimmedLore = trimWords(lore, maxWordsLore);
            Label loreLabel = new Label(trimmedLore, skin, "small"); // use a smaller style
            loreLabel.setFontScale(stageWidth * 0.0008f);
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
     * Trims a string to a maximum number of words, adding "..." if trimmed.
     *
     * @param text     the text to trim
     * @param maxWords the maximum number of words allowed
     * @return the trimmed string with ellipsis if necessary
     */
    private String trimWords(String text, int maxWords) {
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, maxWords)) + "...";
    }

    /**
     * Renders the exit/back button for the book UI.
     * Triggers the "backToMain" event when clicked.
     */
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

    /**
     * Creates a custom button style for book content buttons.
     *
     * @param backGround the texture path to use for the button
     * @param canClick   whether the button is clickable (true) or locked (false)
     * @return a configured {@link TextButton.TextButtonStyle}
     */
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
        if (canClick) {
            style.down = new NinePatchDrawable(pressedPatch);
            style.over = new NinePatchDrawable(hoverPatch);

            style.fontColor = Color.WHITE;
            style.downFontColor = Color.LIGHT_GRAY;
            style.overFontColor = Color.WHITE;
        }

        return style;
    }

    /**
     * Plays the sound effect.
     *
     * @param soundPath the sound should be played
     */
    private void playSound(String soundPath) {
        Sound sound = ServiceLocator.getResourceService().getAsset(soundPath, Sound.class);
        if (sound != null) {
            sound.play(1.0f);
        } else {
            logger.info("Sound not found: " + soundPath);
        }
    }

    /**
     * Empty draw method. Stage handles rendering.
     *
     * @param batch the sprite batch used for drawing
     */
    @Override
    protected void draw(SpriteBatch batch) {
        // Empty
    }
}
