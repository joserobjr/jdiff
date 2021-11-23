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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.*;

/**
 * Creates a Comments from an XML file. The Comments object is the internal
 * representation of the comments for the changes.
 * All methods in this class for populating a Comments object are static.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class Comments {

    /**
     * The text placed into XML comments file where there is no comment yet.
     * It never appears in reports.
     */
    public static final String placeHolderText = "InsertCommentsHere";

    /**
     * All the possible comments known about, accessible by the commentID.
     */
    public static Hashtable<String, String> allPossibleComments = new Hashtable<>();

    /**
     * The old Comments object which is populated from the file read in.
     */
    private static Comments oldComments_;

    /**
     * The file where the XML representing the new Comments object is stored.
     */
    private static PrintWriter outputFile = null;

    /**
     * The list of comments elements associated with this objects
     */
    public List<SingleComment> commentsList_ = new ArrayList<>();

//
// Methods to add data to a Comments object. Called by the XML parser and the 
// report generator.
//

    /**
     * Read the file where the XML for comments about the changes between
     * the old API and new API is stored and create a Comments object for
     * it. The Comments object may be null if no file exists.
     */
    public static Comments readFile(String filename) {
        // If validation is desired, write out the appropriate comments.xsd
        // file in the same directory as the comments XML file.
        if (XMLToAPI.validateXML) {
            writeXSD(filename);
        }

        // If the file does not exist, return null
        File f = new File(filename);
        if (!f.exists())
            return null;

        // The instance of the Comments object which is populated from the file.
        oldComments_ = new Comments();
        try {
            DefaultHandler handler = new CommentsHandler(oldComments_);
            XMLReader parser = null;
            try {
                String parserName = System.getProperty("org.xml.sax.driver");
                if (parserName == null) {
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                } else {
                    // Let the underlying mechanisms try to work out which
                    // class to instantiate
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                }
            } catch (SAXException saxe) {
                System.out.println("SAXException: " + saxe);
                saxe.printStackTrace();
                System.exit(1);
            }

            if (XMLToAPI.validateXML) {
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            }
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            try (FileInputStream fis = new FileInputStream(filename)) {
                parser.parse(new InputSource(new BufferedInputStream(fis)));
            }
        } catch (org.xml.sax.SAXNotRecognizedException snre) {
            System.out.println("SAX Parser does not recognize feature: " + snre);
            snre.printStackTrace();
            System.exit(1);
        } catch (org.xml.sax.SAXNotSupportedException snse) {
            System.out.println("SAX Parser feature is not supported: " + snse);
            snse.printStackTrace();
            System.exit(1);
        } catch (org.xml.sax.SAXException saxe) {
            System.out.println("SAX Exception parsing file '" + filename + "' : " + saxe);
            saxe.printStackTrace();
            System.exit(1);
        } catch (java.io.IOException ioe) {
            System.out.println("IOException parsing file '" + filename + "' : " + ioe);
            ioe.printStackTrace();
            System.exit(1);
        }

        Collections.sort(oldComments_.commentsList_);
        return oldComments_;
    } //readFile()

