package com.ganwl.multiplayerwordgame.helper;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.ganwl.multiplayerwordgame.R;

// static methods all call sound pool via here
public class SoundPoolManager extends Application {

    private static SoundPoolManager singleton;
    private static SoundPool soundPool;
    private int popUpWindowSoundId, closeMenuSoundId, generalClickSoundId, swipePagesId, dropDownOpenId, dropDownCloseId, startButtonId,
            correctSoundId1, correctSoundId2, correctSoundId3, keyboardTypeId;
    private boolean popUpSoundLoaded, closeMenuSoundLoaded, generalClickSoundLoaded, swipePagesLoaded, dropDownOpenLoaded, dropDownCloseLoaded, startButtonLoaded,
            correctSoundLoaded1, correctSoundLoaded2, correctSoundLoaded3, keyboardTypeLoaded;

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
        createSoundPool(this);
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
    }

    @SuppressWarnings("deprecation")
    private void createSoundPool(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(10)
                    .build();
        } else {
            soundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    // 0 means success according to android documentation
                    // determine which sound is ready via an if-else. Cant use switch since the soundId is not editText constant expression
                    if (sampleId == generalClickSoundId) {
                        generalClickSoundLoaded = true;
                    } else if (sampleId == popUpWindowSoundId) {
                        popUpSoundLoaded = true;
                    } else if (sampleId == closeMenuSoundId) {
                        closeMenuSoundLoaded = true;
                    } else if (sampleId == swipePagesId) {
                        swipePagesLoaded = true;
                    } else if (sampleId == dropDownOpenId) {
                        dropDownOpenLoaded = true;
                    } else if (sampleId == dropDownCloseId) {
                        dropDownCloseLoaded = true;
                    } else if (sampleId == startButtonId) {
                        startButtonLoaded = true;
                    } else if (sampleId == correctSoundId1) {
                        correctSoundLoaded1 = true;
                    } else if (sampleId == correctSoundId2) {
                        correctSoundLoaded2 = true;
                    } else if (sampleId == correctSoundId3) {
                        correctSoundLoaded3 = true;
                    } else if (sampleId == keyboardTypeId) {
                        keyboardTypeLoaded = true;
                    }
                }
            }
        });

        // load all the various sound to their respective soundId
        dropDownCloseId = soundPool.load(context, R.raw.dropdownclose, 1);
        popUpWindowSoundId = soundPool.load(context, R.raw.homepage_popupappear, 1);
        closeMenuSoundId = soundPool.load(context, R.raw.homepage_buttonclosequickplay, 1);
        generalClickSoundId = soundPool.load(context, R.raw.homepage_buttonclicked, 1);
        swipePagesId = soundPool.load(context, R.raw.swipesound, 1);
        dropDownOpenId = soundPool.load(context, R.raw.dropdownopen, 1);
        startButtonId = soundPool.load(context, R.raw.startbutton, 1);
        correctSoundId1 = soundPool.load(context, R.raw.answer_correct1, 1);
        correctSoundId2 = soundPool.load(context, R.raw.answer_correct2, 1);
        correctSoundId3 = soundPool.load(context, R.raw.answer_correct3, 1);
        keyboardTypeId = soundPool.load(context, R.raw.keyboardtype, 1);
    }


    public static SoundPoolManager getInstance() {
        return singleton;
    }

    // all soundPool play sound to call this method, and passed in different param for different sound
    // method check if the global mute button is active, as well as if soundPool has finished loading the sound
    public void playSound(int soundToPlay) {
        if (soundPool != null && !Utils.getIfSoundIsMuted(this)) {
            switch (soundToPlay) {
                case 0:
                    // play general click sound
                    if (generalClickSoundLoaded) {
                        soundPool.play(generalClickSoundId, 1, 1, 1, 0, 1f);
                    }

                    break;
                case 1:
                    // play popup appear sound
                    if (popUpSoundLoaded)
                        soundPool.play(popUpWindowSoundId, 1, 1, 1, 0, 1f);
                    break;
                case 2:
                    // play menu close sound
                    if (closeMenuSoundLoaded)
                        soundPool.play(closeMenuSoundId, 1, 1, 1, 0, 1f);
                    break;
                case 3:
                    // play swipe pages sound
                    if (swipePagesLoaded)
                        soundPool.play(swipePagesId, 1, 1, 1, 0, 1f);
                    break;
                case 4:
                    // play drop down open sound
                    if (dropDownOpenLoaded)
                        soundPool.play(dropDownOpenId, 1, 1, 1, 0, 1f);
                    break;
                case 5:
                    // play drop down closed sound
                    if (dropDownCloseLoaded)
                        soundPool.play(dropDownCloseId, 1, 1, 1, 0, 1f);
                    break;
                case 6:
                    // play start button sound
                    if (startButtonLoaded)
                        soundPool.play(startButtonId, 1, 1, 1, 0, 1f);
                    break;

                case 7:
                    // play correct word sound
                    if (correctSoundLoaded1)
                        soundPool.play(correctSoundId1, 1, 1, 1, 0, 1f);
                    break;

                case 8:
                    // play correct word sound
                    if (correctSoundLoaded2)
                        soundPool.play(correctSoundId2, 1, 1, 1, 0, 1f);
                    break;

                case 9:
                    // play correct word sound
                    if (correctSoundLoaded3)
                        soundPool.play(correctSoundId3, 1, 1, 1, 0, 1f);
                    break;

                case 10:
                    // play keyboard sound
                    if (keyboardTypeLoaded)
                        soundPool.play(keyboardTypeId, 1, 1, 1, 0, 1f);
                    break;
            }
        }

    }
}
