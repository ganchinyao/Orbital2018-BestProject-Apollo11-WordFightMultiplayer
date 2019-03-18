package com.ganwl.multiplayerwordgame.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganwl.multiplayerwordgame.R;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;

import java.util.LinkedList;
import java.util.Queue;

public class MultiplayerGameSettings extends Fragment implements View.OnClickListener {
    View rootView;
    private ImageView passItOnSpellButton, tiltSpellButton, jumbleSpellButton, hexSpellButton,
            seeMoreSpellButton, reverseSpellButton, blockSpellButton, guardianAngelSpellButton,
            twoTimesExpSpellButton;
    private TextView passItOnSpellTextView, tiltSpellTextView, jumbleSpellTextView, hexSpellTextView,
            seeMoreSpellTextView, reverseSpellTextView, blockSpellTextView, guardianAngelSpellTextView,
            twoTimesExpSpellTextView;
    // use a queue system to select spell, i.e. the 5th selected will deselect the 1st selected
    private Queue<Integer> spellQueue;

    private OnSpellSelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.multiplayer_gamesettings, container, false);

        spellQueue = new LinkedList<>();

        passItOnSpellButton = rootView.findViewById(R.id.multiplayer_settings_passItOnSpell_ImageView);
        passItOnSpellTextView = rootView.findViewById(R.id.multiplayer_settings_passItOnSpell_TextView);
        tiltSpellButton = rootView.findViewById(R.id.multiplayer_settings_tiltSpell_ImageView);
        tiltSpellTextView = rootView.findViewById(R.id.multiplayer_settings_tiltSpell_TextView);
        jumbleSpellButton = rootView.findViewById(R.id.multiplayer_settings_jumbleSpell_ImageView);
        jumbleSpellTextView = rootView.findViewById(R.id.multiplayer_settings_jumbleSpell_TextView);
        hexSpellButton = rootView.findViewById(R.id.multiplayer_settings_hexSpell_ImageView);
        hexSpellTextView = rootView.findViewById(R.id.multiplayer_settings_hexSpell_TextView);
        seeMoreSpellButton = rootView.findViewById(R.id.multiplayer_settings_seeMoreSpell_ImageView);
        seeMoreSpellTextView = rootView.findViewById(R.id.multiplayer_settings_seeMoreSpell_TextView);
        reverseSpellButton = rootView.findViewById(R.id.multiplayer_settings_reverseSpell_ImageView);
        reverseSpellTextView = rootView.findViewById(R.id.multiplayer_settings_reverseSpell_TextView);
        blockSpellButton = rootView.findViewById(R.id.multiplayer_settings_blockSpell_ImageView);
        blockSpellTextView = rootView.findViewById(R.id.multiplayer_settings_blockSpell_TextView);
        guardianAngelSpellButton = rootView.findViewById(R.id.multiplayer_settings_guardianAngelSpell_ImageView);
        guardianAngelSpellTextView = rootView.findViewById(R.id.multiplayer_settings_guardianAngelSpell_TextView);
        twoTimesExpSpellButton = rootView.findViewById(R.id.multiplayer_settings_2xSpell_ImageView);
        twoTimesExpSpellTextView = rootView.findViewById(R.id.multiplayer_settings_2xSpell_TextView);

        passItOnSpellButton.setOnClickListener(this);
        passItOnSpellTextView.setOnClickListener(this);
        tiltSpellButton.setOnClickListener(this);
        tiltSpellTextView.setOnClickListener(this);
        jumbleSpellButton.setOnClickListener(this);
        jumbleSpellTextView.setOnClickListener(this);
        hexSpellButton.setOnClickListener(this);
        hexSpellTextView.setOnClickListener(this);
        seeMoreSpellButton.setOnClickListener(this);
        seeMoreSpellTextView.setOnClickListener(this);
        reverseSpellButton.setOnClickListener(this);
        reverseSpellTextView.setOnClickListener(this);
        blockSpellButton.setOnClickListener(this);
        blockSpellTextView.setOnClickListener(this);
        guardianAngelSpellButton.setOnClickListener(this);
        guardianAngelSpellTextView.setOnClickListener(this);
        twoTimesExpSpellButton.setOnClickListener(this);
        twoTimesExpSpellTextView.setOnClickListener(this);

        // set the initial 4 spells to be selected
        int firstSpellID = Utils.getFirstSelectedSpell(getContext());
        int secondSpellID = Utils.getSecondSelectedSpell(getContext());
        int thirdSpellID = Utils.getThirdSelectedSpell(getContext());
        int forthSpellID = Utils.getForthSelectedSpell(getContext());
        setSpellSelected(firstSpellID);
        setSpellSelected(secondSpellID);
        setSpellSelected(thirdSpellID);
        setSpellSelected(forthSpellID);
        spellQueue.add(firstSpellID);
        spellQueue.add(secondSpellID);
        spellQueue.add(thirdSpellID);
        spellQueue.add(forthSpellID);

        return rootView;
    }

    private void setSpellSelected(int spellID) {
        switch (spellID) {
            case Constants.SPELL_THROW:
                passItOnSpellButton.setAlpha(1f);
                passItOnSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_TILT:
                tiltSpellButton.setAlpha(1f);
                tiltSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_JUMBLE:
                jumbleSpellButton.setAlpha(1f);
                jumbleSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_HEX:
                hexSpellButton.setAlpha(1f);
                hexSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_SEEMORE:
                seeMoreSpellButton.setAlpha(1f);
                seeMoreSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_REVERSE:
                reverseSpellButton.setAlpha(1f);
                reverseSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_BLOCK:
                blockSpellButton.setAlpha(1f);
                blockSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_GUARDIANANGEL:
                guardianAngelSpellButton.setAlpha(1f);
                guardianAngelSpellTextView.setAlpha(1f);
                break;
            case Constants.SPELL_TWOTIMESEXP:
                twoTimesExpSpellButton.setAlpha(1f);
                twoTimesExpSpellTextView.setAlpha(1f);
                break;
        }
    }

    private void deselectSpell(int spellID) {
        switch (spellID) {
            case Constants.SPELL_THROW:
                passItOnSpellButton.setAlpha(0.3f);
                passItOnSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_TILT:
                tiltSpellButton.setAlpha(0.3f);
                tiltSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_JUMBLE:
                jumbleSpellButton.setAlpha(0.3f);
                jumbleSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_HEX:
                hexSpellButton.setAlpha(0.3f);
                hexSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_SEEMORE:
                seeMoreSpellButton.setAlpha(0.3f);
                seeMoreSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_REVERSE:
                reverseSpellButton.setAlpha(0.3f);
                reverseSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_BLOCK:
                blockSpellButton.setAlpha(0.3f);
                blockSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_GUARDIANANGEL:
                guardianAngelSpellButton.setAlpha(0.3f);
                guardianAngelSpellTextView.setAlpha(0.3f);
                break;
            case Constants.SPELL_TWOTIMESEXP:
                twoTimesExpSpellButton.setAlpha(0.3f);
                twoTimesExpSpellTextView.setAlpha(0.3f);
                break;
        }
    }

    // to be called when the text of the spell is clicked, and pop up a dialog to show user the description
    // of the spell
    private void createSpellDialog(int spellID) {
        SoundPoolManager.getInstance().playSound(4); // play drop down open sound

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.spell_description_dialog, null);
        builder.setView(view);

        final android.support.v7.app.AlertDialog dialogPopup = builder.create();
        Window dialogWindow = dialogPopup.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }
        dialogPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SoundPoolManager.getInstance().playSound(5); // play close dialog sound
            }
        });

        dialogPopup.setCancelable(true);
        dialogPopup.show();

        int imageID;
        String spellName, spellType, spellTypeDescription, spellMainDescription, spellExtraDescription, spellFlavorDescription;
        Resources res = getResources();

        switch (spellID) {
            case Constants.SPELL_BLOCK:
                imageID = R.drawable.spell_blockspell_unclick;
                spellName = res.getString(R.string.block);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_block_desc);
                spellExtraDescription = res.getString(R.string.duration) + " " + (Constants.DURATION_SPELL_BLOCK / 1000.0) + " " + res.getString(R.string.seconds);
                spellFlavorDescription = res.getString(R.string.spell_block_desc_flavor);
                break;
            case Constants.SPELL_HEX:
                imageID = R.drawable.spell_hex_unclick;
                spellName = res.getString(R.string.hex);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_hex_desc);
                spellExtraDescription = res.getString(R.string.duration) + " " + (Constants.DURATION_SPELL_HEX / 1000.0) + " " + res.getString(R.string.seconds);
                spellFlavorDescription = res.getString(R.string.spell_hex_desc_flavor);
                break;
            case Constants.SPELL_JUMBLE:
                imageID = R.drawable.spell_jumble_unclick;
                spellName = res.getString(R.string.jumble);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_jumble_desc);
                spellExtraDescription = res.getString(R.string.duration) + " " + (Constants.DURATION_SPELL_JUMBLE / 1000.0) + " " + res.getString(R.string.seconds);
                spellFlavorDescription = res.getString(R.string.spell_jumble_desc_flavor);
                break;
            case Constants.SPELL_THROW:
                imageID = R.drawable.spell_passiton_unclick;
                spellName = res.getString(R.string.passiton);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_throw_desc);
                spellExtraDescription = "";
                spellFlavorDescription = res.getString(R.string.spell_throw_desc_flavor);
                break;
            case Constants.SPELL_REVERSE:
                imageID = R.drawable.spell_reverse_unclick;
                spellName = res.getString(R.string.reverse);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_reverse_desc);
                spellExtraDescription = "";
                spellFlavorDescription = res.getString(R.string.spell_reverse_desc_flavor);
                break;
            case Constants.SPELL_SEEMORE:
                imageID = R.drawable.spell_seemore_unclick;
                spellName = res.getString(R.string.seemore);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_seemore_desc);
                spellExtraDescription = res.getString(R.string.duration) + " " + (Constants.DURATION_SPELL_SEEMORE / 1000.0) + " " + res.getString(R.string.seconds);
                spellFlavorDescription = res.getString(R.string.spell_seemore_desc_flavor);
                break;
            case Constants.SPELL_TILT:
                imageID = R.drawable.spell_tilt_unclick;
                spellName = res.getString(R.string.tilt);
                spellType = res.getString(R.string.activespell);
                spellTypeDescription = res.getString(R.string.typeactive_desc);
                spellMainDescription = res.getString(R.string.spell_tilt_desc);
                spellExtraDescription = res.getString(R.string.duration) + " " + (Constants.DURATION_SPELL_TILT / 1000.0) + " " + res.getString(R.string.seconds);
                spellFlavorDescription = res.getString(R.string.spell_tilt_desc_flavor);
                break;
            case Constants.SPELL_GUARDIANANGEL:
                imageID = R.drawable.spell_guardianangel_unclick;
                spellName = res.getString(R.string.guardianangel);
                spellType = res.getString(R.string.passivespell);
                spellTypeDescription = res.getString(R.string.typepassive_desc);
                spellMainDescription = res.getString(R.string.spell_guardianangel_desc);
                spellExtraDescription = res.getString(R.string.spell_guardian_desc_condition);
                spellFlavorDescription = res.getString(R.string.spell_guardian_desc_flavor);
                break;
            case Constants.SPELL_TWOTIMESEXP:
                imageID = R.drawable.spell_twotimesscore_unclick;
                spellName = res.getString(R.string.twotimesscore);
                spellType = res.getString(R.string.passivespell);
                spellTypeDescription = res.getString(R.string.typepassive_desc);
                spellMainDescription = res.getString(R.string.spell_twotimesexp_desc);
                spellExtraDescription = res.getString(R.string.spell_twotimesexp_desc_condition);
                spellFlavorDescription = res.getString(R.string.spell_twotimesexp_desc_flavor);
                break;
            default:
                // default should not be called at all, hence dummy value just for sake of initialization
                imageID = R.drawable.spell_seemore_unclick;
                spellName = "";
                spellType = "";
                spellTypeDescription = "";
                spellMainDescription = "";
                spellExtraDescription = "";
                spellFlavorDescription = "";
        }

        // set all resources
        ((ImageView) view.findViewById(R.id.spell_dialog_ImageView)).setImageResource(imageID);
        ((TextView) view.findViewById(R.id.spell_dialog_spellNameTextView)).setText(spellName);
        ((TextView) view.findViewById(R.id.spell_dialog_spellTypeTextView)).setText(spellType);
        ((TextView) view.findViewById(R.id.spell_dialog_spellTypeDescription)).setText(spellTypeDescription);
        ((TextView) view.findViewById(R.id.spell_dialog_spellMainDesc)).setText(spellMainDescription);
        ((TextView) view.findViewById(R.id.spell_dialog_spellExtraDesc)).setText(spellExtraDescription);
        ((TextView) view.findViewById(R.id.spell_dialog_spellFlavorDesc)).setText(spellFlavorDescription);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multiplayer_settings_passItOnSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_THROW)) { // only add if not already selected
                    setSpellSelected(Constants.SPELL_THROW);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_THROW);
                    mCallback.onSpellSelected(Constants.SPELL_THROW);
                }
                break;

            case R.id.multiplayer_settings_passItOnSpell_TextView:
                createSpellDialog(Constants.SPELL_THROW);
                break;

            case R.id.multiplayer_settings_tiltSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_TILT)) {
                    setSpellSelected(Constants.SPELL_TILT);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_TILT);
                    mCallback.onSpellSelected(Constants.SPELL_TILT);
                }
                break;

            case R.id.multiplayer_settings_tiltSpell_TextView:
                createSpellDialog(Constants.SPELL_TILT);
                break;

            case R.id.multiplayer_settings_jumbleSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_JUMBLE)) {
                    setSpellSelected(Constants.SPELL_JUMBLE);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_JUMBLE);
                    mCallback.onSpellSelected(Constants.SPELL_JUMBLE);
                }
                break;

            case R.id.multiplayer_settings_jumbleSpell_TextView:
                createSpellDialog(Constants.SPELL_JUMBLE);
                break;

            case R.id.multiplayer_settings_hexSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_HEX)) {
                    setSpellSelected(Constants.SPELL_HEX);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_HEX);
                    mCallback.onSpellSelected(Constants.SPELL_HEX);
                }
                break;

            case R.id.multiplayer_settings_hexSpell_TextView:
                createSpellDialog(Constants.SPELL_HEX);
                break;

            case R.id.multiplayer_settings_seeMoreSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_SEEMORE)) {
                    setSpellSelected(Constants.SPELL_SEEMORE);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_SEEMORE);
                    mCallback.onSpellSelected(Constants.SPELL_SEEMORE);
                }
                break;

            case R.id.multiplayer_settings_seeMoreSpell_TextView:
                createSpellDialog(Constants.SPELL_SEEMORE);
                break;

            case R.id.multiplayer_settings_reverseSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_REVERSE)) {
                    setSpellSelected(Constants.SPELL_REVERSE);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_REVERSE);
                    mCallback.onSpellSelected(Constants.SPELL_REVERSE);
                }
                break;

            case R.id.multiplayer_settings_reverseSpell_TextView:
                createSpellDialog(Constants.SPELL_REVERSE);
                break;

            case R.id.multiplayer_settings_blockSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_BLOCK)) {
                    setSpellSelected(Constants.SPELL_BLOCK);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_BLOCK);
                    mCallback.onSpellSelected(Constants.SPELL_BLOCK);
                }
                break;

            case R.id.multiplayer_settings_blockSpell_TextView:
                createSpellDialog(Constants.SPELL_BLOCK);
                break;

            case R.id.multiplayer_settings_guardianAngelSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_GUARDIANANGEL)) {
                    setSpellSelected(Constants.SPELL_GUARDIANANGEL);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_GUARDIANANGEL);
                    mCallback.onSpellSelected(Constants.SPELL_GUARDIANANGEL);
                }
                break;

            case R.id.multiplayer_settings_guardianAngelSpell_TextView:
                createSpellDialog(Constants.SPELL_GUARDIANANGEL);
                break;

            case R.id.multiplayer_settings_2xSpell_ImageView:
                SoundPoolManager.getInstance().playSound(0); // play general click sound
                if (!spellQueue.contains(Constants.SPELL_TWOTIMESEXP)) {
                    setSpellSelected(Constants.SPELL_TWOTIMESEXP);
                    deselectSpell(spellQueue.poll());
                    spellQueue.add(Constants.SPELL_TWOTIMESEXP);
                    mCallback.onSpellSelected(Constants.SPELL_TWOTIMESEXP);
                }
                break;

            case R.id.multiplayer_settings_2xSpell_TextView:
                createSpellDialog(Constants.SPELL_TWOTIMESEXP);
                break;
        }

    }

    // Container Activity must implement this interface, talk to the activity
    public interface OnSpellSelectedListener {
        public void onSpellSelected(int spellID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSpellSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSpellSelectedListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                mCallback = (OnSpellSelectedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSpellSelectedListener");
            }
        }
    }
}
