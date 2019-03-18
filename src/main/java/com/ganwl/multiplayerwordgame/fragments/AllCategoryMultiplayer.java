package com.ganwl.multiplayerwordgame.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
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
import com.ganwl.multiplayerwordgame.packs.RecyclerViewAdapterMultiplayer;

import java.util.ArrayList;

public class AllCategoryMultiplayer extends Fragment {
    View rootView;
    RecyclerViewAdapterMultiplayer mAdapter;
    private OnCategoryPackClickListener mCallback;
    private RecyclerView mRecyclerView;

    public interface OnCategoryPackClickListener {
        void onCategoryPackClicked(int position);
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

    public void swapAdapter(int levelDifficulty) {
        switch (levelDifficulty) {
            case Constants.EASY_MODE:
                mAdapter = new RecyclerViewAdapterMultiplayer(getPack(Constants.EASY_MODE), this, getContext());
                mRecyclerView.swapAdapter(mAdapter, false);
                break;
            case Constants.MEDIUM_MODE:
                mAdapter = new RecyclerViewAdapterMultiplayer(getPack(Constants.MEDIUM_MODE), this, getContext());
                mRecyclerView.swapAdapter(mAdapter, false);
                break;
            case Constants.HARD_MODE:
                mAdapter = new RecyclerViewAdapterMultiplayer(getPack(Constants.HARD_MODE), this, getContext());
                mRecyclerView.swapAdapter(mAdapter, false);
                break;
            case Constants.INSANE_MODE:
                mAdapter = new RecyclerViewAdapterMultiplayer(getPack(Constants.INSANE_MODE), this, getContext());
                mRecyclerView.swapAdapter(mAdapter, false);
                break;
        }
    }

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


        mAdapter = new RecyclerViewAdapterMultiplayer(getPack(Utils.getDifficultyPreference(getContext())), this, getContext());
        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    // used by broadcast to indicate packs has change from other users selecting the pack
    public void packsSelected(int levelDifficulty, int position) {
        swapAdapter(levelDifficulty);
        mAdapter.packClicked(position, false);
    }

    // used to broadcast to others
    public void categoryPackChanged(int position) {
        mCallback.onCategoryPackClicked(position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnCategoryPackClickListener) context;
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
                mCallback = (OnCategoryPackClickListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnCategoryPackChangedListener");
            }
        }
    }
}
