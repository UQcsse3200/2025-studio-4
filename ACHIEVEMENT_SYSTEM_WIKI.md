# Achievement System Wiki

## 📋 Overview

The Achievement System is a comprehensive game feature that tracks and rewards player accomplishments. The system consists of:

- **Achievement Button** on the main menu for viewing achievements
- **Achievement Display in Ranking Popup** showing unlocked/locked status
- **Automatic Unlock System** triggered when players meet specific conditions
- **Visual Feedback** with color-coded achievement icons (gray for locked, golden for unlocked)

### Key Features

1. **5 Unique Achievements**:
   - 🏆 **Tough Survivor**: Complete any wave in the game
   - ⚡ **Speed Runner**: Defeat 5 enemies in a single game
   - ⚔️ **Slayer**: Defeat 20 enemies in a single game
   - 🎯 **Perfect Clear**: Win the game
   - 🎮 **Participation**: Play your first game

2. **Persistent Achievement Tracking**: Achievements remain unlocked across game sessions
3. **Interactive UI**: Click achievements to view unlock conditions
4. **Visual States**: Locked (grayscale) vs Unlocked (golden tint)

---

## 🏗️ Architecture

### System Components

```
┌─────────────────────────────────────────────────────┐
│                   GdxGame (Main)                     │
│  - Initializes global AchievementService             │
└──────────────────────┬──────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
         ▼                           ▼
┌──────────────────┐      ┌──────────────────────┐
│  MainMenuScreen  │      │   MainGameScreen      │
│  - Shows UI      │      │  - Unlocks on victory │
└────────┬─────────┘      └──────────┬───────────┘
         │                           │
         ▼                           ▼
┌──────────────────┐      ┌──────────────────────┐
│ Achievement      │      │  MainGameActions     │
│ Dialog           │      │  - Unlock logic      │
└────────┬─────────┘      └──────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│         LeaderboardPopup (Ranking)                │
│  - Displays achievement icons with status         │
└───────────────────────────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────────────────┐
│           ServiceLocator                          │
│  - Global AchievementService instance             │
└───────────────────────────────────────────────────┘
```

---

## 🔑 Key Code Components

### 1. AchievementService.java

**Location**: `source/core/src/main/com/csse3200/game/services/AchievementService.java`

**Purpose**: Core service managing achievement state

```java
public class AchievementService {
    // Achievement IDs
    public static final String TOUGH_SURVIVOR = "tough_survivor";
    public static final String SPEED_RUNNER = "speed_runner";
    public static final String SLAYER = "slayer";
    public static final String PERFECT_CLEAR = "perfect_clear";
    public static final String PARTICIPATION = "participation";
    
    private final Map<String, Boolean> unlockedAchievements;
    
    public void unlockAchievement(String achievementId) {
        if (unlockedAchievements.containsKey(achievementId)) {
            if (!unlockedAchievements.get(achievementId)) {
                unlockedAchievements.put(achievementId, true);
                logger.info("Achievement unlocked: {}", achievementId);
            }
        }
    }
    
    public boolean isUnlocked(String achievementId) {
        return unlockedAchievements.getOrDefault(achievementId, false);
    }
}
```

**Key Methods**:
- `unlockAchievement(String id)`: Unlocks a specific achievement
- `isUnlocked(String id)`: Checks if achievement is unlocked
- `getUnlockedCount()`: Returns number of unlocked achievements
- `resetAchievements()`: Resets all achievements to locked state

---

### 2. ServiceLocator Integration

**Location**: `source/core/src/main/com/csse3200/game/services/ServiceLocator.java`

**Purpose**: Provides global access to AchievementService

```java
// Registration (in GdxGame.java)
ServiceLocator.registerAchievementService(new AchievementService());

// Access anywhere
AchievementService achievementService = ServiceLocator.getAchievementService();
```

**Critical**: Service is registered **once** in `GdxGame.create()` to ensure persistence

---

### 3. Achievement Unlock Logic

**Location**: `source/core/src/main/com/csse3200/game/components/maingame/MainGameActions.java`

**Purpose**: Automatically unlocks achievements on game victory

```java
private void unlockAchievementsOnVictory() {
    AchievementService achievementService = ServiceLocator.getAchievementService();
    
    if (achievementService != null) {
        // Perfect Clear - Win the game
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        
        // Enemy-based achievements
        int enemiesDefeated = ForestGameArea.NUM_ENEMIES_DEFEATED;
        
        if (enemiesDefeated >= 5) {
            achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        }
        
        if (enemiesDefeated >= 20) {
            achievementService.unlockAchievement(AchievementService.SLAYER);
        }
        
        // Tough Survivor & Participation
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
    }
}
```

**Unlock Conditions**:
- Called in `onVictory()` method when player wins
- Checks enemy defeat count for conditional achievements
- Always unlocks base achievements (Perfect Clear, Tough Survivor, Participation)

