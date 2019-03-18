package com.ganwl.multiplayerwordgame;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.ganwl.multiplayerwordgame.fragments.AllCategoryMultiplayer;
import com.ganwl.multiplayerwordgame.fragments.MultiplayerGameSettings;
import com.ganwl.multiplayerwordgame.helper.BytesUtil;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.CustomTextView;
import com.ganwl.multiplayerwordgame.helper.MusicManager;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;

import com.ganwl.multiplayerwordgame.packs.PackData;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/*
    Due how how google play game services work, we need to put everything in 1 activity: popup
    screen selection, handshake process, and actual game play

    Unlike single player, for multiplayer all default screen will be EASY mode, and ALL Category pack.
    This is to prevent race condition such that i am easy, you are medium, i sent broadcast to you easy, you
    sent broadcast to me medium, i sent broadcast to you medium infinite loop.
 */
public class MultiPlayerGame extends AppCompatActivity implements MultiplayerGameSettings.OnSpellSelectedListener,
        AllCategoryMultiplayer.OnCategoryPackClickListener {
    final static String TAG = "gggg";
    private View easyButton, mediumButton, hardButton, insaneButton;
    private TextView easyTextView, mediumTextView, hardTextView, insaneTextView;
    private ImageView easyImageView, mediumImageView, hardImageView, insaneImageView, popupStartButton;
    private int selectedLevelDifficulty;
    private HandshakeUI handshakeUI;
    private HandshakeConnection handshakeConnection;
    // All click listener event to be put into this class
    private ClickListener clickListener = new ClickListener();
    private boolean inGame = false; // set to true when actual game starts
    private MultiPlayerActualGame multiPlayerActualGame;
    // use a queue system to select spell, i.e. the 5th selected will deselect the 1st selected
    private Queue<Integer> spellQueue = new LinkedList<>();
    private MultiplayerPageAdapter pageAdapter; // use to set pack in broadcast
    HashMap<String, PlayerIcon> playerIconImageViewMap = new HashMap<>();
    private int amountOfPlayersReady = 0; // used in ready selection
    private int amountOfPlayersWantToRestart = 0; // used in restart button
    private boolean playingGameDueToRestart = false; // set to true for subsequent restart matches
    boolean isAppRunning = false;
    private MediaPlayer mediaPlayer;
    private boolean onResumeCalledBefore = false;
    final Random randomGenerator = new Random();
    HashSet<Integer> userPassiveSpell = new HashSet<>();
    private boolean goingToPreviousActivity = false;
    boolean exitFromGameToMainMenu = false; // this flag is used to control media sound

    // used in category selection to tell if player is ready or not, and change the image color accordingly
    // also used in restart menu when player wants to restart
    class PlayerIcon {
        private ImageView playerIcon;
        private boolean isReady;

        public PlayerIcon(ImageView playerIcon) {
            this.playerIcon = playerIcon;

            isReady = false;
        }

        public boolean isReady() {
            return isReady;
        }

        public ImageView getPlayerIcon() {
            return playerIcon;
        }

        // set in ready in category selection means player is ready,
        // in restart menu means player wants to restart
        public void setIsReady(boolean value) {
            this.isReady = value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_layout);
        initializeHandshake();
    }

    private boolean isSoundMuted() {
        return Utils.getIfSoundIsMuted(MultiPlayerGame.this);
    }

    // ONE and ONLY place to play sound. Play the sound based on the param passed. Add new case here to play new sound
    void playMediaPlayer(int soundToPlay) {
        if (!isSoundMuted() && isAppRunning) {
            //sound is not muted in main page, hence play sound
            switch (soundToPlay) {
                case 0:
                    // play dialog
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dialogsound);
                    startSound();
                    break;
                case 1:
                    // play gameMode background music

                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.game_sound);
                    startSound();
                    mediaPlayer.setLooping(true);
                    break;
                case 2:
                    // play correct ans sound
//                    switch (randomGenerator.nextInt(3)) {
//                        // play a random corret sound out of 3
//                        case 0:
//                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.answer_correct1);
//                            break;
//                        case 1:
//                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.answer_correct2);
//                            break;
//                        case 2:
//                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.answer_correct3);
//                            break;
//                        default:
//                            // should not be called, but just in case
//                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.answer_correct3);
//                            break;
//                    }
//                    startSound();
                    break;
                case 3:
                    // play wrong ans sound
//                    mediaPlayer = MediaPlayer.create(getApplicationContext(), com.react.reactmultiplayergame.R.raw.answer_wrong);
//                    startSound();
                    break;
                case 4:
                    // play victory page short sound
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.winnerpage_victorysound);
                    startSound();
                    // play long victory sound after
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.reset();
                            playMediaPlayer(5);
                        }
                    });
                    break;
                case 5:
                    // play victory page long background sound
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.winnerpage_backgroundsound);
                    startSound();
                    mediaPlayer.setLooping(true);
                    break;
            }
        }
    }

    private void startSound() {
        if (mediaPlayer != null)
            mediaPlayer.start();
    }

    void releaseMediaPlayer() {
        if (mediaPlayer != null && !isSoundMuted()) {
            mediaPlayer.setLooping(false);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        // when we go to the game mode activity, current activity will be onPause. So we release soundpool resources since now we are at game mode activity
        // this also takes into account user press home button or take phone call e.g, transitting away from the app,
        // hence we stop all sound including background sound
        super.onPause();

        // for the main menu song
        if (!goingToPreviousActivity) {
            // we only want to stop mediaplayer if it app is going background, and NOT when app is onbackpress to go back to previous activity
            // this is achieve by setting the flag goingToPreviousActivity to be true when we onBackPressed.
            // but if user click home button to go home page, goingToPreviousActivity will be false, and stopmediaplayer will be triggered
            MusicManager.stopMediaPlayer();
        }


        // set isAppRunning to false, so no subsequent mediaplayer sound will be called from playMediaPlayer(x)
        // this is impt, becos in the event user pause screen when dialog is calling, subsequent background music will still be called since releaseMediaPlayer is only releasing the dialog sound
        // hence we use this flag to prevent background music from even starting
        isAppRunning = false;
        releaseMediaPlayer();

        if (exitFromGameToMainMenu) {
            // set flag to false we want onResume on MainActivity to play song
            MusicManager.isGoingNextActivity = false;
        } else {
            // this means we are going back from e.g. the category screen or selection screen, where music is already played.
            // Therefore going back we should not start the MainActivity onResume play
            // set this flag to true since we want to retain background music, and onPause will not stop background music
            MusicManager.isGoingNextActivity = true;
        }
    }

    private void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 7;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        handshakeConnection.keepScreenOn();

        handshakeConnection.mRoomConfig = RoomConfig.builder(handshakeConnection.mRoomUpdateCallback)
                .setOnMessageReceivedListener(handshakeConnection.mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(handshakeConnection.mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        handshakeConnection.mRealTimeMultiplayerClient.create(handshakeConnection.mRoomConfig);
    }

    private void initializeHandshake() {
        handshakeUI = new HandshakeUI(clickListener, (ImageView) findViewById(R.id.multiplayer_signIn_Button),
                (ImageView) findViewById(R.id.multiplayer_backbutton), (ImageView) findViewById(R.id.multiplayer_inviteFriends_Button),
                (ImageView) findViewById(R.id.multiplayer_seeInvitation_Button), (ImageView) findViewById(R.id.multiplayer_signOut_Button),
                (ImageView) findViewById(R.id.googleIconImageView), (ImageView) findViewById(R.id.multiplayer_inviteFriendHighFiveIcon),
                (ImageView) findViewById(R.id.multiplayer_seeInvitationMailIcon), (RelativeLayout) findViewById(R.id.multiplayer_invitationRelativeLayout),
                (Button) findViewById(R.id.multiplayer_invitationAcceptButton), (TextView) findViewById(R.id.multiplayer_invitation_questionTextView),
                (TextView) findViewById(R.id.signinHelpText), (TextView) findViewById(R.id.signInTextView),
                (TextView) findViewById(R.id.multiplayer_signOutTextView), (TextView) findViewById(R.id.multiplayer_inviteFriendsTextView),
                (TextView) findViewById(R.id.multiplayer_seeInvitationTextView), (ImageView) findViewById(R.id.multiplayer_quickPlay_Button),
                (TextView) findViewById(R.id.multiplayer_quickPlayTextView), (ImageView) findViewById(R.id.multiplayer_quickPlayIcon),
                (TextView) findViewById(R.id.multiplayer_waitscreen_loadingTextView), (ProgressBar) findViewById(R.id.multiplayer_waitscreenprogressbar));
        handshakeConnection = new HandshakeConnection(this, handshakeUI);
    }

    private void initializeVariables() {
        easyButton = findViewById(R.id.popup_easyButton);
        mediumButton = findViewById(R.id.popup_mediumButton);
        hardButton = findViewById(R.id.popup_hardButton);
        insaneButton = findViewById(R.id.popup_insaneButton);

        easyTextView = findViewById(R.id.popup_easyTextView);
        mediumTextView = findViewById(R.id.popup_mediumTextView);
        hardTextView = findViewById(R.id.popup_hardTextView);
        insaneTextView = findViewById(R.id.popup_insaneTextView);

        easyImageView = findViewById(R.id.popup_easyImageView);
        mediumImageView = findViewById(R.id.popup_mediumImageView);
        hardImageView = findViewById(R.id.popup_hardImageView);
        insaneImageView = findViewById(R.id.popup_insaneImageView);

        popupStartButton = findViewById(R.id.popup_startButton);
    }

    private void initializeOnclickEvents() {
        easyButton.setOnClickListener(clickListener);
        mediumButton.setOnClickListener(clickListener);
        hardButton.setOnClickListener(clickListener);
        insaneButton.setOnClickListener(clickListener);
        popupStartButton.setOnClickListener(clickListener);
    }

    // for category level selection
    private void initializeGraphics() {
        highlightDifficultySelection(Constants.EASY_MODE, true);
    }

    private void setUpViewPager() {
        TabLayout tabLayout = findViewById(R.id.popup_tablayout);
        ViewPager viewPager = findViewById(R.id.popup_viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                SoundPoolManager.getInstance().playSound(3); // play swipe sound
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        pageAdapter = new MultiplayerPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
    }

    void restartYesOrNo(String participantId, boolean wantToRestart) {
        PlayerIcon playerIcon = playerIconImageViewMap.get(participantId);

        if (wantToRestart) {
            amountOfPlayersWantToRestart++;
            // at first dw restart, now want to restart
            playerIcon.setIsReady(true);
            setPlayerIconToFullColor(playerIcon.getPlayerIcon());

            if (amountOfPlayersWantToRestart == handshakeConnection.mParticipants.size()) {
                // all players want to restart
                playingGameDueToRestart = true;
                // prevent further clicking so that we can restart properly
                if (multiPlayerActualGame.restartButton != null)
                    multiPlayerActualGame.restartButton.setClickable(false);

                // deselect all ready so that on category selection it work as correct
                for (PlayerIcon currentPlayIcon : playerIconImageViewMap.values()) {
                    currentPlayIcon.setIsReady(false);
                }

                multiPlayerActualGame.onRestartChanges();

                goToCategorySelectionFromRestart();
            }
        } else {
            amountOfPlayersWantToRestart--;
            // the player dw to restart
            playerIcon.setIsReady(false);
            setPlayerIconToGray(playerIcon.getPlayerIcon());
        }
    }


    // broadcast to others we have change a pack
    private void changePacksDueToBroadcast(int selectedLevelDifficulty, int position) {
        ((AllCategoryMultiplayer) pageAdapter.getAllCategoryFragment()).packsSelected(selectedLevelDifficulty, position);
    }

    private void broadcast_levelDifficultyChanged(int levelDifficulty) {
        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_CATEGORYSELECTION);
        realTimeMultiplayerMessage.setData((byte) Constants.BROADCAST_LEVELDIFFICULTYCHANGED, (byte) levelDifficulty);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendUnreliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId());
        }
    }

    public void broadcast_packsChanged(int packsPosition) {
        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_CATEGORYSELECTION);
        realTimeMultiplayerMessage.setData((byte) Constants.BROADCAST_PACKSCHANGED, (byte) packsPosition);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendUnreliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId());
        }
    }

    // highlight the difficulty that is passed in in param, and set all the rest to default inactive state
    private void highlightDifficultySelection(int difficulty, boolean firstTime) {
        int lightGrayTextColor = ContextCompat.getColor(this, R.color.translucent_white);
        int whiteColor = ContextCompat.getColor(this, R.color.white);
        // set all to default inactive state, except for the param state which is set to active highlighted state
        switch (difficulty) {
            case Constants.EASY_MODE:
                easyButton.setBackgroundResource(R.drawable.difficultyframechecked);
                easyTextView.setTextColor(whiteColor);
                easyImageView.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));

                mediumButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                hardButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                insaneButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                mediumTextView.setTextColor(lightGrayTextColor);
                hardTextView.setTextColor(lightGrayTextColor);
                insaneTextView.setTextColor(lightGrayTextColor);
                mediumImageView.setColorFilter(null);
                hardImageView.setColorFilter(null);
                insaneImageView.setColorFilter(null);

                selectedLevelDifficulty = Constants.EASY_MODE; // used in start button to determine what difficulty to go to in game mode
                if (!firstTime) {
                    ((AllCategoryMultiplayer) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.EASY_MODE);
                }
                popupStartButton.setAlpha(1f);
                popupStartButton.setClickable(true); // now no insane yet so make it unclickable
                break;

            case Constants.MEDIUM_MODE:
                mediumButton.setBackgroundResource(R.drawable.difficultyframechecked);
                mediumTextView.setTextColor(whiteColor);
                mediumImageView.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));

                easyButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                hardButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                insaneButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                easyTextView.setTextColor(lightGrayTextColor);
                hardTextView.setTextColor(lightGrayTextColor);
                insaneTextView.setTextColor(lightGrayTextColor);
                easyImageView.setColorFilter(null);
                hardImageView.setColorFilter(null);
                insaneImageView.setColorFilter(null);

                selectedLevelDifficulty = Constants.MEDIUM_MODE;
                if (!firstTime) {
                    ((AllCategoryMultiplayer) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.MEDIUM_MODE);
                }
                popupStartButton.setAlpha(1f);
                popupStartButton.setClickable(true); // now no insane yet so make it unclickable
                break;
            case Constants.HARD_MODE:
                hardButton.setBackgroundResource(R.drawable.difficultyframechecked);
                hardTextView.setTextColor(whiteColor);
                hardImageView.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));

                easyButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                mediumButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                insaneButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                easyTextView.setTextColor(lightGrayTextColor);
                mediumTextView.setTextColor(lightGrayTextColor);
                insaneTextView.setTextColor(lightGrayTextColor);
                easyImageView.setColorFilter(null);
                mediumImageView.setColorFilter(null);
                insaneImageView.setColorFilter(null);

                selectedLevelDifficulty = Constants.HARD_MODE;
                if (!firstTime) {
                    ((AllCategoryMultiplayer) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.HARD_MODE);
                }
                popupStartButton.setAlpha(1f);
                popupStartButton.setClickable(true); // now no insane yet so make it unclickable
                break;
            case Constants.INSANE_MODE:
                insaneButton.setBackgroundResource(R.drawable.difficultyframechecked);
                insaneTextView.setTextColor(whiteColor);
                insaneImageView.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));

                easyButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                mediumButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                hardButton.setBackgroundResource(R.drawable.difficultyframeuncheck);
                easyTextView.setTextColor(lightGrayTextColor);
                mediumTextView.setTextColor(lightGrayTextColor);
                hardTextView.setTextColor(lightGrayTextColor);
                easyImageView.setColorFilter(null);
                mediumImageView.setColorFilter(null);
                hardImageView.setColorFilter(null);

                selectedLevelDifficulty = Constants.INSANE_MODE;
                if (!firstTime) {
                    ((AllCategoryMultiplayer) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.INSANE_MODE);
                }
                popupStartButton.setAlpha(0.3f);
                popupStartButton.setClickable(false); // now no insane yet so make it unclickable
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (inGame) {
            multiPlayerActualGame.onBackPressed();
        } else {
            goingToPreviousActivity = true; // for main menu sound
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (multiPlayerActualGame != null && multiPlayerActualGame.timer != null) {
            multiPlayerActualGame.timer.cancel();
            multiPlayerActualGame.timer = null;
        }

        releaseMediaPlayer();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // for background music song
        if (onResumeCalledBefore && !inGame) {
            // will not be called the first time this activity is instantiated, since the music is alr playing
            // will be called in subseqent called to onResume, which will occur when app go to background and come back to foregoround again
            MusicManager.initializeAndPlayMediaPlayer(this, Utils.getIfSoundIsMuted(this));
        }

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        handshakeConnection.signInSilently();

        isAppRunning = true; // set back to true to allow playMediaPlayer to play sound


        if (onResumeCalledBefore) {
            // we only play background music onResume if onResume has alr been called out once, after onCreate -> onResume
            // this is not to overlap and call playMediaPlayer twice, since onCreate will call playMediaPlayer(1) itself too.
            // hence, this field is for when user go to background, and onResume again, play sound after
            playMediaPlayer(1);
        }
        // this call determines that onResume is called once before.
        onResumeCalledBefore = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Constants.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);

            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                handshakeConnection.onConnected(signedInAccount);
                Utils.setIfJustSignedIn(MultiPlayerGame.this, true);

            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        } else if (requestCode == Constants.RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handshakeConnection.handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == Constants.RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handshakeConnection.handleInvitationInboxResult(resultCode, intent);

        } else if (requestCode == Constants.RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                goToCategorySelection();
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                handshakeConnection.leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                handshakeConnection.leaveRoom();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // go to category selection again after restart
    private void goToCategorySelectionFromRestart() {
        inGame = false;
        handshakeConnection.switchToScreen(Screen.AFTER_RESTART);

        // initialize all 4 default spells
        spellQueue.add(Utils.getFirstSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getSecondSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getThirdSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getForthSelectedSpell(MultiPlayerGame.this));

        onRestartChangePlayerIcon();
    }

    private void onRestartChangePlayerIcon() {
        LinearLayout playerIconLinearLayout_top = findViewById(R.id.multiplayer_playerIconLinearLayoutTop);
        LinearLayout playerIconLinearLayout_bottom = findViewById(R.id.multiplayer_playerIconLinearLayoutBottom);

        int size = getResources().getDimensionPixelSize(R.dimen._32sdp);

        // set player icon on category selection top right corner
        for (int i = 0; i < handshakeConnection.mParticipants.size(); i++) {
            // put a player name to its image view
            String participantID = handshakeConnection.mParticipants.get(i).getParticipantId();

            ImageView imageView = playerIconImageViewMap.get(participantID).getPlayerIcon();

            setPlayerIconToGray(imageView);

            if (i <= 5) {
                playerIconLinearLayout_top.addView(imageView, new LinearLayout.LayoutParams(size, size));
            } else {
                playerIconLinearLayout_bottom.addView(imageView, new LinearLayout.LayoutParams(size, size));
            }
        }
    }

    private void broadcast_myActualName() {
        String myName = Utils.getPlayerName(MultiPlayerGame.this);
        byte[] myNameByteArr = myName.getBytes();
        byte[] byteArrToSend = new byte[myNameByteArr.length + 1];
        byteArrToSend[0] = Constants.MESSAGE_PLAYERNAME;
        System.arraycopy(myNameByteArr, 0, byteArrToSend, 1, myNameByteArr.length);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(byteArrToSend,
                    handshakeConnection.mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {

                        }
                    });
        }
    }

    // go the select the level difficulty and pack screen from very first time
    private void goToCategorySelection() {
        broadcast_myActualName(); // tell everyone my actual name
        handshakeConnection.switchToScreen(Screen.CATEGORYSELECTION);

        // initialize all 4 default spells
        spellQueue.add(Utils.getFirstSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getSecondSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getThirdSelectedSpell(MultiPlayerGame.this));
        spellQueue.add(Utils.getForthSelectedSpell(MultiPlayerGame.this));

        initializeVariables();
        setUpViewPager();
        initializeOnclickEvents();
        initializeGraphics();

        // set up the player icon on top
        LinearLayout playerIconLinearLayout_top = findViewById(R.id.multiplayer_playerIconLinearLayoutTop);
        LinearLayout playerIconLinearLayout_bottom = findViewById(R.id.multiplayer_playerIconLinearLayoutBottom);

        // make default unready
        popupStartButton.setAlpha(0.5f);

        int size = getResources().getDimensionPixelSize(R.dimen._32sdp);

        // set player icon on category selection top right corner
        for (int i = 0; i < handshakeConnection.mParticipants.size(); i++) {
            // put a player name to its image view
            String participantID = handshakeConnection.mParticipants.get(i).getParticipantId();
            PlayerIcon playerIcon = new PlayerIcon(new ImageView(MultiPlayerGame.this));

            playerIconImageViewMap.put(participantID, playerIcon);

            ImageView imageView = playerIconImageViewMap.get(participantID).getPlayerIcon();

            setPlayerIconToGray(imageView);

            ImageManager manager = ImageManager.create(this);
            manager.loadImage(imageView, handshakeConnection.mParticipants.get(i).getIconImageUri());

            if (i <= 5) {
                playerIconLinearLayout_top.addView(imageView, new LinearLayout.LayoutParams(size, size));
            } else {
                playerIconLinearLayout_bottom.addView(imageView, new LinearLayout.LayoutParams(size, size));
            }
        }
    }

    private void setPlayerIconToGray(ImageView playerIcon) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        playerIcon.setColorFilter(cf);
        playerIcon.setImageAlpha(128);   // 128 = 0.5
    }

    private void setPlayerIconToFullColor(ImageView playerIcon) {
        playerIcon.setColorFilter(null);
        playerIcon.setImageAlpha(255);
    }

    // clicked the ready button, wait for all other players to be ready
    private void readyButtonClicked(String participantID, int readyValue) {
        PlayerIcon playerIcon = playerIconImageViewMap.get(participantID);
        if (readyValue == Constants.IS_READY) {
            // at first unready, now ready him
            // not ready before, now is ready
            amountOfPlayersReady++;
            playerIcon.setIsReady(true);
            // set to full color
            setPlayerIconToFullColor(playerIcon.getPlayerIcon());

            if (isAllPlayerReady()) {
                // all players ready, prevent anymore clicking
                popupStartButton.setClickable(false);
                startGame();
                popupStartButton.setClickable(true);
            }
        } else {
            // unready the player
            amountOfPlayersReady--;
            playerIcon.setIsReady(false);
            // set to gray
            setPlayerIconToGray(playerIcon.getPlayerIcon());

        }
    }

    // clear states so that we can click restart again
    private void clearStatesForRestart() {
        popupStartButton.setAlpha(0.5f);
        // clear all imageviews to be added later in game end menu
        LinearLayout playerIconLinearLayout_top = findViewById(R.id.multiplayer_playerIconLinearLayoutTop);
        LinearLayout playerIconLinearLayout_bottom = findViewById(R.id.multiplayer_playerIconLinearLayoutBottom);
        playerIconLinearLayout_top.removeAllViews();
        playerIconLinearLayout_bottom.removeAllViews();

        for (PlayerIcon playerIcon : playerIconImageViewMap.values()) {
            playerIcon.setIsReady(false);
            setPlayerIconToGray(playerIcon.getPlayerIcon());
        }

        // reset var
        amountOfPlayersWantToRestart = 0;
        amountOfPlayersReady = 0;
    }

    private void broadcast_sendReadyBroadcast(final int readyValue) {
        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_CATEGORYSELECTION);
        realTimeMultiplayerMessage.setData((byte) Constants.BROADCAST_READY, (byte) readyValue);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendUnreliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId());
        }
    }

    private void ready_broadcastReceived(String participantId, int readyValue) {
        readyButtonClicked(participantId, readyValue);
    }

    // return true if all players are ready, false if at least 1 player not ready
    private boolean isAllPlayerReady() {
        return amountOfPlayersReady == handshakeConnection.mParticipants.size();
    }

    // Start the gameplay phase of the game.
    private void startGame() {
        // stop previous sound
        MusicManager.stopMediaPlayer();

        inGame = true;
        Utils.setDifficultyPreference(MultiPlayerGame.this, selectedLevelDifficulty);
        Utils.setCategoryPack(MultiPlayerGame.this, Constants.currentPackSelection);
        handshakeConnection.switchToScreen(Screen.ACTUAL_GAME);

        int firstSpell = spellQueue.poll();
        int secondSpell = spellQueue.poll();
        int thirdSpell = spellQueue.poll();
        int forthSpell = spellQueue.poll();
        Spell[] spellArray = new Spell[4];
        spellArray[0] = new Spell(firstSpell, isSpellActive(firstSpell), willSpellAffectOthers(firstSpell));
        spellArray[1] = new Spell(secondSpell, isSpellActive(secondSpell), willSpellAffectOthers(secondSpell));
        spellArray[2] = new Spell(thirdSpell, isSpellActive(thirdSpell), willSpellAffectOthers(thirdSpell));
        spellArray[3] = new Spell(forthSpell, isSpellActive(forthSpell), willSpellAffectOthers(forthSpell));
        Utils.setFirstSelectedSpell(MultiPlayerGame.this, firstSpell);
        Utils.setSecondSelectedSpell(MultiPlayerGame.this, secondSpell);
        Utils.setThirdSelectedSpell(MultiPlayerGame.this, thirdSpell);
        Utils.setForthSelectedSpell(MultiPlayerGame.this, forthSpell);

        userPassiveSpell.clear();
        // see if user has 2x exp
        if (firstSpell == Constants.SPELL_TWOTIMESEXP || secondSpell == Constants.SPELL_TWOTIMESEXP || thirdSpell == Constants.SPELL_TWOTIMESEXP ||
                forthSpell == Constants.SPELL_TWOTIMESEXP) {
            userPassiveSpell.add(Constants.SPELL_TWOTIMESEXP);
        }
        // see if user has guardian angel
        if (firstSpell == Constants.SPELL_GUARDIANANGEL || secondSpell == Constants.SPELL_GUARDIANANGEL || thirdSpell == Constants.SPELL_GUARDIANANGEL ||
                forthSpell == Constants.SPELL_GUARDIANANGEL) {
            userPassiveSpell.add(Constants.SPELL_GUARDIANANGEL);
        }

        // create the main game UI
        handshakeConnection.switchToScreen(Screen.ACTUAL_GAME);

        if (playingGameDueToRestart) {
            // playing rematch
            multiPlayerActualGame.onRestartStartingGame(spellArray);
        } else {
            // first time play
            multiPlayerActualGame = new MultiPlayerActualGame(this, handshakeConnection, spellArray);
        }

        // clear states so that we can come back again on restarting the game
        clearStatesForRestart();
    }


    // helper method, returns true if spell is an active, false if it is passive
    private boolean isSpellActive(int spellID) {
        switch (spellID) {
            case Constants.SPELL_BLOCK:
                return true;
            case Constants.SPELL_HEX:
                return true;
            case Constants.SPELL_JUMBLE:
                return true;
            case Constants.SPELL_THROW:
                return true;
            case Constants.SPELL_REVERSE:
                return true;
            case Constants.SPELL_SEEMORE:
                return true;
            case Constants.SPELL_TILT:
                return true;
            case Constants.SPELL_GUARDIANANGEL:
                return false;
            case Constants.SPELL_TWOTIMESEXP:
                return false;
        }
        return true;
    }

    // helper method, returns true if spell will affect other players, false if only affect ownself
    private boolean willSpellAffectOthers(int spellID) {
        switch (spellID) {
            case Constants.SPELL_BLOCK:
                return true;
            case Constants.SPELL_HEX:
                return true;
            case Constants.SPELL_JUMBLE:
                return true;
            case Constants.SPELL_THROW:
                return true;
            case Constants.SPELL_REVERSE:
                return true;
            case Constants.SPELL_SEEMORE:
                return false;
            case Constants.SPELL_TILT:
                return true;
            case Constants.SPELL_GUARDIANANGEL:
                return false;
            case Constants.SPELL_TWOTIMESEXP:
                return false;
        }
        return true;
    }

    // opponent score increases, hence update their score in ranking
    private void updateOpponentScore(byte id, byte scoreToUpdate) {
        multiPlayerActualGame.updateOpponentScore(id, scoreToUpdate);
    }

    // opponent used a spell, i kena hit
    private void hitByOpponentSpell(int spellID, byte fromWhoUniqueID, byte[] forThrowUsed) {
        multiPlayerActualGame.hitByOpponentSpell(spellID, fromWhoUniqueID, forThrowUsed);
    }

    @Override
    public void onSpellSelected(int spellID) {
        spellQueue.add(spellID); // add to last
        spellQueue.poll(); // remove first
    }

    @Override
    public void onCategoryPackClicked(int position) {
        // whenever a category pack is clicked we broadcast it
        Log.e(TAG, "here on categorypack clicked");
        broadcast_packsChanged(position);
    }

    class HandshakeUI {
        private ImageView signInButton, backButton, inviteFriends_Button, seeInvitation_Button,
                signOutButton, googleIconImageView, inviteFriendsImageView, seeInvitationImageView,
                quickPlayButton, quickPlayImageView;
        private RelativeLayout invitation_relativeLayout;
        private Button invitation_acceptButton;
        private TextView invitation_questionTextView;
        private TextView signIn_helpTextView, signIn_TextView, signOutTextView, inviteFriendsTextView,
                seeInvitationTextView, quickPlayTextView, waitscreen_loadingTextView;
        private ProgressBar waitscreen_progressBar;

        HandshakeUI(ClickListener clickListener, ImageView signInButton, ImageView backButton,
                    ImageView inviteFriends_Button, ImageView seeInvitation_Button,
                    ImageView signOutButton, ImageView googleIconImageView,
                    ImageView inviteFriendsImageView, ImageView seeInvitationImageView,
                    RelativeLayout invitation_relativeLayout, Button invitation_acceptButton,
                    TextView invitation_questionTextView, TextView signIn_helpTextView,
                    TextView signIn_TextView, TextView signOutTextView, TextView inviteFriendsTextView,
                    TextView seeInvitationTextView, ImageView quickPlay_Button, TextView quickPlayTextView,
                    ImageView quickPlayImageView, TextView waitscreen_loadingTextView, ProgressBar progressBar) {
            this.signInButton = signInButton;
            this.backButton = backButton;
            this.inviteFriends_Button = inviteFriends_Button;
            this.seeInvitation_Button = seeInvitation_Button;
            this.signOutButton = signOutButton;
            this.googleIconImageView = googleIconImageView;
            this.inviteFriendsImageView = inviteFriendsImageView;
            this.seeInvitationImageView = seeInvitationImageView;
            this.invitation_relativeLayout = invitation_relativeLayout;
            this.invitation_acceptButton = invitation_acceptButton;
            this.invitation_questionTextView = invitation_questionTextView;
            this.signIn_helpTextView = signIn_helpTextView;
            this.signIn_TextView = signIn_TextView;
            this.signOutTextView = signOutTextView;
            this.inviteFriendsTextView = inviteFriendsTextView;
            this.seeInvitationTextView = seeInvitationTextView;
            this.quickPlayButton = quickPlay_Button;
            this.quickPlayTextView = quickPlayTextView;
            this.quickPlayImageView = quickPlayImageView;
            this.waitscreen_loadingTextView = waitscreen_loadingTextView;
            this.waitscreen_progressBar = progressBar;

            this.signInButton.setOnClickListener(clickListener);
            this.backButton.setOnClickListener(clickListener);
            this.quickPlayButton.setOnClickListener(clickListener);
            this.inviteFriends_Button.setOnClickListener(clickListener);
            this.seeInvitation_Button.setOnClickListener(clickListener);
            this.signOutButton.setOnClickListener(clickListener);
            this.invitation_acceptButton.setOnClickListener(clickListener);
        }

        public TextView getWaitscreen_loadingTextView() {
            return waitscreen_loadingTextView;
        }

        public ProgressBar getWaitscreen_progressBar() {
            return waitscreen_progressBar;
        }

        public ImageView getSignInButton() {
            return signInButton;
        }

        public ImageView getBackButton() {
            return backButton;
        }

        public ImageView getInviteFriends_Button() {
            return inviteFriends_Button;
        }

        public ImageView getSeeInvitation_Button() {
            return seeInvitation_Button;
        }

        public ImageView getSignOutButton() {
            return signOutButton;
        }

        public ImageView getGoogleIconImageView() {
            return googleIconImageView;
        }

        public ImageView getInviteFriendsImageView() {
            return inviteFriendsImageView;
        }

        public ImageView getSeeInvitationImageView() {
            return seeInvitationImageView;
        }

        public RelativeLayout getInvitation_relativeLayout() {
            return invitation_relativeLayout;
        }

        public Button getInvitation_acceptButton() {
            return invitation_acceptButton;
        }

        public ImageView getQuickPlayButton() {
            return quickPlayButton;
        }

        public ImageView getQuickPlayImageView() {
            return quickPlayImageView;
        }

        public TextView getQuickPlayTextView() {
            return quickPlayTextView;
        }

        public TextView getInvitation_questionTextView() {
            return invitation_questionTextView;
        }

        public TextView getSignIn_helpTextView() {
            return signIn_helpTextView;
        }

        public TextView getSignIn_TextView() {
            return signIn_TextView;
        }

        public TextView getSignOutTextView() {
            return signOutTextView;
        }

        public TextView getInviteFriendsTextView() {
            return inviteFriendsTextView;
        }

        public TextView getSeeInvitationTextView() {
            return seeInvitationTextView;
        }
    }

    class HandshakeConnection {
        final static String TAG = "gggg";
        private HandshakeUI handshakeUI;
        private Context context;

        // Client used to sign in with Google APIs
        private GoogleSignInClient mGoogleSignInClient = null;

        // Client used to interact with the real time multiplayer system.
        RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

        // Client used to interact with the Invitation system.
        private InvitationsClient mInvitationsClient = null;

        // Room ID where the currently active game is taking place; null if we're
        // not playing.
        String mRoomId = null;

        // Holds the configuration of the current room.
        RoomConfig mRoomConfig;

        // The participants in the currently active game
        ArrayList<Participant> mParticipants = null;

        // My participant ID in the currently active game
        String mMyId = null;

        // If non-null, this is the id of the invitation we received via the
        // invitation listener
        String mIncomingInvitationId = null;

        HandshakeConnection(Context context, HandshakeUI handshakeUI) {
            // Create the client used to sign in.
            mGoogleSignInClient = GoogleSignIn.getClient(MultiPlayerGame.this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                            // Since we are using SavedGames, we need to add the SCOPE_APPFOLDER to access Google Drive.
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build());
            this.handshakeUI = handshakeUI;
            this.context = context;
        }

        HashMap<String, String> playerNameMap = new HashMap<>(); // map player google display name to their in game display name


        /**
         * Try to sign in without displaying dialogs to the user.
         * <p>
         * If the user has already signed in previously, it will not show dialog.
         */
        public void signInSilently() {
            Log.d(TAG, "signInSilently()");
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(((Activity) context),
                    new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInSilently(): success");
                                onConnected(task.getResult());
                            } else {
                                Log.d(TAG, "signInSilently(): failure", task.getException());
                                onDisconnected();
                            }
                        }
                    });
        }


        /*
         * CALLBACKS SECTION. This section shows how we implement the several games
         * API callbacks.
         */
        private String mPlayerId;

        // The currently signed in account, used to check the account has changed outside of this activity when resuming.
        GoogleSignInAccount mSignedInAccount = null;

        void onConnected(GoogleSignInAccount googleSignInAccount) {
            Log.d(TAG, "onConnected(): connected to Google APIs");
            Constants.signedIn = true;
            if (mSignedInAccount != googleSignInAccount) {

                mSignedInAccount = googleSignInAccount;

                // update the clients
                mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(context, googleSignInAccount);
                mInvitationsClient = Games.getInvitationsClient(context, googleSignInAccount);

                // get the playerId from the PlayersClient
                PlayersClient playersClient = Games.getPlayersClient(context, googleSignInAccount);
                playersClient.getCurrentPlayer()
                        .addOnSuccessListener(new OnSuccessListener<Player>() {
                            @Override
                            public void onSuccess(Player player) {
                                mPlayerId = player.getPlayerId();

                                switchToMainScreen();
                            }
                        })
                        .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
            }

            // register listener so we are notified if we receive an invitation to play
            // while we are in the game
            mInvitationsClient.registerInvitationCallback(mInvitationCallback);

            // get the invitation from the connection hint
            // Retrieve the TurnBasedMatch from the connectionHint
            GamesClient gamesClient = Games.getGamesClient(context, googleSignInAccount);
            gamesClient.getActivationHint()
                    .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                        @Override
                        public void onSuccess(Bundle hint) {
                            if (hint != null) {
                                Invitation invitation =
                                        hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                                if (invitation != null && invitation.getInvitationId() != null) {
                                    // retrieve and cache the invitation ID
                                    Log.d(TAG, "onConnected: connection hint has a room invite!");
                                    acceptInviteToRoom(invitation.getInvitationId());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
        }

        void onDisconnected() {
            Log.d(TAG, "onDisconnected()");

            Constants.signedIn = false;
            mRealTimeMultiplayerClient = null;
            mInvitationsClient = null;

            switchToMainScreen();
        }

        void signOut() {
            Log.d(TAG, "signOut()");

            mGoogleSignInClient.signOut().addOnCompleteListener((Activity) context,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Log.d(TAG, "signOut(): success");
                            } else {
                                handleException(task.getException(), "signOut() failed!");
                            }

                            onDisconnected();
                        }
                    });
        }


        // Show the waiting room UI to track the progress of other players as they enter the
        // room and get connected.
        private void showWaitingRoom(Room room) {
            // minimum number of players required for our game
            // For simplicity, we require everyone to join the game before we start it
            // (this is signaled by Integer.MAX_VALUE).
            final int MIN_PLAYERS = Integer.MAX_VALUE;
            mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            // show waiting room UI
                            ((Activity) context).startActivityForResult(intent, Constants.RC_WAITING_ROOM);
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
        }

        private InvitationCallback mInvitationCallback = new InvitationCallback() {
            // Called when we get an invitation to play a game. We react by showing that to the user.
            @Override
            public void onInvitationReceived(@NonNull Invitation invitation) {
                // We got an invitation to play a game! So, store it in
                // mIncomingInvitationId
                // and show the popup on the screen.
                mIncomingInvitationId = invitation.getInvitationId();

                handshakeUI.getInvitation_questionTextView().setText(
                        invitation.getInviter().getDisplayName() + " " +
                                context.getString(R.string.is_inviting_you));

                switchToScreen(mCurScreen); // This will show the invitation popup
            }

            @Override
            public void onInvitationRemoved(@NonNull String invitationId) {

                if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                    mIncomingInvitationId = null;
                    switchToScreen(mCurScreen); // This will hide the invitation popup
                }
            }
        };

        private Screen mCurScreen = Screen.SIGNOUTSCREEN;


        void switchToScreen(Screen screen) {
            mCurScreen = screen;
            switch (screen) {
                case WAIT_SCREEN:
                    handshakeUI.getInviteFriends_Button().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitation_Button().setVisibility(View.GONE);
                    handshakeUI.getSignOutButton().setVisibility(View.GONE);
                    handshakeUI.getSignOutTextView().setVisibility(View.GONE);
                    handshakeUI.getInviteFriendsImageView().setVisibility(View.GONE);
                    handshakeUI.getInviteFriendsTextView().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitationImageView().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitationTextView().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayButton().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayImageView().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayTextView().setVisibility(View.GONE);
                    handshakeUI.getWaitscreen_progressBar().setVisibility(View.VISIBLE);
                    handshakeUI.getWaitscreen_loadingTextView().setVisibility(View.VISIBLE);
                    break;

                case SIGNOUTSCREEN:
                    handshakeUI.getInviteFriends_Button().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitation_Button().setVisibility(View.GONE);
                    handshakeUI.getSignOutButton().setVisibility(View.GONE);
                    handshakeUI.getSignOutTextView().setVisibility(View.GONE);
                    handshakeUI.getInviteFriendsImageView().setVisibility(View.GONE);
                    handshakeUI.getInviteFriendsTextView().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitationImageView().setVisibility(View.GONE);
                    handshakeUI.getSeeInvitationTextView().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayButton().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayImageView().setVisibility(View.GONE);
                    handshakeUI.getQuickPlayTextView().setVisibility(View.GONE);

                    handshakeUI.getSignInButton().setVisibility(View.VISIBLE);
                    handshakeUI.getSignIn_helpTextView().setVisibility(View.VISIBLE);
                    handshakeUI.getSignIn_TextView().setVisibility(View.VISIBLE);
                    handshakeUI.getGoogleIconImageView().setVisibility(View.VISIBLE);
                    break;

                case SIGNIN_SCREEN:
                    handshakeUI.getInviteFriends_Button().setVisibility(View.VISIBLE);
                    handshakeUI.getSeeInvitation_Button().setVisibility(View.VISIBLE);
                    handshakeUI.getSignOutButton().setVisibility(View.VISIBLE);
                    handshakeUI.getSignOutTextView().setVisibility(View.VISIBLE);
                    handshakeUI.getInviteFriendsImageView().setVisibility(View.VISIBLE);
                    handshakeUI.getInviteFriendsTextView().setVisibility(View.VISIBLE);
                    handshakeUI.getSeeInvitationImageView().setVisibility(View.VISIBLE);
                    handshakeUI.getSeeInvitationTextView().setVisibility(View.VISIBLE);
                    handshakeUI.getQuickPlayButton().setVisibility(View.VISIBLE);
                    handshakeUI.getQuickPlayImageView().setVisibility(View.VISIBLE);
                    handshakeUI.getQuickPlayTextView().setVisibility(View.VISIBLE);

                    handshakeUI.getWaitscreen_progressBar().setVisibility(View.GONE);
                    handshakeUI.getWaitscreen_loadingTextView().setVisibility(View.GONE);

                    handshakeUI.getSignInButton().setVisibility(View.GONE);
                    handshakeUI.getSignIn_helpTextView().setVisibility(View.GONE);
                    handshakeUI.getSignIn_TextView().setVisibility(View.GONE);
                    handshakeUI.getGoogleIconImageView().setVisibility(View.GONE);
                    break;

                case CATEGORYSELECTION:
                    findViewById(R.id.multiplayer_handshake_overallLayout).setVisibility(View.GONE);
                    findViewById(R.id.multiplayer_popupSelection_overallLayout).setVisibility(View.VISIBLE);
                    break;

                case ACTUAL_GAME:
                    findViewById(R.id.multiplayer_popupSelection_overallLayout).setVisibility(View.GONE);
                    findViewById(R.id.multiplayer_actualGameOverallLayout).setVisibility(View.VISIBLE);
                    break;

                case AFTER_RESTART:
                    findViewById(R.id.multiplayer_actualGameOverallLayout).setVisibility(View.GONE);
                    findViewById(R.id.multiplayer_popupSelection_overallLayout).setVisibility(View.VISIBLE);
                    break;
            }

            // should we show the invitation popup?
            boolean showInvPopup;

            if (mIncomingInvitationId == null) {
                // no invitation, so no popup
                showInvPopup = false;
            } else {
                // only show invitation on main screen
                showInvPopup = (mCurScreen == Screen.SIGNIN_SCREEN);
            }

            handshakeUI.getInvitation_relativeLayout().setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        }

        /**
         * Main screen is the screen where user is presented with the button of find friends, see invitation etc
         */
        private void switchToMainScreen() {
            if (mRealTimeMultiplayerClient != null) {
                switchToScreen(Screen.SIGNIN_SCREEN);
            } else {
                switchToScreen(Screen.SIGNOUTSCREEN);
            }
        }

        // Accept the given invitation.
        private void acceptInviteToRoom(String invitationId) {
            // accept the invitation
            Log.d(TAG, "Accepting invitation: " + invitationId);

            mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                    .setInvitationIdToAccept(invitationId)
                    .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                    .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                    .build();

            switchToScreen(Screen.WAIT_SCREEN);
            keepScreenOn();

            mRealTimeMultiplayerClient.join(mRoomConfig)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Room Joined Successfully!");
                        }
                    });
        }

        // Handle the result of the "Select players UI" we launched when the user clicked the
        // "Invite friends" button. We react by creating a room with those players.

        void handleSelectPlayersResult(int response, Intent data) {
            if (response != Activity.RESULT_OK) {
                Log.w(TAG, "*** select players UI cancelled, " + response);
                switchToMainScreen();
                return;
            }

            Log.d(TAG, "Select players UI succeeded.");

            // get the invitee list
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            Log.d(TAG, "Invitee count: " + invitees.size());

            // get the automatch criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
                Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
            }

            // create the room
            Log.d(TAG, "Creating room...");
            switchToScreen(Screen.WAIT_SCREEN);

            keepScreenOn();

            mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                    .addPlayersToInvite(invitees)
                    .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                    .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                    .setAutoMatchCriteria(autoMatchCriteria).build();
            mRealTimeMultiplayerClient.create(mRoomConfig);
            Log.d(TAG, "Room created, waiting for it to be ready...");
        }

        // Handle the result of the invitation inbox UI, where the player can pick an invitation
        // to accept. We react by accepting the selected invitation, if any.
        void handleInvitationInboxResult(int response, Intent data) {
            if (response != Activity.RESULT_OK) {
                Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
                switchToMainScreen();
                return;
            }

            Log.d(TAG, "Invitation inbox UI succeeded.");
            Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

            // accept invitation
            if (invitation != null) {
                acceptInviteToRoom(invitation.getInvitationId());
            }
        }

        private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
            // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
            // is connected yet).
            @Override
            public void onConnectedToRoom(Room room) {
                Log.d(TAG, "onConnectedToRoom.");

                //get participants and my ID:
                mParticipants = room.getParticipants();
                mMyId = room.getParticipantId(mPlayerId);

                // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
                if (mRoomId == null) {
                    mRoomId = room.getRoomId();
                }

                // print out the list of participants (for debug purposes)
                Log.d(TAG, "Room ID: " + mRoomId);
                Log.d(TAG, "My ID " + mMyId);
                Log.d(TAG, "<< CONNECTED TO ROOM>>");
            }

            // Called when we get disconnected from the room. We return to the main screen.
            @Override
            public void onDisconnectedFromRoom(Room room) {
                mRoomId = null;
                mRoomConfig = null;
                showGameError();
            }


            // We treat most of the room update callbacks in the same way: we update our list of
            // participants and update the display. In a real game we would also have to check if that
            // change requires some action like removing the corresponding player avatar from the screen,
            // etc.
            @Override
            public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onP2PDisconnected(@NonNull String participant) {
            }

            @Override
            public void onP2PConnected(@NonNull String participant) {
            }

            @Override
            public void onPeerJoined(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
                //updateRoom(room);
            }

            @Override
            public void onRoomAutoMatching(Room room) {
                updateRoom(room);
            }

            @Override
            public void onRoomConnecting(Room room) {
                updateRoom(room);
            }

            @Override
            public void onPeersConnected(Room room, @NonNull List<String> peers) {
                updateRoom(room);
            }

            @Override
            public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
                //updateRoom(room);
            }
        };

        /**
         * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
         * your Activity's onActivityResult function
         */
        public void startSignInIntent() {
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), Constants.RC_SIGN_IN);
        }


        private void updateRoom(Room room) {
            if (room != null) {
                mParticipants = room.getParticipants();
            }
        }

        // Leave the room.
        void leaveRoom() {
            Log.d(TAG, "Leaving room.");

            stopKeepingScreenOn();
            if (mRoomId != null) {
                mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mRoomId = null;
                                mRoomConfig = null;
                            }
                        });
                switchToScreen(Screen.WAIT_SCREEN);
            } else {
                switchToMainScreen();
            }
        }

        // Show error message about game being cancelled and return to main screen.
        private void showGameError() {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.game_problem))
                    .setNeutralButton(android.R.string.ok, null).create();

            switchToMainScreen();
        }

        private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

            // Called when room has been created
            @Override
            public void onRoomCreated(int statusCode, Room room) {
                Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                    showGameError();
                    return;
                }

                // save room ID so we can leave cleanly before the game starts.
                mRoomId = room.getRoomId();

                // show the waiting room UI
                showWaitingRoom(room);
            }

            // Called when room is fully connected.
            @Override
            public void onRoomConnected(int statusCode, Room room) {
                Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                    showGameError();
                    return;
                }
                updateRoom(room);
            }

            @Override
            public void onJoinedRoom(int statusCode, Room room) {
                Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                    showGameError();
                    return;
                }

                // show the waiting room UI
                showWaitingRoom(room);
            }

            // Called when we've successfully left the room (this happens a result of voluntarily leaving
            // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
            @Override
            public void onLeftRoom(int statusCode, @NonNull String roomId) {
                // we have left the room; return to main screen.
                Log.d(TAG, "onLeftRoom, code " + statusCode);
                switchToMainScreen();
            }
        };

        private OnFailureListener createFailureListener(final String string) {
            return new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleException(e, string);
                }
            };
        }

        /**
         * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
         *
         * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
         * @param details   Will display alongside the exception if you wish to provide more details for why the exception
         *                  happened
         */
        private void handleException(Exception exception, String details) {
            int status = 0;

            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                status = apiException.getStatusCode();
            }

            String errorString = null;
            switch (status) {
                case GamesCallbackStatusCodes.OK:
                    break;
                case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                    errorString = context.getString(R.string.status_multiplayer_error_not_trusted_tester);
                    break;
                case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                    errorString = context.getString(R.string.match_error_already_rematched);
                    break;
                case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                    errorString = context.getString(R.string.network_error_operation_failed);
                    break;
                case GamesClientStatusCodes.INTERNAL_ERROR:
                    errorString = context.getString(R.string.internal_error);
                    break;
                case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                    errorString = context.getString(R.string.match_error_inactive_match);
                    break;
                case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                    errorString = context.getString(R.string.match_error_locally_modified);
                    break;
                default:
                    errorString = context.getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                    break;
            }

            if (errorString == null) {
                return;
            }

            String message = context.getString(R.string.status_exception_error, details, status, exception);

            new AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(message + "\n" + errorString)
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }

        /*
         * COMMUNICATIONS SECTION. Methods that implement the game's network
         * protocol.
         */

        private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
            @Override
            public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
                byte[] buf = realTimeMessage.getMessageData();

                // this is the message code, used to determine what type of message received
                switch (buf[0]) {
                    case Constants.MESSAGE_CATEGORYSELECTION:
                        switch (buf[1]) {
                            case Constants.BROADCAST_LEVELDIFFICULTYCHANGED:
                                // level difficulty change
                                highlightDifficultySelection(buf[2], false);
                                break;
                            case Constants.BROADCAST_PACKSCHANGED:
                                // category change
                                changePacksDueToBroadcast(selectedLevelDifficulty, buf[2]);
                                break;
                            case Constants.BROADCAST_READY:
                                // someone pressed ready
                                ready_broadcastReceived(realTimeMessage.getSenderParticipantId(), buf[2]);
                                break;
                        }
                        break;

                    case Constants.MESSAGE_SPELLUSED:
                        hitByOpponentSpell(buf[1], buf[2], buf);
                        break;
                    case Constants.MESSAGE_UPDATESCORE:
                        updateOpponentScore(buf[1], buf[2]);
                        break;

                    case Constants.MESSAGE_READY_GETREADYDIALOGSHOWN:
                        if (multiPlayerActualGame != null) {
                            Log.e("gggg", "received ready from others");
                            multiPlayerActualGame.onBroadcastReceive_getReady();
                        } else {
                            Log.e("gggg", "multiplayer null");
                        }
                        break;
                    case Constants.MESSAGE_RESTARTGAME:
                        switch (buf[1]) {
                            case Constants.RESTART_YES:
                                restartYesOrNo(realTimeMessage.getSenderParticipantId(), true);
                                break;
                            case Constants.RESTART_NO:
                                restartYesOrNo(realTimeMessage.getSenderParticipantId(), false);
                                break;
                        }
                    case Constants.MESSAGE_PLAYERNAME:
                        addPlayerActualName(realTimeMessage.getSenderParticipantId(), buf);
                        break;
                }
            }
        };

        // put the actual in game name of the player into the hash map
        private void addPlayerActualName(String playerParticipantId, byte[] theByteArrReceived) {
            String actualName;
            if (theByteArrReceived.length == 1) {
                // means empty name
                actualName = "";
            } else {
                // dont read the first element in the byte [] cos that is message code
                byte[] nameByteArr = Arrays.copyOfRange(theByteArrReceived, 1, theByteArrReceived.length);
                actualName = new String(nameByteArr);
            }
            playerNameMap.put(playerParticipantId, actualName);
        }


        // Sets the flag to keep this screen on. It's recommended to do that during
        // the handshake when setting up a game, because if the screen turns off, the
        // game will be
        // cancelled.
        private void keepScreenOn() {
            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Clears the flag that keeps the screen on.
        void stopKeepingScreenOn() {
            ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.multiplayer_signIn_Button:
                    // start the sign-in flow
                    SoundPoolManager.getInstance().playSound(0);
                    Log.d(TAG, "Sign-in button clicked");
                    handshakeConnection.startSignInIntent();
                    break;

                case R.id.multiplayer_signOut_Button:
                    // user wants to sign out sign out.
                    SoundPoolManager.getInstance().playSound(0);
                    Log.d(TAG, "Sign-out button clicked");
                    handshakeConnection.signOut();
                    handshakeConnection.switchToScreen(Screen.SIGNOUTSCREEN);
                    break;

                case R.id.multiplayer_quickPlay_Button:
                    SoundPoolManager.getInstance().playSound(0);
                    handshakeConnection.switchToScreen(Screen.WAIT_SCREEN);
                    startQuickGame();
                    break;

                case R.id.multiplayer_inviteFriends_Button:
                    SoundPoolManager.getInstance().playSound(0);
                    handshakeConnection.switchToScreen(Screen.WAIT_SCREEN);

                    // show list of invitable players
                    handshakeConnection.mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 7).addOnSuccessListener(
                            new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, Constants.RC_SELECT_PLAYERS);
                                }
                            }
                    ).addOnFailureListener(handshakeConnection.createFailureListener("There was a problem selecting opponents."));
                    break;

                case R.id.multiplayer_seeInvitation_Button:
                    SoundPoolManager.getInstance().playSound(0);
                    handshakeConnection.switchToScreen(Screen.WAIT_SCREEN);

                    // show list of pending invitations
                    handshakeConnection.mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                            new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, Constants.RC_INVITATION_INBOX);
                                }
                            }
                    ).addOnFailureListener(handshakeConnection.createFailureListener("There was a problem getting the inbox."));
                    break;

                case R.id.multiplayer_invitationAcceptButton:
                    SoundPoolManager.getInstance().playSound(0);
                    // user wants to accept the invitation shown on the invitation popup
                    // (the one we got through the OnInvitationReceivedListener).
                    handshakeConnection.acceptInviteToRoom(handshakeConnection.mIncomingInvitationId);
                    handshakeConnection.mIncomingInvitationId = null;
                    break;

                case R.id.multiplayer_backbutton:
                    SoundPoolManager.getInstance().playSound(2);
                    onBackPressed();
                    break;

                case R.id.popup_easyButton:
                    SoundPoolManager.getInstance().playSound(0); // play general click sound
                    // put here instead of inside highlightDifficultySelection becos that method is use at the start too,
                    // and at the start we all is easy, so no point broadcasting again.
                    broadcast_levelDifficultyChanged(Constants.EASY_MODE);
                    highlightDifficultySelection(Constants.EASY_MODE, false);
                    break;
                case R.id.popup_mediumButton:
                    SoundPoolManager.getInstance().playSound(0); // play general click sound
                    broadcast_levelDifficultyChanged(Constants.MEDIUM_MODE);
                    highlightDifficultySelection(Constants.MEDIUM_MODE, false);
                    break;
                case R.id.popup_hardButton:
                    SoundPoolManager.getInstance().playSound(0); // play general click sound
                    broadcast_levelDifficultyChanged(Constants.HARD_MODE);
                    highlightDifficultySelection(Constants.HARD_MODE, false);
                    break;
                case R.id.popup_insaneButton:
                    SoundPoolManager.getInstance().playSound(0); // play general click sound
                    broadcast_levelDifficultyChanged(Constants.INSANE_MODE);
                    highlightDifficultySelection(Constants.INSANE_MODE, false);
                    break;
                case R.id.popup_startButton:
                    SoundPoolManager.getInstance().playSound(6); // play start button sound
                    if (playerIconImageViewMap.get(handshakeConnection.mMyId).isReady()) {
                        // now i unready from ready
                        popupStartButton.setAlpha(0.5f);
                        Log.e("gggg", "i am not ready");
                        broadcast_sendReadyBroadcast(Constants.IS_NOT_READY);
                        readyButtonClicked(handshakeConnection.mMyId, Constants.IS_NOT_READY);
                    } else {
                        // now i ready from unready
                        popupStartButton.setAlpha(1f);
                        Log.e("gggg", "i am ready");
                        broadcast_sendReadyBroadcast(Constants.IS_READY);
                        readyButtonClicked(handshakeConnection.mMyId, Constants.IS_READY);
                    }
                    break;
            }
        }
    }

    enum Screen {
        // used for switching screens
        WAIT_SCREEN, SIGNIN_SCREEN, SIGNOUTSCREEN, ACTUAL_GAME, CATEGORYSELECTION, AFTER_RESTART;
    }

    class MultiplayerPageAdapter extends FragmentPagerAdapter {
        private int numOfTabs;
        private Context context;
        private Fragment m1stFragment;
        private AllCategoryMultiplayer allCategory;

        MultiplayerPageAdapter(FragmentManager fm, int numOfTabs, Context context) {
            super(fm);
            this.numOfTabs = numOfTabs;
            this.context = context;
            allCategory = new AllCategoryMultiplayer();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return context.getString(R.string.allcategories);
            } else {
                return context.getString(R.string.gamesetting);
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return allCategory;
                case 1:
                    return new MultiplayerGameSettings();
                default:
                    return null;
            }
        }

        public Fragment getAllCategoryFragment() {
            return m1stFragment == null ? allCategory : m1stFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    m1stFragment = (AllCategoryMultiplayer) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            return numOfTabs;
        }
    }

}

