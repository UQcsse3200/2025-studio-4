package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.services.ServiceLocator;

public class SaveNameDialog extends Dialog {
    public interface Callback {
        void onConfirmed(String name);
        void onCancelled();
    }

    private final Callback callback;
    private TextField nameField;
    private Label errorLabel;

    public SaveNameDialog(String title, Window.WindowStyle windowStyle, Callback callback) {
        super(title, windowStyle);
        this.callback = callback;
        createContent();
    }

    private void createContent() {
        TextField.TextFieldStyle fieldStyle = createTextFieldStyle();
        nameField = new TextField("", fieldStyle);
        nameField.setMessageText("Enter save name...");
        nameField.setMaxLength(24);

        TextButton.TextButtonStyle buttonStyle = createMainButtonStyle();
        TextButton confirmButton = new TextButton("Confirm", buttonStyle);
        TextButton cancelButton = new TextButton("Cancel", buttonStyle);

        Label.LabelStyle errorStyle = new Label.LabelStyle(SimpleUI.font(), Color.RED);
        errorLabel = new Label("", errorStyle);
        errorLabel.setVisible(false);

        confirmButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { confirm(); }
        });
        cancelButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { cancelDialog(); }
        });
        nameField.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                    confirm();
                }
            }
        });

        // Background panel: use wooden brown background with border
        getContentTable().setBackground(createWoodenBackground());

        Table content = getContentTable();
        content.pad(40);
        
        // Title with enhanced golden color
        Label.LabelStyle titleStyle = new Label.LabelStyle(SimpleUI.font(), new Color(1f, 0.85f, 0.4f, 1f));
        Label title = new Label("Save Game", titleStyle);
        title.setFontScale(1.8f);
        
        content.add(title).colspan(2).padBottom(30); 
        content.row();
        
        // Input field with light background
        content.add(nameField).width(400).height(50).colspan(2).padBottom(15); 
        content.row();
        
        content.add(errorLabel).colspan(2).padBottom(20); 
        content.row();
        
        // Buttons
        Table buttons = new Table();
        buttons.add(cancelButton).width(180).height(60).padRight(20);
        buttons.add(confirmButton).width(180).height(60);
        content.add(buttons).colspan(2);

        setSize(550, 350);
        centerWindow();
    }

    private TextField.TextFieldStyle createTextFieldStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = SimpleUI.font();
        style.fontColor = new Color(0.2f, 0.15f, 0.1f, 1f); // Dark brown text
        // Light tan/beige background for input
        style.background = SimpleUI.solid(new Color(0.9f, 0.8f, 0.65f, 1f));
        style.focusedBackground = SimpleUI.solid(new Color(0.95f, 0.85f, 0.7f, 1f));
        style.cursor = SimpleUI.solid(new Color(0.2f, 0.15f, 0.1f, 1f));
        style.selection = SimpleUI.solid(new Color(0.95f, 0.7f, 0.3f, 0.6f));
        return style;
    }

    private void confirm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showError("Please enter a name"); return; }
        if (name.length() < 2) { showError("Name must be at least 2 characters"); return; }
        if (!name.matches("^[a-zA-Z0-9 _-]+$")) { showError("Only letters, numbers, space, _ and -"); return; }
        hide();
        if (callback != null) callback.onConfirmed(name);
    }

    private void cancelDialog() {
        hide();
        if (callback != null) callback.onCancelled();
    }

    private void showError(String msg) { errorLabel.setText(msg); errorLabel.setVisible(true); }

    private void centerWindow() {
        Stage stage = getStage();
        if (stage != null) {
            setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        }
    }

    @Override public Dialog show(Stage stage) {
        super.show(stage);
        centerWindow();
        stage.setKeyboardFocus(nameField);
        nameField.setCursorPosition(nameField.getText().length());
        return this;
    }

    /**
     * Creates a wooden brown background with border effect
     */
    private Drawable createWoodenBackground() {
        // Create a solid wooden brown background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        // Rich brown color for wooden theme
        pixmap.setColor(new Color(0.45f, 0.28f, 0.18f, 0.95f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        
        TextureRegion region = new TextureRegion(texture);
        NinePatch patch = new NinePatch(region, 3, 3, 3, 3);
        return new NinePatchDrawable(patch);
    }

    /**
     * Create a button style matching the main menu wooden buttons used across the UI.
     */
    private TextButton.TextButtonStyle createMainButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = SimpleUI.font();
        Texture buttonTexture = ServiceLocator.getResourceService()
                .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
        if (buttonTexture != null) {
            TextureRegion buttonRegion = new TextureRegion(buttonTexture);
            NinePatch up = new NinePatch(buttonRegion, 10, 10, 10, 10);
            NinePatch down = new NinePatch(buttonRegion, 10, 10, 10, 10);
            down.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
            NinePatch over = new NinePatch(buttonRegion, 10, 10, 10, 10);
            over.setColor(new Color(1.05f, 1.05f, 1.05f, 1f));
            style.up = new NinePatchDrawable(up);
            style.down = new NinePatchDrawable(down);
            style.over = new NinePatchDrawable(over);
        }
        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;
        return style;
    }
}


