package com.csse3200.game.components.book;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.print.Book;
import java.util.List;
import java.util.Map;

public class BookDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplay.class);
    private BookComponent book;
    private List<DeckComponent> decks;
    private BookPage bookPage;
    private Table table;
    private Image rightImage;
    private Label rightLabel;
    private Table rightContent;
    private final float buttonWidth = 130f;
    private final float buttonHeight = 130f;
    private final float displayWidth = 300f;
    private final float displayHeight = 300f;
    private final float exitButtonWidth = 100f;
    private final float exitButtonHeight = 100f;
    private final String eventName = "changeData";

    Table parent;

    public BookDisplay(GdxGame game, BookPage bookPage) {
        super();
        this.bookPage = bookPage;
        if (bookPage == BookPage.CURRENCY_PAGE) {
            this.book = new BookComponent.CurrencyBookComponent();
        } else if (bookPage == BookPage.ENEMY_PAGE) {
            this.book = new BookComponent.EnemyBookComponent();
        } else if (bookPage == BookPage.TOWER_PAGE) {
            this.book = new BookComponent.TowerBookComponent();
        }
        this.decks = book.getDecks();
    }

    @Override
    public void create() {
        super.create();
        addActors();
        this.entity.getEvents().addListener(eventName, this::changeRightDeck);
    }

    void addActors() {
        this.parent = new Table();
        this.rightContent = new Table();
        this.rightImage = new Image();
        this.renderBackGround();
        this.renderContentList();
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
        table = new Table();
        // table.setFillParent(true);

        // table.top().left().padLeft(400).padTop(200);


        Label labelTitle = new Label("THE COMPLETE BOOK OF " + this.book.getTitle(), skin, "large");
        table.row();
        table.add(labelTitle).colspan(2).center();

        for (DeckComponent deck : decks) {
            Map<DeckComponent.StatType, String> stats = deck.getStats();

            // start a new row
            table.row().padTop(0.3f);

            TextButton.TextButtonStyle buttonStyle = createCustomButtonStyle(stats.get(DeckComponent.StatType.TEXTURE_PATH));
            TextButton button = new TextButton("", buttonStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Button inside bookPage clicked");
                    entity.getEvents().trigger(eventName, deck);
                }
            });

            Label label = new Label(stats.get(DeckComponent.StatType.NAME), skin, "default");
            table.add(button).size(buttonWidth, buttonHeight).padRight(1f);
            table.add(label);
        }

        Table rightTable = new Table();
        // rightTable.setFillParent(true);
        // rightTable.top().right().padRight(380).padTop(120);
        this.changeRightDeck(decks.get(0));
        rightTable.add(this.rightImage).size(this.displayWidth, this.displayHeight);
        rightTable.row().width(500f);
        rightTable.add(this.rightContent);



        parent.pack();
        parent.setPosition(stage.getViewport().getWorldWidth() / 2f,
                stage.getViewport().getWorldHeight() / 2f + 50f,
                Align.center);
        parent.add(table).padRight(160f).center();
        parent.add(rightTable).center();


        stage.addActor(parent);
    }

    private void changeRightDeck(DeckComponent deck) {
        Map<DeckComponent.StatType, String> stats = deck.getStats();

        // Update right image
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(stats.get(DeckComponent.StatType.TEXTURE_PATH), Texture.class);
        this.rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));

        Table rightTable = new Table();

        // Clear and rebuild the existing content table
        this.rightContent.clear();

        for (Map.Entry<DeckComponent.StatType, String> entry : stats.entrySet()) {
            DeckComponent.StatType type = entry.getKey();
            String value = entry.getValue();

            if (type == DeckComponent.StatType.TEXTURE_PATH || type == DeckComponent.StatType.NAME) {
                continue;
            }


            if (!type.getTexturePath().isEmpty()) {
                Table rowTable = new Table();

                Texture statTexture = ServiceLocator.getResourceService()
                        .getAsset(type.getTexturePath(), Texture.class);

                if (statTexture != null) {
                    Image statIcon = new Image(statTexture);
                    rowTable.add(statIcon).size(64f).padRight(10f);

                    Label valueLabel = new Label(value, skin, "default");
                    valueLabel.setWrap(true);
                    rowTable.add(valueLabel).width(400f).left();
                }

                this.rightContent.add(rowTable).left().padTop(5f).row();
            } else if (this.book.getTitle().equals("CURRENCY")) {
                Table rowTable = new Table();
                Label statLabel = new Label(type.getDisplayName() + ": " + value, skin, "default");
                statLabel.setWrap(true);
                rowTable.add(statLabel)
                        .width(550f)    // max width for text area
                        .left()
                        .top();
                this.rightContent.add(rowTable).left().padTop(30f).row();
            }

        }
    }

    private void renderExitButton() {

        // Exit Icon on Top Right Corner
        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle("images/book/hologram.png");
        TextButton exitButton = new TextButton("", exitButtonStyle);

        exitButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Back button clicked");
                        entity.getEvents().trigger("backToMain");
                    }
                });

        // Create a table
        Table exitTable = new Table();
        exitTable.top().right();       // Aligns everything in this table to top-right of the stage
        exitTable.setFillParent(true); // Table covers the whole stage

        // Add the button
        exitTable.add(exitButton).size(exitButtonWidth,exitButtonHeight).pad(20f); // 10px padding from edges

        // Add table to stage
        stage.addActor(exitTable);
    }

    private TextButton.TextButtonStyle createCustomButtonStyle(String backGround) {
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
        style.down = new NinePatchDrawable(pressedPatch);
        style.over = new NinePatchDrawable(hoverPatch);

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    @Override
    protected void draw(SpriteBatch batch) {

    }

    public Label getRightLabel() {
        return rightLabel;
    }

    public Image getRightImage() {
        return rightImage;
    }

}
