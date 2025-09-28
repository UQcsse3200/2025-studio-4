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
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(MainBookDisplay.class);
    private String bookPage;
    private Table table;
    private Image rightImage;
    private Label rightLabel;
    private final float buttonWidth = 170f;
    private final float buttonHeight = 170f;
    private final float displayWidth = 300f;
    private final float displayHeight = 300f;
    private BookComponent bookComponent = new BookComponent();

    public BookDisplay(GdxGame game) {
        super();
        this.bookPage = "currencyPage";
    }

    public BookDisplay(GdxGame game, String bookPage) {
        super();
        this.bookPage = bookPage;
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    void addActors() {
        table = new Table();
        table.setFillParent(true);

        Image backgroundImage =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/book/open_book_theme.png", Texture.class));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        String[] buttonList = new String[0];
        String[] buttonTitle = new String[0];
        if (this.bookPage.equals("currencyPage")) {
            buttonList = this.bookComponent.getCurrencyBackGround();
            buttonTitle = this.bookComponent.getCurrencyTitle();
        } else if (this.bookPage.equals("towerPage")) {
            buttonList = this.bookComponent.getCurrencyBackGround();
            buttonTitle = this.bookComponent.getCurrencyTitle();
        }

        table.top().left().padLeft(450).padTop(150);
        for (int i = 0; i < buttonList.length; i++) {
            // start a new row
            table.row().padTop(0.5f).padLeft(10f);
            final int index = i;

            TextButton.TextButtonStyle buttonStyle = createCustomButtonStyle(buttonList[i]);
            TextButton button = new TextButton("", buttonStyle);
            button.getLabel().setColor(Color.WHITE);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent changeEvent, Actor actor) {
                    logger.debug("Button inside bookPage clicked");
                    entity.getEvents().trigger("changeCurrencyData", index);
                }
            });

            Label label = new Label(buttonTitle[i], skin, "large");
            table.add(button).size(buttonWidth, buttonHeight).padRight(1f);
            table.add(label);
        }

        Table rightTable = new Table();
        rightTable.setFillParent(true);
        rightTable.top().right().padRight(380).padTop(150); // anchor top-right with padding
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(buttonList[0], Texture.class);
        this.rightImage = new Image(tex);
        rightTable.add(rightImage).size(this.displayWidth, this.displayHeight);
        this.rightLabel = new Label(this.bookComponent.getCurrencyData()[0], skin);
        this.rightLabel.setWrap(true);
        rightTable.row().width(500f);
        rightTable.add(this.rightLabel);

        stage.addActor(table);
        stage.addActor(rightTable);
    }

//    public void changed(ChangeListener.ChangeEvent changeEvent, Actor actor, int index) {
//        logger.debug("Button {} clicked", index);
//
//        // Update right image
//        Texture tex = ServiceLocator.getResourceService()
//                .getAsset(currencyBackGround[index], Texture.class);
//        rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
//
//        // Update right label text
//        rightLabel.setText(currencyData[index]);
//    }

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