---

### 4. Achievement Dialog

**Location**: `source/core/src/main/com/csse3200/game/ui/AchievementDialog.java`

**Purpose**: Displays achievement details when clicked from main menu

```java
private static final String[] ACHIEVEMENT_IMAGES = {
    "images/tough survivor.jpg",
    "images/speed runner.jpg",
    "images/slayer.jpg",
    "images/perfect clear.jpg",
    "images/participation.jpg"
};

private static final String[] ACHIEVEMENT_CONDITIONS = {
    "Complete any wave in the game",
    "Defeat 5 enemies in a single game",
    "Defeat 20 enemies in a single game",
    "Win the game",
    "Play your first game!"
};
```

**Features**:
- Grid layout displaying 5 achievement cards
- Click on achievement to view unlock condition
- Scrollable interface for easy navigation

---

### 5. Ranking Popup Integration

**Location**: `source/core/src/main/com/csse3200/game/ui/leaderboard/LeaderboardPopup.java`

**Purpose**: Shows achievement status in ranking screen

```java
private void createAchievementSection() {
    AchievementService achievementService = ServiceLocator.getAchievementService();
    
    for (int i = 0; i < achievementIds.length; i++) {
        Image achievementIcon = createAchievementIcon(
            achievementImages[i], 
            achievementService.isUnlocked(achievementIds[i])
        );
        achievementTable.add(achievementIcon).size(80, 80).pad(5);
    }
}

private Image createAchievementIcon(String imagePath, boolean unlocked) {
    Image image = new Image(texture);
    
    if (!unlocked) {
        // Locked: grayscale with reduced opacity
        image.setColor(0.5f, 0.5f, 0.5f, 0.6f);
    } else {
        // Unlocked: golden tint
        image.setColor(1.2f, 1.1f, 0.8f, 1f);
    }
    
    return image;
}
```

**Visual States**:
- **Locked**: Gray (RGB: 0.5, 0.5, 0.5, Alpha: 0.6)
- **Unlocked**: Golden (RGB: 1.2, 1.1, 0.8, Alpha: 1.0)

---

## 📊 User Flow Diagrams

### Flow 1: Viewing Achievements from Main Menu

```
┌─────────────────┐
│  Main Menu      │
│                 │
│  [Achievement]  │ ◄─── User clicks button
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│  Achievement Dialog             │
│  ┌───────┐ ┌───────┐ ┌───────┐ │
│  │ Icon1 │ │ Icon2 │ │ Icon3 │ │
│  └───────┘ └───────┘ └───────┘ │
│  ┌───────┐ ┌───────┐           │
│  │ Icon4 │ │ Icon5 │           │
│  └───────┘ └───────┘           │
└────────┬────────────────────────┘
         │
         ▼
    User clicks icon
         │
         ▼
┌─────────────────────────────────┐
│  Condition Popup                │
│                                 │
│  Achievement: Speed Runner      │
│  Condition: Defeat 5 enemies    │
│                                 │
│          [OK]                   │
└─────────────────────────────────┘
```

### Flow 2: Achievement Unlock Process

```
┌──────────────────┐
│  Game in Progress│
│  - Player fights │
│  - Enemies spawn │
└────────┬─────────┘
         │
         ▼
    Player Wins!
         │
         ▼
┌──────────────────────────────────┐
│  MainGameActions.onVictory()     │
│  1. Calculate enemy defeats      │
│  2. Check unlock conditions      │
│  3. Call unlockAchievement()     │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  AchievementService              │
│  - Updates achievement map       │
│  - Logs unlock event             │
│  - State persists globally       │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Victory Screen                  │
│  Player returns to main menu     │
└────────┬─────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│  Main Menu → Ranking             │
│  Achievement icons show UNLOCKED │
│  (Golden color)                  │
└──────────────────────────────────┘
```

### Flow 3: Achievement Display in Ranking

```
┌──────────────────┐
│  Main Menu       │
│                  │
│  [Ranking] ◄──── User clicks
└────────┬─────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Leaderboard Popup                      │
│  ┌─────────────────────────────────┐   │
│  │  Player Rankings                │   │
│  │  1. Player A - 5000             │   │
│  │  2. Player B - 4500             │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  Achievements                   │   │
│  │  🏆   ⚡   ⚔️   🎯   🎮        │   │
│  │  Gold Gray Gray Gold Gold       │   │
│  └─────────────────────────────────┘   │
│                                         │
│         [Close]                         │
└─────────────────────────────────────────┘
```

---

## 🗂️ File Structure