class MultiPlayerActualGame implements View.OnClickListener {
    // Our communication link to the EditText
    private InputConnection inputConnection;
    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    private SparseArray<String> keyValues = new SparseArray<>();
    private TextView streakTextView, timerTextView, scoreTextView, questionDescriptionTextView;
    private ImageView hintButton, skipButton, dragOutButton;
    private EditText editText;
    private String sourceArray[] = {"From wiki", "from wordnet"};
    private LinkedList<Question> questions = new LinkedList<>();
    private int streakCount, score;
    private Queue<Question> answered = new LinkedList<>();
    CountDownTimer timer;
    private long progress;
    private boolean gamePaused = false;
    private boolean requestedHint = false;
    private LinearLayout historyLayout;
    private ProgressBar progressBar;
    private HorizontalScrollView historyHorizontalScrollView;
    private boolean hintClickable = true; // true if hint button can be pressed
    private boolean goingToExit = false; // change to true if user clicked exit button in pause menu
    boolean gameHasEnded = false; // set to true when game ends
    private GameStatistics gameStatistics; // keep track of user statistics
    private Context context;
    private Activity aContext;
    private RelativeLayout slideRelativeLayout; // slider for ranking
    private boolean isSliderOpen = false;
    private ConstraintLayout sliderOverallLayout;
    private TextView exitButton; // the exit in slider
    private MultiPlayerGame.HandshakeConnection handshakeConnection;
    private PriorityQueue<GameParticipant> gameParticipantsPriorityQueue;
    // int is id to identify the gameparticipant
    // this is a hashmap used to change the priorityqueue priority
    private SparseArray<GameParticipant> gameParticipantSparseArray;
    private TextView rankingTextViewArray[];
    private byte myUniqueID; // the unique id representing MYSELF, to be sent in broadcast message
    private SpellManager spellManager; // call when kena hit by a spell, or passive activated
    private ImageView spell1_button, spell2_button, spell3_button, spell4_button;
    private Spell[] spellArray; // use to identify spell 1,2,3,4 is what spell
    // use to keep track of what spell is the player currently under influenced by, so that we can reverse
    private ExperiencingSpell experiencingSpell = new ExperiencingSpell();
    // use to keep track of what spell affecting me, so as to take them into account accordingly
    private HashSet<Integer> underSpellInfluenced = new HashSet<>();
    // use to indicate to start game already or not, if full, then we start countdown timer
    // this is used inside actual game display, where if full, we show animation get ready dismiss and start timer
    private int numberOfPlayersOnReady = 0;
    private TextView topText, bottomText; // for get ready dialog
    private boolean gameStarted = false;
    private MultiPlayerGame multiPlayerGame;
    private LinearLayout restartImageLinearLayoutArray[]; // for use in removing image views on restart
    private AlertDialog dialogEnd;
    TextView restartButton;
    private int levelDifficulty;
    private String packName;
    private StringBuffer editTextString = new StringBuffer(); // for keyboard
    private LinearLayout getReadyContainer;
    private NativeBannerAd nativeBannerAd;
    private static final String TAG = "gggg";
    private Question currentQn;
    private Question kenaThrownQuestion; // opponent thrown qn stored here
    private int numberOfSpellSuffered = 0; // used for guardian angel spell
    private int rotationDegree = 0; // used for rotation spell
    private boolean hasNewQuestions = false; // used for seemore edittext typing to match with text

