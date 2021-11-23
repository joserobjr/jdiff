plugins {
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    //implementation("xerces:xercesImpl:2.12.1")
    implementation("xerces:xerces:1.4.4")
    compileOnly(files("${System.getProperty("java.home")}/../lib/tools.jar"))
    runtimeOnly(project(":antjdiff")) {
        exclude(group = "org.apache.ant")
    }
}

tasks {
    fun AbstractArchiveTask.addResources() {
        into("jdiff/lib") {
            from("src/unpacked-resources")
        }
        into("jdiff") {
            from("LICENSE.txt")
            from("README.txt")
            from("doc/jdiff.html")
            from("examples/example.xml")
        }
    }

    distZip {
        addResources()
    }
    distTar {
        addResources()
    }
}
