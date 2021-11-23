/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2004  Matthew Doar <mdoar@pobox.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jdiff;

/**
 * Represents a single comment element. Has an identifier and some text.
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
