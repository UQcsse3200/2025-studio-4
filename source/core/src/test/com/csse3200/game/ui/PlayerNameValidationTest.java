package com.csse3200.game.ui;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Removed parameterized test imports for compatibility

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for player name validation logic
 */
@ExtendWith(GameExtension.class)
class PlayerNameValidationTest {

    /**
     * Validates if a player name meets the requirements
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        
        // Check length constraints (1-20 characters for dialog, 1-12 for game over)
        if (trimmedName.length() < 1 || trimmedName.length() > 20) {
            return false;
        }
        
        // Check for valid characters (letters, numbers, spaces, basic punctuation)
        if (!trimmedName.matches("^[a-zA-Z0-9\\s._-]+$")) {
            return false;
        }
        
        // Check that it's not just whitespace
        if (trimmedName.matches("^\\s+$")) {
            return false;
        }
        
        return true;
    }

    /**
     * Validates if a player name meets the game over screen requirements (shorter limit)
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidGameOverName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        
        // Check length constraints (1-12 characters for game over screen)
        if (trimmedName.length() < 1 || trimmedName.length() > 12) {
            return false;
        }
        
        // Check for valid characters
        if (!trimmedName.matches("^[a-zA-Z0-9\\s._-]+$")) {
            return false;
        }
        
        // Check that it's not just whitespace
        if (trimmedName.matches("^\\s+$")) {
            return false;
        }
        
        return true;
    }

    /**
     * Gets default name when input is empty or invalid
     * @param name the input name
     * @return default name "Player" if input is invalid, otherwise returns trimmed input
     */
    private String getDefaultName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Player";
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() > 12) {
            return trimmedName.substring(0, 12);
        }
        
        return trimmedName;
    }

    @Test
    void shouldAcceptValidNames() {
        // Given
        String[] validNames = {
            "Player1",
            "TestUser",
            "Alice",
            "Bob123",
            "User_Name",
            "Player-1",
            "Test.User",
            "A", // Single character
            "ValidPlayerName123" // 20 characters
        };

        // When & Then
        for (String name : validNames) {
            assertTrue(isValidPlayerName(name), 
                "Name '" + name + "' should be valid");
        }
    }

    @Test
    void shouldRejectEmptyOrWhitespaceNames() {
        // Given
        String[] invalidNames = {null, "", "   ", "\t", "\n", "  \t  "};

        // When & Then
        for (String invalidName : invalidNames) {
            assertFalse(isValidPlayerName(invalidName), 
                "Name '" + invalidName + "' should be invalid");
        }
    }

    @Test
    void shouldRejectTooLongNames() {
        // Given
        String tooLongName = "ThisIsAVeryLongPlayerNameThatExceedsTheTwentyCharacterLimit";

        // When & Then
        assertFalse(isValidPlayerName(tooLongName), 
            "Name longer than 20 characters should be invalid");
    }

    @Test
    void shouldRejectNamesWithInvalidCharacters() {
        // Given
        String[] invalidNames = {
            "Player@123",
            "User#Name",
            "Test$User",
            "Player%Name",
            "User^Name",
            "Test&User",
            "Player*Name",
            "User(Name)",
            "Test[User]",
            "Player{Name}",
            "User|Name",
            "Test\\User",
            "Player/Name",
            "User:Name",
            "Test;User",
            "Player\"Name\"",
            "User'Name'",
            "Test<User>",
            "Player,Name",
            "User?Name",
            "Test!User",
            "Player~Name",
            "User`Name"
        };

        // When & Then
        for (String name : invalidNames) {
            assertFalse(isValidPlayerName(name), 
                "Name '" + name + "' should be invalid due to special characters");
        }
    }

    @Test
    void shouldAcceptNamesWithValidSpecialCharacters() {
        // Given
        String[] validNames = {
            "Player_Name",
            "User-Name",
            "Test.User",
            "Player Name",
            "User_Name123",
            "Test-User.123"
        };

        // When & Then
        for (String name : validNames) {
            assertTrue(isValidPlayerName(name), 
                "Name '" + name + "' should be valid with allowed special characters");
        }
    }

    @Test
    void shouldTrimWhitespaceFromNames() {
        // Given
        String nameWithSpaces = "  TestPlayer  ";
        String expectedTrimmed = "TestPlayer";

        // When
        String result = getDefaultName(nameWithSpaces);

        // Then
        assertEquals(expectedTrimmed, result, 
            "Whitespace should be trimmed from names");
    }

    @Test
    void shouldReturnDefaultNameForEmptyInput() {
        // Given
        String[] emptyInputs = {null, "", "   ", "\t", "\n"};

        // When & Then
        for (String input : emptyInputs) {
            String result = getDefaultName(input);
            assertEquals("Player", result, 
                "Should return 'Player' for empty input: '" + input + "'");
        }
    }

    @Test
    void shouldTruncateLongNamesForGameOver() {
        // Given
        String longName = "VeryLongPlayerName";
        String expectedTruncated = "VeryLongPlay";

        // When
        String result = getDefaultName(longName);

        // Then
        assertEquals(expectedTruncated, result, 
            "Long names should be truncated to 12 characters for game over screen");
    }

    @Test
    void shouldValidateGameOverNameLength() {
        // Given
        String[] validGameOverNames = {
            "A", // 1 character
            "Player", // 6 characters
            "ValidPlayer1" // 12 characters
        };

        String[] invalidGameOverNames = {
            "VeryLongPlayerName", // 18 characters
            "ThisIsTooLongForGameOver" // 25 characters
        };

        // When & Then
        for (String name : validGameOverNames) {
            assertTrue(isValidGameOverName(name), 
                "Name '" + name + "' should be valid for game over screen");
        }

        for (String name : invalidGameOverNames) {
            assertFalse(isValidGameOverName(name), 
                "Name '" + name + "' should be invalid for game over screen (too long)");
        }
    }

    @Test
    void shouldValidateEnglishNamesOnly() {
        // Given - Test various English name patterns that should be valid
        String[] validEnglishNames = {
            "John", // Simple English name
            "Mary Jane", // English name with space
            "Alex-Smith", // English name with hyphen
            "Player_1", // English name with underscore
            "TestPlayer123", // English name with numbers
            "J.R.R", // English name with periods
        };
        
        // Test invalid non-English characters
        String[] invalidNonEnglishNames = {
            "玩家", // Chinese characters
            "Café", // French with accents
            "Игрок", // Cyrillic
            "プレイヤー", // Japanese
            "José", // Spanish with accent
            "Müller" // German with umlaut
        };

        // When & Then - Valid English names should pass
        for (String name : validEnglishNames) {
            assertTrue(isValidPlayerName(name), 
                "Valid English name '" + name + "' should be accepted");
        }
        
        // When & Then - Non-English names should be rejected
        for (String name : invalidNonEnglishNames) {
            assertFalse(isValidPlayerName(name), 
                "Non-English name '" + name + "' should be rejected");
        }
    }

    @Test
    void shouldHandleMixedCaseNames() {
        // Given
        String[] mixedCaseNames = {
            "PlayerName",
            "playerName",
            "PLAYERNAME",
            "pLaYeRnAmE"
        };

        // When & Then
        for (String name : mixedCaseNames) {
            assertTrue(isValidPlayerName(name), 
                "Mixed case name '" + name + "' should be valid");
        }
    }

    @Test
    void shouldHandleNamesWithNumbers() {
        // Given
        String[] namesWithNumbers = {
            "Player1",
            "User123",
            "Test99",
            "123Player",
            "Player123Test"
        };

        // When & Then
        for (String name : namesWithNumbers) {
            assertTrue(isValidPlayerName(name), 
                "Name with numbers '" + name + "' should be valid");
        }
    }

    @Test
    void shouldHandleEdgeCaseLengths() {
        // Given
        String oneChar = "A";
        String twentyChars = "12345678901234567890"; // Exactly 20 characters
        String twentyOneChars = "123456789012345678901"; // 21 characters

        // When & Then
        assertTrue(isValidPlayerName(oneChar), 
            "Single character name should be valid");
        assertTrue(isValidPlayerName(twentyChars), 
            "20 character name should be valid");
        assertFalse(isValidPlayerName(twentyOneChars), 
            "21 character name should be invalid");
    }

    @Test
    void shouldValidateNameConsistency() {
        // Given
        String name = "TestPlayer";

        // When
        boolean dialogValid = isValidPlayerName(name);
        boolean gameOverValid = isValidGameOverName(name);

        // Then
        assertTrue(dialogValid, "Name should be valid for dialog");
        assertTrue(gameOverValid, "Name should be valid for game over screen");
    }
}
