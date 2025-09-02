package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.components.Component; // 或 UIComponent
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.InMemoryLeaderboardService;

public class LeaderboardUI extends Component {
    private Table root;
    private Skin skin;

    @Override
    public void create() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        skin = MinimalSkinFactory.create();

        // 如果没注册过，就注册一个默认实现（只需一次）
        if (ServiceLocator.getLeaderboardService() == null) {
            ServiceLocator.registerLeaderboardService(new InMemoryLeaderboardService("player-001"));
        }

        root = new Table();
        root.setFillParent(true);
        root.top().right().pad(10);
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
