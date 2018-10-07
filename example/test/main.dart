// This is a basic Flutter widget test.
// To perform an interaction with a widget in your test, use the WidgetTester utility that Flutter
// provides. For example, you can send tap and scroll gestures. You can also use WidgetTester to
// find child widgets in the widget tree, read text, and verify that the values of widget properties
// are correct.

import 'package:flutter/material.dart';
import 'package:faceauth/faceauth.dart';

void main() {
  runApp(new MyApp());
}

class MyApp extends StatefulWidget{
  @override
  State<StatefulWidget> createState() {
    return new MyAppState();
  }
}

class MyAppState extends State<MyApp>{
  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: new AppBar(
        title: new Text('Example'),
      ),
      body: new Center(
        child: new Row(
          children: <Widget>[
            new MaterialButton(
              child: new Text('Get lip-language'),
              onPressed: (){
                Faceauth.initSdk('1257084581', 'AKIDc7Tp5BEnLhu00tnAjJ0uz6Og8TfhhUEn', 'test').then((result){
                  print('initSdk result:'+result.toString());
                });
              }
            )
          ],
        ),
      ),
    );
  }
}
