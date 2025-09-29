package com.csse3200.game.components.book;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.Map;

public class BookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplayActions.class);
    private GdxGame game;

    public BookDisplayActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("changeData", this::changeData);
        entity.getEvents().addListener("backToMain", this::onExit);
    }

    private void changeData(DeckComponent deck) {
        Map<DeckComponent.StatType, String> stats = deck.getStats();

        Image rightImage = entity.getComponent(BookDisplay.class).getRightImage();
        Label rightLabel = entity.getComponent(BookDisplay.class).getRightLabel();

        // Update right image
        Texture tex = ServiceLocator.getResourceService()
                .getAsset(stats.get(DeckComponent.StatType.TEXTURE_PATH), Texture.class);
        rightImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));

        // Update right label text
        rightLabel.setText(stats.get(DeckComponent.StatType.NAME));
    }

    private void onExit() {
        game.setScreen(GdxGame.ScreenType.BOOK);
    }
}
