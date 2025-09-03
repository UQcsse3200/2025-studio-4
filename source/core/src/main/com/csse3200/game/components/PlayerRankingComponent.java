package com.csse3200.game.components;

import com.csse3200.game.components.Component;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerRankingComponent extends Component {
    private int enemiesKilled = 0;
    private int towersRemaining = 0;
    private int remainingHealth = 0;
    private float timeTakenInSeconds = 0f;
    private boolean noDamageClear = false;

    private Timer timer = new Timer();
    private TimerTask timerTask;

    public PlayerRankingComponent() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                timeTakenInSeconds += 1;
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    @Override
    public void dispose() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }


    public void addKill() {
        enemiesKilled++;
    }

    public void setTowersRemaining(int count) {
        this.towersRemaining = count;
    }

    public void setRemainingHealth(int health) {
        this.remainingHealth = health;
    }

    public void setNoDamageClear(boolean noDamage) {
        this.noDamageClear = noDamage;
    }

    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }


    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public int getTowersRemaining() {
        return towersRemaining;
    }

    public int getRemainingHealth() {
        return remainingHealth;
    }

    public float getTimeTakenInSeconds() {
        return timeTakenInSeconds;
    }

    public boolean isNoDamageClear() {
        return noDamageClear;
    }
}
