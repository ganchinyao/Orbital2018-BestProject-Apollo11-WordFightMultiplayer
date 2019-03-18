package com.ganwl.multiplayerwordgame;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganwl.multiplayerwordgame.fragments.AllCategory;
import com.ganwl.multiplayerwordgame.fragments.Favorite;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.MusicManager;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;

public class OnePlayerPopup extends AppCompatActivity implements View.OnClickListener{

    private View easyButton, mediumButton, hardButton, insaneButton;
    private TextView easyTextView, mediumTextView, hardTextView, insaneTextView;
    private ImageView easyImageView, mediumImageView, hardImageView, insaneImageView, popupStartButton;
    private int selectedLevelDifficulty;
    private PageAdapter pageAdapter;
    // used to determine if mediaplayer should continue playing or stop
    private boolean onResumeCalledBefore = false;
    private boolean goingToPreviousActivity = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_player_popup);

        initializeVariables();
        setUpViewPager();

        initializeOnclickEvents();

        initializeGraphics();
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
        easyButton.setOnClickListener(this);
        mediumButton.setOnClickListener(this);
        hardButton.setOnClickListener(this);
        insaneButton.setOnClickListener(this);
        popupStartButton.setOnClickListener(this);

    }

    private void initializeGraphics() {
        highlightDifficultySelection(Utils.getDifficultyPreference(OnePlayerPopup.this), true);
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
        //TODO make pager adapter better remove favorite
        pageAdapter = new PageAdapter(getSupportFragmentManager(), 1, this);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);

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
                if(!firstTime) {
                    ((AllCategory) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.EASY_MODE);
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
                if(!firstTime) {
                    ((AllCategory) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.MEDIUM_MODE);
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
                if(!firstTime) {
                    ((AllCategory) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.HARD_MODE);
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
                if(!firstTime) {
                    ((AllCategory) pageAdapter.getAllCategoryFragment()).swapAdapter(Constants.INSANE_MODE);
                }
                popupStartButton.setAlpha(0.3f);
                popupStartButton.setClickable(false); // now no insane yet so make it unclickable
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popup_easyButton:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                highlightDifficultySelection(Constants.EASY_MODE, false);
                break;
            case R.id.popup_mediumButton:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                highlightDifficultySelection(Constants.MEDIUM_MODE, false);
                break;
            case R.id.popup_hardButton:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                highlightDifficultySelection(Constants.HARD_MODE, false);
                break;
            case R.id.popup_insaneButton:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                highlightDifficultySelection(Constants.INSANE_MODE, false);
                break;
            case R.id.popup_startButton:
                SoundPoolManager.getInstance().playSound(6); // play start button sound
                startGame();
        }
    }

    // invokes when the play button is clicked
    private void startGame() {
        // stop previous sound
        if(!MusicManager.isGoingNextActivity) {
            // it is not activated by going next activity, i.e. this call here is activated by using pressing home or similar
            // hence we stop media player
            MusicManager.stopMediaPlayer();
        }

        Utils.setDifficultyPreference(OnePlayerPopup.this, selectedLevelDifficulty);
        Utils.setCategoryPack(OnePlayerPopup.this, Constants.currentPackSelection);
        Intent intent = new Intent(OnePlayerPopup.this, OnePlayerGame.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!goingToPreviousActivity) {
            // we only want to stop mediaplayer if it app is going background, and NOT when app is onbackpress to go back to previous activity
            // this is achieve by setting the flag goingToPreviousActivity to be true when we onBackPressed.
            // but if user click home button to go home page, goingToPreviousActivity will be false, and stopmediaplayer will be triggered
            MusicManager.stopMediaPlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(onResumeCalledBefore) {
            // will not be called the first time this activity is instantiated, since the music is alr playing
            // will be called in subseqent called to onResume, which will occur when app go to background and come back to foregoround again
            MusicManager.initializeAndPlayMediaPlayer(this, Utils.getIfSoundIsMuted(this));
        }
        onResumeCalledBefore = true;
    }

    @Override
    public void onBackPressed() {
        SoundPoolManager.getInstance().playSound(2);
        // signify to onPause that we do not stop playing mediaplayer
        goingToPreviousActivity = true;
        super.onBackPressed();
    }
}

class PageAdapter extends FragmentPagerAdapter {
    private int numOfTabs;
    private Context context;
    private AllCategory allCategory;
    private Fragment m1stFragment;


    PageAdapter(FragmentManager fm, int numOfTabs, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.context = context;
        allCategory = new AllCategory();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return context.getString(R.string.allcategories);
        } else {
            return context.getString(R.string.favorite);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return allCategory;
            case 1:
                return new Favorite();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
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
                m1stFragment = (AllCategory) createdFragment;
                break;
        }
        return createdFragment;
    }
}
