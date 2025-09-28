package com.csse3200.game.components.book;

public class BookComponent {

    private String[] currencyBackGround = {
            "images/currency/metal_scrap.png",
            "images/currency/neurochip.png",
            "images/currency/titanium_core.png",
    };

    private String[] currencyTitle = {
            "Metal scrap",
            "Neurochip",
            "Titanium"
    };

    private String[] currencyData = {
            "A basic and plentiful resource, scavenged from the battlefield. Useful for early upgrades and essential constructions.",
            "A highly advanced implant once designed to enhance human intelligence, now repurposed by AI to control and outsmart its creators. Extremely rare and vital for high-level upgrades",
            "A rare and valuable resource, often obtained from tougher enemies. Needed for powerful upgrades and advanced technology."
    };

    private String[] enemyBackGround = {
            "images/grunt_enemy.png",
            "images/tank_enemy.png",
            "images/drone_enemy.png",
            "images/boss_enemy.png",
            "images/divider_enemy.png"
    };

    private String[] enemyTitle = {
            "Grunt enemy",
            "Tank enemy",
            "Drone enemy",
            "Boss enemy",
            "Divider enemy"
    };

    private String[] enemyData = {
            "The grunt is the most common enemy unit in the cyberpunk wastelands. Medium weight and balanced, they lack any spectacular traits, but their sheer numbers make them a constant threat. Do not underestimate them—when ignored, they can swarm defences and drain your resources.",
            "The tank is a slow-moving heavyweight unit, armoured to withstand enormous amounts of punishment. While sluggish, its heavy cannon can devastate defences if it gets into range. Defeating a tank often requires concentrated firepower or high-damage towers.",
            "The drone is a lightweight, fast-moving aerial enemy. Agile and difficult to hit, drones dart across the battlefield, bypassing ground-based obstacles and exploiting weak anti-air defences. While fragile, their speed makes them a dangerous distraction.",
            "The boss bot is a huge mechanical beast, capable of inflicting severe damage in short bursts. Its armour provides a significant health boost, and upon defeat the bot splits into three smaller grunt enemies (to be implemented...). Be sure to target this enemy early, defeating it near the base might let grunts through!",
            "The Divider enemy is a formidable foe, with its tankier than usual health and slightly lower damage and speed it might seem like an easy target, but don't be fooled. Once killed, the Divider releases its children Dividers who are faster in speed and pack more damage. One mistake and this one will surely pass all your defences."
    };

    private String[] towerBackGround = {
            "images/bone.png",
            "images/dino.png",
            "images/cavemen.png"
    };

    private String[] towerTitle = {
            "Bone tower",
            "Dino tower",
            "Cavemen tower"
    };

    private String[] towerData = {
            "The Bone Tower is a sturdy, reliable defender made from ancient fossilized bones. It may not be the strongest, but its steady damage and decent range make it perfect for holding choke points and supporting other towers. Its projectiles are jagged bones hurled with deadly precision at approaching enemies.",
            "The Dino Tower channels the raw power of prehistoric predators. Its short range forces it to fight up close, but its fiery projectiles can take down even the toughest enemies in seconds. This tower is perfect for high-risk, high-reward positions where enemies cluster together.",
            "The Cavemen Tower is a long-range sniper manned by resourceful hunters. Though its damage is modest, it picks off enemies from a distance using firearms scavenged from fallen AI bots. It excels at softening enemies before they reach the front lines."
    };

    public BookComponent() {

    }

    public String[] getCurrencyBackGround() {
        return currencyBackGround;
    }

    public String[] getCurrencyTitle() {
        return currencyTitle;
    }

    public String[] getCurrencyData() {
        return currencyData;
    }

    public String[] getEnemyBackGround() {
        return enemyBackGround;
    }

    public String[] getEnemyTitle() {
        return enemyTitle;
    }

    public String[] getEnemyData() {
        return enemyData;
    }

    public String[] getTowerTitle() {
        return towerTitle;
    }

    public String[] getTowerBackGround() {
        return towerBackGround;
    }

    public String[] getTowerData() {
        return towerData;
    }
}
