package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog for entering the player's name.
 * Provides a text input field with validation and confirmation buttons.
 */
public class NameInputDialog extends Dialog {
    private static final Logger logger = LoggerFactory.getLogger(NameInputDialog.class);
    
    private TextField nameField;
    private TextButton confirmButton;
    private TextButton cancelButton;
    private Label errorLabel;
    private PlayerNameService playerNameService;
    
    // Callback interface for name input events
    public interface NameInputCallback {
        void onNameConfirmed(String name);
        void onNameCancelled();
    }
    
    private NameInputCallback callback;
    
    public NameInputDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        this.playerNameService = ServiceLocator.getPlayerNameService();
        createContent();
    }
    
    public NameInputDialog(String title, WindowStyle windowStyle, NameInputCallback callback) {
        super(title, windowStyle);
        this.playerNameService = ServiceLocator.getPlayerNameService();
        this.callback = callback;
        createContent();
    }
    
    private void createContent() {
        // Create text field for name input
        TextField.TextFieldStyle fieldStyle = createTextFieldStyle();
        nameField = new TextField("", fieldStyle);
        nameField.setMessageText("Enter your name...");
        nameField.setMaxLength(20); // Limit name length
        
        // Create buttons
        TextButton.TextButtonStyle buttonStyle = SimpleUI.buttonStyle();
        confirmButton = new TextButton("Confirm", buttonStyle);
        cancelButton = new TextButton("Cancel", buttonStyle);
        
        // Create error label (initially hidden)
        Label.LabelStyle errorStyle = new Label.LabelStyle(SimpleUI.font(), Color.RED);
        errorLabel = new Label("", errorStyle);
        errorLabel.setVisible(false);
        
        // Add listeners
        setupListeners();
        
        // Layout the dialog
        Table contentTable = getContentTable();
        contentTable.pad(20);
        
        // Title
        Label titleLabel = new Label("Enter Your Name", SimpleUI.title());
        contentTable.add(titleLabel).colspan(2).padBottom(20);
        contentTable.row();
        
        // Name input field
        contentTable.add(nameField).width(300).height(40).colspan(2).padBottom(10);
        contentTable.row();
        
        // Error message
        contentTable.add(errorLabel).colspan(2).padBottom(10);
        contentTable.row();
        
        // Buttons
        Table buttonTable = new Table();
        buttonTable.add(cancelButton).width(120).height(40).padRight(10);
        buttonTable.add(confirmButton).width(120).height(40);
        contentTable.add(buttonTable).colspan(2);
        
        // Set dialog size and position
        setSize(400, 200);
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
    
    private void setupListeners() {
        // Confirm button
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmName();
            }
        });
        
        // Cancel button
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cancelNameInput();
            }
        });
        
        // Enter key to confirm
        nameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                    confirmName();
                }
            }
        });
    }
    
    private void confirmName() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            showError("Please enter a name");
            return;
        }
        
        if (name.length() < 2) {
            showError("Name must be at least 2 characters");
            return;
        }
        
        if (name.length() > 20) {
            showError("Name must be less than 20 characters");
            return;
        }
        
        // Check for invalid characters
        if (!name.matches("^[a-zA-Z0-9\\s]+$")) {
            showError("Name can only contain letters, numbers, and spaces");
            return;
        }
        
        // Name is valid, save it
        playerNameService.setPlayerName(name);
        logger.info("Player name confirmed: {}", name);
        hide();
        
        // Call callback if provided
        if (callback != null) {
            callback.onNameConfirmed(name);
        }
    }
    
    private void cancelNameInput() {
        logger.debug("Name input cancelled");
        hide();
        
        // Call callback if provided
        if (callback != null) {
            callback.onNameCancelled();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        logger.debug("Name input error: {}", message);
    }
    
    private void centerWindow() {
        Stage stage = getStage();
        if (stage != null) {
            setPosition(
                (stage.getWidth() - getWidth()) / 2,
                (stage.getHeight() - getHeight()) / 2
            );
        }
    }
    
    @Override
    public Dialog show(Stage stage) {
        super.show(stage);
        centerWindow();
        // Focus the text field
        stage.setKeyboardFocus(nameField);
        nameField.setCursorPosition(nameField.getText().length());
        return this;
    }
}
