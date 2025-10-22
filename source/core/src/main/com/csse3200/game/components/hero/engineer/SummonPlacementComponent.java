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

    /** Summon specification (texture + type) */
    public static class SummonSpec {
        public final String texture;  // e.g. "images/engineer/Sentry.png"
        public final String type;     // "melee" | "turret" | "currencyBot"
        public SummonSpec(String texture, String type) { this.texture = texture; this.type = type; }
    }

    private boolean placementActive = false;
    private String pendingType = "melee";
    private String pendingTexture = "images/engineer/Sentry.png";
    private Entity ghost; // Preview entity
    private OrthographicCamera camera;
    private String placeSfxMelee   = "sounds/place_soft_click.ogg";
    private String placeSfxTurret  = "sounds/place_metal_clunk.ogg";
    private String placeSfxCurrency= "sounds/place_energy_drop.ogg";
    private float placeSfxVolume   = 1.0f;
    // Rate limit to avoid double-trigger on a single click
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

    /** Enter summon placement mode */
    public void armSummon(SummonSpec spec) {
        cancel();

        this.placementActive = true;
        this.pendingType = (spec != null && spec.type != null) ? spec.type : "melee";
        this.pendingTexture = (spec != null && spec.texture != null && !spec.texture.isEmpty())
                ? spec.texture : this.pendingTexture;

        // Use the melee ghost as a unified preview (expresses a 1x1 footprint well enough)
        ghost = SummonFactory.createMeleeSummonGhost(pendingTexture, 1f);
        ServiceLocator.getEntityService().register(ghost);
        ghost.create();

        // Notify MapHighlighter: entered "summon placement mode"
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("summon:placement:on", pendingType);
        }
        Gdx.app.log("SummonPlacement", "ON type=" + pendingType);
    }

    /** Exit/cancel current placement */
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

        // Screen → world
        Vector3 mp3 = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mp3);
        Vector2 mouseWorld = new Vector2(mp3.x, mp3.y);

        // Snap to grid
        float ts = terrain.getTileSize();
        GridPoint2 tile = new GridPoint2((int)(mouseWorld.x / ts), (int)(mouseWorld.y / ts));
        GridPoint2 bounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 && tile.x < bounds.x && tile.y < bounds.y;

        Vector2 snap = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;
        if (ghost != null) ghost.setPosition(snap);

        // Rule: must be on the Path, and the tile must not already have a summon (avoid overlap)
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && inBounds) {
            if (!isOnPath(tile)) return;
            if (hasSummonOnTile(tile, ts)) return;
            placeSummon(snap, tile);
        }

        // Right-click to exit
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            cancel();
        }
    }

    /** Actually create the summon entity and register it */
    private void placeSummon(Vector2 snapPos, GridPoint2 tile) {
        String type = pendingType;

        // === [New] Before spawning, ask Engineer if placement is allowed (triggers EngineerSummonComponent.canPlace(...)) ===
        EngineerSummonComponent owner = findEngineerOwner();
        if (owner != null) {
            final boolean[] allow = { true };
            owner.getEntity().getEvents().trigger("summon:canSpawn?", type, allow); // <-- key line (order: String, boolean[])
            if (!allow[0]) {
                cancel();
                Gdx.app.log("SummonPlacement", "blocked by cap at " + tile + " type=" + type);
                return;
            }
        }

        // Clear preview
        if (ghost != null) { try { ghost.dispose(); } catch (Exception ignore) {} ghost = null; }

        // Spawn summon
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
            // Bind owner: it will automatically trigger "summon:spawned" in create()
            created.addComponent(new com.csse3200.game.components.hero.engineer.SummonOwnerComponent(owner, type));
            created.setPosition(snapPos);
            ServiceLocator.getEntityService().register(created);
            playPlaceSfx(type);
            // === [Removed] Do not manually trigger spawned again, it would double-count ===
            // if (owner != null) {
            //     owner.getEntity().getEvents().trigger("summon:spawned", created, type);
            // }
        }

        // Exit placement
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("summon:placement:off");
        }
        placementActive = false;
        Gdx.app.log("SummonPlacement", "placed at " + tile + " type=" + type);
    }


    // ================== Utilities ==================
    private com.csse3200.game.areas.terrain.ITerrainComponent findTerrain() {
        Array<Entity> all = safeEntities(); if (all == null) return null;
        for (Entity e : all) {
            var t = e.getComponent(com.csse3200.game.areas.terrain.TerrainComponent.class);
            if (t != null) return t;
            var t2 = e.getComponent(com.csse3200.game.areas2.terrainTwo.TerrainComponent2.class);
            if (t2 != null) return t2; // Compatible with TerrainComponent2
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
        return pc != null && pc.isPath(tile.x, tile.y); // ✅ strictly path-only
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
        if (placeSfxCd > 0f) return; // rate limit
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
                // Fallback: load directly from file system (ensure corresponding file exists in assets)
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
