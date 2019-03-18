package com.ganwl.multiplayerwordgame;

import android.content.Context;

import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import java.util.ArrayList;

// if want to add brand new achievement, go to google play console add an achievement, then add the achievement below
// at AchievementsName, and add more cases at checkAccomplishments()
public class Accomplishments {
    private int mode, score, streak;
    private Context context;
    private ArrayList<AccomplishmentsName> result = new ArrayList<>(); // store all achievement here and return it later
    private AchievementsClient mAchievementsClient;
    private boolean noSkip; // indicate if user got press skip or not

    public Accomplishments(Context context, int mode, int score, int streak, boolean noSkip) {
        this.context = context;
        this.mode = mode;
        this.score = score;
        this.streak = streak;
        this.noSkip = noSkip;

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if(googleSignInAccount != null) {
            mAchievementsClient = Games.getAchievementsClient(context, googleSignInAccount);
        }

        // check see user matches what accomplishments
        checkAccomplishments();
    }

    // check against all the accomplishments
    private void checkAccomplishments() {
        // check and incremenet number of plays
        switch (mode) {
            case Constants.SINGLE_PLAYER:
                int prevAmountPlayed_single = Utils.getAmountOfGamePlayed_singlePlayer(context);
                Utils.setAmountOfGamePlayed_singlePlayer(context, prevAmountPlayed_single + 1);
                if(prevAmountPlayed_single + 1 >= 100) {
                    addUserJewels(AccomplishmentsName.SINGLE_PLAYER_X100);
                    Utils.setAmountOfGamePlayed_singlePlayer(context, 0); // reset
                    unlockGooglePlayAchievement(R.string.achievement_complete_100_times_single_player_mode);
                } else if(prevAmountPlayed_single + 1 == 50) {
                    addUserJewels(AccomplishmentsName.SINGLE_PLAYER_X50);
                    unlockGooglePlayAchievement(R.string.achievement_complete_50_times_single_player_mode);
                } else if(prevAmountPlayed_single + 1 == 20) {
                    addUserJewels(AccomplishmentsName.SINGLE_PLAYER_X20);
                    unlockGooglePlayAchievement(R.string.achievement_complete_20_times_single_player_mode);
                } else if(prevAmountPlayed_single + 1 == 10) {
                    addUserJewels(AccomplishmentsName.SINGLE_PLAYER_X10);
                    unlockGooglePlayAchievement(R.string.achievement_complete_10_times_single_player_mode);

                }
                break;
            case Constants.MULTI_PLAYER:
                int prevAmountPlayed_multi = Utils.getAmountOfGamePlayed_multiPlayer(context);
                Utils.setAmountOfGamePlayed_multiPlayer(context, prevAmountPlayed_multi + 1);
                if(prevAmountPlayed_multi + 1 >= 100) {
                    addUserJewels(AccomplishmentsName.MULTIPLAYER_X100);
                    Utils.setAmountOfGamePlayed_multiPlayer(context, 0); // reset
                    unlockGooglePlayAchievement(R.string.achievement_complete_100_times_multiplayer_mode);
                } else if(prevAmountPlayed_multi + 1 == 50) {
                    addUserJewels(AccomplishmentsName.MULTIPLAYER_X50);
                    unlockGooglePlayAchievement(R.string.achievement_complete_50_times_multiplayer_mode);
                } else if(prevAmountPlayed_multi + 1 == 20) {
                    addUserJewels(AccomplishmentsName.MULTIPLAYER_X20);
                    unlockGooglePlayAchievement(R.string.achievement_complete_20_times_multiplayer_mode);
                } else if(prevAmountPlayed_multi + 1 == 10) {
                    addUserJewels(AccomplishmentsName.MULTIPLAYER_X10);
                    unlockGooglePlayAchievement(R.string.achievement_complete_10_times_multiplayer_mode);
                }
                break;
        }

        // check scores
        if(score >= 300) {
            addUserJewels(AccomplishmentsName.SCORE_300_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_300_points_insane);
        } else if(score >= 250) {
            addUserJewels(AccomplishmentsName.SCORE_250_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_250_points_expert);
        } else if(score >= 200) {
            addUserJewels(AccomplishmentsName.SCORE_200_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_200_points_master);
        } else if(score >= 150) {
            addUserJewels(AccomplishmentsName.SCORE_150_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_150_points);
        } else if(score >= 100) {
            addUserJewels(AccomplishmentsName.SCORE_100_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_100_points);
        } else if(score >= 60) {
            addUserJewels(AccomplishmentsName.SCORE_60_POINTS);
            unlockGooglePlayAchievement(R.string.achievement_score_60_points);
        }

        // check streak
        if(streak >= 10) {
            addUserJewels(AccomplishmentsName.GET_10_STREAKS);
            unlockGooglePlayAchievement(R.string.achievement_get_10_streaks_insane);
        } else if(streak >= 8) {
            addUserJewels(AccomplishmentsName.GET_8_STREAKS);
            unlockGooglePlayAchievement(R.string.achievement_get_8_streaks_expert);
        } else if(streak >= 6) {
            addUserJewels(AccomplishmentsName.GET_6_STREAKS);
            unlockGooglePlayAchievement(R.string.achievement_get_6_streaks);
        } else if(streak >= 4) {
            addUserJewels(AccomplishmentsName.GET_4_STREAKS);
            unlockGooglePlayAchievement(R.string.achievement_get_4_streaks);
        }

        // check if no skip
        if(noSkip) {
            addUserJewels(AccomplishmentsName.NO_SKIP);
            unlockGooglePlayAchievement(R.string.achievement_no_skip);
        }
    }

