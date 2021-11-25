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

package jdiff;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.taskdefs.Javadoc.DocletInfo;
import org.apache.tools.ant.taskdefs.Javadoc.DocletParam;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Ant task to produce a simple JDiff report. More complex reports still
 * need parameters that are controlled by the Ant Javadoc task.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class JDiffAntTask {

    /**
     * The JDiff Ant task does not inherit from an Ant task, such as the
     * Javadoc task, though this is usually how most Tasks are
     * written. This is because JDiff needs to run Javadoc three times
     * (twice for generating XML, once for generating HTML). The
     * Javadoc task has no easy way to reset its list of packages, so
     * we needed to be able to crate new Javadoc task objects.
     * <p>
     * Note: Don't confuse this class with the ProjectInfo used by JDiff.
     * This Project class is from Ant.
     */
    private Project project;

    /**
     * The destination directory for the generated report.
     * The default is "./jdiff_report".
     */
    private File destdir = new File("jdiff_report");

    /**
     * Increases the JDiff Ant task logging verbosity if set with "yes", "on"
     * or true". Default has to be false.
     * To increase verbosity of Javadoc, start Ant with -v or -verbose.
     */
    private boolean verbose;

    /**
     * Add the -docchanges argument, to track changes in Javadoc documentation
     * as well as changes in classes etc.
     */
    private boolean docchanges;

    /**
     * Add the -incompatible argument, to only report incompatible changes.
     */
    private boolean incompatible;

    /**
     * Add statistics to the report if set. Default can only be false.
     */
    private boolean stats;

    /**
     * Allow the source language version to be specified.
     */
    private String source = "1.8";

    /**
     * Add author tags to the javadoc
     */
    private boolean author = true;

    /**
     * Set the scope to be processed by javadoc.
     */
    private Javadoc.AccessType access = (Javadoc.AccessType) EnumeratedAttribute.getInstance(Javadoc.AccessType.class, "protected");

    /**
     * Set the text to be placed at the bottom of each output file.
     */
    private Javadoc.Html javadocBottom;

    /**
     * Set the header text to be placed at the top of each output file.
     */
    private Javadoc.Html javadocHeader;

    /**
     * Set the footer text to be placed at the bottom of each output file.
     */
    private Javadoc.Html javadocFooter;

    /**
     * Charset for cross-platform viewing of generated documentation.
     */
    private String charset;

    /**
     * Output file encoding.
     */
    private String docEncoding;

    /**
     * Set the encoding name of the source files.
     */
    private String encoding;

    /**
     * Set the title of the generated overview page.
     */
    private Javadoc.Html javadocTitle;

    /**
     * Should the build process fail if Javadoc fails (as indicated by a non zero return code)?
     * Default is false.
     */
    private boolean failsOnError;

    /**
     * Should the build process fail if Javadoc warns (as indicated by the word "warning" on stdout)?
     * Default is false.
     */
    private boolean failsOnWarning;

    /**
     * Group specified packages together in overview page.
     * <p>A command separated list of group specs, each one being a group name and package specification separated by a space.</p>
     */
    private String group;

    /**
     * Makes use of the break iterator class.
     * <p>
     * The BreakIterator class implements methods for finding the location of boundaries in text. Instances of BreakIterator maintain a current position and scan over text returning the index of characters where boundaries occur. Internally, BreakIterator scans text using a CharacterIterator, and is thus able to scan text held by any object implementing that protocol. A StringCharacterIterator is used to scan String objects passed to setText.
     */
    private boolean breakIterator;

    /**
     * Enables the -linksource switch, will be ignored if Javadoc is not the 1.4 version.
     */
    private boolean linkSource;

    /**
     * Link to docs at "url" using package list at "url2"
     * - separate the URLs by using a space character.
     */
    private List<String> linkOffline;

    /**
     * Set an additional parameter on the command line.
     */
    private List<AdditionalParamInfo> additionalParameters;

    /**
     * A ProjectInfo-derived object for the older version of the project
     */
    private ProjectInfo oldProject;

    /**
     * A ProjectInfo-derived object for the newer version of the project
     */
    private ProjectInfo newProject;

    /**
     * Called by Ant to execute the task.
     * @throws BuildException If there were problems executing the task.
     */
    public void execute() throws BuildException {
        try {
            execution();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void execution() throws BuildException {
        // Create, if necessary, the directory for the JDiff HTML report
        if (!destdir.isDirectory() && !destdir.mkdirs()) {
            throw new BuildException(destdir + " is not a valid directory");
        } else {
            logReportLocation();
        }
        // Could also output the other parameters used for JDiff here

        // Check that there are indeed two projects to compare. If there
        // are no directories in the project, let Javadoc do the complaining
        if (oldProject == null || newProject == null) {
            throw new BuildException("Error: two projects are needed, one <old> and one <new>");
        }

        // Extract the required assets asynchronously
        CompletableFuture<File> futureAssets = CompletableFuture.supplyAsync(() -> {
            try {
                return extractAssets();
            } catch (IOException | URISyntaxException e) {
                throw new BuildException(e);
            }
        });

        CompletableFuture<String> futureJDiffClasspath = futureAssets.thenApply(assets->
                Arrays.stream(Objects.requireNonNull(new File(assets, "libs").listFiles()))
                        .map(File::getAbsolutePath)
                        .collect(Collectors.joining(File.pathSeparator))
        );

        // Call Javadoc twice to generate Javadoc for each project
        generateJavadoc(oldProject, AdditionalParamInfo::isOldJavadoc);
        generateJavadoc(newProject, AdditionalParamInfo::isNewJavadoc);

        // Get the result of the async process
        File assets;
        String jDiffClasspath;
        try {
            assets = futureAssets.get();
            jDiffClasspath = futureJDiffClasspath.get();
        } catch (ExecutionException|InterruptedException e) {
            throw new BuildException(e);
        }

        try {
            // Call Javadoc three times for JDiff.
            generateXML(oldProject, jDiffClasspath, AdditionalParamInfo::isOldJavadocXML);
            generateXML(newProject, jDiffClasspath, AdditionalParamInfo::isNewJavadocXML);
            compareXML(oldProject.getName(), newProject.getName(), assets, jDiffClasspath, AdditionalParamInfo::isComparison);

            // Repeat some useful information
            logReportLocation();
        } finally {
            // Delete the temporary folder recursively
            try (Stream<java.nio.file.Path> walk = Files.walk(assets.toPath())) {
                //noinspection ResultOfMethodCallIgnored
                walk.sorted(Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                project.log(" Failed to delete the temporary folder " + assets, e, Project.MSG_WARN);
            }
        }
    }

    private void logReportLocation() {
        project.log(" Report location: " + destdir + File.separator + "changes.html", Project.MSG_INFO);
    }

    /**
     * Extracts the assets into a temporary folder.
     * @return The temporary folder.
     */
    private File extractAssets() throws IOException, URISyntaxException {
        String dir = "/META-INF/assets";
        java.nio.file.Path tempFolder = Files.createTempDirectory("jdiff_jars_");
        URI uri = Objects.requireNonNull(JDiffAntTask.class.getResource(dir)).toURI();
        java.nio.file.Path path;
        FileSystem fileSystem = null;
        try {
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath(dir);
            } else {
                path = Paths.get(uri);
            }
            try(Stream<java.nio.file.Path> walk = Files.walk(path, 2)) {
                walk.forEach(subPath -> {
                    java.nio.file.Path target = tempFolder.resolve(path.relativize(subPath).toString());
                    try {
                        Files.copy(subPath, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new BuildException("Could not copy file " + subPath + " to " +target, e);
                    }
                });
            }
        } finally {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
        return tempFolder.toFile();
    }

    private void addAdditionalParams(Javadoc javadoc, Predicate<AdditionalParamInfo> additionalParamFilter) {
        Optional.ofNullable(additionalParameters).ifPresent(list -> list.forEach(additionalParamInfo -> {
            if (additionalParamFilter.test(additionalParamInfo)) {
                javadoc.createArg().setValue(Objects.requireNonNull(
                        additionalParamInfo.getValue(),
                        "The additional param value cannot be null"
                ));
            }
        }));
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to generate the XML representation of a project's source files.
     *
     * @param proj The current Project
     * @param jDiffClasspath The JDiff doclet classpath
     */
    private void generateXML(ProjectInfo proj, String jDiffClasspath, Predicate<AdditionalParamInfo> additionalParamFilter) {
        String apiName = proj.getName();
        Javadoc javadoc = initJavadoc("Analyzing " + apiName);
        javadoc.setDestdir(getDestdir());
        addSourcePaths(javadoc, proj);
        addAdditionalParams(javadoc, additionalParamFilter);

        // Tell Javadoc which packages we want to scan.
        // JDiff works with packagenames, not sourcefiles.
        javadoc.setPackagenames(getPackageList(proj));

        // Create the DocletInfo first so we have a way to use it to add params
        DocletInfo dInfo = javadoc.createDoclet();
        javadoc.setDoclet("jdiff.JDiff");
        javadoc.setDocletPath(new Path(project, jDiffClasspath));

        // Now set up some parameters for the JDiff doclet.
        DocletParam dp1 = dInfo.createParam();
        dp1.setName("-apiname");
        dp1.setValue(apiName);
        DocletParam dp2 = dInfo.createParam();
        dp2.setName("-baseURI");
        dp2.setValue("http://www.w3.org");
        // Put the generated file in the same directory as the report
        DocletParam dp3 = dInfo.createParam();
        dp3.setName("-apidir");
        dp3.setValue(getDestdir().toString());

        // Execute the Javadoc command to generate the XML file.
        javadoc.perform();
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to compare the XML representations of two instances of a project's
     * source files, and generate an HTML report summarizing the differences.
     * @param oldapiname The name of the older version of the project
     * @param newapiname The name of the newer version of the project
     * @param assets The path to the extracted assets folder
     * @param jDiffClasspath The JDiff doclet classpath
     */
    private void compareXML(String oldapiname, String newapiname, File assets, String jDiffClasspath,
                            Predicate<AdditionalParamInfo> additionalParamFilter) {
        Javadoc javadoc = initJavadoc("Comparing versions");
        Optional.ofNullable(getDocEncoding()).ifPresent(javadoc::setDocencoding);
        addAdditionalParams(javadoc, additionalParamFilter);
        javadoc.setDestdir(getDestdir());

        // Tell Javadoc which files we want to scan - a dummy file in this case
        javadoc.setSourcefiles(assets + File.separator + "Null.java");

        // Create the DocletInfo first so we have a way to use it to add params
        DocletInfo dInfo = javadoc.createDoclet();
        javadoc.setDoclet("jdiff.JDiff");
        javadoc.setDocletPath(new Path(project, jDiffClasspath));

        // Now set up some parameters for the JDiff doclet.
        DocletParam dp1 = dInfo.createParam();
        dp1.setName("-oldapi");
        dp1.setValue(oldapiname);
        DocletParam dp2 = dInfo.createParam();
        dp2.setName("-newapi");
        dp2.setValue(newapiname);
        // Get the generated XML files from the same directory as the report
        DocletParam dp3 = dInfo.createParam();
        dp3.setName("-oldapidir");
        dp3.setValue(getDestdir().toString());
        DocletParam dp4 = dInfo.createParam();
        dp4.setName("-newapidir");
        dp4.setValue(getDestdir().toString());

        // Assume that Javadoc reports already exist in ../"apiname"
        DocletParam dp5 = dInfo.createParam();
        dp5.setName("-javadocold");
        dp5.setValue(".." + File.separator + oldapiname + File.separator);
        DocletParam dp6 = dInfo.createParam();
        dp6.setName("-javadocnew");
        dp6.setValue(".." + File.separator + newapiname + File.separator);

        if (getStats()) {
            // There are no arguments to this argument
            dInfo.createParam().setName("-stats");

            File reportSubDir = new File(getDestdir() + File.separator + "changes");
            if (!reportSubDir.mkdir() && !reportSubDir.exists()) {
                project.log("Warning: unable to create " + reportSubDir, Project.MSG_WARN);
            }

            // We also have to copy two image files for the stats pages
            java.nio.file.Path destDirPath = getDestdir().toPath();
            java.nio.file.Path assetsPath = assets.toPath();
            copyFile(assetsPath.resolve("black.gif"), destDirPath);
            copyFile(assetsPath.resolve("background.gif"), destDirPath);
            copyFile(assetsPath.resolve("api.xsd"), destDirPath);
            copyFile(assetsPath.resolve("comments.xsd"), destDirPath);
        }

        if (getDocchanges()) {
            // There are no arguments to this argument
            dInfo.createParam().setName("-docchanges");
        }

        if (getIncompatible()) {
            // There are no arguments to this argument
            dInfo.createParam().setName("-incompatible");
        }

        // Execute the Javadoc command to compare the two XML files
        javadoc.perform();
    }

    /**
     * Generate the Javadoc for the project. If you want to generate
     * the Javadoc report for the project with different parameters from the
     * simple ones used here, then use the Javadoc Ant task directly, and
     * set the javadoc attribute to the "old" or "new" element.
     *
     * @param proj The current Project
     */
    private void generateJavadoc(ProjectInfo proj, Predicate<AdditionalParamInfo> additionalParamFilter) {
        String javadocPath = proj.getJavadoc();
        if (javadocPath != null && !javadocPath.equalsIgnoreCase("generated")) {
            project.log("Configured to use existing Javadoc located in " + javadocPath, Project.MSG_INFO);
            return;
        }

        String apiName = proj.getName();
        Javadoc javadoc = initJavadoc("Javadoc for " + apiName);
        addAdditionalParams(javadoc, additionalParamFilter);
        Optional.ofNullable(getDocEncoding()).ifPresent(javadoc::setDocencoding);
        javadoc.setDestdir(new File(destdir, apiName));
        addSourcePaths(javadoc, proj);

        javadoc.setPackagenames(getPackageList(proj));

        // Execute the Javadoc command to generate a regular Javadoc report
        javadoc.perform();
    }

    /**
     * Create a fresh new Javadoc task object and initialize it.
     *
     * @param logMsg String which appears as a prefix in the Ant log
     * @return The new task.Javadoc object
     */
    private Javadoc initJavadoc(String logMsg) {
        Javadoc javadoc = new Javadoc();
        javadoc.setProject(project); // Vital, otherwise Ant crashes
        javadoc.setTaskName(logMsg);
        javadoc.setSource(getSource()); // So we can set the language version
        javadoc.setAccess(getAccess());
        javadoc.setAuthor(getAuthor());
        javadoc.setFailonerror(isFailsOnError());
        javadoc.setFailonwarning(isFailsOnWarning());
        javadoc.setGroup(getGroup());
        javadoc.setBreakiterator(isBreakIterator());
        javadoc.setLinksource(isLinkSource());

        Optional.ofNullable(javadocTitle).ifPresent(javadoc::addDoctitle);
        Optional.ofNullable(javadocBottom).ifPresent(javadoc::addBottom);

        Optional.ofNullable(javadocHeader).ifPresent(javadoc::addHeader);
        Optional.ofNullable(javadocFooter).ifPresent(javadoc::addFooter);

        Optional.ofNullable(getEncoding()).ifPresent(javadoc::setEncoding);
        Optional.ofNullable(getCharset()).ifPresent(javadoc::setCharset);

        Optional.ofNullable(linkOffline).ifPresent(list -> list.forEach(javadoc::setLinkoffline));
        javadoc.init();

        // Set up some common parameters for the Javadoc task
        if (verbose) {
            javadoc.setVerbose(true);
        }
        return javadoc;
    }

    /**
     * Add the root directories for the given project to the Javadoc
     * sourcepath.
     */
    private void addSourcePaths(Javadoc jd, ProjectInfo proj) {
        Vector<DirSet> dirSets = proj.getDirsets();
        int numDirSets = dirSets.size();
        for (int i = 0; i < numDirSets; i++) {
            DirSet dirSet = dirSets.elementAt(i);
            jd.setSourcepath(new Path(project, dirSet.getDir(project).getPath()));
        }
    }

    /**
     * Return the comma-separated list of packages. The list is
     * generated from Ant DirSet tasks, and includes all directories
     * in a hierarchy, e.g. com, com/acme. com/acme/foo. Duplicates are
     * ignored.
     */
    private String getPackageList(ProjectInfo proj) throws BuildException {
        StringBuilder sb = new StringBuilder();
        Vector<DirSet> dirSets = proj.getDirsets();
        int numDirSets = dirSets.size();
        boolean addComma = false;
        for (int i = 0; i < numDirSets; i++) {
            DirSet dirSet = dirSets.elementAt(i);
            DirectoryScanner dirScanner = dirSet.getDirectoryScanner(project);
            String[] files = dirScanner.getIncludedDirectories();
            for (String file : files) {
                if (!addComma) {
                    addComma = true;
                } else {
                    sb.append(",");
                }
                sb.append(file);
            }
        }
        String packageList = sb.toString();
        if (packageList.compareTo("") == 0) {
            throw new BuildException("Error: no packages found to scan");
        }
        project.log(" Package list: " + packageList, Project.MSG_INFO);

        return packageList;
    }

    /**
     * Copy a file from src to dst.
     */
    private void copyFile(java.nio.file.Path src, java.nio.file.Path destDir) {
        java.nio.file.Path dest = destDir.resolve(src.getFileName());
        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            project.log("Warning: unable to copy " + src + " to " + dest, e, Project.MSG_WARN);
        }
    }

    public void setProject(Project proj) {
        project = proj;
    }

    public File getDestdir() {
        return this.destdir;
    }

    public void setDestdir(File value) {
        this.destdir = value;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean value) {
        this.verbose = value;
    }

    public boolean getDocchanges() {
        return this.docchanges;
    }

    public void setDocchanges(boolean value) {
        this.docchanges = value;
    }

    public boolean getIncompatible() {
        return this.incompatible;
    }

    public void setIncompatible(boolean value) {
        this.incompatible = value;
    }

    public boolean getStats() {
        return this.stats;
    }

    public void setStats(boolean value) {
        this.stats = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean getAuthor() {
        return author;
    }

    public void setAuthor(boolean author) {
        this.author = author;
    }

    public Javadoc.AccessType getAccess() {
        return access;
    }

    public void setAccess(Javadoc.AccessType access) {
        this.access = access;
    }

    public void addJavadocBottom(Javadoc.Html javadocBottom) {
        this.javadocBottom = javadocBottom;
    }

    public void addJavadocHeader(Javadoc.Html javadocHeader) {
        this.javadocHeader = javadocHeader;
    }

    public void addJavadocFooter(Javadoc.Html javadocFooter) {
        this.javadocFooter = javadocFooter;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getDocEncoding() {
        return docEncoding;
    }

    public void setDocEncoding(String docEncoding) {
        this.docEncoding = docEncoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void addJavadocTitle(Javadoc.Html javadocTitle) {
        this.javadocTitle = javadocTitle;
    }

    public boolean isFailsOnError() {
        return failsOnError;
    }

    public void setFailsOnError(boolean failsOnError) {
        this.failsOnError = failsOnError;
    }

    public boolean isFailsOnWarning() {
        return failsOnWarning;
    }

    public void setFailsOnWarning(boolean failsOnWarning) {
        this.failsOnWarning = failsOnWarning;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isBreakIterator() {
        return breakIterator;
    }

    public void setBreakIterator(boolean breakIterator) {
        this.breakIterator = breakIterator;
    }

    public boolean isLinkSource() {
        return linkSource;
    }

    public void setLinkSource(boolean linkSource) {
        this.linkSource = linkSource;
    }

    public void addLinkOffline(String link) {
        if (linkOffline == null) {
            linkOffline = new ArrayList<>();
        }
        linkOffline.add(link);
    }

    public void addConfiguredAdditionalParam(AdditionalParamInfo param) {
        if (additionalParameters == null) {
            additionalParameters = new ArrayList<>();
        }
        additionalParameters.add(param);
    }

    /**
     * Used to store the child element named "old", which is under the
     * JDiff task XML element.
     */
    public void addConfiguredOld(ProjectInfo projInfo) {
        oldProject = projInfo;
    }

    /**
     * Used to store the child element named "new", which is under the
     * JDiff task XML element.
     */
    public void addConfiguredNew(ProjectInfo projInfo) {
        newProject = projInfo;
    }

    /**
     * This class is allows ant projects to define custom parameters and decide where they apply.
     * <p>
     * All parameters apply to all javadoc calls by default.
     */
    public static class AdditionalParamInfo {
        /**
         * The param.
         */
        private String value;

        /**
         * This parameter is applied when the old javadoc is being generated.
         */
        private boolean oldJavadoc = true;

        /**
         * This parameter is applied when the new javadoc is being generated.
         */
        private boolean newJavadoc = true;

        /**
         * This parameter is applied when the old javadoc XML is being generated.
         */
        private boolean oldJavadocXML = true;

        /**
         * This parameter is applied when the new javadoc XML is being generated.
         */
        private boolean newJavadocXML = true;

        /**
         * This parameter is applied at the final stage, where the jdiff is generating the final files.
         */
        private boolean comparison = true;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isOldJavadoc() {
            return oldJavadoc;
        }

        public void setOldJavadoc(boolean oldJavadoc) {
            this.oldJavadoc = oldJavadoc;
        }

        public boolean isNewJavadoc() {
            return newJavadoc;
        }

        public void setNewJavadoc(boolean newJavadoc) {
            this.newJavadoc = newJavadoc;
        }

        public boolean isOldJavadocXML() {
            return oldJavadocXML;
        }

        public void setOldJavadocXML(boolean oldJavadocXML) {
            this.oldJavadocXML = oldJavadocXML;
        }

        public boolean isNewJavadocXML() {
            return newJavadocXML;
        }

        public void setNewJavadocXML(boolean newJavadocXML) {
            this.newJavadocXML = newJavadocXML;
        }

        public boolean isComparison() {
            return comparison;
        }

        public void setComparison(boolean comparison) {
            this.comparison = comparison;
        }
    }

    /**
     * This class handles the information about a project, whether it is
     * the older or newer version.
     * <p>
     * Note: Don't confuse this class with the Project used by Ant.
     * This ProjectInfo class is from local to this task.
     */
    public static class ProjectInfo {
        /**
         * These are the directories which contain the packages which make
         * up the project. Filesets are not supported by JDiff.
         */
        private final Vector<DirSet> dirsets = new Vector<>();

        /**
         * The name of the project. This is used (without spaces) as the
         * base of the name of the file which contains the XML representing
         * the project.
         */
        private String name;

        /**
         * The location of the Javadoc HTML for this project. Default value
         * is "generate", which will cause the Javadoc to be generated in
         * a subdirectory named "name" in the task's destdir directory.
         */
        private String javadoc;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            name = value;
        }

        public String getJavadoc() {
            return javadoc;
        }

        public void setJavadoc(String value) {
            javadoc = value;
        }

        public void setDirset(DirSet value) {
            dirsets.add(value);
        }

        public Vector<DirSet> getDirsets() {
            return dirsets;
        }

        /**
         * Used to store the child element named "dirset", which is under the
         * "old" or "new" XML elements.
         */
        public void addDirset(DirSet aDirset) {
            setDirset(aDirset);
        }

    }
}
