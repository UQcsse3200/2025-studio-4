package com.csse3200.game.components.rewards;
import com.csse3200.game.components.PlayerRankingComponent;
import java.util.ArrayList;
import java.util.List;
public class RewardGenerator {
    public static List<Reward> generateRewards(PlayerRankingComponent ranking) {
        List<Reward> rewards = new ArrayList<>();

        // 击杀奖励
        if (ranking.getEnemiesKilled() >= 10) {
            rewards.add(new Reward("Slayer", "Killed 10+ enemies", 100));
        }

        // 无伤通关
        if (ranking.isNoDamageClear()) {
            rewards.add(new Reward("Perfect Clear", "Completed level with no damage", 0));
        }

        // 剩余血量
        if (ranking.getRemainingHealth() >= 80) {
            rewards.add(new Reward("Tough Survivor", "Finished with high HP", 50));
        }

        // 快速通关
        if (ranking.getTimeTakenInSeconds() <= 60) {
            rewards.add(new Reward("Speed Runner", "Finished in under 1 min", 75));
        }

        // 没有奖励情况
        if (rewards.isEmpty()) {
            rewards.add(new Reward("Participation", "Thanks for playing!", 10));
        }

        return rewards;
    }
}