    public MultiPlayerActualGame(final Context context, MultiPlayerGame.HandshakeConnection handshakeConnection, Spell[] spellArray) {
        this.context = context;
        this.aContext = (Activity) context;
        this.multiPlayerGame = (MultiPlayerGame) context;
        this.handshakeConnection = handshakeConnection;
        this.spellArray = spellArray;
        this.levelDifficulty = Utils.getDifficultyPreference(context);
        this.packName = Utils.getCategoryPack(context); // TODO point to correct packdata
        multiPlayerGame.isAppRunning = true;
        createWords(); // add words into our list

        nativeBannerAd = new NativeBannerAd(context, Utils.getPauseNativeAdIdMultiplayer());
        nativeBannerAd.loadAd();

        spellManager = new SpellManager();

        gameStatistics = new GameStatistics();

        streakTextView = ((Activity) context).findViewById(R.id.game_streakTextView);
        timerTextView = ((Activity) context).findViewById(R.id.game_timerTextView);
        scoreTextView = ((Activity) context).findViewById(R.id.game_scoreTextView);
        questionDescriptionTextView = ((Activity) context).findViewById(R.id.game_descriptionTextView);
        hintButton = ((Activity) context).findViewById(R.id.game_hintButton);
        skipButton = ((Activity) context).findViewById(R.id.game_skipButton);
        dragOutButton = ((Activity) context).findViewById(R.id.game_dragoutButton);
        progressBar = ((Activity) context).findViewById(R.id.game_progressbar);
        historyLayout = ((Activity) context).findViewById(R.id.game_historyLinearLayout);
        historyHorizontalScrollView = ((Activity) context).findViewById(R.id.game_historyHorizontalScrollView);
        slideRelativeLayout = ((Activity) context).findViewById(R.id.multiplayer_slideoutRelativeLayout);
        sliderOverallLayout = ((Activity) context).findViewById(R.id.multiplayer_slideoutOverallLayout);
        exitButton = ((Activity) context).findViewById(R.id.multiplayer_exit_button);
        editText = ((Activity) context).findViewById(R.id.game_editText);
        spell1_button = ((Activity) context).findViewById(R.id.spell1);
        spell2_button = ((Activity) context).findViewById(R.id.spell2);
        spell3_button = ((Activity) context).findViewById(R.id.spell3);
        spell4_button = ((Activity) context).findViewById(R.id.spell4);

        hintButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        dragOutButton.setOnClickListener(this);

        // set streak and score to zero, then update the screen
        streakCount = 0;
        score = 0;
        updateScreen();

        initializeGameParticipants();
        // let slider be off the screen first for very first time
        slideRelativeLayout.animate().translationXBy(1000f).setDuration(0);
        initializeSliderPlayerDetails();

        initializeKeyboard();

        initializeSpell();

        showGetReadyDialog();

        // if game has not started after 4 sec, maybe due to players not fully connected or what not,
        // we just start it anyway.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameStarted) {
                    Log.e("gggg", "forced start");
                    gameStarted = true;
                    closeGetReadyDialog();
                    timer = createCountDownTimer(Constants.GAME_TIME_MULTIPLAYER);
                    timer.start();
                }
            }
        }, 4000);
    }

    // after get ready dialog has appear, i am ready, hence broadcast to others. This is the start animation
    private void broadcast_tellOthersIAmReady() {
        Log.e("gggg", "broadcasting to others i am ready");

        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_READY_GETREADYDIALOGSHOWN);
        realTimeMultiplayerMessage.setData((byte) Constants.IS_READY);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                            synchronized (this) {
                                onBroadcastReceive_getReady();
                            }
                        }
                    });
        }
    }

    // this is after the get ready animation left to right and bottom to down, then tell ppl i am ready.
    public void onBroadcastReceive_getReady() {
        numberOfPlayersOnReady++;
        if (numberOfPlayersOnReady == handshakeConnection.mParticipants.size()) {
            Log.e("gggg", "all ready oh yeah");

            // start timer frst before closing dialog to prevent delay
            gameStarted = true;
            timer = createCountDownTimer(Constants.GAME_TIME_MULTIPLAYER);
            timer.start();

            YoYo.with(Techniques.SlideOutUp)
                    .duration(400)
                    .playOn(topText);

            YoYo.with(Techniques.SlideOutDown)
                    .duration(400).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    closeGetReadyDialog();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).playOn(bottomText);
        }
    }

    private void closeGetReadyDialog() {
        if (getReadyContainer != null) {
            ((ConstraintLayout) aContext.findViewById(R.id.multiplayer_actualGameOverallLayout)).removeView(getReadyContainer);
        }
    }

    private void showGetReadyDialog() {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.getready_layout, null);
        getReadyContainer = (LinearLayout) view;


        String levelDifficultyString = "";
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                levelDifficultyString = context.getString(R.string.easy);
                break;
            case Constants.MEDIUM_MODE:
                levelDifficultyString = context.getString(R.string.medium);
                break;
            case Constants.HARD_MODE:
                levelDifficultyString = context.getString(R.string.hard);
                break;
            case Constants.INSANE_MODE:
                levelDifficultyString = context.getString(R.string.insane);
                break;
        }

        ((ConstraintLayout) aContext.findViewById(R.id.multiplayer_actualGameOverallLayout)).addView(getReadyContainer,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        topText = view.findViewById(R.id.getready_topText);
        bottomText = view.findViewById(R.id.getready_bottomText);
        bottomText.setText(packName + ":  " + levelDifficultyString);

        multiPlayerGame.playMediaPlayer(0); // play dialog sound

        YoYo.with(Techniques.SlideInLeft)
                .duration(700)
                .playOn(topText);

        YoYo.with(Techniques.SlideInRight)
                .duration(700).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!gameStarted) {
                            broadcast_tellOthersIAmReady();
                        }
                        multiPlayerGame.releaseMediaPlayer();
                        multiPlayerGame.playMediaPlayer(1); // play background music
                    }
                }, 1400);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(bottomText);
    }


    private void initializeSpell() {
        setSpellImageResource(spell1_button, spellArray[0].getSpellID());
        setSpellImageResource(spell2_button, spellArray[1].getSpellID());
        setSpellImageResource(spell3_button, spellArray[2].getSpellID());
        setSpellImageResource(spell4_button, spellArray[3].getSpellID());

        // set clickable only if it is active spells
        if (spellArray[0].isActiveSpell()) {
            spell1_button.setOnClickListener(this);
        } else {
            spell1_button.setClickable(false);
        }

        if (spellArray[1].isActiveSpell()) {
            spell2_button.setOnClickListener(this);
        } else {
            spell2_button.setClickable(false);
        }

        if (spellArray[2].isActiveSpell()) {
            spell3_button.setOnClickListener(this);
        } else {
            spell3_button.setClickable(false);
        }

        if (spellArray[3].isActiveSpell()) {
            spell4_button.setOnClickListener(this);
        } else {
            spell4_button.setClickable(false);
        }
    }

    private void setSpellImageResource(ImageView spellImageView, int spellID) {

        switch (spellID) {
            case Constants.SPELL_BLOCK:
                spellImageView.setImageResource(R.drawable.spell_block_selector);
                break;
            case Constants.SPELL_HEX:
                spellImageView.setImageResource(R.drawable.spell_hex_selector);
                break;
            case Constants.SPELL_JUMBLE:
                spellImageView.setImageResource(R.drawable.spell_jumble_selector);
                break;
            case Constants.SPELL_THROW:
                spellImageView.setImageResource(R.drawable.spell_passiton_selector);
                break;
            case Constants.SPELL_REVERSE:
                spellImageView.setImageResource(R.drawable.spell_reverse_selector);
                break;
            case Constants.SPELL_SEEMORE:
                spellImageView.setImageResource(R.drawable.spell_seemore_selector);
                break;
            case Constants.SPELL_TILT:
                spellImageView.setImageResource(R.drawable.spell_tilt_selector);
                break;
            case Constants.SPELL_GUARDIANANGEL:
                spellImageView.setImageResource(R.drawable.spell_guardianangel_selector);
                break;
            case Constants.SPELL_TWOTIMESEXP:
                spellImageView.setImageResource(R.drawable.spell_twotimesscore_selector);
                break;
        }
    }

    private void initializeGameParticipants() {
        this.gameParticipantsPriorityQueue = new PriorityQueue<>();
        this.gameParticipantSparseArray = new SparseArray<>();

        for (int i = 0; i < handshakeConnection.mParticipants.size(); i++) {
            GameParticipant gameParticipant = new GameParticipant(handshakeConnection.playerNameMap.get(
                    handshakeConnection.mParticipants.get(i).getParticipantId()),
                    0, (byte) i, handshakeConnection.mParticipants.get(i).getParticipantId());
            gameParticipantSparseArray.put(i, gameParticipant);
            gameParticipantsPriorityQueue.add(gameParticipant);
            if (gameParticipant.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me
                myUniqueID = gameParticipant.getUniqueId();
            }
        }
    }


    // opponent casted a spell, kena hit
    void hitByOpponentSpell(int spellID, byte fromWhoUniqueID, byte[] forThrowUsed) {
        if (numberOfSpellSuffered >= 2 && multiPlayerGame.userPassiveSpell.contains(Constants.SPELL_GUARDIANANGEL)) {
            numberOfSpellSuffered = 0; // reset
            return; // end, dont get affected
        }

        switch (spellID) {
            case Constants.SPELL_BLOCK:
                spellManager.suffer_spellBlock();
                break;
            case Constants.SPELL_HEX:
                spellManager.suffer_spellHex(fromWhoUniqueID);
                break;
            case Constants.SPELL_JUMBLE:
                spellManager.suffer_spellJumble(fromWhoUniqueID);
                break;
            case Constants.SPELL_THROW:
                spellManager.suffer_spellThrow(fromWhoUniqueID, forThrowUsed);
                break;
            case Constants.SPELL_TILT:
                spellManager.suffer_spellTilt(fromWhoUniqueID);
                break;
        }
    }

    // id is the unique identifier to identify this game participant
    // this will result in all the participant scores to be looped through the for loop
    private void updateGameParticipantsScore(byte id) {
        // remove old participant
        GameParticipant oldGameParticipant = gameParticipantSparseArray.get(id);
        gameParticipantSparseArray.remove(id);
        gameParticipantsPriorityQueue.remove(oldGameParticipant);

        // add new participant back to update priority queue
        GameParticipant newGameParticipant = new GameParticipant(oldGameParticipant.getName(), oldGameParticipant.getScore(),
                oldGameParticipant.getUniqueId(), oldGameParticipant.getParticipantId());
        gameParticipantSparseArray.put(oldGameParticipant.getUniqueId(), newGameParticipant);
        gameParticipantsPriorityQueue.add(newGameParticipant);

        Queue<GameParticipant> tempQueue = new LinkedList<>();

        int size = gameParticipantsPriorityQueue.size();
        for (int i = 0; i < size; i++) {
            Log.e("gggg", "update game particiapnt score hererere " + i);
            GameParticipant old = gameParticipantsPriorityQueue.poll(); // push all priority queue out

            if (old.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is myself. set to yellow text
                rankingTextViewArray[i].setTextColor(ContextCompat.getColor(context, R.color.primary_yellow));
                rankingTextViewArray[i].setText((i + 1) + ".  " + context.getString(R.string.me) + "  (" + old.getScore() + ")");

                if (i == size - 1) {
                    // I am last
                    if (multiPlayerGame.userPassiveSpell.contains(Constants.SPELL_TWOTIMESEXP)) {
                        enjoy_spellTwoTimesExp();
                    }
                } else {
                    // i not last
                    if (underSpellInfluenced.contains(Constants.SPELL_TWOTIMESEXP)) {
                        remove_enjoy_spellTwoTimesExp();
                    }
                }

            } else {
                // other ppl set to white text
                rankingTextViewArray[i].setTextColor(ContextCompat.getColor(context, R.color.white));
                rankingTextViewArray[i].setText((i + 1) + ".  " + old.getName() + "  (" + old.getScore() + ")");
            }

            tempQueue.add(old);
        }

        // add back all priority queue
        int queueSize = tempQueue.size(); // impt to use var cos this is a changing var
        for (int i = 0; i < queueSize; i++) {
            gameParticipantsPriorityQueue.add(tempQueue.poll());
        }
    }

    private void initializeSliderPlayerDetails() {
        rankingTextViewArray = new TextView[handshakeConnection.mParticipants.size()];
        LinearLayout namesLinearLayout = ((Activity) context).findViewById(R.id.multiplayer_slider_nameslinearLayout);

        // initialize all participants and their score
        int size = handshakeConnection.mParticipants.size();
        for (int i = 0; i < size; i++) {
            CustomTextView textView = new CustomTextView(context);
            textView.setMaxLines(1);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen._16sdp));
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, context.getResources().getDimensionPixelSize(R.dimen._10sdp),
                    context.getResources().getDimensionPixelSize(R.dimen._16sdp),
                    TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM, TypedValue.COMPLEX_UNIT_PX);
            textView.setGravity(Gravity.CENTER);

            namesLinearLayout.addView(textView);

            rankingTextViewArray[i] = textView;
        }
        // randomly call a 0, becos it will update all scores anyway. impt is to call it once
        updateGameParticipantsScore((byte) 0);
    }

    // opponent score has increases, update his score in ranking
    void updateOpponentScore(byte id, byte scoreToAdd) {
        gameParticipantSparseArray.get(id).incrementScore(scoreToAdd);
        updateGameParticipantsScore(id);
    }

    private CountDownTimer createCountDownTimer(final long millisInFuture) {
        return new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // update progress
                progress = millisUntilFinished;

                // convert to mins and seconds
                timerTextView.setText(formatTime(millisUntilFinished));
                // change to red if < 10 secs
                if (millisUntilFinished < 10000) {
                    timerTextView.setTextColor(ContextCompat.getColor(context, R.color.primary_red));
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText(context.getString(R.string.timeup));
                gameEnd();
            }
        };
    }

    private String formatTime(long ms) {
        long s = ms / 1000; // convert from ms to s
        long mins = s / 60;
        long seconds = s % 60;

        if (seconds < 10) {
            return "" + mins + ":" + "0" + seconds;
        } else {
            return "" + mins + ":" + seconds;
        }
    }


    // prevent user from clicking hint
    private void setHintUnclickable() {
        hintClickable = false;
        hintButton.setAlpha(0.3f);
        hintButton.setClickable(false);
    }

    private void setHintClickable() {
        hintClickable = true;
        hintButton.setAlpha(1f);
        hintButton.setClickable(true);
    }


    private void updateScreen() {
        editTextString = new StringBuffer();

        if (questions.size() <= 1) {
            // create new words again if there are <= 1 word, 1 becos we using peek in on text change
            createWords();
        }

        currentQn = questions.poll();

        streakTextView.setText("" + streakCount); // update the streak count
        scoreTextView.setText("" + score); // update the score

        if (underSpellInfluenced.contains(Constants.SPELL_HEX)) {
            // currently hexed
            questionDescriptionTextView.setText(getHexedQn());
        } else if (underSpellInfluenced.contains(Constants.SPELL_JUMBLE)) {
            // currently jumbled
            questionDescriptionTextView.setText(getJumbledQn());
        } else if (underSpellInfluenced.contains(Constants.SPELL_THROW)) {
            // remove it first cos its 1 time use
            underSpellInfluenced.remove(Constants.SPELL_THROW);
            if (kenaThrownQuestion != null) {
                currentQn = kenaThrownQuestion;
            }
        } else {
            questionDescriptionTextView.setText(currentQn.getDescription());
        }

        editText.setHint("" + currentQn.getWord().charAt(0)); // set the hint
        editText.getText().clear(); // clear any previous text
        progressBar.setMax(currentQn.getWord().length());
        progressBar.setProgress(0);

        // allow user to press hint again
        setHintClickable();

        // user is under see more influence, therefore all new qn auto let user see more
        if (underSpellInfluenced.contains(Constants.SPELL_SEEMORE)) {
            activateSeeMore();
        }
    }

    // skip the current question
    private void skip() {
        requestedHint = false;
        streakCount = 0; // reset streak count
        answered.offer(currentQn);
        updateScreen();
        addToHistory();
    }

    private void skipWithoutPenalty() {
        requestedHint = false;
        answered.offer(currentQn);
        updateScreen();
        addToHistory();
    }

    private void answeredCorrectly() {
        hasNewQuestions = true;
        switch (multiPlayerGame.randomGenerator.nextInt(3)) {
            // play a random correct sound out of 3
            case 0:
                SoundPoolManager.getInstance().playSound(7);
                break;
            case 1:
                SoundPoolManager.getInstance().playSound(8);
                break;
            case 2:
                SoundPoolManager.getInstance().playSound(9);
                break;
            default:
                // should not be called, but just in case
                SoundPoolManager.getInstance().playSound(7);
                break;
        }

        // take into account streak to give points
        int streakBonus;

        if (streakCount == 0) {
            streakBonus = 0;
        } else if (streakCount < 5) {
            streakBonus = streakCount * Constants.GAME_STREAKRATE;
        } else { // more than 5
            streakBonus = Constants.GAME_FLATRATE;
        }

        int toAdd = Constants.GAME_FLATRATE + streakBonus;

        if (requestedHint) {
            toAdd = toAdd / 2; // score to be added is halved
        }

        // 2x score
        if (underSpellInfluenced.contains(Constants.SPELL_TWOTIMESEXP)) {
            toAdd = toAdd * 2;
        }

        score += toAdd;
        streakCount++;

        gameStatistics.checkAndSetHighestStreaks(streakCount);
        gameStatistics.incrementTotalNumberOfCorrectWords();

        // skip to next word
        // and reset progressBar
        currentQn.setAnsweredCorrectly();
        answered.offer(currentQn);

        YoYo.with(Techniques.Tada)
                .duration(600)
                .playOn(streakTextView);
        YoYo.with(Techniques.Landing)
                .duration(400)
                .playOn(scoreTextView);

        updateScreen();
        requestedHint = false;
        addToHistory();

        // update my own score
        gameParticipantSparseArray.get(myUniqueID).incrementScore((byte) toAdd);
        updateGameParticipantsScore(myUniqueID);
        // broadcast to everyone my new updated score
        incrementScore_broadcast((byte) toAdd);
    }

    private void createWords() {
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                switch (packName) {
                    case "All Categories":
                        questions = PackData.QN_All_Easy();
                        break;
                    case "Animals":
                        questions = PackData.QN_Animals_Easy();
                        break;
                    case "Companies":
                        questions = PackData.QN_Companies_Easy();
                        break;
                    case "Countries":
                        questions = PackData.QN_Countries_Easy();
                        break;
                    case "Dictionary Word":
                        questions = PackData.QN_Dictionary_Easy();
                        break;
                    case "Football Stars":
                        questions = PackData.QN_Football_Stars_Easy();
                        break;
                    case "Hollywood Artists":
                        questions = PackData.QN_Hollywood_Artists_Easy();
                        break;
                    case "Movies":
                        questions = PackData.QN_Movies_Easy();
                        break;
                    case "Superhero":
                        questions = PackData.QN_Superhero_Easy();
                        break;
                }
                break;
            case Constants.MEDIUM_MODE:
                switch (packName) {
                    case "All Categories":
                        questions = PackData.QN_All_Medium();
                        break;
                    case "Countries":
                        questions = PackData.QN_Countries_Medium();
                        break;
                    case "Dictionary Word":
                        questions = PackData.QN_Dictionary_Medium();
                        break;
                    case "Dog's breed":
                        questions = PackData.QN_Dog_Breed_Medium();
                        break;
                    case "Football Stars":
                        questions = PackData.QN_Football_Medium();
                        break;
                    case "Hollywood Artists":
                        questions = PackData.QN_Hollywood_Medium();
                        break;
                }
                break;
            case Constants.HARD_MODE:
                switch (packName) {
                    case "All Categories":
                        questions = PackData.QN_All_Hard();
                        break;
                    case "Countries":
                        questions = PackData.QN_Country_Hard();
                        break;
                    case "Billionaires Names":
                        questions = PackData.QN_Billionaires_Names_Hard();
                        break;
                    case "Dictionary Word":
                        questions = PackData.QN_Dictionary_Hard();
                        break;
                    case "Football Stars":
                        questions = PackData.QN_Football_Hard();
                        break;
                    case "Hollywood Artists":
                        questions = PackData.QN_Hollywood_Hard();
                        break;
                    case "Landmarks":
                        questions = PackData.QN_Landmarks_Hard();
                        break;
                    case "Programming Languages":
                        questions = PackData.QN_Programming_Languages_Hard();
                        break;
                }
                break;
            case Constants.INSANE_MODE:
                switch (packName) {
                    case "All Categories":
                        defaultDummyPacks();
                        break;
                    case "Dragon Ball":
                        defaultDummyPacks();
                        break;
                    case "Harry potter":
                        defaultDummyPacks();
                        break;
                    case "Pokemon":
                        defaultDummyPacks();
                        break;
                }
                break;
        }
        Collections.shuffle(questions); // shuffle
    }

    private void defaultDummyPacks() {
        questions.offer(new Question("icecream", "A dessert made from frozen sweetened cream or a similar substance, usually flavoured", sourceArray[0]));
        questions.offer(new Question("cheese", "A dairy product made from curdled or cultured milk", sourceArray[0]));
        questions.offer(new Question("cookie", "A sweet baked good (as in the previous sense) which (usually) has chocolate chips, fruit, nuts etc. baked into it", sourceArray[0]));
        questions.offer(new Question("pizza", " A baked Italian dish of a thinly rolled bread dough crust typically topped before baking with tomato sauce, cheese and other ingredients such as meat, vegetables or fruit", sourceArray[0]));
        questions.offer(new Question("butter", " A soft, fatty foodstuff made by churning the cream of milk (generally cow's milk)", sourceArray[0]));

        Collections.shuffle(questions); // shuffle
    }

    // Add the last question to the history list, to be displayed in the history panel
    private void addToHistory() {
        CustomTextView textView = new CustomTextView(context);
        Question question = answered.poll();
        textView.setText(question.getWord());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen._14sdp));
        if (question.getAnsweredCorrectly()) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.primary_green));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(context.getResources().getDimensionPixelSize(R.dimen._10sdp), 0, 0, 0);

        historyLayout.addView(textView, layoutParams);

        // always scroll to the end
        historyHorizontalScrollView.post(new Runnable() {
            public void run() {
                historyHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
            }
        });
    }


    // the method to compare current text with actual answer
    // return 0 if its wrong, else the number of words it is correct so far
    private int compareString(String actualString, String currentString) {
        int counter = 0;
        actualString = actualString.toLowerCase();
        currentString = currentString.toLowerCase();

        for (int i = 0; i < currentString.length(); i++) {
            if (currentString.charAt(i) == actualString.charAt(i)) {
                counter += 1;
            } else {
                return 0;
            }
        }
        return counter;
    }

    private void initializeKeyboard() {
        // prevent soft keyboard from poping up
        if (Build.VERSION.SDK_INT >= 21) {
            editText.setShowSoftInputOnFocus(false);
        } else {
            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int numberOfWordsCorrect = compareString(currentQn.getWord(), s.toString());
                if (numberOfWordsCorrect > 0) {
                    // correct ans so far
                    progressBar.setProgress(compareString(currentQn.getWord(), s.toString()));

                    // set progress to green
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                                R.color.primary_green)));
                    } else {
                        // this is the uglier color where the entire progress bar turns to green hue
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context,
                                R.color.primary_green), PorterDuff.Mode.MULTIPLY);
                    }

                    if (currentQn.getWord().length() == s.toString().length()) {
                        answeredCorrectly();
                    } else if (requestedHint) {
                        setHintUnclickable();
                    } else {
                        // if hint cannot click, make it clickable
                        if (!hintClickable) {
                            setHintClickable();
                        }

                        // if at last word make hint unclickabkle
                        if (numberOfWordsCorrect == currentQn.getWord().length() - 1) {
                            setHintUnclickable();
                        }
                    }

                } else {
                    if (s.length() == 0) {
                        // no word
                        progressBar.setProgress(0);
                    } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                                R.color.primary_red)));
                    } else {
                        // this is the uglier color where the entire progress bar turns to green hue
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context,
                                R.color.primary_red), PorterDuff.Mode.MULTIPLY);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // pass the InputConnection from the EditText to the keyboard
        inputConnection = editText.onCreateInputConnection(new EditorInfo());

        ((Activity) context).findViewById(R.id.button_q).setOnClickListener(this);
        keyValues.put(R.id.button_q, "q");
        ((Activity) context).findViewById(R.id.button_w).setOnClickListener(this);
        keyValues.put(R.id.button_w, "w");
        ((Activity) context).findViewById(R.id.button_e).setOnClickListener(this);
        keyValues.put(R.id.button_e, "e");
        ((Activity) context).findViewById(R.id.button_r).setOnClickListener(this);
        keyValues.put(R.id.button_r, "r");
        ((Activity) context).findViewById(R.id.button_t).setOnClickListener(this);
        keyValues.put(R.id.button_t, "t");
        ((Activity) context).findViewById(R.id.button_y).setOnClickListener(this);
        keyValues.put(R.id.button_y, "y");
        ((Activity) context).findViewById(R.id.button_u).setOnClickListener(this);
        keyValues.put(R.id.button_u, "u");
        ((Activity) context).findViewById(R.id.button_i).setOnClickListener(this);
        keyValues.put(R.id.button_i, "i");
        ((Activity) context).findViewById(R.id.button_o).setOnClickListener(this);
        keyValues.put(R.id.button_o, "o");
        ((Activity) context).findViewById(R.id.button_p).setOnClickListener(this);
        keyValues.put(R.id.button_p, "p");
        ((Activity) context).findViewById(R.id.button_a).setOnClickListener(this);
        keyValues.put(R.id.button_a, "a");
        ((Activity) context).findViewById(R.id.button_s).setOnClickListener(this);
        keyValues.put(R.id.button_s, "s");
        ((Activity) context).findViewById(R.id.button_d).setOnClickListener(this);
        keyValues.put(R.id.button_d, "d");
        ((Activity) context).findViewById(R.id.button_f).setOnClickListener(this);
        keyValues.put(R.id.button_f, "f");
        ((Activity) context).findViewById(R.id.button_g).setOnClickListener(this);
        keyValues.put(R.id.button_g, "g");
        ((Activity) context).findViewById(R.id.button_h).setOnClickListener(this);
        keyValues.put(R.id.button_h, "h");
        ((Activity) context).findViewById(R.id.button_j).setOnClickListener(this);
        keyValues.put(R.id.button_j, "j");
        ((Activity) context).findViewById(R.id.button_k).setOnClickListener(this);
        keyValues.put(R.id.button_k, "k");
        ((Activity) context).findViewById(R.id.button_l).setOnClickListener(this);
        keyValues.put(R.id.button_l, "l");
        ((Activity) context).findViewById(R.id.button_z).setOnClickListener(this);
        keyValues.put(R.id.button_z, "z");
        ((Activity) context).findViewById(R.id.button_x).setOnClickListener(this);
        keyValues.put(R.id.button_x, "x");
        ((Activity) context).findViewById(R.id.button_c).setOnClickListener(this);
        keyValues.put(R.id.button_c, "c");
        ((Activity) context).findViewById(R.id.button_v).setOnClickListener(this);
        keyValues.put(R.id.button_v, "v");
        ((Activity) context).findViewById(R.id.button_b).setOnClickListener(this);
        keyValues.put(R.id.button_b, "b");
        ((Activity) context).findViewById(R.id.button_n).setOnClickListener(this);
        keyValues.put(R.id.button_n, "n");
        ((Activity) context).findViewById(R.id.button_m).setOnClickListener(this);
        keyValues.put(R.id.button_m, "m");
        ((Activity) context).findViewById(R.id.button_backspace).setOnClickListener(this);
        ((Activity) context).findViewById(R.id.button_spacebar).setOnClickListener(this);
        keyValues.put(R.id.button_spacebar, " ");
    }

    private void giveHint() {
        // calling this method reveals hint, depending on the progress so far.

        String actualWord = currentQn.getWord();
        String current = editText.getText().toString();

        if (current.length() == 0) {
            if (actualWord.length() > 3) {
                editTextString.append(actualWord.substring(0, 3));
                editText.setText(editTextString);
                editText.setSelection(editTextString.length());
                return;
            } else if (actualWord.length() == 3) {
                // 3 word, give 2 hint
                editTextString.append(actualWord.substring(0, 2));
                editText.setText(editTextString);
                editText.setSelection(editTextString.length());
                return;
            } else if (actualWord.length() == 2) {
                // 2 word, give 1 hint
                editTextString.append(actualWord.substring(0, 1));
                editText.setText(editTextString);
                editText.setSelection(editTextString.length());
                return;
            } else {
                // 1 word, dont give hint
                return;
            }
        }

        if (compareString(actualWord, current) > 0 && current.length() + 2 < actualWord.length()) {
            // correct so far and is 2 away from the hint.
            editTextString = new StringBuffer();
            editTextString.append(actualWord.substring(0, current.length() + 2));
            editText.setText(editTextString);
            editText.setSelection(editTextString.length());
        } else {
            editTextString = new StringBuffer();
            editTextString.append(current.substring(0, current.length() - 1));
            editText.setText(editTextString);
            editText.setSelection(editTextString.length());
            giveHint();
        }
    }

    private void activateSeeMore() {
        // calling this method reveals hint, depending on the progress so far.

        String actualWord = currentQn.getWord();
        String current = editText.getText().toString();

        if (current.length() == 0) {
            editTextString.append(actualWord.substring(0, 3));
            editText.setText(editTextString);
            editText.setSelection(editTextString.length());
            return;
        }

        if (compareString(actualWord, current) > 0 && current.length() + 2 < actualWord.length()) {
            // correct so far and is 2 away from the hint.
            editTextString = new StringBuffer();
            editTextString.append(actualWord.substring(0, current.length() + 2));
            editText.setText(editTextString);
            editText.setSelection(editTextString.length());
        } else {
            editTextString = new StringBuffer();
            editTextString.append(current.substring(0, current.length() - 1));
            editText.setText(editTextString);
            editText.setSelection(editTextString.length());
            activateSeeMore();
        }
    }

    void onBackPressed() {
        if (!gameHasEnded) {
            if (!gamePaused) {
                SoundPoolManager.getInstance().playSound(1);
                pausedClicked();
            }
        }
    }

    // called when pause button is pressed
    private void pausedClicked() {
        gamePaused = true;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        final View view = inflater.inflate(R.layout.game_pausemenu_multiplayer, null);
        builder.setView(view);

        final android.support.v7.app.AlertDialog dialogPause = builder.create();
        Window dialogWindow = dialogPause.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogPause.setCancelable(true);
        dialogPause.show();

        view.post(new Runnable() {
            @Override
            public void run() {
                if (Utils.getIfPurchasedNoAds(context)) {
                    // buy, therefore hide
                    hidePauseAd(view);
                } else {
                    // did not buy, therefore show
                    setUpPauseDialogNativeAds(view);
                }
            }
        });

        view.findViewById(R.id.pause_resumeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // resume button clicked
                dialogPause.dismiss();
            }
        });


        view.findViewById(R.id.pause_exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingToExit = true;
                multiPlayerGame.exitFromGameToMainMenu = true;
                dialogPause.dismiss();
                ((Activity) context).finish();
            }
        });

        dialogPause.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!goingToExit) {
                    gamePaused = false;
                }
                SoundPoolManager.getInstance().playSound(2);
            }
        });
    }

    private void setUpPauseDialogNativeAds(final View parent) {
        if (nativeBannerAd != null && nativeBannerAd.isAdLoaded()) {
            // Native ad is loaded and ready to be displayed
            Log.d(TAG, "Native ad is loaded and ready to be displayed!");

            // Inflate Native Banner Ad into Container
            inflatePauseAd(nativeBannerAd, parent);
        } else {
            hidePauseAd(parent);
        }
    }

    private void inflatePauseAd(NativeBannerAd nativeBannerAd, View parent) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = parent.findViewById(R.id.pause_adsTitle);
        AdIconView nativeAdIconView = parent.findViewById(R.id.imageView);
        TextView nativeAdCallToAction = parent.findViewById(R.id.pause_adsCTAButton);
        RelativeLayout adChoicesContainer = parent.findViewById(R.id.pause_adChoice);

        parent.findViewById(R.id.pause_adLabel).setVisibility(View.VISIBLE);
        nativeAdTitle.setVisibility(View.VISIBLE);
        nativeAdIconView.setVisibility(View.VISIBLE);
        nativeAdCallToAction.setVisibility(View.VISIBLE);
        adChoicesContainer.setVisibility(View.VISIBLE);

        // Add the AdChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(context, nativeBannerAd, true);
        adChoicesContainer.addView(adChoicesView, 0);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(parent, nativeAdIconView, clickableViews);
    }

    private void hidePauseAd(View parent) {
        parent.findViewById(R.id.pause_adLabel).setVisibility(View.GONE);
        parent.findViewById(R.id.pause_adsTitle).setVisibility(View.GONE);
        parent.findViewById(R.id.imageView).setVisibility(View.GONE);
        parent.findViewById(R.id.pause_adsCTAButton).setVisibility(View.GONE);
        parent.findViewById(R.id.pause_adChoice).setVisibility(View.GONE);
    }

    // my score is incremented. Broadcast to everyone
    private void incrementScore_broadcast(byte scoreToIncrement) {
        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_UPDATESCORE);
        realTimeMultiplayerMessage.setData(myUniqueID, scoreToIncrement);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {

                        }
                    });
        }
    }

    // i used an active spell on others, broadcast to the rest who are higher position than me ONLY
    private void activeSpellUsed_broadcast(byte spellIDused) {
        if (spellIDused == Constants.SPELL_REVERSE) {
            SpellInPlay spellToReverse = experiencingSpell.getASpellToReverse();

            if (spellToReverse != null) {
                // there is smth to reverse
                RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_SPELLUSED);
                realTimeMultiplayerMessage.setData((byte) spellToReverse.getSpellID(), myUniqueID);

                // send spell back to who casted at me
                handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(realTimeMultiplayerMessage.getMessage(),
                        handshakeConnection.mRoomId, gameParticipantSparseArray.get((int) spellToReverse.getFromWhoUniqueID()).getParticipantId(),
                        new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                            }
                        });
            }
        } else if (spellIDused == Constants.SPELL_THROW) {
            RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_SPELLUSED);
            realTimeMultiplayerMessage.setData(spellIDused, myUniqueID);

            try {
                byte[] qnByteArr = BytesUtil.toByteArray(currentQn);

                byte[] finalByteArr = new byte[qnByteArr.length + 3];
                finalByteArr[0] = realTimeMultiplayerMessage.getMessage()[0];
                finalByteArr[1] = realTimeMultiplayerMessage.getMessage()[1];
                finalByteArr[2] = realTimeMultiplayerMessage.getMessage()[2];

                int counter = 0;
                for (int i = 3; i < finalByteArr.length && counter < qnByteArr.length; i++) {
                    finalByteArr[i] = qnByteArr[0];
                    counter++;
                }


                // all the players to be affected by this spell is stored here
                HashSet<String> allHigherRankingParticipantsID = new HashSet<>();
                Queue<GameParticipant> tempQueue = new LinkedList<>(); // temp queue to later restore priority queue

                int numberOfPlayers = gameParticipantsPriorityQueue.size();

                for (int i = 0; i < numberOfPlayers; i++) {
                    GameParticipant player = gameParticipantsPriorityQueue.poll();
                    tempQueue.add(player); // add the player to temp queue to restore back to priority queue later on

                    if (player.getParticipantId().equals(handshakeConnection.mMyId)) {
                        // this is me, hence break this loop
                        break;
                    } else {
                        // not me yet, meaning someone is higher ranking than me. Add him to the hashset
                        allHigherRankingParticipantsID.add(player.getParticipantId());
                    }
                }

                // add back all removed players
                for (int j = 0; j < tempQueue.size(); j++) {
                    gameParticipantsPriorityQueue.add(tempQueue.poll());
                }


                // Send to every other participant.
                for (Participant p : handshakeConnection.mParticipants) {
                    if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                        // this is me, hence dont send
                        continue;
                    }

                    if (!allHigherRankingParticipantsID.contains(p.getParticipantId())) {
                        // this guy is of lower ranking, hence dont affect him with the spell
                        continue;
                    }

                    if (p.getStatus() != Participant.STATUS_JOINED) {
                        continue;
                    }

                    // send spell used to all higher ranking players
                    handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(finalByteArr,
                            handshakeConnection.mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                                @Override
                                public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {

                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                @Override
                                public void onSuccess(Integer tokenId) {
                                }
                            });
                }

            } catch (Exception e) {
                Log.e("gggg", "exception in throw " + e.getMessage());
            }
            // get new question
            skipWithoutPenalty();
        } else {
            // used other active spells
            RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_SPELLUSED);
            realTimeMultiplayerMessage.setData(spellIDused, myUniqueID);

            // all the players to be affected by this spell is stored here
            HashSet<String> allHigherRankingParticipantsID = new HashSet<>();
            Queue<GameParticipant> tempQueue = new LinkedList<>(); // temp queue to later restore priority queue

            int numberOfPlayers = gameParticipantsPriorityQueue.size();

            for (int i = 0; i < numberOfPlayers; i++) {
                GameParticipant player = gameParticipantsPriorityQueue.poll();
                tempQueue.add(player); // add the player to temp queue to restore back to priority queue later on

                if (player.getParticipantId().equals(handshakeConnection.mMyId)) {
                    // this is me, hence break this loop
                    break;
                } else {
                    // not me yet, meaning someone is higher ranking than me. Add him to the hashset
                    allHigherRankingParticipantsID.add(player.getParticipantId());
                }
            }

            // add back all removed players
            for (int j = 0; j < tempQueue.size(); j++) {
                gameParticipantsPriorityQueue.add(tempQueue.poll());
            }

            // Send to every other participant.
            for (Participant p : handshakeConnection.mParticipants) {
                if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                    // this is me, hence dont send
                    continue;
                }

                if (!allHigherRankingParticipantsID.contains(p.getParticipantId())) {
                    // this guy is of lower ranking, hence dont affect him with the spell
                    continue;
                }

                if (p.getStatus() != Participant.STATUS_JOINED) {
                    continue;
                }

                // send spell used to all higher ranking players
                handshakeConnection.mRealTimeMultiplayerClient.sendReliableMessage(realTimeMultiplayerMessage.getMessage(),
                        handshakeConnection.mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                            }
                        });
            }
        }
    }

    private void broadcast_sendRestartRequestBroadcast(final int restartValue) {
        RealTimeMultiplayerMessage realTimeMultiplayerMessage = new RealTimeMultiplayerMessage(Constants.MESSAGE_RESTARTGAME);
        realTimeMultiplayerMessage.setData((byte) restartValue);

        // Send to every other participant.
        for (Participant p : handshakeConnection.mParticipants) {
            if (p.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me, hence dont send
                continue;
            }

            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }

            handshakeConnection.mRealTimeMultiplayerClient.sendUnreliableMessage(realTimeMultiplayerMessage.getMessage(),
                    handshakeConnection.mRoomId, p.getParticipantId());
        }
    }

    // the current game has ended
    private void gameEnd() {
        gameHasEnded = true;
        timer.cancel();
        gameStatistics.setFinalScore(score);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        final View view = inflater.inflate(R.layout.game_endmenu_multiplayer, null);
        builder.setView(view);

        dialogEnd = builder.create();
        Window dialogWindow = dialogEnd.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogEnd.setCancelable(false);
        dialogEnd.show();

        restartButton = view.findViewById(R.id.game_end_restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (multiPlayerGame.playerIconImageViewMap.get(handshakeConnection.mMyId).isReady()) {
                    // now i unready from ready
                    broadcast_sendRestartRequestBroadcast(Constants.RESTART_NO);
                    multiPlayerGame.restartYesOrNo(handshakeConnection.mMyId, false);
                    restartButton.setTextColor(ContextCompat.getColor(context, R.color.endgame_textcolor));
                    restartButton.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    // now i ready from unready
                    broadcast_sendRestartRequestBroadcast(Constants.RESTART_YES);
                    multiPlayerGame.restartYesOrNo(handshakeConnection.mMyId, true);
                    restartButton.setTextColor(Color.WHITE);
                    restartButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_green));
                }
            }
        });


        view.findViewById(R.id.game_end_exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // exit means cannot restart
                broadcast_sendRestartRequestBroadcast(Constants.RESTART_NO);
                handshakeConnection.leaveRoom();
                goingToExit = true;
                multiPlayerGame.exitFromGameToMainMenu = true;
                dismissDialogEnd();
                ((Activity) context).finish();
            }
        });

        ((TextView) view.findViewById(R.id.game_end_score)).setText(context.getResources().getString(R.string.yourscore)
                + "   " + gameStatistics.getFinalScore());

        ((TextView) view.findViewById(R.id.game_end_higheststreak)).setText(context.getResources().getString(R.string.higheststreak)
                + "   " + gameStatistics.getHighestStreaks());

        ((TextView) view.findViewById(R.id.game_end_hintsused)).setText(context.getResources().getString(R.string.hintsused)
                + "   " + gameStatistics.getNumberOfHintsUsed());

        ((TextView) view.findViewById(R.id.game_end_skips)).setText(context.getResources().getString(R.string.skips)
                + "   " + gameStatistics.getNumberOfSkipsUsed());

        ((TextView) view.findViewById(R.id.game_end_correctwords)).setText(context.getResources().getString(R.string.correctwords)
                + "   " + gameStatistics.getTotalNumberOfCorrectWords());

        LinearLayout rankingLinearLayout = view.findViewById(R.id.game_end_rankingLinearLayout);
        // create opponent ranking
        Queue<GameParticipant> tempQueue = new LinkedList<>(); // to keep pq same for restart
        for (int i = 0; i < handshakeConnection.mParticipants.size(); i++) {
            CustomTextView textView = new CustomTextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen._16sdp));
            GameParticipant currentPlayer = gameParticipantsPriorityQueue.poll();
            tempQueue.add(currentPlayer);
            Log.e("gggg", "hererr   " + i + " " + currentPlayer + "queuesize: " + gameParticipantsPriorityQueue.size());
            if (currentPlayer != null && currentPlayer.getParticipantId().equals(handshakeConnection.mMyId)) {
                // this is me
                textView.setTextColor(ContextCompat.getColor(context, R.color.primary_yellow));
                textView.setText("" + (i + 1) + ". " + context.getString(R.string.me) +
                        "  (" + currentPlayer.getScore() + ")");
                // set my final ranking
                switch (i) {
                    case 0:
                        ((TextView) view.findViewById(R.id.game_end_myranking)).setText("1st");
                        break;
                    case 1:
                        ((TextView) view.findViewById(R.id.game_end_myranking)).setText("2nd");
                        break;
                    case 2:
                        ((TextView) view.findViewById(R.id.game_end_myranking)).setText("3rd");
                        break;
                    default:
                        ((TextView) view.findViewById(R.id.game_end_myranking)).setText("" + (i + 1) + "th");
                        break;
                }
            } else if (currentPlayer != null) {
                // other player
                textView.setTextColor(Color.WHITE);
                textView.setText("" + (i + 1) + ". " + currentPlayer.getName() +
                        "  (" + currentPlayer.getScore() + ")");
            }

            rankingLinearLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        for (int i = 0; i < tempQueue.size(); i++) {
            gameParticipantsPriorityQueue.add(tempQueue.poll());
        }

        // set up restart image
        int amountPlayers = handshakeConnection.mParticipants.size();
        int imageSize = context.getResources().getDimensionPixelSize(R.dimen._30sdp);

        // the reason we have 3 is so that we have 3 rows to show the images
        LinearLayout restartImageLinearLayout = view.findViewById(R.id.game_end_restartImageLinearLayout);
        LinearLayout restartImageLinearLayout2 = view.findViewById(R.id.game_end_restartImageLinearLayout2);
        LinearLayout restartImageLinearLayout3 = view.findViewById(R.id.game_end_restartImageLinearLayout3);

        restartImageLinearLayoutArray = new LinearLayout[3];
        restartImageLinearLayoutArray[0] = restartImageLinearLayout;
        restartImageLinearLayoutArray[1] = restartImageLinearLayout2;
        restartImageLinearLayoutArray[2] = restartImageLinearLayout3;
        // add up to 3 to first linear layout
        for (int i = 0; i < amountPlayers && i < 3; i++) {
            restartImageLinearLayout.addView(multiPlayerGame.playerIconImageViewMap.
                            get(handshakeConnection.mParticipants.get(i).getParticipantId()).getPlayerIcon(),
                    new LinearLayout.LayoutParams(imageSize, imageSize));
        }
        // add up to 3 to next linear layout
        for (int i = 3; i < amountPlayers && i < 6; i++) {
            restartImageLinearLayout2.addView(multiPlayerGame.playerIconImageViewMap.
                            get(handshakeConnection.mParticipants.get(i).getParticipantId()).getPlayerIcon(),
                    new LinearLayout.LayoutParams(imageSize, imageSize));
        }

        // add up to 3 to next linear layout
        for (int i = 6; i < amountPlayers && i < 9; i++) {
            restartImageLinearLayout3.addView(multiPlayerGame.playerIconImageViewMap.
                            get(handshakeConnection.mParticipants.get(i).getParticipantId()).getPlayerIcon(),
                    new LinearLayout.LayoutParams(imageSize, imageSize));
        }

        checkAchievements();
    }

    private void checkAchievements() {
        Accomplishments accomplishments = new Accomplishments(context, Constants.SINGLE_PLAYER, score, streakCount,
                gameStatistics.getNumberOfSkipsUsed() == 0);
        ArrayList<AccomplishmentsName> accomplishmentsNameArrayList = accomplishments.getAchievementsList();

        if (accomplishmentsNameArrayList.size() > 0) {
            // there is some achievement, hence create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = aContext.getLayoutInflater();
            View view = inflater.inflate(R.layout.endgame_jewelearned_dialog, null);
            builder.setView(view);

            final AlertDialog dialogJewelEarned = builder.create();
            Window dialogWindow = dialogJewelEarned.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
            }

            dialogJewelEarned.setCancelable(true);
            dialogJewelEarned.show();

            // play short victory sound then long background sound
            multiPlayerGame.releaseMediaPlayer();
            multiPlayerGame.playMediaPlayer(4);

            TextView title = view.findViewById(R.id.jewelsdialog_title);
            LinearLayout linearLayoutContainer = view.findViewById(R.id.jewelsdialog_linearLayoutContainer);

            int totalAmountOfJewelsEarned = 0;
            for (AccomplishmentsName accomplishmentsName : accomplishmentsNameArrayList) {
                CustomTextView textView = new CustomTextView(context);
                int currentJewelGained = accomplishmentsName.getJewelGained();
                totalAmountOfJewelsEarned += currentJewelGained;
                textView.setText(accomplishmentsName.getDesc() + ": " + currentJewelGained + " Jewels");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen._16sdp));
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                linearLayoutContainer.addView(textView);
            }
            title.setText(totalAmountOfJewelsEarned + " " + context.getString(R.string.jewelsearned));
        } else {
            // no achievement, dont play the short sound (4)
            multiPlayerGame.releaseMediaPlayer();
            multiPlayerGame.playMediaPlayer(5);
        }
    }

    void dismissDialogEnd() {
        if (dialogEnd != null) {
            dialogEnd.dismiss();
        }
    }

    void onRestartChanges() {
        // here we using back same instance, becos no point using new instance as the view reference
        // are all still the same since we using same layout

        // this is impt as this child view will be attach to category selection again, therefore
        // need to remove first
        clearRestartImageView();

        dismissDialogEnd();
        setHintClickable();

        gameHasEnded = false;
        requestedHint = false;
        gameStarted = false;
        score = 0;
        streakCount = 0;
        numberOfPlayersOnReady = 0;

        gameStatistics.resetStatistics();
        historyLayout.removeAllViews();

        if (isSliderOpen) {
            slideIn_close();
        }

        // reset the words again
        questions.clear();
        createWords(); // add words into our list

        for (int i = 0; i < gameParticipantSparseArray.size(); i++) {
            gameParticipantSparseArray.get(i).clearScore();
        }
        // call one time to update ranking display
        updateGameParticipantsScore((byte) 0);
    }

    void onRestartStartingGame(Spell[] spellArray) {
        Log.e("gggg", "onrestartstartgame");

        this.levelDifficulty = Utils.getDifficultyPreference(context);
        this.packName = Utils.getCategoryPack(context); // TODO point to correct packdata
        this.spellArray = spellArray;
        spellManager = new SpellManager();

        showGetReadyDialog();

        initializeGameParticipants();
        updateScreen();

        initializeSpell();


        // if game has not started after 4 sec, maybe due to players not fully connected or what not,
        // we just start it anyway.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameStarted) {
                    Log.e("gggg", "forced start");
                    gameStarted = true;
                    closeGetReadyDialog();
                    timer = createCountDownTimer(Constants.GAME_TIME_MULTIPLAYER);
                    timer.start();
                }
            }
        }, 4000);
    }

    void clearRestartImageView() {
        for (int i = 0; i < restartImageLinearLayoutArray.length; i++) {
            if (restartImageLinearLayoutArray[i] != null) {
                restartImageLinearLayoutArray[i].removeAllViews();
            }
        }
    }

    // slide out the current ranking
    private void sliderClicked() {
        if (isSliderOpen) {
            slideIn_close();
        } else {
            slideOut_open();
        }
    }

    private void slideOut_open() {
        isSliderOpen = !isSliderOpen;
        sliderOverallLayout.setVisibility(View.VISIBLE);
        slideRelativeLayout.animate().translationXBy(-1000f).setDuration(200);
        sliderOverallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideIn_close(); // close the slider on clicking anywhere
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.exittitle))
                        .setMessage(context.getString(R.string.exitdialog))
                        .setPositiveButton(context.getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // yes, therefore exit
                                        handshakeConnection.leaveRoom();
                                        goingToExit = true;
                                        multiPlayerGame.exitFromGameToMainMenu = true;
                                        ((Activity) context).finish();
                                    }
                                })
                        .setNegativeButton(context.getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                        dialog.dismiss();
                                    }
                                });
                builder.create().show();
            }
        });


    }

    private void slideIn_close() {
        isSliderOpen = !isSliderOpen;
        slideRelativeLayout.animate().translationXBy(1000).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                slideRelativeLayout.clearAnimation();
                sliderOverallLayout.setClickable(false);
                sliderOverallLayout.setVisibility(View.GONE);
            }
        });

    }

    // use for spells that dont affect others, therefore no need broadcast, but ownself
    // consume the event
    private void spellConsumeSelfEvent(int spellID) {
        switch (spellID) {
            // currently only have spell see more that is a self event
            case Constants.SPELL_SEEMORE:
                activateSeeMore(); // change the current word immediately to give 3 more letters

                // add to underSpellInfluence to take into account for next subsequent words
                underSpellInfluenced.add(Constants.SPELL_SEEMORE);

                new CountDownTimer(Constants.DURATION_SPELL_SEEMORE, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        underSpellInfluenced.remove(Constants.SPELL_SEEMORE);
                    }
                }.start();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_hintButton:
                // score given if correct should be HALVED
                // show next 2 letters,
                // if 2 letters away from completion, show 1 letter

                if (requestedHint) {
                    // requested hint once for this word, don't do anything
                } else {
                    SoundPoolManager.getInstance().playSound(2);
                    requestedHint = true;
                    giveHint();
                }
                // stop user from clicking hint again
                setHintUnclickable();
                gameStatistics.incrementNumberOfHintsUsed();
                break;

            case R.id.game_skipButton:
                SoundPoolManager.getInstance().playSound(6);
                skip();
                gameStatistics.incrementNumberOfSkipsUsed();
                break;

            case R.id.game_dragoutButton:
                SoundPoolManager.getInstance().playSound(3);
                sliderClicked();
                break;

            case R.id.button_backspace:
                // when backspace button is pressed
//                CharSequence selectedText = inputConnection.getSelectedText(0);
//                if (TextUtils.isEmpty(selectedText)) {
//                    // no selection, so delete previous character
//                    inputConnection.deleteSurroundingText(1, 0);
//                } else {
//                    // delete the selection
//                    inputConnection.commitText("", 1);
//                }
                int len = editTextString.length();

                if (len > 0) {
                    int cursorStart = editText.getSelectionStart();
                    int cursorEnd = editText.getSelectionEnd();

                    if (cursorStart != cursorEnd) {
                        // user has selected a chunk of text
                        for (int i = cursorStart; i < cursorEnd; i++) {
                            editTextString.deleteCharAt(cursorStart);
                        }
                        editText.setText(editTextString);
                        editText.setSelection(cursorStart);
                    } else {
                        // both curson end and start same, so use either
                        if (cursorEnd > 0) {
                            editTextString.deleteCharAt(cursorEnd - 1);
                            editText.setText(editTextString);
                            editText.setSelection(cursorEnd - 1);
                        }
                    }
                }
                SoundPoolManager.getInstance().playSound(10); // keyboard sound

                break;

            case R.id.spell1:
                // consume spell
                spell1_button.setClickable(false);
                spell1_button.setAlpha(0.3f);
                if (spellArray[0].getWillAffectOthers()) {
                    activeSpellUsed_broadcast((byte) spellArray[0].getSpellID());
                } else {
                    spellConsumeSelfEvent(spellArray[0].getSpellID());
                }
                break;

            case R.id.spell2:
                // consume spell
                spell2_button.setClickable(false);
                spell2_button.setAlpha(0.3f);
                if (spellArray[1].getWillAffectOthers()) {
                    activeSpellUsed_broadcast((byte) spellArray[1].getSpellID());
                } else {
                    spellConsumeSelfEvent(spellArray[1].getSpellID());
                }
                break;

            case R.id.spell3:
                // consume spell
                spell3_button.setClickable(false);
                spell3_button.setAlpha(0.3f);
                if (spellArray[2].getWillAffectOthers()) {
                    activeSpellUsed_broadcast((byte) spellArray[2].getSpellID());
                } else {
                    spellConsumeSelfEvent(spellArray[2].getSpellID());
                }
                break;

            case R.id.spell4:
                // consume spell
                spell4_button.setClickable(false);
                spell4_button.setAlpha(0.3f);
                if (spellArray[3].getWillAffectOthers()) {
                    activeSpellUsed_broadcast((byte) spellArray[3].getSpellID());
                } else {
                    spellConsumeSelfEvent(spellArray[3].getSpellID());
                }
                break;

            default:
                // default here assumes any of the keyboard key are pressed instead
//                String value = keyValues.get(v.getId());
//                inputConnection.commitText(value, 1);
                // default here assumes any of the keyboard key are pressed instead

                String value = keyValues.get(v.getId());

                int cursorPos = editText.getSelectionEnd();

                editTextString.insert(cursorPos, value);
                editText.setText(editTextString);
                if (editTextString.length() > 0 && hasNewQuestions) {
                    editText.setSelection(3);
                } else if (editTextString.length() > 0 && !hasNewQuestions) {
                    editText.setSelection(cursorPos + 1);
                } else {
                    editText.setSelection(0);
                }

                hasNewQuestions = false;
                SoundPoolManager.getInstance().playSound(10); // keyboard sound
                break;
        }
    }

    private void enjoy_spellTwoTimesExp() {
        underSpellInfluenced.add(Constants.SPELL_TWOTIMESEXP);
    }

    private void remove_enjoy_spellTwoTimesExp() {
        underSpellInfluenced.remove(Constants.SPELL_TWOTIMESEXP);
    }

    // Get the question text in hexed format
    private String getHexedQn() {
        int start = 0;
        int end = 0;

        char[] qnCharArray = currentQn.getDescription().toCharArray();

        for (int i = 0; i < qnCharArray.length; i++) {
            if (qnCharArray[i] == ' ') {
                // 50% chance activated
                if (start != end && multiPlayerGame.randomGenerator.nextBoolean()) {
                    int rand = randInt(start, end);
                    qnCharArray[rand] = '#';
                }
                start = i + 1;
                end = i + 1;
            } else {
                end++;
            }
        }

        return String.valueOf(qnCharArray);
    }

    private String getJumbledQn() {
        int start = 0;
        int end = 0;

        char[] qnCharArray = currentQn.getDescription().toCharArray();

        for (int i = 0; i < qnCharArray.length; i++) {
            if (qnCharArray[i] == ' ' || i == qnCharArray.length - 1 /* last word */) {
                if (end - start > 2) {
                    // not 2 letter word, so proceed to jumble

                    // Scramble the letters using the standard Fisher-Yates shuffle,
                    // alr took into account last word end with fullstop, it will not shuffle the fullstop and the letter infront of fullstop.
                    for (int j = start + 1; j < end - 1; j++) {
                        int ran = randInt(start + 1, end - 1);
                        // Swap letters
                        char temp = qnCharArray[j];
                        qnCharArray[j] = qnCharArray[ran];
                        qnCharArray[ran] = temp;
                    }
                }
                start = i + 1;
                end = i + 1;
            } else {
                end++;
            }
        }

        return String.valueOf(qnCharArray);
    }

    // inclusive min, exclusive max
    private int randInt(int min, int max) {
        return multiPlayerGame.randomGenerator.nextInt((max - min)) + min;
    }

    private void playSpellAnimation(String text) {
        final TextView textView = aContext.findViewById(R.id.multiplayer_hexedword);
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);

        YoYo.with(Techniques.ZoomIn)
                .duration(800).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(textView);
    }

    /*
        Spell class
     */
    class SpellManager {

        public SpellManager() {
        }

        public void suffer_spellBlock() {
            numberOfSpellSuffered++;

            final boolean originalSpell1Clickable, originalSpell2Clickable, originalSpell3Clickable, originalSpell4Clickable;
            originalSpell1Clickable = spell1_button.isClickable();
            originalSpell2Clickable = spell2_button.isClickable();
            originalSpell3Clickable = spell3_button.isClickable();
            originalSpell4Clickable = spell4_button.isClickable();

            if (originalSpell1Clickable) {
                spell1_button.setClickable(false);
                spell1_button.setAlpha(0.3f);
            }

            if (originalSpell2Clickable) {
                spell2_button.setClickable(false);
                spell2_button.setAlpha(0.3f);
            }

            if (originalSpell3Clickable) {
                spell3_button.setClickable(false);
                spell3_button.setAlpha(0.3f);
            }

            if (originalSpell4Clickable) {
                spell4_button.setClickable(false);
                spell4_button.setAlpha(0.3f);
            }

            playSpellAnimation("Blocked!");

            new CountDownTimer(Constants.DURATION_SPELL_BLOCK, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    if (originalSpell1Clickable) {
                        spell1_button.setClickable(true);
                        spell1_button.setAlpha(1f);
                    }

                    if (originalSpell2Clickable) {
                        spell2_button.setClickable(true);
                        spell2_button.setAlpha(1f);
                    }

                    if (originalSpell3Clickable) {
                        spell3_button.setClickable(true);
                        spell3_button.setAlpha(1f);
                    }

                    if (originalSpell4Clickable) {
                        spell4_button.setClickable(true);
                        spell4_button.setAlpha(1f);
                    }
                }
            }.start();
        }

        public void suffer_spellHex(byte fromWhoUniqueId) {
            numberOfSpellSuffered++;

            final SpellInPlay spellInPlay = new SpellInPlay(Constants.SPELL_HEX, fromWhoUniqueId);
            experiencingSpell.influencedBySpell(spellInPlay);
            underSpellInfluenced.add(Constants.SPELL_HEX);

            playSpellAnimation("Hexed!");

            questionDescriptionTextView.setText(getHexedQn());


            new CountDownTimer(Constants.DURATION_SPELL_HEX, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    experiencingSpell.removeInfluencedBySpell(spellInPlay);
                    underSpellInfluenced.remove(Constants.SPELL_HEX);
                    questionDescriptionTextView.setText(currentQn.getDescription());
                }
            }.start();
        }


        public void suffer_spellJumble(byte fromWhoUniqueId) {
            numberOfSpellSuffered++;

            final SpellInPlay spellInPlay = new SpellInPlay(Constants.SPELL_JUMBLE, fromWhoUniqueId);
            experiencingSpell.influencedBySpell(spellInPlay);
            underSpellInfluenced.add(Constants.SPELL_JUMBLE);
            questionDescriptionTextView.setText(getJumbledQn());

            playSpellAnimation("Jumbled!");

            new CountDownTimer(Constants.DURATION_SPELL_JUMBLE, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    experiencingSpell.removeInfluencedBySpell(spellInPlay);
                    underSpellInfluenced.remove(Constants.SPELL_JUMBLE);
                    questionDescriptionTextView.setText(currentQn.getDescription());
                }
            }.start();
        }

        public void suffer_spellThrow(byte fromWhoUniqueId, byte[] forThrowUsed) {
            numberOfSpellSuffered++;

            final SpellInPlay spellInPlay = new SpellInPlay(Constants.SPELL_THROW, fromWhoUniqueId);
            experiencingSpell.influencedBySpell(spellInPlay);
            underSpellInfluenced.add(Constants.SPELL_THROW);


            if (forThrowUsed.length > 3) {
                byte[] temp = new byte[forThrowUsed.length - 3];
                int counter = 3;
                for (int i = 0; i < temp.length && counter < forThrowUsed.length; i++) {
                    temp[i] = forThrowUsed[counter];
                    counter++;
                }

                try {
                    kenaThrownQuestion = (Question) BytesUtil.toObject(temp);
                } catch (Exception e) {
                    Log.e("gggg", "exception at converting byte to object " + e.getMessage());
                }
            }
        }


        public void suffer_spellTilt(byte fromWhoUniqueId) {
            numberOfSpellSuffered++;

            final SpellInPlay spellInPlay = new SpellInPlay(Constants.SPELL_TILT, fromWhoUniqueId);
            experiencingSpell.influencedBySpell(spellInPlay);

            rotationDegree += 90;
            aContext.findViewById(R.id.questionRelativeLayout).setRotation(rotationDegree);

            playSpellAnimation("Tilted!");

            new CountDownTimer(Constants.DURATION_SPELL_TILT, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    experiencingSpell.removeInfluencedBySpell(spellInPlay);
                    rotationDegree = 0;
                    aContext.findViewById(R.id.questionRelativeLayout).setRotation(0);
                }
            }.start();
        }
    }
}

