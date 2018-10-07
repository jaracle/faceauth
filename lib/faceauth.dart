import 'dart:async';

import 'package:flutter/services.dart';

class Faceauth {
  static const MethodChannel _channel =
      const MethodChannel('faceauth');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> initSdk(String appId,String secretId,String secretKey) async {
    final bool result = await _channel.invokeMethod('initSdk',[appId,secretId,secretKey]);
    return result;
  }
}
