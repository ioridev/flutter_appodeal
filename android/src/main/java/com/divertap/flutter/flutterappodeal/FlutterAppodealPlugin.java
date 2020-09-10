package com.divertap.flutter.flutterappodeal;

import android.app.Activity;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.UserSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterAppodealPlugin
 */
public class FlutterAppodealPlugin implements MethodCallHandler, RewardedVideoCallbacks {
    private final Registrar registrar;
    private final MethodChannel channel;

    public FlutterAppodealPlugin(Registrar registrar, MethodChannel channel) {
        this.registrar = registrar;
        this.channel = channel;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_appodeal");
        FlutterAppodealPlugin plugin = new FlutterAppodealPlugin(registrar, channel);
        channel.setMethodCallHandler(plugin);
        Appodeal.setRewardedVideoCallbacks(plugin);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        Activity activity = registrar.activity();

        if (activity == null) {
            result.error("no_activity", "flutler_appodeal plugin requires a foreground activity", null);
            return;
        }

        if (call.method.equals("setUserIdData")) {
            String userId = call.argument("userId");
            Appodeal.setUserId(userId);
            result.success(Boolean.TRUE);
        } if (call.method.equals("setUserFullData")) {
            String userId = call.argument("userId");
            int age = call.argument("age");
            int genderIndex = call.argument("gender");
            Appodeal.setUserId(userId);
            Appodeal.setUserAge(age);
            switch (genderIndex) {
                case 0:
                    Appodeal.setUserGender(UserSettings.Gender.MALE);
                case 1:
                    Appodeal.setUserGender(UserSettings.Gender.FEMALE);
                case 2:
                    Appodeal.setUserGender(UserSettings.Gender.OTHER);
            }
            result.success(Boolean.TRUE);
        } else if (call.method.equals("initialize")) {
            String appKey = call.argument("appKey");
            List<Integer> types = call.argument("types");
            Boolean hasConsent = call.argument("hasConsent");
            int type = Appodeal.NONE;
            for (int type2 : types) {
                type = type | this.appodealAdType(type2);
            }
            Appodeal.initialize(activity, appKey, type, hasConsent);
            result.success(Boolean.TRUE);
        } else if (call.method.equals("showInterstitial")) {
            Appodeal.show(activity, Appodeal.INTERSTITIAL);
            result.success(Boolean.TRUE);
        } else if (call.method.equals("showRewardedVideo")) {
            String placement = call.argument("placement");
            Appodeal.show(activity, Appodeal.REWARDED_VIDEO, placement);
            result.success(Boolean.TRUE);
        } else if (call.method.equals("isLoaded")) {
            int type = call.argument("type");
            int adType = this.appodealAdType(type);
            result.success(Appodeal.isLoaded(adType));
        } else {
            result.notImplemented();
        }
    }

    private int appodealAdType(int innerType) {
        switch (innerType) {
            case 0:
                return Appodeal.INTERSTITIAL;
            case 1:
                return Appodeal.NON_SKIPPABLE_VIDEO;
            case 2:
                return Appodeal.BANNER;
            case 3:
                return Appodeal.NATIVE;
            case 4:
                return Appodeal.REWARDED_VIDEO;
            case 5:
                return Appodeal.MREC;
            case 6:
                return Appodeal.NON_SKIPPABLE_VIDEO;
        }
        return Appodeal.INTERSTITIAL;
    }

    private Map<String, Object> argumentsMap(Object... args) {
        Map<String, Object> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) arguments.put(args[i].toString(), args[i + 1]);
        return arguments;
    }

    // Appodeal Rewarded Video Callbacks
    @Override
    public void onRewardedVideoLoaded(boolean b) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("precache", b);
        channel.invokeMethod("rewardedVideoDidLoadAdIsPrecache", arguments);
    }
    
    @Override
    public void onRewardedVideoFailedToLoad() {
        channel.invokeMethod("onRewardedVideoFailedToLoad", argumentsMap());
    }
    
    @Override
    public void onRewardedVideoShown() {
        channel.invokeMethod("onRewardedVideoPresent", argumentsMap());
    }
    
    @Override
    public void onRewardedVideoShowFailed() {
        channel.invokeMethod("onRewardedVideoDidFailToPresentWithError", argumentsMap());
    }
    
    @Override
    public void onRewardedVideoClicked() {
        channel.invokeMethod("onRewardedVideoPresent", argumentsMap());
    }
    
    @Override
    public void onRewardedVideoClosed(boolean b) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("wasFullyWatched", b);
        channel.invokeMethod("rewardedVideoWillDismissAndWasFullyWatched", arguments);
    }
    
    @Override
    public void onRewardedVideoFinished(double amount, String s) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("rewardAmount",amount);
        arguments.put("rewardType",s);
        channel.invokeMethod("onRewardedVideoFinished", arguments);
    }

    @Override
    public void onRewardedVideoExpired() {
        channel.invokeMethod("onRewardedVideoExpired", argumentsMap());
    }
} 