    // add the jewels earned to user account
    private void addUserJewels(AccomplishmentsName accomplishmentsName) {
        Utils.setUserJewel(context, (Utils.getUserJewel(context) + accomplishmentsName.getJewelGained()));
        Utils.setIfJewelIsUpdated(context, true);
        result.add(accomplishmentsName);
    }

    // unlock this achievement in the google play Achievements
    private void unlockGooglePlayAchievement(int id) {
        if(mAchievementsClient != null) {
            // user is signed in, therefore unlock now
            mAchievementsClient.unlock(context.getString(id));
        } else {
            // add to unlock later
            Utils.addAchievementsToUnlockLater(context, context.getString(id));
        }
    }

    // return the list of achievements users has achieved this round
    public ArrayList<AccomplishmentsName> getAchievementsList() {
        return result;
    }
}

enum AccomplishmentsName {
    SCORE_60_POINTS("Score 60 points", 5), SCORE_100_POINTS("Score 100 points", 10), SCORE_150_POINTS("Score 150 points", 20),
    SCORE_200_POINTS("Score 200 points", 30), SCORE_250_POINTS("Score 250 points", 40), SCORE_300_POINTS("Score 300 points", 50),
    GET_4_STREAKS("Get 4 Streaks", 5), GET_6_STREAKS("Get 6 Streaks", 10), GET_8_STREAKS("Get 8 Streaks", 15), GET_10_STREAKS("Get 10 Streaks", 20),
    SINGLE_PLAYER_X10("Play Single Player 10 times", 5), SINGLE_PLAYER_X20("Play Single Player 20 times", 15),
    SINGLE_PLAYER_X50("Play Single Player 50 times", 40), SINGLE_PLAYER_X100("Play Single Player 100 times", 100),
    MULTIPLAYER_X10("Play Multi-Player 10 times", 5), MULTIPLAYER_X20("Play Multi-Player 20 times", 15),
    MULTIPLAYER_X50("Play Multi-Player 50 times", 40), MULTIPLAYER_X100("Play Multi-Player 100 times", 100),
    NO_SKIP("No Skip", 5);

    private String desc;
    private int jewelGained;

    AccomplishmentsName(String desc, int jewelGained) {
        this.desc = desc;
        this.jewelGained = jewelGained;
    }

    String getDesc() {
        return this.desc;
    }

    int getJewelGained() {
        return this.jewelGained;
    }
}