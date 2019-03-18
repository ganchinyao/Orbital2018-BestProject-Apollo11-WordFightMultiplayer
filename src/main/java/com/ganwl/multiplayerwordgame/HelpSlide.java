package com.ganwl.multiplayerwordgame;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpSlide extends Fragment{

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;
    private String title, desc;
    private int backgroundColor, imageId;

    public static HelpSlide newInstance(int backgroundColor, String title, int imageId, String desc) {
        HelpSlide sampleSlide = new HelpSlide();
        sampleSlide.backgroundColor = backgroundColor;
        sampleSlide.title = title;
        sampleSlide.imageId = imageId;
        sampleSlide.desc = desc;

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, R.layout.helpslide_layout);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);
        ((TextView) view.findViewById(R.id.help_title)).setText(title);
        ((TextView) view.findViewById(R.id.help_desc)).setText(desc);
        ((ImageView) view.findViewById(R.id.help_image)).setImageResource(imageId);
        view.setBackgroundColor(backgroundColor);

        return view;
    }
}