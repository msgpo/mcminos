package com.mcminos.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 17.08.15.
 *
 * Actual objects of a Level. Walls, Main, Doors, Ghosts and pills are
 * created here. the corresponding graphics are in Graphics
 */
public class LevelObject implements  Comparable<LevelObject>, Json.Serializable  {

    public final static int maxzIndex=10000;
    private int x; // windowVPixelXPos-Position in level blocks * virtualBlockResolution
    private int y; // windowVPixelYPos-Position in level blocks * virtualBlockResolution
    private LevelBlock levelBlock = null; // currently associated LevelBlock
    private Graphics gfx; // actual Graphics for the object
    private int zIndex = maxzIndex; // by default it is too high
    private Mover mover = null; // backlink
    private Level level = null;
    private int holeLevel;
    public static final int maxHoleLevel = 4; // maximum open
    private int animDelta = 0; // how much offset for the animation
    private Types type;
    private DoorTypes doorType = DoorTypes.None;
    private int initOneWayType = -1;

    @Override
    public void write(Json json) {
        json.writeValue("x",x);
        json.writeValue("y",y);
        if(gfx != null)
            json.writeValue("g",gfx.getAllGraphicsIndex());
        else
            json.writeValue("g",-1);
        // TODO: check why it does not save a mover here, when mcminos was controlled by keyboard or resave
        json.writeValue( "m", mover, Mover.class ); // write class name if not plain Mover
        json.writeValue("lb", levelBlock);
        json.writeValue("z", zIndex);
        json.writeValue("hl", holeLevel);
        json.writeValue("ad", animDelta);
        json.writeValue("t", type);
        json.writeValue("dt", doorType);
        json.writeValue("ot", levelBlock.getOneWayType());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        x = json.readValue("x",Integer.class,jsonData);
        y = json.readValue("y",Integer.class,jsonData);
        gfx = Graphics.getByIndex( json.readValue("g",Integer.class,jsonData));
        mover = json.readValue("m",Mover.class,jsonData);
        levelBlock = json.readValue("lb", LevelBlock.class, jsonData);
        zIndex = json.readValue("z", Integer.class, jsonData);
        holeLevel = json.readValue("hl", Integer.class, jsonData);
        animDelta = json.readValue("ad", Integer.class, jsonData);
        type = json.readValue("t", Types.class, jsonData);
        doorType = json.readValue("dt", DoorTypes.class, jsonData);
        initOneWayType = json.readValue("ot", Integer.class, jsonData);
    }

    /**
     * make sure levelblock is initialized by coordinates
     * @param game
     */
    public void initAfterJsonLoad( Game game ) {
        Level level = game.getLevel();
        this.level = level;
        if(levelBlock != null) {
            setLevelBlock( level.get(levelBlock.getX(), levelBlock.getY()) );
        }
        if(mover != null)
            mover.initAfterJsonLoad(game,this);
    }

    public int getInitOneWayType() {
        return initOneWayType;
    }

    public enum Types {Unspecified, Power1, Power2, Power3,
        IndestructableWall, InvisibleWall, Rockme, Live, Letter,
        Skull, Bomb, Dynamite, Rock, Pill, Castle, McMinos, Wall, Background, Key, Umbrella,
        DoorClosed, DoorOpened, SpeedUpField, SpeedDownField, WarpHole, KillAllField, OneWay,
        Chocolate, LandMine, LandMineActive, LandMineExplosion, BombFused, DynamiteExplosion,
        BombExplosion, DestroyedWall, Ghost1, Ghost2, Ghost3, Ghost4, KillAllPill, Exit, Bonus1, Bonus2, Bonus3, Whisky, Mirror, Poison, Medicine, SkullField, Destination, DynamiteFused, McMinosDying, McMinosFalling, McMinosWinning, Hole};
    public enum DoorTypes {None, HorizontalOpened,HorizontalClosed, VerticalOpened,VerticalClosed};

    private void construct(LevelBlock levelBlock, int zIndex, Types type) {
        level = levelBlock.getLevel();
        // does not exist here playwindow = game.getPlayWindow();
        x = levelBlock.getX() << PlayWindow.virtualBlockResolutionExponent;
        y = levelBlock.getY() << PlayWindow.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        this.type = type;
        setLevelBlock(levelBlock);
        level.addToAllLevelObjects(this);
    }

    /**
     * This is called when re-constructed from json-save
     */
    LevelObject() {

    }

    /**
     *all.
     * @param x in block coordinates
     * @param y in block coordinates (movable objects can have fraction as coordinate)
     * @param zIndex need to know zIndex to allow correct drawing order later
     */
    LevelObject(Level level, int x, int y, int zIndex, Types type) {
        LevelBlock lb = level.get(x,y);
        construct(lb, zIndex, type);
    }

    public LevelObject(LevelBlock levelBlock, Graphics graphics, Types type) {
        construct(levelBlock, graphics.getzIndex(), type);
        setGfx(graphics);
    }

    /*LevelObject(int windowVPixelXPos, int windowVPixelYPos) {
        LevelObject(windowVPixelXPos,windowVPixelYPos,maxzIndex);
    }
*/
    public void setGfx(Graphics gfx) {
        this.gfx = gfx;
    }

