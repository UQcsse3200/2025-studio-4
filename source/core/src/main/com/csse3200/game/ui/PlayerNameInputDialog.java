package com.csse3200.game.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

/**
 * Dialog for capturing player name input at the start of a new game.
 */
public class PlayerNameInputDialog extends Dialog {
    private TextField nameField;
    private TextButton confirmButton;
    private PlayerNameCallback callback;
    private InputListener escKeyListener;
    
    public interface PlayerNameCallback {
        void onNameConfirmed(String playerName);
        void onNameCancelled();
    }

    public PlayerNameInputDialog(PlayerNameCallback callback) {
        super("", SimpleUI.windowStyle());
        this.callback = callback;
        
        setModal(true);
        setMovable(false);
        setResizable(false);
        
        createContent();
        createButtons();
    }

    private void createContent() {
        // Clear default content
        getContentTable().clear();
        getTitleTable().clear();
        
        // Add custom title
        Label titleLabel = new Label("Enter Your Name", SimpleUI.largeTitle());
        getTitleTable().add(titleLabel).center().expandX().pad(20);
        
        // Create content table
        Table contentTable = getContentTable();
        contentTable.pad(30);
        
        // Add description
        Label descLabel = new Label("Please enter your name to start the game:", SimpleUI.label());
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        contentTable.add(descLabel).center().width(400).padBottom(20).row();
        
        // Create name input field
        nameField = new TextField("", createTextFieldStyle());
        nameField.setMessageText("Your name here...");
        nameField.setAlignment(Align.center);
        nameField.setMaxLength(20); // Limit name length
        
        // Add enter key listener to name field
        nameField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    confirmName();
                    return true;
                }
                return false;
            }
        });
        
        contentTable.add(nameField).center().width(300).height(40).padBottom(20).row();
        
        // Add validation hint
        Label hintLabel = new Label("Name must be 1-20 characters long", SimpleUI.muted());
        hintLabel.setAlignment(Align.center);
        contentTable.add(hintLabel).center().padBottom(10);
    }

    private void createButtons() {
        // Clear default button table
        getButtonTable().clear();
        
        Table buttonTable = getButtonTable();
        buttonTable.pad(20);
        
        // Create buttons
        confirmButton = new TextButton("Start Game", SimpleUI.primaryButton());
        TextButton cancelButton = new TextButton("Cancel", SimpleUI.darkButton());
        
        // Add button listeners
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                confirmName();
            }
        });
        
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cancelInput();
            }
        });
        
        // Add buttons to table
        buttonTable.add(cancelButton).width(120).height(40).padRight(20);
        buttonTable.add(confirmButton).width(120).height(40);
    }
    
    private TextField.TextFieldStyle createTextFieldStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = SimpleUI.font();
        style.fontColor = Color.BLACK;
        style.background = SimpleUI.roundRect(Color.WHITE, Color.GRAY, 5, 2);
        style.focusedBackground = SimpleUI.roundRect(Color.WHITE, Color.valueOf("2aa35a"), 5, 2);
        style.cursor = SimpleUI.solid(Color.BLACK);
        style.selection = SimpleUI.solid(Color.valueOf("87ceeb"));
        style.messageFont = SimpleUI.font();
        style.messageFontColor = Color.GRAY;
        return style;
    }

    private void confirmName() {
        String playerName = nameField.getText().trim();
        
        // Validate name
        if (playerName.isEmpty()) {
            // Show error - name is required
            showError("Please enter a name!");
            return;
        }
        
        if (playerName.length() > 20) {
            showError("Name is too long! Maximum 20 characters.");
            return;
        }
        
        // Name is valid, proceed
        hide();
        if (callback != null) {
            callback.onNameConfirmed(playerName);
        }
    }
    
    private void cancelInput() {
        hide();
        if (callback != null) {
            callback.onNameCancelled();
        }
    }
    
    private void showError(String message) {
        // Change name field border to red to indicate error
        nameField.getStyle().focusedBackground = SimpleUI.roundRect(Color.WHITE, Color.RED, 5, 2);
        
        // You could also add a temporary error label here
        // For now, we'll just change the field appearance
        nameField.setText("");
        nameField.setMessageText(message);
        nameField.getStyle().messageFontColor = Color.RED;
    }

    @Override
    public Dialog show(Stage stage) {
        Dialog dialog = super.show(stage);
        
        // Set dialog size and position
        float prefW = 500f;
        float prefH = 300f;
        setSize(prefW, prefH);
        setPosition(
            (stage.getWidth() - prefW) / 2f,
            (stage.getHeight() - prefH) / 2f
        );
        
        // Focus on the text field
        stage.setKeyboardFocus(nameField);
        
        // Add ESC key listener
        escKeyListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE && isVisible()) {
                    cancelInput();
                    return true;
                }
                return false;
            }
        };
        
        this.addListener(escKeyListener);
        
        return dialog;
    }

    @Override
    public void hide() {
        // Remove ESC key listener
        if (escKeyListener != null) {
            this.removeListener(escKeyListener);
            escKeyListener = null;
        }
        super.hide();
    }
}
