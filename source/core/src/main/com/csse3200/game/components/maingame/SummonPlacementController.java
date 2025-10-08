package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.SummonFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * 专门负责“召唤物放置”的控制器：幽灵预览 + 左键落地。
 * 使用方式：
 *  - armSummon("images/engineer/Sentry.png") 进入放置；
 *  - update() 内部会移动幽灵并处理落地；
 *  - cancelPlacement() 取消放置并清理幽灵。
 */
public class SummonPlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;

    private OrthographicCamera camera;
    private Entity ghostSummon = null;
    private String texture = "images/engineer/Sentry.png";
    private float scale = 1f;

    /** 进入召唤物放置模式 */
    public void armSummon(String texturePath) {
        cancelPlacement();

        this.texture = (texturePath != null && !texturePath.isEmpty()) ? texturePath : this.texture;
        this.placementActive = true;
        this.needRelease = true;

        // 幽灵（只显示，不攻击/不阻挡）
        ghostSummon = SummonFactory.createMeleeSummonGhost(this.texture, scale);
        ServiceLocator.getEntityService().register(ghostSummon);
        ghostSummon.create();

        System.out.println(">>> SummonPlacementController: placement ON, tex=" + this.texture);
    }

    @Override
    public void update() {
        if (!placementActive) return;
        if (camera == null) findWorldCamera();

        TerrainComponent terrain = findTerrain();
        if (camera == null || terrain == null) return;

        // 防止长按直接触发
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        // 屏幕 -> 世界
        Vector3 mp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mp);
        Vector2 mouseWorld = new Vector2(mp.x, mp.y);

        // 吸附到格子
        GridPoint2 tile = new GridPoint2(
                (int)(mouseWorld.x / terrain.getTileSize()),
                (int)(mouseWorld.y / terrain.getTileSize())
        );
        GridPoint2 bounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 && tile.x < bounds.x && tile.y < bounds.y;

        Vector2 snapPos = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;

        if (ghostSummon != null) {
            ghostSummon.setPosition(snapPos);
        }

        // 左键落地（如需限制路径/重叠，可在此加校验）
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (ghostSummon != null) {
                ghostSummon.dispose();
                ghostSummon = null;
            }

            Entity summon = SummonFactory.createMeleeSummon(this.texture, /*sensor=*/false, scale);
            summon.setPosition(snapPos);
            ServiceLocator.getEntityService().register(summon);
            summon.create();

            placementActive = false;
            System.out.println(">>> SummonPlacementController: placed at " + tile);
        }
    }

    /** 取消召唤物放置并清理幽灵 */
    public void cancelPlacement() {
        if (ghostSummon != null) {
            ghostSummon.dispose();
            ghostSummon = null;
        }
        placementActive = false;
        needRelease = false;
        System.out.println(">>> SummonPlacementController: placement OFF");
    }

    public boolean isPlacementActive() { return placementActive; }

    // ===== 工具：找到地形与相机 =====
    private TerrainComponent findTerrain() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return null;
        for (Entity e : all) {
            if (e == null) continue;
            TerrainComponent t = e.getComponent(TerrainComponent.class);
            if (t != null) return t;
        }
        return null;
    }

    private void findWorldCamera() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return;
        for (Entity e : all) {
            if (e == null) continue;
            var cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
            if (cc != null && cc.getCamera() instanceof OrthographicCamera) {
                camera = (OrthographicCamera) cc.getCamera();
                return;
            }
        }
    }
}



