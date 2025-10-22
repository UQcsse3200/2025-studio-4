package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.SummonFactory;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.services.ResourceService;

public class SummonPlacementComponent extends Component {

    /** 召唤规格（纹理 + 类型） */
    public static class SummonSpec {
        public final String texture;  // e.g. "images/engineer/Sentry.png"
        public final String type;     // "melee" | "turret" | "currencyBot"
        public SummonSpec(String texture, String type) { this.texture = texture; this.type = type; }
    }

    private boolean placementActive = false;
    private String pendingType = "melee";
    private String pendingTexture = "images/engineer/Sentry.png";
    private Entity ghost; // 预览实体
    private OrthographicCamera camera;
    private String placeSfxMelee   = "sounds/place_soft_click.ogg";
    private String placeSfxTurret  = "sounds/place_metal_clunk.ogg";
    private String placeSfxCurrency= "sounds/place_energy_drop.ogg";
    private float placeSfxVolume   = 1.0f;
    // 限频，避免一次点击被触发两次
    private float placeSfxMinInterval = 0.05f; // 50ms
    private float placeSfxCd = 0f;

    public SummonPlacementComponent setPlaceSfxKeys(String melee, String turret, String currency) {
        if (melee != null && !melee.isBlank()) this.placeSfxMelee = melee;
        if (turret != null && !turret.isBlank()) this.placeSfxTurret = turret;
        if (currency != null && !currency.isBlank()) this.placeSfxCurrency = currency;
        return this;
    }
    public SummonPlacementComponent setPlaceSfxVolume(float vol) {
        this.placeSfxVolume = Math.max(0f, Math.min(1f, vol));
        return this;
    }
    @Override
    public void create() {
        super.create();
    }

