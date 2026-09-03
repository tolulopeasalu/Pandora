plugins {
    id("starter.android.library")
    id("starter.android.hilt")
}

android {
    namespace = "com.example.pandora.core.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
}
