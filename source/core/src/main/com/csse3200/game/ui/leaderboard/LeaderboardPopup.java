package com.csse3200.game.ui.leaderboard;

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
    private final ScrollPane scroller;
    private final TextButton prevBtn, nextBtn, closeBtn, friendsBtn;
    private Runnable onCloseCallback;

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

        Table content = new Table();
        content.defaults().pad(6);
        content.add(header).growX();
        content.row();
        content.add(headerRow).growX().padTop(6);
        content.row();
        content.add(scroller).grow().minHeight(360);
        content.row();
        content.add(footer).growX();

        add(content).grow().minSize(720, 540);

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
    }

    private void refreshList() {
        listTable.clearChildren();
        var items = controller.loadPage();
        var me = controller.getMyBest();

        for (LeaderboardEntry e : items) listTable.add(buildRow(e, me)).growX().row();

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

        row.add(rank).width(40).left();
        row.add(name).expandX().left().padLeft(8);
        row.add(score).width(120).right();
        row.add(time).width(160).right();

        return row;
    }

    private String formatTime(long ms) {
        var dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
        return dt.toString().replace('T', ' ');
    }

    /**
     * Sets a callback to be executed when the popup is closed
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public void showOn(Stage stage) {
        stage.addActor(this);
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f),
                Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void dismiss() {
        addAction(Actions.sequence(
                Actions.parallel(Actions.alpha(0f, 0.12f), Actions.scaleTo(0.98f, 0.98f, 0.12f)),
                Actions.run(() -> {
                    if (onCloseCallback != null) {
                        onCloseCallback.run();
                    }
                }),
                Actions.removeActor()
        ));
    }
}
