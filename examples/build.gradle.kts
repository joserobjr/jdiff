tasks {
    create("runJdiff") {
        group = "example"
        dependsOn(":antjdiff:jar")

        doLast {
            val antFile = project(":antjdiff").tasks.getByName<Jar>("jar").archiveFile.get().asFile
            val dest = buildDir.resolve("myreport").absolutePath
            ant.withGroovyBuilder {
                "taskdef"(
                    "name" to "jdiff",
                    "classname" to "jdiff.JDiffAntTask",
                    "classpath" to antFile.absolutePath
                )

                "jdiff"("destdir" to dest, "verbose" to "on", "stats" to "on", "docchanges" to "on") {
                    "old"("name" to "Version 1") {
                        "dirset"("dir" to "$projectDir/SuperProduct1.0", "includes" to "com/**")
                    }
                    "new"("name" to "Version 2") {
                        "dirset"("dir" to "$projectDir/SuperProduct2.0", "includes" to "com/**")
                    }
                }
            }
            println("Check the $dest folder to view the results")
        }
    }

    create("clean") {
        group = "build"
        doLast {
            check(buildDir.deleteRecursively())
        }
    }
}