    /** 进入召唤放置模式 */
    public void armSummon(SummonSpec spec) {
        cancel();

        this.placementActive = true;
        this.pendingType = (spec != null && spec.type != null) ? spec.type : "melee";
        this.pendingTexture = (spec != null && spec.texture != null && !spec.texture.isEmpty())
                ? spec.texture : this.pendingTexture;

        // 统一用近战 ghost 作为预览（足够表达 1x1 footprint）
        ghost = SummonFactory.createMeleeSummonGhost(pendingTexture, 1f);
        ServiceLocator.getEntityService().register(ghost);
        ghost.create();

        // 通知 MapHighlighter：进入“召唤放置模式”
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("summon:placement:on", pendingType);
        }
        Gdx.app.log("SummonPlacement", "ON type=" + pendingType);
    }

    /** 退出/取消当前放置 */
    public void cancel() {
        if (ghost != null) {
            try { ghost.dispose(); } catch (Exception ignore) {}
            ghost = null;
        }
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("summon:placement:off");
        }
        placementActive = false;
    }

    @Override
    public void update() {
        if (placeSfxCd > 0f) {
            float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f/60f);
            placeSfxCd = Math.max(0f, placeSfxCd - dt);
        }

        if (!placementActive) return;

        var terrain = findTerrain();
        if (terrain == null) return;

        if (camera == null) findWorldCamera();

        // 屏幕 → 世界
        Vector3 mp3 = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mp3);
        Vector2 mouseWorld = new Vector2(mp3.x, mp3.y);

        // 吸附到格
        float ts = terrain.getTileSize();
        GridPoint2 tile = new GridPoint2((int)(mouseWorld.x / ts), (int)(mouseWorld.y / ts));
        GridPoint2 bounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 && tile.x < bounds.x && tile.y < bounds.y;

        Vector2 snap = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;
        if (ghost != null) ghost.setPosition(snap);

        // 规则：只能 Path 上，且该格没有其他召唤（避免重叠）
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && inBounds) {
            if (!isOnPath(tile)) return;
            if (hasSummonOnTile(tile, ts)) return;
            placeSummon(snap, tile);
        }

        // 右键退出
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            cancel();
        }
    }

    /** 真正创建召唤实体并注册 */
    private void placeSummon(Vector2 snapPos, GridPoint2 tile) {
        String type = pendingType;

        // === [新增] 在真正生成前询问工程师是否允许（会触发 EngineerSummonComponent.canPlace(...)）===
        EngineerSummonComponent owner = findEngineerOwner();
        if (owner != null) {
            final boolean[] allow = { true };
            owner.getEntity().getEvents().trigger("summon:canSpawn?", type, allow); // <-- 关键行（顺序：String, boolean[]）
            if (!allow[0]) {
                cancel();
                Gdx.app.log("SummonPlacement", "blocked by cap at " + tile + " type=" + type);
                return;
            }
        }

        // 清理预览
        if (ghost != null) { try { ghost.dispose(); } catch (Exception ignore) {} ghost = null; }

        // 生成召唤物
        Entity created;
        if ("turret".equals(type)) {
            created = SummonFactory.createDirectionalTurret(pendingTexture, 1f, 1f, new Vector2(-1, 0));
        } else if ("currencyBot".equals(type)) {
            var player = findPlayerEntity();
            created = SummonFactory.createCurrencyBot(
                    pendingTexture, 1f, player,
                    com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP,
                    50, 2f
            );
        } else {
            created = SummonFactory.createMeleeSummon(pendingTexture, false, 1f);
        }

        if (created != null) {
            // 绑定主人：它在 create() 时会自动触发 "summon:spawned"
            created.addComponent(new com.csse3200.game.components.hero.engineer.SummonOwnerComponent(owner, type));
            created.setPosition(snapPos);
            ServiceLocator.getEntityService().register(created);
            playPlaceSfx(type);
            // === [删除] 不要再手动触发 spawned，会双计数 ===
            // if (owner != null) {
            //     owner.getEntity().getEvents().trigger("summon:spawned", created, type);
            // }
        }

        // 退出放置
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("summon:placement:off");
        }
        placementActive = false;
        Gdx.app.log("SummonPlacement", "placed at " + tile + " type=" + type);
    }


    // ================== 工具函数 ==================
    private com.csse3200.game.areas.terrain.ITerrainComponent findTerrain() {
        Array<Entity> all = safeEntities(); if (all == null) return null;
        for (Entity e : all) {
            var t = e.getComponent(com.csse3200.game.areas.terrain.TerrainComponent.class);
            if (t != null) return t;
            var t2 = e.getComponent(com.csse3200.game.areas2.terrainTwo.TerrainComponent2.class);
            if (t2 != null) return t2; // 兼容 TerrainComponent2
        }
        return null;
    }

    private void findWorldCamera() {
        Array<Entity> all = safeEntities(); if (all == null) return;
        for (Entity e : all) {
            var cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
            if (cc != null && cc.getCamera() instanceof OrthographicCamera oc) { camera = oc; return; }
        }
    }

    private boolean isOnPath(GridPoint2 tile) {
        var pc = findPlacementController();
        return pc != null && pc.isPath(tile.x, tile.y); // ✅ 走纯路径
    }


    private boolean hasSummonOnTile(GridPoint2 tile, float ts) {
        Array<Entity> all = safeEntities(); if (all == null) return false;
        for (Entity e : all) {
            if (e == null) continue;
            boolean isSummon =
                    e.getComponent(com.csse3200.game.components.hero.engineer.SummonOwnerComponent.class) != null
                            || e.getComponent(com.csse3200.game.components.hero.engineer.OwnerComponent.class) != null;
            if (!isSummon) continue;
            Vector2 p = e.getPosition(); if (p == null) continue;
            int ex = (int)(p.x / ts), ey = (int)(p.y / ts);
            if (ex == tile.x && ey == tile.y) return true;
        }
        return false;
    }

    private com.csse3200.game.components.maingame.SimplePlacementController findPlacementController() {
        Array<Entity> all = safeEntities(); if (all == null) return null;
        for (Entity e : all) {
            var c = e.getComponent(com.csse3200.game.components.maingame.SimplePlacementController.class);
            if (c != null) return c;
        }
        return null;
    }

    private EngineerSummonComponent findEngineerOwner() {
        Array<Entity> all = safeEntities(); if (all == null) return null;
        for (Entity e : all) {
            var c = e.getComponent(EngineerSummonComponent.class);
            if (c != null) return c;
        }
        return null;
    }

    private Entity findPlayerEntity() {
        Array<Entity> all = safeEntities(); if (all == null) return null;
        for (Entity e : all) {
            if (e.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class) != null)
                return e;
        }
        return null;
    }

    private Array<Entity> safeEntities() {
        try { return ServiceLocator.getEntityService().getEntitiesCopy(); }
        catch (Exception ex) { return null; }
    }
    private void playPlaceSfx(String type) {
        if (placeSfxCd > 0f) return; // 限频
        String key;
        switch (type) {
            case "turret"      -> key = placeSfxTurret;
            case "currencyBot" -> key = placeSfxCurrency;
            default            -> key = placeSfxMelee; // "melee"
        }
        if (key == null || key.isBlank()) return;

        float vol = Math.max(0f, Math.min(1f, placeSfxVolume));

        try {
            ResourceService rs = ServiceLocator.getResourceService();
            Sound s = null;
            if (rs != null) {
                try { s = rs.getAsset(key, Sound.class); } catch (Throwable ignored) {}
            }
            if (s != null) {
                s.play(vol);
            } else {
                // 回退：直接从文件系统加载（确保 assets 里有对应文件）
                if (Gdx.files.internal(key).exists() && Gdx.audio != null) {
                    Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
                    s2.play(vol);
                } else {
                    Gdx.app.error("SummonPlaceSFX", "Sound not found or audio backend null: " + key);
                }
            }
            placeSfxCd = placeSfxMinInterval;
        } catch (Throwable t) {
            Gdx.app.error("SummonPlaceSFX", "Play failed for key=" + key, t);
        }
    }


    public boolean isPlacementActive() { return placementActive; }
    public String getPendingType() { return pendingType; }


    @Override public void dispose() { cancel(); }
}
