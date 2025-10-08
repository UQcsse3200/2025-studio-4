package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
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

        // Background panel: solid color for clarity
        getContentTable().setBackground(SimpleUI.solid(new Color(0f, 0f, 0f, 0.6f)));

        Table content = getContentTable();
        content.pad(20);
        Label title = new Label("Enter Save Name", new Label.LabelStyle(SimpleUI.font(), Color.WHITE));
        content.add(title).colspan(2).padBottom(20); content.row();
        content.add(nameField).width(300).height(40).colspan(2).padBottom(10); content.row();
        content.add(errorLabel).colspan(2).padBottom(10); content.row();
        Table buttons = new Table();
        buttons.add(cancelButton).width(200).height(50).padRight(20);
        buttons.add(confirmButton).width(200).height(50);
        content.add(buttons).colspan(2);

        setSize(500, 260);
        centerWindow();
    }

    private TextField.TextFieldStyle createTextFieldStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = SimpleUI.font();
        style.fontColor = Color.BLACK;
        style.background = SimpleUI.solid(new Color(0.95f, 0.95f, 0.95f, 1f));
        style.focusedBackground = SimpleUI.solid(new Color(0.9f, 0.9f, 0.9f, 1f));
        style.cursor = SimpleUI.solid(Color.BLACK);
        style.selection = SimpleUI.solid(new Color(0.3f, 0.6f, 1f, 0.5f));
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


