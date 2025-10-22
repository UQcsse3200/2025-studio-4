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

    // Store active sound IDs
    private long walkSoundId = -1;
    private long walkSound2Id = -1;
    private long attackSoundId = -1;
    private long ambientSoundId = -1;
    
    private float lastAmbientSound = 0f;
    private static final float MIN_AMBIENT_DELAY = 5f; // Minimum seconds between ambient sounds
    private static final float AMBIENT_CHANCE = 0.001f; // 1% chance per frame when checked
    
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
        entity.getEvents().addListener("chaseStart", this::playWalkSound);
        entity.getEvents().addListener("attackStart", this::playAttackSound);
        entity.getEvents().addListener("entityDeath", this::playDeathSound);
        
        if (isBoss && bossMusic != null) {
            bossMusicId = bossMusic.loop(0.7f);
        }
    }

    @Override
    public void update() {
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
            // Stop previous walk sound if still playing
            if (walkSoundId != -1) {
                walkSound.stop(walkSoundId);
            }
            walkSoundId = walkSound.play(0.3f);
            
            if (walkSound2 != null) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (walkSound2Id != -1) {
                            walkSound2.stop(walkSound2Id);
                        }
                        walkSound2Id = walkSound2.play(0.3f);
                    }
                }, walkSound1Duration);
            }
        }
    }

    private void playAttackSound() {
        if (attackSound != null) {
            // Stop previous attack sound if still playing
            if (attackSoundId != -1) {
                attackSound.stop(attackSoundId);
            }
            attackSoundId = attackSound.play(0.8f);
        }
    }

    private void playDeathSound() {
        if (hasPlayedDeathSound) {
            return;
        }
        hasPlayedDeathSound = true;
        
        // Stop all currently playing sounds
        stopAllSounds();
        
        // Then play the death sound
        if (deathSound != null) {
            //deathSound.play(0.5f);
        }
    }

    private void playAmbientSound() {
        if (ambientSound != null) {
            if (ambientSoundId != -1) {
                ambientSound.stop(ambientSoundId);
            }
            ambientSoundId = ambientSound.play(0.1f);
        }
    }

    /**
     * Stop all currently playing sounds for this enemy
     */
    private void stopAllSounds() {
        if (walkSound != null && walkSoundId != -1) {
            walkSound.stop(walkSoundId);
            walkSoundId = -1;
        }
        
        if (walkSound2 != null && walkSound2Id != -1) {
            walkSound2.stop(walkSound2Id);
            walkSound2Id = -1;
        }
        
        if (attackSound != null && attackSoundId != -1) {
            attackSound.stop(attackSoundId);
            attackSoundId = -1;
        }
        
        if (ambientSound != null && ambientSoundId != -1) {
            ambientSound.stop(ambientSoundId);
            ambientSoundId = -1;
        }
        
        if (isBoss && bossMusic != null && bossMusicId != -1) {
            bossMusic.stop(bossMusicId);
            bossMusicId = -1;
        }
    }

    public void setSecondWalkSound(Sound walkSound2, float walkSound1Duration) {
        this.walkSound2 = walkSound2;
        this.walkSound1Duration = walkSound1Duration;
    }

    @Override
    public void dispose() {
        stopAllSounds();
    }
}