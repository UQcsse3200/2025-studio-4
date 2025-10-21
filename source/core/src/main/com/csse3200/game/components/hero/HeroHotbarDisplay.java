package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.configs.HeroSkinAtlas;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameStateService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * 右侧中部的英雄热键栏（百分比布局版）。
 * - 宽/高/边距使用 Value.percentWidth/percentHeight
 * - 始终位于右侧，垂直居中
 * - 仅显示当前已选英雄的图标按钮，点击可调用 HeroPlacementComponent 进行放置/取消
 */
public class HeroHotbarDisplay extends UIComponent {
    private Table rootTable;
    private Skin uiSkin;
    private HeroPlacementComponent placement;
    private boolean isVisible = true; // 默认可见

    // 背景纹理（手动创建，需要释放）
    private Texture bgTexture;

    // 图标纹理（需要释放）
    private Texture engTex;
    private Texture samTex;
    private Texture defTex;
    private ImageButton chosenBtn;
    private Table btnTable;
    private AutoCloseable unsubSkinChanged;
    private AutoCloseable unsubSelectedHeroChanged;


    @Override
    public void create() {
        super.create();
        placement = entity.getComponent(HeroPlacementComponent.class);
        uiSkin = skin;

        // ===== 1) 根表填满舞台，用于百分比定位 =====
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // ===== 2) 背景容器（半透明深色）=====
        bgTexture = buildSolidTexture(new Color(0.15f, 0.15f, 0.18f, 0.9f));
        Drawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Container<Table> container = new Container<>();
        container.setBackground(bgDrawable);
        // 可按需要用百分比 padding：但这里我们用外层单元格的 padRight 来控制与屏幕右侧的距离
        // container.pad(Value.percentWidth(0.006f, rootTable));

        // ===== 3) 内容：标题 + 图标按钮（仅显示已选英雄）=====
        Table content = new Table();

        Label title = new Label("HERO", uiSkin, "title");
        title.setAlignment(Align.center);
        content.add(title)
                .center()
                .padBottom(Value.percentHeight(0.02f, rootTable))
                .row();

        // 图标按钮区域（只放一个按钮）
        Table btnTable = new Table();
        ImageButton chosenBtn = buildChosenHeroButton();
        // 按钮尺寸也用百分比，基于 rootTable（舞台）或 content 都可以；
        // 这里直接基于 rootTable，使得在不同分辨率下更稳定：
        btnTable.add(chosenBtn)
                .width(Value.percentWidth(0.08f, rootTable))   // 按钮宽 = 屏幕宽的 8%
                .height(Value.percentHeight(0.12f, rootTable))  // 按钮高 = 屏幕高的 12%
                .center();

        ScrollPane sp = new ScrollPane(btnTable, uiSkin);
        sp.setScrollingDisabled(true, true);
        sp.setFadeScrollBars(false);

        content.add(sp).expand().fill();

        container.setActor(content);

        // ===== 4) 把容器放到右侧中部（百分比宽高 + 右侧边距）=====
        rootTable.add(container)
                .width(Value.percentWidth(0.195f, rootTable))
                .height(Value.percentHeight(0.28f, rootTable))   // 面板高 = 屏幕高的 28%
                .expand()                                        // 占据可用空间（让对齐生效）
                .align(Align.right)                              // 水平贴右，垂直默认居中
                .padRight(Value.percentWidth(0f, rootTable));

        // 如果想让它在垂直方向略微上移/下移，可在这里加 padTop/padBottom 的百分比：
        // .padTop(Value.percentHeight(0.03f, rootTable))
        // .padBottom(Value.percentHeight(0.01f, rootTable))

        applyUiScale(); // 维持你原先的 UI 缩放逻辑
        GameStateService gs = ServiceLocator.getGameStateService();

// 皮肤变化：如果变的是当前选中英雄，就刷新热键图标
        unsubSkinChanged = gs.onSkinChanged((who, newSkin) -> {
            if (gs.getSelectedHero() == who) {
                refreshChosenHeroIcon(); // 只换图，不重建按钮
            }
        });

// 选中英雄变化：重建按钮（图标与点击逻辑一起变）
        unsubSelectedHeroChanged = gs.onSelectedHeroChanged(nowSelected -> {
            rebuildChosenHeroButton();   // 重建 UI 上的按钮
        });
    }

