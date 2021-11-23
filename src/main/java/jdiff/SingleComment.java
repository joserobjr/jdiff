package jdiff;

/**
 * Represents a single comment element. Has an identifier and some text.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class SingleComment implements Comparable<SingleComment> {

    /**
     * The identifier for this comment.
     */
    public String id_;

    /**
     * The text of this comment.
     */
    public String text_;

    /**
     * If false, then this comment is inactive.
     */
    public boolean isUsed_ = true;

    public SingleComment(String id, String text) {
        // Escape the commentID in case it contains "<" or ">"
        // characters (generics)
        id_ = id.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        text_ = text;
    }

    /**
     * Compare two SingleComment objects using just the id.
     */
    public int compareTo(SingleComment o) {
        return id_.compareTo(o.id_);
    }
}
