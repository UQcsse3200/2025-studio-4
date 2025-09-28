package com.csse3200.game.components.book;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class BookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplayActions.class);
    private GdxGame game;
    private BookComponent bookComponent = new BookComponent();

    public BookDisplayActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("changeCurrencyData", this::changeCurrencyData);
        entity.getEvents().addListener("changeEnemyData", this::changeEnemyData);
        entity.getEvents().addListener("changeTowerData", this::changeTowerData);
    }

    private void changeCurrencyData(int index) {
        Image rightImage = entity.getComponent(BookDisplay.class).getRightImage();
        Label rightLabel = entity.getComponent(BookDisplay.class).getRightLabel();

        // Update right image
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(this.bookComponent.getCurrencyBackGround()[index], Texture.class);
        rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));

        // Update right label text
        rightLabel.setText(this.bookComponent.getCurrencyData()[index]);
    }

    private void changeEnemyData(int index) {
        Image rightImage = entity.getComponent(BookDisplay.class).getRightImage();
        Label rightLabel = entity.getComponent(BookDisplay.class).getRightLabel();

        // Update right image
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(this.bookComponent.getEnemyBackGround()[index], Texture.class);
        rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));

        // Update right label text
        rightLabel.setText(this.bookComponent.getEnemyData()[index]);
    }

    private void changeTowerData(int index) {
        Image rightImage = entity.getComponent(BookDisplay.class).getRightImage();
        Label rightLabel = entity.getComponent(BookDisplay.class).getRightLabel();

        // Update right image
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(this.bookComponent.getTowerBackGround()[index], Texture.class);
        rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));

        // Update right label text
        rightLabel.setText(this.bookComponent.getTowerData()[index]);
    }
}
