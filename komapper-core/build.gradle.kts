dependencies {
    val kotlinCoroutinesVersion: String by project
    val kotlinVersion: String by project
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
}
