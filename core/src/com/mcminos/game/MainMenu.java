package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen {

    private final Skin skin;
    private final Stage stage;
    private final Table table;
    private final Texture bg;
    private final Image bgimage;
    private final SelectBox sb;

    public MainMenu() {
        bg = new Texture( Gdx.files.internal("images/loadscreen.png"));
        bgimage = new Image(bg);
        bgimage.setZIndex(0);
        bgimage.setScaling(Scaling.none);
        Root.scaleBackground(bgimage);

        skin = Root.defaultSkin;
        stage = new Stage(new ScreenViewport(),Root.batch);
        // table for buttons
        table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center | Align.top);

        TextButton startButton = new TextButton("Start",skin);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Root.setScreen(new Play((String) sb.getSelected()));
            }
        });

        /*Label text1 = new Label( "Level 1", skin );
        Label text2 = new Label( "Level 2", skin );
        Label text3 = new Label( "Level 3", skin );

        Table scrollTable = new Table();
        scrollTable.add(text1).row();
        scrollTable.add(text2).row();
        scrollTable.add(text3).row();

        ScrollPane scroller = new ScrollPane( scrollTable );

        table.pad(32);
        table.add(scroller).fill().expand(); */

        sb = new SelectBox(skin);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Gdx.files.internal("levels/list").read()), 2048);
        String line;
        ArrayList<String> names = new ArrayList<>();

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim(); // remove whitespace
                names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sb.setItems(names.toArray());
        if( Root.currentLevelName != null)
            sb.setSelected(Root.currentLevelName);

        table.add(sb)
                .pad(32)
                .minSize(160, 48)
                .row();
        table.add(startButton)
                .minSize(128, 48);

        stage.addActor(bgimage);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: Create background-picture for loading screen
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        Root.scaleBackground(bgimage);
        table.setBounds(0,0,width,height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        bg.dispose();
    }
}
