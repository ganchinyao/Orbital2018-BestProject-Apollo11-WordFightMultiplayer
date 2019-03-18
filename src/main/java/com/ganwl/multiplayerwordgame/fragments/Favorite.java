package com.ganwl.multiplayerwordgame.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ganwl.multiplayerwordgame.R;

public class Favorite extends Fragment {
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.favorite_layout, container, false);

        return rootView;
    }
}
