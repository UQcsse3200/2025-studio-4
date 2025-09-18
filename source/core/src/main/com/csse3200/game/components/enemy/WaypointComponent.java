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
        this.playerRef = player;
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