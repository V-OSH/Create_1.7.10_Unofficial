plugins {
    id("com.gtnewhorizons.gtnhconvention")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

// Target Java 17 bytecode per PRD — players run JDK 17+ via lwjgl3ify.
// Only applies to our source, not Minecraft's decompiled Java 8 source.
tasks.compileJava {
    options.release.set(17)
}
tasks.compileTestJava {
    options.release.set(17)
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

// Configure Eclipse JDT to use Java 17 for mod sources.
// The GTNH convention plugin defaults to Java 8 (for Minecraft), but our
// mod uses --release 17 and modern syntax.
eclipse {
    jdt {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
