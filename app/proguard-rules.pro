# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Jan\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class org.jsoup.** {*;}
-keep interface org.jsoup.** { *; }
-keep public class org.jsoup.** { *; }

-keep class org.apache.** {*;}
-keep interface org.apache.** { *; }
-keep public class org.apache.** { *; }

-keep class android.** { *; }
-keep interface android.** { *; }
-keep public class android.** { *; }

-keep class com.google.** { *; }
-keep interface com.google.** { *; }
-keep public class com.google.** { *; }