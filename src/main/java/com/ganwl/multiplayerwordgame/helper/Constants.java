package com.ganwl.multiplayerwordgame.helper;

import com.ganwl.multiplayerwordgame.packs.PackData;

public class Constants {
    public static final int SINGLE_PLAYER = 0;
    public static final int MULTI_PLAYER = 1;
    public static final int EASY_MODE = 0;
    public static final int MEDIUM_MODE = 1;
    public static final int HARD_MODE = 2;
    public static final int INSANE_MODE = 3;

    public static final int UNLOCK_PACK_JEWEL_REQUIRED = 200;

    // game stats
    public static final int GAME_TIME = 121000; // in millisecond (2 min)
    public static final int GAME_TIME_MULTIPLAYER = 181000; // in millisecond (3 min)
    public static final byte GAME_FLATRATE = 10; // score per correct answer
    // additional score per streak,
    // e.g. 2 streaks = 4 more points, 3 streaks = 6 more points
    public static final byte GAME_STREAKRATE = 2;

    // Request codes for the UIs that we show with startActivityForResult:
    public static final int RC_SELECT_PLAYERS = 10000;
    public static final int RC_INVITATION_INBOX = 10001;
    public static final int RC_WAITING_ROOM = 10002;
    // Request code used to invoke sign in user interactions.
    public static final int RC_SIGN_IN = 9001;

    public static final char MESSAGE_PLAYERNAME = 'N';
    public static final char MESSAGE_RESTARTGAME = 'O';
    public static final char MESSAGE_READY_GETREADYDIALOGSHOWN = 'R';
    public static final char MESSAGE_CATEGORYSELECTION = 'C';
    public static final char MESSAGE_SPELLUSED = 'S';
    public static final char MESSAGE_UPDATESCORE = 'U';
    public static final int SPELL_TWOTIMESEXP = 0;
    public static final int SPELL_GUARDIANANGEL = 1;
    public static final int SPELL_BLOCK = 2;
    public static final int SPELL_HEX = 3;
    public static final int SPELL_JUMBLE = 4;
    public static final int SPELL_THROW = 5; // aka pass it on spell
    public static final int SPELL_REVERSE = 6;
    public static final int SPELL_SEEMORE = 7;
    public static final int SPELL_TILT = 8;

    // spell duration in milliseconds
    public static final int DURATION_SPELL_BLOCK = 10000;
    public static final int DURATION_SPELL_HEX = 8000;
    public static final int DURATION_SPELL_JUMBLE = 8000;
    public static final int DURATION_SPELL_SEEMORE = 8000;
    public static final int DURATION_SPELL_TILT = 8000;

    public static final int BROADCAST_LEVELDIFFICULTYCHANGED = 0;
    public static final int BROADCAST_PACKSCHANGED = 1;
    public static final int BROADCAST_READY = 2;
    public static final int IS_NOT_READY = 0;
    public static final int IS_READY = 1; // used in 2 places, 1 in category selection ready button, 1 in actual game after getReady dialog shown
    public static final int RESTART_YES = 0;
    public static final int RESTART_NO = 1;

    // facebook audience network id.
    // TODO: Use your own ID for native advertising. In this app we use Facebook Audience Network native ads.
    public static final String homepageNativeAdId = "YOUR_PLACEMENT_ID";
    public static final String pauseNativeAdId = "YOUR_PLACEMENT_ID"; // singleplayer
    public static final String pauseNativeAdId_Multiplayer = "YOUR_PLACEMENT_ID"; // multiplayer
    public static final String endGameNativeAdId_Singleplayer = "YOUR_PLACEMENT_ID"; // singleplayer

    /*
      If add new spell, update:
      MultiplayerGame consumeSpellSelfEvent to add more switch
     */

    public static String currentPackSelection = PackData.nameArray_Easy[0];

    public static boolean signedIn = false; // used to determine if user has sign in

    // TODO: Add your own In-App-Purchase SKU name
    public static final String noAdsSKU = "no_ads";
    public static final String oneHundredJewelSKU = "purchase_100_jewels";
    public static final String threeHundredJewelSKU = "purchase_300_jewels";
    public static final String nineHundredJewelSKU = "purchase_900_jewels";

    // TODO: Add your own API Key for your In-App-Purchase key
    public static final String In_APP_PURCHASE_YOUR_KEY_FROM_GOOGLE_CONSOLE
            = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJQKMOPQRABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopq" +
            "rstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJQKMOPABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkl" +
            "mnopqrstuvwxyzABCDEFGHIJQKABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqr";
}
