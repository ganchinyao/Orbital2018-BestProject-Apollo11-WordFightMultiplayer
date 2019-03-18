package com.ganwl.multiplayerwordgame.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ganwl.multiplayerwordgame.packs.PackData;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    // the global button at home page to mute or unmute sound for the entire application
    public static void setIfSoundIsMuted(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Global_Sound_Mute", value);
        editor.apply();
    }

    public static boolean getIfSoundIsMuted(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("Global_Sound_Mute", false);
    }

    // used to set the name in settings screen
    public static void setPlayerName(Context context, String playerName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Global_User_Name", playerName);
        editor.apply();
    }

    public static String getPlayerName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("Global_User_Name", "Player1");

    }

    // used to determine which difficulty the user was at just now.
    // e.g. if user start game at hard, the next time he start game again, the popup will auto show hard at default for him
    // also used in all QuickPlay mode whereby it will retrieve what difficulty level to play based on getDifficultyPreference
    public static void setDifficultyPreference(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Level_Difficulty_Preference", value);
        editor.apply();
    }

    public static int getDifficultyPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Level_Difficulty_Preference", Constants.MEDIUM_MODE);
    }

    // used to set the name of the last category pack users has selected
    public static void setCategoryPack(Context context, String packName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Category_Pack_Selection", packName);
        editor.apply();
    }

    public static String getCategoryPack(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString("Category_Pack_Selection", PackData.nameArray_Easy[0]);
    }

    // used to determine the first spell user has selected before.
    public static void setFirstSelectedSpell(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Spell_1", value);
        editor.apply();
    }

    public static int getFirstSelectedSpell(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Spell_1", Constants.SPELL_THROW);
    }

    // used to determine the first spell user has selected before.
    public static void setSecondSelectedSpell(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Spell_2", value);
        editor.apply();
    }

    public static int getSecondSelectedSpell(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Spell_2", Constants.SPELL_TILT);
    }

    // used to determine the first spell user has selected before.
    public static void setThirdSelectedSpell(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Spell_3", value);
        editor.apply();
    }

    public static int getThirdSelectedSpell(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Spell_3", Constants.SPELL_JUMBLE);
    }

    // used to determine the first spell user has selected before.
    public static void setForthSelectedSpell(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Spell_4", value);
        editor.apply();
    }

    public static int getForthSelectedSpell(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Spell_4", Constants.SPELL_GUARDIANANGEL);
    }


    // used to determine the amount of jewel users has
    public static void setUserJewel(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_amount_of_jewel", value);
        editor.apply();
    }

    public static int getUserJewel(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("user_amount_of_jewel", 150);
    }


    // whenever user unlock a pack, add the name of the pack inside here
    public static void unlockPack_Easy(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> hashSet = prefs.getStringSet("packstounlock_easy", new HashSet<String>());
        hashSet.add(value);
        editor.putStringSet("packstounlock_easy", hashSet);
        editor.apply();
    }

    public static void unlockPack_Medium(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> hashSet = prefs.getStringSet("packstounlock_medium", new HashSet<String>());
        hashSet.add(value);
        editor.putStringSet("packstounlock_medium", hashSet);
        editor.apply();
    }

    public static void unlockPack_Hard(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> hashSet = prefs.getStringSet("packstounlock_hard", new HashSet<String>());
        hashSet.add(value);
        editor.putStringSet("packstounlock_hard", hashSet);
        editor.apply();
    }

    public static void unlockPack_Insane(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> hashSet = prefs.getStringSet("packstounlock_insane", new HashSet<String>());
        hashSet.add(value);
        editor.putStringSet("packstounlock_insane", hashSet);
        editor.apply();
    }

    public static Set<String> getUnlockedPack_Easy(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet("packstounlock_easy", new HashSet<String>());
    }

    public static Set<String> getUnlockedPack_Medium(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet("packstounlock_medium", new HashSet<String>());
    }

    public static Set<String> getUnlockedPack_Hard(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet("packstounlock_hard", new HashSet<String>());
    }

    public static Set<String> getUnlockedPack_Insane(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet("packstounlock_insane", new HashSet<String>());
    }

    // return true if the pack was unlock before
    public static boolean getIfPackWasUnlock(Context context, int levelDifficulty, String packName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> hashSet;
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                hashSet = preferences.getStringSet("packstounlock_easy", new HashSet<String>());
                break;
            case Constants.MEDIUM_MODE:
                hashSet = preferences.getStringSet("packstounlock_medium", new HashSet<String>());
                break;
            case Constants.HARD_MODE:
                hashSet = preferences.getStringSet("packstounlock_hard", new HashSet<String>());
                break;
            case Constants.INSANE_MODE:
                hashSet = preferences.getStringSet("packstounlock_insane", new HashSet<String>());
                break;
            default: hashSet = preferences.getStringSet("packstounlock_easy", new HashSet<String>());
        }
        return hashSet.contains(packName);
    }


    public static void setIfJewelIsUpdated(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("jewel_updated_value", value);
        editor.apply();
    }

    public static boolean getIfJewelIsUpdated(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("jewel_updated_value", false);
    }

    public static void setIfJustSignedIn(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("just_signed_in", value);
        editor.apply();
    }

    public static boolean getIfJustSignedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("just_signed_in", false);
    }

    public static void setIfFirstLaunch(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_launch", value);
        editor.apply();
    }

    public static boolean getIfFirstLaunch(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("first_launch", true);
    }

    public static void setAmountOfGamePlayed_singlePlayer(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("amount_singleplayer_played", value);
        editor.apply();
    }

    public static int getAmountOfGamePlayed_singlePlayer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("amount_singleplayer_played", 0);
    }

    public static void setAmountOfGamePlayed_multiPlayer(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("amount_multiplayer_played", value);
        editor.apply();
    }

    public static int getAmountOfGamePlayed_multiPlayer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("amount_multiplayer_played", 0);
    }

    // becos not connected, therefore standby to unlock later
    public static void addAchievementsToUnlockLater(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> hashSet = prefs.getStringSet("achievement_unlock_later", new HashSet<String>());
        hashSet.add(value);
        editor.putStringSet("achievement_unlock_later", hashSet);
        editor.apply();
    }

    // becos not connected, therefore standby to unlock later
    public static void clearAchievementsToUnlockLater(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet("achievement_unlock_later", new HashSet<String>());
        editor.apply();
    }

    public static Set<String> getAchievementsToUnlockLater(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet("achievement_unlock_later", new HashSet<String>());
    }

    public static void setIfPurchasedNoAds(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("shop_no_ads", value);
        editor.apply();
    }

    public static boolean getIfPurchasedNoAds(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("shop_no_ads", false);
    }

    public static String getHomePageNativeAdId() {
        return Constants.homepageNativeAdId;
    }

    // for singleplayer
    public static String getPauseNativeAdId() {
        return Constants.pauseNativeAdId;
    }

    public static String getPauseNativeAdIdMultiplayer() {
        return Constants.pauseNativeAdId_Multiplayer;
    }

    public static String getEndGameNativeAdId_SinglePlayer() {
        return Constants.endGameNativeAdId_Singleplayer;
    }
}
