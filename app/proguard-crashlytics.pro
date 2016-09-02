# Crashlytics 1.+

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable,Annotation
-keep public class * extends java.lang.Exception