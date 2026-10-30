# R8 already keeps all manifest-declared components (activities, receivers,
# services). These explicit keeps make that guarantee obvious to an auditor.
-keep class com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver { *; }
-keep class com.solerunner26.lockscreenwidget.LockWidgetProvider { *; }
-keep class com.solerunner26.lockscreenwidget.LockTileService { *; }
-keep class com.solerunner26.lockscreenwidget.LockNowActivity { *; }
-keep class com.solerunner26.lockscreenwidget.SettingsActivity { *; }
