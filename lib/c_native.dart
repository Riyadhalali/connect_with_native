import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class CNative extends StatefulWidget {
  @override
  State<CNative> createState() => _CNativeState();
}

class _CNativeState extends State<CNative> {
  static const MethodChannel channel = MethodChannel('focal');
  String batteryLevel = 'Unknown battery level';
  List<Map<String, String>> callLogs = [];

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

  Future<void> fetchCallLogs() async {
    try {
      final List<dynamic> logs = await channel.invokeMethod('getCallLogs');
      if (logs != null) {
        // Ensure logs is not null
        setState(() {
          callLogs = List<Map<String, String>>.from(
              logs.map((log) => Map<String, String>.from(log)));
        });
      } else {
        print("No call logs found.");
      }
    } on PlatformException catch (e) {
      print("Error fetching call logs: '${e.message}'");
    } catch (e) {
      print("Unexpected error: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Native Battery Level & Call Logs'),
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
          ElevatedButton(
            onPressed: fetchCallLogs,
            child: Text('Get Call Logs'),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: callLogs.length,
              itemBuilder: (context, index) {
                final log = callLogs[index];
                return ListTile(
                  title: Text("Number: ${log['number']}"),
                  subtitle: Text(
                      "Type: ${log['type']}, Duration: ${log['duration']} seconds"),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
