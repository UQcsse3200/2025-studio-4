package com.csse3200.game.components.hero.samurai.attacks;

import com.csse3200.game.components.Component;

public class AttackLockComponent extends Component {
    private boolean busy = false;
    private String who = "";

    public boolean tryAcquire(String name) {
        if (busy) return false;
        busy = true;
        who = name;
        return true;
    }

    public void release(String name) {
        if (busy && (who.equals(name) || name == null)) {
            busy = false;
            who = "";
        }
    }

    public boolean isBusy() { return busy; }
    public String holder(){ return who; }
}
