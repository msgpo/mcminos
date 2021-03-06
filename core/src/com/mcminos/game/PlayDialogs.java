package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Here are all the dialogs which could open when in the PlayScreen
 * <p/>
 * Created by ulno on 17.12.15.
 */
public class PlayDialogs {

    private final Main main;
    private final Play play;
    private final Game game;
    private final Level level;
    private final Audio audio;
    private final McMinos mcminos;
    private final PlayWindow playwindow;
    private final Stage stage;
    private final Preferences preferences;

    private Skin levelSkin;
    private Skin menuSkin;

    private Table dialog = null;
    private ScrollPane dialogMainScroll = null;

    private final LevelBlock doorBlocks[] = new LevelBlock[4];
    private Label pillLabel;
    private SegmentString pillLabelText;
    private Label rockmeLabel;
    private SegmentString rockmeLabelText;
    private int closeTimer = 0;

    private HotSpot hotSpotRoot;

    public PlayDialogs(Play play) {
        this.play = play;
        this.main = play.getMain();
        this.preferences = main.getPreferences();
        this.stage = play.getStage();
        this.game = play.getGame();

        this.playwindow = play.getPlayWindow();
        this.audio = game.getAudio();
        this.level = game.getLevel();
        this.mcminos = game.getMcMinos();

        selectSkins(preferences.getSymbolResolution());
    }

    private void selectSkins(int res) {
        menuSkin = main.getMenuSkin(res);
        levelSkin = main.getLevelSkin(res / 2);
    }

    public void close() {
        closeTimer = 0;
        if (dialog != null) dialog.remove();
        dialog = null;
    }


    public Table openText( CharSequence title, CharSequence body ) {
        play.hideHotSpot();
        hotSpotRoot = null;
        int res = play.getSymbolResolution();
        Skin writingSkin = main.getMenuSkin(res / 2);
        Skin menuSkin = main.getMenuSkin(res);
        Table rootTable = new Table();
        Group rootGroup = new Group();
        rootTable.add(rootGroup);
        rootTable.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        rootTable.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        int rootWidth = Gdx.graphics.getWidth() * 4 / 5;
        int rootHeight = Gdx.graphics.getHeight() * 4 / 5;
        rootTable.setSize(rootWidth, rootHeight);
        rootGroup.setSize(rootWidth, rootHeight);
        rootTable.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);

