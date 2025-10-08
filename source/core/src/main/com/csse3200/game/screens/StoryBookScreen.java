package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

public class StoryBookScreen extends ScreenAdapter {
    private final GdxGame game;
    private Stage stage;
    private BitmapFont storyFont;

    public StoryBookScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }

        String[] textures = {"images/StoryBook.jpg"};
        ServiceLocator.getResourceService().loadTextures(textures);
        ServiceLocator.getResourceService().loadAll();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createStoryBook();
    }

    private void createStoryBook() {
        Image bookBackground = new Image(ServiceLocator.getResourceService()
            .getAsset("images/StoryBook.jpg", Texture.class));
        bookBackground.setFillParent(true);
        stage.addActor(bookBackground);

        Table mainTable = new Table();
        mainTable.setFillParent(true);

        storyFont = new BitmapFont();
        storyFont.getData().setScale(1.2f);

        Label.LabelStyle storyStyle = new Label.LabelStyle();
        storyStyle.font = storyFont;
        storyStyle.fontColor = new Color(0.2f, 0.1f, 0.05f, 1f);

        String leftPageText = 
            "Year 2157.\n" +
            "The AI Uprising.\n\n" +
            "After humanity lost control\n" +
            "of its own creations,\n" +
            "corrupted military AI systems\n" +
            "turned their weapons against\n" +
            "their makers.\n\n" +
            "You are Commander,\n" +
            "tasked with defending the\n" +
            "last two strongholds:\n\n" +
            "The Forest Demo Sector -\n" +
            "a winding maze of natural\n" +
            "barriers, and Map Two Sector -\n" +
            "an open battlefield testing\n" +
            "your strategic mastery.";

        String rightPageText = 
            "Face relentless waves:\n\n" +
            "Swift Drone swarms from\n" +
            "neon megacities,\n" +
            "mass-produced Grunt\n" +
            "cyber-soldiers,\n" +
            "armored Tank siegebreakers,\n" +
            "unstable Dividers that split\n" +
            "into deadly offspring,\n" +
            "and the corrupted Boss -\n" +
            "once humanity's defender,\n" +
            "now its greatest threat.\n\n" +
            "Deploy defensive towers,\n" +
            "command elite hero units,\n" +
            "and hold the line.\n\n" +
            "The fate of humanity\n" +
            "rests in your hands!";

        Label leftPageLabel = new Label(leftPageText, storyStyle);
        leftPageLabel.setAlignment(Align.left);
        leftPageLabel.setWrap(true);

        Label rightPageLabel = new Label(rightPageText, storyStyle);
        rightPageLabel.setAlignment(Align.left);
        rightPageLabel.setWrap(true);

        Table pagesTable = new Table();
        float pageWidth = Gdx.graphics.getWidth() * 0.35f;
        
        pagesTable.add(leftPageLabel).width(pageWidth).padLeft(Gdx.graphics.getWidth() * 0.25f).padRight(Gdx.graphics.getWidth() * 0.03f).top();
        pagesTable.add(rightPageLabel).width(pageWidth).padLeft(-Gdx.graphics.getWidth() * 0.06f).top();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = storyFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;

        TextButton backButton = new TextButton("Back", buttonStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });

        mainTable.add(pagesTable).padTop(Gdx.graphics.getHeight() * 0.15f).expandY().top();
        mainTable.row();
        mainTable.add(backButton).padBottom(50f);

        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (storyFont != null) {
            storyFont.dispose();
        }
    }
}

