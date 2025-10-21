package com.csse3200.game.components.maingame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntroDialogueEntryTest {

  @Test
  void fullConstructorPreservesAllFields() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Hello World",
            "portrait.png",
            "voice.ogg",
            "right",
            "background.png",
            "Speaker");

    assertEquals("Hello World", entry.text());
    assertEquals("portrait.png", entry.portraitPath());
    assertEquals("voice.ogg", entry.soundPath());
    assertEquals("right", entry.portraitSide());
    assertEquals("background.png", entry.dialogueBackgroundPath());
    assertEquals("Speaker", entry.speakerName());
  }

  @Test
  void constructorWithoutBackgroundDefaultsNameToNull() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "left", null);

    assertEquals("Line", entry.text());
    assertEquals("portrait.png", entry.portraitPath());
    assertEquals("voice.ogg", entry.soundPath());
    assertEquals("left", entry.portraitSide());
    assertNull(entry.speakerName());
  }

  @Test
  void constructorWithPortraitSideDefaultsBackgroundAndName() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "right");

    assertEquals("right", entry.portraitSide());
    assertNull(entry.dialogueBackgroundPath());
    assertNull(entry.speakerName());
  }

  @Test
  void constructorWithSoundDefaultsSideToLeft() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg");

    assertEquals("left", entry.portraitSide());
    assertNull(entry.dialogueBackgroundPath());
    assertNull(entry.speakerName());
  }

  @Test
  void minimalConstructorDefaultsOptionalFields() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry("Line", "portrait.png");

    assertEquals("Line", entry.text());
    assertEquals("portrait.png", entry.portraitPath());
    assertNull(entry.soundPath());
    assertEquals("left", entry.portraitSide());
    assertNull(entry.dialogueBackgroundPath());
    assertNull(entry.speakerName());
  }

  @Test
  void equalsAndHashCodeAreBasedOnAllFields() {
    IntroDialogueComponent.DialogueEntry entry1 =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "left", "background.png", "Speaker");
    IntroDialogueComponent.DialogueEntry entry2 =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "left", "background.png", "Speaker");
    IntroDialogueComponent.DialogueEntry different =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "right", "background.png", "Speaker");

    assertEquals(entry1, entry2);
    assertEquals(entry1.hashCode(), entry2.hashCode());
    assertNotEquals(entry1, different);
  }

  @Test
  void toStringListsCoreFields() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "right", "background.png", "Speaker");

    String text = entry.toString();
    assertTrue(text.contains("Line"));
    assertTrue(text.contains("portrait.png"));
    assertTrue(text.contains("Speaker"));
  }

  @Test
  void constructorAllowsNullSpeakerNameButKeepsOthers() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "right", "background.png", null);

    assertNull(entry.speakerName());
    assertEquals("background.png", entry.dialogueBackgroundPath());
    assertEquals("right", entry.portraitSide());
  }

  @Test
  void constructorAcceptsNullPortraitSide() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", null, "background.png", "Speaker");

    assertNull(entry.portraitSide());
    assertEquals("background.png", entry.dialogueBackgroundPath());
  }

  @Test
  void distinctSoundPathsProduceDistinctEntries() {
    IntroDialogueComponent.DialogueEntry entryA =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voiceA.ogg", "left", null, null);
    IntroDialogueComponent.DialogueEntry entryB =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voiceB.ogg", "left", null, null);

    assertNotEquals(entryA, entryB);
    assertNotEquals(entryA.hashCode(), entryB.hashCode());
  }

  @Test
  void backgroundPathMayBeEmptyString() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "left", "", "Speaker");
    assertEquals("", entry.dialogueBackgroundPath());
  }

  @Test
  void speakerNameMayBeEmptyString() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "left", null, "");
    assertEquals("", entry.speakerName());
  }

  @Test
  void recordComponentsProvideDirectAccess() {
    IntroDialogueComponent.DialogueEntry entry =
        new IntroDialogueComponent.DialogueEntry(
            "Line", "portrait.png", "voice.ogg", "right", "background.png", "Speaker");
    assertEquals("Line", entry.text());
    assertEquals("portrait.png", entry.portraitPath());
    assertEquals("voice.ogg", entry.soundPath());
  }
}