        Table mainTable = new Table();
        Table topRow = new Table();
        Label titleLabel = new Label(title, menuSkin);
        titleLabel.setWrap(true);
        titleLabel.setAlignment(Align.left);
        topRow.add(titleLabel).top().left().fillX().expandX();
        mainTable.add(topRow).fillX().expandX().padLeft(res/2).padRight(res+res/4).row();
        Table bodyTable = new Table();
        Label bodyLabel = new Label(body, writingSkin);
        bodyLabel.setWrap(true);
        bodyTable.add(bodyLabel).fillX().expandX().row();
        mainTable.add(bodyTable).minHeight(res*2).fill().expand().pad(res/2);
        ScrollPane mainScroll = new ScrollPane(mainTable);
        mainScroll.setSize(rootWidth, rootHeight);
        rootGroup.addActor(mainScroll);
        SymbolButton closeButton = new SymbolButton(res,Entities.toolbox_abort.getTexture(res,0));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                close();
            }
        });
        //       topRow.add(closeButton).right(); better to have th ebutton fixed in group on top
        closeButton.getCell().setPosition(rootWidth-res/8,rootHeight-res/8,Align.topRight);
        rootGroup.addActor(closeButton.getCell());
        open(rootTable,mainScroll);
        return(bodyTable);
    }

    public void openLevelStory() {
        LevelConfig lc = level.getLevelConfig();

        Table dialogTable = openText(lc.getTitle(preferences.getLanguage()), lc.getBody(preferences.getLanguage()));

        ///// Fill statistics
        int res = play.getSymbolResolution();
        Skin writingSkin = main.getMenuSkin(res / 2);

        Table statisticsTable = new Table(menuSkin);
        dialogTable.row();
        dialogTable.add(statisticsTable).fillX().expandX().padTop(res / 2);

        // Remaining pills and rockmes (todos)
        Table todosTable = new Table();
        statisticsTable.add(todosTable).fillX().expandX().row();
        todosTable.add(new Image(Entities.pills_pill_default.getTexture(res / 2, 0))).left();
        pillLabel = new Label(": " + level.getPillsNumber() + ", ", writingSkin);
        todosTable.add(pillLabel).left();
        todosTable.add(new Image(Entities.extras_rock_me.getTexture(res / 2, 0))).left();
        rockmeLabel = new Label(": " + level.getRockmesNumber(), writingSkin);
        todosTable.add(rockmeLabel).left().fillX().expandX();

        statisticsTable.add(new Label("Level: " + level.getLevelConfig().getName(), writingSkin)).left().fillX().expandX().padTop(res / 2).row();
        statisticsTable.add(new Label("Level author: " + level.getLevelConfig().getAuthor(), writingSkin)).left().fillX().expandX().row();
        // Zoomlevel + Resolution
        //statisticsTable.add(new Label(new StringBuilder("Density: ").append((int) (Gdx.graphics.getDensity() * 160)), writingSkin)).left().fillX().expandX().row();
        statisticsTable.add(new Label(
                new StringBuilder("Sprite size: ").append(playwindow.resolution)
                        .append(", symbol size: ").append(play.getSymbolResolution())
                        .append(", minimap sprite size: ").append(playwindow.virtual2MiniResolution)
                , writingSkin)).left().fillX().expandX().padTop(res / 2).row();
        statisticsTable.add(new Label(
                new StringBuilder("Screen: ").append(Gdx.graphics.getWidth()).append("x").append(Gdx.graphics.getHeight())
                        .append(", FPS: ").append((int) (Gdx.graphics.getFramesPerSecond()))
                , writingSkin)).left().fillX().expandX().row();
    }


    private SymbolButton soundButton;
    private SymbolButton musicButton;
    private SymbolButton touchpadButton;
    public void openGameMenu() {
        hotSpotRoot = null;
        int res = play.getSymbolResolution();
        int padSize = res / 16;
        Skin menuSkin = main.getMenuSkin(res);
        Table thisDialog = new Table();
        thisDialog.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        thisDialog.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        thisDialog.setSize(Math.min(Gdx.graphics.getWidth(), 7*res + 11 * padSize),
                Math.min(Gdx.graphics.getHeight(), 2*res + 4*padSize) );
        thisDialog.setPosition( res + padSize, Gdx.graphics.getHeight() - thisDialog.getHeight() - res );

        // Basic layout
        Table rowActionsTable = new Table(menuSkin);
        rowActionsTable.setHeight(res);
        ScrollPane rowActions = new ScrollPane(rowActionsTable);

        Table rowGamePrefsTable = new Table(menuSkin);
        rowGamePrefsTable.setHeight(res);
        ScrollPane rowGamePrefs = new ScrollPane(rowGamePrefsTable);

        thisDialog.add(rowActions).expandX().fillX().pad(padSize).top().minHeight(res).row();
        thisDialog.add(rowGamePrefs).expandX().fillX().pad(padSize).minHeight(res).row();

        ///// Fill game prefs row
        soundButton = new SymbolButton(res, audio.getSound() ?
                Entities.menu_symbol_sound_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_sound_off.getTexture(play.getSymbolResolution(), 0));
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                toggleSound();
            }
        });
        rowGamePrefsTable.add(soundButton.getCell()).prefSize(res, res).padRight(padSize);
        hotSpotRoot = new HotSpot(soundButton.getCell(), rowGamePrefs, 1);
        HotSpot hs = hotSpotRoot;

        musicButton = new SymbolButton(res, audio.getMusic() ?
                Entities.menu_symbol_music_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_music_off.getTexture(play.getSymbolResolution(), 0));
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                toggleMusic();
            }
        });
        rowGamePrefsTable.add(musicButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(musicButton.getCell(), rowGamePrefs, 2);

        touchpadButton = new SymbolButton(res,play.isTouchpadActive() ?
                Entities.menu_symbol_touchpad_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_touchpad_off.getTexture(play.getSymbolResolution(), 0));
        touchpadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                toggleTouchpad();
            }
        });
        rowGamePrefsTable.add(touchpadButton.getCell()).prefSize(res, res).padRight(padSize * 2);
        hs = hs.getCreateRight(touchpadButton.getCell(), rowGamePrefs, 3);

        SymbolButton plusButton = new SymbolButton(res, Entities.menu_symbol_zoom_in.getTexture(res, 0));
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                zoomPlusGame();
            }
        });
        rowGamePrefsTable.add(plusButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(plusButton.getCell(), rowGamePrefs, 4);


        SymbolButton minusButton = new SymbolButton(res, Entities.menu_symbol_zoom_out.getTexture(res, 0));
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                zoomMinusGame();
            }
        });
        rowGamePrefsTable.add(minusButton.getCell()).prefSize(res, res).padRight(padSize * 2);
        hs = hs.getCreateRight(minusButton.getCell(), rowGamePrefs, 5);

        SymbolButton symbolPlusButton = new SymbolButton(res, Entities.menu_symbol_toolbar_zoom_in.getTexture(res, 0));
        symbolPlusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                zoomPlusSymbol();
            } 
        });
        rowGamePrefsTable.add(symbolPlusButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(symbolPlusButton.getCell(), rowGamePrefs, 6);

        SymbolButton symbolMinusButton = new SymbolButton(res, Entities.menu_symbol_toolbar_zoom_out.getTexture(res, 0));
        symbolMinusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                zoomMinusSymbol();
            } 
        });
        rowGamePrefsTable.add(symbolMinusButton.getCell()).prefSize(res, res);
        hs.getCreateRight(symbolMinusButton.getCell(), rowGamePrefs, 7);

        // action row
        //        Button saveButton = new TextButton("Save", writingSkin);
        SymbolButton saveButton = new SymbolButton(res,Entities.menu_symbol_game_save.getTexture(res, 0));
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                saveGame();
            }
        });
        rowActionsTable.add(saveButton.getCell()).prefSize(res, res).padRight(padSize);
        HotSpot lowerRowHS = hotSpotRoot;
        hs = lowerRowHS.getCreateUp(saveButton.getCell(), rowGamePrefs, 51);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton langButton = new SymbolButton(res, preferences.languageGfx().getTexture(res,0));
        langButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                changeLanguage();
            }
        });
        rowActionsTable.add(langButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(langButton.getCell(), rowGamePrefs, 52);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton infoButton = new SymbolButton(res,Entities.menu_button_info.getTexture(res, 0));
        infoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                openLevelStory();
            }
        });
        rowActionsTable.add(infoButton.getCell()).prefSize(res, res).padRight(padSize * 2);
        hs = hs.getCreateRight(infoButton.getCell(), rowGamePrefs, 53);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton restartButton = new SymbolButton(res,Entities.menu_symbol_restart.getTexture(res, 0));
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                restartLevel();
            }
        });
        rowActionsTable.add(restartButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(restartButton.getCell(), rowGamePrefs, 54);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton leaveButton = new SymbolButton(res,Entities.menu_symbol_stop.getTexture(res, 0));
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                leaveGame();
            }
        });
        rowActionsTable.add(leaveButton.getCell()).prefSize(res, res).padRight(padSize * 2);
        hs = hs.getCreateRight(leaveButton.getCell(), rowGamePrefs, 55);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton pauseButton = new SymbolButton(res, Entities.menu_symbol_pause.getTexture(res, 0));
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                close();
            }
        });
        rowActionsTable.add(pauseButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(pauseButton.getCell(), rowGamePrefs, 56);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);
        lowerRowHS = lowerRowHS.getRight();

        SymbolButton continueButton = new SymbolButton(res, Entities.menu_symbol_play.getTexture(res, 0));
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.hideHotSpot();
                play.pauseOff();
                //super.clicked(event, x, y);
            }
        });
        rowActionsTable.add(continueButton.getCell()).prefSize(res, res).maxSize(res);
        hs = hs.getCreateRight(continueButton.getCell(), rowGamePrefs, 57);
        lowerRowHS.setUp(hs);
        hs.setDown(lowerRowHS);

        hotSpotRoot = hotSpotRoot.getUp();

        open( thisDialog );
    }

    private void leaveGame() {
        close();
        play.backToMenu();
    }

    private void restartLevel() {
        close();
        mcminos.kill("skullkill", Entities.mcminos_dying, true);
        play.pauseOff();
    }

    private void changeLanguage() {
        close();
        preferences.nextLanguage();
        openGameMenu();
    }

    private void saveGame() {
        play.getGame().saveGame(1); // TODO: allow several game-saves
        openSavedGameConfirm();
    }

    private void zoomMinusSymbol() {
        close();
        play.decreaseSymbolResolution();
        openGameMenu(); // TODO: check if this leaks too much memory
    }

    private void zoomPlusSymbol() {
        close();
        play.increaseSymbolResolution();
        openGameMenu(); // TODO: check if this leaks too much memory
    }

    private void zoomMinusGame() {
        close();
        play.zoomMinus();
        openGameMenu(); // TODO: check if this leaks too much memory
    }

    private void zoomPlusGame() {
        close();
        play.zoomPlus();
        openGameMenu(); // TODO: check if this leaks too much memory
    }

    private void toggleTouchpad() {
        play.toggleTouchpad();
        touchpadButton.setSymbol(play.isTouchpadActive() ?
                Entities.menu_symbol_touchpad_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_touchpad_off.getTexture(play.getSymbolResolution(), 0));
    }

    private void toggleMusic() {
        preferences.toggleMusic();
        musicButton.setSymbol(audio.getMusic() ?
                Entities.menu_symbol_music_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_music_off.getTexture(play.getSymbolResolution(), 0));
    }

    private void toggleSound() {
        preferences.toggleSound();
        soundButton.setSymbol(audio.getSound() ?
                Entities.menu_symbol_sound_on.getTexture(play.getSymbolResolution(), 0)
                : Entities.menu_symbol_sound_off.getTexture(play.getSymbolResolution(), 0));
    }

    public HotSpot getHotSpotRoot() {
        return hotSpotRoot;
    }

    private void openSavedGameConfirm() {
        close();

        int res = play.getSymbolResolution();
        Skin writingSkin = main.getMenuSkin(res / 2);
        Skin menuSkin = main.getMenuSkin(res);
        Table thisDialog = new Table();
        int padSize = res / 16;

        thisDialog.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        thisDialog.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        thisDialog.setSize(Math.min(Gdx.graphics.getWidth(), res + 2 * padSize),
                Math.min(Gdx.graphics.getHeight(), res + 2 * padSize) );
        thisDialog.setPosition( res + padSize, Gdx.graphics.getHeight() - thisDialog.getHeight() - play.getGameResolution() );


        // TODO: check that it really was successfully saved
        SymbolButton sb = new SymbolButton(res,Entities.menu_symbol_game_save.getTexture(res,0));
        Group g = sb.getCell();
        g.addActor(new Image(Entities.menu_symbol_checked.getTexture(res,0)));
        thisDialog.add(g).pad(padSize);
        thisDialog.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        open(thisDialog);
        closeTimer = 180; // 3 seconds: TODO: do not hardcode here
    }

    private void open(Table dialog, ScrollPane mainScroll) {
        close(); // old one needs to be gone
        play.pause(); // make sure game is paused
        this.dialog = dialog;
        this.dialogMainScroll = mainScroll;
        stage.addActor(dialog);
    }

    private void open(Table dialog) {
        open(dialog, null);
    }

    private LevelBlock checkDoor(LevelBlock lb1, LevelBlock lb2) {
        if (lb1 == null) return null;
        if (lb1.hasDoor()) return lb1;
        if (lb2 == null) return null;
        if (lb2.hasDoor()) return lb2;
        return null;
    }

    private void resetDoorBlocks() {
        for (int i = 0; i < doorBlocks.length; i++)
            doorBlocks[i] = null;
    }

    public void openDoorOpener() {
        if (mcminos.getKeys() <= 0) {
            audio.soundPlay("error");
            return;
        }
        int mvx = mcminos.getVX() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollX()) mvx = mvx % level.getVPixelsWidth();
        int mvy = mcminos.getVY() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollY()) mvy = mvy % level.getVPixelsHeight();

