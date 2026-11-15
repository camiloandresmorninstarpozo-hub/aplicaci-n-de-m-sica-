#!/bin/bash
set -e
mkdir -p app/src/main/java/com/jk/music
cat > settings.gradle.kts <<'EOF'
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google(); mavenCentral() } }
rootProject.name = "JKMUSICK"
include(":app")
EOF
cat > build.gradle.kts <<'EOF'
plugins {
  id("com.android.application") version "8.5.2" apply false
  id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
EOF
cat > gradle.properties <<'EOF'
android.useAndroidX=true
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8
kotlin.compiler.execution.strategy=in-process
EOF
cat > app/build.gradle.kts <<'EOF'
plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
  namespace = "com.jk.music"
  compileSdk = 34
  defaultConfig { applicationId = "com.jk.music"; minSdk = 24; targetSdk = 34; versionCode = 2; versionName = "2.0" }
  compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
  kotlinOptions { jvmTarget = "17" }
  signingConfigs { create("fixed") { storeFile = file("fixed.p12"); storeType = "pkcs12"; storePassword = "jkmusick"; keyAlias = "jk"; keyPassword = "jkmusick" } }
  buildTypes { getByName("debug") { signingConfig = signingConfigs.getByName("fixed") } }
}
dependencies {
  implementation("androidx.media3:media3-exoplayer:1.3.1")
  implementation("androidx.media3:media3-session:1.3.1")
  implementation("androidx.documentfile:documentfile:1.0.1")
  implementation("androidx.activity:activity-ktx:1.9.0")
  implementation("androidx.core:core-ktx:1.13.1")
}
EOF
cat > app/src/main/AndroidManifest.xml <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
  <uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>
  <uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>
  <uses-permission android:name="android.permission.WAKE_LOCK"/>
  <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
  <application android:label="JKMUSICK" android:icon="@drawable/ic_launcher" android:allowBackup="true" android:theme="@android:style/Theme.Material.NoActionBar">
    <activity android:name=".MainActivity" android:exported="true" android:configChanges="orientation|screenSize|keyboardHidden">
      <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
      </intent-filter>
    </activity>
    <service android:name=".PlayService" android:exported="true" android:foregroundServiceType="mediaPlayback">
      <intent-filter><action android:name="androidx.media3.session.MediaSessionService"/></intent-filter>
    </service>
  </application>
</manifest>
EOF
cp PlayService.kt app/src/main/java/com/jk/music/PlayService.kt
cp MainActivity.kt app/src/main/java/com/jk/music/MainActivity.kt

# ---- Ícono de la app: CD con destello holográfico sobre azul índigo y ondas de audio ----
mkdir -p app/src/main/res/drawable app/src/main/res/drawable-anydpi-v26
HDR='<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">'
cat > /tmp/ic_bg.xml <<'EOF'
<path android:pathData="M0,0h108v108h-108z">
  <aapt:attr name="android:fillColor">
    <gradient android:type="linear" android:startX="0" android:startY="0" android:endX="108" android:endY="108">
      <item android:color="#FF3B2BB5" android:offset="0.0"/>
      <item android:color="#FF2F2298" android:offset="0.5"/>
      <item android:color="#FF150B55" android:offset="1.0"/>
    </gradient>
  </aapt:attr>
</path>
EOF
cat > /tmp/ic_fg.xml <<'EOF'
<path android:pathData="M54,54m-31,0a31,31 0 1,0 62,0a31,31 0 1,0 -62,0">
  <aapt:attr name="android:fillColor">
    <gradient android:type="sweep" android:centerX="54" android:centerY="54">
      <item android:color="#FFEDEEF5" android:offset="0.0"/>
      <item android:color="#FFFFF1A6" android:offset="0.12"/>
      <item android:color="#FFD5D8E4" android:offset="0.25"/>
      <item android:color="#FF9EDCFF" android:offset="0.38"/>
      <item android:color="#FFE4E6EF" android:offset="0.5"/>
      <item android:color="#FFF3B3FF" android:offset="0.62"/>
      <item android:color="#FFD9DCE8" android:offset="0.75"/>
      <item android:color="#FFA8F0D0" android:offset="0.88"/>
      <item android:color="#FFEDEEF5" android:offset="1.0"/>
    </gradient>
  </aapt:attr>
</path>
<path android:pathData="M54,54m-31,0a31,31 0 1,0 62,0a31,31 0 1,0 -62,0" android:strokeColor="#66101040" android:strokeWidth="1.4"/>
<path android:pathData="M54,54m-17,0a17,17 0 1,0 34,0a17,17 0 1,0 -34,0" android:strokeColor="#33FFFFFF" android:strokeWidth="1"/>
<path android:pathData="M54,54m-11,0a11,11 0 1,0 22,0a11,11 0 1,0 -22,0" android:fillColor="#FF2F2298" android:strokeColor="#FFC3CCFF" android:strokeWidth="1.2"/>
<path android:pathData="M54,54m-3.6,0a3.6,3.6 0 1,0 7.2,0a3.6,3.6 0 1,0 -7.2,0" android:fillColor="#FF1B1170"/>
<path android:pathData="M29,49L29,59M34,45L34,63M39,41L39,67M79,49L79,59M74,45L74,63M69,41L69,67" android:strokeColor="#D92F2298" android:strokeWidth="3" android:strokeLineCap="round"/>
EOF
{ echo "$HDR"; cat /tmp/ic_bg.xml; echo '</vector>'; } > app/src/main/res/drawable/ic_bg.xml
{ echo "$HDR"; cat /tmp/ic_fg.xml; echo '</vector>'; } > app/src/main/res/drawable/ic_fg.xml
{ echo "$HDR"; cat /tmp/ic_bg.xml /tmp/ic_fg.xml; echo '</vector>'; } > app/src/main/res/drawable/ic_launcher.xml
cat > app/src/main/res/drawable-anydpi-v26/ic_launcher.xml <<'EOF'
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/ic_bg"/><foreground android:drawable="@drawable/ic_fg"/></adaptive-icon>
EOF

base64 -d ks.b64 > app/fixed.p12
