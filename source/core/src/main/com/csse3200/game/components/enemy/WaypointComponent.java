package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import java.util.List;

/**
 * Component that stores enemy-specific waypoint and navigation data.
 * Each enemy entity should have its own instance of this component to maintain
 * independent waypoint progression.
 */
public class WaypointComponent extends Component {
    private List<Entity> waypoints;
    private int currentWaypointIndex;
    private Entity currentTarget;
    private int priorityTaskCount;
    private Vector2 speed;
    private Vector2 baseSpeed;
    private Entity playerRef;

    /**
     * Creates a new WaypointComponent with the specified parameters.
     *
     * @param waypoints List of waypoint entities for this enemy to follow
     * @param player Reference to the player entity
     * @param speed Movement speed for this enemy
     */
    public WaypointComponent(List<Entity> waypoints, Entity player, Vector2 speed) {
        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalArgumentException("Waypoints list cannot be null or empty");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player reference cannot be null");
        }
        if (speed == null) {
            throw new IllegalArgumentException("Speed cannot be null");
        }

        this.waypoints = waypoints;
        this.currentWaypointIndex = 0;
        this.currentTarget = waypoints.get(0);
        this.priorityTaskCount = 1;
        this.speed = new Vector2(speed); // Create a copy to avoid external modifications
        this.baseSpeed = new Vector2(speed);
        this.playerRef = player;
    }

    /**
     * Rebind this enemy to a new waypoint list and reset progression to start.
     * After rebinding, the enemy will follow the provided path from the beginning.
     *
     * @param newWaypoints new waypoint list (must be non-null and non-empty)
     */
    public void rebindWaypoints(List<Entity> newWaypoints) {
        if (newWaypoints == null || newWaypoints.isEmpty()) {
            throw new IllegalArgumentException("New waypoints list cannot be null or empty");
        }
        this.waypoints = newWaypoints;
        resetWaypoints();
    }

    /**
     * Rebind to new waypoints and snap progression to the nearest waypoint
     * based on the entity's current world position. This keeps movement
     * continuing from the saved spot along the path rather than restarting.
     */
    public void rebindWaypointsAndSnap(List<Entity> newWaypoints, Vector2 entityWorldPos) {
        if (newWaypoints == null || newWaypoints.isEmpty()) {
            throw new IllegalArgumentException("New waypoints list cannot be null or empty");
        }
        this.waypoints = newWaypoints;
        // Find nearest waypoint to current position
        int nearestIdx = 0;
        float bestDst2 = Float.MAX_VALUE;
        for (int i = 0; i < waypoints.size(); i++) {
            Entity wp = waypoints.get(i);
            if (wp == null) continue;
            Vector2 wpPos = wp.getPosition();
            float d2 = wpPos.dst2(entityWorldPos);
            if (d2 < bestDst2) {
                bestDst2 = d2;
                nearestIdx = i;
            }
        }

        // Prefer forward progress: if next waypoint is closer than nearest, target next
        int targetIdx = nearestIdx;
        if (nearestIdx + 1 < waypoints.size()) {
            float dCurr = waypoints.get(nearestIdx).getPosition().dst2(entityWorldPos);
            float dNext = waypoints.get(nearestIdx + 1).getPosition().dst2(entityWorldPos);
            if (dNext <= dCurr) {
                targetIdx = nearestIdx + 1;
            }
        }

        setCurrentWaypointIndex(targetIdx);
        setCurrentTarget(waypoints.get(targetIdx));
    }

    /**
     * Gets the list of waypoints for this enemy.
     *
     * @return List of waypoint entities
     */
    public List<Entity> getWaypoints() {
        return waypoints;
    }

    /**
     * Gets the current waypoint index.
     *
     * @return Current waypoint index
     */
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    /**
     * Gets the current target entity.
     *
     * @return Current target entity
     */
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Gets the current priority task count.
     *
     * @return Priority task count
     */
    public int getPriorityTaskCount() {
        return priorityTaskCount;
    }

    /**
     * Gets the movement speed for this enemy.
     *
     * @return Copy of the speed vector
     */
    public Vector2 getSpeed() {
        return new Vector2(speed); // Return copy to prevent external modification
    }

    /**
     * Gets the base movement speed for this enemy.
     *
     * @return Copy of the base speed vector
     */
    public Vector2 getBaseSpeed() {
        return new Vector2(baseSpeed);
    }

    /**
     * Gets the player entity reference.
     *
     * @return Player entity
     */
    public Entity getPlayerRef() {
        return playerRef;
    }

    /**
     * Advances to the next waypoint index.
     */
    public void incrementWaypointIndex() {
        currentWaypointIndex++;
    }

    /**
     * Sets the current target entity.
     *
     * @param target New target entity
     */
    public void setCurrentTarget(Entity target) {
        this.currentTarget = target;
    }

    /**
     * Increments the priority task count for task management.
     */
    public void incrementPriorityTaskCount() {
        priorityTaskCount++;
    }

    /**
     * Updates the movement speed for this enemy.
     *
     * @param newSpeed New speed vector
     */
    public void setSpeed(Vector2 newSpeed) {
        if (newSpeed != null) {
            this.speed.set(newSpeed);
        }
    }

    /**
     * Checks if there are more waypoints available.
     *
     * @return true if there are more waypoints, false otherwise
     */
    public boolean hasMoreWaypoints() {
        return currentWaypointIndex < waypoints.size();
    }

    /**
     * Gets the next waypoint entity without advancing the index.
     *
     * @return Next waypoint entity, or null if no more waypoints
     */
    public Entity peekNextWaypoint() {
        if (hasMoreWaypoints()) {
            return waypoints.get(currentWaypointIndex);
        }
        return null;
    }

    /**
     * Advances to the next waypoint and returns it.
     *
     * @return Next waypoint entity, or null if no more waypoints
     */
    public Entity getNextWaypoint() {
        if (hasMoreWaypoints()) {
            Entity nextWaypoint = waypoints.get(currentWaypointIndex);
            incrementWaypointIndex();
            setCurrentTarget(nextWaypoint);
            return nextWaypoint;
        }
        return null;
    }

    /**
     * Resets the waypoint progression to the beginning.
     */
    public void resetWaypoints() {
        currentWaypointIndex = 0;
        if (!waypoints.isEmpty()) {
            currentTarget = waypoints.get(0);
        }
    }

    @Override
    public void dispose() {
        // Clean up any resources if needed
        waypoints = null;
        currentTarget = null;
        speed = null;
        baseSpeed = null;
        playerRef = null;
        super.dispose();
    }

    public void setCurrentWaypointIndex(int index) {
        if (index >= 0 && index < waypoints.size()) {
            this.currentWaypointIndex = index;
        } else {
            throw new IllegalArgumentException("Invalid waypoint index: " + index);
        }
    }
}