class GameParticipant implements Comparable<GameParticipant> {
    private String name;
    private int score;
    private byte uniqueId; // id is unique identifier to identify the participant based on send message byte array, a number of 0 - 7
    // participant id defers from id. Participant id is the id returns by google play service Player.getParticipantId()
    private String participantId;

    public GameParticipant(String name, int score, byte uniqueId, String participantId) {
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
        this.score = score;
        this.uniqueId = uniqueId;
        this.participantId = participantId;
    }

    public String getParticipantId() {
        return this.participantId;
    }

    public byte getUniqueId() {
        return this.uniqueId;
    }

    public void incrementScore(byte scoreToAdd) {
        this.score += scoreToAdd;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    // for used in restart
    public void clearScore() {
        this.score = 0;
    }

    @Override
    public String toString() {
        return name + "  (" + score + ")";
    }

    // compare base on whose score is higher
    @Override
    public int compareTo(@NonNull GameParticipant o) {
        return o.score - this.score;
    }
}


/*
    send message
    how message works is that it uses byte array
    index 0 represents the following:
        'N': sending player name. In this case, the byte array length is not fix. it depends on the length of the name.
        'O': restart request.
        'R': ready at actual game after getReady dialog shows finish
        'C': category selection update
        'S': spell. A spell is used.
        'U': update score. A player score has change, hence should update score
        refer to Constants.MESSAGE_xxx for these char
    index 1 represents the following:
        For 'O': 0 represents i want restart, 1 represents i dont want to restart
        For 'R': 0 represents received, you are ready
        For 'C': 0 represents level difficulty change, 1 represents pack change (Use Constants.BROADCAST_LEVELDIFFICULTYCHANGE/PACKSCHANGED),
                 2 represents Ready
        For 'S': refer to Constants.SPELL_xxx for the letter
        For 'U': GameParticipant unique ID
    index 2 represents the following:
        For 'O': nth
        For 'R': nth
        For 'C': if level difficulty change (index 1 is 0), then this is Constants.EASY/MEDIUM/...
                 if packs change (index 1 is 1), then this represents the pack position,
                 if ready (index 1 is 2), then 0 represents not ready, 1 represents ready (Constants.IS_READY)
        For 'S': casted by who, i.e. the unique ID of the caster
        For 'U': the int score to increment by, for the GameParticipant unique ID

    Note: since we always use reliable message to send update score, when game has finished
    the opponent scores are always accurate.
 */
class RealTimeMultiplayerMessage {
    byte[] msgByte;

