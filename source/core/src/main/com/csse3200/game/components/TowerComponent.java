package com.csse3200.game.components;

public class TowerComponent extends Component {
    private TowerState state = TowerState.IDLE;

    public TowerState getState() { return state; }
    public void setState(TowerState state) { this.state = state; }

    @Override
    public void update() {
        // State logic placeholder
        switch (state) {
            case IDLE:
                // Idle behavior
                break;
            case TARGETING:
                // Targeting logic
                break;
            case ATTACKING:
                // Attacking logic
                break;
        }
    }
}