plugins {
    id("com.gtnewhorizons.gtnhconvention")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

// Keep JUnit 5 for shim tests (Phase 0 work continues to pass)
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