//
// Methods to get data from a Comments object. Called by the report generator
//

    /**
     * Write the XML Schema file used for validation.
     */
    public static void writeXSD(String filename) {
        String xsdFileName = filename;
        int idx = xsdFileName.lastIndexOf('\\');
        int idx2 = xsdFileName.lastIndexOf('/');
        if (idx == -1 && idx2 == -1) {
            xsdFileName = "";
        } else if (idx == -1) {
            xsdFileName = xsdFileName.substring(0, idx2 + 1);
        } else if (idx2 == -1) {
            xsdFileName = xsdFileName.substring(0, idx + 1);
        } else {
            int max = Math.max(idx2, idx);
            xsdFileName = xsdFileName.substring(0, max + 1);
        }
        xsdFileName += "comments.xsd";
        try (FileOutputStream fos = new FileOutputStream(xsdFileName);
             PrintWriter xsdFile = new PrintWriter(fos)
        ) {
            // The contents of the comments.xsd file
            xsdFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
            xsdFile.println("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
            xsdFile.println();
            xsdFile.println("<xsd:annotation>");
            xsdFile.println("  <xsd:documentation>");
            xsdFile.println("  Schema for JDiff comments.");
            xsdFile.println("  </xsd:documentation>");
            xsdFile.println("</xsd:annotation>");
            xsdFile.println();
            xsdFile.println("<xsd:element name=\"comments\" type=\"commentsType\"/>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentsType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"comment\" type=\"commentType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"jdversion\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"identifier\" type=\"identifierType\" minOccurs='1' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"text\" type=\"xsd:string\" minOccurs='1' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"identifierType\">");
            xsdFile.println("  <xsd:attribute name=\"id\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("</xsd:schema>");
        } catch (IOException e) {
            System.out.println("IO Error while attempting to create " + xsdFileName);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Return the comment associated with the given id in the Comment object.
     * If there is no such comment, return the placeHolderText.
     */
    public static String getComment(Comments comments, String id) {
        if (comments == null)
            return placeHolderText;
        SingleComment key = new SingleComment(id, null);
        int idx = Collections.binarySearch(comments.commentsList_, key);
        if (idx < 0) {
            return placeHolderText;
        } else {
            int startIdx = comments.commentsList_.indexOf(key);
            int endIdx = comments.commentsList_.indexOf(key);
            int numIdx = endIdx - startIdx + 1;
            if (numIdx != 1) {
                System.out.println("Warning: " + numIdx + " identical ids in the existing comments file. Using the first instance.");
            }
            SingleComment singleComment = comments.commentsList_.get(idx);
            // Convert @link tags to links
            return singleComment.text_;
        }
    }

    /**
     * Convert @link tags to HTML links.
     */
    public static String convertAtLinks(String text, String currentElement,
                                        PackageAPI pkg, ClassAPI cls) {
        if (text == null)
            return null;

        StringBuilder result = new StringBuilder();

        int state = -1;

        final int NORMAL_TEXT = -1;
        final int IN_LINK = 1;
        final int IN_LINK_IDENTIFIER = 2;
        final int IN_LINK_IDENTIFIER_REFERENCE = 3;
        final int IN_LINK_IDENTIFIER_REFERENCE_PARAMS = 6;
        final int IN_LINK_LINKTEXT = 4;
        final int END_OF_LINK = 5;

        StringBuilder identifier = null;
        StringBuilder identifierReference = null;
        StringBuilder linkText = null;

        // Figure out relative reference if required.
        String ref = "";
        if (currentElement.compareTo("class") == 0 ||
                currentElement.compareTo("interface") == 0) {
            ref = pkg.name_ + "." + cls.name_ + ".";
        } else if (currentElement.compareTo("package") == 0) {
            ref = pkg.name_ + ".";
        }
        ref = ref.replace('.', '/');

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char nextChar = i < text.length() - 1 ? text.charAt(i + 1) : (char) -1;
            int remainingChars = text.length() - i;

            switch (state) {
                case NORMAL_TEXT:
                    if (c == '{' && remainingChars >= 6) {
                        if ("{@link".equals(text.substring(i, i + 6))) {
                            state = IN_LINK;
                            identifier = null;
                            identifierReference = null;
                            linkText = null;
                            i += 5;
                            continue;
                        }
                    }
                    result.append(c);
                    break;
                case IN_LINK:
                    if (Character.isWhitespace(nextChar)) continue;
                    if (nextChar == '}') {
                        // End of the link
                        state = END_OF_LINK;
                    } else if (!Character.isWhitespace(nextChar)) {
                        state = IN_LINK_IDENTIFIER;
                    }
                    break;
                case IN_LINK_IDENTIFIER:
                    if (identifier == null) {
                        identifier = new StringBuilder();
                    }

                    if (c == '#') {
                        // We have a reference.
                        state = IN_LINK_IDENTIFIER_REFERENCE;
                        // Don't append #
                        continue;
                    } else if (Character.isWhitespace(c)) {
                        // We hit some whitespace: the next character is the beginning
                        // of the link text.
                        state = IN_LINK_LINKTEXT;
                        continue;
                    }
                    identifier.append(c);
                    // Check for a } that ends the link.
                    if (nextChar == '}') {
                        state = END_OF_LINK;
                    }
                    break;
                case IN_LINK_IDENTIFIER_REFERENCE:
                    if (identifierReference == null) {
                        identifierReference = new StringBuilder();
                    }
                    if (Character.isWhitespace(c)) {
                        state = IN_LINK_LINKTEXT;
                        continue;
                    }
                    identifierReference.append(c);

                    if (c == '(') {
                        state = IN_LINK_IDENTIFIER_REFERENCE_PARAMS;
                    }

                    if (nextChar == '}') {
                        state = END_OF_LINK;
                    }
                    break;
                case IN_LINK_IDENTIFIER_REFERENCE_PARAMS:
                    // We're inside the parameters of a reference. Spaces are allowed.
                    if (c == ')') {
                        state = IN_LINK_IDENTIFIER_REFERENCE;
                    }
                    identifierReference.append(c);
                    if (nextChar == '}') {
                        state = END_OF_LINK;
                    }
                    break;
                case IN_LINK_LINKTEXT:
                    if (linkText == null) linkText = new StringBuilder();

                    linkText.append(c);

                    if (nextChar == '}') {
                        state = END_OF_LINK;
                    }
                    break;
                case END_OF_LINK:
                    if (identifier != null) {
                        result.append("<A HREF=\"");
                        result.append(HTMLReportGenerator.newDocPrefix);
                        result.append(ref);
                        result.append(identifier.toString().replace('.', '/'));
                        result.append(".html");
                        if (identifierReference != null) {
                            result.append("#");
                            result.append(identifierReference);
                        }
                        result.append("\">");   // target=_top?

                        result.append("<TT>");
                        if (linkText != null) {
                            result.append(linkText);
                        } else {
                            result.append(identifier);
                            if (identifierReference != null) {
                                result.append(".");
                                result.append(identifierReference);
                            }
                        }
                        result.append("</TT>");
                        result.append("</A>");
                    }
                    state = NORMAL_TEXT;
                    break;
            }
        }
        return result.toString();
    }

    /**
     * Write the XML representation of comments to a file.
     *
     * @param outputFileName The name of the comments file.
     * @param newComments    The new comments on the changed APIs.
     * @return true if no problems encountered
     */
    public static boolean writeFile(String outputFileName,
                                    Comments newComments) {
        try (FileOutputStream fos = new FileOutputStream(outputFileName);
             PrintWriter writer = new PrintWriter(fos)
        ) {
            outputFile = writer;
            newComments.emitXMLHeader(outputFileName);
            newComments.emitComments();
            newComments.emitXMLFooter();
        } catch (IOException e) {
            System.out.println("IO Error while attempting to create " + outputFileName);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
        return true;
    }

    /**
     * Emit messages about which comments are now unused and which are new.
     */
    public static void noteDifferences(Comments oldComments, Comments newComments) {
        if (oldComments == null) {
            System.out.println("Note: all the comments have been newly generated");
            return;
        }

        // See which comment ids are no longer used and add those entries to
        // the new comments, marking them as unused.
        for (SingleComment oldComment : oldComments.commentsList_) {
            int idx = Collections.binarySearch(newComments.commentsList_, oldComment);
            if (idx < 0) {
                System.out.println("Warning: comment \"" + oldComment.id_ + "\" is no longer used.");
                oldComment.isUsed_ = false;
                newComments.commentsList_.add(oldComment);
            }
        }

    }

    /**
     * Return true if the given HTML tag has no separate </tag> end element.
     * <p>
     * If you want to be able to use sloppy HTML in your comments, then you can
     * add the element, e.g. li back into the condition here. However, if you
     * then become more careful and do provide the closing tag, the output is
     * generally just the closing tag, which is incorrect.
     * <p>
     * tag.equalsIgnoreCase("tr") || // Is sometimes minimized
     * tag.equalsIgnoreCase("th") || // Is sometimes minimized
     * tag.equalsIgnoreCase("td") || // Is sometimes minimized
     * tag.equalsIgnoreCase("dt") || // Is sometimes minimized
     * tag.equalsIgnoreCase("dd") || // Is sometimes minimized
     * tag.equalsIgnoreCase("img") || // Is sometimes minimized
     * tag.equalsIgnoreCase("code") || // Is sometimes minimized (error)
     * tag.equalsIgnoreCase("font") || // Is sometimes minimized (error)
     * tag.equalsIgnoreCase("ul") || // Is sometimes minimized
     * tag.equalsIgnoreCase("ol") || // Is sometimes minimized
     * tag.equalsIgnoreCase("li") // Is sometimes minimized
     */
    public static boolean isMinimizedTag(String tag) {
        return tag.equalsIgnoreCase("p") ||
                tag.equalsIgnoreCase("br") ||
                tag.equalsIgnoreCase("hr");
    }

    /**
     * Add the SingleComment object to the list of comments kept by this
     * object.
     */
    public void addComment(SingleComment comment) {
        commentsList_.add(comment);
    }

    /**
     * Write the Comments object out in XML.
     */
    public void emitComments() {
        for (SingleComment currComment : commentsList_) {
            if (!currComment.isUsed_)
                outputFile.println("<!-- This comment is no longer used ");
            outputFile.println("<comment>");
            outputFile.println("  <identifier id=\"" + currComment.id_ + "\"/>");
            outputFile.println("  <text>");
            outputFile.println("    " + currComment.text_);
            outputFile.println("  </text>");
            outputFile.println("</comment>");
            if (!currComment.isUsed_)
                outputFile.println("-->");
        }
    }

    /**
     * Dump the contents of a Comments object out for inspection.
     */
    public void dump() {
        Iterator<SingleComment> iter = commentsList_.iterator();
        int i = 0;
        while (iter.hasNext()) {
            i++;
            SingleComment currComment = (iter.next());
            System.out.println("Comment " + i);
            System.out.println("id = " + currComment.id_);
            System.out.println("text = \"" + currComment.text_ + "\"");
            System.out.println("isUsed = " + currComment.isUsed_);
        }
    }

    /**
     * Emit the XML header.
     */
    public void emitXMLHeader(String filename) {
        outputFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
        outputFile.println("<comments");
        outputFile.println("  xmlns:xsi='" + RootDocToXML.baseURI + "/2001/XMLSchema-instance'");
        outputFile.println("  xsi:noNamespaceSchemaLocation='comments.xsd'");
        // Extract the identifier from the filename by removing the suffix
        int idx = filename.lastIndexOf('.');
        String apiIdentifier = filename.substring(0, idx);
        // Also remove the output directory and directory separator if present
        if (HTMLReportGenerator.outputDir != null)
            apiIdentifier = apiIdentifier.substring(HTMLReportGenerator.outputDir.length() + 1);
        // Also remove "user_comments_for_"
        apiIdentifier = apiIdentifier.substring(18);
        outputFile.println("  name=\"" + apiIdentifier + "\"");
        outputFile.println("  jdversion=\"" + JDiff.version + "\">");
        outputFile.println();
        outputFile.println("<!-- This file contains comments for a JDiff report. -->");
        outputFile.println("<!-- It is used only in generating the report, and does not need to ship with the final report. -->");
        outputFile.println();
        outputFile.println("<!-- The id attribute in an identifier element identifies the change as noted in the report. -->");
        outputFile.println("<!-- An id has the form package[.class[.[ctor|method|field].signature]], where [] indicates optional text. -->");
        outputFile.println("<!-- A comment element can have multiple identifier elements, which will -->");
        outputFile.println("<!-- will cause the same text to appear at each place in the report, but -->");
        outputFile.println("<!-- will be converted to separate comments when the comments file is used. -->");
        outputFile.println("<!-- HTML tags in the text field will appear in the report. -->");
        outputFile.println("<!-- You also need to close p HTML elements, used for paragraphs - see the top-level documentation. -->");
    }

    /**
     * Emit the XML footer.
     */
    public void emitXMLFooter() {
        outputFile.println();
        outputFile.println("</comments>");
    }

}


