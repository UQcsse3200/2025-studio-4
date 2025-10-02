package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.PlayerNameService;
import com.csse3200.game.services.PlayerAvatarService;
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
    private PlayerAvatarService playerAvatarService;
    
    // Avatar selection components
    private ButtonGroup<ImageButton> avatarButtonGroup;
    private String selectedAvatarId;
    
    // Callback interface for name input events
    public interface NameInputCallback {
        void onNameConfirmed(String name, String avatarId);
        void onNameCancelled();
    }
    
    private NameInputCallback callback;
    
    public NameInputDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        this.playerNameService = ServiceLocator.getPlayerNameService();
        this.playerAvatarService = ServiceLocator.getPlayerAvatarService();
        this.selectedAvatarId = playerAvatarService.getPlayerAvatar();
        createContent();
    }
    
    public NameInputDialog(String title, WindowStyle windowStyle, NameInputCallback callback) {
        super(title, windowStyle);
        this.playerNameService = ServiceLocator.getPlayerNameService();
        this.playerAvatarService = ServiceLocator.getPlayerAvatarService();
        this.callback = callback;
        this.selectedAvatarId = playerAvatarService.getPlayerAvatar();
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
        Label titleLabel = new Label("Create Your Character", SimpleUI.title());
        contentTable.add(titleLabel).colspan(2).padBottom(20);
        contentTable.row();
        
        // Name input field
        Label nameLabel = new Label("Name:", SimpleUI.label());
        contentTable.add(nameLabel).left().padBottom(5);
        contentTable.row();
        contentTable.add(nameField).width(300).height(40).colspan(2).padBottom(15);
        contentTable.row();
        
        // Avatar selection
        Label avatarLabel = new Label("Choose Avatar:", SimpleUI.label());
        contentTable.add(avatarLabel).left().padBottom(10);
        contentTable.row();
        
        Table avatarTable = createAvatarSelectionTable();
        contentTable.add(avatarTable).colspan(2).padBottom(15);
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
        setSize(450, 350);
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
    
    private Table createAvatarSelectionTable() {
        Table avatarTable = new Table();
        avatarButtonGroup = new ButtonGroup<>();
        avatarButtonGroup.setMaxCheckCount(1);
        avatarButtonGroup.setMinCheckCount(1);
        
        String[] availableAvatars = playerAvatarService.getAvailableAvatars();
        
        for (int i = 0; i < availableAvatars.length; i++) {
            String avatarId = availableAvatars[i];
            String imagePath = playerAvatarService.getAvatarImagePath(avatarId);
            String displayName = playerAvatarService.getAvatarDisplayName(avatarId);
            
            // Create avatar button
            ImageButton avatarButton = createAvatarButton(avatarId, imagePath, displayName);
            avatarButtonGroup.add(avatarButton);
            
            // Set default selection
            if (avatarId.equals(selectedAvatarId)) {
                avatarButton.setChecked(true);
            }
            
            // Add to table (2 avatars per row)
            avatarTable.add(avatarButton).size(80, 80).pad(5);
            if ((i + 1) % 2 == 0) {
                avatarTable.row();
            }
        }
        
        return avatarTable;
    }
    
    private ImageButton createAvatarButton(String avatarId, String imagePath, String displayName) {
        // Create button style
        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        
        try {
            // Load texture
            Texture texture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
            TextureRegion region = new TextureRegion(texture);
            TextureRegionDrawable drawable = new TextureRegionDrawable(region);
            
            buttonStyle.up = drawable;
            buttonStyle.down = drawable;
            // Create red border for selected state
            buttonStyle.checked = createRedBorderDrawable(drawable);
            
        } catch (Exception e) {
            // Fallback to a simple colored button if texture loading fails
            logger.warn("Failed to load avatar texture: {}", imagePath);
            buttonStyle.up = SimpleUI.solid(new Color(0.8f, 0.8f, 0.8f, 1f));
            buttonStyle.down = SimpleUI.solid(new Color(0.7f, 0.7f, 0.7f, 1f));
            buttonStyle.checked = SimpleUI.solid(new Color(1f, 0.2f, 0.2f, 0.8f)); // Red background for fallback
        }
        
        ImageButton button = new ImageButton(buttonStyle);
        
        // Add click listener
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedAvatarId = avatarId;
                logger.debug("Avatar selected: {} ({})", displayName, avatarId);
            }
        });
        
        return button;
    }

    /**
     * Creates a drawable with red border overlay for selected avatar
     */
    private Drawable createRedBorderDrawable(TextureRegionDrawable originalDrawable) {
        return new TextureRegionDrawable(originalDrawable.getRegion()) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                // Draw original image
                super.draw(batch, x, y, width, height);
                // Draw red border
                drawRedBorder(batch, x, y, width, height);
            }
        };
    }
    
    /**
     * Draws a red border around the specified area
     */
    private void drawRedBorder(Batch batch, float x, float y, float width, float height) {
        float borderWidth = 3f; // Border thickness
        Color borderColor = new Color(1f, 0f, 0f, 1f); // Red color
        
        // Create a simple red border by drawing rectangles
        Drawable redDrawable = SimpleUI.solid(borderColor);
        
        // Draw top border
        redDrawable.draw(batch, x, y + height - borderWidth, width, borderWidth);
        // Draw bottom border  
        redDrawable.draw(batch, x, y, width, borderWidth);
        // Draw left border
        redDrawable.draw(batch, x, y, borderWidth, height);
        // Draw right border
        redDrawable.draw(batch, x + width - borderWidth, y, borderWidth, height);
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
        
        // Name is valid, save it along with avatar
        playerNameService.setPlayerName(name);
        playerAvatarService.setPlayerAvatar(selectedAvatarId);
        logger.info("Player name confirmed: {}, Avatar: {}", name, selectedAvatarId);
        hide();
        
        // Call callback if provided
        if (callback != null) {
            callback.onNameConfirmed(name, selectedAvatarId);
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
