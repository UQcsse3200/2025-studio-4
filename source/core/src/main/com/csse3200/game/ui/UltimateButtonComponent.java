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
 * Ultimate button (shows cost + currency icon, supports cooldown).
 *
 * <ul>
 *   <li>{@code createUltimateButton(hero)} — legacy usage (no cost/icon label)</li>
 *   <li>{@code createUltimateButton(hero, cost, currency)} — displays {@code "ULT (cost)"} with a currency icon</li>
 * </ul>
 * <p>
 * Currency icon images are expected under {@code assets/images/currency/*.png}.
 */
public class UltimateButtonComponent extends Component {

    /**
     * Legacy variant (no cost/currency icon).
     */
    public static TextButton createUltimateButton(Entity heroEntity) {
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), BitmapFont.class);
        skin.add("default", SimpleUI.buttonStyle(), TextButton.TextButtonStyle.class);
        TextButton btn = new TextButton("ULT (?)", skin);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

    /**
     * New: button with cost and currency icon (recommended).
     */
    public static ImageTextButton createUltimateButton(Entity heroEntity, int cost, CurrencyType currencyType) {
        Skin skin = new Skin();
        skin.add("default-font", SimpleUI.font(), BitmapFont.class);

        // Inherit your base button style
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

        // Currency icon
        style.imageUp = currencyIcon(currencyType);

        final String defaultText = buildDefaultText(cost);
        final ImageTextButton btn = new ImageTextButton(defaultText, style);

        // Adjust icon size and label spacing as needed
        btn.getImageCell().size(36f, 36f);
        btn.getLabelCell().padLeft(6f);

        // Click: request ultimate
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                heroEntity.getEvents().trigger("ultimate.request");
            }
        });

        // Cooldown state
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

        // Cooldown countdown (update text only; icon unchanged)
        heroEntity.getEvents().addListener("ultimate.remaining", (Float sec) -> {
            if (sec == null) return;
            btn.setText(String.format("ULT %.1fs", Math.max(0f, sec)));
        });

        heroEntity.getEvents().addListener("ultimate.failed", (String reason) -> {
            Gdx.app.log("ULT", "Failed: " + reason);
        });

        return btn;
    }

    /**
     * Builds the default label text for the ULT button based on cost.
     */
    private static String buildDefaultText(int cost) {
        return cost >= 0 ? "ULT (" + cost + ")" : "ULT (?)";
    }

    /**
     * Maps a currency type to a {@link Drawable} icon.
     * Attempts to fetch via {@link com.csse3200.game.services.ResourceService} if preloaded,
     * otherwise falls back to loading directly from assets.
     */
    private static Drawable currencyIcon(CurrencyType t) {
        String path = switch (t) {
            case METAL_SCRAP -> "images/currency/metal_scrap.png";
            case TITANIUM_CORE -> "images/currency/titanium_core.png";
            case NEUROCHIP -> "images/currency/neurochip.png";
            default -> "images/currency/currency_unknown.png";
        };

        // Prefer ResourceService (if already loaded), else read from assets
        var res = ServiceLocator.getResourceService();
        Texture tex = res != null ? res.getAsset(path, Texture.class) : null;
        if (tex == null) {
            try {
                tex = new Texture(Gdx.files.internal(path));
            } catch (Exception e) {
                // Fallback: simple placeholder to avoid NPEs
                Pixmap pm = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
                pm.setColor(Color.GRAY);
                pm.fill();
                tex = new Texture(pm);
                pm.dispose();
            }
        }
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
    }
}
