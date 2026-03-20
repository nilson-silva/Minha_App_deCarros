plugins {
    alias(libs.plugins.android.application) apply false
    // VERIFIQUE ESTA LINHA ABAIXO:
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.0" apply false
}