package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** 一个最简单的“个人排名卡片”UI，用假数据展示 */
public class MyRankCard extends Group {
    private final Label nameLbl;
    private final Label rankLbl;
    private final Label scoreLbl;

    public MyRankCard() {
        // 基础字体和样式
        BitmapFont font = new BitmapFont();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        Table root = new Table();
        root.defaults().pad(5);

        nameLbl = new Label("Name: --", style);
        rankLbl = new Label("Rank: --", style);
        scoreLbl = new Label("Score: --", style);

        root.add(nameLbl).left().row();
        root.add(rankLbl).left().row();
        root.add(scoreLbl).left().row();

        root.pack();
        setSize(root.getPrefWidth(), root.getPrefHeight());
        addActor(root);
    }

    /** 刷新卡片内容 */
    public void setData(PlayerRank data) {
        nameLbl.setText("Name: " + data.name);
        rankLbl.setText("Rank: #" + data.rank);
        scoreLbl.setText("Score: " + data.score);
    }
}

