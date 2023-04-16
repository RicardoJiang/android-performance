// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version ("7.4.0") apply false
    id("com.android.library") version ("7.4.0") apply false
    id("org.jetbrains.kotlin.android") version ("1.8.10") apply false
    id("com.bytedance.rhea-trace") version ("1.0.1") apply false
}

apply(from = "gradle/scripts/btrace.gradle")