plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.nfcreader.universal"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.nfcreader.universal"
    minSdk = 24
    targetSdk = 34
    versionCode = 2
    versionName = "neko-chan-1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Release signing: used only when all four values are provided (env vars or Gradle
  // properties). Otherwise the release build falls back to the debug key.
  fun releaseProp(name: String): String? =
    System.getenv(name) ?: (project.findProperty(name) as String?)
  val relStoreFile = releaseProp("RELEASE_STORE_FILE")
  val relStorePassword = releaseProp("RELEASE_STORE_PASSWORD")
  val relKeyAlias = releaseProp("RELEASE_KEY_ALIAS")
  val relKeyPassword = releaseProp("RELEASE_KEY_PASSWORD")
  val hasReleaseSigning = listOf(relStoreFile, relStorePassword, relKeyAlias, relKeyPassword)
    .all { !it.isNullOrBlank() } && file(relStoreFile!!).exists()

  signingConfigs {
    if (hasReleaseSigning) {
      create("release") {
        storeFile = file(relStoreFile!!)
        storePassword = relStorePassword
        keyAlias = relKeyAlias
        keyPassword = relKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release")
      else signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = "11" }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.09.00"))
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.material:material-icons-core")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
  implementation("androidx.room:room-ktx:2.6.1")
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
  testImplementation("androidx.compose.ui:ui-test-junit4")
  testImplementation("androidx.test:core:1.6.1")
  testImplementation("androidx.test.ext:junit:1.2.1")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.12.2")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.runner:1.6.2")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  debugImplementation("androidx.compose.ui:ui-tooling")
  ksp("androidx.room:room-compiler:2.6.1")
}
