package com.csse3200.game.ui;

public class PlayerRank {
    public String playerId;
    public String name;
    public int rank;
    public int score;
    /** 名次变化：负数=上升；正数=下降；0=不变 */
    public int delta;
    public String mode; // Global / Friends / Endless...

    public PlayerRank(String playerId, String name, int rank, int score, int delta, String mode) {
        this.playerId = playerId;
        this.name = name;
        this.rank = rank;
        this.score = score;
        this.delta = delta;
        this.mode = mode;
    }

    /** 提供一条假数据，演示用 */
    public static PlayerRank mock() {
        return new PlayerRank("local-001", "Mengdie", 17, 84210, -2, "Global");
    }
}

