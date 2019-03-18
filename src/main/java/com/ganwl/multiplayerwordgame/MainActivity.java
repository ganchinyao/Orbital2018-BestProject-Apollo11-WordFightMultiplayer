package com.ganwl.multiplayerwordgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.ganwl.multiplayerwordgame.helper.BytesUtil;
import com.ganwl.multiplayerwordgame.helper.Constants;
import com.ganwl.multiplayerwordgame.helper.MusicManager;
import com.ganwl.multiplayerwordgame.helper.SavedGameState;
import com.ganwl.multiplayerwordgame.helper.SnapshotCoordinator;
import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;
import com.ganwl.multiplayerwordgame.helper.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.facebook.ads.*;

import static com.ganwl.multiplayerwordgame.helper.Constants.signedIn;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, BillingProcessor.IBillingHandler {
    private boolean settingsKeyboardOut = false;
    private ImageView questionMark;
    private static final String TAG = "gggg";
    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;
    // Client used to interact with Google Snapshots.
    private SnapshotsClient mSnapshotsClient = null;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Request code for listing saved games
    private static final int RC_LIST_SAVED_GAMES = 9002;

    private static final int RC_ACHIEVEMENT_UI = 9003;

    // Members related to the conflict resolution chooser of Snapshots.
    final static int MAX_SNAPSHOT_RESOLVE_RETRIES = 50;
    private SavedGameState savedGameState; // the user saved game state

    private int userCurrentJewel;
    private TextView signInTextView;
    private AchievementsClient mAchievementsClient;
    private int shopCurrentPosition = 1; // start from 1 to 2,3,4

    private BillingProcessor bp;

    private NativeBannerAd nativeBannerAd;
    private PopupWindow shopPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.getIfPurchasedNoAds(MainActivity.this)) {
            // user purchased ads
            setContentView(R.layout.activity_main_noads);
        } else {
            // user didnt purchase ads
            setContentView(R.layout.activity_main);
            setUpHomePageNativeAds();
        }

        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Since we are using SavedGames, we need to add the SCOPE_APPFOLDER to access Google Drive.
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build());

        findViewById(R.id.homepage_1player_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set this flag to true since we want to retain background music, and onPause will not stop background music
                MusicManager.isGoingNextActivity = true;
                SoundPoolManager.getInstance().playSound(4);
                Intent intent = new Intent(MainActivity.this, OnePlayerPopup.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.homepage_multiplayer_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set this flag to true since we want to retain background music, and onPause will not stop background music
                MusicManager.isGoingNextActivity = true;
                SoundPoolManager.getInstance().playSound(4);
                Intent intent = new Intent(MainActivity.this, MultiPlayerGame.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.homepage_setting_Button).setOnClickListener(this);
        findViewById(R.id.homepage_shop_Button).setOnClickListener(this);
        findViewById(R.id.homepage_achievementButton).setOnClickListener(this);
        findViewById(R.id.homepage_jewelBuy).setOnClickListener(this);
        questionMark = findViewById(R.id.homepage_help_Button);
        questionMark.setOnClickListener(this);

        if (Utils.getIfFirstLaunch(MainActivity.this)) {
            // first time launch, therefore play help
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        }

        bp = BillingProcessor.newBillingProcessor(this, constructActual_APIKey(), this); // doesn't bind
        bp.initialize(); // binds
    }

    private void setUpHomePageNativeAds() {
        nativeBannerAd = new NativeBannerAd(this, Utils.getHomePageNativeAdId());
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
                hideAd();
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
                inflateAd(nativeBannerAd);
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

    private void inflateAd(NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = findViewById(R.id.homepage_AdTitle);
        AdIconView nativeAdIconView = findViewById(R.id.homepage_Adicon);
        TextView nativeAdCallToAction = findViewById(R.id.homepage_Ad_CTAButton);
        RelativeLayout adChoicesContainer = findViewById(R.id.homepage_AdChoiceIcon);
        View container = findViewById(R.id.view);

        container.setVisibility(View.VISIBLE);
        findViewById(R.id.homepage_adLabel).setVisibility(View.VISIBLE);
        nativeAdTitle.setVisibility(View.VISIBLE);
        nativeAdIconView.setVisibility(View.VISIBLE);
        nativeAdCallToAction.setVisibility(View.VISIBLE);
        adChoicesContainer.setVisibility(View.VISIBLE);

        // Add the AdChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(MainActivity.this, nativeBannerAd, true);
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
        nativeBannerAd.registerViewForInteraction(container, nativeAdIconView, clickableViews);
    }

    private void hideAd() {
        findViewById(R.id.view).setVisibility(View.GONE);
        findViewById(R.id.homepage_adLabel).setVisibility(View.GONE);
        findViewById(R.id.homepage_AdTitle).setVisibility(View.GONE);
        findViewById(R.id.homepage_Adicon).setVisibility(View.GONE);
        findViewById(R.id.homepage_Ad_CTAButton).setVisibility(View.GONE);
        findViewById(R.id.homepage_AdChoiceIcon).setVisibility(View.GONE);
    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        switch (productId) {
            case Constants.noAdsSKU: {
                Utils.setIfPurchasedNoAds(MainActivity.this, true);
                if (shopPopupWindow != null) {
                    shopPopupWindow.dismiss();
                }
                if (nativeBannerAd != null && nativeBannerAd.isAdLoaded()) {
                    nativeBannerAd.destroy();
                    nativeBannerAd = null;
                    hideAd();
                }

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.successfullyrestored_alertdialog, null);
                builder.setView(view);

                final android.support.v7.app.AlertDialog successfullyRestoredDialog = builder.create();
                Window dialogWindow = successfullyRestoredDialog.getWindow();
                if (dialogWindow != null) {
                    dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
                }

                successfullyRestoredDialog.setCancelable(true);
                successfullyRestoredDialog.show();

                successfullyRestoredDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        recreate();
                    }
                });
                break;
            }
            case Constants.oneHundredJewelSKU: {
                dismissShopPopup();
                showSuccessfullyPurchasedDialog();
                addUserJewels(100);
                bp.consumePurchase(Constants.oneHundredJewelSKU);
                break;
            }

            case Constants.threeHundredJewelSKU: {
                dismissShopPopup();
                showSuccessfullyPurchasedDialog();
                addUserJewels(300);
                bp.consumePurchase(Constants.threeHundredJewelSKU);
                break;
            }

            case Constants.nineHundredJewelSKU: {
                dismissShopPopup();
                showSuccessfullyPurchasedDialog();
                addUserJewels(900);
                bp.consumePurchase(Constants.nineHundredJewelSKU);
                break;
            }
        }
    }

    private void dismissShopPopup() {
        if (shopPopupWindow != null) {
            shopPopupWindow.dismiss();
        }
    }

    private void showSuccessfullyPurchasedDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.successfullypurchased_alertdialog, null);
        builder.setView(view);

        final android.support.v7.app.AlertDialog successfullyPurchasedDialog = builder.create();
        Window dialogWindow = successfullyPurchasedDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        successfullyPurchasedDialog.setCancelable(true);
        successfullyPurchasedDialog.show();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    private String constructActual_APIKey() {
        return Constants.In_APP_PURCHASE_YOUR_KEY_FROM_GOOGLE_CONSOLE;
    }


    private void openShopPopup() {
        final View view = getLayoutInflater().inflate(R.layout.shop_layout, null, false);

        shopPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        shopPopupWindow.setTouchable(true);
        shopPopupWindow.setFocusable(true);
        shopPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        shopPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SoundPoolManager.getInstance().playSound(2);
            }
        });

        final int totalAmountOfItems = 4; // change this if add more shop items

        final TextView buyNowButton = view.findViewById(R.id.shop_buynowButton);
        final View[] bubbleArray = new View[totalAmountOfItems];
        View bubble1 = view.findViewById(R.id.shop_page1Circle);
        View bubble2 = view.findViewById(R.id.shop_page2Circle);
        View bubble3 = view.findViewById(R.id.shop_page3Circle);
        View bubble4 = view.findViewById(R.id.shop_page4Circle);
        bubbleArray[0] = bubble1;
        bubbleArray[1] = bubble2;
        bubbleArray[2] = bubble3;
        bubbleArray[3] = bubble4;

        final ImageView itemImageView = view.findViewById(R.id.shop_saleContentImageView);
        final TextView itemTextView = view.findViewById(R.id.shop_saleContentTextView);
        final TextView restorePurchase = view.findViewById(R.id.shop_restorePurchase);
        final TextView shopPriceTextView = view.findViewById(R.id.shop_priceTextView);

        ArrayList<String> sku_ID = new ArrayList<>();
        sku_ID.add(Constants.noAdsSKU); // index 0
        sku_ID.add(Constants.oneHundredJewelSKU); // index 1
        sku_ID.add(Constants.threeHundredJewelSKU); // index 2
        sku_ID.add(Constants.nineHundredJewelSKU); // index 3
        final List<SkuDetails> skuDetails = bp.getPurchaseListingDetails(sku_ID);
        // set first item
        if (skuDetails != null && skuDetails.size() >= 4) {
            // just checking if skuDetails is not null. can use >0 or == 2 instead of >=4 doesnt matter, becos we expect 4 entries
            shopPriceTextView.setText(skuDetails.get(0).priceText); // priceText includes the currency and the value, e.g £2.99, 3,99€, $4.99
        }

        class ShopClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.shop_closeButton:
                        shopPopupWindow.dismiss();
                        break;
                    case R.id.shop_overallLayout:
                        shopPopupWindow.dismiss();
                        break;
                    case R.id.shop_nextRightArrow:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition++;
                        if (shopCurrentPosition > totalAmountOfItems) {
                            shopCurrentPosition = 1; // jump to end
                        }
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_nextLeftArrow:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition--;
                        if (shopCurrentPosition < 1) {
                            shopCurrentPosition = totalAmountOfItems; // jump to end
                        }
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_buynowButton:
                        SoundPoolManager.getInstance().playSound(0);
                        switch (shopCurrentPosition) {
                            case 1:
                                bp.purchase(MainActivity.this, Constants.noAdsSKU);
                                break;
                            case 2:
                                bp.purchase(MainActivity.this, Constants.oneHundredJewelSKU);
                                break;
                            case 3:
                                bp.purchase(MainActivity.this, Constants.threeHundredJewelSKU);
                                break;
                            case 4:
                                bp.purchase(MainActivity.this, Constants.nineHundredJewelSKU);
                                break;
                        }
                        break;
                    case R.id.shop_page1Circle:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition = 1;
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_page2Circle:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition = 2;
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_page3Circle:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition = 3;
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_page4Circle:
                        SoundPoolManager.getInstance().playSound(3);
                        shopCurrentPosition = 4;
                        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
                        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
                        break;
                    case R.id.shop_restorePurchase:
                        // first check if there is network connection
                        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (cm != null) {
                            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                                // there is internet connection. Proceed.
                                bp.loadOwnedPurchasesFromGoogle();
                                boolean hasPurchased = false;
                                if (bp.isPurchased(Constants.noAdsSKU)) {
                                    // user has purchased ads before
                                    Utils.setIfPurchasedNoAds(MainActivity.this, true);
                                    if (nativeBannerAd != null && nativeBannerAd.isAdLoaded()) {
                                        nativeBannerAd.destroy();
                                        nativeBannerAd = null;
                                        hideAd();
                                    }
                                    hasPurchased = true;
                                    shopPopupWindow.dismiss();
                                }

                                if (hasPurchased) {
                                    // show alertdialog
                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                                    View view = inflater.inflate(R.layout.successfullyrestored_alertdialog, null);
                                    builder.setView(view);

                                    final android.support.v7.app.AlertDialog successfullyRestoredDialog = builder.create();
                                    Window dialogWindow = successfullyRestoredDialog.getWindow();
                                    if (dialogWindow != null) {
                                        dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
                                    }

                                    successfullyRestoredDialog.setCancelable(true);
                                    successfullyRestoredDialog.show();

                                    successfullyRestoredDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            recreate();
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, getString(R.string.restorepurchasednopurchase), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                // no network connection
                                Toast.makeText(MainActivity.this, getString(R.string.restorepurchasednointernet), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        ShopClickListener shopClickListener = new ShopClickListener();

        view.findViewById(R.id.shop_closeButton).setOnClickListener(shopClickListener);
        view.setOnClickListener(shopClickListener);
        view.findViewById(R.id.shop_hat).setOnClickListener(shopClickListener); // dummy clicklistener becos the overall layout result in popup dismiss
        view.findViewById(R.id.shop_mainbodyLayout).setOnClickListener(shopClickListener); // dummy clicklistener
        view.findViewById(R.id.shop_nextRightArrow).setOnClickListener(shopClickListener);
        view.findViewById(R.id.shop_nextLeftArrow).setOnClickListener(shopClickListener);
        restorePurchase.setOnClickListener(shopClickListener);
        buyNowButton.setOnClickListener(shopClickListener);
        bubble1.setOnClickListener(shopClickListener);
        bubble2.setOnClickListener(shopClickListener);
        bubble3.setOnClickListener(shopClickListener);
        bubble4.setOnClickListener(shopClickListener);

        // start with page 1
        highlightShopPopupBubble(shopCurrentPosition - 1, bubbleArray);
        shop_changeItem(itemImageView, itemTextView, buyNowButton, restorePurchase, skuDetails, shopPriceTextView);
    }

    // highlight the bubble in shop popup
    private void highlightShopPopupBubble(int positionToHighlight, View[] bubbleArray) {
        for (int i = 0; i < bubbleArray.length; i++) {
            if (i == positionToHighlight) {
                bubbleArray[i].setBackgroundResource(R.drawable.shop_circle_clicked);
            } else {
                bubbleArray[i].setBackgroundResource(R.drawable.shop_circle_unclick);
            }
        }
    }

    private void shop_changeItem(ImageView itemImageView, TextView itemTextView, TextView buyNowButton, TextView restorePurchase,
                                 List<SkuDetails> skuDetails, TextView shopPriceTextView) {
        switch (shopCurrentPosition) {
            case 1:
                if (Utils.getIfPurchasedNoAds(MainActivity.this)) {
                    // ad free version, therefore set sold out
                    itemImageView.setImageResource(R.drawable.noadsbackground_purchased);
                    itemTextView.setText(getString(R.string.purchased));
                    itemTextView.setTextColor(Color.parseColor("#a6a6a6"));
                    buyNowButton.setClickable(false);
                    buyNowButton.setBackgroundResource(R.drawable.shopbuynowbackground_cantclick);
                } else {
                    // haven buy yet
                    itemImageView.setImageResource(R.drawable.noadsbackground);
                    itemTextView.setText(getString(R.string.noads));
                    itemTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.bright_cyan));
                    buyNowButton.setClickable(true);
                    buyNowButton.setBackgroundResource(R.drawable.shop_buynow_selector);
                }
                restorePurchase.setVisibility(View.VISIBLE);
                if (skuDetails != null && skuDetails.size() >= 4) {
                    // just checking if skuDetails is not null. can use >0 or == 2 instead of >=4 doesnt matter, becos we expect 4 entries
                    shopPriceTextView.setText(skuDetails.get(0).priceText); // priceText includes the currency and the value, e.g £2.99, 3,99€, $4.99
                }
                break;
            case 2:
                itemImageView.setImageResource(R.drawable.buyjewel_50);
                itemTextView.setText(getString(R.string.fiftyjewels));
                itemTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.bright_cyan));
                buyNowButton.setClickable(true);
                buyNowButton.setBackgroundResource(R.drawable.shop_buynow_selector);
                restorePurchase.setVisibility(View.GONE);
                if (skuDetails != null && skuDetails.size() >= 4) {
                    // just checking if skuDetails is not null. can use >0 or == 2 instead of >=4 doesnt matter, becos we expect 4 entries
                    shopPriceTextView.setText(skuDetails.get(1).priceText); // priceText includes the currency and the value, e.g £2.99, 3,99€, $4.99
                }
                break;
            case 3:
                itemImageView.setImageResource(R.drawable.buyjewel_300);
                itemTextView.setText(getString(R.string.threehundredjewels));
                itemTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.bright_cyan));
                buyNowButton.setClickable(true);
                buyNowButton.setBackgroundResource(R.drawable.shop_buynow_selector);
                restorePurchase.setVisibility(View.GONE);
                if (skuDetails != null && skuDetails.size() >= 4) {
                    // just checking if skuDetails is not null. can use >0 or == 2 instead of >=4 doesnt matter, becos we expect 4 entries
                    shopPriceTextView.setText(skuDetails.get(2).priceText); // priceText includes the currency and the value, e.g £2.99, 3,99€, $4.99
                }
                break;
            case 4:
                itemImageView.setImageResource(R.drawable.buyjewel_900);
                itemTextView.setText(getString(R.string.ninehundredjewels));
                itemTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.bright_cyan));
                buyNowButton.setClickable(true);
                buyNowButton.setBackgroundResource(R.drawable.shop_buynow_selector);
                restorePurchase.setVisibility(View.GONE);
                if (skuDetails != null && skuDetails.size() >= 4) {
                    // just checking if skuDetails is not null. can use >0 or == 2 instead of >=4 doesnt matter, becos we expect 4 entries
                    shopPriceTextView.setText(skuDetails.get(3).priceText); // priceText includes the currency and the value, e.g £2.99, 3,99€, $4.99
                }
                break;
        }
    }

    private void openAchievementPopUpIfNotSignedIn() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.achievement_notsignedin_layout, null);
        builder.setView(view);

        final android.support.v7.app.AlertDialog achievementDialog = builder.create();
        Window dialogWindow = achievementDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
        }

        achievementDialog.setCancelable(true);
        achievementDialog.show();

        ImageView signInButton = view.findViewById(R.id.settings_linkaccountButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPoolManager.getInstance().playSound(0);
                startSignInIntent();
                achievementDialog.dismiss();
            }
        });
    }

    private void openSettingsPopUp() {
        final View view = getLayoutInflater().inflate(R.layout.settings_layout, null, false);

        final PopupWindow settingsPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        settingsPopupWindow.setTouchable(true);
        settingsPopupWindow.setFocusable(true);

        settingsPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        settingsPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SoundPoolManager.getInstance().playSound(2);
            }
        });
        final ImageView soundCheckbox = view.findViewById(R.id.settings_sound_checkboxImageView);
        final ConstraintLayout mainContainer = view.findViewById(R.id.settings_mainBackground);
        final EditText playerNameEditText = view.findViewById(R.id.settings_playerNameEditText);
        signInTextView = view.findViewById(R.id.settings_linkaccountTextView);

        if (signedIn) {
            signInTextView.setText(getString(R.string.signout));
        } else {
            signInTextView.setText(getString(R.string.linkaccount));
        }


        class SettingsClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.settings_mainBackground:
                        // the main container, clicking here will not dismiss the popup
                        // hence need this dummy click listener
                        if (settingsKeyboardOut) {
                            hideKeyboard(v);
                            view.requestFocus();
                        }
                        break;
                    case R.id.settings_overallLayout:
                        if (settingsKeyboardOut) {
                            hideKeyboard(v);
                        } else {
                            settingsPopupWindow.dismiss();
                        }
                        break;

                    case R.id.settings_sound_checkboxImageView:
                        if (Utils.getIfSoundIsMuted(MainActivity.this)) {
                            // mute -> unmute
                            Utils.setIfSoundIsMuted(MainActivity.this, false);
                            soundCheckbox.setImageResource(R.drawable.checkboxchecked);
                            MusicManager.playMediaPlayer();
                        } else {
                            // unmute -> mute
                            Utils.setIfSoundIsMuted(MainActivity.this, true);
                            soundCheckbox.setImageResource(R.drawable.checkboxuncheck);
                            MusicManager.pauseMediaPlayer();
                        }
                        break;

                    case R.id.settings_closeButton:
                        settingsPopupWindow.dismiss();
                        break;

                    case R.id.settings_privacypolicyTextView:
                        // set this flag to true since we want to retain background music, and onPause will not stop background music
                        MusicManager.isGoingNextActivity = true;
                        SoundPoolManager.getInstance().playSound(0);
                        startActivity(new Intent(MainActivity.this, PrivacyPolicy.class));
                        break;
                    case R.id.settings_faqTextView:
                        // set this flag to true since we want to retain background music, and onPause will not stop background music
                        MusicManager.isGoingNextActivity = true;
                        SoundPoolManager.getInstance().playSound(0);
                        startActivity(new Intent(MainActivity.this, FAQ.class));
                        break;
                    case R.id.settings_linkaccountButton:
                        SoundPoolManager.getInstance().playSound(0);
                        signedIn = !signedIn;
                        if (signedIn) {
                            startSignInIntent();
                        } else {
                            Log.d(TAG, "signOut()");

                            mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this,
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "signOut(): success");
                                            } else {
                                                handleException(task.getException(), "signOut() failed!");
                                            }

                                            onDisconnected();
                                        }
                                    });

                        }
                        break;
                }
            }
        }

        SettingsClickListener settingsClickListener = new SettingsClickListener();

        // set player name
        playerNameEditText.setText(Utils.getPlayerName(MainActivity.this));
        playerNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // no focus, hence hide keyboard
                    hideKeyboard(v);
                    playerNameEditText.setFocusableInTouchMode(true);
                } else {
                    view.setFocusableInTouchMode(true);
                    // edit text has focus, means window is open
                    settingsKeyboardOut = true;
                }
            }
        });
        playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Utils.setPlayerName(MainActivity.this, s.toString());
            }
        });

        mainContainer.setOnClickListener(settingsClickListener);

        playerNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    view.requestFocus(); // so that user press enter the edittext will lose focus
                    return true; //this is required to stop sending key event to parent
                }
                return false;
            }
        });

        // when clicked on anything outside
        view.setOnClickListener(settingsClickListener);
        view.findViewById(R.id.settings_linkaccountButton).setOnClickListener(settingsClickListener);

        if (Utils.getIfSoundIsMuted(MainActivity.this)) {
            soundCheckbox.setImageResource(R.drawable.checkboxuncheck);
        }
        soundCheckbox.setOnClickListener(settingsClickListener);

        view.findViewById(R.id.settings_closeButton).setOnClickListener(settingsClickListener);
        view.findViewById(R.id.settings_privacypolicyTextView).setOnClickListener(settingsClickListener);
        view.findViewById(R.id.settings_faqTextView).setOnClickListener(settingsClickListener);
    }

    private void hideKeyboard(View view) {
        view.setFocusableInTouchMode(false);
        settingsKeyboardOut = false;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homepage_jewelBuy:
                SoundPoolManager.getInstance().playSound(1);
                openShopPopup();
                break;
            case R.id.homepage_achievementButton:
                // set this flag to true since we want to retain background music, and onPause will not stop background music
                MusicManager.isGoingNextActivity = true;
                SoundPoolManager.getInstance().playSound(1);
                showAchievements();
                break;
            case R.id.homepage_setting_Button:
                SoundPoolManager.getInstance().playSound(1);
                openSettingsPopUp();
                break;
            case R.id.homepage_shop_Button:
                SoundPoolManager.getInstance().playSound(1);
                openShopPopup();
                break;
            case R.id.homepage_help_Button:
                // set this flag to true since we want to retain background music, and onPause will not stop background music
                MusicManager.isGoingNextActivity = true;
                SoundPoolManager.getInstance().playSound(1);
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void showAchievements() {
        if (mAchievementsClient != null) {
            mAchievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
        } else {
            openAchievementPopUpIfNotSignedIn();
        }
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            onAccountChanged(googleSignInAccount);
        }
        signedIn = true;
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);

        if (Utils.getIfJustSignedIn(MainActivity.this)) {
            loadFromSnapshot(null);
            Utils.setIfJustSignedIn(MainActivity.this, false);
        }

        if (signInTextView != null) {
            signInTextView.setText(getString(R.string.signout));
        }

        // check and update achievements
        Set<String> achievementSet = Utils.getAchievementsToUnlockLater(MainActivity.this);
        if (achievementSet.size() > 0) {
            for (String str : achievementSet) {
                mAchievementsClient.unlock(str);
            }
            // clear it
            Utils.clearAchievementsToUnlockLater(MainActivity.this);
        }
    }

    private void onAccountChanged(GoogleSignInAccount googleSignInAccount) {
        mSnapshotsClient = Games.getSnapshotsClient(this, googleSignInAccount);

        // Sign-in worked!
    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        mSnapshotsClient = null;
        signedIn = false;
        if (signInTextView != null) {
            signInTextView.setText(getString(R.string.linkaccount));
        }
        mAchievementsClient = null;
    }

    private boolean isSoundMuted() {
        return Utils.getIfSoundIsMuted(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();

        userCurrentJewel = Utils.getUserJewel(MainActivity.this);
        ((TextView) findViewById(R.id.homepage_jewelAmount)).setText("" + userCurrentJewel);

        if (signedIn && Utils.getIfJewelIsUpdated(MainActivity.this)) {
            saveSnapshot(null, getSavedGameStateInByteArray());
            Utils.setIfJewelIsUpdated(MainActivity.this, false);
        }

        if (!MusicManager.isGoingNextActivity) {
            // this is only called on first oncreate, or after user press home btn and back to the game agn, which means mediaplayer is paused and now needs to be played agn
            MusicManager.initializeAndPlayMediaPlayer(getApplicationContext(), isSoundMuted());
        }

        // we have to reinstatiate this flag to false agn in case user set it to true when they going to next activity. If user set it to true when going next activity such as settings,
        // on pause stop music player will not be called. But when that activity pops off the stack, we do not want onResume here to initializeMediaPlayer again (the previous line).
        // hence since flag is still true, we did not reinitialize. But we have to set it to false agn so that on user home press it is reinitialize.
        MusicManager.isGoingNextActivity = false;
    }

    @Override
    protected void onPause() {
        // when we go to the game mode activity, current activity will be onPause.
        // this also takes into account user press home button or take phone call e.g, transitting away from the app,
        // hence we stop all background sound
        super.onPause();

        // we do not set isGoingNextActivity to true when user start game, hence this method will be called and the media player is auto close.
        // we only set this field to true when user go to pages like settings and findoutmore insane where we still want to continue background music
        if (!MusicManager.isGoingNextActivity) {
            // it is not activated by going next activity, i.e. this call here is activated by using pressing home or similar
            // hence we stop media player
            MusicManager.stopMediaPlayer();
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * You can capture the Snapshot selection intent in the onActivityResult method. The result
     * either indicates a new Snapshot was created (EXTRA_SNAPSHOT_NEW) or was selected.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
                Utils.setIfJustSignedIn(MainActivity.this, true);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        } else if (requestCode == RC_LIST_SAVED_GAMES) {
            // the standard snapshot selection intent
            if (intent != null) {
                if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                    // Load a snapshot.
                    SnapshotMetadata snapshotMetadata =
                            intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                    currentSaveName = snapshotMetadata.getUniqueName();
                    loadFromSnapshot(snapshotMetadata);
                } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                    // Create a new snapshot named with a unique string
                    String unique = Long.toString(System.currentTimeMillis());
                    currentSaveName = "snapshotTemp-" + unique;
                    saveSnapshot(null, getSavedGameStateInByteArray());
                }
            }
        }

        if (!bp.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }


    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();

    }

    /**
     * Prepares saving Snapshot to the user's synchronized storage, conditionally resolves errors,
     * and stores the Snapshot.
     */
    void saveSnapshot(final SnapshotMetadata snapshotMetadata, final byte[] bytesToWrite) {
        waitForClosedAndOpen(snapshotMetadata)
                .addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                    @Override
                    public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                        SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
                        Snapshot snapshotToWrite = result.getData();

                        if (snapshotToWrite == null) {
                            // No snapshot available yet; waiting on the user to choose one.
                            return;
                        }

                        Log.d(TAG, "Writing data to snapshot: " + snapshotToWrite.getMetadata().getUniqueName());
                        writeSnapshot(snapshotToWrite, bytesToWrite)
                                .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
                                    @Override
                                    public void onComplete(@NonNull Task<SnapshotMetadata> task) {
                                        if (task.isSuccessful()) {
                                            Log.i(TAG, "Snapshot saved!");
                                            postSaveSnapshot();
                                        } else {
                                            handleException(task.getException(), getString(R.string.write_snapshot_error));
                                        }
                                    }
                                });
                    }
                });
    }

    private String currentSaveName = "snapshotTemp";

    private Task<SnapshotsClient.DataOrConflict<Snapshot>> waitForClosedAndOpen(final SnapshotMetadata snapshotMetadata) {

        final boolean useMetadata = snapshotMetadata != null && snapshotMetadata.getUniqueName() != null;
        if (useMetadata) {
            Log.i(TAG, "Opening snapshot using metadata: " + snapshotMetadata);
        } else {
            Log.i(TAG, "Opening snapshot using currentSaveName: " + currentSaveName);
        }

        final String filename = useMetadata ? snapshotMetadata.getUniqueName() : currentSaveName;

        return SnapshotCoordinator.getInstance()
                .waitForClosed(filename)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "There was a problem waiting for the file to close!");
                    }
                })
                .continueWithTask(new Continuation<Result, Task<SnapshotsClient.DataOrConflict<Snapshot>>>() {
                    @Override
                    public Task<SnapshotsClient.DataOrConflict<Snapshot>> then(@NonNull Task<Result> task) throws Exception {
                        Task<SnapshotsClient.DataOrConflict<Snapshot>> openTask = useMetadata
                                ? SnapshotCoordinator.getInstance().open(mSnapshotsClient, snapshotMetadata)
                                : SnapshotCoordinator.getInstance().open(mSnapshotsClient, filename, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);
                        return openTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                handleException(e,
                                        useMetadata
                                                ? getString(R.string.error_opening_metadata)
                                                : getString(R.string.error_opening_filename)
                                );
                            }
                        });
                    }
                });
    }


    /**
     * Generates metadata, takes a screenshot, and performs the write operation for saving a
     * snapshot.
     */
    private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot, byte[] bytes) {
        // Set the data payload for the snapshot.
        snapshot.getSnapshotContents().writeBytes(bytes);

        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .build();
        return SnapshotCoordinator.getInstance().commitAndClose(mSnapshotsClient, snapshot, metadataChange);
    }

    /**
     * Loads a Snapshot from the user's synchronized storage.
     */
    void loadFromSnapshot(final SnapshotMetadata snapshotMetadata) {
        //TODO progressdialog here
        waitForClosedAndOpen(snapshotMetadata)
                .addOnSuccessListener(new OnSuccessListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                    @Override
                    public void onSuccess(SnapshotsClient.DataOrConflict<Snapshot> result) {

                        Snapshot snapshot = result.getData();

                        if (snapshot == null) {
                            Log.w(TAG, "Conflict was not resolved automatically, waiting for user to resolve.");
                        } else {
                            try {
                                readSavedGame(snapshot);
                                Log.i(TAG, "Snapshot loaded.");
                            } catch (IOException e) {
                                Log.e(TAG, "Error while reading snapshot contents: " + e.getMessage());
                            }
                        }

                        SnapshotCoordinator.getInstance().discardAndClose(mSnapshotsClient, snapshot)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        handleException(e, "There was a problem discarding the snapshot!");
                                    }
                                });

                        //TODO
