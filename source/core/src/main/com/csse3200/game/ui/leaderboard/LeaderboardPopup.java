package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardEntry;
import com.csse3200.game.services.ServiceLocator;
import java.time.*;

public class LeaderboardPopup extends Window {
    private final Skin skin;
    private final LeaderboardController controller;
    private final Table listTable = new Table();
    private final Table achievementTable = new Table();
    private final ScrollPane scroller;
    private final TextButton prevBtn, nextBtn, closeBtn, friendsBtn;

    public LeaderboardPopup(Skin skin, LeaderboardController controller) {
        super("Leaderboard", skin);
        this.skin = skin;
        this.controller = controller;

        setModal(true);
        setMovable(false);
        pad(16);
        getTitleLabel().setAlignment(Align.center);

        friendsBtn = new TextButton("All", skin);
        closeBtn = new TextButton("Close", skin);
        prevBtn = new TextButton("< Prev", skin);
        nextBtn = new TextButton("Next >", skin);

        Table header = new Table();
        header.add(new Label("Leaderboard", skin, "title")).expandX().left();
        header.add(friendsBtn).right();

        Table headerRow = new Table(skin);
        headerRow.add(new Label("#", skin)).width(40).left();
        headerRow.add(new Label("Player", skin)).expandX().left().padLeft(8);
        headerRow.add(new Label("Score", skin)).width(120).right();
        headerRow.add(new Label("Time", skin)).width(160).right();

        scroller = new ScrollPane(listTable, skin);
        scroller.setFadeScrollBars(false);
        scroller.setScrollingDisabled(true, false);

        Table footer = new Table();
        footer.add(prevBtn).left().padRight(8);
        footer.add(nextBtn).left().padRight(8);
        footer.add().expandX();
        footer.add(closeBtn).right();

        // Create achievement section
        createAchievementSection();
        
        Table content = new Table();
        content.defaults().pad(6);
        content.add(header).growX();
        content.row();
        content.add(headerRow).growX().padTop(6);
        content.row();
        content.add(scroller).grow().minHeight(360);
        content.row();
        // Add achievement section
        content.add(achievementTable).growX().padTop(10);
        content.row();
        content.add(footer).growX();

        add(content).grow().minSize(720, 640);

        // 事件
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { dismiss(); }
        });
        prevBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { controller.prevPage(); refreshList(); }
        });
        nextBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { controller.nextPage(); refreshList(); }
        });
        friendsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                controller.toggleFriends();
                friendsBtn.setText(controller.isFriendsOnly() ? "Friends" : "All");
                refreshList();
            }
        });

        refreshList();

        // 入场动画
        getColor().a = 0f;
        addAction(Actions.sequence(
                Actions.alpha(1f, 0.15f),
                Actions.scaleTo(1.03f, 1.03f, 0.06f),
                Actions.scaleTo(1f, 1f, 0.06f)
        ));
        
        // 确保弹窗可见
        setVisible(true);
    }

    private void refreshList() {
        listTable.clearChildren();
        var items = controller.loadPage();
        var me = controller.getMyBest();

        if (items.isEmpty()) {
            // Show a message when no entries exist
            Label noEntriesLabel = new Label("No rankings yet. Play a game to get on the leaderboard!", skin);
            noEntriesLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            listTable.add(noEntriesLabel).growX().pad(20);
        } else {
            for (LeaderboardEntry e : items) listTable.add(buildRow(e, me)).growX().row();
        }

        prevBtn.setDisabled(controller.isFirstPage());
        nextBtn.setDisabled(items.size() < 20); // 简化版
        scroller.layout();
        scroller.updateVisualScroll();
        scroller.setScrollY(0);
    }

    private Table buildRow(LeaderboardEntry e, LeaderboardEntry me) {
        boolean isMe = (me != null && me.playerId.equals(e.playerId));
        Table row = new Table(skin);
        row.pad(4);

        Label rank = new Label(String.valueOf(e.rank), skin);
        Label name = new Label(e.displayName + (isMe ? " (You)" : ""), skin);
        Label score = new Label(String.valueOf(e.score), skin);
        Label time = new Label(formatTime(e.achievedAtMs), skin);

        if (e.rank == 1) rank.setText("🥇 " + e.rank);
        else if (e.rank == 2) rank.setText("🥈 " + e.rank);
        else if (e.rank == 3) rank.setText("🥉 " + e.rank);

        if (isMe) row.setBackground("selection"); // 需要在 skin 里有 selection

        // 添加头像显示
        Image avatarImage = createAvatarImage(e.avatarId);
        
        row.add(rank).width(40).left();
        row.add(avatarImage).size(32, 32).padLeft(8);
        row.add(name).expandX().left().padLeft(8);
        row.add(score).width(120).right();
        row.add(time).width(160).right();

        return row;
    }
    
    private Image createAvatarImage(String avatarId) {
        try {
            // 获取头像服务
            if (ServiceLocator.getPlayerAvatarService() != null) {
                String imagePath = ServiceLocator.getPlayerAvatarService().getAvatarImagePath(avatarId);
                Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
                return new Image(texture);
            }
        } catch (Exception e) {
            // 如果加载失败，使用默认头像
        }
        
        // 返回默认头像或占位符
        return new Image(); // 空图片作为占位符
    }

    private String formatTime(long ms) {
        var dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
        return dt.toString().replace('T', ' ');
    }

    public void showOn(Stage stage) {
        // 暂停游戏
        ServiceLocator.getTimeSource().setTimeScale(0f);
        
        stage.addActor(this);
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f),
                Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void dismiss() {
        // 恢复游戏
        ServiceLocator.getTimeSource().setTimeScale(1f);
        
        addAction(Actions.sequence(
                Actions.parallel(Actions.alpha(0f, 0.12f), Actions.scaleTo(0.98f, 0.98f, 0.12f)),
                Actions.removeActor()
        ));
    }
    
    /**
     * Creates the achievement display section
     */
    private void createAchievementSection() {
        achievementTable.clear();
        
        // Title
        Label achievementTitle = new Label("Achievements", skin, "title");
        achievementTable.add(achievementTitle).colspan(5).padBottom(10);
        achievementTable.row();
        
        // Achievement IDs and paths
        String[] achievementIds = {
            com.csse3200.game.services.AchievementService.TOUGH_SURVIVOR,
            com.csse3200.game.services.AchievementService.SPEED_RUNNER,
            com.csse3200.game.services.AchievementService.SLAYER,
            com.csse3200.game.services.AchievementService.PERFECT_CLEAR,
            com.csse3200.game.services.AchievementService.PARTICIPATION
        };
        
        String[] achievementImages = {
            "images/tough survivor.jpg",
            "images/speed runner.jpg",
            "images/slayer.jpg",
            "images/perfect clear.jpg",
            "images/participation.jpg"
        };
        
        // Get achievement service
        com.csse3200.game.services.AchievementService achievementService = 
            ServiceLocator.getAchievementService();
        
        // Display achievements
        for (int i = 0; i < achievementIds.length; i++) {
            final int index = i;
            Image achievementIcon = createAchievementIcon(
                achievementImages[index], 
                achievementService != null && achievementService.isUnlocked(achievementIds[index])
            );
            
            achievementTable.add(achievementIcon).size(80, 80).pad(5);
        }
    }
    
    /**
     * Creates an achievement icon, either colored (unlocked) or grayscale (locked)
     */
    private Image createAchievementIcon(String imagePath, boolean unlocked) {
        try {
            Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
            Image image = new Image(texture);
            
            if (!unlocked) {
                // Make the image grayscale
                image.setColor(0.5f, 0.5f, 0.5f, 0.6f);
            } else {
                // Unlocked achievements have golden tint
                image.setColor(1.2f, 1.1f, 0.8f, 1f);
            }
            
            return image;
        } catch (Exception e) {
            // Return empty image if texture not found
            return new Image();
        }
    }
}
