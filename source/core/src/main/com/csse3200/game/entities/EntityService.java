package com.csse3200.game.entities;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Provides a global access point for entities to register themselves. This allows for iterating
 * over entities to perform updates each loop. All game entities should be registered here.
 *
 * Avoid adding additional state here! Global access is often the easy but incorrect answer to
 * sharing data.
 */
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);
    private static final int INITIAL_CAPACITY = 16;

    private final Array<Entity> entities = new Array<>(false, INITIAL_CAPACITY);
    // NEW: keyed pools for reusable entities (e.g., "bullet:<tex>", "interceptor:<sprite>")
    private final ObjectMap<String, Array<Entity>> pools = new ObjectMap<>();

    /**
     * Register a new entity with the entity service. The entity will be created and start updating.
     * @param entity new entity.
     */
    public void register(Entity entity) {
        logger.debug("Registering {} in entity service", entity);
        entities.add(entity);
        entity.create();
    }

    /**
     * Unregister an entity with the entity service. The entity will be removed and stop updating.
     * @param entity entity to be removed.
     */
    public void unregister(Entity entity) {
        logger.debug("Unregistering {} in entity service", entity);
        entities.removeValue(entity, true);
    }

    /**
     * Update all registered entities. Should only be called from the main game loop.
     */
    public void update() {
        for (Entity entity : entities) {
            entity.earlyUpdate();
            entity.update();
        }
    }

    /**
     * Dispose all entities.
     */
    public void dispose() {
        for (Entity entity : entities) {
            entity.dispose();
        }
    }

    /**
     * @return a shallow copy of the current entity list for safe iteration
     */
    public Array<Entity> getEntitiesCopy() {
        return new Array<Entity>(entities);
    }

    /**
     * Get the live array of registered entities. Mutating this externally is not supported.
     * @return internal entity array
     */
    public Array<Entity> getEntities() {
        return entities;
    }

    // ====== NEW: pooling helpers ======

    /**
     * Obtain a reusable entity from the pool identified by key, or create one via the provided factory
     * when no pooled instances are available. The returned entity may still need registration depending
     * on the factory implementation.
     * @param key     pool identifier
     * @param factory factory used to create an entity when the pool is empty
     * @return pooled or newly created entity
     */
    public Entity obtain(String key, Supplier<Entity> factory) {
        Array<Entity> pool = pools.get(key);
        if (pool != null && pool.size > 0) {
            return pool.pop();
        }
        // Create a new one via factory; caller should ensure registration happens in factory or afterwards
        return factory.get();
    }

    /**
     * Return an entity to the specified pool. The entity should already be made inactive by callers
     * (e.g., disable physics/hide sprite) before despawning.
     * @param e   entity to return
     * @param key pool identifier
     */
    public void despawnEntity(Entity e, String key) {
        Array<Entity> pool = pools.get(key);
        if (pool == null) {
            pool = new Array<>(false, 16);
            pools.put(key, pool);
        }
        pool.add(e);
    }

    /**
     * Convenience method: return an entity to a pool based on its ProjectileComponent's pool key,
     * if present; otherwise unregister the entity.
     * @param e entity to return or unregister
     */
    public void despawnEntity(Entity e) {
        try {
            var pc = e.getComponent(com.csse3200.game.components.projectile.ProjectileComponent.class);
            if (pc != null && pc.getPoolKey() != null) {
                despawnEntity(e, pc.getPoolKey());
                return;
            }
        } catch (Throwable ignored) {}
        // If no pooling key, just unregister to avoid leaks
        unregister(e);
    }
}
