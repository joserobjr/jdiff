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
 * Track the various modifiers for a program element.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this set of modifiers.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class Modifiers implements Comparable<Modifiers> {

    /**
     * Set if the program element is static.
     */
    public boolean isStatic;

    /**
     * Set if the program element is final.
     */
    public boolean isFinal;

    /**
     * Set if the program element is deprecated.
     */
    public boolean isDeprecated;

    /**
     * The visibility level; "public", "protected", "package" or
     * "private"
     */
    public String visibility;

    /**
     * Default constructor.
     */
    public Modifiers() {
    }

    /**
     * Compare two Modifiers objects by their contents.
     */
    public int compareTo(Modifiers o) {
        if (isStatic != o.isStatic)
            return -1;
        if (isFinal != o.isFinal)
            return -1;
        if (isDeprecated != o.isDeprecated)
            return -1;
        if (visibility != null) {
            return visibility.compareTo(o.visibility);
        }
        return 0;
    }

    /**
     * Generate a String describing the differences between the current
     * (old) Modifiers object and a new Modifiers object. The string has
     * no leading space, but does end in a period.
     *
     * @param newModifiers The new Modifiers object.
     * @return The description of the differences, null if there is no change.
     */
    public String diff(Modifiers newModifiers) {
        String res = "";
        boolean hasContent = false;
        if (isStatic != newModifiers.isStatic) {
            res += "Change from ";
            if (isStatic)
                res += "static to non-static.<br>";
            else
                res += "non-static to static.<br>";
            hasContent = true;
        }
        if (isFinal != newModifiers.isFinal) {
            if (hasContent)
                res += " ";
            res += "Change from ";
            if (isFinal)
                res += "final to non-final.<br>";
            else
                res += "non-final to final.<br>";
            hasContent = true;
        }
        if (!HTMLReportGenerator.incompatibleChangesOnly &&
                isDeprecated != newModifiers.isDeprecated) {
            if (hasContent)
                res += " ";
            if (isDeprecated)
                res += "Change from deprecated to undeprecated.<br>";
            else
                res += "<b>Now deprecated</b>.<br>";
            hasContent = true;
        }
        if (visibility != null) {
            int comp = visibility.compareTo(newModifiers.visibility);
            if (comp != 0) {
                if (hasContent)
                    res += " ";
                res += "Change of visibility from " + visibility + " to " +
                        newModifiers.visibility + ".<br>";
                hasContent = true;
            }
        }
        if (res.compareTo("") == 0)
            return null;
        return res;
    }
}
