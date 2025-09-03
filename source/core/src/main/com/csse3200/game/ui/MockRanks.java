// com.csse3200.game.ui.MockRanks.java
package com.csse3200.game.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class MockRanks {
    private static final String[] NAMES = {
            "Player1","Player2","Alice","Bob","Carol","Dave","Eve","Mallory","Trent","Victor",
            "Heidi","Oscar","Peggy","Trudy","Walter","Grace","Judy","Niaj","Sybil","Zoe"
    };

    private MockRanks() {}

    /** Generate a count of fake data, with scores starting from high to low and ranks starting from 1. */
    public static List<PlayerRank> make(int count) {
        Random r = new Random();
        List<PlayerRank> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = NAMES[r.nextInt(NAMES.length)] + (r.nextInt(90)+10); // 随机后缀
            int score = 10000 - r.nextInt(5000);
            int delta = r.nextInt(3) - 1;
            list.add(new PlayerRank("id"+i, name, i, score, delta, "Global"));
        }
        // Sort in descending order of scores, then rewrite the rank in the new order
        list.sort(Comparator.comparingInt((PlayerRank p) -> p.score).reversed());
        for (int i = 0; i < list.size(); i++) list.get(i).rank = i + 1;
        return list;
    }
}
