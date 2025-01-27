buildscript {
    repositories {
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
        jcenter()
        flatDir {
            dirs("libs") // Location of the .aar file
        }

    }

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.53.1")
    }

}



plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.android.library") version "7.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.53.1" apply false
}
