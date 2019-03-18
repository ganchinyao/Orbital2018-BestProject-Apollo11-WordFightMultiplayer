# Orbital2018-BestProject-Apollo11-WordFightMultiplayer
NUS School of Computing Orbital Best Project (Apollo 11, Advanced Level) 2018.

#### Splashdown Poster:

<img src="/promotional/splashdown.jpg" height="700" />

#### Milestone 3 video:

[<img src="/promotional/youtubevideo.png" height="300" />](https://youtu.be/Ns_xHE7bVUA)

## About
* NUS School of Computing Orbital Programme is a self-directed, independent project work for freshmen to participate during the Summer holiday from May - August each year. For more information about the programme, visit https://orbital.comp.nus.edu.sg/
* Word Fight Multiplayer Game was an Orbital Project done by myself and my team mate Lim Wai Lun.
* This is a fully functional Android Real-time Multiplayer Word Game. Check it out at https://play.google.com/store/apps/details?id=com.ganwl.multiplayerwordgame&hl=en
* This project won the **Best Project (Apollo 11, Advanced Level)** out of 400+ participants in the Orbital Programme 2018.

## Statistic
* This app was completed from scratch in about 2 weeks.
* Total lines of Java code written, including comments, excluding libraries count: ~10k
* Total lines of XML code written, including comments, excluding libraries count: ~7k
* Libraries used: [Google Play Game Services Sign-in](https://developers.google.com/games/services/android/signin),
[Google Real-time Multiplayer](https://developers.google.com/games/services/android/realtimeMultiplayer),
[Google Achievements](https://developers.google.com/games/services/android/achievements),
[Google Saved Games](https://developers.google.com/games/services/android/savedgames),
[Google Admob](https://developers.google.com/admob/android/quick-start),
[Facebook Audience Network](https://developers.google.com/admob/android/quick-start),
[intuit/sdp](https://github.com/intuit/sdp),
[intuit/ssp](https://github.com/intuit/ssp),
[daimajia/AndroidViewAnimations](https://github.com/daimajia/AndroidViewAnimations),
[AppIntro/AppIntro](https://github.com/AppIntro/AppIntro),
[anjlab/android-inapp-billing-v3](https://github.com/anjlab/android-inapp-billing-v3),
[cachapa/ExpandableLayout](https://github.com/cachapa/ExpandableLayout)

## Milestone 3
* [Milestone 3 documentation](/promotional/Orbital2018_Milestone3_GanChinYao_LimWaiLun.pdf)
* [Milestone 3 video](https://youtu.be/Ns_xHE7bVUA)

## How to use
* Navigate to `Constants.java` in `helper` package. Follow all the `TODO`
* For Facebook Audience Network Ads to work properly: Update `homepageNativeAdId`, `pauseNativeAdId`, 
`pauseNativeAdId_Multiplayer`, `endGameNativeAdId_Singleplayer` in `Constants.java` to your own Placement_ID created from Facebook Audience Network.
* For In-App-Purchase to work properly: Update `noAdsSKU`, `oneHundredJewelSKU`, `threeHundredJewelSKU`, `nineHundredJewelSKU`, `In_APP_PURCHASE_YOUR_KEY_FROM_GOOGLE_CONSOLE` in `Constants.java` to your own SKU id and license key from Google Play Console.
* For Achievements to work properly: Update all the `TODO` in `games-ids.xml` from your Google Play Console Achievements Page.
* For game to launch without crashing: Update `app_id` in `games-ids.xml` from your Google Play Console

## Note
* This app will NOT be updated anymore
