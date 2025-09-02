package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class RankingDialog extends Dialog {
    public RankingDialog() {
        super("Ranking", SimpleUI.windowStyle());
        setModal(true);
        setMovable(true);
        setResizable(false);

        // 弹窗内容
        Table content = getContentTable();
        MyRankCard card = new MyRankCard();
        card.setData(PlayerRank.mock()); // 本地假数据
        content.add(card).pad(10);

        // 关闭按钮
        button("Close");
    }

    /** 工具方法：在舞台中央显示 */
    public void showCentered(Stage stage) {
        show(stage);
        pack();
        setPosition(
                (stage.getWidth()  - getWidth()) / 2f,
                (stage.getHeight() - getHeight()) / 2f
        );
    }
}
