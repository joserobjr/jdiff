package jdiff;

import java.util.Comparator;

/**
 * Class to compare two ClassDiff objects.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class CompareClassPdiffs implements Comparator<ClassDiff> {
    /**
     * Compare two class diffs by their percentage difference,
     * and then by name.
     */
    public int compare(ClassDiff obj1, ClassDiff obj2) {
        if (obj1.pdiff < obj2.pdiff)
            return 1;
        if (obj1.pdiff > obj2.pdiff)
            return -1;
        return obj1.name_.compareTo(obj2.name_);
    }
}
