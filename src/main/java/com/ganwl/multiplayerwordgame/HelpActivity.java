package com.ganwl.multiplayerwordgame;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ganwl.multiplayerwordgame.helper.Utils;
import com.github.paolorotolo.appintro.AppIntro;

public class HelpActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(HelpSlide.newInstance(Color.parseColor("#4bafdf"), getString(R.string.help_page1_title), R.drawable.help1, getString(R.string.help_page1_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#ee7a47"), getString(R.string.help_page2_title), R.drawable.help2, getString(R.string.help_page2_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#fc9dc9"), getString(R.string.help_page3_title), R.drawable.help3, getString(R.string.help_page3_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#9363bc"), getString(R.string.help_page4_title), R.drawable.help4, getString(R.string.help_page4_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#9d7489"), getString(R.string.help_page5_title), R.drawable.help5, getString(R.string.help_page5_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#005c83"), getString(R.string.help_page6_title), R.drawable.help6, getString(R.string.help_page6_desc)));
        addSlide(HelpSlide.newInstance(Color.parseColor("#8a8f8e"), getString(R.string.help_page7_title), R.drawable.help7, getString(R.string.help_page7_desc)));

        // first bar
        setBarColor(Color.BLACK);

        // Hide Skip/Done button.
        if (Utils.getIfFirstLaunch(HelpActivity.this)) {
            showSkipButton(false);
            Utils.setIfFirstLaunch(HelpActivity.this, false);
        } else {
            showSkipButton(true);
        }
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}