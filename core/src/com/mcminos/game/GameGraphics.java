package com.mcminos.game;

/**
 * Created by ulno on 14.08.15.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** represent on of the entities in the game like mcminos itself, ghosts, walls or other things in the game
 *
 */
public class GameGraphics {
    private char symbol;
    private int anchorX, anchorY;
    private int zIndex;
    private boolean moving;
    private int sizeX, sizeY;
    private int totalAnimationLength;

    final int precision = 10; // not more precise than 10ms -? 1/100s second
    private int[] timeList = null;

    // Hierarchy here is: Hashmap of sizes mapped to to ArrayList of Images
    private HashMap<Integer,ArrayList> ResolutionList = new HashMap<Integer, ArrayList>();
    // the reference step-list (each step references the correct image number) for animating this entity per category
    private HashMap<String,ArrayList> animationCategorySteps = new HashMap<String, ArrayList>();
//    ArrayList<Integer> animationSteps = new ArrayList<Integer>();
    private ArrayList<IntPair> stepList = new ArrayList<IntPair>();


    GameGraphics(char symbol, int anchorX, int anchorY, int zIndex, boolean moving, int sizeX, int sizeY) {
        this.symbol = symbol;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.zIndex = zIndex;
        this.moving = moving;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        totalAnimationLength = 0;
    }

    // Is called at end of initialisation, when all graphics has been added
    void finishInit() {
        generateTimeList();
    }

    // category can include subcategory and is separeted with a dot
    void addImage( String file, int resolution, int step ) {

        ArrayList textures;

        if( ResolutionList.containsKey(resolution)) {
            textures = ResolutionList.get( resolution );
        }
        else { // Resolution not in there
            textures = new ArrayList<Texture>();
            ResolutionList.put(resolution, textures);
        }

        Texture texture = new Texture( Gdx.files.internal( file ) );
        textures.add(texture);
    }

    /**
     * @param step Reference nr for corresponding animation step
     * @param length length to display in ms
     */
    void addAnimationStep( int step, int length )
    {
        stepList.add( new IntPair(step, length) );
        totalAnimationLength += length;
    }

    void generateTimeList( )
    {
        int size = (totalAnimationLength + precision - 1) / precision;
        timeList = new int[size];
        int currentTime = 0;
        int nextAnimation = stepList.get(0).second;
        int currentAnimation = 0;
        int timeListIndex = 0;
        while( currentTime < totalAnimationLength ) {
            if( currentTime < nextAnimation ) {
                timeList[timeListIndex] = currentAnimation;
                timeListIndex ++;
                currentTime += precision;
            }
            else {
                currentAnimation ++;
                nextAnimation += stepList.get(currentAnimation).second;
            }
        }
    }

    int getAnimationIndex(long gametime) {
        gametime %= totalAnimationLength;
        gametime /= precision;
        return stepList.get(timeList[(int)gametime]).first;
    }

    Texture getTexture(int resolution, long gametime) {
        ArrayList<Texture> textures = ResolutionList.get(resolution);
        return textures.get( getAnimationIndex(gametime) );
    }
}
