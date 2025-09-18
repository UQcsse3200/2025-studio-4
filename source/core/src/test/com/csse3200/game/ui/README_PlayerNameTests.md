# Player Name Input Functionality Tests

This directory contains comprehensive test coverage for the player name input functionality implemented in the game.

## Test Files Overview

### 1. MainGameOverTest.java
**Location**: `source/core/src/test/com/csse3200/game/screens/MainGameOverTest.java`

**Purpose**: Tests the name input functionality in the game over screen.

**Key Test Areas**:
- UI component creation and initialization
- Button creation (Restart, Main Menu)
- Background image loading
- Service integration (EntityService, LeaderboardService)
- Error handling and graceful degradation
- Z-index and layout management

**Tested Components**:
- `MainGameOver` class from `com.csse3200.game.components.maingame`

### 2. PlayerNameInputDialogTest.java
**Location**: `source/core/src/test/com/csse3200/game/ui/PlayerNameInputDialogTest.java`

**Purpose**: Tests the dialog-based player name input system.

**Key Test Areas**:
- Dialog creation and configuration
- TextField setup and properties
- Button creation and event handling
- Modal dialog behavior
- ESC key handling
- Callback mechanism
- Input validation
- Dialog lifecycle (show/hide)

**Tested Components**:
- `PlayerNameInputDialog` class from `com.csse3200.game.ui`

### 3. PlayerNameValidationTest.java
**Location**: `source/core/src/test/com/csse3200/game/ui/PlayerNameValidationTest.java`

**Purpose**: Tests the name validation logic and business rules.

**Key Test Areas**:
- Valid name acceptance (letters, numbers, allowed special characters)
- Invalid name rejection (empty, too long, invalid characters)
- Length constraints (1-20 for dialog, 1-12 for game over)
- Default name handling ("Player" fallback)
- Unicode character handling
- Mixed case and number handling
- Edge case validation

**Validation Rules Tested**:
- Minimum length: 1 character
- Maximum length: 20 characters (dialog), 12 characters (game over)
- Allowed characters: `[a-zA-Z0-9\s._-]`
- Default name: "Player" for empty input

### 4. PlayerNameScoreIntegrationTest.java
**Location**: `source/core/src/test/com/csse3200/game/screens/PlayerNameScoreIntegrationTest.java`

**Purpose**: Tests the integration between name input and score saving functionality.

**Key Test Areas**:
- Service integration (LeaderboardService, SaveGameService)
- Name and score consistency
- Error handling for missing services
- Multiple submission handling
- Concurrent input handling
- Complete integration flow validation

**Integration Points**:
- EntityService for player data
- LeaderboardService for score storage
- SaveGameService for persistence

### 5. PlayerNameTestSuite.java
**Location**: `source/core/src/test/com/csse3200/game/ui/PlayerNameTestSuite.java`

**Purpose**: Summary test suite documenting all player name tests.

## Test Coverage Summary

### Functional Areas Covered:
1. **Name Input Field Creation**
   - TextField configuration
   - Placeholder text
   - Length limits
   - Character restrictions

2. **Name Validation**
   - Empty name handling
   - Length validation
   - Character validation
   - Default name fallback

3. **UI Integration**
   - Dialog creation and display
   - Button event handling
   - ESC key support
   - Modal behavior

4. **Service Integration**
   - LeaderboardService integration
   - SaveGameService integration
   - Error handling for missing services

5. **Edge Cases**
   - Null input handling
   - Service failures
   - Concurrent operations
   - Unicode characters

### Test Statistics:
- **Total Test Files**: 5
- **Total Test Methods**: ~50+
- **Coverage Areas**: 9 major functional areas
- **Mock Objects**: EntityService, LeaderboardService, SaveGameService, Entity, Stage, etc.

## Running the Tests

### Prerequisites:
- JUnit 5
- Mockito
- GameExtension for libGDX mocking

### Command Line:
```bash
# Run all player name tests
./gradlew test --tests "*PlayerName*"

# Run specific test file
./gradlew test --tests "com.csse3200.game.ui.PlayerNameValidationTest"

# Run with coverage
./gradlew test jacocoTestReport
```

### IDE:
- Right-click on test files and select "Run Tests"
- Use IDE test runner with JUnit 5 support

## Test Dependencies

### Required Services:
- `EntityService` - For player entity management
- `LeaderboardService` - For score storage
- `SaveGameService` - For data persistence
- `ResourceService` - For asset loading

### Mock Objects:
- `Entity` - Player entity mock
- `Stage` - UI stage mock
- `SpriteBatch` - Rendering mock
- Various service mocks

## Notes

### Limitations:
- Some tests use mocks due to libGDX dependencies
- Parameterized tests removed for compatibility
- Some integration tests are conceptual due to service complexity

### Future Improvements:
- Add more parameterized tests
- Increase integration test coverage
- Add performance tests
- Add accessibility tests

## Related Implementation Files

### Main Implementation:
- `MainGameOver.java` - Game over screen with name input
- `PlayerNameInputDialog.java` - Dialog-based name input
- `SimpleUI.java` - UI styling and components

### Supporting Classes:
- `PlayerRank.java` - Player ranking data model
- `MockRanks.java` - Test data generation
- `RankingDialog.java` - Leaderboard display

This test suite provides comprehensive coverage of the player name input functionality, ensuring reliability and maintainability of the feature.
