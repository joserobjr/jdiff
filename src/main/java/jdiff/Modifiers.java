package jdiff;

/**
 * Track the various modifiers for a program element.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this set of modifiers.
 * <p>
 * See the file LICENSE.txt for copyright details.
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
