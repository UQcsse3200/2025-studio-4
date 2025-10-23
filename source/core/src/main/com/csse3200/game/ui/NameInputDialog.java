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
    
    public NameInputDialog(String title, Skin skin) {
        super(title, skin);
        this.playerNameService = ServiceLocator.getPlayerNameService();
        this.playerAvatarService = ServiceLocator.getPlayerAvatarService();
        this.selectedAvatarId = playerAvatarService.getPlayerAvatar();
        createContent();
    }
    
    public NameInputDialog(String title, Skin skin, NameInputCallback callback) {
        super(title, skin);
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
        
        // Create buttons using main menu orange button style
        TextButton.TextButtonStyle buttonStyle = UIStyleHelper.orangeButtonStyle();
        confirmButton = new TextButton("Confirm", buttonStyle);
        cancelButton = new TextButton("Cancel", buttonStyle);
        
        // Create error label (initially hidden) - using segoe_ui font
        Label.LabelStyle errorStyle = new Label.LabelStyle(getSkin().getFont("segoe_ui"), Color.RED);
        errorLabel = new Label("", errorStyle);
        errorLabel.setVisible(false);
        
        // Add listeners
        setupListeners();
        
        // Layout the dialog
        Table contentTable = getContentTable();
        // Add extra top padding to move all content down
        contentTable.pad(Theme.PAD * 3, Theme.PAD, Theme.PAD, Theme.PAD);
        
        // Use custom background image with scaling
        try {
            Texture bgTexture = ServiceLocator.getResourceService()
                .getAsset("images/name system background.png", Texture.class);
            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(bgTexture));
            
            // Set min size to allow the background to scale properly to fit the dialog
            // This will make the image scale down if it's larger than the dialog
            drawable.setMinWidth(0);
            drawable.setMinHeight(0);
            
            contentTable.setBackground(drawable);
        } catch (Exception e) {
            // Fallback to default background if image fails to load
            logger.warn("Failed to load name system background, using default", e);
            contentTable.setBackground(SimpleUI.solid(Theme.WINDOW_BG));
        }
        
        // Title - using segoe_ui font with black color for emphasis, centered
        Label.LabelStyle titleStyle = new Label.LabelStyle(getSkin().getFont("segoe_ui"), Color.BLACK);
        Label titleLabel = new Label("Create Your Character", titleStyle);
        contentTable.add(titleLabel).colspan(2).center().padBottom(Theme.PAD);
        contentTable.row();
        
        // Name input field - using segoe_ui font
        Label.LabelStyle labelStyle = new Label.LabelStyle(getSkin().getFont("segoe_ui"), Theme.ROW_FG);
        Label nameLabel = new Label("Name:", labelStyle);
        contentTable.add(nameLabel).left().padBottom(Theme.PAD_SM);
        contentTable.row();
        contentTable.add(nameField).width(300).height(40).colspan(2).padBottom(Theme.PAD);
        contentTable.row();
        
        // Avatar selection - using segoe_ui font
        Label avatarLabel = new Label("Choose Avatar:", labelStyle);
        contentTable.add(avatarLabel).left().padBottom(Theme.PAD_SM);
        contentTable.row();
        
        Table avatarTable = createAvatarSelectionTable();
        contentTable.add(avatarTable).colspan(2).padBottom(Theme.PAD);
        contentTable.row();
        
        // Error message
        contentTable.add(errorLabel).colspan(2).padBottom(Theme.PAD_SM);
        contentTable.row();
        
        // Buttons - using main menu button size (200x50)
        Table buttonTable = new Table();
        buttonTable.add(cancelButton).width(200).height(50).padRight(20);
        buttonTable.add(confirmButton).width(200).height(50);
        contentTable.add(buttonTable).colspan(2);
        
        // Set dialog size and position (increased width for larger buttons)
        setSize(500, 420);
        centerWindow();
    }
    
    private TextField.TextFieldStyle createTextFieldStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = getSkin().getFont("segoe_ui");  // Use segoe_ui font for consistency with main menu
        style.fontColor = Theme.ROW_FG;
        style.background = SimpleUI.solid(Theme.TABLE_BG);
        style.focusedBackground = SimpleUI.solid(Theme.ROW_HOVER_BG);
        style.cursor = SimpleUI.solid(Theme.ROW_FG);
        style.selection = SimpleUI.solid(new Color(Theme.BTN_PRIMARY_BG.r, Theme.BTN_PRIMARY_BG.g, Theme.BTN_PRIMARY_BG.b, 0.5f));
        style.messageFontColor = Theme.ROW_MUTED;
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
            // Fallback to a simple colored button if texture loading fails using Theme colors
            logger.warn("Failed to load avatar texture: {}", imagePath);
            buttonStyle.up = SimpleUI.solid(Theme.TABLE_BG);
            buttonStyle.down = SimpleUI.solid(Theme.ROW_HOVER_BG);
            buttonStyle.checked = SimpleUI.solid(Theme.BTN_PRIMARY_BG); // Green background for selected state
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
     * Draws a green border around the specified area using Theme colors
     */
    private void drawRedBorder(Batch batch, float x, float y, float width, float height) {
        float borderWidth = 3f; // Border thickness
        Color borderColor = Theme.BTN_PRIMARY_BG; // Use primary green color from Theme
        
        // Create a simple border by drawing rectangles
        Drawable borderDrawable = SimpleUI.solid(borderColor);
        
        // Draw top border
        borderDrawable.draw(batch, x, y + height - borderWidth, width, borderWidth);
        // Draw bottom border  
        borderDrawable.draw(batch, x, y, width, borderWidth);
        // Draw left border
        borderDrawable.draw(batch, x, y, borderWidth, height);
        // Draw right border
        borderDrawable.draw(batch, x + width - borderWidth, y, borderWidth, height);
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
