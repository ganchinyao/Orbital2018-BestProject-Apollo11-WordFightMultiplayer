package com.ganwl.multiplayerwordgame.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ganwl.multiplayerwordgame.R;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.Utils;
import com.ganwl.multiplayerwordgame.packs.Pack;
import com.ganwl.multiplayerwordgame.packs.PackData;
import com.ganwl.multiplayerwordgame.packs.RecyclerViewAdapter;

import java.util.ArrayList;

public class AllCategory extends Fragment {
    View rootView;
    private RecyclerView mRecyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.allcategory_layout, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);

        mRecyclerView.setLayoutManager(mLayoutManager);


        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(getPack(Utils.getDifficultyPreference(getContext())), getContext());
        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    public void swapAdapter(int levelDifficulty) {
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                mRecyclerView.swapAdapter(new RecyclerViewAdapter(getPack(Constants.EASY_MODE), getContext()), false);
                break;
            case Constants.MEDIUM_MODE:
                mRecyclerView.swapAdapter(new RecyclerViewAdapter(getPack(Constants.MEDIUM_MODE), getContext()), false);
                break;
            case Constants.HARD_MODE:
                mRecyclerView.swapAdapter(new RecyclerViewAdapter(getPack(Constants.HARD_MODE), getContext()), false);
                break;
            case Constants.INSANE_MODE:
                mRecyclerView.swapAdapter(new RecyclerViewAdapter(getPack(Constants.INSANE_MODE), getContext()), false);
                break;
        }
    }

    private ArrayList<Pack> getPack(int levelDifficulty) {
        ArrayList<Pack> dataArrayList = new ArrayList<>();
        String[] arr;
        Integer[] iconArr;
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                arr = PackData.nameArray_Easy;
                iconArr = PackData.drawableArray_Easy;
                break;
            case Constants.MEDIUM_MODE:
                arr = PackData.nameArray_Medium;
                iconArr = PackData.drawableArray_Medium;
                break;
            case Constants.HARD_MODE:
                arr = PackData.nameArray_Hard;
                iconArr = PackData.drawableArray_Hard;
                break;
            case Constants.INSANE_MODE:
                arr = PackData.nameArray_Insane;
                iconArr = PackData.drawableArray_Insane;
                break;
            default:
                arr = PackData.nameArray_Easy;
                iconArr = PackData.drawableArray_Easy;
                break;
        }

        for (int i = 0; i < arr.length; i++) {
            dataArrayList.add(new Pack(
                    arr[i],
                    iconArr[i], PackData.isPackLocked(levelDifficulty, arr[i], getContext()), levelDifficulty
            ));
        }
        return dataArrayList;
    }
}
