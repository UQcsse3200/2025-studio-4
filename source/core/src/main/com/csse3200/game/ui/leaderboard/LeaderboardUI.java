package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.components.Component; // 或 UIComponent
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;

public class LeaderboardUI extends Component {
    private Table root;
    private Skin skin;

    @Override
    public void create() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        skin = MinimalSkinFactory.create();
        // if (ServiceLocator.getLeaderboardService() == null) { // Removed
        //     ServiceLocator.registerLeaderboardService(new InMemoryLeaderboardService("player-001")); // Removed
        // } // Removed

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
