package com.csse3200.game.services;

import com.csse3200.game.events.EventHandler;

public class ScoreService {
    private int totalScore = 0;
    private final EventHandler pointsEvent = new EventHandler();

    // get current total score
    public int getScore() {
        return totalScore;
    }

    // reset the score to 0
    public void reset() {
        totalScore = 0;
        pointsEvent.trigger("scoreChanged", totalScore);
    }

    // add points for each enemy to the total
    public void addPoints(int points) {
        totalScore += points;
        pointsEvent.trigger("scoreChanged", totalScore);
    }

    // Expose the event bus so HUD can subscribe to updates
    public EventHandler getEvents() {
        return pointsEvent;
    }


}
