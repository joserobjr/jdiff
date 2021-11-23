package jdiff;

import java.util.Comparator;

/**
 * Class to compare two PackageDiff objects.
 * <p>
 * See the file LICENSE.txt for copyright details.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
class ComparePkgPdiffs implements Comparator<PackageDiff> {
    /**
     * Compare two package diffs by their percentage difference,
     * and then by name.
     */
    public int compare(PackageDiff obj1, PackageDiff obj2) {
        if (obj1.pdiff < obj2.pdiff)
            return 1;
        if (obj1.pdiff > obj2.pdiff)
            return -1;
        return obj1.name_.compareTo(obj2.name_);
    }
}
