package com.mcminos.game;

import java.util.HashSet;

/**
 * Created by ulno on 17.08.15.
 *
 * These are the actual blocks the field consists off.
 *
 */
public class LevelBlock {
    private int x,y; // Position in Level Field
    private Level level; // corresponding level
    private LevelObject wall=null; // The wall or door connected here
    private LevelObject rock=null; // a rock connected to this field
    private LevelObject pill=null; // a potential pill on this field
    private LevelObject castle=null; // a part of a castle
    private HashSet<LevelObject> movables=new HashSet<>(); // ghosts, mcminos, explosions, rocks hovering here.
    private HashSet<LevelObject> collectibles =new HashSet<>(); // collectibles on the field
    private LevelObject door=null; // a potential door
    private int oneWay = -1; // -1, no oneway, 0 up, 1 right, 2 down, 3 left, +4 rotatable


    /**
     * @param lo check if this is in movable or collectible (connected with this field)
     * @return
     */
    public boolean has( LevelObject lo) {
        return hasAsMovable(lo) || hasAsItem(lo);
    }

    public boolean hasAsMovable(LevelObject lo) {
        return movables.contains(lo);
    }

    public boolean hasAsItem(LevelObject lo) {
        return collectibles.contains(lo);
    }

    /**
     *
     * @param lo remove this (either if in collectibles or movables)
     * @return
     */
    public boolean remove( LevelObject lo)
    {
        return removeMovable(lo) || removeItem(lo);
    }

    public boolean removeMovable(LevelObject lo) {
        return movables.remove(lo);
    }

    public boolean removeItem(LevelObject lo) {
        return collectibles.remove(lo);
    }

    public LevelBlock(Level level, int x, int y) {
        this.level = level;
        this.x = x;
        this.y = y;

    }

    public void updateWall() {
        // TODO: think about impact of neighborrelation when there are invisible walls -> now the neighbors know!
        if( wall != null && ! wall.isIndestructable() && ! wall.isInvisible()) { // only update, if this is a normal wall
            // create wall number, based on neighbors
            int wallNr = 0;
            LevelBlock u = level.getUp(x, y);
            LevelBlock r = level.getRight(x, y);
            LevelBlock d = level.getDown(x, y);
            LevelBlock l = level.getLeft(x, y);

            if (u != null && u.hasWall())
                wallNr += 1;
            if (r != null && r.hasWall())
                wallNr += 2;
            if (d != null && d.hasWall())
                wallNr += 4;
            if (l != null && l.hasWall())
                wallNr += 8;
            GameGraphics[] walls = { // TODO: consider how to switch to different walls (for example castle)!
                    Entities.walls_default_00,
                    Entities.walls_default_01,
                    Entities.walls_default_02,
                    Entities.walls_default_03,
                    Entities.walls_default_04,
                    Entities.walls_default_05,
                    Entities.walls_default_06,
                    Entities.walls_default_07,
                    Entities.walls_default_08,
                    Entities.walls_default_09,
                    Entities.walls_default_10,
                    Entities.walls_default_11,
                    Entities.walls_default_12,
                    Entities.walls_default_13,
                    Entities.walls_default_14,
                    Entities.walls_default_15
            };
            wall.setGfx(walls[wallNr]);
        }
    }

    public void updateDoor() {
        if( door != null ) { // There is a door here
            LevelBlock u = level.getUp(x, y);
            //LevelBlock r = level.getRight(x, y);
            LevelBlock d = level.getDown(x, y);
            //LevelBlock l = level.getLeft(x, y);
            boolean isopen = door.getDoorType() == LevelObject.DoorTypes.HorizontalOpened ||
                    door.getDoorType() == LevelObject.DoorTypes.VerticalOpened;
            if (u != null && d != null && u.hasWall() && d.hasWall()) {
                if(isopen) {
                    door.setGfx(Entities.walls_door_open_vertical);
                    door.setDoorType(LevelObject.DoorTypes.VerticalOpened);
                }
                else {
                    door.setGfx(Entities.walls_door_closed_vertical);
                    door.setDoorType(LevelObject.DoorTypes.VerticalClosed);
                }
            }
            else {// WE will assume then we take a horizontal door
                if(isopen) {
                    door.setGfx(Entities.walls_door_open_horizontal);
                    door.setDoorType(LevelObject.DoorTypes.HorizontalOpened);
                }
                else {
                    door.setGfx(Entities.walls_door_closed_horizontal);
                    door.setDoorType(LevelObject.DoorTypes.HorizontalClosed);
                }
            }
        }
    }

    public boolean hasWall() {
        return wall != null;
    }


    public void makeWall() {
        wall = new LevelObject(x,y,Entities.walls_default_00.getzIndex(),LevelObject.Types.Wall);
        updateWall();
    }

    public void makeIndestructableWall() {
        // TODO: need Andreas' graphics for indestructable wall
        wall = new LevelObject(x,y,Entities.walls_indestructable.getzIndex(),LevelObject.Types.IndestructableWall);
        wall.setGfx(Entities.walls_indestructable);
    }

    public void makeInvisibleWall() {
        wall = new LevelObject(x,y,Entities.walls_default_00.getzIndex(),LevelObject.Types.InvisibleWall);
    }

    public void makeMcMinos() {
        LevelObject lo = new LevelObject(x,y,Entities.mcminos_default_front.getzIndex(),LevelObject.Types.McMinos);
        lo.setGfx(Entities.mcminos_default_front);
        movables.add(lo);
        Game.mcminos=lo;
        Game.destination.setXY(x, y);
    }

    public void makePill() {
        putPill();
    }

    public void makePowerPill1() {
        LevelObject lo = new LevelObject(x,y,Entities.pills_power_pill_apple.getzIndex(),LevelObject.Types.Power1);
        lo.setGfx(Entities.pills_power_pill_apple);
        collectibles.add(lo);
    }

