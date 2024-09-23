import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class CNative extends StatefulWidget {
  @override
  State<CNative> createState() => _CNativeState();
}

class _CNativeState extends State<CNative> {
  var channel = const MethodChannel('focal');
  String batteryLevel = 'Unknown battery level';

  Future<void> callNativeCode() async {
    try {
      final int newBattery = await channel.invokeMethod('getBattery');
      setState(() {
        batteryLevel = 'Battery level: $newBattery%';
      });
    } on PlatformException catch (e) {
      setState(() {
        batteryLevel = "Error: '${e.message}'";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Native Battery Level'),
        centerTitle: true,
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(batteryLevel),
          ElevatedButton(
            onPressed: callNativeCode,
            child: Text('Get Battery Level'),
          ),
        ],
      ),
    );
  }
}
