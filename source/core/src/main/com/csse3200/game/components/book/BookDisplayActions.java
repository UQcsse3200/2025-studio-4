package com.csse3200.game.components.book;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookDisplayActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BookDisplayActions.class);
    private GdxGame game;

    public BookDisplayActions(GdxGame game) {
        this.game = game;
    }
}
