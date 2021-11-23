plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.apache.ant:ant:1.10.12")
}

tasks {
    processResources {
        dependsOn(":jar")
        from(rootProject.tasks.jar.get().archiveFile) {
            into("META-INF/assets/libs")
        }
        from(rootProject.configurations.runtimeClasspath) {
            into("META-INF/assets/libs")
        }
    }
}
