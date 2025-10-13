package com.csse3200.game.components.projectile;

import com.csse3200.game.components.Component;

/**
 * Marker component to identify tank-fired interceptor projectiles.
 * Presence of this tag lets tanks ignore each other's pellets and
 * helps collision logic avoid interceptor-vs-interceptor chain reactions.
 * Used by AntiProjectileShooterComponent and InterceptOnHitComponent to filter entities.
 */
public class InterceptorTagComponent extends Component {
    // Intentionally empty — the type itself is the tag.
}