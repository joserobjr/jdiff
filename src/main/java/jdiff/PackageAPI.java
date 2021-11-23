package jdiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a package, analogous to PackageDoc in the
 * Javadoc doclet API.
 * <p>
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this package.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class PackageAPI implements Comparable<PackageAPI> {

    /**
     * Full qualified name of the package.
     */
    public String name_;

    /**
     * Classes within this package.
     */
    public List<ClassAPI> classes_;  // ClassAPI[]

    /**
     * The doc block, default is null.
     */
    public String doc_ = null;

    /**
     * Constructor.
     */
    public PackageAPI(String name) {
        name_ = name;
        classes_ = new ArrayList<>(); // ClassAPI[]
    }

    /**
     * Compare two PackageAPI objects by name.
     */
    public int compareTo(PackageAPI o) {
        if (APIComparator.docChanged(doc_, o.doc_))
            return -1;
        return name_.compareTo(o.name_);
    }

    /**
     * Tests two packages, using just the package name, used by indexOf().
     */
    public boolean equals(Object o) {
        return o instanceof PackageAPI && name_.compareTo(((PackageAPI) o).name_) == 0;
    }
}
