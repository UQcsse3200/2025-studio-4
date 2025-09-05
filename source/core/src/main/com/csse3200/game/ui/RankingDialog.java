package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import java.util.List;

public class RankingDialog extends Dialog {
    private final List<PlayerRank> players;
    private final int pageSize;
    private int currentPage = 0;
    private float prefW = 1000f, prefH = 650f;

    public RankingDialog(String title, List<PlayerRank> players, int pageSize) {
        super(title, SimpleUI.windowStyle());
        this.players = players;
        this.pageSize = pageSize;

        setModal(true);
        setMovable(true);
        setResizable(false);

        refreshPage();

        createBottomButtons();
    }
    private Image createSeparator() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1f, 1f, 1f, 0.2f)); // 半透明白线
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new Image(texture);
    }

    private void refreshPage() {
        Table content = getContentTable();
        content.clear();

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, players.size());

        // gauge outfit
        Table header = new Table();
        header.add(new Label("#", SimpleUI.label())).width(60).left().pad(5);
        header.add(new Label("Player", SimpleUI.label())).expandX().left().pad(5);
        header.add(new Label("Score", SimpleUI.label())).width(100).right().pad(5);
        content.add(header).growX().row();

        // table row
        for (int i = start; i < end; i++) {
            PlayerRank p = players.get(i);
            Table row = new Table();
            row.add(new Label(String.valueOf(p.rank), SimpleUI.label())).width(60).left().pad(5);
            row.add(new Label(p.name, SimpleUI.label())).expandX().left().pad(5);
            row.add(new Label(String.valueOf(p.score), SimpleUI.label())).width(100).right().pad(5);
            content.add(row).growX().row();
            Image separator = createSeparator();
            content.add(separator).height(1).expandX().fillX().padBottom(5).row();
        }

        // Enlarge the table as a whole
        content.pad(20);
    }

    private void createBottomButtons() {
        TextButton prevBtn = new TextButton("Prev", SimpleUI.buttonStyle());
        TextButton nextBtn = new TextButton("Next", SimpleUI.buttonStyle());
        TextButton closeBtn = new TextButton("Close", SimpleUI.buttonStyle());

        prevBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentPage > 0) {
                    currentPage--;
                    refreshPage();
                }
            }
        });

        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ((currentPage + 1) * pageSize < players.size()) {
                    currentPage++;
                    refreshPage();
                }
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        // 底部按钮排列：Prev | 页码 | Next   +   Close 在右边
        Table buttons = getButtonTable();
        buttons.clear();
        buttons.left();
        buttons.add(prevBtn).pad(10);
        buttons.add(new Label("Page " + (currentPage + 1) + " / " +
                Math.max(1, (int) Math.ceil(players.size() / (float) pageSize)),
                SimpleUI.label())).pad(10);
        buttons.add(nextBtn).pad(10);
        buttons.add().expandX(); // 占位，把 Close 挤到右边
        buttons.add(closeBtn).pad(10);
    }
    @Override
    public Dialog show(Stage stage) {
        Dialog d = super.show(stage);

        // Customized width and height (adjusted as needed)
        float prefW = 1000f;  // 例如 1000px
        float prefH = 650f;   // 例如 650px
        setSize(prefW, prefH);

        // Repositioning
        setPosition(
                (stage.getWidth() - prefW) / 2f,
                (stage.getHeight() - prefH) / 2f
        );

        return d;
    }

}
