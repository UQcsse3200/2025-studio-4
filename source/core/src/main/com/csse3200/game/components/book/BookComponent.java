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
}
