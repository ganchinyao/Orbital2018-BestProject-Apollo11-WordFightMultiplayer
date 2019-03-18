package com.ganwl.multiplayerwordgame.packs;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ganwl.multiplayerwordgame.R;
import com.ganwl.multiplayerwordgame.fragments.AllCategoryMultiplayer;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;

import java.util.ArrayList;


public class RecyclerViewAdapterMultiplayer extends RecyclerView.Adapter<RecyclerViewAdapterMultiplayer.ViewHolder> {
    private ArrayList<Pack> mDataset;
    private AllCategoryMultiplayer allCategory;
    private int selectedPosition = 0; // first position selected
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView categoryTextView;
        ImageView categoryImageView;
        RelativeLayout container;
        boolean isSelected;

        public ViewHolder(RelativeLayout view) {
            super(view);
            container = view;
            categoryTextView = view.findViewById(R.id.cardview_categoryName);
            categoryImageView = view.findViewById(R.id.cardview_categoryImage);
            isSelected = false;
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;

            if(isSelected) {
                container.setBackgroundResource(R.drawable.categorychecked);
            } else {
                container.setBackgroundResource(R.drawable.categoryunchecked);
            }
        }

        void setIsLocked() {
            View topPadding_default = container.findViewById(R.id.cardview_notLockTopPadding);
            View topPadding_forLock = container.findViewById(R.id.cardview_lockTopPadding);
            ImageView lockedIcon = container.findViewById(R.id.cardview_lockIcon);

            topPadding_default.setVisibility(View.GONE);
            topPadding_forLock.setVisibility(View.VISIBLE);
            lockedIcon.setVisibility(View.VISIBLE);
            categoryImageView.setAlpha(0.1f);
        }

        void setIsUnlocked() {
            View topPadding_default = container.findViewById(R.id.cardview_notLockTopPadding);
            View topPadding_forLock = container.findViewById(R.id.cardview_lockTopPadding);
            ImageView lockedIcon = container.findViewById(R.id.cardview_lockIcon);

            topPadding_default.setVisibility(View.VISIBLE);
            topPadding_forLock.setVisibility(View.GONE);
            lockedIcon.setVisibility(View.GONE);
            categoryImageView.setAlpha(1f);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapterMultiplayer(ArrayList<Pack> myDataset, AllCategoryMultiplayer allCategory, Context context) {
        mDataset = myDataset;
        this.allCategory = allCategory;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapterMultiplayer.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.categoryTextView.setText(mDataset.get(position).getPackName());
        holder.categoryImageView.setImageResource(mDataset.get(position).getPackImage());

        // set default category
        if(mDataset.get(position).getPackName().equals(Constants.currentPackSelection)) {
            holder.container.setBackgroundResource(R.drawable.categorychecked);
            holder.setIsSelected(true);
        }

        holder.setIsSelected(selectedPosition == position);

        if(mDataset.get(position).getIsLocked()) {
            // current pack is locked
            holder.setIsLocked();
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    final View view = inflater.inflate(R.layout.lockedpack_popup, null);
                    builder.setView(view);

                    TextView unlockPackButton = view.findViewById(R.id.locked_unlockButton);
                    TextView cancelButton = view.findViewById(R.id.locked_cancelButton);

                    final AlertDialog lockedDialog = builder.create();
                    Window dialogWindow = lockedDialog.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
                    }

                    lockedDialog.setCancelable(true);
                    lockedDialog.show();

                    unlockPackButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int currentUserJewel = Utils.getUserJewel(context);
                            if(currentUserJewel < Constants.UNLOCK_PACK_JEWEL_REQUIRED) {
                                // not enough jewel to unlock
                                Toast.makeText(context, "You do not have enough Jewels to unlock!", Toast.LENGTH_SHORT).show();
                            } else {
                                // purchased
                                Utils.setUserJewel(context, currentUserJewel - Constants.UNLOCK_PACK_JEWEL_REQUIRED);
                                switch (mDataset.get(position).getLevelDifficulty()) {
                                    case Constants.EASY_MODE:
                                        Utils.unlockPack_Easy(context, mDataset.get(position).getPackName());
                                        break;
                                    case Constants.MEDIUM_MODE:
                                        Utils.unlockPack_Medium(context, mDataset.get(position).getPackName());
                                        break;
                                    case Constants.HARD_MODE:
                                        Utils.unlockPack_Hard(context, mDataset.get(position).getPackName());
                                        break;
                                    case Constants.INSANE_MODE:
                                        Utils.unlockPack_Insane(context, mDataset.get(position).getPackName());
                                        break;
                                }

                                ((TextView) view.findViewById(R.id.locked_title)).setText(context.getString(R.string.successfullyunlock));
                                view.findViewById(R.id.locked_unlockImageView).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.lock_unlockedPaddingBottom).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.locked_desc).setVisibility(View.GONE);
                                view.findViewById(R.id.locked_unlockButton).setVisibility(View.GONE);
                                view.findViewById(R.id.locked_cancelButton).setVisibility(View.GONE);
                                view.findViewById(R.id.lock_originalPaddingBottom).setVisibility(View.GONE);
                                mDataset.get(position).setUnlock();
                                notifyItemChanged(position);
                                Utils.setIfJewelIsUpdated(context, true);
                            }
                        }
                    });


                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            lockedDialog.dismiss();
                        }
                    });

                }
            });

        } else {
            // not lock, therefore normal
            holder.setIsUnlocked();
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundPoolManager.getInstance().playSound(0); // play general click sound
                    packClicked(holder.getAdapterPosition(), true);
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void packClicked(int position, boolean broadCastToOthers) {
        // deselect old and select new
        notifyItemChanged(selectedPosition);
        selectedPosition = position;
        notifyItemChanged(selectedPosition);

        Constants.currentPackSelection = mDataset.get(position).getPackName();

        // broadcast to others
        if(broadCastToOthers) {
            allCategory.categoryPackChanged(position);
        }
    }
}

