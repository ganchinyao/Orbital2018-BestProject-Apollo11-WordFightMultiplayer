package com.ganwl.multiplayerwordgame.helper;

import android.content.Context;

import java.util.HashSet;

// used to transmit user data to saved state google api
public class SavedGameState implements java.io.Serializable {
    private int amountOfJewel;
    private HashSet<String> unlocked_easy;
    private HashSet<String> unlocked_medium;
    private HashSet<String> unlocked_hard;
    private HashSet<String> unlocked_insane;

    public SavedGameState(Context context) {
        this.amountOfJewel = Utils.getUserJewel(context);
        this.unlocked_easy = ((HashSet<String>) Utils.getUnlockedPack_Easy(context));
        this.unlocked_medium = ((HashSet<String>) Utils.getUnlockedPack_Medium(context));
        this.unlocked_hard = ((HashSet<String>) Utils.getUnlockedPack_Hard(context));
        this.unlocked_insane = ((HashSet<String>) Utils.getUnlockedPack_Insane(context));
    }

    public int getAmountOfJewel() {
        return amountOfJewel;
    }

    public HashSet<String> getUnlocked_easy() {
        return unlocked_easy;
    }

    public HashSet<String> getUnlocked_medium() {
        return unlocked_medium;
    }

    public HashSet<String> getUnlocked_hard() {
        return unlocked_hard;
    }

    public HashSet<String> getUnlocked_insane() {
        return unlocked_insane;
    }
}
