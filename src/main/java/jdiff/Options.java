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

import com.sun.javadoc.DocErrorReporter;

import java.util.Locale;

/**
 * Class to handle options for JDiff.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class Options {

    /**
     * Set to enable increased logging verbosity for debugging.
     */
    private static final boolean trace = false;

    /**
     * All the options passed on the command line. Logged to XML.
     */
    public static String cmdOptions = "";

    /**
     * Default constructor.
     */
    public Options() {
    }

    /**
     * Returns the "length" of a given option. If an option takes no
     * arguments, its length is one. If it takes one argument, its
     * length is two, and so on. This method is called by Javadoc to
     * parse the options it does not recognize. It then calls
     * {@link #validOptions} to validate them.
     * <blockquote>
     * <b>Note:</b><br>
     * The options arrive as case-sensitive strings. For options that
     * are not case-sensitive, use toLowerCase() on the option string
     * before comparing it.
     * </blockquote>
     *
     * @param option a String containing an option
     * @return an int telling how many components that option has
     */
    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static int optionLength(String option) {
        String opt = option.toLowerCase();

        switch (opt) {
            // Standard options
            case "-authorid": return 2;
            case "-versionid": return 2;
            case "-d": return 2;
            case "-classlist": return 1;
            case "-title": return 2;
            case "-docletid": return 1;
            case "-evident": return 2;
            case "-skippkg": return 2;
            case "-skipclass": return 2;
            case "-execdepth": return 2;
            case "-encoding": return 2;
            case "-docencoding": return 2;
            case "-charset": return 2;
            case "-help": return 1;
            case "-version": return 1;
            case "-package": return 1;
            case "-protected": return 1;
            case "-public": return 1;
            case "-private": return 1;
            case "-sourcepath": return 2;

            // Options to control JDiff
            case "-apiname": return 2;
            case "-oldapi": return 2;
            case "-newapi": return 2;

            // Options to control the location of the XML files
            case "-apidir": return 2;
            case "-oldapidir": return 2;
            case "-newapidir": return 2;

            // Options for the exclusion level for classes and members
            case "-excludeclass": return 2;
            case "-excludemember": return 2;
            case "-firstsentence": return 1;
            case "-docchanges": return 1;
            case "-incompatible": return 1;
            case "-packagesonly": return 1;
            case "-showallchanges": return 1;

            // Option to change the location for the existing Javadoc
            // documentation for the new API. Default is "../"
            case "-javadocnew": return 2;
            // Option to change the location for the existing Javadoc
            // documentation for the old API. Default is null.
            case "-javadocold": return 2;

            case "-baseuri": return 2;

            // Option not to suggest comments at all
            case "-nosuggest": return 2;

            // Option to enable checking that the comments end with a period.
            case "-checkcomments": return 1;
            // Option to retain non-printing characters in comments.
            case "-retainnonprinting": return 1;
            // Option for the name of the exclude tag
            case "-excludetag": return 2;
            // Generate statistical output
            case "-stats": return 1;

            // Set the browser window title
            case "-windowtitle": return 2;
            // Set the report title
            case "-doctitle": return 2;

            default: return 0;
        }
    }

    /**
     * After parsing the available options using {@link #optionLength},
     * Javadoc invokes this method with an array of options-arrays, where
     * the first item in any array is the option, and subsequent items in
     * that array are its arguments. So, if -print is an option that takes
     * no arguments, and -copies is an option that takes 1 argument, then
     * <pre>
     *     -print -copies 3
     * </pre>
     * produces an array of arrays that looks like:
     * <pre>
     *      option[0][0] = -print
     *      option[1][0] = -copies
     *      option[1][1] = 3
     * </pre>
     * (By convention, command line switches start with a "-", but
     * they don't have to.)
     * <p>
     * <b>Note:</b><br>
     * Javadoc passes <i>all</i>parameters to this method, not just
     * those that Javadoc doesn't recognize. The only way to
     * identify unexpected arguments is therefore to check for every
     * Javadoc parameter as well as doclet parameters.
     *
     * @param options  an array of String arrays, one per option
     * @param reporter a DocErrorReporter for generating error messages
     * @return true if no errors were found, and all options are
     * valid
     */
    public static boolean validOptions(String[][] options,
                                       DocErrorReporter reporter) {
        final DocErrorReporter errOut = reporter;

        // A nice object-oriented way of handling errors. An instance of this
        // class puts out an error message and keeps track of whether or not
        // an error was found.
        class ErrorHandler {
            boolean noErrorsFound = true;

            void msg(String msg) {
                noErrorsFound = false;
                errOut.printError(msg);
            }
        }

        ErrorHandler err = new ErrorHandler();
        if (trace)
            System.out.println("Command line arguments: ");
        for (String[] strings : options) {
            for (String string : strings) {
                Options.cmdOptions += " " + string;
                if (trace)
                    System.out.print(" " + string);
            }
        }
        if (trace)
            System.out.println();

        for (String[] option : options) {
            String mainOption = option[0].toLowerCase(Locale.ENGLISH);
            switch (mainOption) {
                case "-apiname":
                    if (option.length < 2) {
                        err.msg("No version identifier specified after -apiname option.");
                    } else if (JDiff.compareAPIs) {
                        err.msg("Use the -apiname option, or the -oldapi and -newapi options, but not both.");
                    } else {
                        String filename = option[1];
                        RootDocToXML.apiIdentifier = filename;
                        filename = filename.replace(' ', '_');
                        RootDocToXML.outputFileName = filename + ".xml";
                        JDiff.writeXML = true;
                        JDiff.compareAPIs = false;
                    }
                    break;
                case "-apidir":
                    if (option.length < 2) {
                        err.msg("No directory specified after -apidir option.");
                    } else {
                        RootDocToXML.outputDirectory = option[1];
                    }
                    break;
                case "-oldapi":
                    if (option.length < 2) {
                        err.msg("No version identifier specified after -oldapi option.");
                    } else if (JDiff.writeXML) {
                        err.msg("Use the -apiname or -oldapi option, but not both.");
                    } else {
                        String filename = option[1];
                        filename = filename.replace(' ', '_');
                        JDiff.oldFileName = filename + ".xml";
                        JDiff.writeXML = false;
                        JDiff.compareAPIs = true;
                    }
                    break;
                case "-oldapidir":
                    if (option.length < 2) {
                        err.msg("No directory specified after -oldapidir option.");
                    } else {
                        JDiff.oldDirectory = option[1];
                    }
                    break;
                case "-newapi":
                    if (option.length < 2) {
                        err.msg("No version identifier specified after -newapi option.");
                    } else if (JDiff.writeXML) {
                        err.msg("Use the -apiname or -newapi option, but not both.");
                    } else {
                        String filename = option[1];
                        filename = filename.replace(' ', '_');
                        JDiff.newFileName = filename + ".xml";
                        JDiff.writeXML = false;
                        JDiff.compareAPIs = true;
                    }
                    break;
                case "-newapidir":
                    if (option.length < 2) {
                        err.msg("No directory specified after -newapidir option.");
                    } else {
                        JDiff.newDirectory = option[1];
                    }
                    break;
                case "-d":
                    if (option.length < 2) {
                        err.msg("No directory specified after -d option.");
                    } else {
                        HTMLReportGenerator.outputDir = option[1];
                    }
                    break;
                case "-javadocnew":
                    if (option.length < 2) {
                        err.msg("No location specified after -javadocnew option.");
                    } else {
                        HTMLReportGenerator.newDocPrefix = option[1];
                    }
                    break;
                case "-javadocold":
                    if (option.length < 2) {
                        err.msg("No location specified after -javadocold option.");
                    } else {
                        HTMLReportGenerator.oldDocPrefix = option[1];
                    }
                    break;
                case "-baseuri":
                    if (option.length < 2) {
                        err.msg("No base location specified after -baseURI option.");
                    } else {
                        RootDocToXML.baseURI = option[1];
                    }
                    break;
                case "-excludeclass":
                    if (option.length < 2) {
                        err.msg("No level (public|protected|package|private) specified after -excludeclass option.");
                    } else {
                        String level = option[1];
                        if (level.compareTo("public") != 0 &&
                                level.compareTo("protected") != 0 &&
                                level.compareTo("package") != 0 &&
                                level.compareTo("private") != 0) {
                            err.msg("Level specified after -excludeclass option must be one of (public|protected|package|private).");
                        } else {
                            RootDocToXML.classVisibilityLevel = level;
                        }
                    }
                    break;
                case "-excludemember":
                    if (option.length < 2) {
                        err.msg("No level (public|protected|package|private) specified after -excludemember option.");
                    } else {
                        String level = option[1];
                        if (level.compareTo("public") != 0 &&
                                level.compareTo("protected") != 0 &&
                                level.compareTo("package") != 0 &&
                                level.compareTo("private") != 0) {
                            err.msg("Level specified after -excludemember option must be one of (public|protected|package|private).");
                        } else {
                            RootDocToXML.memberVisibilityLevel = level;
                        }
                    }
                    break;
                case "-firstsentence":
                    RootDocToXML.saveAllDocs = false;
                    break;
                case "-docchanges":
                    HTMLReportGenerator.reportDocChanges = true;
                    Diff.noDocDiffs = false;
                    break;
                case "-incompatible":
                    HTMLReportGenerator.incompatibleChangesOnly = true;
                    break;
                case "-packagesonly":
                    RootDocToXML.packagesOnly = true;
                    break;
                case "-showallchanges":
                    Diff.showAllChanges = true;
                    break;
                case "-nosuggest":
                    if (option.length < 2) {
                        err.msg("No level (all|remove|add|change) specified after -nosuggest option.");
                    } else {
                        String level = option[1];
                        if (level.compareTo("all") != 0 &&
                                level.compareTo("remove") != 0 &&
                                level.compareTo("add") != 0 &&
                                level.compareTo("change") != 0) {
                            err.msg("Level specified after -nosuggest option must be one of (all|remove|add|change).");
                        } else {
                            if (level.compareTo("removal") == 0)
                                HTMLReportGenerator.noCommentsOnRemovals = true;
                            else if (level.compareTo("add") == 0)
                                HTMLReportGenerator.noCommentsOnAdditions = true;
                            else if (level.compareTo("change") == 0)
                                HTMLReportGenerator.noCommentsOnChanges = true;
                            else if (level.compareTo("all") == 0) {
                                HTMLReportGenerator.noCommentsOnRemovals = true;
                                HTMLReportGenerator.noCommentsOnAdditions = true;
                                HTMLReportGenerator.noCommentsOnChanges = true;
                            }
                        }
                    }
                    break;
                case "-checkcomments":
                    APIHandler.checkIsSentence = true;
                    break;
                case "-retainnonprinting":
                    RootDocToXML.stripNonPrintables = false;
                    break;
                case "-excludetag":
                    if (option.length < 2) {
                        err.msg("No exclude tag specified after -excludetag option.");
                    } else {
                        RootDocToXML.excludeTag = option[1];
                        RootDocToXML.excludeTag = RootDocToXML.excludeTag.trim();
                        RootDocToXML.doExclude = true;
                    }
                    break;
                case "-stats":
                    HTMLReportGenerator.doStats = true;
                    break;
                case "-doctitle":
                    if (option.length < 2) {
                        err.msg("No HTML text specified after -doctitle option.");
                    } else {
                        HTMLReportGenerator.docTitle = option[1];
                    }
                    break;
                case "-windowtitle":
                    if (option.length < 2) {
                        err.msg("No text specified after -windowtitle option.");
                    } else {
                        HTMLReportGenerator.windowTitle = option[1];
                    }
                    break;
                case "-charset":
                    if (option.length < 2) {
                        err.msg("No charset specified after -charset option.");
                    } else {
                        JDiff.charset = option[1];
                    }
                    break;
                case "-version":
                    System.out.println("JDiff version: " + JDiff.version);
                    System.exit(0);
                    break;
                case "-help":
                    usage();
                    System.exit(0);
                    break;
            }
        }
        if (!JDiff.writeXML && !JDiff.compareAPIs) {
            err.msg("First use the -apiname option to generate an XML file for one API.");
            err.msg("Then use the -apiname option again to generate another XML file for a different version of the API.");
            err.msg("Finally use the -oldapi option and -newapi option to generate a report about how the APIs differ.");
        }
        return err.noErrorsFound;
    }// validOptions()

    /**
     * Display the arguments for JDiff.
     */
    public static void usage() {
        System.err.println("JDiff version: " + JDiff.version);
        System.err.println();
        System.err.println("Valid JDiff arguments:");
        System.err.println();
        System.err.println("  -apiname <Name of a version>");
        System.err.println("  -oldapi <Name of a version>");
        System.err.println("  -newapi <Name of a version>");

        System.err.println("  Optional Arguments");
        System.err.println();
        System.err.println("  -d <directory> Destination directory for output HTML files");
        System.err.println("  -apidir <directory> Destination directory for the XML file generated with the '-apiname' argument.");
        System.err.println("  -oldapidir <directory> Location of the XML file for the old API");
        System.err.println("  -newapidir <directory> Location of the XML file for the new API");
        System.err.println("  -sourcepath <location of Java source files>");
        System.err.println("  -javadocnew <location of existing Javadoc files for the new API>");
        System.err.println("  -javadocold <location of existing Javadoc files for the old API>");

        System.err.println("  -baseURI <base> Use \"base\" as the base location of the various DTDs and Schemas used by JDiff");
        System.err.println("  -excludeclass [public|protected|package|private] Exclude classes which are not public, protected etc");
        System.err.println("  -excludemember [public|protected|package|private] Exclude members which are not public, protected etc");

        System.err.println("  -firstsentence Save only the first sentence of each comment block with the API.");
        System.err.println("  -docchanges Report changes in Javadoc comments between the APIs");
        System.err.println("  -incompatible Only report incompatible changes");
        System.err.println("  -nosuggest [all|remove|add|change] Do not add suggested comments to all, or the removed, added or chabged sections");
        System.err.println("  -checkcomments Check that comments are sentences");
        System.err.println("  -stripnonprinting Remove non-printable characters from comments.");
        System.err.println("  -excludetag <tag> Define the Javadoc tag which implies exclusion");
        System.err.println("  -stats Generate statistical output");
        System.err.println("  -help       (generates this output)");
        System.err.println();
        System.err.println("For more help, see jdiff.html");
    }
}
