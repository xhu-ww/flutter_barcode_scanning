import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class BarcodeScanningWidget extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _BarcodeScanningWidget();
}

class _BarcodeScanningWidget extends State<BarcodeScanningWidget> {
  final String viewType = 'flutter_barcode_scanning';

  @override
  Widget build(BuildContext context) {
    return PlatformViewLink(
      surfaceFactory:
          (BuildContext context, PlatformViewController controller) {
        return AndroidViewSurface(
          controller: controller,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      viewType: 'flutter_barcode_scanning',
      onCreatePlatformView: (PlatformViewCreationParams params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: <String, dynamic>{},
          creationParamsCodec: StandardMessageCodec(),
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}

class FlutterBarcodeScanning {
  static const MethodChannel _channel =
      const MethodChannel('flutter_barcode_scanning');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

class BarcodeScanningController {

}

class BarcodeScanningPreview extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    throw UnimplementedError();
  }
}
