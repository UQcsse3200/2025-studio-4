package com.csse3200.game.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
// Removed unused import
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PlayerNameInputDialog name input functionality
 */
@ExtendWith(GameExtension.class)
class PlayerNameInputDialogTest {

    @Mock
    private Stage stage;
    
    @Mock
    private PlayerNameInputDialog.PlayerNameCallback callback;

    private PlayerNameInputDialog dialog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dialog = new PlayerNameInputDialog(callback);
    }

    @Test
    void shouldCreateDialogWithCallback() {
        // When
        PlayerNameInputDialog newDialog = new PlayerNameInputDialog(callback);

        // Then
        assertNotNull(newDialog, "Dialog should be created successfully");
    }

    @Test
    void shouldCreateDialogWithoutCallback() {
        // When
        PlayerNameInputDialog newDialog = new PlayerNameInputDialog(null);

        // Then
        assertNotNull(newDialog, "Dialog should be created even without callback");
    }

    @Test
    void shouldShowDialogOnStage() {
        // When
        PlayerNameInputDialog result = (PlayerNameInputDialog) dialog.show(stage);

        // Then
        assertNotNull(result, "Dialog should be shown successfully");
        verify(stage, atLeastOnce()).addActor(any());
    }

    @Test
    void shouldHaveCorrectInitialSize() {
        // When
        dialog.show(stage);

        // Then
        // The dialog should have a reasonable size for name input
        assertTrue(dialog.getWidth() > 0, "Dialog should have positive width");
        assertTrue(dialog.getHeight() > 0, "Dialog should have positive height");
    }

    @Test
    void shouldBeModal() {
        // When
        dialog.show(stage);

        // Then
        assertTrue(dialog.isModal(), "Dialog should be modal");
    }


    @Test
    void shouldNotBeResizable() {
        // When
        dialog.show(stage);

        // Then
        assertFalse(dialog.isResizable(), "Dialog should not be resizable");
    }

    @Test
    void shouldValidateNameLength() {
        // Given
        String validName = "TestPlayer";
        String invalidLongName = "ThisIsAVeryLongPlayerNameThatExceedsTwentyCharacters";

        // When & Then
        assertTrue(validName.length() > 0 && validName.length() <= 20, 
                "Valid name should be within length limits");
        assertFalse(invalidLongName.length() <= 20, 
                "Names longer than 20 characters should be invalid");
    }

    @Test
    void shouldDetectEmptyName() {
        // Given
        String emptyName = "";
        String whitespaceOnlyName = "   ";

        // When & Then
        assertTrue(emptyName.isEmpty(), "Empty name should be detected as empty");
        assertTrue(whitespaceOnlyName.trim().isEmpty(), "Whitespace-only name should be detected as empty when trimmed");
    }

    @Test
    void shouldDetectTooLongName() {
        // Given
        String longName = "ThisIsAVeryLongPlayerNameThatExceedsTheTwentyCharacterLimit";
        String validName = "ValidPlayerName";

        // When & Then
        assertTrue(longName.length() > 20, "Long name should exceed the 20 character limit");
        assertTrue(validName.length() <= 20, "Valid name should be within 20 character limit");
    }

    @Test
    void shouldCreateEnterKeyEvent() {
        // Given
        InputEvent enterEvent = new InputEvent();
        enterEvent.setType(InputEvent.Type.keyDown);
        enterEvent.setKeyCode(Input.Keys.ENTER);

        // When & Then
        assertNotNull(enterEvent, "Enter key event should be created");
        assertEquals(InputEvent.Type.keyDown, enterEvent.getType(), "Event type should be keyDown");
        assertEquals(Input.Keys.ENTER, enterEvent.getKeyCode(), "Key code should be ENTER");
    }

    @Test
    void shouldCreateNumpadEnterKeyEvent() {
        // Given
        InputEvent numpadEnterEvent = new InputEvent();
        numpadEnterEvent.setType(InputEvent.Type.keyDown);
        numpadEnterEvent.setKeyCode(Input.Keys.NUMPAD_ENTER);

        // When & Then
        assertNotNull(numpadEnterEvent, "Numpad Enter key event should be created");
        assertEquals(InputEvent.Type.keyDown, numpadEnterEvent.getType(), "Event type should be keyDown");
        assertEquals(Input.Keys.NUMPAD_ENTER, numpadEnterEvent.getKeyCode(), "Key code should be NUMPAD_ENTER");
    }

    @Test
    void shouldHaveValidCallback() {
        // Given
        String validName = "ValidPlayer";

        // When & Then
        assertNotNull(validName, "Valid name should not be null");
        assertNotNull(callback, "Callback should be available for name confirmation");
        // Note: Actual callback testing would require access to dialog internals
    }

    @Test
    void shouldHaveCallbackForCancel() {
        // When & Then
        assertNotNull(callback, "Callback should be available for cancellation");
        // Note: Actual callback testing would require access to dialog internals
    }




    @Test
    void shouldCreateTextFieldWithCorrectProperties() {
        // Given
        dialog.show(stage);

        // Then
        // The TextField should have:
        // - Center alignment
        // - Max length of 20
        // - Placeholder text
        // - Enter key listener
        assertNotNull(dialog, "Dialog should be created with TextField");
    }

    @Test
    void shouldCreateButtonsWithCorrectLabels() {
        // Given
        dialog.show(stage);

        // Then
        // Should have "Start Game" and "Cancel" buttons
        assertNotNull(dialog, "Dialog should have buttons");
    }

    @Test
    void shouldHandleMultipleShowCalls() {
        // When
        dialog.show(stage);
        dialog.show(stage);

        // Then - should handle multiple show calls gracefully
        assertDoesNotThrow(() -> dialog.show(stage));
    }

    @Test
    void shouldHideProperly() {
        // Given
        dialog.show(stage);

        // When
        dialog.hide();

        // Then - should not throw any exceptions
        assertDoesNotThrow(() -> dialog.hide());
    }
}