```
source/core/src/main/com/csse3200/game/
│
├── services/
│   ├── ServiceLocator.java           (Modified: Added AchievementService)
│   └── AchievementService.java       (NEW: Core achievement logic)
│
├── components/
│   └── maingame/
│       └── MainGameActions.java      (Modified: Added unlock logic)
│
├── ui/
│   ├── AchievementDialog.java        (NEW: Achievement viewer)
│   └── leaderboard/
│       └── LeaderboardPopup.java     (Modified: Added achievement section)
│
├── screens/
│   └── MainMenuScreen.java           (Modified: Register TimeSource & AchievementService)
│
├── components/
│   └── mainmenu/
│       ├── MainMenuDisplay.java      (Modified: Added Achievement button)
│       └── MainMenuActions.java      (Modified: Added onAchievement handler)
│
└── GdxGame.java                      (Modified: Global AchievementService registration)
```

---

## 🎨 Asset Files

**Location**: `source/core/assets/images/`

- `tough survivor.jpg` - Tough Survivor achievement icon
- `speed runner.jpg` - Speed Runner achievement icon
- `slayer.jpg` - Slayer achievement icon
- `perfect clear.jpg` - Perfect Clear achievement icon
- `participation.jpg` - Participation achievement icon

**Image Specifications**:
- Format: JPG
- Recommended size: 256x256 pixels
- Color: Full color (grayscale/golden tint applied programmatically)

---

## 🔄 Data Flow

```
Game Start
    │
    ▼
┌─────────────────────────────────────┐
│ GdxGame.create()                    │
│ - Register AchievementService       │
│ - Initialize with all locked        │
└────────────┬────────────────────────┘
             │
             ▼
    Game Play & Victory
             │
             ▼
┌─────────────────────────────────────┐
│ MainGameActions.onVictory()         │
│ - Check conditions                  │
│ - Unlock achievements               │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│ AchievementService                  │
│ - Update internal map               │
│ - Achievement state persists        │
└────────────┬────────────────────────┘
             │
             ▼
    Return to Main Menu
             │
             ▼
┌─────────────────────────────────────┐
│ User clicks Ranking/Achievement     │
│ - Query AchievementService          │
│ - Display unlocked status           │
└─────────────────────────────────────┘
```

---

## 🧪 Testing Scenarios

### Test 1: First-Time Player
1. Start new game
2. Win the game (defeat < 5 enemies)
3. Return to main menu → Click Ranking
4. **Expected**: Participation, Perfect Clear, Tough Survivor unlocked (golden)

### Test 2: Experienced Player
1. Start new game
2. Defeat 20+ enemies and win
3. Return to main menu → Click Ranking
4. **Expected**: All 5 achievements unlocked (golden)

### Test 3: Achievement Dialog
1. Main menu → Click Achievement button
2. Click any achievement icon
3. **Expected**: Popup shows achievement name and unlock condition

### Test 4: Persistence Check
1. Win game (unlock achievements)
2. Return to main menu
3. Click Ranking → Verify achievements are golden
4. Start new game → Quit to menu
5. Click Ranking again
6. **Expected**: Previously unlocked achievements remain golden

---

## 🐛 Known Issues & Fixes

### Issue 1: Achievements Reset on Menu Return
**Problem**: AchievementService was recreated in MainMenuScreen  
**Fix**: Moved registration to GdxGame.create() and added null check in MainMenuScreen

### Issue 2: Ranking Button No Response
**Problem**: TimeSource service not registered in MainMenuScreen  
**Fix**: Added ServiceLocator.registerTimeSource() in MainMenuScreen initialization

---

## 🚀 Future Enhancements

1. **Save to Disk**: Persist achievements between game sessions
2. **More Achievements**: Add achievements for specific tower types, wave completion times, etc.
3. **Achievement Notifications**: Show toast/popup when achievement is unlocked
4. **Progress Tracking**: Display progress bars for achievements in progress (e.g., "Defeated 15/20 enemies")
5. **Rewards**: Give currency/stars when achievements are unlocked
6. **Steam Integration**: Sync with Steam achievements (if deployed on Steam)

---

## 📝 Code Maintenance

### Adding New Achievements

1. **Define ID in AchievementService**:
```java
public static final String NEW_ACHIEVEMENT = "new_achievement";
```

2. **Initialize in constructor**:
```java
unlockedAchievements.put(NEW_ACHIEVEMENT, false);
```

3. **Add unlock logic** in appropriate location (e.g., MainGameActions)

4. **Update UI arrays** in AchievementDialog and LeaderboardPopup:
```java
private static final String[] ACHIEVEMENT_IMAGES = {
    // ... existing,
    "images/new_achievement.jpg"
};
```

5. **Add asset file** to `source/core/assets/images/`

---

## 👥 Contributors

- Achievement System Design & Implementation
- UI/UX Integration
- Testing & Bug Fixes

---

## 📄 License

This achievement system is part of the game project and follows the same license as the main project.

---

**Last Updated**: 2025-01-08  
**Version**: 1.0  
**Status**: Production Ready ✅

