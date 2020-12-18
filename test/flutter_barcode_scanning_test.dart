import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_barcode_scanning/flutter_barcode_scanning.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_barcode_scanning');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterBarcodeScanning.platformVersion, '42');
  });
}
