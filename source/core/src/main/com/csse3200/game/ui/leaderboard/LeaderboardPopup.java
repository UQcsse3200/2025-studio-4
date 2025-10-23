package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.leaderboard.LeaderboardService.LeaderboardEntry;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIStyleHelper;
import java.time.*;

public class LeaderboardPopup extends Window {
    private final Skin skin;
    private final LeaderboardController controller;
    private final Table listTable = new Table();
    private final Table achievementTable = new Table();
    private final ScrollPane scroller;
    private final TextButton prevBtn, nextBtn, closeBtn, friendsBtn;

    public LeaderboardPopup(Skin skin, LeaderboardController controller) {
        super("", skin);  // 空标题，我们自定义标题栏
        this.skin = skin;
        this.controller = controller;

        setModal(true);
        setMovable(false);
        pad(0);  // 移除默认 padding，我们自己控制
        getTitleLabel().setAlignment(Align.center);
        
        // Set background image - 使用新的排行榜背景
        try {
            Texture bgTexture = ServiceLocator.getResourceService().getAsset(
                "images/leaderboard background.png", Texture.class);
            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(bgTexture));
            // 设置最小尺寸为0，允许背景图片缩放以适应对话框
            drawable.setMinWidth(0);
            drawable.setMinHeight(0);
            setBackground(drawable);
        } catch (Exception e) {
            // If background fails to load, continue without it
        }

        // 使用与主界面一致的橙色按钮样式
        TextButton.TextButtonStyle orangeStyle = UIStyleHelper.orangeButtonStyle();
        friendsBtn = new TextButton("All", orangeStyle);
        closeBtn = new TextButton("Close", orangeStyle);
        prevBtn = new TextButton("< Prev", orangeStyle);
        nextBtn = new TextButton("Next >", orangeStyle);

        // 标题栏
        Table header = new Table();
        header.pad(20, 20, 15, 20);
        
        // 使用 segoe_ui 字体创建标题，居中显示，放大并使用醒目的金色
        Label.LabelStyle titleStyle = new Label.LabelStyle(
            skin.getFont("segoe_ui"), 
            new com.badlogic.gdx.graphics.Color(1.0f, 0.84f, 0.0f, 1.0f) // 金色 #FFD700
        );
        Label titleLabel = new Label("Leaderboard", titleStyle);
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(2.5f); // 放大2.5倍，更醒目
        
        header.add(titleLabel).expandX().center().padBottom(10);
        header.add(friendsBtn).width(100).height(36).right();

