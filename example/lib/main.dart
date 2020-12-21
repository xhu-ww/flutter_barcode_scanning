import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter_barcode_scanning/flutter_barcode_scanning.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  BarcodeScanningController _controller = BarcodeScanningController();

  @override
  void initState() {
    super.initState();
    _controller.initialize().then((value) {
      setState(() {});
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Stack(
          fit: StackFit.expand,
          children: [
            BarcodeScanningPreview(_controller),
          ],
        ),
      ),
    );
  }
}
