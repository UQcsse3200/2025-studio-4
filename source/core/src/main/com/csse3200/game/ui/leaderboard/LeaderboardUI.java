package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.components.Component; // 或 UIComponent
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderboardUI extends Component {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardUI.class);
    private Table root;
    private Skin skin;

    @Override
    public void create() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        skin = MinimalSkinFactory.create();
        // 排行榜服务应该在GdxGame中全局注册，这里不需要重复注册
        if (ServiceLocator.getLeaderboardService() == null) {
            logger.warn("Leaderboard service not found, this should not happen");
            ServiceLocator.registerLeaderboardService(new com.csse3200.game.services.leaderboard.SessionLeaderboardService("player-001"));
        }

        root = new Table();
        root.setFillParent(true);
        root.bottom().right().pad(10);
        TextButton open = new TextButton("Leaderboard", skin);
        root.add(open).width(160).height(44);
        stage.addActor(root);

        open.addListener(e -> {
            LeaderboardService lb = ServiceLocator.getLeaderboardService();
            var controller = new LeaderboardController(lb);
            new LeaderboardPopup(skin, controller).showOn(stage);
            return true;
        });
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        if (skin != null) skin.dispose();
    }
}
