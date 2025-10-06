package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class MainGameActionsSaveFlowTest {

  private Entity ui;
  private boolean showSaveUITriggered;
  private boolean showPauseUITriggered;
  private boolean saveFeedbackTriggered;

  @BeforeEach
  void setup() {
    // Minimal services required by MainGameActions save path
    ServiceLocator.registerEntityService(new EntityService());

    ui = new Entity();
    // Provide a lightweight GdxGame; MainGameActions only uses it to set screens for
    // other events, not in the paths we test here.
    GdxGame dummyGame = new GdxGame();
    ui.addComponent(new MainGameActions(dummyGame));

    // Observe events
    EventHandler events = ui.getEvents();
    events.addListener("showSaveUI", () -> showSaveUITriggered = true);
    events.addListener("showPauseUI", () -> showPauseUITriggered = true);
    events.addListener("showSaveSuccess", () -> saveFeedbackTriggered = true);
    events.addListener("showSaveError", () -> saveFeedbackTriggered = true);

    // Complete entity creation to wire listeners
    ServiceLocator.getEntityService().register(ui);
  }

  @AfterEach
  void cleanup() {
    ServiceLocator.clear();
    showSaveUITriggered = false;
    showPauseUITriggered = false;
    saveFeedbackTriggered = false;
  }

  @Test
  void saveEvent_shouldOnlyShowSaveUI_andNotPause() {
    // When
    ui.getEvents().trigger("save");

    // Then
    assertTrue(showSaveUITriggered, "save should trigger showSaveUI");
    assertFalse(showPauseUITriggered, "save should not trigger showPauseUI (no implicit pause)");
  }

  @Test
  void performSave_shouldGiveFeedback_andNotPause() {
    // When
    ui.getEvents().trigger("performSave");

    // Then: one of success/error feedback must be fired, but never pause UI
    assertTrue(saveFeedbackTriggered, "performSave should emit save feedback event");
    assertFalse(showPauseUITriggered, "performSave should not trigger showPauseUI");
  }
}