    public void makeCastle() {
        castle = new LevelObject(x,y,Entities.castle_default.getzIndex(),LevelObject.Types.Castle);
        updateCastle();
    }

    public void updateCastle() {
        if( castle != null) { // only update if this is castle
            // only add graphics, when lower button corner of a
            // castle (so there needs to be a castle up and one right)
            LevelBlock u = level.getUp(x, y);
            LevelBlock r = level.getRight(x, y);
            if (u != null && u.hasCastle()
                    && r != null && r.hasCastle())
                castle.setGfx(Entities.castle_default);
        }
    }

    public boolean hasCastle() {
        return castle != null;
    }

    public void makeGhost1() {
        LevelObject lo = new LevelObject(x,y,Entities.ghosts_hanky.getzIndex(),LevelObject.Types.Ghost1);
        lo.setGfx(Entities.ghosts_hanky);
        movables.add(lo);
    }

    public void makeLive() {
        LevelObject lo = new LevelObject(x,y,Entities.pills_heart.getzIndex(),LevelObject.Types.Live);
        lo.setGfx(Entities.pills_heart);
        collectibles.add(lo);
    }

    public void makeLetter() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_letter.getzIndex(),LevelObject.Types.Letter);
        lo.setGfx(Entities.extras_letter);
        collectibles.add(lo);
    }

    public void makeSkull() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_skull.getzIndex(),LevelObject.Types.Skull);
        lo.setGfx(Entities.extras_skull);
        collectibles.add(lo);
    }

    public void makeBomb() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_bomb_default.getzIndex(),LevelObject.Types.Bomb);
        lo.setGfx(Entities.extras_bomb_default);
        collectibles.add(lo);
    }

    public void makeDynamite() {
        // TODO: Image by Andreas missing
        LevelObject lo = new LevelObject(x,y,Entities.extras_dynamite_default.getzIndex(),LevelObject.Types.Dynamite);
        lo.setGfx(Entities.extras_dynamite_default);
        collectibles.add(lo);
    }

    public void makeKey() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_key.getzIndex(),LevelObject.Types.Key);
        lo.setGfx(Entities.extras_key);
        collectibles.add(lo);
    }

    public void makeUmbrella() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_umbrella.getzIndex(),LevelObject.Types.Umbrella);
        lo.setGfx(Entities.extras_umbrella);
        collectibles.add(lo);
    }


    public void makeRock() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_rock.getzIndex(),LevelObject.Types.Rock);
        lo.setGfx(Entities.extras_rock);
        rock = lo;
        movables.add(lo);
        Game.movables.add(lo);
    }

    public void makeRockMe() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_rock_me.getzIndex(),LevelObject.Types.Rockme);
        lo.setGfx(Entities.extras_rock_me);
        level.increaseRockmes();
    }

    public void makeDoorClosed() {
        LevelObject lo = new LevelObject(x,y,Entities.walls_door_closed_horizontal.getzIndex(),LevelObject.Types.DoorClosed);
        // graphics comes in wall update
        lo.setDoorType(LevelObject.DoorTypes.HorizontalClosed);
        door = lo;
    }

    public void makeDoorOpened() {
        LevelObject lo = new LevelObject(x,y,Entities.walls_door_closed_horizontal.getzIndex(),LevelObject.Types.DoorOpened);
        // graphics comes in wall update
        lo.setDoorType(LevelObject.DoorTypes.HorizontalOpened);
        door = lo;
    }

    public void putMoveable(LevelObject lo) {
        movables.add(lo);
    }

    public void putItem(LevelObject lo) {
        collectibles.add(lo);
    }

    public boolean hasRock() {
        return rock != null;
    }

    public LevelObject getRock() {
        return rock;
    }

    public void setRock(LevelObject rock) {
        this.rock = rock;
    }

    public boolean hasPill() {
        return pill != null;
    }


    public void removePill() {
        level.decreasePills();
        pill.dispose();
        pill = null;
    }

    public void putPill() {
        level.increasePills();
        LevelObject lo = new LevelObject(x,y,Entities.pills_pill_default.getzIndex(),LevelObject.Types.Pill);
        lo.setGfx(Entities.pills_pill_default);
        pill = lo;
    }

    /**
     * Create a hole in the ground
     * @param i 0: smallest, 4: biggest
     */
    public void makeHole(int i) {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.Hole);
        lo.setHoleLevel( i );
    }

    public boolean hasClosedDoor() {
        if( door == null) return false;
        return door.getDoorType() == LevelObject.DoorTypes.HorizontalClosed || door.getDoorType() == LevelObject.DoorTypes.VerticalClosed;
    }

    public void makeSpeedUpField() {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.SpeedUpField);
        lo.setGfx(Entities.fields_field_speed_up);
        collectibles.add(lo);
    }

    public void makeSpeedDownField() {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.SpeedDownField);
        lo.setGfx(Entities.fields_field_speed_down);
        collectibles.add(lo);
    }

    public void makeWarpHole() {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.WarpHole);
        lo.setGfx(Entities.fields_field_warp_hole);
        level.addWarpHole(this);
        collectibles.add(lo);
    }

    public void makeKillAllField() {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.KillAllField);
        lo.setGfx(Entities.fields_field_skull);
        collectibles.add(lo);
    }

    /**
     * Make a one-way street. 0: up, 1: right, 2: down, 3: left, +4 rotatable
      * @param i
     */
    public void makeOneWay(int i) {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex(),LevelObject.Types.OneWay);
        oneWay = i;
        switch(i) {
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

    public HashSet<LevelObject> getCollectibles() {
        return collectibles;
    }
}