    /**
     * 根据当前 GameStateService 选择的英雄，创建对应的图标按钮并挂载点击逻辑。
     */
    private ImageButton buildChosenHeroButton() {
        GameStateService gs = ServiceLocator.getGameStateService();
        GameStateService.HeroType chosen =
                (gs != null) ? gs.getSelectedHero() : GameStateService.HeroType.HERO;

        String skinKey = (gs != null) ? gs.getSelectedSkin(chosen) : "default";
        String iconPath = HeroSkinAtlas.body(chosen, skinKey);

        // 建议用 ResourceService 避免重复 new Texture
        var rs = ServiceLocator.getResourceService();
        Texture tex = rs.getAsset(iconPath, Texture.class);
        if (tex == null) {
            rs.loadTextures(new String[]{iconPath});
            while (!rs.loadForMillis(10)) {}
            tex = rs.getAsset(iconPath, Texture.class);
        }

        ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(tex)));

        // 点击回调（保持你原来的逻辑）
        switch (chosen) {
            case ENGINEER -> addHeroClick(btn, "engineer");
            case SAMURAI  -> addHeroClick(btn, "samurai");
            default       -> addHeroClick(btn, "default");
        }
        return btn;
    }


    /**
     * 点击回调：请求/取消放置对应英雄。
     */
    private void addHeroClick(ImageButton btn, String heroType) {
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (placement != null) {
                    // 再次点击同类型会在 HeroPlacementComponent 内部切换为取消
                    placement.requestPlacement(heroType);
                } else {
                    // 兜底事件（如果没挂载组件）
                    entity.getEvents().trigger("heroPlacement:request", heroType);
                }
            }
        });
    }

    /**
     * 应用用户设置中的 UI 缩放。
     */
    private void applyUiScale() {
        UserSettings.Settings st = UserSettings.get();
        if (rootTable != null) {
            rootTable.setTransform(true);
            rootTable.validate();
            rootTable.setOrigin(0f, 0f);
            rootTable.setScale(st.uiScale);
        }
    }

    /**
     * 工具：创建纯色纹理（用作半透明背景）
     */
    private Texture buildSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    /**
     * 工具：安全加载纹理（出现问题时返回 1x1 的透明纹理，避免 NPE）
     */
    private Texture safeLoad(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("HeroHotbarDisplay", "Failed to load texture: " + path, e);
            // fallback：透明像素
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(0, 0, 0, 0);
            pm.fill();
            Texture tex = new Texture(pm);
            pm.dispose();
            return tex;
        }
    }

    private void refreshChosenHeroIcon() {
        if (chosenBtn == null) return;
        GameStateService gs = ServiceLocator.getGameStateService();
        if (gs == null) return;

        GameStateService.HeroType chosen = gs.getSelectedHero();
        String skinKey = gs.getSelectedSkin(chosen);
        String iconPath = HeroSkinAtlas.body(chosen, skinKey);

        Texture tex = safeLoad(iconPath); // 或用 ResourceService 方案
        chosenBtn.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(tex));
        chosenBtn.invalidateHierarchy();
    }

    private void rebuildChosenHeroButton() {
        if (chosenBtn == null) return;
        Actor parent = chosenBtn.getParent();
        if (!(parent instanceof Table btnTable)) return;

        ImageButton newBtn = buildChosenHeroButton();
        btnTable.clearChildren();
        btnTable.add(newBtn)
                .width(Value.percentWidth(0.08f, rootTable))
                .height(Value.percentHeight(0.12f, rootTable))
                .center();
        btnTable.invalidateHierarchy();
        chosenBtn = newBtn;
    }

    /**
     * 设置英雄UI的可见性
     * @param visible true显示，false隐藏
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (rootTable != null) {
            rootTable.setVisible(visible);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // UI 用 Stage 渲染，这里无需额外绘制
    }

    @Override
    public void dispose() {
        if (rootTable != null) rootTable.clear();
        try { if (unsubSkinChanged != null) unsubSkinChanged.close(); } catch (Exception ignore) {}
        try { if (unsubSelectedHeroChanged != null) unsubSelectedHeroChanged.close(); } catch (Exception ignore) {}

        // 释放手动创建/加载的纹理
        if (bgTexture != null) { bgTexture.dispose(); bgTexture = null; }
        if (engTex != null) { engTex.dispose(); engTex = null; }
        if (samTex != null) { samTex.dispose(); samTex = null; }
        if (defTex != null) { defTex.dispose(); defTex = null; }
        super.dispose();
    }
}
