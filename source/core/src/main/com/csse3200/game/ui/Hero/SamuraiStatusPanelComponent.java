package com.csse3200.game.ui.Hero;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.hero.samurai.SkillCooldowns;
import com.csse3200.game.entities.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 武士状态栏（无召唤区），监听并显示三种剑技的冷却：jab/sweep/spin。
 */
public class SamuraiStatusPanelComponent extends BaseHeroStatusPanelComponent {

    // 显示用的控件映射
    private final Map<String, ProgressBar> bars = new HashMap<>();
    private final Map<String, Label> remainLabels = new HashMap<>();
    private final Map<String, Image> icons = new HashMap<>();

    // 技能顺序与图标（没有图标也没关系，会给占位）
    private static final String[] SKILLS = {"jab", "sweep", "spin"};
    private static final String[] ICONS  = {
            "images/samurai/Stab.png",
            "images/samurai/Slash.png",
            "images/samurai/Spin.png"
    };

    public SamuraiStatusPanelComponent(Entity hero, String heroName) {
        super(
                hero,
                heroName != null ? heroName : "Samurai",
                new Color(0.15f, 0.12f, 0.14f, 0.92f), // 背景更偏红
                Color.WHITE,
                new Color(0.95f, 0.25f, 0.25f, 1f),    // 强调：武士红
                0.24f
        );
    }

    @Override
    protected void buildExtraSections(Table card, Skin skin, float sw, float sh) {
        // 竖排容器
        Table col = new Table();
        col.defaults().pad(4f).left().expandX().fillX();

        ProgressBar.ProgressBarStyle barStyle = buildBarStyle();

        for (int i = 0; i < SKILLS.length; i++) {
            String skill = SKILLS[i];
            String iconPath = (i < ICONS.length) ? ICONS[i] : null;

            Image icon = makeIcon(iconPath);
            icons.put(skill, icon);

            ProgressBar bar = new ProgressBar(0f, 1f, 0.001f, false, barStyle);
            bar.setAnimateDuration(0.08f);
            bar.setValue(1f); // 初始就绪

            Label remain = new Label("", skin);
            remain.setAlignment(Align.left);

            // 一行：左图标 | 右侧（条 + 数字）
            Table row = new Table();
            row.defaults().left().pad(2f);

            row.add(icon).size(26f).padRight(6f);

            Table right = new Table();
            right.defaults().left().expandX().fillX();
            right.add(bar).height(8f).expandX().fillX().row(); // 关键：expandX + fillX
            right.add(remain).left();

            row.add(right).expandX().fillX();

            col.add(row).expandX().fillX().row();

            bars.put(skill, bar);
            remainLabels.put(skill, remain);
        }

        // 放进卡片
        card.add(col).left().expandX().fillX().row();
    }



    @Override
    protected void bindExtraListeners() {
        // 监听来自 hero 的技能 CD 事件（确保剑侧有转发）
        hero.getEvents().addListener("skill:cooldown", (SkillCooldowns.SkillCooldownInfo info) -> {
            updateSkillUI(info.skill, info.progress01, info.remain);
        });

        hero.getEvents().addListener("skill:ready", (String skill) -> {
            updateSkillUI(skill, 1f, 0f);
            flashReady(skill);
        });
    }

    // ==== 内部：更新与小效果 ====

    private void updateSkillUI(String skill, float progress01, float remainSec) {
        ProgressBar bar = bars.get(skill);
        if (bar != null) {
            bar.setValue(clamp01(progress01));
        }
        Label label = remainLabels.get(skill);
        if (label != null) {
            if (remainSec > 0.001f) {
                float display = (float) Math.ceil(remainSec * 10f) / 10f; // 一位小数向上取整
                label.setText(display + "s");
            } else {
                label.setText("");
            }
        }
    }

    private void flashReady(String skill) {
        Image icon = icons.get(skill);
        if (icon == null) return;
        icon.clearActions();
        icon.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.15f, 1.15f, 0.08f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.10f)
        ));
    }

    private static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    private Image makeIcon(String path) {
        try {
            if (path != null) {
                Texture tex = new Texture(path);
                return new Image(new TextureRegionDrawable(new TextureRegion(tex)));
            }
        } catch (Throwable ignored) {}
        // 占位：深灰色方块
        return new Image(new TextureRegionDrawable(makeSolid(12, 12, new Color(0.27f,0.27f,0.30f,1f))));
    }
}




