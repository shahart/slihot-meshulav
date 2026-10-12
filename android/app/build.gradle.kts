plugins {
  id("com.android.application")
}

android {
  namespace = "org.malegilboa.slihot"
  compileSdk = 36

  buildFeatures {
    buildConfig = true
  }

  defaultConfig {
    applicationId = "org.malegilboa.slihot"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}

dependencies {
  testImplementation("junit:junit:4.13.2")

  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test:runner:1.7.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

