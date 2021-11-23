tasks {
    val home = buildDir.resolve("jdiffHome").also { it.mkdirs() }
    val unzipJDiff = create<Copy>("unzipJDiff") {
        from(zipTree(rootProject.tasks.getByName<Zip>("distZip").archiveFile))
        into(home.absolutePath)
    }

    create("runJdiff") {
        dependsOn(":antjdiff:jar", ":distZip", unzipJDiff)

        doLast {
            val antFile = project(":antjdiff").tasks.getByName<Jar>("jar").archiveFile.get().asFile
            ant.withGroovyBuilder {
                "property"(
                    "name" to "JDIFF_HOME",
                    "value" to home.resolve("jdiff").resolve("lib").absolutePath
                )

                "taskdef"(
                    "name" to "jdiff",
                    "classname" to "jdiff.JDiffAntTask",
                    "classpath" to antFile.absolutePath
                )

                "jdiff"("destdir" to buildDir.resolve("myreport").absolutePath, "verbose" to "on", "stats" to "on", "docchanges" to "on") {
                    "old"("name" to "Version 1") {
                        "dirset"("dir" to "$projectDir/SuperProduct1.0", "includes" to "com/**")
                    }
                    "new"("name" to "Version 1") {
                        "dirset"("dir" to "$projectDir/SuperProduct1.0", "includes" to "com/**")
                    }
                }
            }
        }
    }

    create("clean") {
        doLast {
            check(buildDir.deleteRecursively())
        }
    }
}
