package com.ganwl.multiplayerwordgame;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.CountDownTimer;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.CustomTextView;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;
import com.ganwl.multiplayerwordgame.packs.PackData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class OnePlayerGame extends AppCompatActivity implements View.OnClickListener {
    // Our communication link to the EditText
    private InputConnection inputConnection;
    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    private SparseArray<String> keyValues = new SparseArray<>();
    private TextView streakTextView, timerTextView, scoreTextView, questionDescriptionTextView;
    private ImageView hintButton, skipButton, pauseButton;
    private EditText editText;
    private String sourceArray[] = {"From wiki", "from wordnet"};
    private LinkedList<Question> questions = new LinkedList<>();
    private int streakCount, score;
    private Queue<Question> answered = new LinkedList<>();
    private CountDownTimer timer;
    private long progress;
    private boolean gamePaused = false;
    private boolean requestedHint = false;
    private LinearLayout historyLayout;
    private ProgressBar progressBar;
    private HorizontalScrollView historyHorizontalScrollView;
    private boolean hintClickable = true; // true if hint button can be pressed
    private boolean goingToExit = false; // change to true if user clicked exit button in pause menu
    private boolean gameHasEnded = false; // set to true when game ends
    private GameStatistics gameStatistics; // keep track of user statistics
    private TextView topText, bottomText; // for get ready dialog
    private int levelDifficulty;
    private String packName;
    private StringBuffer editTextString = new StringBuffer();
    private LinearLayout getReadyContainer;
    private NativeBannerAd nativeBannerAd;
    private static final String TAG = "gggg";
    private MediaPlayer mediaPlayer;
    private boolean isAppRunning = false;
    private boolean onResumeCalledBefore = false;
    private final Random randomGenerator = new Random();

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
                    timerTextView.setTextColor(ContextCompat.getColor(OnePlayerGame.this, R.color.primary_red));
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText(getString(R.string.timeup));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oneplayer_game);
        isAppRunning = true;

        nativeBannerAd = new NativeBannerAd(this, Utils.getPauseNativeAdId());
        nativeBannerAd.loadAd();

        this.levelDifficulty = Utils.getDifficultyPreference(OnePlayerGame.this);
        this.packName = Utils.getCategoryPack(OnePlayerGame.this);
        showGetReadyDialog();
        initializeKeyboard();

        createWords(); // add words into our list

        gameStatistics = new GameStatistics();


        streakTextView = findViewById(R.id.game_streakTextView);
        timerTextView = findViewById(R.id.game_timerTextView);
        scoreTextView = findViewById(R.id.game_scoreTextView);
        questionDescriptionTextView = findViewById(R.id.game_descriptionTextView);

        hintButton = findViewById(R.id.game_hintButton);
        skipButton = findViewById(R.id.game_skipButton);
        pauseButton = findViewById(R.id.game_pauseButton);
        progressBar = findViewById(R.id.game_progressbar);
        historyLayout = findViewById(R.id.game_historyLinearLayout);
        historyHorizontalScrollView = findViewById(R.id.game_historyHorizontalScrollView);

        editText = findViewById(R.id.game_editText);
        // prevent soft keyboard from poping up
        if (Build.VERSION.SDK_INT >= 21) {
            editText.setShowSoftInputOnFocus(false);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int numberOfWordsCorrect = compareString(questions.peek().getWord(), s.toString());
                if (numberOfWordsCorrect > 0) {
                    // correct ans so far
                    progressBar.setProgress(compareString(questions.peek().getWord(), s.toString()));

                    // set progress to green
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(OnePlayerGame.this,
                                R.color.primary_green)));
                    } else {
                        // this is the uglier color where the entire progress bar turns to green hue
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(OnePlayerGame.this,
                                R.color.primary_green), PorterDuff.Mode.MULTIPLY);
                    }

                    if (questions.peek().getWord().length() == s.toString().length()) {
                        answeredCorrectly();
                    } else if (requestedHint) {
                        setHintUnclickable();
                    } else {
                        // if hint cannot click, make it clickable
                        if (!hintClickable) {
                            setHintClickable();
                        }

                        // if at last word make hint unclickabkle
                        if (numberOfWordsCorrect == questions.peek().getWord().length() - 1) {
                            setHintUnclickable();

                        }
                    }

                } else {
                    if (s.length() == 0) {
                        // no word
                        progressBar.setProgress(0);
                    } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(OnePlayerGame.this,
                                R.color.primary_red)));
                    } else {
                        // this is the uglier color where the entire progress bar turns to green hue
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(OnePlayerGame.this,
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

        hintButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);

        // set streak and score to zero, then update the screen
        streakCount = 0;
        score = 0;
        updateScreen();
    }

    private void showGetReadyDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.getready_layout, null);
        getReadyContainer = (LinearLayout) view;

        topText = view.findViewById(R.id.getready_topText);
        bottomText = view.findViewById(R.id.getready_bottomText);
        String levelDifficultyString = "";
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                levelDifficultyString = getString(R.string.easy);
                break;
            case Constants.MEDIUM_MODE:
                levelDifficultyString = getString(R.string.medium);
                break;
            case Constants.HARD_MODE:
                levelDifficultyString = getString(R.string.hard);
                break;
            case Constants.INSANE_MODE:
                levelDifficultyString = getString(R.string.insane);
                break;
        }
        bottomText.setText(packName + ":  " + levelDifficultyString);

        ((ConstraintLayout) findViewById(R.id.oneplayer_root)).addView(getReadyContainer,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        playMediaPlayer(0); // play dialog sound

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
                        releaseMediaPlayer();
                        playMediaPlayer(1); // play background music
                        // wait 1 sec before starting so its not too rush
                        startTopToBottomAnimation();
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
        AdChoicesView adChoicesView = new AdChoicesView(OnePlayerGame.this, nativeBannerAd, true);
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

    private void setUpEndGameNativeAds(final View parent) {
        nativeBannerAd = new NativeBannerAd(this, Utils.getEndGameNativeAdId_SinglePlayer());
        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
                hideEndGameAd(parent);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateEndGameAd(nativeBannerAd, parent);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });
        nativeBannerAd.loadAd();
    }

    private void inflateEndGameAd(NativeBannerAd nativeBannerAd, View parent) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = parent.findViewById(R.id.game_end_adsTitle);
        AdIconView nativeAdIconView = parent.findViewById(R.id.game_end_adsImageView);
        TextView nativeAdCallToAction = parent.findViewById(R.id.game_end_adsCTAButton);
        RelativeLayout adChoicesContainer = parent.findViewById(R.id.game_end_adsChoice);

        parent.findViewById(R.id.game_end_adLabel).setVisibility(View.VISIBLE);
        nativeAdTitle.setVisibility(View.VISIBLE);
        nativeAdIconView.setVisibility(View.VISIBLE);
        nativeAdCallToAction.setVisibility(View.VISIBLE);
        adChoicesContainer.setVisibility(View.VISIBLE);

        // Add the AdChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(OnePlayerGame.this, nativeBannerAd, true);
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

    private void hideEndGameAd(View parent) {
        parent.findViewById(R.id.game_end_adLabel).setVisibility(View.GONE);
        parent.findViewById(R.id.game_end_adsTitle).setVisibility(View.GONE);
        parent.findViewById(R.id.game_end_adsImageView).setVisibility(View.GONE);
        parent.findViewById(R.id.game_end_adsCTAButton).setVisibility(View.GONE);
        parent.findViewById(R.id.game_end_adsChoice).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        super.onDestroy();
    }

    private void closeGetReadyDialog() {
        if (getReadyContainer != null) {
            ((ConstraintLayout) findViewById(R.id.oneplayer_root)).removeView(getReadyContainer);
        }
    }

    private void startTopToBottomAnimation() {
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
                timer = createCountDownTimer(Constants.GAME_TIME);
                timer.start();
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

    // prevent user from clicking hint
    private void setHintUnclickable() {
        Log.e("gggg", "hint unclick");
        hintClickable = false;
        hintButton.setAlpha(0.3f);
        hintButton.setClickable(false);
    }

    private void setHintClickable() {
        Log.e("gggg", "hint clicable");

        hintClickable = true;
        hintButton.setAlpha(1f);
        hintButton.setClickable(true);
    }


    private void updateScreen() {
        editTextString = new StringBuffer();
        editText.getText().clear(); // clear any previous text


        if (questions.size() <= 1) {
            // create new words again if there are <= 1 word, 1 becos we using peek in on text change
            createWords();
        }

        Question ques = questions.peek();

        streakTextView.setText("" + streakCount); // update the streak count
        scoreTextView.setText("" + score); // update the score
        questionDescriptionTextView.setText(
                ques.getDescription()
        ); // update description for new word
        editText.setHint("" + ques.getWord().charAt(0)); // set the hint
        progressBar.setMax(questions.peek().getWord().length());
        progressBar.setProgress(0);

        // allow user to press hint again
        setHintClickable();
    }

    // skip the current question
    private void skip() {
        requestedHint = false;
        streakCount = 0; // reset streak count
        answered.offer(questions.poll());
        updateScreen();
        addToHistory();
    }

    private void answeredCorrectly() {
        switch (randomGenerator.nextInt(3)) {
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
            toAdd = toAdd / 2;
            score += toAdd; // score to be added is halved
        } else {
            score += toAdd;
        }
        streakCount++;

        gameStatistics.checkAndSetHighestStreaks(streakCount);
        gameStatistics.incrementTotalNumberOfCorrectWords();

        // skip to next word
        // and reset progressBar
        questions.peek().setAnsweredCorrectly();
        answered.offer(questions.poll());
        updateScreen();
        YoYo.with(Techniques.Tada)
                .duration(600)
                .playOn(streakTextView);
        YoYo.with(Techniques.Landing)
                .duration(400)
                .playOn(scoreTextView);
        requestedHint = false;
        addToHistory();
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
        CustomTextView textView = new CustomTextView(OnePlayerGame.this);
        Question question = answered.poll();
        textView.setText(question.getWord());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen._14sdp));
        if (question.getAnsweredCorrectly()) {
            textView.setTextColor(ContextCompat.getColor(OnePlayerGame.this, R.color.primary_green));
        } else {
            textView.setTextColor(ContextCompat.getColor(OnePlayerGame.this, R.color.light_gray));
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen._10sdp), 0, 0, 0);

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
        findViewById(R.id.button_q).setOnClickListener(this);
        keyValues.put(R.id.button_q, "q");
        findViewById(R.id.button_w).setOnClickListener(this);
        keyValues.put(R.id.button_w, "w");
        findViewById(R.id.button_e).setOnClickListener(this);
        keyValues.put(R.id.button_e, "e");
        findViewById(R.id.button_r).setOnClickListener(this);
        keyValues.put(R.id.button_r, "r");
        findViewById(R.id.button_t).setOnClickListener(this);
        keyValues.put(R.id.button_t, "t");
        findViewById(R.id.button_y).setOnClickListener(this);
        keyValues.put(R.id.button_y, "y");
        findViewById(R.id.button_u).setOnClickListener(this);
        keyValues.put(R.id.button_u, "u");
        findViewById(R.id.button_i).setOnClickListener(this);
        keyValues.put(R.id.button_i, "i");
        findViewById(R.id.button_o).setOnClickListener(this);
        keyValues.put(R.id.button_o, "o");
        findViewById(R.id.button_p).setOnClickListener(this);
        keyValues.put(R.id.button_p, "p");
        findViewById(R.id.button_a).setOnClickListener(this);
        keyValues.put(R.id.button_a, "a");
        findViewById(R.id.button_s).setOnClickListener(this);
        keyValues.put(R.id.button_s, "s");
        findViewById(R.id.button_d).setOnClickListener(this);
        keyValues.put(R.id.button_d, "d");
        findViewById(R.id.button_f).setOnClickListener(this);
        keyValues.put(R.id.button_f, "f");
        findViewById(R.id.button_g).setOnClickListener(this);
        keyValues.put(R.id.button_g, "g");
        findViewById(R.id.button_h).setOnClickListener(this);
        keyValues.put(R.id.button_h, "h");
        findViewById(R.id.button_j).setOnClickListener(this);
        keyValues.put(R.id.button_j, "j");
        findViewById(R.id.button_k).setOnClickListener(this);
        keyValues.put(R.id.button_k, "k");
        findViewById(R.id.button_l).setOnClickListener(this);
        keyValues.put(R.id.button_l, "l");
        findViewById(R.id.button_z).setOnClickListener(this);
        keyValues.put(R.id.button_z, "z");
        findViewById(R.id.button_x).setOnClickListener(this);
        keyValues.put(R.id.button_x, "x");
        findViewById(R.id.button_c).setOnClickListener(this);
        keyValues.put(R.id.button_c, "c");
        findViewById(R.id.button_v).setOnClickListener(this);
        keyValues.put(R.id.button_v, "v");
        findViewById(R.id.button_b).setOnClickListener(this);
        keyValues.put(R.id.button_b, "b");
        findViewById(R.id.button_n).setOnClickListener(this);
        keyValues.put(R.id.button_n, "n");
        findViewById(R.id.button_m).setOnClickListener(this);
        keyValues.put(R.id.button_m, "m");
        findViewById(R.id.button_backspace).setOnClickListener(this);
        findViewById(R.id.button_spacebar).setOnClickListener(this);
        keyValues.put(R.id.button_spacebar, " ");

    }

    private void giveHint() {
        // calling this method reveals hint, depending on the progress so far.

        String actualWord = questions.peek().getWord();
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

    // called when pause button is pressed
    private void pausedClicked() {
        gamePaused = true;
        if (timer != null)
            timer.cancel();

        AlertDialog.Builder builder = new AlertDialog.Builder(OnePlayerGame.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.game_pausemenu, null);
        builder.setView(view);

        final AlertDialog dialogPause = builder.create();
        Window dialogWindow = dialogPause.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogPause.setCancelable(true);
        dialogPause.show();

        view.post(new Runnable() {
            @Override
            public void run() {
                if (Utils.getIfPurchasedNoAds(OnePlayerGame.this)) {
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
                dialogPause.dismiss();
                finish();
            }
        });

        dialogPause.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!goingToExit) {
                    // start timer again if user is not exiting the game
                    timer = createCountDownTimer(progress - 1000);
                    timer.start();
                    gamePaused = false;
                }
                SoundPoolManager.getInstance().playSound(2);
            }
        });
    }

    // restart this round, to be trigger by the Restart button when game end
    private void restartGame() {
        releaseMediaPlayer();
        playMediaPlayer(1); // play background music

        gameHasEnded = false;
        requestedHint = false;

        score = 0;
        streakCount = 0;

        showGetReadyDialog();

        gameStatistics.resetStatistics();
        historyLayout.removeAllViews();

        // reset the words again
        questions.clear();
        createWords(); // add words into our list

        updateScreen();
    }

    // the current game has ended
    private void gameEnd() {
        gameHasEnded = true;
        timer.cancel();
        gameStatistics.setFinalScore(score);

        AlertDialog.Builder builder = new AlertDialog.Builder(OnePlayerGame.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.game_endmenu, null);
        builder.setView(view);

        final AlertDialog dialogPause = builder.create();
        Window dialogWindow = dialogPause.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogPause.setCancelable(false);
        dialogPause.show();

        view.post(new Runnable() {
            @Override
            public void run() {
                if (Utils.getIfPurchasedNoAds(OnePlayerGame.this)) {
                    // buy, therefore hide
                    hideEndGameAd(view);
                } else {
                    // did not buy, therefore show
                    setUpEndGameNativeAds(view);
                }
            }
        });

        view.findViewById(R.id.game_end_restartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPause.dismiss();
                restartGame();
            }
        });


        view.findViewById(R.id.game_end_exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingToExit = true;
                dialogPause.dismiss();
                finish();
            }
        });

        ((TextView) view.findViewById(R.id.game_end_score)).setText(getResources().getString(R.string.yourscore)
                + "   " + gameStatistics.getFinalScore());

        ((TextView) view.findViewById(R.id.game_end_higheststreak)).setText(getResources().getString(R.string.higheststreak)
                + "   " + gameStatistics.getHighestStreaks());

        ((TextView) view.findViewById(R.id.game_end_hintsused)).setText(getResources().getString(R.string.hintsused)
                + "   " + gameStatistics.getNumberOfHintsUsed());

        ((TextView) view.findViewById(R.id.game_end_skips)).setText(getResources().getString(R.string.skips)
                + "   " + gameStatistics.getNumberOfSkipsUsed());

        ((TextView) view.findViewById(R.id.game_end_correctwords)).setText(getResources().getString(R.string.correctwords)
                + "   " + gameStatistics.getTotalNumberOfCorrectWords());

        checkAchievements();

    }

    private void checkAchievements() {
        Accomplishments accomplishments = new Accomplishments(OnePlayerGame.this, Constants.SINGLE_PLAYER, score, streakCount,
                gameStatistics.getNumberOfSkipsUsed() == 0);
        ArrayList<AccomplishmentsName> accomplishmentsNameArrayList = accomplishments.getAchievementsList();

        if (accomplishmentsNameArrayList.size() > 0) {
            // there is some achievement, hence create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(OnePlayerGame.this);
            LayoutInflater inflater = this.getLayoutInflater();
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
            releaseMediaPlayer();
            playMediaPlayer(4);

            TextView title = view.findViewById(R.id.jewelsdialog_title);
            LinearLayout linearLayoutContainer = view.findViewById(R.id.jewelsdialog_linearLayoutContainer);

            int totalAmountOfJewelsEarned = 0;
            for (AccomplishmentsName accomplishmentsName : accomplishmentsNameArrayList) {
                CustomTextView textView = new CustomTextView(OnePlayerGame.this);
                int currentJewelGained = accomplishmentsName.getJewelGained();
                totalAmountOfJewelsEarned += currentJewelGained;
                textView.setText(accomplishmentsName.getDesc() + ": " + currentJewelGained + " Jewels");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen._16sdp));
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                linearLayoutContainer.addView(textView);
            }
            title.setText(totalAmountOfJewelsEarned + " " + getString(R.string.jewelsearned));
        } else {
            // no achievement, dont play the short sound (4)
            releaseMediaPlayer();
            playMediaPlayer(5);
        }
    }

    @Override
    protected void onPause() {
        // when we go to the game mode activity, current activity will be onPause. So we release soundpool resources since now we are at game mode activity
        // this also takes into account user press home button or take phone call e.g, transitting away from the app,
        // hence we stop all sound including background sound
        super.onPause();
        // set isAppRunning to false, so no subsequent mediaplayer sound will be called from playMediaPlayer(x)
        // this is impt, becos in the event user pause screen when dialog is calling, subsequent background music will still be called since releaseMediaPlayer is only releasing the dialog sound
        // hence we use this flag to prevent background music from even starting
        isAppRunning = false;
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        // when we press back from game mode, this activity will onResume from onPause, and we need to reinstate all soundPool resources again since we released onPause
        super.onResume();
        isAppRunning = true; // set back to true to allow playMediaPlayer to play sound

        if (!isSoundMuted()) {
            // unlike MainActivity, we can check if !isSoundMuted and do not instantiate if sound is muted.
            // But in MainActivity we just create anw even if sound is muted, becos there is the chance of user pressing the mute button again to toggle to sound unmute
            // However in QuickPlay, there is no chance for user to press the unmute sound, so its ok to check if !isSoundMuted()
        }

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
    public void onBackPressed() {
        if (!gameHasEnded) {
            if (!gamePaused) {
                SoundPoolManager.getInstance().playSound(1);
                pausedClicked();
            }
        }

    }

    private boolean isSoundMuted() {
        return Utils.getIfSoundIsMuted(OnePlayerGame.this);
    }

    // ONE and ONLY place to play sound. Play the sound based on the param passed. Add new case here to play new sound
    private void playMediaPlayer(int soundToPlay) {
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
//                            mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.answer_correct2);
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
//                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.answer_wrong);
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

    private void releaseMediaPlayer() {
        if (mediaPlayer != null && !isSoundMuted()) {
            mediaPlayer.release();
            mediaPlayer = null;
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

            case R.id.game_pauseButton:
                SoundPoolManager.getInstance().playSound(1);
                pausedClicked();
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
            default:
                // default here assumes any of the keyboard key are pressed instead
                String value = keyValues.get(v.getId());

                int cursorPos = editText.getSelectionEnd();
                editTextString.insert(cursorPos, value);
                editText.setText(editTextString);
                editText.setSelection(editTextString.length() > 0 ? cursorPos + 1 : 0);
                SoundPoolManager.getInstance().playSound(10); // keyboard sound
                break;
        }
    }
}
