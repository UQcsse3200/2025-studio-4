package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.List;

public class MyRankCard extends Group {
    private final Table root;
    private final Table titleBar;
    private final Table content;

    private final Label titleLbl;
    private final Label nameLbl;
    private final Label rankLbl;
    private final Label scoreLbl;

    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;


    private final List<Texture> ownedTextures = new ArrayList<>();

    public MyRankCard() {
        // 1) Font (LibGDX comes with default font)
        titleFont = new BitmapFont();
        bodyFont = new BitmapFont();

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label.LabelStyle bodyStyle  = new Label.LabelStyle(bodyFont,  Color.WHITE);

        // 2) Root Table
        root = new Table();
        root.pad(12);
        root.defaults().left().pad(4);
        addActor(root);

        root.setBackground(makePanelBg(
                new Color(0, 0, 0, 0.80f),
                new Color(1, 1, 1, 0.12f)
        ));

        // 3) Title Bar
        titleBar = new Table();
        titleBar.pad(6);
        titleBar.setBackground(makeSolidBg(new Color(0.15f, 0.17f, 0.22f, 1f)));

        titleLbl = new Label("Leaderboard", titleStyle);
        titleBar.add(titleLbl).left();

        root.add(titleBar).growX().row();

        // 4) Content Area
        content = new Table();
        content.defaults().left().pad(2);

        nameLbl  = new Label("Name: --", bodyStyle);
        rankLbl  = new Label("Rank: --", bodyStyle);
        scoreLbl = new Label("Score: --", bodyStyle);

        content.add(nameLbl).row();
        content.add(rankLbl).row();
        content.add(scoreLbl).row();

        root.add(content).growX().row();

        // 5) Calculate your own size based on the content
        packSelf();
    }

    /** Refresh data with complete PlayerRank */
    public void setData(PlayerRank pr) {
        setData(pr.name, pr.rank, pr.score);
    }

    /** Refresh data using only name/rank/score (more convenient) */
    public void setData(String name, int rank, int score) {
        nameLbl.setText("Name: " + name);
        rankLbl.setText("Rank: #" + rank);
        scoreLbl.setText("Score: " + score);
        packSelf();
    }

    /** Modify the title text */
    public void setTitle(String title) {
        titleLbl.setText(title);
        packSelf();
    }

    /** Release the texture we created (called when Screen # dispose() or Actor is removed) */
    public void dispose() {
        for (Texture t : ownedTextures) {
            if (t != null) t.dispose();
        }
        ownedTextures.clear();
    }


    private void packSelf() {
        root.pack();
        setSize(root.getPrefWidth(), root.getPrefHeight());
    }

    /** Generate solid color background */
    private Drawable makeSolidBg(Color color) {
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fillRectangle(0, 0, pm.getWidth(), pm.getHeight());
        Texture tex = new Texture(pm);
        pm.dispose();
        ownedTextures.add(tex);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    /** Generate panel background with borders */
    private Drawable makePanelBg(Color fill, Color border) {
        int w = 32, h = 32;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        pm.setColor(fill);
        pm.fillRectangle(0, 0, w, h);

        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);

        Texture tex = new Texture(pm);
        pm.dispose();
        ownedTextures.add(tex);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }
}
