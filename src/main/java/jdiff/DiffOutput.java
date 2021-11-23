package jdiff;

/**
 * Class to represent a single documentation difference.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class DiffOutput implements Comparable<DiffOutput> {

    /**
     * The package name for this difference.
     */
    public String pkgName_;

    /**
     * The class name for this difference, may be null.
     */
    public String className_;

    /**
     * The HTML named anchor identifier for this difference.
     */
    public String id_;

    /**
     * The title for this difference.
     */
    public String title_;

    /**
     * The text for this difference, with deleted and added words marked.
     */
    public String text_;

    /**
     * Constructor.
     */
    public DiffOutput(String pkgName, String className, String id,
                      String title, String text) {
        pkgName_ = pkgName;
        className_ = className;
        id_ = id;
        title_ = title;
        text_ = text;
    }

    /**
     * Compare two DiffOutput objects, so they will appear in the correct
     * package.
     */
    public int compareTo(DiffOutput o) {
        int comp = pkgName_.compareTo(o.pkgName_);
        if (comp != 0)
            return comp;
        // Always put the package-level output at the top - not yet working
//        if (id_.compareTo("package") == 0)
//            return -1;
        return id_.compareTo(o.id_);
    }

}  