//                        hideAlertBar();
                    }
                });
    }

    private void readSavedGame(Snapshot snapshot) throws IOException {
        Log.e("gggg", "reading saved game");
        savedGameState = getSavedGameStateAsObject(snapshot.getSnapshotContents().readFully());

        updateJewelAmount();

        HashSet<String> unlockedPack_easy = savedGameState.getUnlocked_easy();
        HashSet<String> unlockedPack_medium = savedGameState.getUnlocked_medium();
        HashSet<String> unlockedPack_hard = savedGameState.getUnlocked_hard();
        HashSet<String> unlockedPack_insane = savedGameState.getUnlocked_insane();

        for (String string : unlockedPack_easy) {
            Utils.unlockPack_Easy(MainActivity.this, string);
        }

        for (String string : unlockedPack_medium) {
            Utils.unlockPack_Medium(MainActivity.this, string);
        }

        for (String string : unlockedPack_hard) {
            Utils.unlockPack_Hard(MainActivity.this, string);
        }

        for (String string : unlockedPack_insane) {
            Utils.unlockPack_Insane(MainActivity.this, string);
        }
    }

    private void updateJewelAmount() {
        TextView jewelTextView = findViewById(R.id.homepage_jewelAmount);
        int amount = getJewelsInInt();

        userCurrentJewel = amount;

        Log.e("gggg", "amount: " + amount);
        Utils.setUserJewel(MainActivity.this, amount);
        jewelTextView.setText("" + amount);
    }

    // add the jewels earned to user account
    private void addUserJewels(int amount) {
        int oldAmount = Utils.getUserJewel(MainActivity.this);
        int newAmount = oldAmount + amount;
        Utils.setUserJewel(MainActivity.this, newAmount);
        Utils.setIfJewelIsUpdated(MainActivity.this, true);
        TextView jewelTextView = findViewById(R.id.homepage_jewelAmount);
        jewelTextView.setText("" + newAmount);
    }

    private byte[] getSavedGameStateInByteArray() {
        SavedGameState savedGameState = new SavedGameState(MainActivity.this);

        try {
            byte[] result = BytesUtil.toByteArray(savedGameState);
            return result;
        } catch (Exception e) {
        }
        return null;
    }

    private SavedGameState getSavedGameStateAsObject(byte[] data) {
        try {
            return ((SavedGameState) BytesUtil.toObject(data));
        } catch (Exception e) {
        }
        return null;
    }

    private int getJewelsInInt() {
        if (savedGameState == null) {
            savedGameState = new SavedGameState(MainActivity.this);
        }

        int amountOfJewel = savedGameState.getAmountOfJewel();
        return amountOfJewel;
    }

    private void postSaveSnapshot() {
        Utils.setUserJewel(MainActivity.this, userCurrentJewel);
        TextView jewelTextView = findViewById(R.id.homepage_jewelAmount);
        jewelTextView.setText("" + userCurrentJewel);
    }
}
