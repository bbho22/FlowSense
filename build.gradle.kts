// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()        // <-- make sure this is present
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // or your AGP version
        classpath("com.google.gms:google-services:4.4.2") // Firebase plugin
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}