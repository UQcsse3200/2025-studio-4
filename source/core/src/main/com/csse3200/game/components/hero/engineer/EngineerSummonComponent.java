package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * 工程师召唤组件（方案A：事件拦截）
 * - 键位触发 armSummon（1/2/3）
 * - 放置“前”通过事件询问是否允许（summon:canSpawn?）
 * - 召唤物生成后/死亡后通过事件维护数量（summon:spawned / summon:died）
 * - HUD 监听 summonAliveChanged(alive, max) 实时显示
 */
public class EngineerSummonComponent extends Component {
    private final float cooldownSec;         // 召唤CD（秒）
    private final int maxSummons;            // 全局上限
    private final String summonTexture;      // 默认贴图（可不用）
    private final Vector2 summonSpeed;       // 默认速度（可不用）

    private float cd = 0f;                   // CD计时
    private int alive = 0;                   // 当前总存活

    // （可选）类型上限：不需要可清空
    private final Map<String, Integer> typeCaps = new HashMap<>();
    // 每类型已存活数
    private final Map<String, Integer> aliveByType = new HashMap<>();

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


        // melee 不做限制则不写

        // ① 放置前询问：Placement 发出 "summon:canSpawn?"
        entity.getEvents().addListener("summon:canSpawn?", (String type, boolean[] allow) -> {
            if (!canPlace(type)) allow[0] = false;
        });

        // ② 召唤物生成后 +1
        entity.getEvents().addListener("summon:spawned", (Entity e, String type) -> onSummonSpawned(type));

        // ③ 召唤物死亡/移除后 -1
        entity.getEvents().addListener("summon:died", (Entity e, String type) -> onSummonDied(type));

        // 键盘适配器（你的 Plan A）
        attachToMultiplexer(qAdapter = makeKeyAdapter());
    }

    @Override
    public void dispose() {
        super.dispose();
        detachFromMultiplexer(qAdapter);
        qAdapter = null;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (cd > 0f) cd -= dt;
    }

    // ===== 键盘监听 =====
    private InputAdapter makeKeyAdapter() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (cd > 0f) return true; // 在CD内，吞掉按键
                var ctrl = findPlacementController();
                if (ctrl == null) return false;

                String type = null;
                String tex = null;

                if (keycode == Input.Keys.NUM_1) {
                    type = "melee";
                    tex = "images/engineer/Sentry.png";
                } else if (keycode == Input.Keys.NUM_2) {
                    type = "turret";
                    tex = "images/engineer/Turret.png";
                } else if (keycode == Input.Keys.NUM_3) {
                    type = "currencyBot";
                    tex = "images/engineer/Currency_tower.png";
                } else {
                    return false;
                }

                // 这里只负责“切到放置模式”；真正生成时，Placement 会再通过事件问可不可以
                ctrl.armSummon(new SimplePlacementController.SummonSpec(tex, type));
                return true;
            }
        };
    }

    // ===== 能否放置（按照全局上限 + 类型上限） =====
    private boolean canPlace(String type) {
        if (alive >= maxSummons) return false;
        Integer cap = typeCaps.get(type);
        if (cap != null) {
            int cur = aliveByType.getOrDefault(type, 0);
            if (cur >= cap) return false;
        }
        return true;
    }

    // ===== 计数 + HUD 通知 =====
    public void onSummonSpawned(String type) {
        alive++;
        aliveByType.put(type, aliveByType.getOrDefault(type, 0) + 1);
        cd = cooldownSec;
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
    }

    public void onSummonDied(String type) {
        if (alive > 0) alive--;
        if (type != null) {
            int cur = aliveByType.getOrDefault(type, 0);
            aliveByType.put(type, Math.max(0, cur - 1));
        }
        entity.getEvents().trigger("summonAliveChanged", alive, maxSummons);
    }

    // ===== LibGDX multiplexer 工具 =====
    private static void attachToMultiplexer(InputAdapter adapter) {
        var cur = Gdx.input.getInputProcessor();
        if (cur instanceof InputMultiplexer mux) {
            mux.addProcessor(0, adapter);
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

    // ===== 工具：找到 PlacementController =====
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
}
