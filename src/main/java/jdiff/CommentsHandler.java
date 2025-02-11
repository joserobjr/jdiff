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

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Handle the parsing of an XML file and the generation of a Comments object.
 * <p>
 * All HTML written for the comments sections in the report must
 * use tags such as &lt;p/&gt; rather than just &lt;p&gt;, since the XML
 * parser used requires that or matching end elements.
 * <p>
 * From http://www.w3.org/TR/2000/REC-xhtml1-20000126:
 * "Empty elements must either have an end tag or the start tag must end with /&lt;".
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class CommentsHandler extends DefaultHandler {

    /**
     * Set to enable increased logging verbosity for debugging.
     */
    private static final boolean trace = false;

    /**
     * The stack of SingleComments still waiting for comment text.
     */
    private final LinkedList<String> tagStack = new LinkedList<>();

    /**
     * The Comments object which is populated from the XML file.
     */
    public Comments comments_;

    /**
     * The current SingleComment object being populated.
     */
    private List<SingleComment> currSingleComment_; // SingleComment[]

    /**
     * Set if in text.
     */
    private boolean inText;

    /**
     * The current text which is being assembled from chunks.
     */
    private String currentText;

    /**
     * Default constructor.
     */
    public CommentsHandler(Comments comments) {
        comments_ = comments;
    }

    public void startDocument() {
    }

    public void endDocument() {
        if (trace)
            comments_.dump();
    }

    public void startElement(java.lang.String uri, java.lang.String localName,
                             java.lang.String qName, Attributes attributes) {
        // The change to JAXP compliance produced this change.
        if (localName.equals(""))
            localName = qName;
        if (localName.compareTo("comments") == 0) {
            String commentsName = attributes.getValue("name");
            //String version = attributes.getValue("jdversion"); // Not used yet
            if (commentsName == null) {
                System.out.println("Error: no identifier found in the comments XML file.");
                System.exit(3);
            }
            // Check the given names against the names of the APIs
            int idx1 = JDiff.oldFileName.lastIndexOf('.');
            int idx2 = JDiff.newFileName.lastIndexOf('.');
            String filename2 = JDiff.oldFileName.substring(0, idx1) +
                    "_to_" + JDiff.newFileName.substring(0, idx2);
            if (filename2.compareTo(commentsName) != 0) {
                System.out.println("Warning: API identifier in the comments XML file (" + filename2 + ") differs from the name of the file.");
            }
        } else if (localName.compareTo("comment") == 0) {
            currSingleComment_ = new ArrayList<>(); // SingleComment[];
        } else if (localName.compareTo("identifier") == 0) {
            // May have multiple identifiers for one comment's text
            String id = attributes.getValue("id");
            SingleComment newComment = new SingleComment(id, null);
            // Store it here until we can add text to it
            currSingleComment_.add(newComment);
        } else if (localName.compareTo("text") == 0) {
            inText = true;
            currentText = null;
        } else {
            if (inText) {
                // Start of an element, probably an HTML element
                addStartTagToText(localName, attributes);
            } else {
                System.out.println("Error: unknown element type: " + localName);
                System.exit(-1);
            }
        }
    }

    public void endElement(java.lang.String uri, java.lang.String localName,
                           java.lang.String qName) {
        if (localName.equals(""))
            localName = qName;
        if (localName.compareTo("text") == 0) {
            inText = false;
            addTextToComments();
        } else if (inText) {
            addEndTagToText(localName);
        }

    }

    /**
     * Deal with a chunk of text. The text may come in multiple chunks.
     */
    public void characters(char[] ch, int start, int length) {
        if (inText) {
            String chunk = new String(ch, start, length);
            if (currentText == null)
                currentText = chunk;
            else
                currentText += chunk;
        }
    }

    /**
     * Trim the current text, check it is a sentence and add it to all
     * the comments which are waiting for it.
     */
    public void addTextToComments() {
        // Eliminate any whitespace at each end of the text.
        currentText = currentText.trim();
        // Check that it is a sentence
        if (!currentText.endsWith(".") &&
                !currentText.endsWith("?") &&
                !currentText.endsWith("!") &&
                currentText.compareTo(Comments.placeHolderText) != 0) {
            System.out.println("Warning: text of comment does not end in a period: " + currentText);
        }
        // Add this comment to all the SingleComments waiting for it
        for (SingleComment currComment : currSingleComment_) {
            if (currComment.text_ == null)
                currComment.text_ = currentText;
            else
                currComment.text_ += currentText;
            comments_.addComment(currComment);
        }
    }

    /**
     * Add the start tag to the current comment text.
     */
    public void addStartTagToText(String localName, Attributes attributes) {
        // Need to insert the HTML tag into the current text
        // Save the tag in a stack
        tagStack.add(localName);
        StringBuilder tag = new StringBuilder("<").append(localName);
        // Now add all the attributes into the current text
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            String name = attributes.getLocalName(i);
            String value = attributes.getValue(i);
            tag.append(" ").append(name).append("=\"").append(value).append("\"");
        }

        // End the tag
        if (Comments.isMinimizedTag(localName)) {
            tag.append("/>");
        } else {
            tag.append(">");
        }
        // Now insert the HTML tag into the current text
        if (currentText == null)
            currentText = tag.toString();
        else
            currentText += tag;
    }

    /**
     * Add the end tag to the current comment text.
     */
    public void addEndTagToText(@SuppressWarnings("unused") String localName) {
        // Close the current HTML tag
        String currentHTMLTag = (tagStack.removeLast());
        if (!Comments.isMinimizedTag(currentHTMLTag))
            currentText += "</" + currentHTMLTag + ">";
    }

    public void warning(SAXParseException e) {
        System.out.println("Warning (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
    }

    public void error(SAXParseException e) {
        System.out.println("Error (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
        System.exit(1);
    }

    public void fatalError(SAXParseException e) {
        System.out.println("Fatal Error (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
        System.exit(1);
    }

}