    public void draw(PlayWindow playwindow) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.draw(playwindow,level,x,y,animDelta);
    }

    public void drawMini(PlayWindow playwindow, SpriteBatch batch) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.drawMini(playwindow,level,batch,x,y,animDelta);
    }

    @Override
    public int compareTo(LevelObject lo) {
        return  zIndex - lo.zIndex;
    }

    public LevelBlock moveTo(int vpx, int vpy, LevelBlock headingTo ) {
        LevelBlock from = levelBlock;
        // check and eventually fix coordinates
        // if(Game.getScrollX()) { always allow
        if (vpx < 0) vpx += level.getWidth() << PlayWindow.virtualBlockResolutionExponent;
        if (vpx >= level.getWidth() << PlayWindow.virtualBlockResolutionExponent)
            vpx -= level.getWidth() << PlayWindow.virtualBlockResolutionExponent;
        //}
        //if(Game.getScrollY()) {
        if (vpy < 0) vpy += level.getHeight() << PlayWindow.virtualBlockResolutionExponent;
        if (vpy >= level.getHeight() << PlayWindow.virtualBlockResolutionExponent)
            vpy -= level.getHeight() << PlayWindow.virtualBlockResolutionExponent;
        //}

        // needs to be updated to check for collisions via associations
        if (from != headingTo) {
            if(from != null)
                from.remove(this);
            /* deals already with rockmes // check if rock and update rockme counters
            if (type == LevelObject.Types.Rock) {
                // Check, if we are on a rockme
                if (from.isRockme()) level.increaseRockmes();
            }
            // happens in setLevelBlock: headingTo.putMoveable(this);
            */
        }
        setLevelBlock( headingTo ); // todo: might be not totally correct for destination
        setXY(vpx,vpy);
        return levelBlock;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * assign a matching LevelBlock based on the current coordinates
     */
    public void assignLevelBlock(PlayWindow playwindow) {
        setLevelBlock( level.getLevelBlockFromVPixel(x, y) );
    }

    public boolean hasGfx() {
        return gfx != null;
    }

    public void setMover(Mover mover) {
        this.mover = mover;
    }

    public Mover getMover() {
        return mover;
    }

    /**
     * make sure to remove yourself from list
     * also cleans itself from associated levelblock
     */
    public void dispose() {
        if(mover!=null) {
            mover.remove(this);
        }
        if(levelBlock != null ) { // has levelBlock
            levelBlock.remove(this);
            levelBlock = null;
        }
        level.removeFromAllLevelObjects(this);
    }

    public boolean isIndestructable() {
        return type == Types.IndestructableWall;
    }

    public boolean isInvisible() {
        return type == Types.InvisibleWall;
    }

    public boolean isRockme() {
        return type == Types.Rockme;
    }

    public void setHoleLevel(int holeLevel) {
        this.holeLevel = holeLevel;
        // set gfx
        Graphics[] holes =
                new Graphics[]{Entities.holes_0, Entities.holes_1,
                        Entities.holes_2, Entities.holes_3, Entities.holes_4};
        this.setGfx(holes[holeLevel]);
    }

    public boolean increaseHole(Audio audio) {
        holeLevel ++;
        if(holeLevel > maxHoleLevel) {
            holeLevel = maxHoleLevel;
            return false;
        }
        // it got bigger
        audio.soundPlay("holegrow");
        setHoleLevel(holeLevel);
        return true;
    }

    public int getVX() {
        return x;
    }
    public int getVY() {
        return y;
    }

    public void setDoorType(DoorTypes doorType) {
        this.doorType = doorType;
    }

    public DoorTypes getDoorType() {
        return doorType;
    }

    public Types getType() {
        return type;
    }

    public LevelBlock getLevelBlock() {
        return levelBlock;
    }

    void animationStartNow(Game game) {
        int len = gfx.getAnimationFramesLength();

        animDelta = len - (int)(game.getTimerFrame() % (long)len); // TODO: check if we need to use Timer or Animationframe here
    }

    void animationStartRandom(Game game) {
        animDelta = game.random(gfx.getAnimationFramesLength());
    }

    public boolean fullOnBlock() {
        return (getVX() % PlayWindow.virtualBlockResolution  == 0) && (getVY() % PlayWindow.virtualBlockResolution == 0);
    }

    public boolean holeIsMax() {
        return holeLevel == maxHoleLevel;
    }

    public void setOneWayGfx(int i) {
        LevelObject lo = this;
        switch (i) {
            case 0:
                lo.setGfx(Entities.arrows_static_up);
                break;
            case 1:
                lo.setGfx(Entities.arrows_static_right);
                break;
            case 2:
                lo.setGfx(Entities.arrows_static_down);
                break;
            case 3:
                lo.setGfx(Entities.arrows_static_left);
                break;
            case 4:
                lo.setGfx(Entities.arrows_rotatable_up);
                break;
            case 5:
                lo.setGfx(Entities.arrows_rotatable_right);
                break;
            case 6:
                lo.setGfx(Entities.arrows_rotatable_down);
                break;
            case 7:
                lo.setGfx(Entities.arrows_rotatable_left);
                break;
        }

    }

    public void setLevelBlock(LevelBlock newBlock) {
        if(newBlock != levelBlock) { // new levelBlock
            if (levelBlock != null) { // it has one assigned, so it needs to be removed
                levelBlock.remove(this);
            }
            // now we know that it has nothing assigned
            levelBlock = newBlock;
            if(newBlock != null) {
                levelBlock.add(this);
            }
        }
    }

    public int getGhostNr() {
        if(type.ordinal() >= Types.Ghost1.ordinal() && type.ordinal() <= Types.Ghost4.ordinal() ) {
            return type.ordinal() - Types.Ghost1.ordinal();
        }
        return -1;
    }

    public int getzIndex() {
        return zIndex;
    }

}
