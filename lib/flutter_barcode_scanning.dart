import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

final MethodChannel _channel =
    const MethodChannel('plugins.flutter.io/barcodeScanning');

class BarcodeScanningController {
  bool isInitialized;
  int _textureId;

  Future<void> initialize() async {
    final Map<String, dynamic> reply =
        await _channel.invokeMapMethod<String, dynamic>('initialize');
    _textureId = reply['textureId'];
    isInitialized = true;
  }
}

class BarcodeScanningPreview extends StatelessWidget {
  const BarcodeScanningPreview(this.controller);

  final BarcodeScanningController controller;

  @override
  Widget build(BuildContext context) {
    return controller.isInitialized
        ? Texture(textureId: controller._textureId)
        : Container();
  }
}
