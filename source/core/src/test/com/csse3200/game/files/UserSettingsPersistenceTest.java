package com.csse3200.game.files;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class UserSettingsPersistenceTest {

  @Test
  void shouldPersistNewFields() {
    // Arrange
    UserSettings.Settings settings = new UserSettings.Settings();
    settings.fps = 75;
    settings.fullscreen = false;
    settings.vsync = false;

    // New fields under test
    settings.musicVolume = 0.7f;
    settings.soundVolume = 0.3f;
    settings.difficulty = "Hard";
    settings.language = "中文";

    // Act: write settings without applying to GDX immediately
    UserSettings.set(settings, false);

    // Read back
    UserSettings.Settings loaded = UserSettings.get();

    // Assert: newly added fields are persisted and read back correctly
    assertEquals(0.7f, loaded.musicVolume, 1e-6);
    assertEquals(0.3f, loaded.soundVolume, 1e-6);
    assertEquals("Hard", loaded.difficulty);
    assertEquals("中文", loaded.language);

    // Also sanity check a couple of existing fields to ensure no regression
    assertEquals(75, loaded.fps);
    assertFalse(loaded.fullscreen);
    assertFalse(loaded.vsync);
  }
}


