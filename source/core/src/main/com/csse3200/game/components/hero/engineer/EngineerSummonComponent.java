package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.components.maingame.SummonPlacementController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * 工程师召唤组件（方案A：使用 LibGDX InputMultiplexer 直连键盘事件）。
 * - 在 Multiplexer 的 index=0 注入一个 InputAdapter，优先捕获 Q。
 * - 不依赖项目自定义的 InputService，避免被 UI/Stage 吃掉事件。
 */
public class EngineerSummonComponent extends Component {
    private final float cooldownSec;
    private final int maxSummons;
    private final String summonTexture;
    private final Vector2 summonSpeed;

    private float cd = 0f;
    private int alive = 0;

    /**
     * 方案A使用的输入监听器（注意：不是项目的 InputComponent）
     */
    private InputAdapter qAdapter;

    public EngineerSummonComponent(float cooldownSec, int maxSummons,
                                   String summonTexture, Vector2 summonSpeed) {
        this.cooldownSec = cooldownSec;
        this.maxSummons = maxSummons;
        this.summonTexture = summonTexture;
        this.summonSpeed = summonSpeed;
    }

    @Override
    public void create() {
        super.create();

        // 1) 创建一个 InputAdapter，只拦截 Q 键
        // Q 键监听
        qAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (cd > 0f) return true;
                if (alive >= maxSummons) return true;

                var ctrl = findPlacementController();
                if (ctrl == null) return false;

                if (keycode == Input.Keys.NUM_1) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Sentry.png", "melee"
                    ));
                    return true;
                } else if (keycode == Input.Keys.NUM_2) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Turret.png", "turret"
                    ));
                    return true;
                } else if (keycode == Input.Keys.NUM_3) {
                    ctrl.armSummon(new SimplePlacementController.SummonSpec(
                            "images/engineer/Currency_tower.png",  // 放一张你的机器人贴图
                            "currencyBot"                       // 新增类型标识
                    ));
                    return true;
                }

                return false;
            }
        };


        // 3) 把 qAdapter 插到全局 Multiplexer 的最前面（index=0）
        attachToMultiplexer(qAdapter);
    }

    @Override
    public void dispose() {
        super.dispose();
        // 4) 记得从 Multiplexer 中移除监听器，避免泄漏/重复回调
        detachFromMultiplexer(qAdapter);
        qAdapter = null;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (cd > 0f) cd -= dt;
    }

    // ===== 工具方法：挂/卸 Multiplexer =====
    private static void attachToMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) {
            mux.addProcessor(0, adapter); // 放最前
        } else {
            InputMultiplexer mux = new InputMultiplexer();
            mux.addProcessor(adapter);
            if (cur != null) mux.addProcessor(cur);
            Gdx.input.setInputProcessor(mux);
        }
    }

    private static void detachFromMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux && adapter != null) {
            mux.removeProcessor(adapter);
        }
    }

    // ===== 你已有的方法：寻找放置控制器 =====
    // import com.csse3200.game.components.maingame.SimplePlacementController;

    private SimplePlacementController findPlacementController() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return null;
        for (var e : all) {
            if (e == null) continue;
            SimplePlacementController c = e.getComponent(SimplePlacementController.class);
            if (c != null) return c;
        }
        return null;
    }

    // =====（可选）供生成逻辑回调调用：维护冷却与计数 =====
    public void onSummonSpawned() {
        alive++;
        cd = cooldownSec;
    }

    public void onSummonDied() {
        if (alive > 0) alive--;
    }
}

