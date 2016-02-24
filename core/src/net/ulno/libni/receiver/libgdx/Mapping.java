package net.ulno.libni.receiver.libgdx;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 22.02.16.
 */
public class Mapping {
    HashMap<Integer,ArrayList<Integer>> keycodeToLibniButtons = new HashMap<>();
    HashMap<Integer,ArrayList<Integer>> libniButtonToKeycodes = new HashMap<>();
    HashMap<Integer,ArrayList<Integer>> analogToLibniButtons = new HashMap();
    HashMap<Integer,ArrayList<Boolean>> analogToLibniButtonSelect = new HashMap();
    HashMap<Integer,ArrayList<Integer>> libniButtonToAnalogs = new HashMap<>();
    HashMap<Integer,Integer> analogToLibniAnalog = new HashMap();
    HashMap<Integer,ArrayList<Integer>> libniAnalogToAnalogs = new HashMap<>();

    public Mapping addButton( int libniButton, int keycode ) {
        ArrayList<Integer> libniButtons;

        if(keycodeToLibniButtons.containsKey(keycode)) {
            libniButtons = keycodeToLibniButtons.get(keycode);
        } else {
            libniButtons = new ArrayList<>();
            keycodeToLibniButtons.put(keycode, libniButtons);
        }
        // add to existing list
        libniButtons.add(libniButton);

        ArrayList<Integer> keycodes;
        if (libniButtonToKeycodes.containsKey(libniButton)) {
            // add to existing list
            keycodes = libniButtonToKeycodes.get(libniButton);
        } else {
            // create new list
            keycodes = new ArrayList<Integer>();
            libniButtonToKeycodes.put(libniButton, keycodes);
        }
        keycodes.add(keycode);

        return this;
    }

    /**
     * Map an analog value to a button
     *
     * @param libniButton
     * @param analogNr
     * @param positive sepcify if this being positive is mapped to pressed (and if negative to unpressed) or vice versa
     */
    public Mapping addButtonFromAnalog(int libniButton, int analogNr, boolean positive) {
        ArrayList<Integer> libniButtons;
        ArrayList<Boolean> libniButtonsPositive;

        if(analogToLibniButtons.containsKey(analogNr)) {
            libniButtons = analogToLibniButtons.get(analogNr);
            libniButtonsPositive = analogToLibniButtonSelect.get(analogNr);
        } else {
            libniButtons = new ArrayList<>();
            analogToLibniButtons.put(analogNr, libniButtons);
            libniButtonsPositive = new ArrayList<>();
            analogToLibniButtonSelect.put(analogNr, libniButtonsPositive);
        }
        // add to existing list
        libniButtons.add(libniButton);
        libniButtonsPositive.add(positive);


        ArrayList<Integer> list;

        if (libniButtonToAnalogs.containsKey(libniButton)) {
            // add to existing list
            list = libniButtonToAnalogs.get(libniButton);
        } else {
            // create new list
            list = new ArrayList<Integer>();
            libniButtonToAnalogs.put(libniButton, list);
        }
        list.add(analogNr);

        return this;
    }

    public Mapping addAnalog( int libniAnalog, int analogNr ) {
        // TODO: implement!
        return this;
    }


    public void initDefault() {
        for(int i = 0; i< LibniController.NUMBER_OF_BUTTONS; i++ ) {
            addButton(i,i);
        }
        for(int i = 0; i< LibniController.NUMBER_OF_ANALOGS; i++ ) {
            addAnalog(i,i);
        }
    }

    public ArrayList<Integer> getLibniButtonsFromUnmappedButton(int unmappedButton) {
        return keycodeToLibniButtons.get(unmappedButton);
    }

    public int getLibniAnalogFromUnmappedAnalog(int analogNr) {
        if(!analogToLibniAnalog.containsKey(analogNr)) return -1;
        return analogToLibniAnalog.get(analogNr);
    }

    public ArrayList<Integer> getLibniButtonsFromUnmappedAnalog(int analogNr) {
        return analogToLibniButtons.get(analogNr);
    }

    public ArrayList<Integer> getUnmappedButtonsFromLibniButton(int libniButton) {
        return libniButtonToKeycodes.get(libniButton);
    }

    public ArrayList<Integer> getUnmappedAnalogsFromLibniButton( int libniButton ) {
        return libniButtonToAnalogs.get(libniButton);
    }

    public ArrayList<Integer> getUnmappedAnalogsFromLibniAnalog(int libniAnalog) {
        return libniAnalogToAnalogs.get(libniAnalog);
    }

    public boolean selectButtonValue(int unmappedAnalogNr, int libniButtonNr, long analogValue) {
        ArrayList<Integer> triggerButtons = analogToLibniButtons.get(unmappedAnalogNr);
        int buttonIndex = triggerButtons.indexOf(libniButtonNr);
        boolean positive = analogToLibniButtonSelect.get(unmappedAnalogNr).get(buttonIndex); // TODO: merge into one class to speed up
        if(analogValue != 0) {
            if (positive) {
                return analogValue > 0;
            } else {
                return analogValue < 0;
            }
        }
        return false;
    }

/*    public boolean isAnalogPositive(int analogNr) {
        return analogToLibniButtonSelect.get(analogNr);
    }*/
}
