#import "AndroidWebviewPlugin.h"
#import <android_webview/android_webview-Swift.h>

@implementation AndroidWebviewPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAndroidWebviewPlugin registerWithRegistrar:registrar];
}
@end
