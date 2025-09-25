package com.csse3200.game.components.heroselect;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Hero selection UI styled to match MainMenuDisplay.
 * 三张卡片：Hero / Engineer / Samurai
 */
public class HeroSelectDisplay extends UIComponent {
    private Table root;

    // Asset keys (must already be loaded via ResourceService)
    private static final String BG_PATH = "images/main_menu_background.png";
    private static final String HERO_IMG = "images/hero/Heroshoot.png";
    private static final String ENG_IMG = "images/engineer/Engineer.png";
    private static final String SAM_IMG = "images/samurai/Samurai.png"; // 新增：武士图片
    private static final String BTN_BG = "images/Main_Menu_Button_Background.png";

    @Override
    public void create() {
        super.create();

        // ===== root & background =====
        root = new Table();
        root.setFillParent(true);
        root.defaults().pad(12f);

        Image bg = new Image(ServiceLocator.getResourceService().getAsset(BG_PATH, Texture.class));
        bg.setFillParent(true);
        stage.addActor(bg);

        // ===== styles (match main menu) =====
        TextButtonStyle btnStyle = createCustomButtonStyle();
        LabelStyle titleStyle = new LabelStyle(skin.getFont("segoe_ui"), Color.WHITE);
        LabelStyle nameStyle = new LabelStyle(skin.getFont("segoe_ui"), Color.WHITE);

        // ===== title =====
        Label title = new Label("Select Your Hero", titleStyle);
        title.setAlignment(Align.center);

        float imgSize = 96f;

        // ===== hero card =====
        Image heroImg = new Image(ServiceLocator.getResourceService().getAsset(HERO_IMG, Texture.class));
        heroImg.setSize(imgSize, imgSize);

        TextButton heroBtn = new TextButton("Hero", btnStyle);
        heroBtn.addListener(e -> {
            if (!heroBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("pickHero");
            return true;
        });

        Table heroCol = new Table();
        heroCol.defaults().pad(6f).center();
        heroCol.add(heroImg).size(imgSize).padBottom(6f).row();
        heroCol.add(new Label("Hero", nameStyle)).padBottom(6f).row();
        heroCol.add(heroBtn).width(220f).height(56f);

        // ===== engineer card =====
        Image engImg = new Image(ServiceLocator.getResourceService().getAsset(ENG_IMG, Texture.class));
        engImg.setSize(imgSize, imgSize);

        TextButton engBtn = new TextButton("Engineer", btnStyle);
        engBtn.addListener(e -> {
            if (!engBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("pickEngineer");
            return true;
        });

        Table engCol = new Table();
        engCol.defaults().pad(6f).center();
        engCol.add(engImg).size(imgSize).padBottom(6f).row();
        engCol.add(new Label("Engineer", nameStyle)).padBottom(6f).row();
        engCol.add(engBtn).width(220f).height(56f);

        // ===== samurai card（新增） =====
        Image samImg = new Image(ServiceLocator.getResourceService().getAsset(SAM_IMG, Texture.class));
        samImg.setSize(imgSize, imgSize);

        TextButton samBtn = new TextButton("Samurai", btnStyle);
        samBtn.addListener(e -> {
            if (!samBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("pickSamurai"); // 对应 MainMenuActions.onPickSamurai
            return true;
        });

        Table samCol = new Table();
        samCol.defaults().pad(6f).center();
        samCol.add(samImg).size(imgSize).padBottom(6f).row();
        samCol.add(new Label("Samurai", nameStyle)).padBottom(6f).row();
        samCol.add(samBtn).width(220f).height(56f);

        // ===== cards row (horizontal) =====
        Table cardsRow = new Table();
        cardsRow.defaults().pad(16f).top();
        cardsRow.add(heroCol).padRight(24f);
        cardsRow.add(engCol).padLeft(12f).padRight(12f);
        cardsRow.add(samCol).padLeft(24f); // 新增 Samurai 列

        // ===== back =====
        TextButton backBtn = new TextButton("Back", btnStyle);
        backBtn.addListener(e -> {
            if (!backBtn.isPressed()) return false;
            if (entity != null) entity.getEvents().trigger("goBackToMenu");
            return true;
        });

        // ===== layout =====
        root.add().expandY().row();
        root.add(title).padTop(10f).row();
        root.add(cardsRow).padTop(10f).row();
        root.add(backBtn).width(240f).height(56f).padTop(14f).row();
        root.add().expandY();

        stage.addActor(root);
    }

    /**
     * Create custom button style using same look as MainMenuDisplay.
     */
    private TextButtonStyle createCustomButtonStyle() {
        TextButtonStyle style = new TextButtonStyle();
        style.font = skin.getFont("segoe_ui");

        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(BTN_BG, Texture.class);
        TextureRegion region = new TextureRegion(buttonTexture);

        NinePatch up = new NinePatch(region, 10, 10, 10, 10);
        NinePatch down = new NinePatch(region, 10, 10, 10, 10);
        down.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        NinePatch over = new NinePatch(region, 10, 10, 10, 10);
        over.setColor(new Color(1.08f, 1.08f, 1.08f, 1f));

        style.up = new NinePatchDrawable(up);
        style.down = new NinePatchDrawable(down);
        style.over = new NinePatchDrawable(over);
        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;
        return style;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // global stage draw handles it
    }

    @Override
    public void dispose() {
        if (root != null) {
            root.clear();
            root.remove();
            root = null;
        }
        super.dispose();
    }
}



