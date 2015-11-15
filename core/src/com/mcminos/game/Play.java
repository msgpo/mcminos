package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Play implements Screen, GestureListener, InputProcessor {
    private final OrthographicCamera camera;
    private Game game;
    private PlayWindow playwindow;
    private final Skin skin;
    private McMinos mcminos;
    private final Audio audio;
    private final BitmapFont font;
    private final SpriteBatch stageBatch;
    private final SpriteBatch gameBatch;
    private final SpriteBatch backgroundBatch;
    private final SpriteBatch miniBatch;
    private final ShapeRenderer miniScreenBackground;

    private Stage stage;
    private final Main main;
    private Level level;
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;
    private int gameResolutionCounter = 0;
    Graphics background;
    private Touchpad touchpad;
    private SegmentString scoreInfo;
    private SegmentString score;
    private SegmentString powerScore;
    private SegmentString umbrellaScore;
    private SegmentString toxicScore;
    private SegmentString livesScore;
    private SegmentString mirroredScore;
    private SegmentString framerateScore;

    private Toolbox toolbox;
    private boolean menusActivated = true;

    public Play(final Main main, String levelName) {
        this.main = main;
        gameBatch = main.getBatch();
        camera = new OrthographicCamera();
        font = main.getFont();
        skin = main.getSkin();
        audio = main.getAudio();
        // don't conflict with gameBatch
        stageBatch = new SpriteBatch();
        backgroundBatch = new SpriteBatch();
        miniBatch = new SpriteBatch();
        miniScreenBackground = new ShapeRenderer();
        init(levelName);
    }

    public void init(String levelName) {
        game = new Game(main, this, camera);
//        background = Entities.backgrounds_punched_plate_03;
        background = Entities.backgrounds_amoeboid_01;
        game.disableMovement();
        game.currentLevelName = levelName;
        level = game.loadLevel(levelName);
        mcminos = game.getMcMinos();
        playwindow = game.getPlayWindow();

        //  Basically, based on density and screensize, we want to set out default zoomlevel.
        float density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI

        int preferredResolution =  Math.max( (int) (density * 32),
                Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() ) / 16
                );
        gameResolutionCounter = playwindow.setClosestResolution(preferredResolution);

        // Init stage
        stage = new Stage(new ScreenViewport(), stageBatch);

        toolbox = new Toolbox(game,this, stage,skin);

        // init scoreinfo display
        scoreInfo = new SegmentString( "S00000 P00 U00 T00 L00 F00 M" );
        score = scoreInfo.sub(1,5);
        powerScore = scoreInfo.sub(8,2);
        umbrellaScore = scoreInfo.sub(12,2);
        toxicScore = scoreInfo.sub(16,2);
        livesScore = scoreInfo.sub(20,2);
        framerateScore = scoreInfo.sub(24,2);
        mirroredScore = scoreInfo.sub(27,1);

        // virtual joystick (called touchpad in libgdx)
        touchpad = new Touchpad(32, skin);
        Color tpColor = touchpad.getColor();
        touchpad.setColor(tpColor.r, tpColor.g, tpColor.b, 0.7f);
        touchpadResize();
        touchpad.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.isToolboxActivated()) {
                    mcminos.updateTouchpadDirections(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
                    if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                        game.enableMovement();
                    }
                }
            }
        });
        // load this from settings
        //stage.addActor(touchpad);

        // InputProcessor
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(stage, gd, this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
    }

    public void toogleTouchpad() {
        if(touchpad.hasParent())
            touchpad.remove();
        else {
            touchpadResize();
            stage.addActor(touchpad);
        }
    }


    private void touchpadResize() {
        int width = Gdx.graphics.getWidth();
        int tpwidth = width / 4;
        int height = Gdx.graphics.getHeight();
        touchpad.setSize(tpwidth, tpwidth);
        touchpad.setDeadzone(tpwidth / 5);
        touchpad.setPosition(width * 3 / 4, 0);
    }

    public void backToMenu() {
        this.dispose();
        main.setScreen(new MainMenu(main, level.getLevelName()));
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        /////// Handle timing events (like moving and events)
        if (game.updateTime()) { // not finished
            // Handle drawing
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if(menusActivated) {
                backgroundBatch.begin();
                for (int x = 0; x < playwindow.getWidthInPixels() + playwindow.resolution; x += playwindow.resolution) {
                    for (int y = 0; y < playwindow.getHeightInPixels() + playwindow.resolution; y += playwindow.resolution) {
                        background.draw(playwindow, backgroundBatch, x, y);
                    }
                }
                backgroundBatch.end();
            }

//            gameBatch.setColor(Color.WHITE); // reset to full brightness as destroyed by menu
            gameBatch.begin();

            gameBatch.flush();
            ScissorStack.pushScissors(playwindow.getScissors());

            game.draw(menusActivated);

            gameBatch.flush();
            ScissorStack.popScissors();


            gameBatch.end(); // must end before other layers

            if(menusActivated) {
                // draw a dark transparent rectangle to have some background for mini screen
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
                miniScreenBackground.setColor(0, 0, 0, 0.5f); // a little transparent
                miniScreenBackground.rect(Graphics.virtualToMiniX(playwindow, 0, 0) - playwindow.virtual2MiniResolution,
                        Graphics.virtualToMiniY(playwindow, 0, 0) - playwindow.virtual2MiniResolution,
                        Graphics.virtualToMiniX(playwindow, playwindow.getVPixelsLevelWidth() - 1, 0),
                        Graphics.virtualToMiniX(playwindow, playwindow.getVPixelsLevelHeight() - 1, 0));
                miniScreenBackground.end();

                // mini screen
                miniBatch.begin();
                game.drawMini(miniBatch);
                miniBatch.end();

                drawVisibleMarker();


                stageBatch.begin();
                // score etc.
                score.writeInteger(mcminos.getScore());
                powerScore.writeInteger(mcminos.getPowerDuration() >> game.timeResolutionExponent);
                umbrellaScore.writeInteger(mcminos.getUmbrellaDuration() >> game.timeResolutionExponent);
                toxicScore.writeInteger((mcminos.getPoisonDuration() + mcminos.getDrunkLevel()) >> game.timeResolutionExponent);
                framerateScore.writeInteger(Gdx.graphics.getFramesPerSecond());
                mirroredScore.writeChar(0, mcminos.isMirrored() ? 'M' : ' ');
                font.draw(stageBatch, scoreInfo, playwindow.resolution + 20, Gdx.graphics.getHeight() - 20);
                // add stage and menu
                stageBatch.end();

                toolbox.update(); // update toolbox based on inventory
                stage.draw();
                stage.act(delta);
            }
        } // else level is finished
        else {
            backToMenu();
        }

    }

    private void drawVisibleMarker() {

        // visible area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
        miniScreenBackground.setColor(255,128,0,0.5f); // orange transparent

        // These are up to 8 lines (4 corners) to draw
        int t = playwindow.virtual2MiniResolution / 2; // line thickness
        int t2 = t * 2;
        int thickness = 1+t;
        // compute the visible area lower left corner
        int x0 = Graphics.virtualToMiniX(playwindow,playwindow.windowVPixelXPos,0);
        int y0 = Graphics.virtualToMiniY(playwindow,playwindow.windowVPixelYPos,0);
        // compute the upper right corner
        int x1 = Graphics.virtualToMiniX(playwindow,((playwindow.windowVPixelXPos + playwindow.getVisibleWidthInVPixels() - 1) % playwindow.getVPixelsLevelWidth()),0);
        int y1 = Graphics.virtualToMiniY(playwindow,((playwindow.windowVPixelYPos + playwindow.getVisibleHeightInVPixels() - 1) % playwindow.getVPixelsLevelHeight()),0);
        // lower left corner of mini-screen
        int mx0 = Graphics.virtualToMiniX(playwindow,0,0);
        int my0 = Graphics.virtualToMiniY(playwindow,0,0);
        // upper right corner of mini-screen
        int mx1 = Graphics.virtualToMiniX(playwindow,playwindow.getVPixelsLevelWidth()-1,0);
        int my1 = Graphics.virtualToMiniY(playwindow,playwindow.getVPixelsLevelHeight()-1,0);

        if(x0<x1) { // normal, no split
            miniScreenBackground.rect( x0-t,y0-t,x1-x0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y1,x1-x0+t2+1,thickness );
        } else { // split necessary x1 < x0
            miniScreenBackground.rect( mx0-t,y0-t,x1-mx0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y0-t,mx1-x0+t2,thickness );
            miniScreenBackground.rect( mx0-t,y1,x1-mx0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y1,mx1-x0+t2,thickness );
        }
        if(y0<y1) { // normal, no split
            miniScreenBackground.rect( x0-t,y0+1,thickness,y1-y0-1 );
            miniScreenBackground.rect( x1,y0+1,thickness,y1-y0-1 );
        } else { // split necessary y1 < y0
            miniScreenBackground.rect( x0-t,my0+1,thickness,y1-my0-1 );
            miniScreenBackground.rect( x0-t,y0+1,thickness,my1-y0-1);
            miniScreenBackground.rect( x1,my0+1,thickness,y1-my0-1 );
            miniScreenBackground.rect( x1,y0+1,thickness,my1-y0-1 );
        }

        miniScreenBackground.end();

    }

    @Override
    public void resize(int width, int height) {
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0,0,width,height);
        backgroundBatch.setProjectionMatrix(matrix);
        miniBatch.setProjectionMatrix(matrix);
        stageBatch.setProjectionMatrix(matrix);
        miniScreenBackground.setProjectionMatrix(matrix);

        playwindow.resize(width, height);
        //menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        stage.getViewport().update(width, height, true);
        //toolboxTable.setSize(width / 3, height * 4 / 5);
        toolbox.resize();
        touchpadResize();
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
        //stageBatch.dispose();
        game.dispose();
        stage.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        mcminos.updateKeyDirections();
        if (!game.isToolboxActivated()) {
            if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                game.enableMovement();
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        mcminos.updateKeyDirections();
        toolbox.checkDoorKey( keycode );
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        switch (character) {
            case '+':
                zoomPlus();
                playwindow.setResolution(gameResolutionCounter);
                toolbox.resize();
                break;
            case '-':
                zoomMinus();
                playwindow.setResolution(gameResolutionCounter);
                toolbox.resize();
                break;
            case '1':
                toolbox.activate();
                toolbox.activateChocolate();
                break;
            case '2':
                toolbox.activate();
                toolbox.doorOpener();
                break;
            case '3':
                toolbox.activate();
                toolbox.activateBomb();
                break;
            case '4':
                toolbox.activate();
                toolbox.activateDynamite();
                break;
            case '5':
                toolbox.activate();
                toolbox.activateLandmine();
                break;
            case '6':
                toolbox.activate();
                toolbox.activateUmbrella();
                break;
            case '7':
                toolbox.activate();
                toolbox.activateMedicine();
                break;
            case '9':
                ScreenshotFactory.saveScreenshot();
                break;
            case '0':
                menusActivated = ! menusActivated;
                break;
            case 27: // Escape
            case 't':
            case 'T':
            case ' ':
                if (game.isToolboxActivated()) {
                    toolbox.deactivate();
                } else {
                    toolbox.activate();
                }
                break;
            case 'p':
            case 'P':
                // TODO: check if this enables to cheat
                game.disableMovement();
                break;
        }
        return false;
    }

    public void zoomPlus() {
        gameResolutionCounter--;
        if (gameResolutionCounter < 0) gameResolutionCounter = 0;
        playwindow.setResolution(gameResolutionCounter);
        toolbox.resize();
    }

    public void zoomMinus() {
        gameResolutionCounter++;
        if (gameResolutionCounter > Entities.resolutionList.length - 1)
            gameResolutionCounter = Entities.resolutionList.length - 1;
        playwindow.setResolution(gameResolutionCounter);
        toolbox.resize();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!game.isToolboxActivated()) { // just pan in this case -> see there
            // TODO: consider only first button/finger
            if (button > 0) return false;
            if (!mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                game.enableMovement();
            }
            int x = windowToGameX(screenX);
            int y = windowToGameY(screenY);
            mcminos.setDestination(x, y);
            return false; // needs to be evtl. dealt with at drag
        }
        return false;
    }

    public int windowToGame(int screenCoordinate, int vpixelsize, int projection, int vpixelpos, boolean scroll) {
        // map to game coordinates
        int y = Util.shiftLeftLogical(screenCoordinate - projection, (PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent))
                + vpixelpos - (PlayWindow.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        if (scroll) {
            if (y >= vpixelsize)
                y -= vpixelsize;
            if (y <= -(playwindow.virtualBlockResolution >> 1))
                y += vpixelsize;
        } else {
            if (y >= vpixelsize - PlayWindow.virtualBlockResolution)
                y = vpixelsize - PlayWindow.virtualBlockResolution - 1;
            if (y <= 0) y = 0;
        }
        return y;
    }

    public int windowToGameY(int screenY) {
        return windowToGame(Gdx.graphics.getHeight() - screenY, playwindow.getVPixelsLevelHeight(),
                playwindow.getProjectionY(), playwindow.windowVPixelYPos, level.getScrollY());
    }

    public int windowToGameX(int screenX) {
        return windowToGame(screenX, playwindow.getVPixelsLevelWidth(),
                playwindow.getProjectionX(), playwindow.windowVPixelXPos, level.getScrollX());
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return touchDown(screenX, screenY, pointer, 0); // Forward to touch
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount > 0) {
            zoomMinus();
            return true;
        } else if (amount < 0) {
            zoomPlus();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return touchDown((int) x, (int) y, pointer, button); // Forward to touch
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (button > 0 || count > 1) {
            if (game.isToolboxActivated()) {
                toolbox.deactivate();
            } else {
                toolbox.activate();
            }
            return true;
        }
        return touchDown(x, y, 0, button);
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        /*// quick hack to allow touch events
        if(velocityX < 0) {
            keyTyped('-');
            return true;
        }
        else if(velocityX>0) {
            keyTyped('+');
            return true;
        }
        return true;*/
        return false;
    }

    @Override
    public boolean pan(float screenX, float screenY, float deltaX, float deltaY) {
        if (playwindow.game.isToolboxActivated()) {
            int dxi = Util.shiftLeftLogical((int) deltaX, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);
            int dyi = Util.shiftLeftLogical((int) deltaY, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);
            playwindow.windowVPixelXPos = (playwindow.windowVPixelXPos + playwindow.getVPixelsLevelWidth() - dxi) % playwindow.getVPixelsLevelWidth();
            playwindow.windowVPixelYPos = (playwindow.windowVPixelYPos + playwindow.getVPixelsLevelHeight() + dyi) % playwindow.getVPixelsLevelHeight();
            return true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (game.getGameTime() - lastZoomTime > 500) { // ignore some events
            if (initialDistance > distance + playwindow.visibleHeightInPixels / 4) {
                zoomMinus();
            } else if (initialDistance < distance - playwindow.visibleHeightInPixels / 4) {
                zoomPlus();
            }
            lastZoomTime = game.getGameTime();
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    public int getGameResolutionCounter() {
        return gameResolutionCounter;
    }
}
