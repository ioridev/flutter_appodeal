#import "FlutterAppodealPlugin.h"

@interface FlutterAppodealPlugin(){
    FlutterMethodChannel* channel;
}
@end

@implementation FlutterAppodealPlugin

+ (UIViewController *)rootViewController {
    return [UIApplication sharedApplication].delegate.window.rootViewController;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_appodeal"
            binaryMessenger:[registrar messenger]];
    FlutterAppodealPlugin* instance = [[FlutterAppodealPlugin alloc] init];
    [instance setChannel:channel];
    [Appodeal setRewardedVideoDelegate:instance];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void) setChannel:(FlutterMethodChannel*) chan{
    channel = chan;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"setUserIdData" isEqualToString:call.method]) {
      NSString* userId = call.arguments[@"userId"];
      [Appodeal setUserId:userId];
      result([NSNumber numberWithBool:YES]);
  }else if ([@"setUserFullData" isEqualToString:call.method]){
      NSString* userId = call.arguments[@"userId"];
      NSUInteger age = [call.arguments[@"age"] longValue];
      NSUInteger genderIndex = [call.arguments[@"gender"] longValue];
      [Appodeal setUserId:userId];
      [Appodeal setUserAge:age];
      switch (genderIndex) {
        case 0:
            [Appodeal setUserGender:AppodealUserGenderMale];
            break;
        case 1:
            [Appodeal setUserGender:AppodealUserGenderFemale];
            break;
        case 2:
            [Appodeal setUserGender:AppodealUserGenderOther];
            break;
        default:
            break;
      }
      result([NSNumber numberWithBool:YES]);
  }else if ([@"initialize" isEqualToString:call.method]) {
      NSString* appKey = call.arguments[@"appKey"];
      NSArray* types = call.arguments[@"types"];
      NSNumber* hasConsent = call.arguments[@"hasConsent"];
      AppodealAdType type = types.count > 0 ? [self typeFromParameter:types.firstObject] : AppodealAdTypeInterstitial;
      int i = 1;
      while (i < types.count) {
          type = type | [self typeFromParameter:types[i]];
          i++;
      }
      [Appodeal initializeWithApiKey:appKey types:type hasConsent: [hasConsent boolValue]];
      result([NSNumber numberWithBool:YES]);
  }else if ([@"showInterstitial" isEqualToString:call.method]) {
      [Appodeal showAd:AppodealShowStyleInterstitial rootViewController:[FlutterAppodealPlugin rootViewController]];
      result([NSNumber numberWithBool:YES]);
  }else if ([@"showRewardedVideo" isEqualToString:call.method]) {
      NSString* placement = call.arguments[@"placement"];
      if ([Appodeal isInitalizedForAdType:AppodealAdTypeRewardedVideo] && [Appodeal canShow:AppodealAdTypeRewardedVideo forPlacement:placement]) {
            [Appodeal showAd:AppodealShowStyleRewardedVideo forPlacement:placement rootViewController:[FlutterAppodealPlugin rootViewController]];
      }
      result([NSNumber numberWithBool:YES]);
  }else if ([@"isLoaded" isEqualToString:call.method]) {
      NSNumber *type = call.arguments[@"type"];
      result([NSNumber numberWithBool:[Appodeal isReadyForShowWithStyle:[self showStyleFromParameter:type]]]);
  }else {
    result(FlutterMethodNotImplemented);
  }
}

- (AppodealAdType) typeFromParameter:(NSNumber*) parameter{
    switch ([parameter intValue]) {
        case 4:
            return AppodealAdTypeRewardedVideo;
        default:
            break;
    }
    return AppodealAdTypeInterstitial;
}

- (AppodealShowStyle) showStyleFromParameter:(NSNumber*) parameter{
    switch ([parameter intValue]) {
        case 4:
            return AppodealShowStyleRewardedVideo;
        default:
            break;
    }
    return AppodealShowStyleInterstitial;
}

#pragma mark - RewardedVideo Delegate

- (void)rewardedVideoDidLoadAdIsPrecache:(BOOL)precache {
    [channel invokeMethod:@"rewardedVideoDidLoadAdIsPrecache" arguments:@{@"precache":@(precache)}];
}

- (void)rewardedVideoDidFailToLoadAd {
    [channel invokeMethod:@"onRewardedVideoFailedToLoad" arguments:nil];
}

- (void)rewardedVideoDidFailToPresentWithError:(NSError *)error {
    [channel invokeMethod:@"onRewardedVideoDidFailToPresentWithError" arguments:@{@"error":error}];
}


- (void)rewardedVideoDidPresent {
    [channel invokeMethod:@"onRewardedVideoPresent" arguments:nil];
}

- (void)rewardedVideoWillDismissAndWasFullyWatched:(BOOL)wasFullyWatched {
    [channel invokeMethod:@"rewardedVideoWillDismissAndWasFullyWatched" arguments:@{@"wasFullyWatched":@(wasFullyWatched)}];
}

- (void)rewardedVideoDidFinish:(float)rewardAmount name:(NSString *)rewardName {
    NSDictionary *params =  @{ @"rewardAmount" : @(rewardAmount),
                                @"rewardType" : rewardName};
                                                 
   [channel invokeMethod:@"onRewardedVideoFinished" arguments: params];
}

- (void)rewardedVideoDidExpired {
    [channel invokeMethod:@"onRewardedVideoExpired" arguments:nil];
}

@end