    public RealTimeMultiplayerMessage(char index0) {
        msgByte = new byte[7];
        msgByte[0] = (byte) index0;
    }

    public void setData(byte index1) {
        msgByte[1] = index1;
    }

    public void setData(byte index1, byte index2) {
        msgByte[1] = index1;
        msgByte[2] = index2;
    }

    public byte[] getMessage() {
        return msgByte;
    }
}

class Spell {
    private int spellID; // based on Constants.SPELL_xxx;
    private boolean isActiveSpell; // true if it is an active, false otherwise
    private boolean willAffectOthers; // true if this spell will target others instead of benefiting ownself.
    // isActiveSpell is used to see if the spell is clickable, willAffectOthers is used to see if
    // we should broadcast message or ownself consume the event

    public Spell(int spellID, boolean isActiveSpell, boolean willAffectOthers) {
        this.spellID = spellID;
        this.isActiveSpell = isActiveSpell;
        this.willAffectOthers = willAffectOthers;
    }

    public int getSpellID() {
        return spellID;
    }

    public boolean isActiveSpell() {
        return isActiveSpell;
    }

    public boolean getWillAffectOthers() {
        return this.willAffectOthers;
    }
}

// whether ornot i am under influence by any spells now casted by opponent.
// used to keep track of who casted what spell at me, so that i can reverse back
class ExperiencingSpell {
    private ArrayList<SpellInPlay> currentlyInfluencedBySpellsArrayList;

    public ExperiencingSpell() {
        currentlyInfluencedBySpellsArrayList = new ArrayList<>();
    }

    public void influencedBySpell(SpellInPlay spellInPlay) {
        currentlyInfluencedBySpellsArrayList.add(spellInPlay);
    }

    public void removeInfluencedBySpell(SpellInPlay spellInPlay) {
        currentlyInfluencedBySpellsArrayList.remove(spellInPlay);
    }

    public SpellInPlay getASpellToReverse() {
        if (currentlyInfluencedBySpellsArrayList.size() > 0) {
            // return last element
            return currentlyInfluencedBySpellsArrayList.get(currentlyInfluencedBySpellsArrayList.size() - 1);
        } else {
            // nothing to reverse, therefore return null
            return null;
        }
    }
}

class SpellInPlay {
    private int spellID;
    private byte fromWhoUniqueID;

    public SpellInPlay(int spellID, byte fromWhoUniqueID) {
        this.spellID = spellID;
        this.fromWhoUniqueID = fromWhoUniqueID;
    }

    public int getSpellID() {
        return spellID;
    }

    public byte getFromWhoUniqueID() {
        return fromWhoUniqueID;
    }
}