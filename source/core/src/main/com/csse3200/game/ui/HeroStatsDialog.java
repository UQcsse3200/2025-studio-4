package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;

/**
 * 英雄数值窗口：显示等级、生命、攻击，并支持升级。
 */
public class HeroStatsDialog extends Window {
    private final Entity hero;
    private final Label lvlLabel, hpLabel, atkLabel, costLabel;
    private final TextButton upgradeBtn, closeBtn;

    // Follow settings
    private final Vector3 tmp = new Vector3();
    private final Vector2 offset = new Vector2(0f, 48f); // pixels above hero

    // High-res fonts (dispose on remove)
    private BitmapFont fontTitle;
    private BitmapFont fontBody;
    private BitmapFont fontButton;

    public HeroStatsDialog(Entity hero) {
        super("Hero Information", SimpleUI.windowStyle());
        this.hero = hero;

        setModal(true);
        setMovable(true);
        setTouchable(Touchable.enabled); // 确保点击空白背景也能接收事件
        pad(14);
        getTitleLabel().setAlignment(Align.center);

        lvlLabel = new Label("", SimpleUI.label());
        hpLabel = new Label("", SimpleUI.label());
        atkLabel = new Label("", SimpleUI.label());
        costLabel = new Label("", SimpleUI.muted());

        upgradeBtn = new TextButton("Upgrade", SimpleUI.primaryButton());
        closeBtn = new TextButton("Close", SimpleUI.darkButton());

        // 使用高分辨率位图字体并小幅缩放（避免放大导致模糊）
        fontTitle = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
        fontTitle.getData().setScale(0.85f);
        fontTitle.setColor(Color.BLACK);

        fontBody = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
        fontBody.getData().setScale(0.7f);
        fontBody.setColor(Color.BLACK);

        fontButton = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/arial_black_32.fnt"));
        fontButton.getData().setScale(0.75f);
        fontButton.setColor(Color.BLACK);

        getTitleLabel().setStyle(new Label.LabelStyle(fontTitle, Color.BLACK));
        Label.LabelStyle body = new Label.LabelStyle(fontBody, Color.BLACK);
        lvlLabel.setStyle(body);
        hpLabel.setStyle(body);
        atkLabel.setStyle(body);
        costLabel.setStyle(new Label.LabelStyle(fontBody, Color.BLACK));

        TextButton.TextButtonStyle upStyle = SimpleUI.primaryButton();
        upStyle.font = fontButton;
        upStyle.fontColor = Color.BLACK;
        upStyle.overFontColor = Color.BLACK;
        upStyle.downFontColor = Color.BLACK;
        upgradeBtn.setStyle(upStyle);

        TextButton.TextButtonStyle closeStyle = SimpleUI.darkButton();
        closeStyle.font = fontButton;
        closeStyle.fontColor = Color.BLACK;
        closeStyle.overFontColor = Color.BLACK;
        closeStyle.downFontColor = Color.BLACK;
        // 白色背景
        closeStyle.up = SimpleUI.solid(Color.WHITE);
        closeStyle.over = SimpleUI.solid(Color.WHITE);
        closeStyle.down = SimpleUI.solid(Color.WHITE);
        closeBtn.setStyle(closeStyle);

        Table t = new Table();
        t.defaults().pad(6).left();
        t.add(lvlLabel).row();
        t.add(hpLabel).row();
        t.add(atkLabel).row();
        t.add(costLabel).row();

        Table actions = new Table();
        actions.add(upgradeBtn).width(140).padRight(8);
        actions.add(closeBtn).width(140);

        add(t).growX().row();
        add(new Separator()).height(8).growX().row();
        add(actions).right().padTop(6);

        refresh();

        upgradeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // 触发升级事件（Player 可能在 HeroUpgradeComponent 里已 attach）
                hero.getEvents().trigger("requestUpgrade", findPlayer());
                refresh();
            }
        });
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                remove();
            }
        });

        // 使用捕获监听：点击窗口或其任意子元素时置顶
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                toFront();
                if (getStage() != null) setZIndex(getStage().getActors().size - 1);
                return false; // 不拦截，继续分发给子元素
            }
        });

        // 响应升级结果，刷新显示
        hero.getEvents().addListener("upgraded", (Integer level, Object currencyType, Integer cost) -> refresh());
        hero.getEvents().addListener("upgradeFailed", (String msg) -> refresh());
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if (r) {
            if (fontTitle != null) { fontTitle.dispose(); fontTitle = null; }
            if (fontBody != null) { fontBody.dispose(); fontBody = null; }
            if (fontButton != null) { fontButton.dispose(); fontButton = null; }
        }
        return r;
    }

    private Entity findPlayer() {
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            CurrencyManagerComponent wallet = e.getComponent(CurrencyManagerComponent.class);
            if (wallet != null) return e;
        }
        return null;
    }

    private void refresh() {
        HeroUpgradeComponent up = hero.getComponent(HeroUpgradeComponent.class);
        CombatStatsComponent stats = hero.getComponent(CombatStatsComponent.class);

        int lvl = (up != null) ? up.getLevel() : 1;
        int hp = (stats != null) ? stats.getHealth() : 0;
        int atk = (stats != null) ? stats.getBaseAttack() : 0;

        lvlLabel.setText("Level: " + lvl);
        hpLabel.setText("Health: " + hp);
        atkLabel.setText("Damage: " + atk);

        int next = lvl + 1;
        int cost = (next <= 3) ? next * 200 : 0;
        costLabel.setText(next <= 3 ? ("Upgrade cost: " + cost) : "Already reach the max Level");
        upgradeBtn.setDisabled(next > 3);
    }

    public void showOn(Stage stage) {
        stage.addActor(this);
        pack();
        // Enforce a larger minimum size, then place near hero initially
        float minW = 500f;
        float minH = 300f;
        setSize(Math.max(getWidth(), minW), Math.max(getHeight(), minH));
        updateFollowPosition(stage);
        // 确保最新窗口在最上层
        toFront();
        setZIndex(stage.getActors().size - 1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Stage s = getStage();
        if (s != null) updateFollowPosition(s);
    }

    private void updateFollowPosition(Stage stage) {
        Camera cam = getWorldCamera();
        if (cam == null || hero == null) return;

        // Project hero world position to screen
        tmp.set(hero.getPosition().x, hero.getPosition().y, 0f);
        cam.project(tmp);

        float px = tmp.x - getWidth() / 2f + offset.x;
        float py = tmp.y + offset.y;

        // Clamp to stage bounds with small margins
        float margin = 8f;
        px = Math.max(margin, Math.min(px, stage.getWidth() - getWidth() - margin));
        py = Math.max(margin, Math.min(py, stage.getHeight() - getHeight() - margin));

        setPosition(Math.round(px), Math.round(py));
    }

    private Camera getWorldCamera() {
        Renderer r = Renderer.getCurrentRenderer();
        return (r != null && r.getCamera() != null) ? r.getCamera().getCamera() : null;
    }

    // 简单分隔条
    private static class Separator extends Table {
        public Separator() {
            setBackground(SimpleUI.solid(new com.badlogic.gdx.graphics.Color(0,0,0,0.08f)));
        }
    }
} 