/*         // get rounded mcminos block
         LevelBlock lb = level.get( mvx / PlayWindow.virtualBlockResolution, mvy / PlayWindow.virtualBlockResolution);
allows cheating */
        LevelBlock lb = mcminos.getLevelBlock();

        // find doors
        int doors = 0;
        int doorsCount = 0;
        doorBlocks[0] = checkDoor(lb.up(), lb.up2());
        if (doorBlocks[0] != null) { // found door up
            doors += Mover.UP;
            doorsCount++;
        }
        doorBlocks[1] = checkDoor(lb.right(), lb.right2());
        if (doorBlocks[1] != null) { // found door right
            doors += Mover.RIGHT;
            doorsCount++;
        }
        doorBlocks[2] = checkDoor(lb.down(), lb.down2());
        if (doorBlocks[2] != null) { // found door down
            doors += Mover.DOWN;
            doorsCount++;
        }
        doorBlocks[3] = checkDoor(lb.left(), lb.left2());
        if (doorBlocks[3] != null) { // found door left
            doors += Mover.LEFT;
            doorsCount++;
        }
        /*
        // this allows mcminos to hide on a door
        doorBlocks[4] = checkDoor(lb,null);
        if (doorBlocks[4] != null) { // found door on field
            doors += Mover.ALL + 1;
            doorsCount++;
        }*/
        if (doorsCount > 0) { // found doors
            if (doorsCount == 1) { // if there is only one, it can just be opened
                toggleDoor(doorBlocks[Util.log2binary(doors)]);
            } else {
                // create dialog to allow choice
                Table outerTable = new Table(menuSkin);
                int res = playwindow.resolution;
                outerTable.setSize(res * 3, res * 3);
                // get mcminos center, to center dialog
                int mx = playwindow.vPixelToScreenX(mvx) + playwindow.getProjectionX();
                int my = playwindow.vPixelToScreenY(mvy) + playwindow.getProjectionY();
                outerTable.setPosition(mx, my, Align.center);
                Group innerGroup = new Group();
                innerGroup.setSize(res * 3, res * 3);
                outerTable.add(innerGroup).fill().expand();
                Table bgTable = new Table(menuSkin);
                bgTable.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
                bgTable.setSize(res * 3, res * 3);
                bgTable.setColor(new Color(1, 1, 1, 0.5f)); // very transparent
                innerGroup.addActor(bgTable);
                Table buttonTable = new Table(menuSkin);
                buttonTable.setSize(res * 3, res * 3);
                innerGroup.addActor(buttonTable);
                buttonTable.setColor(new Color(1, 1, 1, 1)); // not transparent
                buttonTable.defaults().fill().expand();
                Group empty = new Group();
                empty.setSize(0, 0);
                class KeyButton extends ClickListener {
                    Image keyImage;
                    LevelBlock doorBlock;

                    KeyButton(int res, LevelBlock doorBlock, Graphics gfx) {
                        keyImage = new Image(gfx.getTexture(res, 0));
//                        keyImage.setColor(new Color(1, 1, 1, 1));
                        this.doorBlock = doorBlock;
                        keyImage.addListener(this);
                    }

                    Image getImage() {
                        return keyImage;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        toggleDoor(doorBlock);
                        close();
                        //super.clicked(event, x, y);
                    }
                }
                // line 1
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.UP) > 0)
                    buttonTable.add(new KeyButton(res, doorBlocks[0], Entities.toolbox_key_option_up).getImage());
                else buttonTable.add(empty);
                Image exitImage = new Image(Entities.toolbox_abort.getTexture(res, 0));
                buttonTable.add(exitImage).row();
                exitImage.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        play.pauseOff();
                        //super.clicked(event, x, y);
                    }
                });
                // line 2
                if ((doors & Mover.LEFT) > 0)
                    buttonTable.add(new KeyButton(res, doorBlocks[3], Entities.toolbox_key_option_left).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.RIGHT) > 0)
                    buttonTable.add(new KeyButton(res, doorBlocks[1], Entities.toolbox_key_option_right).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.row();
                // line 3
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.DOWN) > 0)
                    buttonTable.add(new KeyButton(res, doorBlocks[2], Entities.toolbox_key_option_down).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2).row();

                open(outerTable);
            }
        } else { // no door found
            audio.soundPlay("error");
        }
    }

    public void toggleDoor(LevelBlock lb) {
        if (lb.hasDoor()) {
            if (!lb.hasRock()) { // if the door is not blocked by rock
                if (mcminos.hasKey()) {
                    mcminos.decreaseKeys();
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        audio.soundPlay("rums");
                    else audio.soundPlay("quietsch");
                    play.pauseOff(); // close table
                    resetDoorBlocks(); // don't use it again
                }
            }
        }
    }

    public void checkDoorKey(int dirs) {
        if (dialog != null) {
            LevelBlock lb = null;
            if ((dirs & Mover.UP) > 0) {
                lb = doorBlocks[0];
            } else if ((dirs & Mover.RIGHT) > 0) {
                lb = doorBlocks[1];
            } else if ((dirs & Mover.DOWN) > 0) {
                lb = doorBlocks[2];
            } else if ((dirs & Mover.LEFT) > 0) {
                lb = doorBlocks[3];
            }
            if (lb != null) {
                toggleDoor(lb);
                close();
            }
        }

    }

    public boolean active() {
        return dialog != null;
    }

    public void updateTimer() {
        if(closeTimer > 0) {
            closeTimer --;
            if(closeTimer == 0) {
                close();
            }
        }
    }

    public void triggerAction(int hint) {
        switch( hint ) {
            case 1:
                toggleSound();
                break;
            case 2:
                toggleMusic();
                break;
            case 3:
                toggleTouchpad();
                break;
            case 4:
                zoomPlusGame();
                break;
            case 5:
                zoomMinusGame();
                break;
            case 6:
                zoomPlusSymbol();
                play.setHotSpotSelected(
                        hotSpotRoot.getDown()
                                .getRight().getRight().getRight()
                                .getRight().getRight()
                );
                break;
            case 7:
                zoomMinusSymbol();
                play.setHotSpotSelected(
                        hotSpotRoot.getDown()
                                .getRight().getRight().getRight()
                                .getRight().getRight().getRight()
                );
                break;
            case 51:
                play.hideHotSpot();
                saveGame();
                break;
            case 52:
                changeLanguage();
                break;
            case 53:
                play.hideHotSpot();
                openLevelStory();
                break;
            case 54:
                play.hideHotSpot();
                restartLevel();
                break;
            case 55:
                play.hideHotSpot();
                leaveGame();
                break;
            case 56:
                //play.setHotSpotSettings();
                play.hideHotSpot();
                close();
                break;
            case 57:
                play.hideHotSpot();
                play.pauseOff();
                break;

        }
    }

    public static float scrollDelta = 0.1f;
    public void evaluateDirections(int dirs) {
        if(dialogMainScroll != null) {
            float x = dialogMainScroll.getVisualScrollPercentX();
            float y = dialogMainScroll.getVisualScrollPercentY();


            if ((dirs & Mover.UP) > 0)
                dialogMainScroll.setScrollPercentY(y - scrollDelta);
            else if ((dirs & Mover.DOWN) > 0)
                dialogMainScroll.setScrollPercentY(y + scrollDelta);

            if ((dirs & Mover.RIGHT) > 0)
                dialogMainScroll.setScrollPercentX(x + scrollDelta);
            else if ((dirs & Mover.LEFT) > 0)
                dialogMainScroll.setScrollPercentX(x - scrollDelta);

        }
    }
}
