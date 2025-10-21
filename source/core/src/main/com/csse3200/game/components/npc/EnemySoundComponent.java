package com.csse3200.game.components.npc;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * This component manages sound effects for enemies by hooking into the same
 * events as EnemyAnimationController: chaseStart, attackStart, and entityDeath.
 */
public class EnemySoundComponent extends Component {
    private Sound walkSound;
    private Sound walkSound2; // For boss's second walk sound
    private float walkSound1Duration = 0f;
    private Sound attackSound;
    private Sound deathSound;
    private Sound ambientSound;
    private Sound bossMusic;
    
    private float lastAmbientSound = 0f;
    private static final float MIN_AMBIENT_DELAY = 5f; // Minimum seconds between ambient sounds
    private static final float AMBIENT_CHANCE = 0.01f; // 1% chance per frame when checked
    
    private boolean isBoss = false;
    private long bossMusicId = -1;
    private boolean hasPlayedDeathSound = false; // Prevent multiple death sounds

    /**
     * Create the sound component with standard sounds
     */
    public EnemySoundComponent(Sound walkSound, Sound attackSound, Sound deathSound, Sound ambientSound) {
        this.walkSound = walkSound;
        this.attackSound = attackSound;
        this.deathSound = deathSound;
        this.ambientSound = ambientSound;
    }

    /**
     * Constructor for enemies with two-part walk sounds (like boss)
     */
    public EnemySoundComponent(Sound walkSound1, float walkSound1Duration, Sound walkSound2, 
                             Sound attackSound, Sound deathSound, Sound ambientSound) {
        this.walkSound = walkSound1;
        this.walkSound2 = walkSound2;
        this.walkSound1Duration = walkSound1Duration;
        this.attackSound = attackSound;
        this.deathSound = deathSound;
        this.ambientSound = ambientSound;
    }

    /**
     * Create a sound component specifically for a boss enemy with music
     */
    public EnemySoundComponent(Sound walkSound, Sound attackSound, Sound deathSound, 
                             Sound ambientSound, Sound bossMusic) {
        this(walkSound, attackSound, deathSound, ambientSound);
        this.bossMusic = bossMusic;
        this.isBoss = true;
    }

    @Override
    public void create() {
        // Hook into the SAME events as EnemyAnimationController
        entity.getEvents().addListener("chaseStart", this::playWalkSound);
        entity.getEvents().addListener("attackStart", this::playAttackSound);
        entity.getEvents().addListener("entityDeath", this::playDeathSound);
        
        // Start boss music if this is a boss
        if (isBoss && bossMusic != null) {
            bossMusicId = bossMusic.loop(0.7f); // Play at 70% volume
        }
    }

    @Override
    public void update() {
        // Random chance to play ambient sound if enough time has passed
        if (ambientSound != null) {
            float currentTime = ServiceLocator.getTimeSource().getTime();
            if (currentTime - lastAmbientSound >= MIN_AMBIENT_DELAY && MathUtils.random() < AMBIENT_CHANCE) {
                playAmbientSound();
                lastAmbientSound = currentTime;
            }
        }
    }

    private void playWalkSound() {
        if (walkSound != null) {
            walkSound.play(0.3f); // Walk sounds at 30% volume
            // If this is a boss with two walk sounds
            if (walkSound2 != null) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        walkSound2.play(0.3f);
                    }
                }, walkSound1Duration);
            }
        }
    }

    private void playAttackSound() {
        if (attackSound != null) {
            attackSound.play(0.8f); // Attack sounds at 80% volume
        }
    }

    private void playDeathSound() {
        // Only play death sound once
        if (hasPlayedDeathSound) {
            return;
        }
        hasPlayedDeathSound = true;
        
        if (deathSound != null) {
            deathSound.play(0.5f); // Death sound at 50% volume
        }
        
        // Stop boss music if this was a boss
        if (isBoss && bossMusic != null && bossMusicId != -1) {
            bossMusic.stop(bossMusicId);
        }
    }

    private void playAmbientSound() {
        if (ambientSound != null) {
            ambientSound.play(0.4f); // Ambient sounds at 40% volume
        }
    }

    /**
     * Set a second walk sound that will play after the first one (for boss)
     */
    public void setSecondWalkSound(Sound walkSound2, float walkSound1Duration) {
        this.walkSound2 = walkSound2;
        this.walkSound1Duration = walkSound1Duration;
    }

    @Override
    public void dispose() {
        if (isBoss && bossMusic != null && bossMusicId != -1) {
            bossMusic.stop(bossMusicId);
        }
        if (walkSound2 != null) {
            walkSound2.dispose();
        }
    }
}