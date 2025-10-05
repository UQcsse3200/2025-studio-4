package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * 左侧黄色栏里的“英雄状态卡片”。
 * - 显示：头像、名字、HP、能量/怒气、技能CD、等级等
 * - 通过事件刷新：hero.hp, hero.energy, ultimate.remaining, hero.level
 * - 也可以直接持有 hero 引用定时pull（看你项目习惯）
 */
public class HeroStatusPanelComponent extends Component {
    private final Entity hero; // 建议把英雄实体传进来；如果你们用 SelectedHeroService，也可以从那取
    private Stage stage;
    private Table root;         // 贴到 Stage 的根Table（只负责定位）
    private Table card;         // 实际的卡片内容

    // UI 控件引用，便于更新文字
    private Label nameLabel, hpLabel, energyLabel, levelLabel, ultCdLabel;

    public HeroStatusPanelComponent(Entity hero) {
        this.hero = hero;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // ==== 皮肤与字体（沿用你们的 SimpleUI）====
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        // 生成基础 Label 样式
        Label.LabelStyle ls = new Label.LabelStyle(SimpleUI.font(), Color.BLACK);
        skin.add("default", ls);

        // ==== 背景：浅黄卡片 ====
        TextureRegionDrawable yellowBg = new TextureRegionDrawable(makeSolid(240, 200, new Color(0xFFF3ACFF))); // #FFF3AC

        // ==== 左侧根布局：固定在左边，宽度与你黄色栏一致 ====
        root = new Table();
        root.setFillParent(true);
        root.left().center().padLeft(16);    // 左上角
        root.defaults().left();

        // 如果黄色背景是全屏左栏，这里**不再**设置背景；若想自己画出一块卡片：
        card = new Table(skin);
        card.setBackground(yellowBg);
        card.pad(12);
        card.defaults().left().padBottom(6);


        // ==== 文本 ====
        nameLabel   = new Label("Samurai", skin);
        hpLabel     = new Label("HP: 100/100", skin);
        energyLabel = new Label("Energy: 50/50", skin);
        levelLabel  = new Label("Lv. 1", skin);
        ultCdLabel  = new Label("ULT: ready", skin);
        Label damageLabel = new Label("DMG: -", skin);

        // 布局：头像+右侧信息
        Table row = new Table();
        Table info = new Table();
        info.add(nameLabel).left().row();
        info.add(levelLabel).left();
        row.add(info).left().expandX();

        card.add(row).left().row();
        card.add(hpLabel).left().row();
        card.add(damageLabel).left().row();
        card.add(energyLabel).left().row();
        card.add(ultCdLabel).left().row();

        // 把卡片塞到左栏（按你黄色区域从上往下的顺序放）
        root.add(card).width(220).growX();

        stage.addActor(root);

        // ==== 事件对接：根据你现有事件名监听并更新 ====
        // 1) HP 变化
        hero.getEvents().addListener("hero.hp", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            hpLabel.setText("HP: " + cur + "/" + max);
        });

        // ★ 新增：伤害事件
        hero.getEvents().addListener("hero.damage", (Integer dmg) -> {
            if (dmg == null) return;
            damageLabel.setText("DMG: " + dmg);
        });

        // 2) 能量/怒气 变化
        hero.getEvents().addListener("hero.energy", (Integer cur, Integer max) -> {
            if (cur == null || max == null) return;
            energyLabel.setText("Energy: " + cur + "/" + max);
        });

        // 3) 等级变化
        hero.getEvents().addListener("hero.level", (Integer lv) -> {
            if (lv == null) return;
            levelLabel.setText("Lv. " + lv);
        });

        // 4) 大招CD（沿用你 ULT 按钮的事件）
        hero.getEvents().addListener("ultimate.state", (Boolean on) -> {
            if (Boolean.TRUE.equals(on)) {
                ultCdLabel.setText("ULT: ACTIVE");
            } else {
                ultCdLabel.setText("ULT: ready");
            }
        });
        hero.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            float v = Math.max(0f, sec);
            if (v > 0f) {
                ultCdLabel.setText(String.format("ULT: %.1fs", v));
            } else {
                ultCdLabel.setText("ULT: ready");
            }
        });

    }

    @Override
    public void dispose() {
        if (root != null) root.remove(); // 移除UI
    }

    /** 生成纯色背景贴图（小工具） */
    private static TextureRegion makeSolid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegion(tex);
    }
}
