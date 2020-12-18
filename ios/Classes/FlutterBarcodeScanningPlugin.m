#import "FlutterBarcodeScanningPlugin.h"
#if __has_include(<flutter_barcode_scanning/flutter_barcode_scanning-Swift.h>)
#import <flutter_barcode_scanning/flutter_barcode_scanning-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_barcode_scanning-Swift.h"
#endif

@implementation FlutterBarcodeScanningPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterBarcodeScanningPlugin registerWithRegistrar:registrar];
}
@end
