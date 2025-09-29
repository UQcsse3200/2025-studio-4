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
    private final float buttonWidth = 170f;
    private final float buttonHeight = 170f;
    private final float displayWidth = 300f;
    private final float displayHeight = 300f;
    private final float exitIconWidth = 100f;
    private final float exitIconHeight = 100f;

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
    }

    void addActors() {
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
        table.setFillParent(true);

        String tmpData = "";
        final String eventName = "changeData";

        table.top().left().padLeft(450).padTop(120);
        for (int i = 0; i < decks.size(); i++) {
            DeckComponent deck = decks.get(i);
            Map<DeckComponent.StatType, String> stats = deck.getStats();

            // start a new row
            table.row().padTop(0.5f).padLeft(10f);
            final int index = i;

            TextButton.TextButtonStyle buttonStyle = createCustomButtonStyle(stats.get(DeckComponent.StatType.TEXTURE_PATH));
            TextButton button = new TextButton("", buttonStyle);
            button.getLabel().setColor(Color.WHITE);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Button inside bookPage clicked");
                    entity.getEvents().trigger(eventName, index);
                }
            });

            Label label = new Label(stats.get(DeckComponent.StatType.NAME), skin, "large");
            table.add(button).size(buttonWidth, buttonHeight).padRight(1f);
            table.add(label);
        }

        Table rightTable = new Table();
        rightTable.setFillParent(true);
        rightTable.top().right().padRight(380).padTop(120); // anchor top-right with padding
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(decks.get(0).getStats().get(DeckComponent.StatType.TEXTURE_PATH), Texture.class);
        this.rightImage = new Image(tex);
        rightTable.add(rightImage).size(this.displayWidth, this.displayHeight);

        this.rightLabel = new Label(tmpData, skin);
        this.rightLabel.setWrap(true);
        rightTable.row().width(500f);
        rightTable.add(this.rightLabel);

        stage.addActor(table);
        stage.addActor(rightTable);
    }

    private void renderExitButton() {

        // Exit Icon on Top Right Corner
        TextButton.TextButtonStyle exitButtonStyle = createCustomButtonStyle("images/book/stamp.png");
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
        exitTable.add(exitButton).size(exitIconWidth,exitIconHeight).pad(10f); // 10px padding from edges

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
