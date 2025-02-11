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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Emit HTML indexes which appear in the bottom left frame in the report.
 * All indexes are links to JDiff-generated pages.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class HTMLIndexes {

    /**
     * Whether to log all missing @since tags to a file or not.
     * If false, just warn the user.
     */
    public static boolean logMissingSinces = true;

    /**
     * The file used to output details of missing @since tags.
     */
    public static PrintWriter missingSincesFile;

    /**
     * The number of non-breaking spaces to indent a duplicate indexes'
     * entries by.
     */
    private final int INDENT_SIZE = 2;

    /**
     * The HTMLReportGenerator instance used to write HTML.
     */
    private final HTMLReportGenerator h_;

    /**
     * The list of all changes for all program elements.
     */
    private List<Index> allNames;

    /**
     * The list of all package changes.
     */
    private List<Index> packageNames;

    /**
     * The list of all class changes.
     */
    private List<Index> classNames;

    /**
     * The list of all constructor changes.
     */
    private List<Index> ctorNames;

    /**
     * The list of all method changes.
     */
    private List<Index> methNames;

    /**
     * The list of all field changes.
     */
    private List<Index> fieldNames;

    /**
     * If set, then use allNames to generate the letter indexes.
     */
    private boolean isAllNames;

    /**
     * Set if there was at least one removal in the entire API.
     */
    private boolean atLeastOneRemoval;

    /**
     * Set if there was at least one addition in the entire API.
     */
    private boolean atLeastOneAddition;

    /**
     * Set if there was at least one change in the entire API.
     */
    private boolean atLeastOneChange;

    /**
     * Constructor.
     */
    public HTMLIndexes(HTMLReportGenerator h) {
        h_ = h;
    }

    /**
     * Emit all the bottom left frame index files.
     */
    public void emitAllBottomLeftFiles(String packagesIndexName,
                                       String classesIndexName,
                                       String constructorsIndexName,
                                       String methodsIndexName,
                                       String fieldsIndexName,
                                       String allDiffsIndexName,
                                       APIDiff apiDiff) {

        // indexType values: 0 = removals only, 1 = additions only,
        // 2 = changes only. 3 = all differences. Run all differences
        // first for all program element types so we know whether there
        // are any removals etc for the allDiffs index.
        emitBottomLeftFile(packagesIndexName, apiDiff, 3, "Package");
        emitBottomLeftFile(classesIndexName, apiDiff, 3, "Class");
        emitBottomLeftFile(constructorsIndexName, apiDiff, 3, "Constructor");
        emitBottomLeftFile(methodsIndexName, apiDiff, 3, "Method");
        emitBottomLeftFile(fieldsIndexName, apiDiff, 3, "Field");
        // The allindex must be done last, since it uses the results from
        // the previous ones
        emitBottomLeftFile(allDiffsIndexName, apiDiff, 3, "All");
        // Now generate the other indexes
        for (int indexType = 0; indexType < 3; indexType++) {
            emitBottomLeftFile(packagesIndexName, apiDiff, indexType, "Package");
            emitBottomLeftFile(classesIndexName, apiDiff, indexType, "Class");
            emitBottomLeftFile(constructorsIndexName, apiDiff, indexType, "Constructor");
            emitBottomLeftFile(methodsIndexName, apiDiff, indexType, "Method");
            emitBottomLeftFile(fieldsIndexName, apiDiff, indexType, "Field");
            emitBottomLeftFile(allDiffsIndexName, apiDiff, indexType, "All");
        }
        if (missingSincesFile != null)
            missingSincesFile.close();
    }

    /**
     * Emit a single bottom left frame with the given kind of differences for
     * the given program element type in an alphabetical index.
     *
     * @param indexBaseName      The base name of the index file.
     * @param apiDiff            The root element containing all the API differences.
     * @param indexType          0 = removals only, 1 = additions only,
     *                           2 = changes only, 3 = all differences,
     * @param programElementType "Package", "Class", "Constructor",
     *                           "Method", "Field" or "All".
     */
    public void emitBottomLeftFile(String indexBaseName,
                                   APIDiff apiDiff, int indexType,
                                   String programElementType) {
        String filename = indexBaseName;
        String title = "JDiff";
        if (indexType == 0) {
            filename += "_removals" + HTMLReportGenerator.reportFileExt;
            title = programElementType + " Removals Index";
        } else if (indexType == 1) {
            filename += "_additions" + HTMLReportGenerator.reportFileExt;
            title = programElementType + " Additions Index";
        } else if (indexType == 2) {
            filename += "_changes" + HTMLReportGenerator.reportFileExt;
            title = programElementType + " Changes Index";
        } else if (indexType == 3) {
            filename += "_all" + HTMLReportGenerator.reportFileExt;
            title = programElementType + " Differences Index";
        }

        try (FileOutputStream fos = new FileOutputStream(filename);
             PrintWriter writer = new PrintWriter(fos)
        ) {
            HTMLReportGenerator.reportFile = writer;

            h_.writeStartHTMLHeader();
            h_.writeHTMLTitle(title);
            h_.writeStyleSheetRef();
            h_.writeText("</HEAD>");
            h_.writeText("<BODY>");

            if (programElementType.compareTo("Package") == 0) {
                emitPackagesIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Class") == 0) {
                emitClassesIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Constructor") == 0) {
                emitConstructorsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Method") == 0) {
                emitMethodsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Field") == 0) {
                emitFieldsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("All") == 0) {
                emitAllDiffsIndex(apiDiff, indexType);
            } else {
                System.out.println("Error: unknown program element type.");
                System.exit(3);
            }

            h_.writeHTMLFooter();
        } catch (IOException e) {
            System.out.println("IO Error while attempting to create " + filename);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Generate a small header of letters which link to each section, but
     * do not emit a linked letter for the current section. Finish the list off
     * with a link to the top of the index.
     * Caching the results of this function would save about 10s with large APIs.
     */
    private void generateLetterIndex(List<Index> list, char currChar, @SuppressWarnings("SameParameterValue") boolean larger) {
        if (larger)
            return; // Currently not using the larger functionality
        int size = -2;
        //noinspection ConstantConditions
        if (larger)
            size = -1;
        char oldsw = '\0';
        for (Index entry : isAllNames ? allNames : list) {
            char sw = entry.name_.charAt(0);
            char swu = Character.toUpperCase(sw);
            if (swu != Character.toUpperCase(oldsw)) {
                // Don't emit a reference to the current letter
                if (Character.toUpperCase(sw) != Character.toUpperCase(currChar)) {
                    if (swu == '_') {
                        h_.writeText("<a href=\"#" + swu + "\"><font size=\"" + size + "\">" + "underscore" + "</font></a> ");
                    } else {
                        h_.writeText("<a href=\"#" + swu + "\"><font size=\"" + size + "\">" + swu + "</font></a> ");
                    }
                }
                oldsw = sw;
            }
        }
        h_.writeText(" <a href=\"#topheader\"><font size=\"" + size + "\">TOP</font></a>");
        h_.writeText("<br>");
    }

    /**
     * Emit a header for an index, including suitable links for removed,
     * added and changes sub-indexes.
     */
    private void emitIndexHeader(String indexName, int indexType,
                                 boolean hasRemovals,
                                 boolean hasAdditions, boolean hasChanges) {
        String linkIndexName = indexName.toLowerCase();
        boolean isAllDiffs = false;
        if (indexName.compareTo("All Differences") == 0) {
            linkIndexName = "alldiffs";
            isAllDiffs = true;
        }
        h_.writeText("<a NAME=\"topheader\"></a>"); // Named anchor
        h_.writeText("<table summary=\"Index for " + indexName + "\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("  <tr>");
        h_.writeText("  <td bgcolor=\"#FFFFCC\">");
        // The index name is also a hidden link to the *index_all page
        if (isAllDiffs)
            h_.writeText("<font size=\"+1\"><a href=\"" + linkIndexName + "_index_all" + HTMLReportGenerator.reportFileExt + "\" class=\"staysblack\">" + indexName + "</a></font>");
        else
            h_.writeText("<font size=\"+1\"><a href=\"" + linkIndexName + "_index_all" + HTMLReportGenerator.reportFileExt + "\" class=\"staysblack\">All " + indexName + "</a></font>");
        h_.writeText("  </td>");
        h_.writeText("  </tr>");

        h_.writeText("  <tr>");
        h_.writeText("  <td bgcolor=\"#FFFFFF\">");
        h_.writeText("  <FONT SIZE=\"-1\">");
        if (hasRemovals) {
            if (indexType == 0) {
                h_.writeText("<b>Removals</b>");
            } else {
                h_.writeText("<A HREF=\"" + linkIndexName + "_index_removals" + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">Removals</A>");
            }
        } else {
            h_.writeText("<font color=\"#999999\">Removals</font>");
        }
        h_.writeText("  </FONT>");
        h_.writeText("  </td>");
        h_.writeText("  </tr>");

        h_.writeText("  <tr>");
        h_.writeText("  <td bgcolor=\"#FFFFFF\">");
        h_.writeText("  <FONT SIZE=\"-1\">");
        if (hasAdditions) {
            if (indexType == 1) {
                h_.writeText("<b>Additions</b>");
            } else {
                h_.writeText("<A HREF=\"" + linkIndexName + "_index_additions" + HTMLReportGenerator.reportFileExt + "\"class=\"hiddenlink\">Additions</A>");
            }
        } else {
            h_.writeText("<font color=\"#999999\">Additions</font>");
        }
        h_.writeText("  </FONT>");
        h_.writeText("  </td>");
        h_.writeText("  </tr>");

        h_.writeText("  <tr>");
        h_.writeText("  <td bgcolor=\"#FFFFFF\">");
        h_.writeText("  <FONT SIZE=\"-1\">");
        if (hasChanges) {
            if (indexType == 2) {
                h_.writeText("<b>Changes</b>");
            } else {
                h_.writeText("<A HREF=\"" + linkIndexName + "_index_changes" + HTMLReportGenerator.reportFileExt + "\"class=\"hiddenlink\">Changes</A>");
            }
        } else {
            h_.writeText("<font color=\"#999999\">Changes</font>");
        }
        h_.writeText("  </FONT>");
        h_.writeText("  </td>");
        h_.writeText("  </tr>");
        h_.writeText("  <tr>");
        h_.writeText("  <td>");
        h_.writeText("<font size=\"-2\"><b>Bold</b>&nbsp;is&nbsp;New,&nbsp;<strike>strike</strike>&nbsp;is&nbsp;deleted</font>");
        h_.writeText("  </td>");
        h_.writeText("  </tr>");
        h_.writeText("</table><br>");
    }

    /**
     * Emit the index of packages, which appears in the bottom left frame.
     */
    public void emitPackagesIndex(APIDiff apiDiff, int indexType) {
        // Add all the names of packages to a new list, to be sorted later
        packageNames = new ArrayList<>(); // Index[]
        boolean hasRemovals = apiDiff.packagesRemoved.size() != 0;
        boolean hasAdditions = apiDiff.packagesAdded.size() != 0;
        boolean hasChanges = apiDiff.packagesChanged.size() != 0;
        recordDiffs(hasRemovals, hasAdditions, hasChanges);
        if (indexType == 3 || indexType == 0) {
            for (PackageAPI pkg : apiDiff.packagesRemoved) {
                packageNames.add(new Index(pkg.name_, 0));
            }
        }
        if (indexType == 3 || indexType == 1) {
            for (PackageAPI pkg : apiDiff.packagesAdded) {
                packageNames.add(new Index(pkg.name_, 1));
            }
        }
        if (indexType == 3 || indexType == 2) {
            for (PackageDiff pkg : apiDiff.packagesChanged) {
                packageNames.add(new Index(pkg.name_, 2));
            }
        }
        Collections.sort(packageNames);

        // No letter index needed for packages

        // Now emit all the package names and links to their respective files
        emitIndexHeader("Packages", indexType, hasRemovals, hasAdditions, hasChanges);

        // Extra line because no index is emitted
        h_.writeText("<br>");

        // Package names are unique, so no need to check for duplicates.
        char oldsw = '\0';
        for (Index pkg : packageNames) {
            oldsw = emitPackageIndexEntry(pkg, oldsw);
        }
    }

    /**
     * Emit an index entry for a package.
     * Package names are unique, so no need to check for duplicates.
     */
    public char emitPackageIndexEntry(Index pkg, char oldsw) {
        char res = oldsw;
        // See if we are in a new section of the alphabet
        char sw = pkg.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            // No need to emit section letters for packages
            res = sw;
            // Add the named anchor for this new letter
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
        }
        // Package names are unique, so no need to check for duplicates.
        if (pkg.changeType_ == 0) {
            h_.writeText("<A HREF=\"" + HTMLReportGenerator.reportFileName + "-summary" + HTMLReportGenerator.reportFileExt + "#" + pkg.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + pkg.name_ + "</strike></A><br>");
        } else if (pkg.changeType_ == 1) {
            h_.writeText("<A HREF=\"" + HTMLReportGenerator.reportFileName + "-summary" + HTMLReportGenerator.reportFileExt + "#" + pkg.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + pkg.name_ + "</b></A><br>");
        } else if (pkg.changeType_ == 2) {
            h_.writeText("<A HREF=\"pkg_" + pkg.name_ + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\">" + pkg.name_ + "</A><br>");
        }
        return res;
    }

    /**
     * Emit all the entries and links for the given iterator
     * to their respective files.
     */
    public void emitIndexEntries(Iterator<Index> iter) {
        char oldsw = '\0';
        int multipleMarker = 0;
        Index currIndex = null; // The entry which is emitted
        while (iter.hasNext()) {
            // The next entry after the current one
            Index nextIndex = (iter.next());
            if (currIndex == null) {
                currIndex = nextIndex; // Prime the pump
            } else {
                if (nextIndex.name_.compareTo(currIndex.name_) == 0) {
                    // It's a duplicate index, so emit the name and then
                    // the indented entries
                    if (multipleMarker == 0)
                        multipleMarker = 1; // Start of a duplicate index
                    else if (multipleMarker == 1)
                        multipleMarker = 2; // Inside a duplicate index
                    oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
                } else {
                    if (multipleMarker == 1)
                        multipleMarker = 2; // Inside a duplicate index
                    oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
                    multipleMarker = 0; // Not in a duplicate index any more
                }
                currIndex = nextIndex;
            }
        }
        // Emit the last entry left in currIndex
        if (multipleMarker == 1)
            multipleMarker = 2; // Inside a duplicate index
        if (currIndex != null)
            oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
    }

    /**
     * Emit elements in the given iterator which were added and
     * missing @since tags.
     */
    public void emitMissingSinces(Iterator<Index> iter) {
//        if (!logMissingSinces)
//            return;
        if (missingSincesFile == null) {
            String sinceFileName = HTMLReportGenerator.outputDir + JDiff.DIR_SEP + "missingSinces.txt";
            try {
                FileOutputStream fos = new FileOutputStream(sinceFileName);
                missingSincesFile = new PrintWriter(fos);
            } catch (IOException e) {
                System.out.println("IO Error while attempting to create " + sinceFileName);
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }
        while (iter.hasNext()) {
            Index currIndex = (iter.next());
            // Only display information about added elements
            if (currIndex.changeType_ != 1)
                continue;
            String programElementType = currIndex.ename_;
            String details = null;
            if (programElementType.compareTo("class") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.name_;
                if (currIndex.isInterface_)
                    details = details + " Interface";
                else
                    details = details + " Class";
            } else if (programElementType.compareTo("constructor") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.name_ + " Constructor (" + currIndex.type_ + ")";
            } else if (programElementType.compareTo("method") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.className_ + " " + "Method " + currIndex.name_ + "(" + currIndex.type_ + ")";
            } else if (programElementType.compareTo("field") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.className_ + " " + "Field " + currIndex.name_;
            } else {
                System.out.println("Error: unknown program element type");
                System.exit(3);
            }
            if (currIndex.doc_ == null) {
                if (logMissingSinces)
                    missingSincesFile.println("NO DOC BLOCK: " + details);
                else
                    System.out.println("Warning: the doc block for the new element: " + details + " is missing, so there is no @since tag");
            } else if (currIndex.doc_.contains("@since")) {
                if (logMissingSinces)
                    missingSincesFile.println("OK: " + details);
            } else {
                if (logMissingSinces)
                    missingSincesFile.println("MISSING @SINCE TAG: " + details);
                else
                    System.out.println("Warning: the doc block for the new element: " + details + " is missing an @since tag");
            }
        }
    }

    /**
     * Emit a single entry and the link to its file.
     *
     * @param currIndex "Class", "Constructor",
     *                  "Method", or "Field".
     */
    public char emitIndexEntry(Index currIndex, char oldsw, int multipleMarker) {
        String programElementType = currIndex.ename_;
        if (programElementType.compareTo("class") == 0) {
            return emitClassIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("constructor") == 0) {
            return emitCtorIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("method") == 0) {
            return emitMethodIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("field") == 0) {
            return emitFieldIndexEntry(currIndex, oldsw, multipleMarker);
        } else {
            System.out.println("Error: unknown program element type");
            System.exit(3);
        }
        return '\0';
    }

    /**
     * Emit the index of classes, which appears in the bottom left frame.
     */
    public void emitClassesIndex(APIDiff apiDiff, int indexType) {
        // Add all the names of classes to a new list, to be sorted later
        classNames = new ArrayList<>(); // Index[]
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        for (PackageDiff pkgDiff : apiDiff.packagesChanged) {
            if (pkgDiff.classesRemoved.size() != 0)
                hasRemovals = true;
            if (pkgDiff.classesAdded.size() != 0)
                hasAdditions = true;
            if (pkgDiff.classesChanged.size() != 0)
                hasChanges = true;
            recordDiffs(hasRemovals, hasAdditions, hasChanges);
            String pkgName = pkgDiff.name_;
            if (indexType == 3 || indexType == 0) {
                for (ClassAPI cls : pkgDiff.classesRemoved) {
                    classNames.add(new Index(cls.name_, 0, pkgName, cls.isInterface_));
                }
            }
            if (indexType == 3 || indexType == 1) {
                for (ClassAPI cls : pkgDiff.classesAdded) {
                    Index idx = new Index(cls.name_, 1, pkgName, cls.isInterface_);
                    idx.doc_ = cls.doc_; // Used for checking @since
                    classNames.add(idx);
                }
            }
            if (indexType == 3 || indexType == 2) {
                for (ClassDiff cls : pkgDiff.classesChanged) {
                    classNames.add(new Index(cls.name_, 2, pkgName, cls.isInterface_));
                }
            }
        }
        Collections.sort(classNames);
        emitIndexHeader("Classes", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(classNames.iterator());
        if (indexType == 1)
            emitMissingSinces(classNames.iterator());
    }

    /**
     * Emit an index entry for a class.
     */
    public char emitClassIndexEntry(Index cls, char oldsw,
                                    int multipleMarker) {
        char res = oldsw;
        String className = cls.pkgName_ + "." + cls.name_;
        String classRef = cls.pkgName_ + "." + cls.name_;
        boolean isInterface = cls.isInterface_;
        // See if we are in a new section of the alphabet
        char sw = cls.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            // Add the named anchor for this new letter
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(classNames, sw, false);
        }
        // Deal with displaying duplicate indexes
        if (multipleMarker == 1) {
            h_.writeText("<i>" + cls.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        if (cls.changeType_ == 0) {
            // Emit a reference to the correct place for the class in the
            // JDiff page for the package
            h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + HTMLReportGenerator.reportFileExt +
                    "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + cls.name_ + "</strike></A><br>");
        } else if (cls.changeType_ == 1) {
            String cn = cls.name_;
            if (multipleMarker != 0)
                cn = cls.pkgName_;
            if (isInterface)
                h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + HTMLReportGenerator.reportFileExt + "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><b><i>" + cn + "</i></b></A><br>");
            else
                h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + HTMLReportGenerator.reportFileExt + "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + cn + "</b></A><br>");
        } else if (cls.changeType_ == 2) {
            String cn = cls.name_;
            if (multipleMarker != 0)
                cn = cls.pkgName_;
            if (isInterface)
                h_.writeText("<A HREF=\"" + classRef + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\"><i>" + cn + "</i></A><br>");
            else
                h_.writeText("<A HREF=\"" + classRef + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\">" + cn + "</A><br>");
        }
        return res;
    }

    /**
     * Emit the index of all constructors, which appears in the bottom left
     * frame.
     */
    public void emitConstructorsIndex(APIDiff apiDiff, int indexType) {
        // Add all the names of constructors to a new list, to be sorted later
        ctorNames = new ArrayList<>(); // Index[]
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        for (PackageDiff pkgDiff : apiDiff.packagesChanged) {
            String pkgName = pkgDiff.name_;
            for (ClassDiff classDiff : pkgDiff.classesChanged) {
                if (classDiff.ctorsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.ctorsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.ctorsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                if (indexType == 3 || indexType == 0) {
                    for (ConstructorAPI ctor : classDiff.ctorsRemoved) {
                        ctorNames.add(new Index(className, 0, pkgName, ctor.type_));
                    }
                }
                if (indexType == 3 || indexType == 1) {
                    for (ConstructorAPI ctor : classDiff.ctorsAdded) {
                        Index idx = new Index(className, 1, pkgName, ctor.type_);
                        idx.doc_ = ctor.doc_; // Used for checking @since
                        ctorNames.add(idx);
                    }
                }
                if (indexType == 3 || indexType == 2) {
                    for (MemberDiff ctor : classDiff.ctorsChanged) {
                        ctorNames.add(new Index(className, 2, pkgName, ctor.newType_));
                    }
                }
            }
        }
        Collections.sort(ctorNames);
        emitIndexHeader("Constructors", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(ctorNames.iterator());
        if (indexType == 1)
            emitMissingSinces(ctorNames.iterator());
    }

    /**
     * Emit an index entry for a constructor.
     */
    public char emitCtorIndexEntry(Index ctor, char oldsw, int multipleMarker) {
        char res = oldsw;
        String className = ctor.pkgName_ + "." + ctor.name_;
        String memberRef = ctor.pkgName_ + "." + ctor.name_;
        String type = ctor.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        // See if we are in a new section of the alphabet
        char sw = ctor.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            // Add the named anchor for this new letter
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(ctorNames, sw, false);
        }
        // Deal with displaying duplicate indexes
        if (multipleMarker == 1) {
            h_.writeText("<i>" + ctor.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        // Deal with each type of difference
        // The output displayed for unique or duplicate entries is the same
        // for constructors.
        if (ctor.changeType_ == 0) {
            String commentID = className + ".ctor_removed(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + ctor.name_ + "</strike>");
            HTMLReportGenerator.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        } else if (ctor.changeType_ == 1) {
            String commentID = className + ".ctor_added(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + ctor.name_ + "</b>");
            HTMLReportGenerator.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        } else if (ctor.changeType_ == 2) {
            String commentID = className + ".ctor_changed(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + ctor.name_);
            HTMLReportGenerator.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        }
        return res;
    }

    /**
     * Emit the index of all methods, which appears in the bottom left frame.
     */
    public void emitMethodsIndex(APIDiff apiDiff, int indexType) {
        // Add all the names of methods to a new list, to be sorted later
        methNames = new ArrayList<>(); // Index[]
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        for (PackageDiff pkgDiff : apiDiff.packagesChanged) {
            String pkgName = pkgDiff.name_;
            for (ClassDiff classDiff : pkgDiff.classesChanged) {
                if (classDiff.methodsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.methodsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.methodsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                if (indexType == 3 || indexType == 0) {
                    for (MethodAPI meth : classDiff.methodsRemoved) {
                        methNames.add(new Index(meth.name_, 0, pkgName, className, meth.getSignature()));
                    }
                }
                if (indexType == 3 || indexType == 1) {
                    for (MethodAPI meth : classDiff.methodsAdded) {
                        Index idx = new Index(meth.name_, 1, pkgName, className, meth.getSignature());
                        idx.doc_ = meth.doc_; // Used for checking @since
                        methNames.add(idx);
                    }
                }
                if (indexType == 3 || indexType == 2) {
                    for (MemberDiff meth : classDiff.methodsChanged) {
                        methNames.add(new Index(meth.name_, 2, pkgName, className, meth.newSignature_));
                    }
                }
            }
        }
        Collections.sort(methNames);
        emitIndexHeader("Methods", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(methNames.iterator());
        if (indexType == 1)
            emitMissingSinces(methNames.iterator());
    }

    /**
     * Emit an index entry for a method.
     */
    public char emitMethodIndexEntry(Index meth, char oldsw,
                                     int multipleMarker) {
        char res = oldsw;
        String className = meth.pkgName_ + "." + meth.className_;
        String memberRef = meth.pkgName_ + "." + meth.className_;
        String type = meth.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        // See if we are in a new section of the alphabet
        char sw = meth.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            // Add the named anchor for this new letter
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(methNames, sw, false);
        }
        // Deal with displaying duplicate indexes
        if (multipleMarker == 1) {
            h_.writeText("<i>" + meth.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        // Deal with each type of difference
        if (meth.changeType_ == 0) {
            String commentID = className + "." + meth.name_ + "_removed(" + type + ")";
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + meth.name_ + "</strike>");
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;<strike>");
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
                h_.writeText("</strike>&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        } else if (meth.changeType_ == 1) {
            String commentID = className + "." + meth.name_ + "_added(" + type + ")";
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + meth.name_ + "</b>");
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;<b>");
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
                h_.writeText("</b>&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        } else if (meth.changeType_ == 2) {
            String commentID = className + "." + meth.name_ + "_changed(" + type + ")";
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + meth.name_);
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;");
                HTMLReportGenerator.emitTypeWithParens(shownType, false);
                h_.writeText("&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        }
        return res;
    }

    /**
     * Emit the index of all fields, which appears in the bottom left frame.
     */
    public void emitFieldsIndex(APIDiff apiDiff, int indexType) {
        // Add all the names of fields to a new list, to be sorted later
        fieldNames = new ArrayList<>(); // Index[]
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        for (PackageDiff pkgDiff : apiDiff.packagesChanged) {
            String pkgName = pkgDiff.name_;
            for (ClassDiff classDiff : pkgDiff.classesChanged) {
                if (classDiff.fieldsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.fieldsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.fieldsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                if (indexType == 3 || indexType == 0) {
                    for (FieldAPI fld : classDiff.fieldsRemoved) {
                        fieldNames.add(new Index(fld.name_, 0, pkgName, className, fld.type_, true));
                    }
                }
                if (indexType == 3 || indexType == 1) {
                    for (FieldAPI fld : classDiff.fieldsAdded) {
                        Index idx = new Index(fld.name_, 1, pkgName, className, fld.type_, true);
                        idx.doc_ = fld.doc_; // Used for checking @since
                        fieldNames.add(idx);
                    }
                }
                if (indexType == 3 || indexType == 2) {
                    for (MemberDiff fld : classDiff.fieldsChanged) {
                        fieldNames.add(new Index(fld.name_, 2, pkgName, className, fld.newType_, true));
                    }
                }
            }
        }
        Collections.sort(fieldNames);
        emitIndexHeader("Fields", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(fieldNames.iterator());
        if (indexType == 1)
            emitMissingSinces(fieldNames.iterator());
    }

    /**
     * Emit an index entry for a field.
     */
    public char emitFieldIndexEntry(Index fld, char oldsw,
                                    int multipleMarker) {
        char res = oldsw;
        String className = fld.pkgName_ + "." + fld.className_;
        String memberRef = fld.pkgName_ + "." + fld.className_;
        String type = fld.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        // See if we are in a new section of the alphabet
        char sw = fld.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            // Add the named anchor for this new letter
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(fieldNames, sw, false);
        }
        // Deal with displaying duplicate indexes
        if (multipleMarker == 1) {
            h_.writeText("<i>" + fld.name_ + "</i><br>");
        }
        if (multipleMarker != 0) {
// More context than this is helpful here: h_.indent(INDENT_SIZE);
            h_.writeText("&nbsp;in&nbsp;");
        }
        // Deal with each type of difference
        if (fld.changeType_ == 0) {
            String commentID = className + "." + fld.name_;
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + fld.name_ + "</strike></A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + className + "</strike></A>");
                h_.writeText("</nobr><br>");
            }
        } else if (fld.changeType_ == 1) {
            String commentID = className + "." + fld.name_;
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + fld.name_ + "</A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + className + "</A>");
                h_.writeText("</nobr><br>");
            }
        } else if (fld.changeType_ == 2) {
            String commentID = className + "." + fld.name_;
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + fld.name_ + "</A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + HTMLReportGenerator.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + className + "</A>");
                h_.writeText("</nobr><br>");
            }
        }
        return res;
    }

    /**
     * Emit the index of all changes, which appears in the bottom left frame.
     * Has to be run after all the other indexes have been written, since it
     * uses data from when they are generated.
     */
    public void emitAllDiffsIndex(APIDiff apiDiff, int indexType) {
        allNames = new ArrayList<>(); // Index[]
        // Add all the changes into one big list, and sort it by name,
        // ignoring case
        allNames.addAll(packageNames);
        allNames.addAll(classNames);
        allNames.addAll(ctorNames);
        allNames.addAll(methNames);
        allNames.addAll(fieldNames);
        // Compares two Index objects' names, ignoring case differences.
        Collections.sort(allNames);

        emitIndexHeader("All Differences", indexType, atLeastOneRemoval,
                atLeastOneAddition, atLeastOneChange);

        // Tell generateLetterIndex to use allNames as the list when
        // using the other methods to generate the indexes.
        isAllNames = true;

        // Now emit a line for each entry in the list in the appropriate
        // format for each program element
        Iterator<Index> iter = allNames.iterator();
        char oldsw = '\0';
        int multipleMarker = 0;
        Index currIndex = null; // The entry which is emitted
        while (iter.hasNext()) {
            // The next entry after the current one
            Index nextIndex = (iter.next());
            if (currIndex == null) {
                currIndex = nextIndex; // Prime the pump
            } else {
                if (nextIndex.name_.compareTo(currIndex.name_) == 0) {
                    // It's a duplicate index, so emit the name and then
                    // the indented entries
                    if (multipleMarker == 0)
                        multipleMarker = 1; // Start of a duplicate index
                    else if (multipleMarker == 1)
                        multipleMarker = 2; // Inside a duplicate index
                    oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);
                } else {
                    if (multipleMarker == 1)
                        multipleMarker = 2; // Inside a duplicate index
                    oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);
                    multipleMarker = 0; // Not in a duplicate index any more
                }
                currIndex = nextIndex;
            }
        }
        // Emit the last entry left in currIndex
        if (multipleMarker == 1)
            multipleMarker = 2; // Inside a duplicate index
        if (currIndex != null)
            oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);

        // Tell generateLetterIndex to stop using allNames as the list when
        // using the other methods to generate the indexes.
        isAllNames = false;
    }

    /**
     * Call the appropriate *IndexEntry method for each entry.
     */
    public char emitIndexEntryForAny(Index currIndex, char oldsw,
                                     int multipleMarker) {
        if (currIndex.ename_.compareTo("package") == 0) {
            h_.writeText("<!-- Package " + currIndex.name_ + " -->");
            return emitPackageIndexEntry(currIndex, oldsw);
        } else if (currIndex.ename_.compareTo("class") == 0) {
            h_.writeText("<!-- Class " + currIndex.name_ + " -->");
            return emitClassIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("constructor") == 0) {
            h_.writeText("<!-- Constructor " + currIndex.name_ + " -->");
            return emitCtorIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("method") == 0) {
            h_.writeText("<!-- Method " + currIndex.name_ + " -->");
            return emitMethodIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("field") == 0) {
            h_.writeText("<!-- Field " + currIndex.name_ + " -->");
            return emitFieldIndexEntry(currIndex, oldsw, multipleMarker);
        }
        return '\0';
    }

    /**
     * If any of the parameters are set, then set the respective atLeastOne
     * variable, used to generate the links at the top of the allDiffs index.
     * Never unset an atLeastOne variable.
     */
    private void recordDiffs(boolean hasRemovals, boolean hasAdditions,
                             boolean hasChanges) {
        if (hasRemovals)
            atLeastOneRemoval = true;
        if (hasAdditions)
            atLeastOneAddition = true;
        if (hasChanges)
            atLeastOneChange = true;
    }
}

/**
 * Class used to produce indexes of packages and classes.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class Index implements Comparable<Index> {

    /**
     * The name of the program element this Index object represents.
     */
    public String ename_;

    /**
     * Name of the changed package, class or member.
     */
    public String name_;

    /**
     * Type of change. 0 = remove, 1 = add, 2 = change.
     */
    public int changeType_;

    /**
     * Name of the changed package if name_ is a class name.
     */
    public String pkgName_;

    /**
     * Set if this class is an interface.
     */
    public boolean isInterface_;

    /**
     * The doc block of added elements, default is null.
     */
    public String doc_;

    /**
     * The new member type. For methods, this is the signature.
     */
    public String type_;

    /**
     * The class name. Only used by methods.
     */
    public String className_;

    /**
     * Constructor for packages.
     */
    public Index(String name, int changeType) {
        ename_ = "package";
        name_ = name;
        changeType_ = changeType;
    }

    /**
     * Constructor for classes.
     */
    public Index(String name, int changeType, String pkgName, boolean isInterface) {
        ename_ = "class";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        isInterface_ = isInterface;
    }

    /**
     * Constructor for constructors.
     */
    public Index(String name, int changeType, String pkgName, String type) {
        ename_ = "constructor";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        type_ = type;
    }

    /**
     * Constructor for methods.
     */
    public Index(String name, int changeType, String pkgName,
                 String className, String type) {
        ename_ = "method";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        className_ = className;
        type_ = type;
    }

    /**
     * Constructor for fields.
     * <p>
     * The boolean <code>fld</code> is simply there to differentiate this
     * constructor from the one for methods.
     */
    public Index(String name, int changeType, String pkgName,
                 String className, String type, boolean fld) {
        ename_ = "field";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        className_ = className;
        type_ = type;
    }


    /**
     * Compare two Index objects by their simple names, ignoring case.
     */
    public int compareTo(Index o) {
        return name_.compareToIgnoreCase(o.name_);
    }

}

