package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.services.ServiceLocator;

/**
 * ULT 按钮（带成本+币种图标，支持倒计时）。
 * - createUltimateButton(hero) 兼容旧用法（不显示成本与图标）
 * - createUltimateButton(hero, cost, currency) 显示 "ULT (cost)" + 币种图标
 * 图标文件放在 assets/images/currency/*.png
 */
public class UltimateButtonComponent extends Component {

    /** 兼容旧的（不带成本/图标） */
    public static TextButton createUltimateButton(Entity heroEntity) {
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), BitmapFont.class);
        skin.add("default", SimpleUI.buttonStyle(), TextButton.TextButtonStyle.class);
        TextButton btn = new TextButton("ULT (?)", skin);
        btn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                heroEntity.getEvents().trigger("ultimate.request");
            }
        });
        heroEntity.getEvents().addListener("ultimate.state", (Boolean on) -> {
            boolean active = Boolean.TRUE.equals(on);
            btn.setDisabled(active);
            if (!active) btn.setText("ULT (?)");
        });
        heroEntity.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            btn.setText(String.format("ULT %.1fs", Math.max(0f, sec)));
        });
        heroEntity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });
        return btn;
    }

    /** 新：带成本与币种图标的按钮（推荐） */
    public static ImageTextButton createUltimateButton(Entity heroEntity, int cost, CurrencyType currencyType) {
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), BitmapFont.class);

        // 继承你们的基础按钮样式
        TextButton.TextButtonStyle base = SimpleUI.buttonStyle();
        skin.add("base", base);

        ImageTextButtonStyle style = new ImageTextButtonStyle();
        style.up = base.up;
        style.down = base.down;
        style.over = base.over;
        style.disabled = base.disabled;
        style.font = SimpleUI.font();
        style.fontColor = base.fontColor;
        style.overFontColor = base.overFontColor;
        style.downFontColor = base.downFontColor;

        // 币种图标
        style.imageUp = currencyIcon(currencyType);

        final String defaultText = buildDefaultText(cost);
        final ImageTextButton btn = new ImageTextButton(defaultText, style);

        // 图标大小与间距可按需微调
        btn.getImageCell().size(36f, 36f);
        btn.getLabelCell().padLeft(6f);

        // 点击触发
        btn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                heroEntity.getEvents().trigger("ultimate.request");
            }
        });

        // 冷却状态
        heroEntity.getEvents().addListener("ultimate.state", (Boolean on) -> {
            boolean active = Boolean.TRUE.equals(on);
            btn.setDisabled(active);
            if (active) {
                btn.getLabel().setColor(0.7f, 0.7f, 0.7f, 1f);
                if (btn.getImage() != null) btn.getImage().setColor(1f, 1f, 1f, 0.6f);
            } else {
                btn.setText(defaultText);
                btn.getLabel().setColor(1f, 1f, 1f, 1f);
                if (btn.getImage() != null) btn.getImage().setColor(1f, 1f, 1f, 1f);
            }
        });

        // 冷却倒计时（只更新文字，图标不变）
        heroEntity.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            btn.setText(String.format("ULT %.1fs", Math.max(0f, sec)));
        });

        heroEntity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });

        return btn;
    }

    private static String buildDefaultText(int cost) {
        return cost >= 0 ? "ULT (" + cost + ")" : "ULT (?)";
    }

    /** 将不同币种映射为图标 Drawable（assets/images/currency/*.png） */
    private static Drawable currencyIcon(CurrencyType t) {
        String path = switch (t) {
            case METAL_SCRAP -> "images/currency/metal_scrap.png";
            case TITANIUM_CORE -> "images/currency/titanium_core.png";
            case NEUROCHIP -> "images/currency/neurochip.png";
            default -> "images/currency/currency_unknown.png";
        };

        // 先尝试资源服务（若你已预加载），否则直接从 assets 读取
        var res = ServiceLocator.getResourceService();
        Texture tex = res != null ? res.getAsset(path, Texture.class) : null;
        if (tex == null) {
            try {
                tex = new Texture(Gdx.files.internal(path));
            } catch (Exception e) {
                // 兜底：占位方块，避免 NPE
                Pixmap pm = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
                pm.setColor(Color.GRAY);
                pm.fill();
                tex = new Texture(pm);
                pm.dispose();
            }
        }
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    @Override public void create() {}
    @Override public void dispose() {}
}
