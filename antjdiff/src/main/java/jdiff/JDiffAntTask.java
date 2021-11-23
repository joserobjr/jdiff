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
import org.apache.tools.ant.types.Path;

import java.io.*;
import java.util.Vector;

/**
 * An Ant task to produce a simple JDiff report. More complex reports still
 * need parameters that are controlled by the Ant Javadoc task.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class JDiffAntTask {

    /**
     * Forward or backward slash, as appropriate.
     */
    static String DIR_SEP = System.getProperty("file.separator");

    /**
     * Set if ant was started with -v or -verbose
     */
    private final boolean verboseAnt = false;

    /**
     * The JDiff Ant task does not inherit from an Ant task, such as the
     * Javadoc task, though this is usually how most Tasks are
     * written. This is because JDiff needs to run Javadoc three times
     * (twice for generating XML, once for generating HTML). The
     * Javadoc task has not easy way to reset its list of packages, so
     * we needed to be able to crate new Javadoc task objects.
     * <p>
     * Note: Don't confuse this class with the ProjectInfo used by JDiff.
     * This Project class is from Ant.
     */
    private Project project;

    /**
     * JDIFF_HOME must be set as a property in the Ant build file.
     * It should be set to the root JDiff directory, ie. the one where
     * jdiff.jar is found.
     */
    private String jdiffHome = "(not set)";

    /**
     * The classpath used by Javadoc to find jdiff.jar and xerces.jar.
     */
    private String jdiffClassPath = "(not set)";

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
    private String source = "1.5"; // Default is 1.5, so generics will work

    /**
     * A ProjectInfo-derived object for the older version of the project
     */
    private ProjectInfo oldProject;

    /**
     * A ProjectInfo-derived object for the newer version of the project
     */
    private ProjectInfo newProject;

    @SuppressWarnings("unused")
    public void execute() throws BuildException {
        execution();
    }

    private void execution() {
        jdiffHome = project.getProperty("JDIFF_HOME");
        if (jdiffHome == null || jdiffHome.compareTo("") == 0 |
                jdiffHome.compareTo("(not set)") == 0) {
            throw new BuildException("Error: invalid JDIFF_HOME property. Set it in the build file to the directory where jdiff is installed");
        }
        project.log(" JDiff home: " + jdiffHome, Project.MSG_INFO);

        jdiffClassPath = jdiffHome + DIR_SEP + "jdiff.jar" +
                System.getProperty("path.separator") +
                jdiffHome + DIR_SEP + "xerces-1.4.4.jar";

        // TODO detect and set verboseAnt

        // Create, if necessary, the directory for the JDiff HTML report
        if (!destdir.mkdir() && !destdir.exists()) {
            throw new BuildException(getDestdir() + " is not a valid directory");
        } else {
            project.log(" Report location: " + getDestdir() + DIR_SEP
                    + "changes.html", Project.MSG_INFO);
        }
        // Could also output the other parameters used for JDiff here

        // Check that there are indeed two projects to compare. If there
        // are no directories in the project, let Javadoc do the complaining
        if (oldProject == null || newProject == null) {
            throw new BuildException("Error: two projects are needed, one <old> and one <new>");
        }

        // Call Javadoc twice to generate Javadoc for each project
        generateJavadoc(oldProject);
        generateJavadoc(newProject);

        // Call Javadoc three times for JDiff.
        generateXML(oldProject);
        generateXML(newProject);
        compareXML(oldProject.getName(), newProject.getName());

        // Repeat some useful information
        project.log(" Report location: " + getDestdir() + DIR_SEP
                + "changes.html", Project.MSG_INFO);
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to generate the XML representation of a project's source files.
     *
     * @param proj The current Project
     */
    protected void generateXML(ProjectInfo proj) {
        String apiname = proj.getName();
        Javadoc jd = initJavadoc("Analyzing " + apiname);
        jd.setDestdir(getDestdir());
        addSourcePaths(jd, proj);

        // Tell Javadoc which packages we want to scan.
        // JDiff works with packagenames, not sourcefiles.
        jd.setPackagenames(getPackageList(proj));

        // Create the DocletInfo first so we have a way to use it to add params
        DocletInfo dInfo = jd.createDoclet();
        jd.setDoclet("jdiff.JDiff");
        jd.setDocletPath(new Path(project, jdiffClassPath));

        // Now set up some parameters for the JDiff doclet.
        DocletParam dp1 = dInfo.createParam();
        dp1.setName("-apiname");
        dp1.setValue(apiname);
        DocletParam dp2 = dInfo.createParam();
        dp2.setName("-baseURI");
        dp2.setValue("http://www.w3.org");
        // Put the generated file in the same directory as the report
        DocletParam dp3 = dInfo.createParam();
        dp3.setName("-apidir");
        dp3.setValue(getDestdir().toString());

        // Execute the Javadoc command to generate the XML file.
        jd.perform();
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to compare the XML representations of two instances of a project's
     * source files, and generate an HTML report summarizing the differences.
     *
     * @param oldapiname The name of the older version of the project
     * @param newapiname The name of the newer version of the project
     */
    protected void compareXML(String oldapiname, String newapiname) {
        Javadoc jd = initJavadoc("Comparing versions");
        jd.setDestdir(getDestdir());
        jd.setPrivate(true);

        // Tell Javadoc which files we want to scan - a dummy file in this case
        jd.setSourcefiles(jdiffHome + DIR_SEP + "Null.java");

        // Create the DocletInfo first so we have a way to use it to add params
        DocletInfo dInfo = jd.createDoclet();
        jd.setDoclet("jdiff.JDiff");
        jd.setDocletPath(new Path(project, jdiffClassPath));

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
        dp5.setValue(".." + DIR_SEP + oldapiname + DIR_SEP);
        DocletParam dp6 = dInfo.createParam();
        dp6.setName("-javadocnew");
        dp6.setValue(".." + DIR_SEP + newapiname + DIR_SEP);

        if (getStats()) {
            // There are no arguments to this argument
            dInfo.createParam().setName("-stats");
            // We also have to copy two image files for the stats pages
            copyFile(jdiffHome + DIR_SEP + "black.gif",
                    getDestdir().toString() + DIR_SEP + "black.gif");
            copyFile(jdiffHome + DIR_SEP + "background.gif",
                    getDestdir().toString() + DIR_SEP + "background.gif");
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
        jd.perform();
    }

    /**
     * Generate the Javadoc for the project. If you want to generate
     * the Javadoc report for the project with different parameters from the
     * simple ones used here, then use the Javadoc Ant task directly, and
     * set the javadoc attribute to the "old" or "new" element.
     *
     * @param proj The current Project
     */
    protected void generateJavadoc(ProjectInfo proj) {
        String javadoc = proj.getJavadoc();
        if (javadoc != null && javadoc.compareTo("generated") != 0) {
            project.log("Configured to use existing Javadoc located in " +
                    javadoc, Project.MSG_INFO);
            return;
        }

        String apiname = proj.getName();
        Javadoc jd = initJavadoc("Javadoc for " + apiname);
        jd.setDestdir(new File(getDestdir().toString() + DIR_SEP + apiname));
        addSourcePaths(jd, proj);

        jd.setPrivate(true);
        jd.setPackagenames(getPackageList(proj));

        // Execute the Javadoc command to generate a regular Javadoc report
        jd.perform();
    }

    /**
     * Create a fresh new Javadoc task object and initialize it.
     *
     * @param logMsg String which appears as a prefix in the Ant log
     * @return The new task.Javadoc object
     */
    protected Javadoc initJavadoc(String logMsg) {
        Javadoc jd = new Javadoc();
        jd.setProject(project); // Vital, otherwise Ant crashes
        jd.setTaskName(logMsg);
        jd.setSource(getSource()); // So we can set the language version
        jd.init();

        // Set up some common parameters for the Javadoc task
        if (verboseAnt) {
            jd.setVerbose(true);
        }
        return jd;
    }

    /**
     * Add the root directories for the given project to the Javadoc
     * sourcepath.
     */
    protected void addSourcePaths(Javadoc jd, ProjectInfo proj) {
        Vector<DirSet> dirSets = proj.getDirsets();
        int numDirSets = dirSets.size();
        for (int i = 0; i < numDirSets; i++) {
            DirSet dirSet = dirSets.elementAt(i);
            jd.setSourcepath(new Path(project, dirSet.getDir(project).toString()));
        }
    }

    /**
     * Return the comma-separated list of packages. The list is
     * generated from Ant DirSet tasks, and includes all directories
     * in a hierarchy, e.g. com, com/acme. com/acme/foo. Duplicates are
     * ignored.
     */
    protected String getPackageList(ProjectInfo proj) throws BuildException {
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
     * Copy a file from src to dst. Also checks that "destdir/changes" exists
     */
    protected void copyFile(String src, String dst) {
        File srcFile = new File(src);
        File dstFile = new File(dst);
        try {
            File reportSubdir = new File(getDestdir().toString() +
                    DIR_SEP + "changes");
            if (!reportSubdir.mkdir() && !reportSubdir.exists()) {
                project.log("Warning: unable to create " + reportSubdir,
                        Project.MSG_WARN);
            }

            try (
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dst)
            ) {
                // Transfer bytes from in to out
                byte[] buf = new byte[8 * 1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException fnfe) {
            project.log("Warning: unable to copy " + src +
                    " to " + dst, Project.MSG_WARN);
            // Discard the exception
        }
    }

    /**
     * Used as part of Ant's startup.
     */
    public void setProject(Project proj) {
        project = proj;
    }

    public File getDestdir() {
        return this.destdir;
    }

    /**
     * Used to store the destdir attribute of the JDiff task XML element.
     */
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
        private final Vector<DirSet> dirsets = new Vector<DirSet>();

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
