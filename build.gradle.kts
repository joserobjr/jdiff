/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2021  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */

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
