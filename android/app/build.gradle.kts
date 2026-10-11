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
  }
}

dependencies {
  testImplementation("junit:junit:4.13.2")
}