        // 表头行 - 使用 segoe_ui 字体
        Table headerRow = new Table();
        headerRow.pad(8, 20, 8, 20);
        Label.LabelStyle headerStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), com.badlogic.gdx.graphics.Color.DARK_GRAY);
        headerRow.add(new Label("#", headerStyle)).width(50).left();
        headerRow.add(new Label("Player", headerStyle)).expandX().left().padLeft(8);
        headerRow.add(new Label("Score", headerStyle)).width(100).right();
        headerRow.add(new Label("Time", headerStyle)).width(160).right();

        scroller = new ScrollPane(listTable, skin);
        scroller.setFadeScrollBars(false);
        scroller.setScrollingDisabled(true, false);

        // 底部按钮栏
        Table footer = new Table();
        footer.pad(12, 20, 16, 20);
        footer.add(prevBtn).width(100).height(36).left().padRight(8);
        footer.add(nextBtn).width(100).height(36).left().padRight(8);
        footer.add().expandX();
        footer.add(closeBtn).width(100).height(36).right();

        // Create achievement section
        createAchievementSection();
        
        Table content = new Table();
        content.add(header).growX();
        content.row();
        content.add(headerRow).growX();
        content.row();
        content.add(scroller).grow().minHeight(400).pad(0, 20, 0, 20);
        content.row();
        // Add achievement section
        content.add(achievementTable).growX().padTop(10);
        content.row();
        content.add(footer).growX();

        add(content).grow().minSize(800, 640);

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
            // Show a message when no entries exist - 使用 segoe_ui 字体
            Label.LabelStyle emptyStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), com.badlogic.gdx.graphics.Color.GRAY);
            Label noEntriesLabel = new Label("No rankings yet. Play a game to get on the leaderboard!", emptyStyle);
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
        row.pad(10, 0, 10, 0);  // 增加行高

        // 使用 segoe_ui 字体创建所有标签
        Label.LabelStyle rowStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), com.badlogic.gdx.graphics.Color.BLACK);
        Label rank = new Label(String.valueOf(e.rank), rowStyle);
        Label name = new Label(e.displayName + (isMe ? " (You)" : ""), rowStyle);
        Label score = new Label(String.valueOf(e.score), rowStyle);
        Label time = new Label(formatTime(e.achievedAtMs), rowStyle);

        // 使用奖牌图标标记前三名
        if (e.rank == 1) rank.setText("🥇 " + e.rank);
        else if (e.rank == 2) rank.setText("🥈 " + e.rank);
        else if (e.rank == 3) rank.setText("🥉 " + e.rank);

        // 高亮自己的排名
        if (isMe) {
            row.setBackground("selection");
            // 使用 segoe_ui 字体，白色显示
            Label.LabelStyle highlightStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), com.badlogic.gdx.graphics.Color.WHITE);
            rank.setStyle(highlightStyle);
            name.setStyle(highlightStyle);
            score.setStyle(highlightStyle);
            time.setStyle(highlightStyle);
        }

        // 添加头像显示
        Image avatarImage = createAvatarImage(e.avatarId);
        
        row.add(rank).width(50).left();
        row.add(avatarImage).size(32, 32).padLeft(8);
        row.add(name).expandX().left().padLeft(12);
        row.add(score).width(100).right();
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
        // Find the entity with PauseInputComponent and trigger its pause
        triggerPauseSystem(true);
        
        stage.addActor(this);
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f),
                Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void dismiss() {
        // Find the entity with PauseInputComponent and trigger its resume
        triggerPauseSystem(false);
        
        addAction(Actions.sequence(
                Actions.parallel(Actions.alpha(0f, 0.12f), Actions.scaleTo(0.98f, 0.98f, 0.12f)),
                Actions.removeActor()
        ));
    }
    
    /**
     * Triggers the pause system by finding the entity with PauseInputComponent
     * and sending it the appropriate event
     * 
     * @param pause true to pause, false to resume
     */
    private void triggerPauseSystem(boolean pause) {
        try {
            if (ServiceLocator.getEntityService() == null) {
                // Fallback to direct time scale manipulation
                ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
                return;
            }
            
            com.badlogic.gdx.utils.Array<com.csse3200.game.entities.Entity> all = 
                ServiceLocator.getEntityService().getEntities();
            
            // Find the entity with PauseInputComponent
            for (int i = 0; i < all.size; i++) {
                com.csse3200.game.entities.Entity entity = all.get(i);
                if (entity.getComponent(com.csse3200.game.components.maingame.PauseInputComponent.class) != null) {
                    // Found it! Trigger the event it listens for
                    if (pause) {
                        // Check if already paused by checking time scale
                        if (ServiceLocator.getTimeSource().getTimeScale() > 0) {
                            entity.getEvents().trigger("togglePause");
                        }
                    } else {
                        entity.getEvents().trigger("resume");
                    }
                    return;
                }
            }
            
            // If we didn't find PauseInputComponent, fall back to direct time scale manipulation
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
            
        } catch (Exception e) {
            // Fallback to direct time scale manipulation if anything fails
            ServiceLocator.getTimeSource().setTimeScale(pause ? 0f : 1f);
        }
    }
    
    /**
     * Creates the achievement display section
     */
    private void createAchievementSection() {
        achievementTable.clear();
        
        // Title - 使用 segoe_ui 字体
        Label.LabelStyle achievementTitleStyle = new Label.LabelStyle(skin.getFont("segoe_ui"), com.badlogic.gdx.graphics.Color.BLACK);
        Label achievementTitle = new Label("Achievements", achievementTitleStyle);
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